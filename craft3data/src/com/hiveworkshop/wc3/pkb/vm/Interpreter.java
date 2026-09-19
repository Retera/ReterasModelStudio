package com.hiveworkshop.wc3.pkb.vm;

import com.hiveworkshop.wc3.pkb.bind.ExternalBinding;

/**
 * Scalar (one particle at a time) interpreter of the CBEM/IR instruction set.
 * Not thread safe: it keeps scratch registers as fields.
 */
public final class Interpreter {
	// MathOp sub ids
	public static final int OP_ADD = 0, OP_SUB = 1, OP_MUL = 2, OP_DIV = 3, OP_MOD = 4, OP_NEG = 5, OP_SHL = 6,
			OP_SHR = 7, OP_BIT_AND = 8, OP_BIT_OR = 9, OP_BIT_XOR = 10, OP_BIT_NOT = 11, OP_LT = 12, OP_LE = 13,
			OP_GT = 14, OP_GE = 15, OP_EQ = 16, OP_NE = 17;

	private final RegisterValue a = new RegisterValue();
	private final RegisterValue b = new RegisterValue();
	private final RegisterValue c = new RegisterValue();
	private final RegisterValue d = new RegisterValue();
	private final RegisterValue out = new RegisterValue();
	private final float[] sinCos = new float[2];
	private final float[] flat = new float[4];
	private final Dispatchers dispatchers = new Dispatchers(this);
	private final java.util.Set<Integer> warnedOps = new java.util.HashSet<>();

	public Dispatchers getDispatchers() {
		return dispatchers;
	}

	// ------------------------------------------------------------------ registers

	public static boolean isConstPoolHit(final ExecContext ctx, final int regId) {
		if (Bank.scopeOf(regId) != Bank.SCOPE_CONST) {
			return false;
		}
		final int off = Bank.localIndexOf(regId) * 32;
		return (off + 16) <= ctx.constantsPool.length;
	}

	public static void readConst(final ExecContext ctx, final int slot, final int bank, final RegisterValue out) {
		final int off = slot * 32;
		if ((off + 16) > ctx.constantsPool.length) {
			throw new VmException("VM: constant-pool slot out of range");
		}
		final byte[] p = ctx.constantsPool;
		for (int lane = 0; lane < 4; lane++) {
			final int o = off + (lane * 4);
			final int bits = (p[o] & 0xFF) | ((p[o + 1] & 0xFF) << 8) | ((p[o + 2] & 0xFF) << 16)
					| ((p[o + 3] & 0xFF) << 24);
			out.lanes[lane] = Float.intBitsToFloat(bits);
		}
		out.typeBank = bank;
		out.componentCount = Bank.componentCountForBank(bank);
	}

	public static void readSrc(final ExecContext ctx, final int regId, final RegisterValue out) {
		if (regId == Bank.REG_VOID) {
			out.clear();
			out.componentCount = 1;
			out.typeBank = Bank.FLOAT;
			return;
		}
		final int bank = Bank.bankOf(regId);
		final int scope = Bank.scopeOf(regId);
		final int idx = Bank.localIndexOf(regId);
		if (isConstPoolHit(ctx, regId)) {
			readConst(ctx, idx, bank, out);
			return;
		}
		final RegisterValue[] regs = ctx.scopeRegisters[scope];
		if ((regs == null) || (idx >= regs.length)) {
			throw new VmException("VM: register index out of bounds (scope " + scope + ", idx " + idx + ")");
		}
		out.set(regs[idx]);
		if (out.componentCount == 0) {
			out.componentCount = Bank.componentCountForBank(bank);
			out.typeBank = bank;
		}
	}

	public static void writeDst(final ExecContext ctx, final int regId, final RegisterValue v) {
		final int bank = Bank.bankOf(regId);
		final int scope = Bank.scopeOf(regId);
		final int idx = Bank.localIndexOf(regId);
		if (isConstPoolHit(ctx, regId)) {
			throw new VmException("VM: write to constant-pool register");
		}
		final RegisterValue[] regs = ctx.scopeRegisters[scope];
		if ((regs == null) || (idx >= regs.length)) {
			throw new VmException("VM: register index out of bounds (scope " + scope + ", idx " + idx + ")");
		}
		final RegisterValue stored = regs[idx];
		stored.set(v);
		if (stored.componentCount == 0) {
			stored.componentCount = Bank.componentCountForBank(bank);
		}
		if (stored.typeBank == 0) {
			stored.typeBank = bank;
		}
	}

