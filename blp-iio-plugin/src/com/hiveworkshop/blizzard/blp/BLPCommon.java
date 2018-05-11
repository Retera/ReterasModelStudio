package com.hiveworkshop.blizzard.blp;

import com.hiveworkshop.lang.MagicInt;

/**
 * Class containing static constants and methods shared by various other classes
 * of the BLP library.
 * 
 * @author ImperialGood
 */
public abstract class BLPCommon {
	// indexed content

	/**
	 * The number of bits for selecting indexed color.
	 */
	static final int INDEXED_PALETTE_BITS = 8;

	/**
	 * The number of indexed colors for indexed content.
	 */
	public static final int INDEXED_PALETTE_SIZE = 1 << INDEXED_PALETTE_BITS;

	// mipmap constants

	/**
	 * Maximum number of mipmaps a BLP file can contain. Since version 1.
	 */
	public static final int MIPMAP_MAX = 16;

	/**
	 * Array containing all the BLP version magic numbers in chronological
	 * order.
	 */
	private static final MagicInt[] BLP_VERSION_MAGIC = { new MagicInt("BLP0"),
			new MagicInt("BLP1"), new MagicInt("BLP2") };
	
	/**
	 * Converts a BLP magic number into a version number. If the magic number is
	 * not a known BLP magic number then an invalid version of -1 is returned.
	 * 
	 * @param magicint
	 *            file magic number.
	 * @return the BLP version number or -1 if not known.
	 */
	public static final int resolveVersion(MagicInt magicint) {
		// simple linear search
		for (int i = 0; i < BLP_VERSION_MAGIC.length; i += 1) {
			if (magicint.equals(BLP_VERSION_MAGIC[i]))
				return i;
		}

		// failure
		return -1;
	}

	/**
	 * Converts a BLP version number into a magic number.
	 * 
	 * @param ver
	 *            the BLP version number.
	 * @return the BLP file magic number in big-endian order.
	 * @throws IndexOutOfBoundsException
	 *             if ver is not a supported BLP version.
	 */
	public static final MagicInt resolveMagic(int ver) {
		return BLP_VERSION_MAGIC[ver];
	}
}
