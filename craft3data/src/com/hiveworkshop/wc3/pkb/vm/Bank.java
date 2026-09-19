package com.hiveworkshop.wc3.pkb.vm;

/** Register bank (type) codes carried in the top byte of a register id. */
public final class Bank {
	public static final int HANDLE = 0x00;
	public static final int BOOL = 0x02;
	public static final int BOOL3 = 0x04;
	public static final int INT = 0x08;
	public static final int INT2 = 0x09;
	public static final int PTR = 0x1A;
	public static final int INT2_ALT = 0x1B;
	public static final int INT3 = 0x1C;
	public static final int INT4 = 0x1D;
	public static final int FLOAT = 0x20;
	public static final int FLOAT2 = 0x21;
	public static final int FLOAT3 = 0x22;
	public static final int FLOAT4 = 0x23;
	/** Four float lanes (quaternions); despite the name it is in the float family. */
	public static final int INT_ALT = 0x25;
	public static final int INT2_ALT2 = 0x26;

	public static final int SCOPE_CONST = 0;
	public static final int SCOPE_LOCAL = 1;
	public static final int SCOPE_INPUT = 2;
	public static final int SCOPE_STREAM = 3;
	public static final int SCOPE_BUCKETS = 4;

	public static final int REG_VOID = 0xFFFFFFFF;
	public static final int ONE_F32_BITS = 0x3F800000;
	public static final int BOOL_TRUE_BITS = 0xFFFFFFFF;
	public static final int RAND_MANTISSA_SHIFT = 9;

	public static final int FAMILY_FLOAT = 0;
	public static final int FAMILY_INT = 1;
	public static final int FAMILY_BOOL = 2;
	public static final int FAMILY_OTHER = 3;

	private Bank() {
	}

	public static int bankOf(final int regId) {
		return (regId >>> 24) & 0xFF;
	}

	public static int scopeOf(final int regId) {
		final int scopeByte = (regId >>> 16) & 0xFF;
		return ((scopeByte >= 0x20) ? (scopeByte >>> 5) : scopeByte) & 0x03;
	}

	public static int localIndexOf(final int regId) {
		return regId & 0xFFFF;
	}

	public static int componentCountForBank(final int bank) {
		switch (bank) {
		case FLOAT2:
		case INT2:
		case INT2_ALT:
		case INT2_ALT2:
			return 2;
		case FLOAT3:
		case INT3:
		case BOOL3:
			return 3;
		case FLOAT4:
		case INT4:
		case INT_ALT:
			return 4;
		default:
			return 1;
		}
	}

	public static int floatBankForComponentCount(final int components) {
		switch (components) {
		case 2:
			return FLOAT2;
		case 3:
			return FLOAT3;
		case 4:
			return FLOAT4;
		default:
			return FLOAT;
		}
	}

	public static int intBankForComponentCount(final int components) {
		switch (components) {
		case 2:
			return INT2;
		case 3:
			return INT3;
		case 4:
			return INT4;
		default:
			return INT;
		}
	}

	public static boolean isIntegral(final int bank) {
		return (bank == BOOL) || (bank == INT) || (bank == INT2) || (bank == INT2_ALT) || (bank == INT2_ALT2)
				|| (bank == INT3) || (bank == INT4) || (bank == PTR);
	}

	public static int scalarFamily(final int bank) {
		if ((bank == BOOL) || (bank == BOOL3)) {
			return FAMILY_BOOL;
		}
		if (isIntegral(bank)) {
			return FAMILY_INT;
		}
		if ((bank == FLOAT) || (bank == FLOAT2) || (bank == FLOAT3) || (bank == FLOAT4) || (bank == INT_ALT)) {
			return FAMILY_FLOAT;
		}
		return FAMILY_OTHER;
	}
}
