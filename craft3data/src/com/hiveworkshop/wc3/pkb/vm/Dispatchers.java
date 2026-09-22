package com.hiveworkshop.wc3.pkb.vm;

import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.pkb.bind.EventPayloadDecl;
import com.hiveworkshop.wc3.pkb.bind.ExternalBinding;
import com.hiveworkshop.wc3.pkb.bind.FunctionBinding;
import com.hiveworkshop.wc3.pkb.bind.SamplerResource;
import com.hiveworkshop.wc3.pkb.bind.SpatialLayerResource;
import com.hiveworkshop.wc3.pkb.sim.ProximityHash;
import com.hiveworkshop.wc3.pkb.sim.SpawnEvent;

/**
 * Native functions the bytecode calls through FunctionCall: random numbers,
 * effect/scene queries, event generation and kicking, samplers, spatial
 * layers, transforms and shapes.
 */
public final class Dispatchers {
	private static final float TWO_PI = 6.28318530717958647692f;
	private static final int POSITION_NAME_ID = EventPayloadDecl.payloadNameId("Position");
	private static final int ORIENTATION_NAME_ID = EventPayloadDecl.payloadNameId("Orientation");
	private static final int INF_BITS = 0x7F800000;

	private final Interpreter interpreter;
	private final RegisterValue r0 = new RegisterValue();
	private final RegisterValue r1 = new RegisterValue();
	private final RegisterValue r2 = new RegisterValue();
	private final RegisterValue r3 = new RegisterValue();
	private final RegisterValue r4 = new RegisterValue();
	private final RegisterValue r5 = new RegisterValue();
	private final RegisterValue out = new RegisterValue();
	private final float[] v3 = new float[3];
	private final float[] v3b = new float[3];
	private final float[] v3c = new float[3];
	private final float[] f4 = new float[4];
	private final Set<String> stubbed = new HashSet<>();

