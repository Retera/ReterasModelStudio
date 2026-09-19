package com.hiveworkshop.wc3.pkb.sim;

import com.hiveworkshop.wc3.pkb.vm.Bank;

/** The engine's 32-bit LCG. */
public final class FastRand {
	public static final int MULTIPLIER = 0x000D0F95;
	public static final int INCREMENT = 0x00D19EC3;

	private int state;

	public FastRand() {
	}

	public FastRand(final int seed) {
		state = seed;
	}

	public static int advanceStatic(final int state) {
		return (state * MULTIPLIER) + INCREMENT;
	}

	public int state() {
		return state;
	}

	public void setState(final int state) {
		this.state = state;
	}

	public int advance() {
		state = advanceStatic(state);
		return state;
	}

	/** A float in [1, 2) built from the top mantissa bits of the next draw. */
	public float unit12() {
		final int raw = advance();
		return Float.intBitsToFloat((raw >>> Bank.RAND_MANTISSA_SHIFT) | Bank.ONE_F32_BITS);
	}

	/** A float in [0, 1). */
	public float unit() {
		return unit12() - 1.0f;
	}
}
