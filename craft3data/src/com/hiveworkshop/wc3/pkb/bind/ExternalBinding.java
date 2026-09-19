package com.hiveworkshop.wc3.pkb.bind;

/** One external (stream field, attribute, sampler handle, ...) of a scope program. */
public final class ExternalBinding {
	/** Slot as referenced by the bytecode of this scope. */
	public int slot;
	public String name = "";
	public String typeName = "";
	public int nativeType;
	public int storageSize;
	public int accessMask;
	/** Layer-wide slot shared by every scope that names the same external (1-based). */
	public int canonicalSlot;

	public int resolvedSlot() {
		return ((canonicalSlot == 0) && (slot != 0)) ? slot : canonicalSlot;
	}

	public static ExternalBinding findByName(final ExternalBinding[] bindings, final String name) {
		if ((bindings == null) || (name == null)) {
			return null;
		}
		for (final ExternalBinding b : bindings) {
			if (b.name.equals(name)) {
				return b;
			}
		}
		return null;
	}

	public static ExternalBinding findBySlot(final ExternalBinding[] bindings, final int slot) {
		if (bindings == null) {
			return null;
		}
		for (final ExternalBinding b : bindings) {
			if (b.slot == slot) {
				return b;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "ext[" + slot + "->" + canonicalSlot + "] " + name;
	}
}