	public static int canonicalExternalSlot(final ExecContext ctx, final int byteSlot) {
		if (byteSlot < ctx.externalBindings.length) {
			final ExternalBinding b = ctx.externalBindings[byteSlot];
			if ((b.canonicalSlot == 0) && (b.slot != 0)) {
				return b.slot;
			}
			return b.canonicalSlot;
		}
		return byteSlot;
	}

	// ------------------------------------------------------------------ run

	/** Runs the program to completion; returns the number of instructions executed. */
	public int run(final Instruction[] program, final ExecContext ctx) {
		int executed = 0;
		for (final Instruction ins : program) {
			step(ins, ctx);
			executed++;
		}
		return executed;
	}

	public void step(final Instruction ins, final ExecContext ctx) {
		switch (ins.opcode) {
		case NOP:
			return;
		case LOAD_EXTERNAL:
			execLoadExternal(ins, ctx);
			return;
		case STORE_TO_EXTERNAL:
			execStoreToExternal(ins, ctx);
			return;
		case REINTERPRET:
		case TYPE_CONVERTER:
			execMove(ins, ctx);
			return;
		case VEC_CTOR:
			execVecCtor(ins, ctx);
			return;
		case VEC_SWIZZLE:
			execVecSwizzle(ins, ctx);
			return;
		case MATH_OP:
		case MATH_OP_CMETA:
			execMathOp(ins, ctx, ins.operands[0], ins.operands[1], ins.operands[2], ins.operands[3]);
			return;
		case MATH_FUNC1:
			execMathFunc1(ins, ctx);
			return;
		case MATH_FUNC2:
			execMathFunc2(ins, ctx);
			return;
		case MATH_FUNC3:
			execMathFunc3(ins, ctx);
			return;
		case SELECT:
			execSelect(ins, ctx);
			return;
		case FUNCTION_CALL:
			dispatchers.execFunctionCall(ins, ctx);
			return;
		case EXTERNAL_CLEAR:
			execExternalClear(ins, ctx);
			return;
		case BROADCAST:
			execBroadcast(ins, ctx);
			return;
		case MATH_OP_ADD:
			execMathOp(ins, ctx, OP_ADD, ins.operands[0], ins.operands[1], ins.operands[2]);
			return;
		case MATH_OP_SUB:
			execMathOp(ins, ctx, OP_SUB, ins.operands[0], ins.operands[1], ins.operands[2]);
			return;
		case MATH_OP_MUL:
			execMathOp(ins, ctx, OP_MUL, ins.operands[0], ins.operands[1], ins.operands[2]);
			return;
		case MATH_OP_DIV:
			execMathOp(ins, ctx, OP_DIV, ins.operands[0], ins.operands[1], ins.operands[2]);
			return;
		case IDIV_MUL_INV:
			execIDivMulInv(ins, ctx);
			return;
		case MADD:
			execMadd(ins, ctx);
			return;
		case FUNCTION_PROLOG:
			ctx.functionDepth++;
			return;
		case FUNCTION_EPILOG:
			if (ctx.functionDepth == 0) {
				throw new VmException("CBEM: FunctionEpilog without matching Prolog");
			}
			ctx.functionDepth--;
			return;
		default:
			throw new VmException("VM: opcode not in IR or CBEM range: " + ins.opcode);
		}
	}

	// ------------------------------------------------------------------ opcodes

	private void execLoadExternal(final Instruction ins, final ExecContext ctx) {
		final int dstReg = ins.operands[0];
		final int byteSlot = ins.operands[1] & 0xFFFF;
		final int bank = Bank.bankOf(dstReg);
		final int slot = canonicalExternalSlot(ctx, byteSlot);
		if (slot >= ctx.externals.size()) {
			throw new VmException("IR: LoadExternal slot out of bounds");
		}
		ctx.externals.get(slot, out);
		if (out.componentCount == 0) {
			out.componentCount = Bank.componentCountForBank(bank);
			out.typeBank = bank;
		}
		if (bank == Bank.HANDLE) {
			boolean replaced = false;
			for (int i = 0; i < ctx.handleRegisterCount; i++) {
				if (ctx.handleRegisterSlots[i].reg == dstReg) {
					ctx.handleRegisterSlots[i].slot = byteSlot;
					replaced = true;
					break;
				}
			}
			if (!replaced && (ctx.handleRegisterCount < ctx.handleRegisterSlots.length)) {
				ctx.handleRegisterSlots[ctx.handleRegisterCount].reg = dstReg;
				ctx.handleRegisterSlots[ctx.handleRegisterCount].slot = byteSlot;
				ctx.handleRegisterCount++;
			}
		}
		writeDst(ctx, dstReg, out);
	}

