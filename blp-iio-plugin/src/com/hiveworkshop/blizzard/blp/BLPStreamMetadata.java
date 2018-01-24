package com.hiveworkshop.blizzard.blp;

import static com.hiveworkshop.blizzard.blp.BLPCommon.*;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;

import com.hiveworkshop.lang.LocalizedFormatedString;
import com.hiveworkshop.lang.MagicInt;

/**
 * BLP file header object. Describes the contents of a BLP file (the metadata).
 * The rest of the file is processed using this content description.
 * <p>
 * BLP header versions 0, 1 and 2 are supported. Each version imposes different
 * limitations on the state which can be represented as stated in the format
 * specification. Mutator method calls which attempt to set object state to
 * values not supported by the version will result in an
 * IllegalArgumentException being thrown.
 * <p>
 * Reading a header object with some invalid state from an image stream may
 * generate warnings however the resulting object state will still be valid.
 * <p>
 * Node view and manipulation is currently not supported as no metadata format
 * has been designed for BLP. Type cast BLP stream IIOMetadata objects to this
 * class and manipulate them directly using the many accessor and mutator
 * methods.
 * <p>
 * Only BLP version 0 and 1 are fully supported. Version 2 lacks sufficient
 * documentation to create a reliable implementation. It will still parse and
 * produce version 2 headers however such headers cannot be used in a sensible
 * way.
 * 
 * @author Imperial Good
 */
public final class BLPStreamMetadata extends IIOMetadata {
	/**
	 * The maximum dimension size allowed by version 0 and loaded by version 1.
	 */
	public static final int LEGACY_MAX_DIMENSION = 512;

	/**
	 * The BLP Content type.
	 */
	public enum ContentType {
		/**
		 * Image data is stored as a JFIF (JPEG) file of BGRA components.
		 */
		JPEG,
		/**
		 * Image data is stored directly as a map of pixels of varying formats.
		 */
		DIRECT
	}

	/**
	 * The BLP Content type.
	 */
	public enum PixmapType {
		/**
		 * Said to be associated with JPEG content.
		 */
		NONE,
		/**
		 * 256 color indexed with optional alpha.
		 */
		INDEXED,
		/**
		 * One of a variety of sample model based compressions.
		 */
		SAMPLED,
		/**
		 * Separate BGRA component values.
		 */
		BGRA,
		/**
		 * Possibly has different component order to BGRA.
		 */
		BGRA_2
	}

	/**
	 * The BLP sample type, used as a form of BLP compression.
	 */
	public enum SampleType {
		/**
		 * DXT1 sample model. 4*4 pixels in 64 bits with 1 bit alpha support.
		 */
		DXT1,
		/**
		 * DXT3 sample model. 4*4 pixels in 128 bits with 4 bit alpha support.
		 */
		DXT3,
		/**
		 * Component sampling model with 32 bit pixels of full color.
		 */
		BGRA8888,
		/**
		 * Component sampling model with 16 bit pixels and alpha masking.
		 */
		BGRA5551,
		/**
		 * Component sampling model with 16 bit pixels of 16 value color
		 * components.
		 */
		BGRA4444,
		/**
		 * Component sampling model with 16 bit pixels and no alpha.
		 */
		BGR565,
		/**
		 * Who knows!
		 */
		UNKNOWN1,
		/**
		 * DXT5 sample model. 4*4 pixels in 128 bits with 8 bit alpha support.
		 */
		DXT5,
		/**
		 * Who knows!
		 */
		UNKNOWN2,
		/**
		 * Component sampling model with 18 bit pixels?
		 */
		BGRA2565
	}

	/**
	 * BLP format version. Determines overall compatibility and features.
	 */
	private int version;

	/**
	 * Content type being used.
	 */
	private ContentType contentType;

	/**
	 * Pixmap type being used.
	 */
	private PixmapType pixmapType;

	/**
	 * Sample type being used.
	 */
	private SampleType sampleType;

	/**
	 * Alpha bit precision of contained image data.
	 */
	private byte alphaBits;

	/**
	 * Accompanying mipmap images exist.
	 */
	private boolean hasMipmaps;

	/**
	 * Image width in pixels.
	 */
	private int width;

	/**
	 * Image height in pixels.
	 */
	private int height;

