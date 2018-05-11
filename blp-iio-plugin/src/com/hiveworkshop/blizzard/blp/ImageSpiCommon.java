package com.hiveworkshop.blizzard.blp;

/**
 * Common for blp spis.
 * <p>
 * Contains all constants in common with the spi classes.
 * 
 * @author Imperial Good
 */
abstract class ImageSpiCommon {
	// file format specification
	static final String VENDOR = "Hive Workshop";
	static final String VERSION = "1.1";
	static final String[] FORMAT_NAMES = { "Blizzard Picture", "blp" };
	static final String[] FORMAT_SUFFIXES = { "blp" };
	static final String[] FORMAT_MIMES = { "image/hw.blp" };

	// metadata format specification
	static final boolean STANDARD_STREAM_METADATA_SUPPORT = false;
	static final String NATIVE_STREAM_METADATA_NAME = null;
	static final String NATIVE_STREAM_METADATA_CLASS = null;
	static final String[] EXTRA_STREAM_METADATA_NAME = null;
	static final String[] EXTRA_STREAM_METADATA_CLASS = null;
	static final boolean STANDARD_IMAGE_METADATA_SUPPORT = false;
	static final String NATIVE_IMAGE_METADATA_NAME = null;
	static final String NATIVE_IMAGE_METADATA_CLASS = null;
	static final String[] EXTRA_IMAGE_METADATA_NAME = null;
	static final String[] EXTRA_IMAGE_METADATA_CLASS = null;

}