	private void execStoreToExternal(final Instruction ins, final ExecContext ctx) {
		final int srcReg = ins.operands[0];
		final int byteSlot = ins.operands[1] & 0xFFFF;
		final int slot = canonicalExternalSlot(ctx, byteSlot);
		if (slot >= ctx.externals.size()) {
			throw new VmException("IR: StoreToExternal slot out of bounds");
		}
		readSrc(ctx, srcReg, a);
		ctx.externals.set(slot, a);
	}

	private void execExternalClear(final Instruction ins, final ExecContext ctx) {
		final int byteSlot = ins.operands[0] & 0xFFFF;
		final int slot = canonicalExternalSlot(ctx, byteSlot);
		if (slot >= ctx.externals.size()) {
			throw new VmException("CBEM: external slot out of bounds");
		}
		out.setScalar(0);
		ctx.externals.set(slot, out);
	}

	private void execMove(final Instruction ins, final ExecContext ctx) {
		final int dstReg = ins.operands[0];
		final int srcReg = ins.operands[1];
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		final boolean isTypeConvert = ins.opcode == Opcode.TYPE_CONVERTER;
		readSrc(ctx, srcReg, a);
		if (!isTypeConvert) {
			a.typeBank = dstBank;
			a.componentCount = dstComps;
			writeDst(ctx, dstReg, a);
			return;
		}
		final int dstFam = Bank.scalarFamily(dstBank);
		final int srcFam = Bank.scalarFamily(a.typeBank);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		final int lanesToConvert = Math.min(dstComps, a.componentCount);
		for (int i = 0; i < lanesToConvert; i++) {
			if ((srcFam == Bank.FAMILY_FLOAT) && (dstFam == Bank.FAMILY_INT)) {
				out.setLaneInt(i, (int) a.lanes[i]);
			} else if ((srcFam == Bank.FAMILY_INT) && (dstFam == Bank.FAMILY_FLOAT)) {
				out.lanes[i] = a.laneAsInt(i);
			} else if ((srcFam == Bank.FAMILY_BOOL) && (dstFam == Bank.FAMILY_FLOAT)) {
				out.lanes[i] = (a.laneAsInt(i) != 0) ? 1.0f : 0.0f;
			} else if ((srcFam == Bank.FAMILY_BOOL) && (dstFam == Bank.FAMILY_INT)) {
				out.setLaneInt(i, (a.laneAsInt(i) != 0) ? 1 : 0);
			} else {
				out.lanes[i] = a.lanes[i];
			}
		}
		writeDst(ctx, dstReg, out);
	}

	private void execVecCtor(final Instruction ins, final ExecContext ctx) {
		final int argc = ins.operands[0];
		final int dstReg = ins.operands[1];
		final int srcCount = argc + 1;
		if ((srcCount > 4) || (ins.extraOperands.length != srcCount)) {
			throw new VmException("IR: VecCtor source count mismatch");
		}
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		int flatCount = 0;
		for (int i = 0; i < srcCount; i++) {
			readSrc(ctx, ins.extraOperands[i], a);
			final int srcComps = a.componentCount > 0 ? a.componentCount : 1;
			for (int j = 0; (j < srcComps) && (flatCount < 4); j++) {
				flat[flatCount++] = a.lanes[j];
			}
		}
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		if ((flatCount == 1) && (dstComps > 1)) {
			for (int i = 0; i < dstComps; i++) {
				out.lanes[i] = flat[0];
			}
		} else {
			for (int i = 0; i < dstComps; i++) {
				out.lanes[i] = i < flatCount ? flat[i] : 0;
			}
		}
		writeDst(ctx, dstReg, out);
	}