	/**
	 * An extra integer that is never used.
	 */
	private int extra;

	/**
	 * Warning consumer function. Default is to log own warnings.
	 */
	private Consumer<LocalizedFormatedString> warning;

	/**
	 * Constructs a BLP1 header for JPEG content with no alpha or mipmaps and
	 * image dimensions of 1*1.
	 */
	public BLPStreamMetadata() {
		version = 1;
		setEncoding(BLPEncodingType.JPEG, (byte) 0);
		hasMipmaps = true;
		width = 1;
		height = 1;
		extra = 6;
		setWarningHandler(null);
	}

	private static void warn(LocalizedFormatedString msg) {
		Logger.getLogger(BLPStreamMetadata.class.getName()).warning(
				msg.toString());
	}

	/**
	 * Sets the function for processing warning messages. Warning messages are
	 * usually generated during input from potentially malformed files which
	 * still can be parsed.
	 * <p>
	 * If handler is null then a default logging function will be used.
	 * 
	 * @param handler
	 *            function to handle warning messages.
	 */
	public void setWarningHandler(Consumer<LocalizedFormatedString> handler) {
		if (handler == null)
			handler = BLPStreamMetadata::warn;
		warning = handler;
	}

	/**
	 * Get the general encoding type of the image. This is the high-level
	 * meaning of several different fields.
	 * 
	 * @return the encoding type for the image.
	 */
	public BLPEncodingType getEncodingType() {
		// convert configuration to encoding
		if (contentType == ContentType.JPEG && pixmapType == PixmapType.NONE)
			return BLPEncodingType.JPEG;
		else if (contentType == ContentType.DIRECT
				&& pixmapType == PixmapType.INDEXED)
			return BLPEncodingType.INDEXED;
		return BLPEncodingType.UNKNOWN;
	}

	/**
	 * Get the alpha component bit precision. The value returned is always valid
	 * for the encoding type used.
	 * 
	 * @return the bit precision of the alpha component.
	 */
	public byte getAlphaBits() {
		return alphaBits;
	}

	/**
	 * Set the encoding type for the image. Takes both an encoding type and
	 * alpha component bit precision value to enforce consistency.
	 * 
	 * @param encodingType
	 *            the encoding type to use.
	 * @param alphaBits
	 *            the bit precision of the alpha component.
	 * @throws IllegalArgumentException
	 *             if encodingType is UNKNOWN.
	 * @throws IllegalArgumentException
	 *             if encodingType does not support alphaBit.
	 * @throws IllegalArgumentException
	 *             if version does not support encodingType.
	 */
	public void setEncoding(BLPEncodingType encodingType, byte alphaBits) {
		if (encodingType == BLPEncodingType.UNKNOWN)
			throw new IllegalArgumentException(
					"cannot use UNKNOWN encodingType");
		else if (!encodingType.isAlphaBitsValid(alphaBits))
			throw new IllegalArgumentException(
					"encodingType does not support alphaBits");
		else if (encodingType.minVersion > version)
			throw new IllegalArgumentException(
					"version does not support encodingType");

		// convert encoding to configuration
		switch (encodingType) {
		default:
		case JPEG:
			contentType = ContentType.JPEG;
			pixmapType = PixmapType.NONE;
			sampleType = SampleType.DXT1;
			break;
		case INDEXED:
			contentType = ContentType.DIRECT;
			pixmapType = PixmapType.INDEXED;
			sampleType = SampleType.DXT1;
			break;
		}

		this.alphaBits = alphaBits;
	}

	/**
	 * Get the BLP version number that is currently being used.
	 * 
	 * @return the blp version number.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Specify which BLP version to use. Different BLP versions have different
	 * features and compatibility.
	 * <p>
	 * Version 0 is only used by the Warcraft III Reign of Chaos beta. Version 1
	 * is used by the release versions of Warcraft III. Version 2 is used by
	 * World of Warcraft.
	 * 
	 * @param version
	 *            the new version number.
	 * @throws IllegalArgumentException
	 *             if version is not a supported version (0 to 2).
	 */
	public void setVersion(int version) {
		if (version < 0 || 2 < version)
			throw new IllegalArgumentException("versions 0 to 2 supported");
		this.version = version;
	}

