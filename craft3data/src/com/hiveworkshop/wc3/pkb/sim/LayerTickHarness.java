package com.hiveworkshop.wc3.pkb.sim;

import com.hiveworkshop.wc3.pkb.bind.ExternalBinding;
import com.hiveworkshop.wc3.pkb.bind.LayerProgram;
import com.hiveworkshop.wc3.pkb.bind.ProgramDescriptor;
import com.hiveworkshop.wc3.pkb.vm.Bank;
import com.hiveworkshop.wc3.pkb.vm.ExecContext;
import com.hiveworkshop.wc3.pkb.vm.Instruction;
import com.hiveworkshop.wc3.pkb.vm.Interpreter;
import com.hiveworkshop.wc3.pkb.vm.RegisterValue;
import com.hiveworkshop.wc3.pkb.vm.VmException;

/** One particle: its register banks, external view, RNG, life and spawn payload state. */
public final class LayerTickHarness {
	public static final int FALLBACK_EXTERNAL_COUNT = 1024;
	public static final int FALLBACK_REGISTER_COUNT = 1024;

	private final RegisterValue[][] scopeRegisters = new RegisterValue[Bank.SCOPE_BUCKETS][];
	private ExternalStore externalStore;
	private int externalIndex;
	private int externalCount;
	private final FastRand rng = new FastRand();
	private float effectAge;
	private float initSceneTime;
	private boolean effectIsRunning = true;
	private float timeWindowEnd;
	private float timeWindowStart;
	private final ExecContext.Mat4x3 sceneL2W = new ExecContext.Mat4x3();
	private ExecContext.SceneCamera[] cameras = new ExecContext.SceneCamera[0];
	private float simLod;
	private float simLodDistanceMin = 5;
	private float simLodDistanceMax = 200;
	private final float[] spawnTranslate = new float[3];
	private final float[] spawnQuat = { 0, 0, 0, 1 };
	private final float[] spawnScale = { 1, 1, 1 };
	private boolean hasSpawnIntPayload;
	private int spawnIntPayloadWidth;
	private final int[] spawnIntPayload = new int[4];
	private int spawnIntPayloadId;
	private boolean hasSpawnBoolPayload;
	private int spawnBoolPayloadWidth;
	private final int[] spawnBoolPayload = new int[4];
	private int spawnBoolPayloadId;
	private int spawnPositionPayloadId;
	private boolean spawnFrameLocal;
	private int spawnOrientationPayloadId;
	private final SpawnEvent.PayloadFloatSlot[] spawnFloatSlots = SpawnEvent.newFloatSlots();
	private SpawnEvent.Queue spawnQueue;
	private ProximityHash[] spatialHashes = new ProximityHash[0];
	private ProximityHash[] spatialHashesWrite = new ProximityHash[0];
	private long selfId;
	private long parentSelfId;
	private int parentRngState;
	private boolean inInitScope;
	private boolean wasDeadAtFrameStart;
	private float lifeRatio;

	public LayerTickHarness(final int[] registersPerBank) {
		for (int b = 0; b < Bank.SCOPE_BUCKETS; b++) {
			scopeRegisters[b] = newBank(registersPerBank == null ? FALLBACK_REGISTER_COUNT : registersPerBank[b]);
		}
	}

	private static RegisterValue[] newBank(final int size) {
		final RegisterValue[] bank = new RegisterValue[size];
		for (int i = 0; i < size; i++) {
			bank[i] = new RegisterValue();
		}
		return bank;
	}