	private void execVecSwizzle(final Instruction ins, final ExecContext ctx) {
		final int maskOperand = ins.operands[0];
		final int dstReg = ins.operands[1];
		final int srcReg = ins.operands[2];
		final int b2 = (maskOperand >>> 8) & 0xFF;
		final int b3 = (maskOperand >>> 16) & 0xFF;
		final int packed = ((b3 & 0xF0) << 4) | b2;
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		readSrc(ctx, srcReg, a);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		for (int i = 0; (i < dstComps) && (i < 4); i++) {
			final int code = (packed >>> (3 * i)) & 0x7;
			switch (code) {
			case 0:
			case 1:
			case 2:
			case 3:
				out.lanes[i] = a.lanes[code];
				break;
			case 4:
				out.lanes[i] = 0;
				break;
			case 5:
				if (dstBank == Bank.BOOL) {
					out.setLaneInt(i, Bank.BOOL_TRUE_BITS);
				} else if (Bank.isIntegral(dstBank)) {
					out.setLaneInt(i, 1);
				} else {
					out.lanes[i] = 1.0f;
				}
				break;
			default:
				throw new VmException("IR: VecSwizzle component code out of range");
			}
		}
		writeDst(ctx, dstReg, out);
	}

	private void execMathOp(final Instruction ins, final ExecContext ctx, final int op, final int dstReg,
			final int src0Reg, final int src1Reg) {
		final int dstBank = Bank.bankOf(dstReg);
		final int components = Bank.componentCountForBank(dstBank);
		final boolean integerOp = Bank.isIntegral(dstBank);
		readSrc(ctx, src0Reg, a);
		readSrc(ctx, src1Reg, b);
		out.clear();
		out.typeBank = dstBank;
		applyMathOp(op, a, b, components, integerOp, out);
		writeDst(ctx, dstReg, out);
	}

	public void applyMathOp(final int op, final RegisterValue a, final RegisterValue b, final int components,
			final boolean integerOp, final RegisterValue out) {
		out.componentCount = components;
		for (int i = 0; i < components; i++) {
			if (!integerOp) {
				final float av = a.lanes[i];
				final float bv = b.lanes[i];
				float r;
				switch (op) {
				case OP_ADD:
					r = av + bv;
					break;
				case OP_SUB:
					r = av - bv;
					break;
				case OP_MUL:
					r = av * bv;
					break;
				case OP_DIV:
					r = av / bv;
					break;
				case OP_MOD:
					r = fmod(av, bv);
					break;
				case OP_NEG:
					r = -av;
					break;
				case OP_LT:
					out.setLaneInt(i, av < bv ? 1 : 0);
					continue;
				case OP_LE:
					out.setLaneInt(i, av <= bv ? 1 : 0);
					continue;
				case OP_GT:
					out.setLaneInt(i, av > bv ? 1 : 0);
					continue;
				case OP_GE:
					out.setLaneInt(i, av >= bv ? 1 : 0);
					continue;
				case OP_EQ:
					out.setLaneInt(i, av == bv ? 1 : 0);
					continue;
				case OP_NE:
					out.setLaneInt(i, av != bv ? 1 : 0);
					continue;
				default:
					warnUnknownOp(op);
					r = av;
					break;
				}
				out.lanes[i] = r;
			} else {
				final int av = a.laneAsInt(i);
				final int bv = b.laneAsInt(i);
				int r;
				switch (op) {
				case OP_ADD:
					r = av + bv;
					break;
				case OP_SUB:
					r = av - bv;
					break;
				case OP_MUL:
					r = av * bv;
					break;
				case OP_DIV:
					r = bv == 0 ? 0 : av / bv;
					break;
				case OP_MOD:
					r = bv == 0 ? 0 : av % bv;
					break;
				case OP_NEG:
					r = -av;
					break;
				case OP_SHL:
					r = av << (bv & 31);
					break;
				case OP_SHR:
					r = av >> (bv & 31);
					break;
				case OP_BIT_AND:
					r = av & bv;
					break;
				case OP_BIT_OR:
					r = av | bv;
					break;
				case OP_BIT_XOR:
					r = av ^ bv;
					break;
				case OP_BIT_NOT:
					r = ~av;
					break;
				case OP_LT:
					r = av < bv ? 1 : 0;
					break;
				case OP_LE:
					r = av <= bv ? 1 : 0;
					break;
				case OP_GT:
					r = av > bv ? 1 : 0;
					break;
				case OP_GE:
					r = av >= bv ? 1 : 0;
					break;
				case OP_EQ:
					r = av == bv ? 1 : 0;
					break;
				case OP_NE:
					r = av != bv ? 1 : 0;
					break;
				default:
					warnUnknownOp(op);
					r = av;
					break;
				}
				out.setLaneInt(i, r);
			}
		}
	}