	/**
	 * Test if mipmap images are specified.
	 * 
	 * @return true if mipmap images are specified, otherwise false.
	 */
	public boolean hasMipmaps() {
		return hasMipmaps;
	}

	/**
	 * Specify the existence of mipmap images. If true then a full series of
	 * mipmap images are available. If false then only the full sized image is
	 * available.
	 * <p>
	 * Mipmap images are usually required by model textures. Mipmap images are
	 * usually not required by UI elements.
	 * 
	 * @param hasMipmaps
	 *            if mipmap images exist.
	 */
	public void setMipmaps(boolean hasMipmaps) {
		this.hasMipmaps = hasMipmaps;
	}

	/**
	 * Scales an image dimension to be for a given mipmap level.
	 * 
	 * @param dimension
	 *            the dimension to scale in pixels.
	 * @param level
	 *            the mipmap level.
	 * @return the mipmap dimension in pixels.
	 */
	private static int scaleImageDimension(int dimension, int level) {
		return Math.max(dimension >>> level, 1);
	}

	/**
	 * Get the image width in pixels.
	 * 
	 * @return width in pixels.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the image width in pixels for a certain mipmap level.
	 * 
	 * @param level
	 *            mipmap level.
	 * @return width in pixels.
	 */
	public int getWidth(int level) {
		return scaleImageDimension(width, level);
	}

	/**
	 * Set the image width in pixels. Width is valid between 1 and
	 * getDimensionMaximum.
	 * 
	 * @param width
	 *            width in pixels.
	 * @throws IllegalArgumentException
	 *             if width is invalid.
	 */
	public void setWidth(int width) {
		if (width < 1 || getDimensionMaximum() < width)
			throw new IllegalArgumentException("Invalid dimension size.");
		this.width = width;
	}

	/**
	 * Get the image height in pixels.
	 * 
	 * @return height in pixels.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Get the image height in pixels for a certain mipmap level.
	 * 
	 * @param level
	 *            mipmap level.
	 * @return height in pixels.
	 */
	public int getHeight(int level) {
		return scaleImageDimension(height, level);
	}

	/**
	 * Set the image height in pixels. height is valid between 1 and
	 * getDimensionMaximum.
	 * 
	 * @param height
	 *            height in pixels.
	 * @throws IllegalArgumentException
	 *             if height is invalid.
	 */
	public void setHeight(int height) {
		if (height < 1 || getDimensionMaximum() < height)
			throw new IllegalArgumentException("Invalid dimension size.");
		this.height = height;
	}

	/**
	 * Maximum image dimension size in pixels. With a dimension larger than this
	 * value are not valid.
	 * 
	 * @return the maximum allowed dimension size.
	 */
	public int getDimensionMaximum() {
		if (version < 1)
			return LEGACY_MAX_DIMENSION;
		return (1 << MIPMAP_MAX) - 1;
	}

	/**
	 * Convenience method that derives the number of mipmap levels for the
	 * image.
	 * <p>
	 * Images without mipmaps have only 1 level which is the full sized image.
	 * Images with mipmaps have a number of mipmaps based on the maximum of
	 * image height and width.
	 * 
	 * @return the number of mipmap levels for the image.
	 */
	public int getMipmapCount() {
		// if mipmaps then number of mipmap levels based on largest dimension
		return hasMipmaps ? 32 - Integer.numberOfLeadingZeros(Math.max(width,
				height)) : 1;
	}

