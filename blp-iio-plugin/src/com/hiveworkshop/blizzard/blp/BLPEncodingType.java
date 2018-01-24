package com.hiveworkshop.blizzard.blp;

import java.util.Arrays;

/**
 * Enum class of all supported BLP image encoding formats. Although the color
 * component precision is fixed for each encoding type, some have multiple
 * supported alpha component precisions. Methods exist to query which alpha
 * component precisions are supported.
 * <p>
 * Only formats which are supported are available. Many World of Warcraft image
 * formats are currently not supported due to a lack of a detailed specification
 * and test cases.
 * 
 * @author Imperial Good
 */
public enum BLPEncodingType {
	/**
	 * Images stored as JFIF (commonly called JPEG) files. Non-standard 8 bit
	 * per component BGRA is used. Most JPEG image readers are not directly
	 * compatible with the image files directly as they may incorrectly perform
	 * a Y'CrCb to RGB color space conversions.
	 * <p>
	 * Supports 0 and 8 bit alpha. Alpha component values are always present but
	 * are opaque for 0 bit alpha.
	 * <p>
	 * Mipmap images share the same common JPEG file header to reduce file size.
	 * JPEG component data is already compressed so the resulting BLP file will
	 * not gain much from further compression. JPEG compression is lossy and can
	 * be controlled with standard quality settings.
	 */
	JPEG(0x101, 0),
	/**
	 * Images stored using indexed color. A 256 index BGR lookup table (palette)
	 * is used to interpret the 8 bit index band. Alpha is supported by a
	 * separate multi-pixel packed band.
	 * <p>
	 * Supports 0, 1, 4 and 8 bit alpha. Alpha component values with sub-byte
	 * precision are stored in a multi-pixel packed way for improved storage
	 * efficiency.
	 * <p>
	 * Mipmap images share the same 256 color palette to reduce file size.
	 * Storage is technically lossless and depending on content the resulting
	 * BLP files can greatly benefit from further compression. Encoding an image
	 * into indexed color for storage can cause a loss in color space.
	 */
	INDEXED(0x113, 0),
	/**
	 * This is an unsupported encoding type. Any attempt to read or write this
	 * encoding type will throw an exception. Chances are this is one of the
	 * poorly documented World of Warcraft BLP encoding formats.
	 */
	UNKNOWN(0x001, 2);

	/**
	 * Mask of what alphaBits values are valid for a given encoding type. Bit 0
	 * (value 1) represents the alphaBits value of 0.
	 */
	public final int alphaBitsMask;

	/**
	 * Minimum version that supports this encoding type.
	 */
	public final int minVersion;

	/**
	 * Cached array of valid alphaBits values generated from the alphaBitsMask.
	 */
	private final byte[] alphaBitsArray;

	BLPEncodingType(int alphaBitsMask, int minVestion) {
		this.alphaBitsMask = alphaBitsMask;
		this.minVersion = minVestion;

		// generate alphaBitsArray
		byte[] alphabitsdetect = new byte[32];
		int detected = 0;
		byte i = 0;
		while (alphaBitsMask != 0) {
			if ((alphaBitsMask & 0x1) != 0)
				alphabitsdetect[detected++] = i;
			alphaBitsMask >>>= 1;
			i += 1;
		}

		alphaBitsArray = Arrays.copyOfRange(alphabitsdetect, 0, detected);
	}

	/**
	 * Test if the specified alpha precision value is valid for this encoding
	 * type.
	 * 
	 * @param alphaBits
	 *            the alpha bit precision.
	 * @return true if the alpha bit precision is valid otherwise false.
	 */
	public boolean isAlphaBitsValid(int alphaBits) {
		return (alphaBitsMask >> alphaBits & 0x1) != 0;
	}

	/**
	 * Gets an array of alpha component precision values which are valid for
	 * this encoding type. All indexes in the array will contain a valid alpha
	 * precision value running from lowest precision in index 0 to highest
	 * precision at index length - 1.
	 * 
	 * @return array of valid alpha component precision values.
	 */
	public byte[] getAlphaBitsArray() {
		return alphaBitsArray.clone();
	}

	/**
	 * Gets the highest alpha component precision value supported by this
	 * encoding type. Using this value will give the least amount of alpha loss
	 * for this encoding type.
	 * 
	 * @return the highest alpha component precision value.
	 */
	public byte getBestAlphaBits() {
		return alphaBitsArray[alphaBitsArray.length - 1];
	}
}