	private void warnUnknownOp(final int op) {
		if (warnedOps.add(op)) {
			System.err.println("[pkb vm] MathOp sub-id not implemented (engine no-op): " + op);
		}
	}

	/** C fmod: result has the sign of the dividend. */
	public static float fmod(final float x, final float y) {
		if ((y == 0) || Float.isInfinite(x) || Float.isNaN(x) || Float.isNaN(y)) {
			return Float.NaN;
		}
		if (Float.isInfinite(y)) {
			return x;
		}
		return (float) (x - (y * (double) (float) truncate(x / y)));
	}

	private static float truncate(final float v) {
		return (float) (v < 0 ? Math.ceil(v) : Math.floor(v));
	}

	// MathFunc1 ids
	private static final int F1_SQRT = 0, F1_RSQRT = 1, F1_CBRT = 2, F1_LENGTH = 3, F1_NORMALIZE = 4, F1_SIN = 5,
			F1_COS = 6, F1_SINCOS = 7, F1_TAN = 8, F1_ASIN = 9, F1_ACOS = 10, F1_ATAN = 11, F1_EXP = 13, F1_EXP2 = 14,
			F1_LOG = 15, F1_LOG2 = 16, F1_RCP = 17, F1_ABS = 18, F1_SIGN = 19, F1_CEIL = 20, F1_FLOOR = 21,
			F1_FRAC_UNSIGNED = 22, F1_FRAC = 23, F1_SATURATE = 24, F1_ALL = 49, F1_ANY = 50, F1_IS_FINITE = 51,
			F1_IS_INFINITE = 52;

	/** Maps the Fast* variants (31..48) onto their exact counterparts. */
	private static int normalizeFunc1(final int fn) {
		if ((fn >= 31) && (fn <= 48)) {
			return fn - 31;
		}
		return fn;
	}

	private void execMathFunc1(final Instruction ins, final ExecContext ctx) {
		final int fn = normalizeFunc1(ins.operands[0]);
		final int dstReg = ins.operands[1];
		final int srcReg = ins.operands[2];
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		readSrc(ctx, srcReg, a);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		if (fn == F1_LENGTH) {
			out.lanes[0] = vectorLength(a);
			writeDst(ctx, dstReg, out);
			return;
		}
		if (fn == F1_NORMALIZE) {
			final float len = vectorLength(a);
			final float invLen = len > 1e-12f ? 1.0f / len : 0;
			for (int i = 0; i < dstComps; i++) {
				out.lanes[i] = i < a.componentCount ? a.lanes[i] * invLen : 0;
			}
			writeDst(ctx, dstReg, out);
			return;
		}
		if (fn == F1_SINCOS) {
			for (int i = 0; i < dstComps; i++) {
				final int srcIdx = i / 2;
				final float x = srcIdx < a.componentCount ? a.lanes[srcIdx] : 0;
				EngineMath.sinCos(x, sinCos);
				out.lanes[i] = (i & 1) != 0 ? sinCos[1] : sinCos[0];
			}
			writeDst(ctx, dstReg, out);
			return;
		}
		if (fn == F1_ALL) {
			boolean allTrue = true;
			for (int i = 0; i < a.componentCount; i++) {
				if (a.lanes[i] == 0) {
					allTrue = false;
					break;
				}
			}
			out.lanes[0] = allTrue ? 1 : 0;
			writeDst(ctx, dstReg, out);
			return;
		}
		if (fn == F1_ANY) {
			boolean anyTrue = false;
			for (int i = 0; i < a.componentCount; i++) {
				if (a.lanes[i] != 0) {
					anyTrue = true;
					break;
				}
			}
			out.lanes[0] = anyTrue ? 1 : 0;
			writeDst(ctx, dstReg, out);
			return;
		}
		for (int i = 0; i < dstComps; i++) {
			final float x = a.lanes[i];
			float r;
			switch (fn) {
			case F1_SQRT:
				r = EngineMath.sqrt(x);
				break;
			case F1_RSQRT:
				r = EngineMath.rsqrt(x);
				break;
			case F1_CBRT:
				r = (float) Math.cbrt(x);
				break;
			case F1_SIN:
				r = (float) Math.sin(x);
				break;
			case F1_COS:
				r = (float) Math.cos(x);
				break;
			case F1_TAN:
				r = (float) Math.tan(x);
				break;
			case F1_ASIN:
				r = (float) Math.asin(x);
				break;
			case F1_ACOS:
				r = (float) Math.acos(x);
				break;
			case F1_ATAN:
				r = (float) Math.atan(x);
				break;
			case F1_EXP:
				r = EngineMath.exp(x);
				break;
			case F1_EXP2:
				r = EngineMath.exp2(x);
				break;
			case F1_LOG:
				r = (float) Math.log(x);
				break;
			case F1_LOG2:
				r = (float) (Math.log(x) / Math.log(2.0));
				break;
			case F1_RCP:
				r = EngineMath.rcp(x);
				break;
			case F1_ABS:
				r = Math.abs(x);
				break;
			case F1_SIGN:
				r = (x > 0 ? 1 : 0) - (x < 0 ? 1 : 0);
				break;
			case F1_CEIL:
				r = (float) Math.ceil(x);
				break;
			case F1_FLOOR:
				r = (float) Math.floor(x);
				break;
			case F1_FRAC_UNSIGNED:
				r = x - (float) Math.floor(x);
				break;
			case F1_FRAC:
				r = x - truncate(x);
				break;
			case F1_SATURATE:
				r = x < 0 ? 0 : (x > 1 ? 1 : x);
				break;
			case F1_IS_FINITE:
				r = (Float.isNaN(x) || Float.isInfinite(x)) ? 0 : 1;
				break;
			case F1_IS_INFINITE:
				r = Float.isInfinite(x) ? 1 : 0;
				break;
			default:
				if (warnedOps.add(1000 + fn)) {
					System.err.println("[pkb vm] MathFunc1 sub-id not implemented (engine no-op): " + fn);
				}
				r = x;
				break;
			}
			out.lanes[i] = r;
		}
		writeDst(ctx, dstReg, out);
	}