	/**
	 * Registers per scope bank: the highest index any instruction of the layer
	 * touches plus one (the declared counts are far larger than what is used),
	 * falling back to the declared counts when a program could not be decoded.
	 */
	public static int[] registersPerBankFor(final LayerProgram layer) {
		final int[] perBank = new int[Bank.SCOPE_BUCKETS];
		boolean anyCount = false;
		boolean undecoded = false;
		for (final ProgramDescriptor s : layer.scopePrograms()) {
			if ((s.instructions.length == 0) && (s.bytecode.length > 0)) {
				undecoded = true;
			}
			for (final Instruction ins : s.instructions) {
				noteRegisters(ins, perBank);
			}
			for (int scopeIx = 0; scopeIx < Bank.SCOPE_BUCKETS; scopeIx++) {
				if (s.registerCounts[scopeIx + 1] > 0) {
					anyCount = true;
				}
			}
		}
		if (undecoded) {
			for (final ProgramDescriptor s : layer.scopePrograms()) {
				for (int scopeIx = 0; scopeIx < Bank.SCOPE_BUCKETS; scopeIx++) {
					perBank[scopeIx] = Math.max(perBank[scopeIx], s.registerCounts[scopeIx + 1]);
				}
			}
		}
		if (!anyCount && !undecoded) {
			boolean anyUsed = false;
			for (final int n : perBank) {
				anyUsed |= n > 0;
			}
			if (!anyUsed) {
				java.util.Arrays.fill(perBank, FALLBACK_REGISTER_COUNT);
			}
		}
		return perBank;
	}

	private static void noteRegister(final int regId, final int[] perBank) {
		if (regId == Bank.REG_VOID) {
			return;
		}
		final int scope = Bank.scopeOf(regId);
		if (scope == Bank.SCOPE_CONST) {
			return;
		}
		final int idx = Bank.localIndexOf(regId) + 1;
		if (idx > perBank[scope]) {
			perBank[scope] = idx;
		}
	}

	private static void noteRegisters(final Instruction ins, final int[] perBank) {
		switch (ins.opcode) {
		case LOAD_EXTERNAL:
		case STORE_TO_EXTERNAL:
			noteRegister(ins.operands[0], perBank);
			return;
		case REINTERPRET:
		case TYPE_CONVERTER:
		case BROADCAST:
			noteRegister(ins.operands[0], perBank);
			noteRegister(ins.operands[1], perBank);
			return;
		case VEC_CTOR:
			noteRegister(ins.operands[1], perBank);
			for (final int reg : ins.extraOperands) {
				noteRegister(reg, perBank);
			}
			return;
		case VEC_SWIZZLE:
			noteRegister(ins.operands[1], perBank);
			noteRegister(ins.operands[2], perBank);
			return;
		case MATH_OP:
		case MATH_OP_CMETA:
		case MATH_FUNC1:
		case MATH_FUNC2:
		case MATH_FUNC3:
			for (int i = 1; i < ins.operandCount; i++) {
				noteRegister(ins.operands[i], perBank);
			}
			return;
		case SELECT:
		case MATH_OP_ADD:
		case MATH_OP_SUB:
		case MATH_OP_MUL:
		case MATH_OP_DIV:
		case MADD:
		case IDIV_MUL_INV:
			for (int i = 0; i < ins.operandCount; i++) {
				noteRegister(ins.operands[i], perBank);
			}
			return;
		case FUNCTION_CALL:
			noteRegister(ins.operands[4], perBank);
			for (int i = 0; i < ins.argc(); i++) {
				noteRegister(ins.argReg(i), perBank);
			}
			return;
		default:
			return;
		}
	}

	public static int externalStorageSizeFor(final LayerProgram layer) {
		int needed = 0;
		boolean sawAnything = false;
		for (final ProgramDescriptor s : layer.scopePrograms()) {
			if (s.externals.length > 0) {
				sawAnything = true;
				needed = Math.max(needed, s.externals.length);
				for (final ExternalBinding b : s.externals) {
					needed = Math.max(needed, b.resolvedSlot() + 1);
				}
			}
			if ((s.instructions.length == 0) && (s.bytecode.length > 0)) {
				return FALLBACK_EXTERNAL_COUNT;
			}
			for (final Instruction ins : s.instructions) {
				int byteSlot;
				switch (ins.opcode) {
				case LOAD_EXTERNAL:
				case STORE_TO_EXTERNAL:
					byteSlot = ins.operands[1];
					break;
				case EXTERNAL_CLEAR:
					byteSlot = ins.operands[0];
					break;
				default:
					continue;
				}
				sawAnything = true;
				needed = Math.max(needed, (byteSlot & 0xFFFF) + 1);
			}
		}
		if (!sawAnything) {
			return FALLBACK_EXTERNAL_COUNT;
		}
		return Math.min(needed, FALLBACK_EXTERNAL_COUNT);
	}