	Dispatchers(final Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	// ------------------------------------------------------------------ argument helpers

	private static void readFnArg(final Instruction ins, final int i, final ExecContext ctx, final RegisterValue out) {
		final int argRegIndex = (i * 2) + 1;
		if (argRegIndex >= ins.extraOperands.length) {
			throw new VmException("IR: FunctionCall arg index out of range");
		}
		Interpreter.readSrc(ctx, ins.extraOperands[argRegIndex], out);
	}

	private static boolean isHandleArg(final int reg) {
		return (reg != Bank.REG_VOID) && (Bank.bankOf(reg) == Bank.HANDLE);
	}

	private static int valueArgCount(final Instruction ins) {
		int count = 0;
		for (int i = 0; i < ins.argc(); i++) {
			final int idx = (i * 2) + 1;
			if ((idx < ins.extraOperands.length) && !isHandleArg(ins.extraOperands[idx])) {
				count++;
			}
		}
		return count;
	}

	private static boolean readValueArg(final Instruction ins, final int n, final ExecContext ctx,
			final RegisterValue out) {
		int seen = 0;
		for (int i = 0; i < ins.argc(); i++) {
			final int idx = (i * 2) + 1;
			if ((idx >= ins.extraOperands.length) || isHandleArg(ins.extraOperands[idx])) {
				continue;
			}
			if (seen == n) {
				Interpreter.readSrc(ctx, ins.extraOperands[idx], out);
				return true;
			}
			seen++;
		}
		return false;
	}

	private float valueArgScalar(final Instruction ins, final int idx, final ExecContext ctx, final float fallback) {
		if (!readValueArg(ins, idx, ctx, r5)) {
			return fallback;
		}
		return r5.lanes[0];
	}

	/** Object slot of the call: the function's symbol slot, else the instruction's objSlot. */
	private static int objectSlot(final Instruction ins, final ExecContext ctx) {
		final int extFunc = ins.operands[2];
		if (extFunc >= ctx.functions.length) {
			return FunctionBinding.SYMBOL_SLOT_UNBOUND;
		}
		int slot = ctx.functions[extFunc].symbolSlot;
		if (slot == FunctionBinding.SYMBOL_SLOT_UNBOUND) {
			slot = ins.operands[1];
		}
		return slot;
	}

	/** Reads the event id stored in the external the call's object slot names; -1 when unresolved. */
	private static long resolveKickEventIdFromObjSlot(final Instruction ins, final ExecContext ctx) {
		final int objSlot = objectSlot(ins, ctx);
		if (objSlot == FunctionBinding.SYMBOL_SLOT_UNBOUND) {
			return -1;
		}
		final ExternalBinding b = ExternalBinding.findBySlot(ctx.externalBindings, objSlot & 0xFFFF);
		if (b == null) {
			return -1;
		}
		final int canonical = b.resolvedSlot();
		if (canonical >= ctx.externals.size()) {
			return -1;
		}
		return ctx.externals.laneBits(canonical, 0) & 0xFFFFFFFFL;
	}

	private static String resolveEventChannelName(final Instruction ins, final ExecContext ctx) {
		final int extFunc = ins.operands[2];
		if (extFunc >= ctx.functions.length) {
			return "";
		}
		final int symSlot = ctx.functions[extFunc].symbolSlot;
		if (symSlot == FunctionBinding.SYMBOL_SLOT_UNBOUND) {
			return "";
		}
		final ExternalBinding b = ExternalBinding.findBySlot(ctx.externalBindings, symSlot & 0xFFFF);
		return b == null ? "" : b.name;
	}

	public static SamplerResource resolveTargetSampler(final Instruction ins, final ExecContext ctx) {
		final int extSlot = objectSlot(ins, ctx);
		if (extSlot == FunctionBinding.SYMBOL_SLOT_UNBOUND) {
			return null;
		}
		final ExternalBinding b = ExternalBinding.findBySlot(ctx.externalBindings, extSlot & 0xFFFF);
		if ((b == null) || b.name.isEmpty()) {
			return null;
		}
		return SamplerResource.findByName(ctx.samplers, b.name);
	}

	private static int resolveSpatialLayerIndex(final Instruction ins, final ExecContext ctx) {
		final int extFunc = ins.operands[2];
		if (extFunc >= ctx.functions.length) {
			return -1;
		}
		final int extSlot = ctx.functions[extFunc].symbolSlot;
		if (extSlot == FunctionBinding.SYMBOL_SLOT_UNBOUND) {
			return -1;
		}
		final ExternalBinding b = ExternalBinding.findBySlot(ctx.externalBindings, extSlot & 0xFFFF);
		if ((b == null) || b.name.isEmpty()) {
			return -1;
		}
		for (int i = 0; i < ctx.spatialLayers.length; i++) {
			if (ctx.spatialLayers[i].name.equals(b.name)) {
				return i;
			}
		}
		return -1;
	}

	private static SamplerResource resolveHandleArgSampler(final Instruction ins, final ExecContext ctx,
			final int kind) {
		for (int i = 0; i < ins.argc(); i++) {
			final int idx = (i * 2) + 1;
			if (idx >= ins.extraOperands.length) {
				break;
			}
			final int reg = ins.extraOperands[idx];
			if (!isHandleArg(reg)) {
				continue;
			}
			for (int h = 0; h < ctx.handleRegisterCount; h++) {
				if (ctx.handleRegisterSlots[h].reg != reg) {
					continue;
				}
				final int slot = ctx.handleRegisterSlots[h].slot;
				if (slot < ctx.externalBindings.length) {
					final SamplerResource res = SamplerResource.findByName(ctx.samplers,
							ctx.externalBindings[slot].name);
					if ((res != null) && (res.kind == kind)) {
						return res;
					}
				}
				break;
			}
		}
		return null;
	}

	// ------------------------------------------------------------------ entry point

	public void execFunctionCall(final Instruction ins, final ExecContext ctx) {
		final int extFunc = ins.operands[2];
		final int retReg = ins.operands[4];
		String symbol = "";
		String canon = "";
		if (extFunc < ctx.functions.length) {
			symbol = ctx.functions[extFunc].symbolName;
			canon = ctx.functions[extFunc].canonicalName;
		}
		if (canon.isEmpty()) {
			canon = FunctionBinding.canonicalizeSymbol(symbol);
		}
		out.clear();
		out.componentCount = 1;
		out.typeBank = Bank.FLOAT;
		final int dispatched = dispatch(canon, ins, ctx);
		if (dispatched == UNHANDLED) {
			stubFunctionCall(symbol, ins, ctx);
			return;
		}
		if (dispatched == FAILED) {
			throw new VmException("IR: FunctionCall failed: " + symbol);
		}
		if (retReg == Bank.REG_VOID) {
			return;
		}
		final int bank = Bank.bankOf(retReg);
		out.typeBank = bank;
		if (out.componentCount == 0) {
			out.componentCount = Bank.componentCountForBank(bank);
		}
		Interpreter.writeDst(ctx, retReg, out);
	}

	private static final int OK = 1;
	private static final int FAILED = 0;
	private static final int UNHANDLED = -1;

	private static int fatal(final boolean ok) {
		return ok ? OK : FAILED;
	}

	private static int stub(final boolean ok) {
		return ok ? OK : UNHANDLED;
	}

	private void stubFunctionCall(final String symbol, final Instruction ins, final ExecContext ctx) {
		final String name = symbol.isEmpty() ? "(unresolved)" : symbol;
		if (stubbed.add(name)) {
			System.err.println("[pkb vm] FunctionCall stub: " + name);
		}
		final int retReg = ins.operands[4];
		if (retReg == Bank.REG_VOID) {
			return;
		}
		final int bank = Bank.bankOf(retReg);
		out.clear();
		out.componentCount = Bank.componentCountForBank(bank);
		out.typeBank = bank;
		Interpreter.writeDst(ctx, retReg, out);
	}

	private int dispatch(final String canon, final Instruction ins, final ExecContext ctx) {
		// ---- specials
		switch (canon) {
		case "rotate":
		case "radians.rotate":
			return fatal(ins.argc() >= 3 ? rotateAxisAngle(ins, ctx) : rotateOrientation(ins, ctx));
		case "orientation_axisSide":
			return fatal(orientationAxis(ins, ctx, 0));
		case "orientation_axisUp":
			return fatal(orientationAxis(ins, ctx, 1));
		case "orientation_axisForward":
			return fatal(orientationAxis(ins, ctx, 2));
		case "effect.axisSide":
			return fatal(effectAxis(ctx, 0, 1));
		case "effect.axisVertical":
			return fatal(effectAxis(ctx, 2, 1));
		case "effect.axisDepth":
			return fatal(effectAxis(ctx, 1, 1));
		default:
			break;
		}
		if (canon.startsWith("view.axis")) {
			for (int i = 0; i < 6; i++) {
				if (canon.equals("view." + ShapeSampling.AXIS_NAMES[i])) {
					return fatal(viewAxis(ins, ctx, ShapeSampling.ABSOLUTE_AXIS_COLUMN[i],
							ShapeSampling.ABSOLUTE_AXIS_SIGN[i]));
				}
			}
		}
		if (canon.startsWith("effect.axis")) {
			for (int i = 0; i < 6; i++) {
				if (canon.equals("effect." + ShapeSampling.AXIS_NAMES[i])) {
					return fatal(effectAxis(ctx, ShapeSampling.ABSOLUTE_AXIS_COLUMN[i],
							ShapeSampling.ABSOLUTE_AXIS_SIGN[i]));
				}
			}
		}
		// ---- exact table
		switch (canon) {
		case "rand":
			return fatal(rand(ins, ctx));
		case "vrand":
			return fatal(vrand(ins, ctx));
		case "effect.age": {
			final float age = ctx.effectAge - ctx.timeWindowEnd;
			out.setScalar(age > 0 ? age : 0);
			return OK;
		}
		case "effect.isRunning":
			out.setScalarI(ctx.effectIsRunning ? -1 : 0);
			return OK;
		case "effect.isRenderingEnabled":
			out.setScalarI(-1);
			return OK;
		case "effect.isTeleporting":
			out.setScalarI(0);
			return OK;
		case "effect.position":
			out.setFloat3(ctx.sceneL2W.m[0][3], ctx.sceneL2W.m[1][3], ctx.sceneL2W.m[2][3]);
			return OK;
		case "sim.lod": {
			final float v = ctx.simLod + ctx.simLodBias;
			out.setScalar(Math.min(1, Math.max(0, v)));
			return OK;
		}
		case "sim.lodBias":
			out.setScalar(ctx.simLodBias);
			return OK;
		case "sim.lodDistanceMin":
			out.setScalar(ctx.simLodDistanceMin);
			return OK;
		case "sim.lodDistanceMax":
			out.setScalar(ctx.simLodDistanceMax);
			return OK;
		case "duration":
			out.setScalar(1.0e6f);
			return OK;
		case "self.kill":
			return fatal(selfKill(ins, ctx));
		case "generate":
			return fatal(generate(ins, ctx));
		case "trigger":
			return fatal(trigger(ins, ctx));
		case "initPayload":
			return fatal(initPayload(ins, ctx));
		case "kick":
			return fatal(kick(ins, ctx));
		case "hasPayloadElement":
			out.setScalarI(ctx.currentSelfId != 0 ? -1 : 0);
			return OK;
		case "sample":
			return stub(sample(ins, ctx));
		case "sampleCDF":
			return stub(sampleCdf(ins, ctx));
		case "dimensions":
			return stub(textureDimensions(ins, ctx));
		case "atlasRectCount":
			return stub(textureAtlasRectCount(ins, ctx));
		case "samplePosition":
			return stub(samplePosition(ins, ctx));
		case "sampleNormal":
			return stub(sampleNormal(ins, ctx));
		case "sampleTangent":
			return stub(primitiveClearedChannel(ins, ctx, Bank.FLOAT4, 4));
		case "sampleVelocity":
			return stub(primitiveClearedChannel(ins, ctx, Bank.FLOAT3, 3));
		case "projectPCoords":
			return stub(projectPCoords(ins, ctx));
		case "xform_l2w_f_masked":
			return fatal(xformL2W(ins, ctx, true));
		case "xform_l2w_d_masked":
			return fatal(xformL2W(ins, ctx, false));
		case "xform_w2l_f_masked":
			return fatal(xformW2L(ins, ctx, true));
		case "xform_w2l_d_masked":
			return fatal(xformW2L(ins, ctx, false));
		case "allocatePayload": {
			final int key = ctx.simUnitScratchCounter++;
			out.setScalarI(key);
			return OK;
		}
		case "insert":
			return fatal(spatialInsert(ins, ctx));
		case "neighborCount":
		case "neighborCount2":
			return fatal(spatialNeighborCount(ins, ctx));
		case "hsv2rgb":
			return fatal(hsv2rgb(ins, ctx));
		case "rgb2hsv":
			return fatal(rgb2hsv(ins, ctx));
		case "orientation_mult":
			return fatal(orientationMult(ins, ctx));
		case "noise":
			return stub(noise(ins, ctx));
		case "view.count":
			writeInt(out, ctx.cameras.length);
			return OK;
		case "view.resolution":
			return fatal(viewResolution(ins, ctx));
		case "view.aspect":
			return fatal(viewAspect(ins, ctx));
		case "view.position":
			return fatal(viewPosition(ins, ctx));
		case "view.distance":
			return fatal(viewDistance(ins, ctx));
		case "view.direction":
			return fatal(viewAxis(ins, ctx, 1, 1));
		case "contains":
			return stub(shapeContains(ins, ctx));
		case "sampleDistanceField":
			return stub(shapeDistanceField(ins, ctx));
		case "project":
			return stub(shapeProject(ins, ctx));
		case "intersect":
			return stub(shapeIntersect(ins, ctx));
		case "samplePCoords":
			return stub(samplePCoords(ins, ctx));
		case "sampleSurfacePCoordsFromUV":
			return stub(sampleSurfacePCoordsFromUV(ins, ctx));
		case "intersectPCoords":
			return stub(shapeIntersectPCoords(ins, ctx));
		default:
			break;
		}
		// ---- prefix table
		if (canon.startsWith("buildPayloadElement")) {
			return fatal(buildPayloadElement(ins, ctx));
		}
		if (canon.startsWith("appendPayload")) {
			return fatal(appendPayload(ins, ctx));
		}
		if (canon.startsWith("scene.intersect")) {
			out.setFloat4(0, 0, 0, 0);
			return OK;
		}
		// ---- symbol table
		if (ShapeSampling.isGetterName(canon)) {
			final SamplerResource res = resolveTargetSampler(ins, ctx);
			if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)) {
				return UNHANDLED;
			}
			return stub(ShapeSampling.getter(res.shape, canon, out));
		}
		if (canon.startsWith("shape.buildPCoords")) {
			return stub(buildPCoords(canon, ins, ctx));
		}
		if (canon.startsWith("extractPayloadElement")) {
			return fatal(extractPayloadElement(ins, ctx, canon));
		}
		if (canon.startsWith("scene.orientation")) {
			return fatal(sceneOrientation(ins, ctx));
		}
		if (canon.startsWith("closest")) {
			return fatal(spatialClosest(ins, ctx, canon));
		}
		if (canon.startsWith("sumKernel") || canon.startsWith("averageKernel") || canon.startsWith("sum")
				|| canon.startsWith("average")) {
			return stub(spatialReduce(ins, ctx, canon));
		}
		if (canon.startsWith("sampleTexcoord") || canon.startsWith("sampleColor")) {
			return stub(sampleChannelSym(ins, ctx, canon));
		}
		return UNHANDLED;
	}

	private static void writeInt(final RegisterValue out, final int v) {
		out.clear();
		out.componentCount = 1;
		out.typeBank = Bank.PTR;
		out.setLaneInt(0, v);
	}

	private static void writeInt2(final RegisterValue out, final int x, final int y) {
		out.clear();
		out.componentCount = 2;
		out.typeBank = Bank.INT2_ALT;
		out.setLaneInt(0, x);
		out.setLaneInt(1, y);
	}

	// ------------------------------------------------------------------ random

	private boolean rand(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 2) {
			throw new VmException("IR: rand requires at least 2 args");
		}
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		final int retReg = ins.operands[4];
		int outComponents;
		if (retReg != Bank.REG_VOID) {
			outComponents = Bank.componentCountForBank(Bank.bankOf(retReg));
		} else {
			outComponents = Math.max(r0.componentCount, r1.componentCount);
		}
		if (outComponents == 0) {
			outComponents = 1;
		}
		out.clear();
		out.componentCount = outComponents;
		out.typeBank = Bank.floatBankForComponentCount(outComponents);
		for (int i = 0; i < outComponents; i++) {
			final float t12 = ctx.rng == null ? 1.0f : ctx.rng.unit12();
			final float a = r0.lanes[i];
			final float b = r1.lanes[i];
			final float d = b - a;
			out.lanes[i] = (t12 * d) + (a - d);
		}
		return true;
	}

	private boolean vrand(final Instruction ins, final ExecContext ctx) {
		final float uPhi = ctx.rng == null ? 0 : ctx.rng.unit();
		final float uCos = ctx.rng == null ? 0 : ctx.rng.unit();
		final float phi = uPhi * TWO_PI;
		final float cosTheta = 1.0f - (2.0f * uCos);
		final float sinTheta = (float) Math.sqrt(Math.max(0, 1.0f - (cosTheta * cosTheta)));
		float r = 1.0f;
		if (ins.argc() >= 2) {
			readFnArg(ins, 0, ctx, r0);
			readFnArg(ins, 1, ctx, r1);
			final float a = Math.max(0, r0.lanes[0]);
			final float b = Math.max(0, r1.lanes[0]);
			final float rmax = Math.max(a, b);
			final float rmin = Math.min(a, b);
			final float uR = ctx.rng == null ? 0 : ctx.rng.unit();
			final float r3min = rmin * rmin * rmin;
			final float r3max = rmax * rmax * rmax;
			r = (float) Math.cbrt(r3min + ((r3max - r3min) * uR));
		}
		out.setFloat3(sinTheta * (float) Math.cos(phi) * r, cosTheta * r, sinTheta * (float) Math.sin(phi) * r);
		return true;
	}

	// ------------------------------------------------------------------ colour

	private static float frac(final float x) {
		return x - (float) Math.floor(x);
	}

	private static float clamp01(final float x) {
		return Math.min(1, Math.max(0, x));
	}

	private boolean hsv2rgb(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 1) {
			throw new VmException("IR: hsv2rgb requires 1 vec3 arg");
		}
		readFnArg(ins, 0, ctx, r0);
		final float h = r0.lanes[0];
		final float s = clamp01(r0.lanes[1]);
		final float v = Math.max(0, r0.lanes[2]);
		final float px = Math.abs((frac(h + 1.0f) * 6.0f) - 3.0f);
		final float py = Math.abs((frac(h + (2.0f / 3.0f)) * 6.0f) - 3.0f);
		final float pz = Math.abs((frac(h + (1.0f / 3.0f)) * 6.0f) - 3.0f);
		final float rx = clamp01(px - 1.0f);
		final float ry = clamp01(py - 1.0f);
		final float rz = clamp01(pz - 1.0f);
		out.setFloat4(v * (1.0f + ((rx - 1.0f) * s)), v * (1.0f + ((ry - 1.0f) * s)), v * (1.0f + ((rz - 1.0f) * s)),
				r0.componentCount >= 4 ? r0.lanes[3] : 1.0f);
		return true;
	}

	private boolean rgb2hsv(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 1) {
			throw new VmException("IR: rgb2hsv requires 1 vec3 arg");
		}
		readFnArg(ins, 0, ctx, r0);
		final float r = r0.lanes[0];
		final float g = r0.lanes[1];
		final float b = r0.lanes[2];
		final float vMax = Math.max(r, Math.max(g, b));
		final float vMin = Math.min(r, Math.min(g, b));
		final float d = vMax - vMin;
		float h = 0;
		float s = 0;
		final float v = vMax;
		if (d > 1.0e-6f) {
			final float invD = 1.0f / d;
			if (vMax == r) {
				h = (g - b) * invD;
				if (h < 0) {
					h += 6.0f;
				}
			} else if (vMax == g) {
				h = 2.0f + ((b - r) * invD);
			} else {
				h = 4.0f + ((r - g) * invD);
			}
			h /= 6.0f;
			s = d / vMax;
		}
		out.setFloat4(h, s, v, r0.componentCount >= 4 ? r0.lanes[3] : 1.0f);
		return true;
	}

	private boolean selfKill(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() >= 1) {
			readFnArg(ins, 0, ctx, r0);
			if (r0.truthy(0)) {
				ctx.selfKillRequested = true;
			}
		} else {
			ctx.selfKillRequested = true;
		}
		out.setScalarI(0);
		return true;
	}

	// ------------------------------------------------------------------ generate / trigger / kick

	private void fillEventCache(final ExecContext ctx, final int key, final int count, final int cappedCount) {
		final ExecContext.EventCacheEntry entry = ctx.allocEventCacheEntry(key);
		if (entry != null) {
			entry.count = count;
			entry.countDup = count;
			entry.currentElementIdx = 0;
			entry.forwardFlag = 0;
			for (int i = 0; i < cappedCount; i++) {
				entry.particleIndices[i] = i;
				entry.tFractions[i] = ctx.lastGenerateTs[i];
				entry.lerpedTimes[i] = ctx.lastGenerateLerpedTimes[i];
			}
		}
	}

	private boolean eventStreamGenerate(final Instruction ins, final ExecContext ctx,
			final SamplerResource.EventStream stream) {
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		readFnArg(ins, 2, ctx, r2);
		final float prevAge = r1.lanes[0];
		final float currentAge = r2.lanes[0];
		int count = 0;
		for (final float t : stream.times) {
			if ((t >= prevAge) && (t < currentAge)) {
				count++;
			}
		}
		final int key = ctx.simUnitScratchCounter++;
		final int priorTotal = r0.laneAsInt(1);
		final int newTotal = priorTotal + count;
		out.clear();
		out.componentCount = 3;
		out.typeBank = Bank.INT3;
		out.setLaneInt(0, 0);
		out.setLaneInt(1, newTotal);
		out.setLaneInt(2, key);
		ctx.lastGenerateCount = count;
		ctx.lastGenerateValid = true;
		final int cappedCount = Math.min(count, ExecContext.MAX_PENDING_POSITIONS);
		for (int i = 0; i < cappedCount; i++) {
			ctx.lastGenerateTs[i] = 1.0f;
			ctx.lastGenerateLerpedTimes[i] = ctx.timeWindowEnd;
		}
		fillEventCache(ctx, key, count, cappedCount);
		return true;
	}

	private boolean generate(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 5) {
			throw new VmException("IR: generate requires 5+ args");
		}
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res != null) && (res.kind == SamplerResource.KIND_EVENT_STREAM)) {
			return eventStreamGenerate(ins, ctx, res.eventStream);
		}
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		readFnArg(ins, 2, ctx, r2);
		final float offsets = r1.lanes[0];
		final float intervals = r2.lanes[0];
		final float carry = r0.lanes[0];
		final int priorTotal = r0.laneAsInt(1);
		final float advance = (intervals > 1e-12f) ? (offsets / intervals) : 0;
		final float totalF = carry + advance;
		final int count = (totalF > 0) ? (int) totalF : 0;
		final float newCarry = totalF - count;
		final int newTotal = priorTotal + count;
		final int key = ctx.simUnitScratchCounter++;
		out.clear();
		out.componentCount = 3;
		out.typeBank = Bank.INT3;
		out.lanes[0] = newCarry;
		out.setLaneInt(1, newTotal);
		out.setLaneInt(2, key);
		ctx.lastGenerateCount = count;
		ctx.lastGenerateValid = true;
		final int cappedCount = Math.min(count, ExecContext.MAX_PENDING_POSITIONS);
		if (advance > 0) {
			final float step = 1.0f / advance;
			for (int i = 0; i < cappedCount; i++) {
				final float raw = step * ((i + 1) - carry);
				final float clamped = (raw > 1) ? 1 : (raw < 0 ? 0 : raw);
				ctx.lastGenerateTs[i] = clamped;
				ctx.lastGenerateLerpedTimes[i] = ctx.timeWindowStart
						+ ((ctx.timeWindowEnd - ctx.timeWindowStart) * clamped);
			}
		} else {
			for (int i = 0; i < cappedCount; i++) {
				ctx.lastGenerateTs[i] = 1.0f;
				ctx.lastGenerateLerpedTimes[i] = ctx.timeWindowEnd;
			}
		}
		fillEventCache(ctx, key, count, cappedCount);
		return true;
	}

	private boolean trigger(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 2) {
			throw new VmException("IR: trigger requires 2+ args (condition, fraction)");
		}
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		final int cond = r0.laneAsInt(0);
		final int isTriggered = cond != 0 ? 1 : 0;
		final int key = ctx.simUnitScratchCounter++;
		out.clear();
		out.componentCount = 3;
		out.typeBank = Bank.INT3;
		out.setLaneInt(0, 0);
		out.setLaneInt(1, isTriggered);
		out.setLaneInt(2, key);
		ctx.lastGenerateCount = isTriggered;
		ctx.lastGenerateValid = true;
		if (isTriggered != 0) {
			final float frac = r1.lanes[0];
			final float clamped = (frac > 1) ? 1 : (frac < 0 ? 0 : frac);
			ctx.lastGenerateTs[0] = clamped;
			ctx.lastGenerateLerpedTimes[0] = ctx.timeWindowStart
					+ ((ctx.timeWindowEnd - ctx.timeWindowStart) * clamped);
		}
		fillEventCache(ctx, key, isTriggered, isTriggered);
		return true;
	}

	private boolean initPayload(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 2) {
			throw new VmException("IR: initPayload requires 2+ args");
		}
		int eventId;
		final long resolved = resolveKickEventIdFromObjSlot(ins, ctx);
		if (resolved >= 0) {
			eventId = (int) resolved;
		} else {
			readFnArg(ins, 0, ctx, r0);
			eventId = r0.laneAsInt(0);
		}
		int count = 0;
		final int nargs = valueArgCount(ins);
		if ((nargs >= 1) && readValueArg(ins, nargs - 1, ctx, r1)) {
			final int genKey = r1.laneAsInt(2);
			final ExecContext.EventCacheEntry e = ctx.findEventCacheEntry(genKey);
			if (e != null) {
				count = e.count;
			}
		}
		ctx.setPendingKickCount(eventId, count);
		out.setScalarI(0);
		return true;
	}

	private boolean kick(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 1) {
			throw new VmException("IR: kick requires 1+ args");
		}
		int eventId;
		final long resolved = resolveKickEventIdFromObjSlot(ins, ctx);
		if (resolved >= 0) {
			eventId = (int) resolved;
		} else {
			readFnArg(ins, 0, ctx, r0);
			eventId = r0.laneAsInt(0);
		}
		final int count = ctx.takePendingKickCount(eventId);
		ExecContext.PendingPayloadElement pending = null;
		for (final ExecContext.PendingPayloadElement slot : ctx.pendingPayloadElements) {
			if (slot.valid && (slot.eventId == eventId)) {
				pending = slot;
				slot.valid = false;
				break;
			}
		}
		if (ctx.spawnQueue != null) {
			final long parentSelfId = ctx.currentSelfId;
			for (int i = 0; i < count; i++) {
				if ((ctx.spawnQueue.capacity != 0) && (ctx.spawnQueue.events.size() >= ctx.spawnQueue.capacity)) {
					ctx.spawnQueue.dropped += count - i;
					break;
				}
				final int parentRng = ctx.rng != null ? ctx.rng.advance() : 0;
				final SpawnEvent ev = new SpawnEvent();
				ev.eventId = eventId;
				ev.sequenceIndex = i;
				ev.parentSelfId = parentSelfId;
				ev.parentRngState = parentRng;
				ev.spawnFrameLocal = !ctx.layerWorldSpace;
				if (pending != null) {
					if (i < pending.positionCount) {
						ev.hasSpawnPosition = true;
						System.arraycopy(pending.positions[i], 0, ev.spawnPosition, 0, 3);
						ev.spawnPositionPayloadId = pending.positionPayloadId;
					}
					if (pending.hasOrientation) {
						ev.hasSpawnOrientation = true;
						System.arraycopy(pending.orientation, 0, ev.spawnOrientation, 0, 4);
						ev.spawnOrientationPayloadId = pending.orientationPayloadId;
					}
					if (pending.hasIntPayload) {
						ev.hasIntPayload = true;
						ev.intPayloadWidth = pending.intPayloadWidth;
						System.arraycopy(pending.intPayload, 0, ev.intPayload, 0, 4);
						ev.intPayloadId = pending.intPayloadId;
					}
					if (pending.hasSpawnIndexPayload) {
						ev.hasIntPayload = true;
						ev.intPayloadWidth = 1;
						ev.intPayloadId = pending.spawnIndexPayloadId;
						ev.intPayload[0] = pending.spawnIndexBase + i;
						ev.intPayload[1] = ev.intPayload[2] = ev.intPayload[3] = 0;
					}
					if (pending.hasBoolPayload) {
						ev.hasBoolPayload = true;
						ev.boolPayloadWidth = pending.boolPayloadWidth;
						System.arraycopy(pending.boolPayload, 0, ev.boolPayload, 0, 4);
						ev.boolPayloadId = pending.boolPayloadId;
					}
					SpawnEvent.copyFloatSlots(pending.floatSlots, ev.floatSlots);
				}
				if (i < ExecContext.MAX_PENDING_POSITIONS) {
					ev.subFrameFraction = ctx.lastGenerateTs[i];
					ev.lerpedTime = ctx.lastGenerateLerpedTimes[i];
				}
				ctx.spawnQueue.events.add(ev);
			}
		}
		if (pending != null) {
			pending.positionCount = 0;
			pending.hasOrientation = false;
		}
		out.setScalarI(0);
		return true;
	}

	private boolean buildPayloadElement(final Instruction ins, final ExecContext ctx) {
		final int payloadElementId = ctx.nextPayloadElementId++;
		out.setScalarI(payloadElementId);
		final int argc = ins.argc();
		if (argc < 3) {
			return true;
		}
		final RegisterValue payloadA = r0;
		final RegisterValue payloadB = r1;
		final RegisterValue payloadAd = r2;
		final RegisterValue payloadBd = r3;
		readFnArg(ins, 1, ctx, payloadA);
		readFnArg(ins, 2, ctx, payloadB);
		boolean hasDerivatives = false;
		payloadAd.clear();
		payloadBd.clear();
		int packedSemantic = 1;
		if (argc >= 6) {
			readFnArg(ins, 3, ctx, payloadAd);
			readFnArg(ins, 4, ctx, payloadBd);
			hasDerivatives = true;
			readFnArg(ins, 5, ctx, r4);
			packedSemantic = r4.laneAsInt(0);
		} else if (argc == 5) {
			readFnArg(ins, 3, ctx, payloadAd);
			readFnArg(ins, 4, ctx, payloadBd);
			hasDerivatives = true;
			packedSemantic = 2;
		} else if (argc == 4) {
			readFnArg(ins, 3, ctx, r4);
			packedSemantic = r4.laneAsInt(0);
		}
		final int semByte = packedSemantic & 0xFF;
		final int bnk = payloadA.typeBank;
		final boolean isIntBank = (bnk == Bank.INT) || (bnk == Bank.INT2) || (bnk == Bank.INT2_ALT)
				|| (bnk == Bank.INT2_ALT2) || (bnk == Bank.INT3) || (bnk == Bank.INT4) || (bnk == Bank.PTR);
		final boolean isScalarBool = (bnk == Bank.BOOL) && (payloadA.componentCount == 1);
		final boolean isQuaternion = bnk == Bank.INT_ALT;
		final int intWidth = Math.min(payloadA.componentCount, 4);
		final int fWidth = Math.min(Math.max(payloadA.componentCount, payloadB.componentCount), 4);
		if (!isIntBank && !isScalarBool) {
			for (int lane = 0; lane < 4; lane++) {
				f4[lane] = semByte == 0 ? payloadA.lanes[lane] : payloadB.lanes[lane];
			}
			ctx.stashBuiltPayloadFloat(payloadElementId, fWidth, f4);
		} else if (isIntBank) {
			readFnArg(ins, 0, ctx, r4);
			ctx.builtPayloadIndex.valid = true;
			ctx.builtPayloadIndex.elementId = payloadElementId;
			ctx.builtPayloadIndex.base = r4.laneAsInt(1);
		}
		final long resolved = resolveKickEventIdFromObjSlot(ins, ctx);
		if (resolved < 0) {
			return true;
		}
		final ExecContext.PendingPayloadElement slot = ctx.findOrCreatePendingPayload((int) resolved);
		if (slot == null) {
			return true;
		}
		if (isScalarBool) {
			slot.hasBoolPayload = true;
			slot.boolPayloadWidth = 1;
			slot.boolPayloadId = payloadElementId;
			final int src = semByte == 0 ? payloadA.laneAsInt(0) : payloadB.laneAsInt(0);
			slot.boolPayload[0] = src != 0 ? 1 : 0;
			return true;
		}
		if (isIntBank && (intWidth >= 1) && (intWidth <= 4)) {
			slot.hasIntPayload = true;
			slot.intPayloadWidth = intWidth;
			slot.intPayloadId = payloadElementId;
			for (int lane = 0; lane < intWidth; lane++) {
				slot.intPayload[lane] = semByte == 0 ? payloadA.laneAsInt(lane) : payloadB.laneAsInt(lane);
			}
			return true;
		}
		if (isQuaternion) {
			slot.hasOrientation = true;
			slot.orientationPayloadId = payloadElementId;
			for (int i = 0; i < 4; i++) {
				slot.orientation[i] = semByte == 0 ? payloadA.lanes[i] : payloadB.lanes[i];
			}
			return true;
		}
		if (fWidth == 3) {
			final int count = ctx.lastGenerateValid
					? Math.min(ctx.lastGenerateCount, ExecContext.MAX_PENDING_POSITIONS)
					: 0;
			slot.positionCount = count;
			slot.positionPayloadId = payloadElementId;
			for (int i = 0; i < count; i++) {
				final float t = ctx.lastGenerateTs[i];
				for (int lane = 0; lane < 3; lane++) {
					final float a = payloadA.lanes[lane];
					final float b = payloadB.lanes[lane];
					float v;
					if (semByte == 0) {
						v = a;
					} else if ((semByte == 2) && hasDerivatives) {
						final float t2 = t * t;
						final float t3 = t2 * t;
						final float h10 = (t3 - (2.0f * t2)) + t;
						final float h11 = t3 - t2;
						v = a + ((b - a) * t) + (h10 * payloadAd.lanes[lane]) + (h11 * payloadBd.lanes[lane]);
					} else {
						v = a + ((b - a) * t);
					}
					slot.positions[i][lane] = v;
				}
			}
		}
		return true;
	}

	private boolean appendPayload(final Instruction ins, final ExecContext ctx) {
		out.setScalarI(0);
		final int argc = ins.argc();
		final int spatialIndex = resolveSpatialLayerIndex(ins, ctx);
		if (spatialIndex >= 0) {
			final SpatialLayerResource layer = ctx.spatialLayers[spatialIndex];
			if (argc >= 1) {
				readFnArg(ins, 0, ctx, r0);
				out.set(r0);
			}
			if (argc >= 2) {
				readFnArg(ins, 1, ctx, r1);
				final int key = r0.laneAsInt(0);
				int nameHash = 0;
				if (argc >= 3) {
					readFnArg(ins, 2, ctx, r2);
					nameHash = layer.payloadNameHashById(r2.laneAsInt(0));
				}
				for (final ExecContext.SpatialAppendSlot slot : ctx.spatialAppendStaged) {
					if (!slot.valid || ((slot.key == key) && (slot.nameHash == nameHash))) {
						slot.valid = true;
						slot.key = key;
						slot.nameHash = nameHash;
						slot.components = r1.componentCount > 0 ? r1.componentCount : 3;
						System.arraycopy(r1.lanes, 0, slot.value, 0, 4);
						break;
					}
				}
			}
			return true;
		}
		if (argc >= 1) {
			readFnArg(ins, 0, ctx, r0);
			out.set(r0);
		}
		if (argc < 3) {
			return true;
		}
		final long resolved = resolveKickEventIdFromObjSlot(ins, ctx);
		if (resolved < 0) {
			return true;
		}
		readFnArg(ins, 1, ctx, r1);
		readFnArg(ins, 2, ctx, r2);
		final int payloadElementId = r1.laneAsInt(0);
		final int elementId = r2.laneAsInt(0);
		int nameId = 0;
		final String channel = resolveEventChannelName(ins, ctx);
		for (final EventPayloadDecl.Kicked decl : ctx.kickedEventDecls) {
			if (decl.channel.equals(channel) && (payloadElementId >= 0) && (payloadElementId < decl.elements.length)) {
				nameId = decl.elements[payloadElementId].nameId;
				break;
			}
		}
		if (ctx.builtPayloadIndex.valid && (ctx.builtPayloadIndex.elementId == elementId)) {
			final ExecContext.PendingPayloadElement slot = ctx.findOrCreatePendingPayload((int) resolved);
			if (slot != null) {
				slot.hasSpawnIndexPayload = true;
				slot.spawnIndexPayloadId = nameId != 0 ? nameId : payloadElementId;
				slot.spawnIndexBase = ctx.builtPayloadIndex.base;
			}
			return true;
		}
		for (final ExecContext.BuiltPayloadFloat b : ctx.builtPayloadFloats) {
			if (!b.valid || (b.elementId != elementId)) {
				continue;
			}
			final ExecContext.PendingPayloadElement slot = ctx.findOrCreatePendingPayload((int) resolved);
			if (slot == null) {
				break;
			}
			if (nameId != 0) {
				SpawnEvent.PayloadFloatSlot dst = null;
				for (final SpawnEvent.PayloadFloatSlot fs : slot.floatSlots) {
					if (fs.valid && (fs.nameId == nameId)) {
						dst = fs;
						break;
					}
				}
				if (dst == null) {
					for (final SpawnEvent.PayloadFloatSlot fs : slot.floatSlots) {
						if (!fs.valid) {
							dst = fs;
							break;
						}
					}
				}
				if (dst != null) {
					dst.nameId = nameId;
					dst.valid = true;
					dst.width = b.width;
					System.arraycopy(b.value, 0, dst.value, 0, 4);
				}
			}
			if ((nameId == POSITION_NAME_ID) && (b.width == 3)) {
				slot.positionCount = ExecContext.MAX_PENDING_POSITIONS;
				slot.positionPayloadId = nameId;
				for (int i = 0; i < ExecContext.MAX_PENDING_POSITIONS; i++) {
					slot.positions[i][0] = b.value[0];
					slot.positions[i][1] = b.value[1];
					slot.positions[i][2] = b.value[2];
				}
			}
			if ((nameId == ORIENTATION_NAME_ID) && (b.width == 4)) {
				slot.hasOrientation = true;
				slot.orientationPayloadId = nameId;
				System.arraycopy(b.value, 0, slot.orientation, 0, 4);
			}
			break;
		}
		return true;
	}

	private boolean extractPayloadElement(final Instruction ins, final ExecContext ctx, final String symbol) {
		out.clear();
		final int base = "extractPayloadElement".length();
		final char suffix = symbol.length() > base ? symbol.charAt(base) : '\0';
		final char widthCh = symbol.length() > (base + 1) ? symbol.charAt(base + 1) : '\0';
		int payloadIndex = 0;
		if (ins.argc() >= 1) {
			readFnArg(ins, 0, ctx, r0);
			payloadIndex = r0.laneAsInt(0);
		}
		final int declNameId = ((payloadIndex >= 0) && (payloadIndex < ctx.rootEventDecl.length))
				? ctx.rootEventDecl[payloadIndex].nameId
				: 0;
		if (suffix == 'O') {
			out.componentCount = 4;
			out.typeBank = Bank.FLOAT4;
			if (payloadMatches(payloadIndex, declNameId, ctx.spawnOrientationPayloadId)) {
				System.arraycopy(ctx.spawnQuat, 0, out.lanes, 0, 4);
			}
			return true;
		}
		final int width = ((widthCh >= '1') && (widthCh <= '4')) ? widthCh - '0' : 1;
		if (suffix == 'F') {
			out.componentCount = width;
			out.typeBank = Bank.floatBankForComponentCount(width);
			if (declNameId != 0) {
				for (final SpawnEvent.PayloadFloatSlot fs : ctx.spawnFloatSlots) {
					if (fs.valid && (fs.nameId == declNameId)) {
						for (int i = 0; i < width; i++) {
							out.lanes[i] = fs.value[i];
						}
						return true;
					}
				}
			}
			return true;
		}
		out.componentCount = width;
		out.typeBank = Bank.intBankForComponentCount(width);
		if ((suffix == 'I') && ctx.hasSpawnIntPayload && payloadMatches(payloadIndex, declNameId, ctx.spawnIntPayloadId)) {
			final int staged = Math.min(ctx.spawnIntPayloadWidth, width);
			for (int i = 0; i < staged; i++) {
				out.setLaneInt(i, ctx.spawnIntPayload[i]);
			}
			return true;
		}
		if ((suffix == 'B') && ctx.hasSpawnBoolPayload
				&& payloadMatches(payloadIndex, declNameId, ctx.spawnBoolPayloadId)) {
			final int staged = Math.min(ctx.spawnBoolPayloadWidth, width);
			for (int i = 0; i < staged; i++) {
				out.setLaneInt(i, ctx.spawnBoolPayload[i]);
			}
			return true;
		}
		return true;
	}

	private static boolean payloadMatches(final int payloadIndex, final int declNameId, final int stagedId) {
		return ((declNameId != 0) && (declNameId == stagedId)) || (payloadIndex == 0) || (payloadIndex == stagedId);
	}

	// ------------------------------------------------------------------ samplers

	private boolean sampleTurbulenceToReg(final SamplerResource res, final Instruction ins, final ExecContext ctx) {
		float qx = 0, qy = 0, qz = 0;
		if (ins.argc() >= 1) {
			readFnArg(ins, 0, ctx, r0);
			qx = r0.lanes[0];
			qy = r0.lanes[1];
			qz = r0.lanes[2];
		}
		if (res.turbulenceField != null) {
			res.turbulenceField.sampleVelocity(qx, qy, qz, ctx.effectAge, v3);
			out.setFloat3(v3[0], v3[1], v3[2]);
		} else {
			out.setFloat3(0, 0, 0);
		}
		return true;
	}

	private boolean sample(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if (res == null) {
			return false;
		}
		if (res.kind == SamplerResource.KIND_TURBULENCE) {
			return sampleTurbulenceToReg(res, ins, ctx);
		}
		if (res.kind == SamplerResource.KIND_TEXTURE) {
			final int comps = res.textureScriptOutputType == 1 ? 1 : 4;
			out.clear();
			out.componentCount = comps;
			out.typeBank = Bank.floatBankForComponentCount(comps);
			return true;
		}
		if (res.kind != SamplerResource.KIND_CURVE) {
			return false;
		}
		if (ins.argc() < 1) {
			throw new VmException("IR: sample requires a t arg");
		}
		readFnArg(ins, 0, ctx, r0);
		final int comps = res.curve.components;
		if ((comps < 1) || (comps > 4)) {
			return false;
		}
		out.clear();
		out.componentCount = comps;
		out.typeBank = Bank.floatBankForComponentCount(comps);
		return res.curve.evalVec(r0.lanes[0], out.lanes, 4) != 0;
	}

	private boolean sampleCdf(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_CURVE)) {
			return false;
		}
		if (ins.argc() < 1) {
			throw new VmException("IR: sampleCDF requires a cursor arg");
		}
		readFnArg(ins, 0, ctx, r0);
		out.setScalar(res.curve.evalCdf(r0.lanes[0], 0));
		return true;
	}

	private boolean textureDimensions(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_TEXTURE)) {
			return false;
		}
		out.setFloats(2, Bank.FLOAT2);
		return true;
	}

	private boolean textureAtlasRectCount(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_TEXTURE)) {
			return false;
		}
		out.setFloats(1, Bank.INT);
		return true;
	}

	// ------------------------------------------------------------------ shapes

	private boolean readShapePCoordsArg(final Instruction ins, final ExecContext ctx, final float[] pc) {
		if ((valueArgCount(ins) < 1) || !readValueArg(ins, 0, ctx, r4) || (r4.componentCount < 3)) {
			return false;
		}
		pc[0] = r4.lanes[0];
		pc[1] = r4.lanes[1];
		pc[2] = r4.lanes[2];
		return true;
	}

	private boolean samplePosition(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if (res == null) {
			return false;
		}
		if (res.kind == SamplerResource.KIND_TURBULENCE) {
			return sampleTurbulenceToReg(res, ins, ctx);
		}
		if ((res.kind != SamplerResource.KIND_SHAPE) || (ctx.rng == null)) {
			return false;
		}
		float[] pc = null;
		if ((res.shape.type != SamplerResource.SHAPE_MESH) && readShapePCoordsArg(ins, ctx, v3c)) {
			pc = v3c;
		}
		return ShapeSampling.samplePosition(res.shape, ctx.rng, pc, out);
	}

	private boolean sampleNormal(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)
				|| (res.shape.type == SamplerResource.SHAPE_MESH)) {
			return false;
		}
		if (!samplePosition(ins, ctx)) {
			return false;
		}
		v3[0] = out.lanes[0];
		v3[1] = out.lanes[1];
		v3[2] = out.lanes[2];
		ShapeSampling.worldToLocal(res.shape, v3, v3b);
		ShapeSampling.surfaceNormal(res.shape, v3b, v3);
		if (res.shape.transformRotate) {
			ShapeSampling.rotateDirection(res.shape, v3[0], v3[1], v3[2], v3b);
			out.setFloat3(v3b[0], v3b[1], v3b[2]);
		} else {
			out.setFloat3(v3[0], v3[1], v3[2]);
		}
		return true;
	}

	private boolean primitiveClearedChannel(final Instruction ins, final ExecContext ctx, final int bank,
			final int components) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)
				|| (res.shape.type == SamplerResource.SHAPE_MESH)) {
			return false;
		}
		out.setFloats(components, bank);
		return true;
	}

	private boolean sampleChannelSym(final Instruction ins, final ExecContext ctx, final String symbol) {
		final boolean isColor = symbol.startsWith("sampleColor");
		final int baseLen = isColor ? "sampleColor".length() : "sampleTexcoord".length();
		int stream = -1;
		if (symbol.length() == (baseLen + 1)) {
			final char c = symbol.charAt(baseLen);
			if ((c < '0') || (c > '9')) {
				return false;
			}
			stream = c - '0';
		} else if (symbol.length() != baseLen) {
			return false;
		}
		final boolean isFloat4 = isColor && (stream < 0);
		return primitiveClearedChannel(ins, ctx, isFloat4 ? Bank.FLOAT4 : Bank.FLOAT2, isFloat4 ? 4 : 2);
	}

	private boolean projectPCoords(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE) || (valueArgCount(ins) < 1)) {
			return false;
		}
		if (!readValueArg(ins, 0, ctx, r0)) {
			return false;
		}
		if (res.shape.type == SamplerResource.SHAPE_MESH) {
			return false;
		}
		v3[0] = r0.lanes[0];
		v3[1] = r0.lanes[1];
		v3[2] = r0.lanes[2];
		ShapeSampling.worldToLocal(res.shape, v3, v3b);
		return ShapeSampling.projectPCoordsPrimitive(res.shape, v3b, out);
	}

	private boolean readPoint3(final Instruction ins, final int idx, final ExecContext ctx, final float[] dst) {
		if (!readValueArg(ins, idx, ctx, r4)) {
			return false;
		}
		dst[0] = r4.lanes[0];
		dst[1] = r4.lanes[1];
		dst[2] = r4.lanes[2];
		return true;
	}

	private boolean shapeContains(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)) {
			return false;
		}
		if (!readPoint3(ins, 0, ctx, v3)) {
			return false;
		}
		ShapeSampling.worldToLocal(res.shape, v3, v3b);
		final boolean inside = ShapeSampling.contains(res.shape, v3b);
		out.clear();
		out.componentCount = 1;
		out.typeBank = Bank.BOOL;
		out.setLaneInt(0, inside ? 0xFFFFFFFF : 0);
		return true;
	}

	private boolean shapeDistanceField(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)) {
			return false;
		}
		if (!readPoint3(ins, 0, ctx, v3)) {
			return false;
		}
		ShapeSampling.worldToLocal(res.shape, v3, v3b);
		out.setScalar(ShapeSampling.distanceField(res.shape, v3b));
		return true;
	}

	private boolean shapeProject(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)) {
			return false;
		}
		if (!readPoint3(ins, 0, ctx, v3)) {
			return false;
		}
		if (res.shape.type == SamplerResource.SHAPE_MESH) {
			return false;
		}
		ShapeSampling.worldToLocal(res.shape, v3, v3b);
		ShapeSampling.project(res.shape, v3b, v3c);
		ShapeSampling.localToWorld(res.shape, v3c, v3);
		out.setFloat3(v3[0], v3[1], v3[2]);
		return true;
	}

	private boolean shapeIntersect(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)) {
			return false;
		}
		final float[] origin = new float[3];
		final float[] dir = new float[3];
		if (!readPoint3(ins, 0, ctx, origin) || !readPoint3(ins, 1, ctx, dir)) {
			return false;
		}
		final float length = readValueArg(ins, 2, ctx, r4) ? r4.lanes[0] : 1.0f;
		final boolean twoSided = readValueArg(ins, 3, ctx, r4) && (r4.lanes[0] != 0);
		final SamplerResource.Shape sh = res.shape;
		final float[] localO = new float[3];
		final float[] localD = new float[3];
		ShapeSampling.worldToLocal(sh, origin, localO);
		if (sh.transformRotate) {
			ShapeSampling.unrotateDirection(sh, dir[0], dir[1], dir[2], localD);
		} else {
			System.arraycopy(dir, 0, localD, 0, 3);
		}
		out.setFloat4(0, 0, 0, 0);
		final float t = ShapeSampling.intersect(sh, localO, localD, length, twoSided);
		if (Float.isNaN(t)) {
			return true;
		}
		v3[0] = localO[0] + (localD[0] * length * t);
		v3[1] = localO[1] + (localD[1] * length * t);
		v3[2] = localO[2] + (localD[2] * length * t);
		ShapeSampling.localToWorld(sh, v3, v3b);
		out.setFloat4(v3b[0], v3b[1], v3b[2], t);
		return true;
	}

	private boolean samplePCoords(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE) || (ctx.rng == null)) {
			return false;
		}
		return ShapeSampling.samplePCoords(res.shape, ctx.rng, out);
	}

	private boolean buildPCoords(final String symbol, final Instruction ins, final ExecContext ctx) {
		final float a0 = valueArgScalar(ins, 0, ctx, 0);
		final float a1 = valueArgScalar(ins, 1, ctx, 0);
		final float a2 = valueArgScalar(ins, 2, ctx, 0);
		return ShapeSampling.buildPCoords(symbol, a0, a1, a2, valueArgCount(ins), out);
	}

	private boolean sampleSurfacePCoordsFromUV(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)
				|| (res.shape.type == SamplerResource.SHAPE_MESH)) {
			return false;
		}
		final float u = valueArgScalar(ins, 0, ctx, 0);
		final float v = readValueArg(ins, 0, ctx, r4) ? r4.lanes[1] : 0;
		return ShapeSampling.sampleSurfacePCoordsFromUV(res.shape, u, v, out);
	}

	private boolean shapeIntersectPCoords(final Instruction ins, final ExecContext ctx) {
		final SamplerResource res = resolveTargetSampler(ins, ctx);
		if ((res == null) || (res.kind != SamplerResource.KIND_SHAPE)
				|| (res.shape.type == SamplerResource.SHAPE_MESH)) {
			return false;
		}
		if (!shapeIntersect(ins, ctx)) {
			return false;
		}
		if (out.lanes[3] == 0) {
			out.clear();
			out.componentCount = 3;
			out.typeBank = Bank.INT3;
			return true;
		}
		v3[0] = out.lanes[0];
		v3[1] = out.lanes[1];
		v3[2] = out.lanes[2];
		ShapeSampling.worldToLocal(res.shape, v3, v3b);
		return ShapeSampling.projectPCoordsPrimitive(res.shape, v3b, out);
	}

	// ------------------------------------------------------------------ spatial layers

	private ProximityHash spatialHashFor(final ExecContext ctx, final int index, final boolean write) {
		if (index < 0) {
			return null;
		}
		final ProximityHash[] target = write && (ctx.spatialHashesWrite.length > 0) ? ctx.spatialHashesWrite
				: ctx.spatialHashes;
		if (index >= target.length) {
			return null;
		}
		return target[index];
	}

	private boolean spatialInsert(final Instruction ins, final ExecContext ctx) {
		out.setScalarI(0);
		if (ins.argc() < 2) {
			throw new VmException("IR: spatial insert requires (key, position)");
		}
		final int index = resolveSpatialLayerIndex(ins, ctx);
		final ProximityHash hash = spatialHashFor(ctx, index, true);
		if (hash == null) {
			return true;
		}
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		final float[] pos = { r1.lanes[0], r1.lanes[1], r1.lanes[2] };
		final int key = r0.laneAsInt(0);
		final float[] payload = { pos[0], pos[1], pos[2] };
		final ProximityHash.Payload[] named = new ProximityHash.Payload[ProximityHash.MAX_PAYLOADS];
		int namedCount = 0;
		boolean haveLegacy = false;
		for (final ExecContext.SpatialAppendSlot slot : ctx.spatialAppendStaged) {
			if (!slot.valid || (slot.key != key)) {
				continue;
			}
			if (!haveLegacy) {
				payload[0] = slot.value[0];
				payload[1] = slot.value[1];
				payload[2] = slot.value[2];
				haveLegacy = true;
			}
			if (namedCount < ProximityHash.MAX_PAYLOADS) {
				final ProximityHash.Payload p = new ProximityHash.Payload();
				p.nameHash = slot.nameHash;
				p.components = slot.components;
				System.arraycopy(slot.value, 0, p.value, 0, 4);
				named[namedCount++] = p;
			}
			slot.valid = false;
		}
		hash.insert(pos, payload, ctx.currentSelfId, named, namedCount);
		return true;
	}

	private static void readSpatialPayload(final ProximityHash.Entry entry, final int nameHash, final int width,
			final RegisterValue out) {
		final ProximityHash.Payload p = nameHash != 0 ? entry.findPayload(nameHash) : null;
		for (int i = 0; (i < width) && (i < 4); i++) {
			if (p != null) {
				out.lanes[i] = i < p.components ? p.value[i] : 0;
			} else {
				out.lanes[i] = i < 3 ? entry.payload[i] : 0;
			}
		}
	}

	private boolean spatialClosest(final Instruction ins, final ExecContext ctx, final String symbol) {
		out.clear();
		final int base = "closest".length();
		final char suffix = symbol.length() > base ? symbol.charAt(base) : '\0';
		final char widthCh = symbol.length() > (base + 1) ? symbol.charAt(base + 1) : '\0';
		final int width = ((widthCh >= '1') && (widthCh <= '4')) ? widthCh - '0' : 1;
		out.componentCount = width;
		out.typeBank = suffix == 'F' ? Bank.floatBankForComponentCount(width) : Bank.intBankForComponentCount(width);
		final int index = resolveSpatialLayerIndex(ins, ctx);
		final ProximityHash hash = spatialHashFor(ctx, index, false);
		if (hash == null) {
			return true;
		}
		final int values = valueArgCount(ins);
		if (values < 3) {
			return true;
		}
		if (!readValueArg(ins, 0, ctx, r0) || !readValueArg(ins, 1, ctx, r1)) {
			return false;
		}
		int nIndex = 0;
		int payloadIndex = 2;
		if (values >= 6) {
			if (readValueArg(ins, 2, ctx, r2)) {
				nIndex = r2.laneAsInt(0);
			}
			payloadIndex = 5;
		} else if (values == 4) {
			if (readValueArg(ins, 2, ctx, r2)) {
				r1.set(r2);
			}
			payloadIndex = 3;
		}
		int nameHash = 0;
		if (readValueArg(ins, payloadIndex, ctx, r3)) {
			nameHash = ctx.spatialLayers[index].payloadNameHashById(r3.laneAsInt(0));
		}
		final float[] target = { r0.lanes[0], r0.lanes[1], r0.lanes[2] };
		final ProximityHash.Entry hit = hash.closestN(target, r1.lanes[0], nIndex);
		if (hit == null) {
			if (suffix == 'F') {
				for (int i = 0; (i < out.componentCount) && (i < 4); i++) {
					out.lanes[i] = Float.intBitsToFloat(INF_BITS);
				}
			}
			return true;
		}
		if (suffix == 'F') {
			readSpatialPayload(hit, nameHash, width, out);
			return true;
		}
		r4.clear();
		readSpatialPayload(hit, nameHash, width, r4);
		for (int i = 0; (i < width) && (i < 4); i++) {
			out.setLaneInt(i, (int) r4.lanes[i]);
		}
		return true;
	}

	private boolean spatialReduce(final Instruction ins, final ExecContext ctx, final String symbol) {
		out.clear();
		String base;
		boolean wantAverage = false;
		boolean wantKernel = false;
		if (symbol.startsWith("sumKernel")) {
			base = "sumKernel";
			wantKernel = true;
		} else if (symbol.startsWith("averageKernel")) {
			base = "averageKernel";
			wantAverage = true;
			wantKernel = true;
		} else if (symbol.startsWith("average")) {
			base = "average";
			wantAverage = true;
		} else if (symbol.startsWith("sum")) {
			base = "sum";
		} else {
			return false;
		}
		if (symbol.length() < (base.length() + 2)) {
			return false;
		}
		final char suffix = symbol.charAt(base.length());
		final char widthCh = symbol.charAt(base.length() + 1);
		if (((suffix != 'F') && (suffix != 'I')) || (widthCh < '1') || (widthCh > '4')) {
			return false;
		}
		final int width = widthCh - '0';
		out.componentCount = width;
		out.typeBank = suffix == 'F' ? Bank.floatBankForComponentCount(width) : Bank.intBankForComponentCount(width);
		final int index = resolveSpatialLayerIndex(ins, ctx);
		final ProximityHash hash = spatialHashFor(ctx, index, false);
		if (hash == null) {
			return true;
		}
		if (valueArgCount(ins) < 3) {
			return true;
		}
		if (!readValueArg(ins, 0, ctx, r0) || !readValueArg(ins, 1, ctx, r1) || !readValueArg(ins, 2, ctx, r2)) {
			return false;
		}
		final int nameHash = ctx.spatialLayers[index].payloadNameHashById(r2.laneAsInt(0));
		SamplerResource.Curve kernelCurve = null;
		if (wantKernel) {
			final SamplerResource res = resolveHandleArgSampler(ins, ctx, SamplerResource.KIND_CURVE);
			if (res == null) {
				return false;
			}
			kernelCurve = res.curve;
		}
		final float[] target = { r0.lanes[0], r0.lanes[1], r0.lanes[2] };
		final float radius = r1.lanes[0];
		final float invRadius = radius != 0 ? 1.0f / radius : 0;
		final float[] sum = new float[4];
		final float[] count = { 0 };
		final SamplerResource.Curve kernel = kernelCurve;
		final RegisterValue tmp = new RegisterValue();
		final float[] k = new float[4];
		hash.forEachInRadius(target, radius, (e, dSq) -> {
			float w = 1.0f;
			if (kernel != null) {
				final float cursor = (float) Math.sqrt(dSq) * invRadius;
				if (kernel.evalVec(cursor, k, 4) == 0) {
					return;
				}
				w = k[0];
			}
			tmp.clear();
			readSpatialPayload(e, nameHash, width, tmp);
			for (int i = 0; (i < width) && (i < 4); i++) {
				sum[i] += tmp.lanes[i] * w;
			}
			count[0] += w;
		});
		if (count[0] == 0) {
			for (int i = 0; (i < width) && (i < 4); i++) {
				if (suffix == 'F') {
					out.lanes[i] = Float.intBitsToFloat(INF_BITS);
				} else {
					out.setLaneInt(i, 0);
				}
			}
			return true;
		}
		final float scale = wantAverage ? 1.0f / count[0] : 1.0f;
		for (int i = 0; (i < width) && (i < 4); i++) {
			if (suffix == 'F') {
				out.lanes[i] = sum[i] * scale;
			} else {
				out.setLaneInt(i, (int) (sum[i] * scale));
			}
		}
		return true;
	}

	private boolean spatialNeighborCount(final Instruction ins, final ExecContext ctx) {
		out.setScalarI(0);
		final int index = resolveSpatialLayerIndex(ins, ctx);
		final ProximityHash hash = spatialHashFor(ctx, index, false);
		if ((hash == null) || (ins.argc() < 2)) {
			return true;
		}
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		final float[] target = { r0.lanes[0], r0.lanes[1], r0.lanes[2] };
		out.setScalarI(hash.neighborCount(target, r1.lanes[0]));
		return true;
	}

	// ------------------------------------------------------------------ orientation and transforms

	private boolean sceneOrientation(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 1) {
			throw new VmException("IR: scene.orientation requires forward arg");
		}
		readFnArg(ins, 0, ctx, r0);
		final float fx = r0.lanes[0];
		final float fy = r0.lanes[1];
		final float fz = r0.lanes[2];
		float qx = -fy;
		float qy = fx;
		float qz = 0;
		float qw = 1.0f + fz;
		final float mag2 = (qx * qx) + (qy * qy) + (qz * qz) + (qw * qw);
		if (mag2 < 1e-12f) {
			qx = 1;
			qy = 0;
			qz = 0;
			qw = 0;
		} else {
			final float invMag = 1.0f / (float) Math.sqrt(mag2);
			qx *= invMag;
			qy *= invMag;
			qz *= invMag;
			qw *= invMag;
		}
		out.setFloat4(qx, qy, qz, qw);
		return true;
	}

	private static final int XF_FILTER = 0x07;
	private static final int XF_FILTER_T = 0x01;
	private static final int XF_FILTER_Q = 0x02;
	private static final int XF_SPACE_MASK = 0x3;
	private static final int XF_SPACE_LOCAL_BIT = 0x01;
	private static final int XF_SPACE_PAYLOAD_BIT = 0x02;
	private static final int XF_SPACE_ENTER_SHIFT = 3;
	private static final int XF_SPACE_LEAVE_SHIFT = 5;

	private static final boolean TRACE_XFORM = Boolean.getBoolean("pkb.traceXform");
	/** -Dpkb.legacyPayloadFrame=true keeps the reference's payload-frame rule in evolve scopes (diagnostics). */
	private static final boolean LEGACY_PAYLOAD_FRAME = Boolean.getBoolean("pkb.legacyPayloadFrame");
	/** Diagnostics: how often each xform mask value was seen (index = mask & 63). */
	public static final int[] XFORM_MASK_HISTOGRAM = new int[64];

	private boolean xformL2W(final Instruction ins, final ExecContext ctx, final boolean isPoint) {
		if (ins.argc() < 1) {
			throw new VmException("IR: xform_l2w_*_masked requires arg");
		}
		readFnArg(ins, 0, ctx, r0);
		int mask = XF_FILTER | (XF_SPACE_LOCAL_BIT << XF_SPACE_ENTER_SHIFT);
		if (ins.argc() >= 2) {
			readFnArg(ins, 1, ctx, r1);
			final int mv = r1.laneAsInt(0);
			if (mv != 0) {
				mask = mv;
			}
		}
		XFORM_MASK_HISTOGRAM[mask & 63]++;
		final int filter = mask & XF_FILTER;
		final int spaceEnter = (mask >>> XF_SPACE_ENTER_SHIFT) & XF_SPACE_MASK;
		final int spaceLeave = (mask >>> XF_SPACE_LEAVE_SHIFT) & XF_SPACE_MASK;
		final float ix = r0.lanes[0];
		final float iy = r0.lanes[1];
		final float iz = r0.lanes[2];
		float ox = ix;
		float oy = iy;
		float oz = iz;
		final boolean bothPayload = ((spaceEnter & XF_SPACE_PAYLOAD_BIT) != 0)
				&& ((spaceLeave & XF_SPACE_PAYLOAD_BIT) != 0);
		final boolean noop = (spaceEnter == spaceLeave) || bothPayload;
		if (!noop) {
			final boolean wantPayloadEnter = (spaceEnter & XF_SPACE_PAYLOAD_BIT) != 0;
			final boolean wantPayloadLeave = (spaceLeave & XF_SPACE_PAYLOAD_BIT) != 0;
			final boolean tryPayload = wantPayloadEnter && !wantPayloadLeave;
			final boolean hasPositionPayload = ctx.spawnPositionPayloadId != 0;
			final boolean hasOrientationPayload = ctx.spawnOrientationPayloadId != 0;
			final boolean usePayloadPath = tryPayload && (hasPositionPayload || hasOrientationPayload);
			final boolean wantQ = (filter & XF_FILTER_Q) != 0;
			final boolean wantT = isPoint && ((filter & XF_FILTER_T) != 0);
			if (usePayloadPath && !ctx.inInitScope && ctx.spawnFrameLocal && !LEGACY_PAYLOAD_FRAME) {
				// Evolve scopes of a child spawned by a local-space parent: the parent's
				// payload frame was expressed in the emitter's frame (the parent never
				// converted to world space), and the value re-transformed here is the spawn
				// position stored at init through that same frame. Leaving to world space
				// therefore means applying the emitter transform, not the parent frame
				// again; the reference applies the parent frame twice and the flares of
				// the game's weapon-glow trails collapse at the origin. Children of
				// world-space parents keep the reference rule below.
				final float[][] m = ctx.sceneL2W.m;
				if (wantQ) {
					final float rx = (m[0][0] * ix) + (m[0][1] * iy) + (m[0][2] * iz);
					final float ry = (m[1][0] * ix) + (m[1][1] * iy) + (m[1][2] * iz);
					final float rz = (m[2][0] * ix) + (m[2][1] * iy) + (m[2][2] * iz);
					ox = rx;
					oy = ry;
					oz = rz;
				}
				if (wantT) {
					ox += m[0][3];
					oy += m[1][3];
					oz += m[2][3];
				}
			} else if (usePayloadPath) {
				// Spawn scope: the payload frame is the parent particle's transform.
				if (wantQ && hasOrientationPayload) {
					final float qx = ctx.spawnQuat[0];
					final float qy = ctx.spawnQuat[1];
					final float qz = ctx.spawnQuat[2];
					final float qw = ctx.spawnQuat[3];
					final float tx = ((qy * iz) - (qz * iy)) + (qw * ix);
					final float ty = ((qz * ix) - (qx * iz)) + (qw * iy);
					final float tz = ((qx * iy) - (qy * ix)) + (qw * iz);
					ox = ix + (2.0f * ((qy * tz) - (qz * ty)));
					oy = iy + (2.0f * ((qz * tx) - (qx * tz)));
					oz = iz + (2.0f * ((qx * ty) - (qy * tx)));
				}
				if (wantT) {
					ox += ctx.spawnTranslate[0];
					oy += ctx.spawnTranslate[1];
					oz += ctx.spawnTranslate[2];
				}
			} else {
				final float[][] m = ctx.sceneL2W.m;
				if (wantQ) {
					final float rx = (m[0][0] * ix) + (m[0][1] * iy) + (m[0][2] * iz);
					final float ry = (m[1][0] * ix) + (m[1][1] * iy) + (m[1][2] * iz);
					final float rz = (m[2][0] * ix) + (m[2][1] * iy) + (m[2][2] * iz);
					ox = rx;
					oy = ry;
					oz = rz;
				}
				if (wantT) {
					ox += m[0][3];
					oy += m[1][3];
					oz += m[2][3];
				}
			}
		}
		if (TRACE_XFORM) {
			System.err.println("xform_l2w L" + ctx.layerId + (ctx.inInitScope ? " init" : " evolve") + " local=" + ctx.spawnFrameLocal + " mask=0x" + Integer.toHexString(mask) + " filter=" + filter + " enter=" + spaceEnter + " leave=" + spaceLeave + " noop=" + noop + " posPayload=" + ctx.spawnPositionPayloadId + " oriPayload=" + ctx.spawnOrientationPayloadId + " in=" + ix + "," + iy + "," + iz + " out=" + ox + "," + oy + "," + oz + " L2Wt=" + ctx.sceneL2W.m[0][3] + "," + ctx.sceneL2W.m[1][3] + "," + ctx.sceneL2W.m[2][3] + " spawnT=" + ctx.spawnTranslate[0] + "," + ctx.spawnTranslate[1] + "," + ctx.spawnTranslate[2]);
		}
		out.setFloat3(ox, oy, oz);
		return true;
	}

	private boolean xformW2L(final Instruction ins, final ExecContext ctx, final boolean isPoint) {
		if (ins.argc() < 1) {
			throw new VmException("IR: xform_w2l_*_masked requires arg");
		}
		readFnArg(ins, 0, ctx, r0);
		float v0 = r0.lanes[0];
		float v1 = r0.lanes[1];
		float v2 = r0.lanes[2];
		final float[][] m = ctx.sceneL2W.m;
		if (isPoint) {
			v0 -= m[0][3];
			v1 -= m[1][3];
			v2 -= m[2][3];
		}
		final float lx = (m[0][0] * v0) + (m[1][0] * v1) + (m[2][0] * v2);
		final float ly = (m[0][1] * v0) + (m[1][1] * v1) + (m[2][1] * v2);
		final float lz = (m[0][2] * v0) + (m[1][2] * v1) + (m[2][2] * v2);
		out.setFloat3(lx, ly, lz);
		return true;
	}

	private boolean rotateAxisAngle(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 3) {
			throw new VmException("IR: rotate(axis,angle) requires 3 args");
		}
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		readFnArg(ins, 2, ctx, r2);
		float ax = r1.lanes[0];
		float ay = r1.lanes[1];
		float az = r1.lanes[2];
		final float len = (float) Math.sqrt((ax * ax) + (ay * ay) + (az * az));
		if (len > 0) {
			ax /= len;
			ay /= len;
			az /= len;
		}
		final float c = (float) Math.cos(r2.lanes[0]);
		final float s = (float) Math.sin(r2.lanes[0]);
		final float oneMinusC = 1.0f - c;
		final float vx = r0.lanes[0];
		final float vy = r0.lanes[1];
		final float vz = r0.lanes[2];
		final float dot = (ax * vx) + (ay * vy) + (az * vz);
		final float cx = (ay * vz) - (az * vy);
		final float cy = (az * vx) - (ax * vz);
		final float cz = (ax * vy) - (ay * vx);
		out.setFloat3((vx * c) + (cx * s) + (ax * dot * oneMinusC), (vy * c) + (cy * s) + (ay * dot * oneMinusC),
				(vz * c) + (cz * s) + (az * dot * oneMinusC));
		return true;
	}

	private boolean rotateOrientation(final Instruction ins, final ExecContext ctx) {
		if (ins.argc() < 2) {
			throw new VmException("IR: rotate(orientation) requires 2 args");
		}
		readFnArg(ins, 0, ctx, r0);
		readFnArg(ins, 1, ctx, r1);
		final float qx = r1.lanes[0];
		final float qy = r1.lanes[1];
		final float qz = r1.lanes[2];
		final float qw = r1.lanes[3];
		final float vx = r0.lanes[0];
		final float vy = r0.lanes[1];
		final float vz = r0.lanes[2];
		final float tx = ((qy * vz) - (qz * vy)) + (qw * vx);
		final float ty = ((qz * vx) - (qx * vz)) + (qw * vy);
		final float tz = ((qx * vy) - (qy * vx)) + (qw * vz);
		out.setFloat3(vx + (2.0f * ((qy * tz) - (qz * ty))), vy + (2.0f * ((qz * tx) - (qx * tz))),
				vz + (2.0f * ((qx * ty) - (qy * tx))));
		return true;
	}

	private boolean orientationMult(final Instruction ins, final ExecContext ctx) {
		if (!readValueArg(ins, 0, ctx, r0) || !readValueArg(ins, 1, ctx, r1)) {
			return false;
		}
		final float ax = r0.lanes[0], ay = r0.lanes[1], az = r0.lanes[2], aw = r0.lanes[3];
		final float bx = r1.lanes[0], by = r1.lanes[1], bz = r1.lanes[2], bw = r1.lanes[3];
		out.setFloat4(((aw * bx) + (ax * bw) + (ay * bz)) - (az * by), ((aw * by) - (ax * bz)) + (ay * bw) + (az * bx),
				((aw * bz) + (ax * by)) - (ay * bx) + (az * bw), (aw * bw) - (ax * bx) - (ay * by) - (az * bz));
		return true;
	}

	private boolean orientationAxis(final Instruction ins, final ExecContext ctx, final int axis) {
		if (ins.argc() < 1) {
			throw new VmException("IR: orientation_axis* requires arg");
		}
		readFnArg(ins, 0, ctx, r0);
		final float qx = r0.lanes[0];
		final float qy = r0.lanes[1];
		final float qz = r0.lanes[2];
		final float qw = r0.lanes[3];
		switch (axis) {
		case 0:
			out.setFloat3(1.0f - (2.0f * ((qy * qy) + (qz * qz))), 2.0f * ((qx * qy) + (qw * qz)),
					2.0f * ((qx * qz) - (qw * qy)));
			break;
		case 1:
			out.setFloat3(2.0f * ((qx * qy) - (qw * qz)), 1.0f - (2.0f * ((qx * qx) + (qz * qz))),
					2.0f * ((qy * qz) + (qw * qx)));
			break;
		default:
			out.setFloat3(2.0f * ((qx * qz) + (qw * qy)), 2.0f * ((qy * qz) - (qw * qx)),
					1.0f - (2.0f * ((qx * qx) + (qy * qy))));
			break;
		}
		return true;
	}

	private boolean noise(final Instruction ins, final ExecContext ctx) {
		if (valueArgCount(ins) < 1) {
			return false;
		}
		if (!readValueArg(ins, 0, ctx, r0)) {
			return false;
		}
		float value;
		switch (r0.componentCount) {
		case 1:
			value = Noise.simplex1(r0.lanes[0]);
			break;
		case 2:
			value = Noise.simplex2(r0.lanes[0], r0.lanes[1]);
			break;
		case 3:
			value = Noise.simplex3(r0.lanes[0], r0.lanes[1], r0.lanes[2]);
			break;
		case 4:
			value = Noise.simplex4(r0.lanes[0], r0.lanes[1], r0.lanes[2], r0.lanes[3]);
			break;
		default:
			return false;
		}
		out.setScalar(value);
		return true;
	}

	// ------------------------------------------------------------------ view / effect axes

	private int readCameraIndex(final Instruction ins, final int n, final ExecContext ctx) {
		if (valueArgCount(ins) <= n) {
			return 0;
		}
		if (!readValueArg(ins, n, ctx, r4)) {
			return 0;
		}
		return r4.laneAsInt(0);
	}

	private boolean viewResolution(final Instruction ins, final ExecContext ctx) {
		final int idx = readCameraIndex(ins, 0, ctx);
		if ((idx < 0) || (idx >= ctx.cameras.length)) {
			writeInt2(out, 1, 1);
			return true;
		}
		writeInt2(out, ctx.cameras[idx].resolution[0], ctx.cameras[idx].resolution[1]);
		return true;
	}

	private boolean viewAspect(final Instruction ins, final ExecContext ctx) {
		final int idx = readCameraIndex(ins, 0, ctx);
		final int[] res = ((idx >= 0) && (idx < ctx.cameras.length)) ? ctx.cameras[idx].resolution
				: new int[] { 1, 1 };
		out.setScalar((float) res[0] / (float) res[1]);
		return true;
	}

	private boolean viewPosition(final Instruction ins, final ExecContext ctx) {
		final int idx = readCameraIndex(ins, 0, ctx);
		if ((idx < 0) || (idx >= ctx.cameras.length)) {
			final float inf = Float.intBitsToFloat(INF_BITS);
			out.setFloat3(inf, inf, inf);
			return true;
		}
		final float[] p = ctx.cameras[idx].position;
		out.setFloat3(p[0], p[1], p[2]);
		return true;
	}

	private boolean viewDistance(final Instruction ins, final ExecContext ctx) {
		if (!readValueArg(ins, 0, ctx, r0)) {
			return false;
		}
		final int idx = readCameraIndex(ins, 1, ctx);
		if ((idx < 0) || (idx >= ctx.cameras.length)) {
			out.setScalar(Float.intBitsToFloat(INF_BITS));
			return true;
		}
		final float[] eye = ctx.cameras[idx].position;
		final float dx = r0.lanes[0] - eye[0];
		final float dy = r0.lanes[1] - eye[1];
		final float dz = r0.lanes[2] - eye[2];
		out.setScalar((float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz)));
		return true;
	}

	private boolean viewAxis(final Instruction ins, final ExecContext ctx, final int column, final float sign) {
		final int idx = readCameraIndex(ins, 0, ctx);
		float[] axis;
		if ((idx >= 0) && (idx < ctx.cameras.length)) {
			axis = ctx.cameras[idx].basis[column];
		} else {
			axis = new float[] { column == 0 ? 1 : 0, column == 1 ? 1 : 0, column == 2 ? 1 : 0 };
		}
		out.setFloat3(axis[0] * sign, axis[1] * sign, axis[2] * sign);
		return true;
	}

	private boolean effectAxis(final ExecContext ctx, final int column, final float sign) {
		float ax = ctx.sceneL2W.m[0][column];
		float ay = ctx.sceneL2W.m[1][column];
		float az = ctx.sceneL2W.m[2][column];
		final float lenSq = (ax * ax) + (ay * ay) + (az * az);
		if (lenSq > 0) {
			final float inv = 1.0f / (float) Math.sqrt(lenSq);
			ax *= inv;
			ay *= inv;
			az *= inv;
		} else {
			ax = column == 0 ? 1 : 0;
			ay = column == 1 ? 1 : 0;
			az = column == 2 ? 1 : 0;
		}
		out.setFloat3(ax * sign, ay * sign, az * sign);
		return true;
	}
}
