package com.hiveworkshop.wc3.pkb.bind;

import java.util.HashSet;
import java.util.Set;

/** One native function a scope program may call. */
public final class FunctionBinding {
	public static final int SYMBOL_SLOT_UNBOUND = 0xFFFFFFFF;

	public int slot;
	public String symbolName = "";
	public int symbolSlot = SYMBOL_SLOT_UNBOUND;
	public int traits;
	public String canonicalName = "";

	private static final Set<String> MANGLE_TOKENS = new HashSet<>();
	static {
		for (final String t : new String[] { "int", "int2", "int3", "int4", "uint", "uint2", "uint3", "uint4", "float",
				"float2", "float3", "float4", "bool", "bool2", "bool3", "bool4", "half", "half2", "half3", "half4",
				"quaternion", "orientation", "pCtxS", "pCtxI", "SceneCtx", "RandCtx", "SI" }) {
			MANGLE_TOKENS.add(t);
		}
	}

	/** Strips the namespace prefix and the trailing type mangling: {@code a::b:rand_float_float} -> {@code rand}. */
	public static String canonicalizeSymbol(String sym) {
		final int colon = sym.lastIndexOf(':');
		if (colon >= 0) {
			sym = sym.substring(colon + 1);
		}
		while (true) {
			final int us = sym.lastIndexOf('_');
			if ((us < 0) || !MANGLE_TOKENS.contains(sym.substring(us + 1))) {
				break;
			}
			sym = sym.substring(0, us);
		}
		return sym;
	}

	@Override
	public String toString() {
		return "fn[" + slot + "] " + symbolName + " (" + canonicalName + ") objSlot=" + symbolSlot;
	}
}