	public void resizeForLayer(final LayerProgram layer) {
		final int[] perBank = registersPerBankFor(layer);
		for (int b = 0; b < Bank.SCOPE_BUCKETS; b++) {
			if (scopeRegisters[b].length != perBank[b]) {
				final RegisterValue[] fresh = newBank(perBank[b]);
				final int keep = Math.min(fresh.length, scopeRegisters[b].length);
				for (int i = 0; i < keep; i++) {
					fresh[i].set(scopeRegisters[b][i]);
				}
				scopeRegisters[b] = fresh;
			}
		}
	}

	public void bindExternals(final ExternalStore store, final int index, final int count) {
		externalStore = store;
		externalIndex = index;
		externalCount = count;
	}

	public RegisterValue[] scopeRegs(final int scope) {
		return scopeRegisters[scope];
	}

	public ExternalStore.View externals() {
		final ExternalStore.View v = new ExternalStore.View();
		v.store = externalStore;
		v.particle = externalIndex;
		v.count = externalCount;
		return v;
	}

	private void setExternal(final int slot, final RegisterValue v) {
		externalStore.store(slot, externalIndex, v);
	}

	public float externalLane(final int slot, final int lane) {
		return externalStore.loadLane(slot, externalIndex, lane);
	}

	public void setExternalScalar(final int slot, final float v) {
		final RegisterValue r = new RegisterValue();
		r.setScalar(v);
		setExternal(slot, r);
	}

	public void setExternalFloat4(final int slot, final float[] value) {
		final RegisterValue r = new RegisterValue();
		r.lanes[0] = value[0];
		r.lanes[1] = value[1];
		r.lanes[2] = value[2];
		r.lanes[3] = value[3];
		r.componentCount = 4;
		setExternal(slot, r);
	}

	public int externalCount() {
		return externalCount;
	}

	// ---- setters mirroring the reference

	public void setRngSeed(final int seed) {
		rng.setState(seed);
	}

	public int rngState() {
		return rng.state();
	}

	public void setEffectAge(final float age) {
		effectAge = age;
	}

	public void setInitSceneTime(final float t) {
		initSceneTime = t;
	}

	public void setEffectIsRunning(final boolean running) {
		effectIsRunning = running;
	}

	public void setTimeWindowEnd(final float end) {
		timeWindowEnd = end;
	}

	public void setTimeWindowStart(final float start) {
		timeWindowStart = start;
	}

	public void advanceLifeRatio(final float dtTimesInvLife) {
		lifeRatio += dtTimesInvLife;
		if (!(lifeRatio < 1.0f)) {
			lifeRatio = 1.0f;
		}
	}

	public void markDead() {
		lifeRatio = 1.0f;
	}

	public float lifeRatio() {
		return lifeRatio;
	}

	public boolean isDead() {
		return (Float.floatToRawIntBits(lifeRatio) & 0xFFFFFFFFL) >= 0x3F800000L;
	}

	public void noteFrameStartDeadState() {
		wasDeadAtFrameStart = isDead();
	}

	public boolean wasDeadAtFrameStart() {
		return wasDeadAtFrameStart;
	}

	public void setSceneL2W(final ExecContext.Mat4x3 m) {
		sceneL2W.set(m);
	}

	public void setCameras(final ExecContext.SceneCamera[] cameras) {
		this.cameras = cameras;
	}

	public void setSimLod(final float level) {
		simLod = level;
	}

	public void setSimLodDistances(final float minDist, final float maxDist) {
		simLodDistanceMin = minDist;
		simLodDistanceMax = maxDist;
	}