	private static float vectorLength(final RegisterValue v) {
		float acc = 0;
		for (int i = 0; i < v.componentCount; i++) {
			acc += v.lanes[i] * v.lanes[i];
		}
		return (float) Math.sqrt(acc);
	}

	private static final int F2_ATAN2 = 12, F2_FAST_ATAN2 = 43, F2_STEP = 25, F2_DISCRETIZE = 26, F2_MIN = 27,
			F2_MAX = 28, F2_DOT = 29, F2_CROSS = 30;

	private void execMathFunc2(final Instruction ins, final ExecContext ctx) {
		final int fn = ins.operands[0];
		final int dstReg = ins.operands[1];
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		readSrc(ctx, ins.operands[2], a);
		readSrc(ctx, ins.operands[3], b);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		for (int i = 0; i < dstComps; i++) {
			final float x = a.lanes[i];
			final float y = b.lanes[i];
			float r;
			switch (fn) {
			case F2_ATAN2:
			case F2_FAST_ATAN2:
				r = (float) Math.atan2(x, y);
				break;
			case F2_STEP:
				r = (y < x) ? 0 : 1;
				break;
			case F2_DISCRETIZE:
				r = (y != 0) ? truncate(x / y) * y : 0;
				break;
			case F2_MIN:
				r = (x < y) ? x : y;
				break;
			case F2_MAX:
				r = (x > y) ? x : y;
				break;
			case F2_DOT: {
				float acc = 0;
				for (int j = 0; (j < a.componentCount) && (j < b.componentCount); j++) {
					acc += a.lanes[j] * b.lanes[j];
				}
				out.componentCount = 1;
				out.lanes[0] = acc;
				writeDst(ctx, dstReg, out);
				return;
			}
			case F2_CROSS:
				out.componentCount = 3;
				out.lanes[0] = (a.lanes[1] * b.lanes[2]) - (a.lanes[2] * b.lanes[1]);
				out.lanes[1] = (a.lanes[2] * b.lanes[0]) - (a.lanes[0] * b.lanes[2]);
				out.lanes[2] = (a.lanes[0] * b.lanes[1]) - (a.lanes[1] * b.lanes[0]);
				writeDst(ctx, dstReg, out);
				return;
			default:
				if (warnedOps.add(2000 + fn)) {
					System.err.println("[pkb vm] MathFunc2 sub-id not implemented (engine no-op): " + fn);
				}
				r = x;
				break;
			}
			out.lanes[i] = r;
		}
		writeDst(ctx, dstReg, out);
	}

