package com.hiveworkshop.wc3.pkb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

/**
 * One decoded field of a baked object: the declared type, the raw little
 * endian bytes, and the resolved strings when the type is a string type.
 * Scalars are 4 bytes ({@code bool} is 1), vector types are packed, arrays
 * carry a u32 count first.
 */
public final class PkbValue {
	public static final long NULL_LINK = 0xFFFFFFFFL;

	public final String name;
	public final String type;
	private final byte[] bytes;
	private final List<String> strings;

	PkbValue(final String name, final String type, final byte[] bytes, final List<String> strings) {
		this.name = name;
		this.type = type;
		this.bytes = bytes;
		this.strings = strings == null ? Collections.emptyList() : strings;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public boolean isArray() {
		return type.endsWith("[]");
	}

	public String baseType() {
		return isArray() ? type.substring(0, type.length() - 2) : type;
	}

	private ByteBuffer buffer() {
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
	}

	public boolean asBool() {
		return (bytes.length > 0) && (bytes[0] != 0);
	}

	public int asInt() {
		return bytes.length >= 4 ? buffer().getInt(0) : 0;
	}

	public long asUint() {
		return asInt() & 0xFFFFFFFFL;
	}

	public float asFloat() {
		return bytes.length >= 4 ? buffer().getFloat(0) : 0f;
	}

	/** Vector types (float2/3/4, int2/3/4) as floats; ints are converted. */
	public float[] asFloats() {
		final int dim = dimensionOf(baseType());
		final boolean isInt = baseType().startsWith("int") || baseType().startsWith("uint");
		final ByteBuffer buffer = buffer();
		final float[] out = new float[dim];
		for (int i = 0; (i < dim) && (((i + 1) * 4) <= bytes.length); i++) {
			out[i] = isInt ? buffer.getInt(i * 4) : buffer.getFloat(i * 4);
		}
		return out;
	}

	/** Object index the link points at (0-based), or -1 for a null link. */
	public int asLink() {
		final long raw = asUint();
		return ((raw == NULL_LINK) || (raw == 0)) ? -1 : (int) (raw - 1);
	}

	public int arrayLength() {
		return isArray() && (bytes.length >= 4) ? buffer().getInt(0) : 0;
	}

	/** link[] as 0-based object indices, -1 for null entries. */
	public int[] asLinks() {
		final int count = arrayLength();
		final ByteBuffer buffer = buffer();
		final int[] out = new int[count];
		for (int i = 0; i < count; i++) {
			final long raw = buffer.getInt(4 + (i * 4)) & 0xFFFFFFFFL;
			out[i] = ((raw == NULL_LINK) || (raw == 0)) ? -1 : (int) (raw - 1);
		}
		return out;
	}

	public float[] asFloatArray() {
		final int count = arrayLength();
		final int perElement = Math.max(1, dimensionOf(baseType()));
		final ByteBuffer buffer = buffer();
		final float[] out = new float[count * perElement];
		final boolean isInt = baseType().startsWith("int") || baseType().startsWith("uint");
		for (int i = 0; (i < out.length) && ((4 + ((i + 1) * 4)) <= bytes.length); i++) {
			out[i] = isInt ? buffer.getInt(4 + (i * 4)) : buffer.getFloat(4 + (i * 4));
		}
		return out;
	}

	public int[] asIntArray() {
		final int count = arrayLength();
		final int perElement = Math.max(1, dimensionOf(baseType()));
		final ByteBuffer buffer = buffer();
		final int[] out = new int[count * perElement];
		for (int i = 0; (i < out.length) && ((4 + ((i + 1) * 4)) <= bytes.length); i++) {
			out[i] = buffer.getInt(4 + (i * 4));
		}
		return out;
	}

	/** The raw words of an {@code unknown[]} blob (compiled bytecode). */
	public byte[] asBlobBytes() {
		final int count = arrayLength();
		final int size = Math.min(count * 4, Math.max(0, bytes.length - 4));
		final byte[] out = new byte[size];
		System.arraycopy(bytes, 4, out, 0, size);
		return out;
	}

	public String asString() {
		return strings.isEmpty() ? "" : strings.get(0);
	}

	public List<String> asStrings() {
		return strings;
	}

	static int dimensionOf(final String scalarType) {
		if (scalarType.isEmpty()) {
			return 1;
		}
		final char tail = scalarType.charAt(scalarType.length() - 1);
		if ((tail >= '2') && (tail <= '4')) {
			return tail - '0';
		}
		return 1;
	}

	@Override
	public String toString() {
		switch (baseType()) {
		case "bool":
			return isArray() ? "bool[" + arrayLength() + "]" : Boolean.toString(asBool());
		case "int":
			return isArray() ? java.util.Arrays.toString(asIntArray()) : Integer.toString(asInt());
		case "uint":
			return isArray() ? java.util.Arrays.toString(asIntArray()) : Long.toString(asUint());
		case "float":
			return isArray() ? java.util.Arrays.toString(asFloatArray()) : Float.toString(asFloat());
		case "link":
			return isArray() ? java.util.Arrays.toString(asLinks()) : ("->" + asLink());
		case "string":
		case "string_unicode":
			return isArray() ? strings.toString() : ('"' + asString() + '"');
		case "unknown":
			return "blob[" + asBlobBytes().length + " bytes]";
		default:
			if (baseType().startsWith("float") || baseType().startsWith("int") || baseType().startsWith("uint")) {
				return isArray() ? java.util.Arrays.toString(asFloatArray()) : java.util.Arrays.toString(asFloats());
			}
			return type + "[" + bytes.length + " bytes]";
		}
	}
}