	public void readObject(ImageInputStream src) throws IOException {
		src.setByteOrder(ByteOrder.LITTLE_ENDIAN);

		// read and validate magic and version
		version = resolveVersion(new MagicInt(src.readInt(),
				ByteOrder.LITTLE_ENDIAN));
		if (version == -1) {
			throw new IIOException("Not valid BLP file magic.");
		}

		// read contentType
		int content = src.readInt();
		ContentType[] ctvalues = ContentType.values();
		if (content >= ctvalues.length || content < 0) {
			final ContentType defaultContentType = ContentType.JPEG;
			warning.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "BadContent", content,
					defaultContentType.name()));
			content = defaultContentType.ordinal();
		}
		contentType = ctvalues[content];

		if (version < 2) {
			// read alphaBits
			alphaBits = (byte) src.readInt();

			// fill in non-existent fields
			pixmapType = PixmapType.values()[contentType.ordinal()];
			sampleType = SampleType.DXT1;
		} else {
			// read pixmapType
			int pixmap = src.readByte() & 0xFF;
			PixmapType[] ptvalues = PixmapType.values();
			if (pixmap >= ptvalues.length)
				throw new IIOException(String.format(
						"pixmap type %#0X is invalid", pixmap));
			pixmapType = ptvalues[pixmap];

			// read alphaBits
			alphaBits = src.readByte();

			// read sampleType
			int sample = src.readByte() & 0xFF;
			SampleType[] stvalues = SampleType.values();
			if (sample >= stvalues.length)
				throw new IIOException(String.format(
						"sample type %#0X is invalid", (byte) sample));
			sampleType = SampleType.values()[sample];

			// read hasMipmaps
			hasMipmaps = src.readByte() != 0;
		}

		final BLPEncodingType encodingType = getEncodingType();
		if (encodingType == BLPEncodingType.UNKNOWN)
			warning.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "BadEncoding", contentType
							.name(), pixmapType.name(), sampleType.name()));
		if (!encodingType.isAlphaBitsValid(alphaBits)) {
			final int defaultAlphaBits = 0;
			warning.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "BadAlpha", alphaBits,
					defaultAlphaBits));
			alphaBits = defaultAlphaBits;
		}

		// read width and height
		width = src.readInt();
		height = src.readInt();

		// clamp width and height
		final int maxDim = getDimensionMaximum();
		final long widthU = width & 0xFFFFFFFFL;
		final long heightU = height & 0xFFFFFFFFL;
		if (maxDim < Math.max(widthU, heightU)) {
			if (version < 1) {
				// assumed behavior based on Warcraft III prior to 1.27b
				throw new IIOException(
						String.format("Invalid image dimensions %d*%d pixels.",
								width, height));
			}
			final long oldWidth = widthU;
			final long oldHeight = heightU;

			// clamp to maximum dimension
			width = (int) (Math.min(widthU, maxDim));
			height = (int) (Math.min(heightU, maxDim));

			warning.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "BadDimension", oldWidth,
					oldHeight, width, height));
		}

		if (version < 2) {
			// read extra value
			extra = src.readInt();

			// read hasMipmaps
			hasMipmaps = src.readInt() != 0;
		}

		// warn about unusable mipmaps
		int bigDim = Math.max(width, height);
		if (version < 2 && LEGACY_MAX_DIMENSION < bigDim) {
			int i = 0;
			while (LEGACY_MAX_DIMENSION < bigDim) {
				i += 1;
				bigDim >>>= 1;
			}
			warning.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "WastefulDimension", i));
		}
	}

	public void writeObject(ImageOutputStream dst) throws IOException {
		dst.setByteOrder(ByteOrder.LITTLE_ENDIAN);

		// write magic and version
		dst.writeInt(resolveMagic(version).toInt(ByteOrder.LITTLE_ENDIAN));

		// write content
		dst.writeInt(contentType.ordinal());

		if (version < 2) {
			// write alphaBits
			dst.writeInt(alphaBits & 0xFF);
		} else {
			// write pixmapType
			dst.writeByte(pixmapType.ordinal());

			// write alphaBits
			dst.writeByte(alphaBits);

			// write sampleType
			dst.writeByte(sampleType.ordinal());

			// write hasMipmaps
			dst.writeByte(hasMipmaps ? 1 : 0);
		}

		// write width and height
		dst.writeInt(width);
		dst.writeInt(height);

		if (version < 2) {
			// write unknown, value does not appear to matter.
			dst.writeInt(extra);

			// write hasMipmaps
			dst.writeInt(hasMipmaps ? 1 : 0);
		}
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Node getAsTree(String formatName) {
		throw new IllegalArgumentException("no formats are supported");
	}

	@Override
	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		throw new IllegalStateException();

	}

	@Override
	public void reset() {
		throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return "{BLP Stream Metadata: Version = " + version + ", width = "
				+ width + ", height = " + height + ", content = " + contentType
				+ ", pixmap = " + pixmapType + ", sample = " + sampleType
				+ ", alpha bits = " + alphaBits + ", mipmaps = " + hasMipmaps
				+ ", extra = " + extra + "}";
	}

}