	private void execMathFunc3(final Instruction ins, final ExecContext ctx) {
		final int fn = ins.operands[0];
		final int dstReg = ins.operands[1];
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		readSrc(ctx, ins.operands[2], a);
		readSrc(ctx, ins.operands[3], b);
		readSrc(ctx, ins.operands[4], c);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		for (int i = 0; i < dstComps; i++) {
			final float x = a.lanes[i];
			final float y = b.lanes[i];
			final float z = c.lanes[i];
			switch (fn) {
			case 0:
				out.lanes[i] = x + ((y - x) * z);
				break;
			case 1:
				out.lanes[i] = (x < y) ? y : (x > z ? z : x);
				break;
			case 2:
				out.setLaneInt(i, ((x >= y) && (x <= z)) ? 1 : 0);
				break;
			default:
				throw new VmException("IR: MathFunc3 sub-id not implemented: " + fn);
			}
		}
		writeDst(ctx, dstReg, out);
	}

	private void execSelect(final Instruction ins, final ExecContext ctx) {
		final int dstReg = ins.operands[0];
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		readSrc(ctx, ins.operands[3], c); // cond
		readSrc(ctx, ins.operands[2], b); // true value
		readSrc(ctx, ins.operands[1], a); // false value
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		for (int i = 0; i < dstComps; i++) {
			final int cl = i < c.componentCount ? i : 0;
			out.lanes[i] = c.truthy(cl) ? b.lanes[i] : a.lanes[i];
		}
		writeDst(ctx, dstReg, out);
	}

	private void execBroadcast(final Instruction ins, final ExecContext ctx) {
		final int dstReg = ins.operands[0];
		final int dstBank = Bank.bankOf(dstReg);
		final int dstComps = Bank.componentCountForBank(dstBank);
		readSrc(ctx, ins.operands[1], a);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = dstComps;
		for (int i = 0; i < dstComps; i++) {
			out.lanes[i] = a.lanes[0];
		}
		writeDst(ctx, dstReg, out);
	}

	private void execMadd(final Instruction ins, final ExecContext ctx) {
		final int dstReg = ins.operands[0];
		final int dstBank = Bank.bankOf(dstReg);
		final int components = Bank.componentCountForBank(dstBank);
		readSrc(ctx, ins.operands[1], a);
		readSrc(ctx, ins.operands[2], b);
		readSrc(ctx, ins.operands[3], c);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = components;
		for (int i = 0; i < components; i++) {
			out.lanes[i] = (a.lanes[i] * b.lanes[i]) + c.lanes[i];
		}
		writeDst(ctx, dstReg, out);
	}

	private void execIDivMulInv(final Instruction ins, final ExecContext ctx) {
		final int dstReg = ins.operands[0];
		final int dstBank = Bank.bankOf(dstReg);
		final int components = Bank.componentCountForBank(dstBank);
		readSrc(ctx, ins.operands[1], a);
		readSrc(ctx, ins.operands[2], b);
		readSrc(ctx, ins.operands[3], c);
		readSrc(ctx, ins.operands[4], d);
		out.clear();
		out.typeBank = dstBank;
		out.componentCount = components;
		final int mInvSign = b.laneAsInt(0);
		final int magic = c.laneAsInt(0);
		final int shiftAmt = d.laneAsInt(0) & 31;
		final int mSignbit = mInvSign >> 31;
		final int addSignbit = magic >> 31;
		final int v15 = mSignbit & ~addSignbit;
		for (int i = 0; i < components; i++) {
			final int x = a.laneAsInt(i);
			final int v16 = x & addSignbit & ~mSignbit;
			final long prod = (long) magic * (long) x;
			final int hi32 = (int) (prod >>> 32);
			final int adjusted = (hi32 + v16) - (x & v15);
			final int shifted = adjusted >> shiftAmt;
			final int result = shifted + (shifted >>> 31);
			out.setLaneInt(i, result);
		}
		writeDst(ctx, dstReg, out);
	}
}
