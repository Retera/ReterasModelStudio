package com.hiveworkshop.wc3.pkb.vm;

/** Bytecode opcodes; the CBEM aliases of IR opcodes are normalised at decode time. */
public enum Opcode {
	NOP(0x42), LOAD_EXTERNAL(0x43), STORE_TO_EXTERNAL(0x44), REINTERPRET(0x4A), TYPE_CONVERTER(0x4B), VEC_CTOR(0x4C),
	VEC_SWIZZLE(0x4D), MATH_OP(0x4E), MATH_FUNC1(0x4F), MATH_FUNC2(0x50), MATH_FUNC3(0x51), SELECT(0x52),
	FUNCTION_CALL(0x53), EXTERNAL_CLEAR(0x6B), BROADCAST(0x6F), MATH_OP_CMETA(0x70), MATH_OP_ADD(0x71),
	MATH_OP_SUB(0x72), MATH_OP_MUL(0x73), MATH_OP_DIV(0x74), MADD(0x75), IDIV_MUL_INV(0x76), FUNCTION_PROLOG(0x7C),
	FUNCTION_EPILOG(0x7D);

	public final int code;

	Opcode(final int code) {
		this.code = code;
	}

	private static final Opcode[] BY_CODE = new Opcode[256];
	static {
		for (final Opcode op : values()) {
			BY_CODE[op.code] = op;
		}
		BY_CODE[0x69] = LOAD_EXTERNAL;
		BY_CODE[0x6A] = STORE_TO_EXTERNAL;
		BY_CODE[0x6C] = TYPE_CONVERTER;
		BY_CODE[0x6D] = VEC_CTOR;
		BY_CODE[0x6E] = VEC_SWIZZLE;
		BY_CODE[0x77] = MATH_FUNC1;
		BY_CODE[0x78] = MATH_FUNC2;
		BY_CODE[0x79] = MATH_FUNC3;
		BY_CODE[0x7A] = SELECT;
		BY_CODE[0x7B] = FUNCTION_CALL;
	}

	/** The opcode for a raw stream byte, or null for a byte outside both ranges. */
	public static Opcode normalize(final int raw) {
		return BY_CODE[raw & 0xFF];
	}
}
