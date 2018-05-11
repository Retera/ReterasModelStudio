package com.hiveworkshop.lang;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * Class to represent a 32 bit magic number.
 * <p>
 * Such numbers are often produced by combining four human readable 8 bit ASCII
 * characters. It is often desired to treat them as a {@code String} when
 * interacting with users to preserve the literal meaning while processing them
 * as {@code int} internally for speed.
 * <p>
 * An internal magic number represents a magic number in primitive form such as
 * used by file IO. A human readable magic string represents a magic number as a
 * 4 character string. Conversion methods can specify a specific ByteOrder to
 * use so that the internal magic number produced is compatible with a stream or
 * buffer using the same ByteOrder. This removes the need to change ByteOrder or
 * invert human readable magic strings.
 * 
 * @author Imperial Good
 */
public class MagicInt implements Comparable<MagicInt> {
	private final int value;

	/**
	 * Constructs a magic number from an internal magic number assuming
	 * BIG_ENDIAN byte order.
	 * 
	 * @param value
	 *            internal magic number.
	 */
	public MagicInt(int value) {
		this.value = value;
	}

	/**
	 * Constructs a magic number from an internal magic number with the
	 * specified byte order.
	 * 
	 * @param value
	 *            internal magic number.
	 * @param bo
	 *            byte order to use.
	 */
	public MagicInt(int value, ByteOrder bo) {
		this.value = ByteBuffer.allocate(4).order(bo).putInt(0, value)
				.order(ByteOrder.BIG_ENDIAN).getInt(0);
	}

	/**
	 * Constructs a magic number from a human readable magic string.
	 * <p>
	 * A readable magic string must be exactly 4 characters long. Only
	 * characters that can fit into 1 byte will parse correctly.
	 * 
	 * @param value
	 *            - the type string.
	 * @throws StringIndexOutOfBoundsException
	 *             - if the type string is not exactly 4 characters long.
	 */
	public MagicInt(String value) {
		this.value = stringToMagic(value);
	}

	/**
	 * Converts a human readable magic string into an internal magic number.
	 * <p>
	 * A readable magic number must be exactly 4 characters long. Only
	 * characters that can fit into 1 byte will parse correctly.
	 * 
	 * @param value
	 *            human readable magic number.
	 * @return internal magic number.
	 * @throws StringIndexOutOfBoundsException
	 *             if the type string is not exactly 4 characters long.
	 */
	public static int stringToMagic(String value) {
		if (value.length() != 4)
			throw new StringIndexOutOfBoundsException(
					String.format(
							"'%s' is not a valid type string (must be exactly 4 characters long)",
							value));
		byte[] bytes = new byte[4];
		for (int i = 0; i < 4; i += 1)
			bytes[i] = (byte) value.charAt(i);
		return ByteBuffer.wrap(bytes).getInt(0);
	}

	/**
	 * Converts an internal magic number into a human readable magic string.
	 * <p>
	 * Not all magic numbers might be human readable.
	 * 
	 * @param value
	 *            internal magic number.
	 * @return a human readable magic string.
	 */
	public static String magicToString(int value) {
		byte[] bytes = ByteBuffer.allocate(4).putInt(0, value).array();
		char[] chars = new char[4];
		for (int i = 0; i < 4; i += 1)
			chars[i] = (char) bytes[i];
		return new String(chars);
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MagicInt other = (MagicInt) obj;
		if (value != other.value)
			return false;
		return true;
	}

	/**
	 * Gets the magic number as a human readable magic string.
	 * 
	 * @return a human readable magic string.
	 */
	@Override
	public String toString() {
		return magicToString(value);
	}

	/**
	 * Gets an internal magic number assuming BIG_ENDIAN byte order.
	 * 
	 * @return the internal magic number.
	 */
	public int toInt() {
		return value;
	}

	/**
	 * Gets an internal magic number using the specified byte order..
	 * 
	 * @param bo
	 *            byte order to use.
	 * @return the internal magic number.
	 */
	public int toInt(ByteOrder bo) {
		return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
				.putInt(0, value).order(bo).getInt(0);
	}

	@Override
	public int compareTo(MagicInt o) {
		return value - o.value;
	}
}
