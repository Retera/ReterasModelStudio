package com.hiveworkshop.wc3.pkb.vm;

/**
 * One VM register: four float lanes (integers and bools live in the lanes as
 * raw bits), the number of meaningful lanes, and the bank code they came from.
 */
public final class RegisterValue {
	public final float[] lanes = new float[4];
	public int componentCount;
	public int typeBank;

	public RegisterValue() {
	}

	public RegisterValue(final RegisterValue other) {
		set(other);
	}

	public void clear() {
		lanes[0] = 0;
		lanes[1] = 0;
		lanes[2] = 0;
		lanes[3] = 0;
		componentCount = 0;
		typeBank = 0;
	}

	public void set(final RegisterValue other) {
		lanes[0] = other.lanes[0];
		lanes[1] = other.lanes[1];
		lanes[2] = other.lanes[2];
		lanes[3] = other.lanes[3];
		componentCount = other.componentCount;
		typeBank = other.typeBank;
	}

	public RegisterValue copy() {
		return new RegisterValue(this);
	}

	public static RegisterValue scalar(final float v) {
		final RegisterValue r = new RegisterValue();
		r.lanes[0] = v;
		r.componentCount = 1;
		r.typeBank = Bank.FLOAT;
		return r;
	}

	public static RegisterValue scalarI(final int v) {
		final RegisterValue r = new RegisterValue();
		r.lanes[0] = Float.intBitsToFloat(v);
		r.componentCount = 1;
		r.typeBank = Bank.INT;
		return r;
	}

	public void setScalar(final float v) {
		clear();
		lanes[0] = v;
		componentCount = 1;
		typeBank = Bank.FLOAT;
	}

	public void setScalarI(final int v) {
		clear();
		lanes[0] = Float.intBitsToFloat(v);
		componentCount = 1;
		typeBank = Bank.INT;
	}

	public void setFloat3(final float x, final float y, final float z) {
		clear();
		lanes[0] = x;
		lanes[1] = y;
		lanes[2] = z;
		componentCount = 3;
		typeBank = Bank.FLOAT3;
	}

	public void setFloat4(final float x, final float y, final float z, final float w) {
		clear();
		lanes[0] = x;
		lanes[1] = y;
		lanes[2] = z;
		lanes[3] = w;
		componentCount = 4;
		typeBank = Bank.FLOAT4;
	}

	public void setFloats(final int components, final int bank) {
		clear();
		componentCount = components;
		typeBank = bank;
	}

	public int laneAsInt(final int lane) {
		return Float.floatToRawIntBits(lanes[lane]);
	}

	public void setLaneInt(final int lane, final int v) {
		lanes[lane] = Float.intBitsToFloat(v);
	}

	/** True when the lane is non-zero either as an integer bit pattern or as a float. */
	public boolean truthy(final int lane) {
		return (laneAsInt(lane) != 0) || (lanes[lane] != 0f);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[bank=").append(Integer.toHexString(typeBank)).append(" n=").append(componentCount).append(':');
		for (int i = 0; i < 4; i++) {
			sb.append(' ');
			if (Bank.isIntegral(typeBank)) {
				sb.append(laneAsInt(i));
			} else {
				sb.append(lanes[i]);
			}
		}
		return sb.append(']').toString();
	}
}
