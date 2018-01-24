package com.hiveworkshop.blizzard.blp;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.hiveworkshop.lang.LocalizedFormatedString;

/**
 * Implementation class for the BLP image reader.
 * <p>
 * Supports opening of BLP versions 0 and 1. Mipmap levels translate into image
 * number.
 * <p>
 * Default resulting BufferedImage objects may come in a variety of image types
 * based on the content of the blp file. The image type chosen aims to preserve
 * the underlying data structure.
 * <p>
 * No image metadata can be extracted to preserve JPEG content image quality.
 * <p>
 * Raster is not supported. Read progress updates are not supported, but all
 * other listeners work.
 * 
 * @author ImperialGood
 */
public class BLPReader extends ImageReader {
	/**
	 * BLP stream metadata object. Represents the contents of the BLP file
	 * header and is used to decode all mipmap levels.
	 */
	private BLPStreamMetadata streamMeta = null;

	/**
	 * Internally managed ImageInputStream.
	 */
	private ImageInputStream intSrc = null;

	/**
	 * Mipmap manager adapter class. Turns varying manager interfaces into a
	 * standard reader interface.
	 */
	private static abstract class MipmapReader {
		public abstract byte[] getMipmapDataChunk(int mipmap)
				throws IOException;

		public void flushTo(int mipmap) throws IOException {
		}
	}

	/**
	 * Mipmap reader to get mipmap data chunks from.
	 */
	private MipmapReader mipmapReader;

	/**
	 * Mipmap processor for content.
	 */
	private MipmapProcessor mipmapProcessor = null;

	public String tempGetInfo() throws IOException {
		loadHeader();
		return streamMeta.toString();
	}

	public BLPReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/**
	 * Loads the BLP header from an input source. The header is only loaded once
	 * with the results cached for performance.
	 * 
	 * @throws IOException
	 *             - is header cannot be loaded
	 */
	private void loadHeader() throws IOException {
		// only do something if header has not already been loaded
		if (streamMeta != null)
			return;

		// check if a source has been set
		if (input == null)
			throw new IllegalStateException("no input source has been set");

		// check if input is a file system path
		Path path = null;
		if (input instanceof Path) {
			// directly use path
			path = (Path) input;
		} else if (input instanceof File) {
			// use path of file
			path = ((File) input).toPath();
		}

		// resolve input stream
		ImageInputStream src;
		if (input instanceof ImageInputStream) {
			// ImageInputStream provided
			src = (ImageInputStream) input;
		} else if (path != null) {
			// create internally managed ImageInputStream
			intSrc = new FileImageInputStream(path.toFile());

			// validate Path
			if (intSrc == null)
				throw new IllegalStateException(
						"Cannot create ImageInputStream from path.");
			src = intSrc;
		} else
			// invalid input has been assigned
			throw new IllegalStateException("bad input state");

		// start from beginning of stream
		src.seek(0);

		BLPStreamMetadata streamMeta = new BLPStreamMetadata();
		streamMeta.setWarningHandler(this::processWarningOccurred);
		streamMeta.readObject(src);

		// read mipmap location data
		MipmapReader mipmapReader;
		if (streamMeta.getVersion() > 0) {
			// mipmap chunks within same file
			InternalMipmapManager imm = new InternalMipmapManager();
			imm.readObject(src);
			BLPReader thisref = this;

			mipmapReader = new MipmapReader() {
				@Override
				public byte[] getMipmapDataChunk(int mipmap) throws IOException {
					return imm.getMipmapDataChunk(src, mipmap,
							thisref::processWarningOccurred);
				}

				@Override
				public void flushTo(int mipmap) throws IOException {
					imm.flushToMipmap(src, mipmap);
				}
			};
		} else if (path != null) {
			// file must have ".blp" extension
			ExternalMipmapManager emm = new ExternalMipmapManager(path);

			mipmapReader = new MipmapReader() {
				@Override
				public byte[] getMipmapDataChunk(int mipmap) throws IOException {
					return emm.getMipmapDataChunk(mipmap);
				}
			};
		} else {
			// no path to locate mipmap chunk files
			throw new IIOException(
					"BLP0 image can only be loaded from Path or File input.");
		}

		// read content header
		if (streamMeta.getEncodingType() == BLPEncodingType.JPEG) {
			mipmapProcessor = new JPEGMipmapProcessor(streamMeta.getAlphaBits());
		} else if (streamMeta.getEncodingType() == BLPEncodingType.INDEXED) {
			mipmapProcessor = new IndexedMipmapProcessor(
					streamMeta.getAlphaBits());
		} else {
			throw new IIOException("Unsupported content type.");
		}
		mipmapProcessor.readObject(src, this::processWarningOccurred);

		// if seeking forward only then header data can now be discarded
		if (seekForwardOnly)
			mipmapReader.flushTo(0);

		this.streamMeta = streamMeta;
		this.mipmapReader = mipmapReader;
	}

	/**
	 * Checks if the given image index is valid.
	 * 
	 * @param imageIndex
	 *            the image index to check.
	 * @throws IndexOutOfBoundsException
	 *             if the image does not exist.
	 */
	private void checkImageIndex(int imageIndex) {
		// test if image mipmap level exists
		if (streamMeta.getMipmapCount() <= imageIndex)
			throw new IndexOutOfBoundsException(String.format(
					"Mipmap level does not exist: %d.", imageIndex));

		// test for seekForwardOnly functionality
		if (imageIndex < minIndex)
			throw new IndexOutOfBoundsException(String.format(
					"Violation of seekForwardOnly: at %d wanting %d.",
					minIndex, imageIndex));
	}

