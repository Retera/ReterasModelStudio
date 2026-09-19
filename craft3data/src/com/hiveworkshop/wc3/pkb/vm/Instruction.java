package com.hiveworkshop.wc3.pkb.vm;

/** One decoded instruction: up to five inline operands plus a variable tail. */
public final class Instruction {
	public static final int[] NO_EXTRA = new int[0];

	public Opcode opcode;
	public int streamOffset;
	public final int[] operands = new int[5];
	public int operandCount;
	public int[] extraOperands = NO_EXTRA;

	/** For FUNCTION_CALL: number of declared arguments. */
	public int argc() {
		return operands[3];
	}

	/** For FUNCTION_CALL: register of argument {@code i}. */
	public int argReg(final int i) {
		return extraOperands[(i * 2) + 1];
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("%05X %-16s", streamOffset, opcode));
		for (int i = 0; i < operandCount; i++) {
			sb.append(' ').append(Integer.toHexString(operands[i]));
		}
		if (extraOperands.length > 0) {
			sb.append(" [");
			for (int i = 0; i < extraOperands.length; i++) {
				if (i > 0) {
					sb.append(' ');
				}
				sb.append(Integer.toHexString(extraOperands[i]));
			}
			sb.append(']');
		}
		return sb.toString();
	}
}