	public void setSpawnTRS(final float[] translate, final float[] quaternion, final float[] scale) {
		System.arraycopy(translate, 0, spawnTranslate, 0, 3);
		System.arraycopy(quaternion, 0, spawnQuat, 0, 4);
		System.arraycopy(scale, 0, spawnScale, 0, 3);
	}

	public void setSpawnPositionPayloadId(final int id) {
		spawnPositionPayloadId = id;
	}

	public void setSpawnFrameLocal(final boolean local) {
		spawnFrameLocal = local;
	}

	public void setSpawnOrientationPayloadId(final int id) {
		spawnOrientationPayloadId = id;
	}

	public void setSpawnFloatSlots(final SpawnEvent.PayloadFloatSlot[] slots) {
		SpawnEvent.copyFloatSlots(slots, spawnFloatSlots);
	}

	public void setSpawnIntPayload(final int width, final int[] value, final int payloadId) {
		spawnIntPayloadWidth = width;
		System.arraycopy(value, 0, spawnIntPayload, 0, 4);
		spawnIntPayloadId = payloadId;
		hasSpawnIntPayload = true;
	}

	public void clearSpawnIntPayload() {
		hasSpawnIntPayload = false;
		spawnIntPayloadWidth = 0;
		java.util.Arrays.fill(spawnIntPayload, 0);
		spawnIntPayloadId = 0;
	}

	public void setSpawnBoolPayload(final int width, final int[] value, final int payloadId) {
		spawnBoolPayloadWidth = width;
		System.arraycopy(value, 0, spawnBoolPayload, 0, 4);
		spawnBoolPayloadId = payloadId;
		hasSpawnBoolPayload = true;
	}

	public void clearSpawnBoolPayload() {
		hasSpawnBoolPayload = false;
		spawnBoolPayloadWidth = 0;
		java.util.Arrays.fill(spawnBoolPayload, 0);
		spawnBoolPayloadId = 0;
	}

	public void setSpawnQueue(final SpawnEvent.Queue queue) {
		spawnQueue = queue;
	}

	public void setSpatialHashes(final ProximityHash[] read, final ProximityHash[] write) {
		spatialHashes = read;
		spatialHashesWrite = write;
	}

	public void setSelfId(final long id) {
		selfId = id;
	}

	public long selfId() {
		return selfId;
	}

	public void setParentIdentity(final long parentSelfId, final int parentRngState) {
		this.parentSelfId = parentSelfId;
		this.parentRngState = parentRngState;
	}

	public long parentSelfId() {
		return parentSelfId;
	}

	// ---- execution

