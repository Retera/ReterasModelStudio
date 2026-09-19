package com.hiveworkshop.wc3.pkb.sim;

import com.hiveworkshop.wc3.pkb.vm.RegisterValue;

/**
 * Structure-of-arrays storage of every particle's externals for one pool:
 * four planes of raw words per slot plus the component count and bank of
 * what was last stored.
 */
public final class ExternalStore {
	private int[] planes = new int[0];
	private byte[] componentCount = new byte[0];
	private byte[] typeBank = new byte[0];
	private int slots;
	private int particleStride;

	public void resize(final int newSlots, final int particles) {
		if ((newSlots == slots) && (particles <= particleStride)) {
			return;
		}
		final int newStride = Math.max(particles, particleStride);
		final int[] newPlanes = new int[newSlots * 4 * newStride];
		final byte[] newCount = new byte[newSlots * newStride];
		final byte[] newBank = new byte[newSlots * newStride];
		final int keepSlots = Math.min(newSlots, slots);
		final int keepParticles = Math.min(newStride, particleStride);
		for (int slot = 0; slot < keepSlots; slot++) {
			for (int c = 0; c < 4; c++) {
				System.arraycopy(planes, ((slot * 4) + c) * particleStride, newPlanes, ((slot * 4) + c) * newStride,
						keepParticles);
			}
			System.arraycopy(componentCount, slot * particleStride, newCount, slot * newStride, keepParticles);
			System.arraycopy(typeBank, slot * particleStride, newBank, slot * newStride, keepParticles);
		}
		planes = newPlanes;
		componentCount = newCount;
		typeBank = newBank;
		slots = newSlots;
		particleStride = newStride;
	}

	public int slotCount() {
		return slots;
	}

	public int particleStride() {
		return particleStride;
	}

	public void load(final int slot, final int particle, final RegisterValue out) {
		final int base = slot * 4 * particleStride;
		out.lanes[0] = Float.intBitsToFloat(planes[base + particle]);
		out.lanes[1] = Float.intBitsToFloat(planes[base + particleStride + particle]);
		out.lanes[2] = Float.intBitsToFloat(planes[base + (2 * particleStride) + particle]);
		out.lanes[3] = Float.intBitsToFloat(planes[base + (3 * particleStride) + particle]);
		out.componentCount = componentCount[(slot * particleStride) + particle] & 0xFF;
		out.typeBank = typeBank[(slot * particleStride) + particle] & 0xFF;
	}

	public float loadLane(final int slot, final int particle, final int lane) {
		return Float.intBitsToFloat(planes[(((slot * 4) + lane) * particleStride) + particle]);
	}

	public int loadLaneBits(final int slot, final int particle, final int lane) {
		return planes[(((slot * 4) + lane) * particleStride) + particle];
	}

	public void store(final int slot, final int particle, final RegisterValue v) {
		final int base = slot * 4 * particleStride;
		planes[base + particle] = Float.floatToRawIntBits(v.lanes[0]);
		planes[base + particleStride + particle] = Float.floatToRawIntBits(v.lanes[1]);
		planes[base + (2 * particleStride) + particle] = Float.floatToRawIntBits(v.lanes[2]);
		planes[base + (3 * particleStride) + particle] = Float.floatToRawIntBits(v.lanes[3]);
		componentCount[(slot * particleStride) + particle] = (byte) v.componentCount;
		typeBank[(slot * particleStride) + particle] = (byte) v.typeBank;
	}

	public void clearParticle(final int particle) {
		for (int slot = 0; slot < slots; slot++) {
			final int base = slot * 4 * particleStride;
			planes[base + particle] = 0;
			planes[base + particleStride + particle] = 0;
			planes[base + (2 * particleStride) + particle] = 0;
			planes[base + (3 * particleStride) + particle] = 0;
			componentCount[(slot * particleStride) + particle] = 0;
			typeBank[(slot * particleStride) + particle] = 0;
		}
	}

	/** One particle's window onto the store. */
	public static final class View {
		public ExternalStore store;
		public int particle;
		public int count;

		public int size() {
			return count;
		}

		public void get(final int slot, final RegisterValue out) {
			store.load(slot, particle, out);
		}

		public RegisterValue get(final int slot) {
			final RegisterValue v = new RegisterValue();
			store.load(slot, particle, v);
			return v;
		}

		public float lane(final int slot, final int lane) {
			return store.loadLane(slot, particle, lane);
		}

		public int laneBits(final int slot, final int lane) {
			return store.loadLaneBits(slot, particle, lane);
		}

		public void set(final int slot, final RegisterValue v) {
			store.store(slot, particle, v);
		}

		public void clear() {
			if (store != null) {
				store.clearParticle(particle);
			}
		}
	}
}
