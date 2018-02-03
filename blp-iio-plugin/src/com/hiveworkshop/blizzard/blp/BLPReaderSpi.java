package com.hiveworkshop.blizzard.blp;

import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.EXTRA_IMAGE_METADATA_CLASS;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.EXTRA_IMAGE_METADATA_NAME;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.EXTRA_STREAM_METADATA_CLASS;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.EXTRA_STREAM_METADATA_NAME;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.FORMAT_MIMES;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.FORMAT_NAMES;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.FORMAT_SUFFIXES;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.NATIVE_IMAGE_METADATA_CLASS;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.NATIVE_IMAGE_METADATA_NAME;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.NATIVE_STREAM_METADATA_CLASS;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.NATIVE_STREAM_METADATA_NAME;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.STANDARD_IMAGE_METADATA_SUPPORT;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.STANDARD_STREAM_METADATA_SUPPORT;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.VENDOR;
import static com.hiveworkshop.blizzard.blp.ImageSpiCommon.VERSION;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import com.hiveworkshop.lang.MagicInt;

/**
 * Service provider for BLP image file ImageReader.
 *
 * @author Imperial Good
 */
public class BLPReaderSpi extends ImageReaderSpi {
	static final String READER_CLASS = "com.hiveworkshop.blizzard.blp.BLPReader";
	static final Class<?>[] INPUT_TYPES = { ImageInputStream.class, File.class, Path.class };
	static final String[] WRITER_SPI_CLASSES = { "com.hiveworkshop.blizzard.blp.BLPWriterSpi" };

	public BLPReaderSpi() {
		super(VENDOR, VERSION, FORMAT_NAMES, FORMAT_SUFFIXES, FORMAT_MIMES, READER_CLASS, INPUT_TYPES,
				WRITER_SPI_CLASSES, STANDARD_STREAM_METADATA_SUPPORT, NATIVE_STREAM_METADATA_NAME,
				NATIVE_STREAM_METADATA_CLASS, EXTRA_STREAM_METADATA_NAME, EXTRA_STREAM_METADATA_CLASS,
				STANDARD_IMAGE_METADATA_SUPPORT, NATIVE_IMAGE_METADATA_NAME, NATIVE_IMAGE_METADATA_CLASS,
				EXTRA_IMAGE_METADATA_NAME, EXTRA_IMAGE_METADATA_CLASS);
	}

	@Override
	public boolean canDecodeInput(final Object source) throws IOException {
		if (source instanceof ImageInputStream) {
			// Can check ImageInputStream.
			final ImageInputStream src = (ImageInputStream) source;

			// Check stream.
			final ByteOrder originalByteOrder = src.getByteOrder();
			src.mark();
			try {
				// Check magic number.
				src.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				final MagicInt magic = new MagicInt(src.readInt(), ByteOrder.LITTLE_ENDIAN);
				if (BLPCommon.resolveVersion(magic) != -1) {
					return true;
				}
			} finally {
				// Always rewind stream.
				src.reset();
				src.setByteOrder(originalByteOrder);
			}

		}

		return false;
	}

	@Override
	public ImageReader createReaderInstance(final Object arg0) throws IOException {
		return new BLPReader(this);
	}

	@Override
	public String getDescription(final Locale locale) {
		return "BLP file image reader.";
	}

}