	private void bindContext(final ProgramDescriptor scope, final LayerProgram layer, final ExecContext ctx) {
		ctx.layerWorldSpace = layer.simulatesInWorldSpace();
		ctx.layerId = layer.id;
		ctx.resetPerScopeRun();
		for (int s = 0; s < Bank.SCOPE_BUCKETS; s++) {
			ctx.scopeRegisters[s] = scopeRegisters[s];
		}
		ctx.externals.store = externalStore;
		ctx.externals.particle = externalIndex;
		ctx.externals.count = externalCount;
		ctx.constantsPool = scope.constantsPool;
		ctx.functions = scope.functions;
		ctx.externalBindings = scope.externals;
		ctx.samplers = layer.samplers;
		ctx.spatialLayers = layer.spatialLayers;
		ctx.kickedEventDecls = layer.kickedEventDecls;
		ctx.rootEventDecl = layer.rootEventDecl;
		ctx.spatialHashes = spatialHashes;
		ctx.spatialHashesWrite = spatialHashesWrite;
		ctx.rng = rng;
		ctx.effectAge = effectAge;
		ctx.effectIsRunning = effectIsRunning;
		ctx.sceneL2W.set(sceneL2W);
		ctx.cameras = cameras;
		ctx.simLod = simLod;
		ctx.simLodDistanceMin = simLodDistanceMin;
		ctx.simLodDistanceMax = simLodDistanceMax;
		System.arraycopy(spawnTranslate, 0, ctx.spawnTranslate, 0, 3);
		System.arraycopy(spawnQuat, 0, ctx.spawnQuat, 0, 4);
		System.arraycopy(spawnScale, 0, ctx.spawnScale, 0, 3);
		ctx.inInitScope = inInitScope;
		ctx.spawnQueue = spawnQueue;
		ctx.timeWindowEnd = timeWindowEnd;
		ctx.timeWindowStart = timeWindowStart;
		ctx.currentSelfId = selfId;
		ctx.hasSpawnIntPayload = hasSpawnIntPayload;
		ctx.spawnIntPayloadWidth = spawnIntPayloadWidth;
		System.arraycopy(spawnIntPayload, 0, ctx.spawnIntPayload, 0, 4);
		ctx.spawnIntPayloadId = spawnIntPayloadId;
		ctx.hasSpawnBoolPayload = hasSpawnBoolPayload;
		ctx.spawnBoolPayloadWidth = spawnBoolPayloadWidth;
		System.arraycopy(spawnBoolPayload, 0, ctx.spawnBoolPayload, 0, 4);
		ctx.spawnBoolPayloadId = spawnBoolPayloadId;
		ctx.spawnPositionPayloadId = spawnPositionPayloadId;
		ctx.spawnFrameLocal = spawnFrameLocal;
		ctx.spawnOrientationPayloadId = spawnOrientationPayloadId;
		SpawnEvent.copyFloatSlots(spawnFloatSlots, ctx.spawnFloatSlots);
	}

	private boolean runScope(final ProgramDescriptor scope, final LayerProgram layer, final Interpreter vm,
			final ExecContext ctx, final SimIssues issues) {
		if (scope.isEmpty()) {
			return true;
		}
		bindContext(scope, layer, ctx);
		boolean ok = true;
		try {
			vm.run(scope.instructions, ctx);
		} catch (final VmException e) {
			issues.report(layer, e.getMessage());
			ok = false;
		} catch (final RuntimeException e) {
			issues.report(layer, e.toString());
			ok = false;
		}
		if (ctx.selfKillRequested) {
			markDead();
		}
		return ok;
	}

	public boolean initParticle(final LayerProgram layer, final Interpreter vm, final ExecContext ctx,
			final SimIssues issues) {
		for (final RegisterValue[] bank : scopeRegisters) {
			for (final RegisterValue r : bank) {
				r.clear();
			}
		}
		externalStore.clearParticle(externalIndex);
		for (final LayerProgram.AttributeDefault attr : layer.attributeDefaults) {
			final ExternalBinding hit = layer.findBindingAcrossScopes(attr.name);
			if (hit == null) {
				continue;
			}
			final int slot = hit.resolvedSlot();
			if (slot >= externalCount) {
				continue;
			}
			setExternalFloat4(slot, attr.defaultValue);
		}
		for (final LayerProgram.EventExternal evt : layer.eventExternals) {
			final ExternalBinding hit = layer.findBindingAcrossScopes(evt.externalName);
			if (hit == null) {
				continue;
			}
			final int slot = hit.resolvedSlot();
			if (slot >= externalCount) {
				continue;
			}
			setExternal(slot, RegisterValue.scalarI(evt.globalEventSlotId));
		}
		final ExternalBinding st = ExternalBinding.findByName(layer.initProgram.externals, "scene.time");
		if (st != null) {
			final int slot = st.resolvedSlot();
			if (slot < externalCount) {
				setExternalScalar(slot, initSceneTime);
			}
		}
		lifeRatio = 0;
		inInitScope = true;
		final boolean ok = runScope(layer.initProgram, layer, vm, ctx, issues);
		inInitScope = false;
		return ok;
	}

	public boolean tick(final LayerProgram layer, final Interpreter vm, final ExecContext ctx, final SimIssues issues) {
		return runScope(layer.evolveProgram(), layer, vm, ctx, issues);
	}
}
