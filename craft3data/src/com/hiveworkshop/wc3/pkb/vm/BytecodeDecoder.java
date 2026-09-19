package com.hiveworkshop.wc3.pkb.vm;

import java.util.ArrayList;
import java.util.List;

/** Decodes a CBEM/IR byte stream into instructions. */
public final class BytecodeDecoder {
	private final byte[] bytes;
	private int off;

	private BytecodeDecoder(final byte[] bytes) {
		this.bytes = bytes;
	}

	public static Instruction[] decode(final byte[] bytes) {
		if ((bytes == null) || (bytes.length == 0)) {
			return new Instruction[0];
		}
		final BytecodeDecoder d = new BytecodeDecoder(bytes);
		final List<Instruction> out = new ArrayList<>();
		while (d.off < bytes.length) {
			final int raw = bytes[d.off] & 0xFF;
			final Opcode opcode = Opcode.normalize(raw);
			if (opcode == null) {
				throw new VmException(String.format("bytecode decoder: unknown opcode 0x%02X at offset %d", raw, d.off));
			}
			final Instruction ins = new Instruction();
			ins.opcode = opcode;
			ins.streamOffset = d.off;
			d.off++;
			d.decodeOne(ins);
			out.add(ins);
		}
		return out.toArray(new Instruction[0]);
	}

	private void ensure(final int need, final String what) {
		if ((off + need) > bytes.length) {
			throw new VmException(what);
		}
	}

	private int u8() {
		return bytes[off++] & 0xFF;
	}

	private int u16() {
		final int v = (bytes[off] & 0xFF) | ((bytes[off + 1] & 0xFF) << 8);
		off += 2;
		return v;
	}

	private int u24() {
		final int v = (bytes[off] & 0xFF) | ((bytes[off + 1] & 0xFF) << 8) | ((bytes[off + 2] & 0xFF) << 16);
		off += 3;
		return v;
	}

	private int u32() {
		final int v = (bytes[off] & 0xFF) | ((bytes[off + 1] & 0xFF) << 8) | ((bytes[off + 2] & 0xFF) << 16)
				| ((bytes[off + 3] & 0xFF) << 24);
		off += 4;
		return v;
	}

	private void u32s(final Instruction ins, final int count, final String what) {
		ensure(count * 4, what);
		for (int i = 0; i < count; i++) {
			ins.operands[i] = u32();
		}
		ins.operandCount = count;
	}

	private void taggedU32s(final Instruction ins, final int count, final String what) {
		ensure(1 + (count * 4), what);
		ins.operands[0] = u8();
		for (int i = 0; i < count; i++) {
			ins.operands[i + 1] = u32();
		}
		ins.operandCount = count + 1;
	}

	private void decodeOne(final Instruction ins) {
		switch (ins.opcode) {
		case NOP:
		case FUNCTION_EPILOG:
			ins.operandCount = 0;
			return;
		case LOAD_EXTERNAL:
		case STORE_TO_EXTERNAL:
			ensure(6, "IR: LoadExternal/StoreToExternal truncated");
			ins.operands[0] = u32();
			ins.operands[1] = u16();
			ins.operandCount = 2;
			return;
		case REINTERPRET:
		case TYPE_CONVERTER:
			u32s(ins, 2, "IR: Reinterpret/TypeConverter truncated");
			return;
		case VEC_CTOR: {
			ensure(5, "IR: VecCtor header truncated");
			final int argc = u8();
			final int dst = u32();
			final int srcCount = argc + 1;
			ensure(srcCount * 4, "IR: VecCtor sources truncated");
			ins.operands[0] = argc;
			ins.operands[1] = dst;
			ins.operandCount = 2;
			ins.extraOperands = new int[srcCount];
			for (int i = 0; i < srcCount; i++) {
				ins.extraOperands[i] = u32();
			}
			return;
		}
		case VEC_SWIZZLE:
			ensure(11, "IR: VecSwizzle truncated");
			ins.operands[0] = u24();
			ins.operands[1] = u32();
			ins.operands[2] = u32();
			ins.operandCount = 3;
			return;
		case MATH_OP:
		case MATH_OP_CMETA:
			taggedU32s(ins, 3, "IR: MathOp truncated");
			return;
		case MATH_FUNC1:
			taggedU32s(ins, 2, "IR: MathFunc1 truncated");
			return;
		case MATH_FUNC2:
			taggedU32s(ins, 3, "IR: MathFunc2 truncated");
			return;
		case MATH_FUNC3:
			taggedU32s(ins, 4, "IR: MathFunc3 truncated");
			return;
		case SELECT:
			u32s(ins, 4, "IR: Select truncated");
			return;
		case FUNCTION_CALL: {
			ensure(10, "IR: FunctionCall header truncated");
			final int flags = u8();
			final int objSlotRaw = u16();
			final int extFunc = u16();
			final int argc = u8();
			final int retReg = u32();
			ensure(argc * 5, "IR: FunctionCall args truncated");
			ins.operands[0] = flags;
			ins.operands[1] = (short) objSlotRaw; // sign-extended i16, like the reference
			ins.operands[2] = extFunc;
			ins.operands[3] = argc;
			ins.operands[4] = retReg;
			ins.operandCount = 5;
			ins.extraOperands = new int[argc * 2];
			for (int i = 0; i < argc; i++) {
				ins.extraOperands[i * 2] = u8();
				ins.extraOperands[(i * 2) + 1] = u32();
			}
			return;
		}
		case EXTERNAL_CLEAR:
			ensure(2, "CBEM: ExternalClear truncated");
			ins.operands[0] = u16();
			ins.operandCount = 1;
			return;
		case BROADCAST:
			u32s(ins, 2, "CBEM: Broadcast truncated");
			return;
		case MATH_OP_ADD:
		case MATH_OP_SUB:
		case MATH_OP_MUL:
		case MATH_OP_DIV:
			u32s(ins, 3, "CBEM: MathOp* truncated");
			return;
		case MADD:
			u32s(ins, 4, "CBEM: Madd truncated");
			return;
		case IDIV_MUL_INV:
			u32s(ins, 5, "CBEM: IDivMulInv truncated");
			return;
		case FUNCTION_PROLOG:
			ensure(1, "CBEM: FunctionProlog truncated");
			off++;
			ins.operandCount = 0;
			return;
		default:
			throw new VmException("bytecode decoder: unhandled opcode " + ins.opcode);
		}
	}
}