	@Override
	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		// parent performs type checks and generates exceptions
		super.setInput(input, seekForwardOnly, ignoreMetadata);

		// close internal ImageInputStream
		if (intSrc != null) {
			try {
				intSrc.close();
			} catch (IOException e) {
				processWarningOccurred(new LocalizedFormatedString(
						"com.hiveworkshop.text.blp", "ISCloseFail",
						e.getMessage()));
			}
			intSrc = null;
		}

		streamMeta = null;
		mipmapReader = null;
	}

	/**
	 * Sends all attached warning listeners a warning message. The messages will
	 * be localized for each warning listener.
	 * 
	 * @param msg
	 *            the warning message to send to all warning listeners.
	 */
	protected void processWarningOccurred(LocalizedFormatedString msg) {
		if (warningListeners == null)
			return;
		else if (msg == null)
			throw new IllegalArgumentException("msg is null.");
		int numListeners = warningListeners.size();
		for (int i = 0; i < numListeners; i++) {
			IIOReadWarningListener listener = warningListeners.get(i);
			Locale locale = (Locale) warningLocales.get(i);
			if (locale == null) {
				locale = Locale.getDefault();
			}
			listener.warningOccurred(this, msg.toString(locale));
		}
	}

	@Override
	public int getHeight(int imageIndex) throws IOException {
		loadHeader();
		checkImageIndex(imageIndex);
		return streamMeta.getHeight(imageIndex);
	}

	@Override
	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return null;
	}

	@Override
	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
			throws IOException {
		loadHeader();
		checkImageIndex(imageIndex);

		return mipmapProcessor.getSupportedImageTypes(
				streamMeta.getWidth(imageIndex),
				streamMeta.getHeight(imageIndex));
	}

	@Override
	public int getNumImages(boolean allowSearch) throws IOException {
		loadHeader();
		return streamMeta.getMipmapCount();
	}

	@Override
	public IIOMetadata getStreamMetadata() throws IOException {
		loadHeader();
		return streamMeta;
	}

	@Override
	public int getWidth(int imageIndex) throws IOException {
		loadHeader();
		checkImageIndex(imageIndex);
		return streamMeta.getWidth(imageIndex);
	}

	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param)
			throws IOException {
		loadHeader();
		checkImageIndex(imageIndex);

		// seek forward functionality
		if (seekForwardOnly && minIndex < imageIndex) {
			minIndex = imageIndex;
			mipmapReader.flushTo(minIndex);
		}

		if (!mipmapProcessor.canDecode())
			throw new IIOException("Mipmap processor cannot decode.");

		processImageStarted(imageIndex);

		// get mipmap image data
		byte[] mmData = mipmapReader.getMipmapDataChunk(imageIndex);

		// unpack mipmap image data into a mipmap image
		final int width = streamMeta.getWidth(imageIndex);
		final int height = streamMeta.getHeight(imageIndex);
		BufferedImage srcImg = mipmapProcessor.decodeMipmap(mmData, param,
				width, height, this::processWarningOccurred);
		// imageIndex);
		BufferedImage destImg;

		// return src image if direct read mode is specified or no
		// ImageReadParam is present
		if (param == null
				|| (param instanceof BLPReadParam && ((BLPReadParam) param)
						.isDirectRead()))
			destImg = srcImg;
		else {
			destImg = getDestination(param, getImageTypes(imageIndex), width,
					height);

			checkReadParamBandSettings(param, srcImg.getSampleModel()
					.getNumBands(), destImg.getSampleModel().getNumBands());

			Rectangle srcRegion = new Rectangle();
			Rectangle destRegion = new Rectangle();
			computeRegions(param, width, height, destImg, srcRegion, destRegion);

			// extract param settings
			int[] srcBands = param.getSourceBands();
			int[] destBands = param.getDestinationBands();
			int ssX = param.getSourceXSubsampling();
			int ssY = param.getSourceYSubsampling();

			WritableRaster srcRaster = srcImg.getRaster().createWritableChild(
					srcRegion.x, srcRegion.y, srcRegion.width,
					srcRegion.height, 0, 0, srcBands);
			WritableRaster destRaster = destImg.getRaster()
					.createWritableChild(destRegion.x, destRegion.y,
							destRegion.width, destRegion.height, 0, 0,
							destBands);

			// copy pixels
			Object dataElements = null;
			for (int y = 0; y < destRegion.height; y += 1) {
				for (int x = 0; x < destRegion.width; x += 1) {
					final int srcXOff = ssX * x;
					final int srcYOff = ssY * y;
					dataElements = srcRaster.getDataElements(srcXOff, srcYOff,
							null);
					destRaster.setDataElements(x, y, dataElements);
				}
			}
		}

		processImageComplete();
		return destImg;
	}

	@Override
	public void dispose() {
		// force cleanup of existing state
		setInput(null);
	}

	@Override
	public ImageReadParam getDefaultReadParam() {
		return new BLPReadParam();
	}
}
