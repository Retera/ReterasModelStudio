package com.hiveworkshop.blizzard.blp;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.hiveworkshop.lang.LocalizedFormatedString;

/**
 * Mipmap processor for JPEG content BLP files.
 * <p>
 * In the case that a decoded JPEG image is not the correct size, it is resized
 * and a warning generated. Resizing occurs by padding/cropping the right and
 * bottom edges of the image. Padding is transparent black.
 * <p>
 * Some poor BLP implementations, such as used by Warcraft III 1.27a, do not
 * read and process mipmap data safely so might be able to extract a valid JPEG
 * file from a technically corrupt file.
 * <p>
 * Both 8 and 0 bit alpha is supported. A fully opaque alpha band is encoded
 * when set to 0 bits. When decoding 0 bit alpha and not using direct read a
 * warning is generated if the alpha channel is not fully opaque. Some poor BLP
 * implementations, such as used by Warcraft III 1.27a, can still process the
 * dummy alpha band which can result in undesirable visual artifacts depending
 * on use.
 * <p>
 * The JPEG ImageReader used can be controlled by a BLPReadParam. Likewise the
 * JPEG ImageWriter used can be controlled by a BLPWriteParam. For best encoding
 * results it is recommended the JPEG ImageWriter be kept constant for all
 * mipmap levels.
 * 
 * @author Imperial Good
 */
class JPEGMipmapProcessor extends MipmapProcessor {
	/**
	 * The maximum valid shared header length.
	 * <p>
	 * Shared headers beyond this size might cause massive image corruption or
	 * crashes in some readers.
	 */
	private static final int MAX_SHARED_HEADER_LENGTH = 0x270;

	/**
	 * BLP JPEG content band mapping array.
	 */
	private static final int[] JPEG_BAND_ARRAY = { 2, 1, 0, 3 };

	/**
	 * The color model that the processor will use.
	 */
	private final ColorModel jpegBLPColorModel;

	/**
	 * JPEG header block.
	 */
	private byte[] jpegHeader = null;

	/**
	 * Constructs a MipmapProcessor for JPEG content.
	 * 
	 * @param alphaBits
	 *            the alpha component bits, if any.
	 * @throws IllegalArgumentException
	 *             if alphaBits is not valid.
	 */
	public JPEGMipmapProcessor(int alphaBits) {
		if (!BLPEncodingType.JPEG.isAlphaBitsValid(alphaBits))
			throw new IllegalArgumentException("Unsupported alphaBits.");
		final boolean hasAlpha = alphaBits == 8;
		jpegBLPColorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), hasAlpha,
				false, hasAlpha ? Transparency.TRANSLUCENT
						: Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	}

	@Override
	public boolean mustPostProcess() {
		return true;
	}

	@Override
	public List<byte[]> postProcessMipmapData(List<byte[]> mmDataList,
			Consumer<LocalizedFormatedString> handler) {
		// determine maximum shared header
		byte[] sharedHeader = mmDataList.get(0).clone();
		int sharedLength = sharedHeader.length;
		final int mmDataNum = mmDataList.size();
		for (int i = 1; i < mmDataNum; i += 1) {
			final byte[] mmData = mmDataList.get(i);
			for (int shared = 0; shared < sharedLength; shared += 1) {
				if (mmData[shared] != sharedHeader[shared]) {
					sharedLength = shared;
					break;
				}
			}
		}

		// process shared header length
		sharedLength = Math.min(sharedLength, MAX_SHARED_HEADER_LENGTH);
		if (sharedLength < 64) {
			handler.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "JPEGSmallShared",
					sharedLength));
		}

		// produce shared header
		jpegHeader = Arrays.copyOf(sharedHeader, sharedLength);
		canDecode = true;

		// process mipmap data
		if (sharedLength == 0)
			return mmDataList;
		List<byte[]> mmDataListOut = new ArrayList<byte[]>(mmDataNum);
		for (int i = 0; i < mmDataNum; i += 1) {
			final byte[] mmData = mmDataList.get(i);
			mmDataListOut.add(Arrays.copyOfRange(mmData, sharedLength,
					mmData.length));
		}

		return mmDataListOut;
	}

	@Override
	public byte[] encodeMipmap(BufferedImage img, ImageWriteParam param,
			Consumer<LocalizedFormatedString> handler) throws IOException {
		// resolve a JPEG ImageWriter
		ImageWriter jpegWriter = null;
		if (param instanceof BLPWriteParam
				&& ((BLPWriteParam) param).getJPEGSpi() != null) {
			// use explicit JPEG reader
			jpegWriter = ((BLPWriteParam) param).getJPEGSpi()
					.createWriterInstance();
		} else {
			// find a JPEG reader
			Iterator<ImageWriter> jpegWriters = ImageIO
					.getImageWritersByFormatName("jpeg");
			while (jpegWriters.hasNext()) {
				final ImageWriter writer = jpegWriters.next();
				if (writer.canWriteRasters()) {
					jpegWriter = writer;
					break;
				}
			}
		}
		// validate JPEG writer
		if (jpegWriter == null)
			throw new IIOException("No suitable JPEG ImageWriter installed.");
		else if (!jpegWriter.canWriteRasters()) {
			throw new IIOException(String.format(
					"JPEG ImageWriter cannot write raster: vendor = %s.",
					jpegWriter.getOriginatingProvider().getVendorName()));
		}

		// prepare raster
		final WritableRaster srcWR = img.getRaster();
		final SampleModel srcSM = srcWR.getSampleModel();
		final int h = srcSM.getHeight();
		final int w = srcSM.getWidth();
		final WritableRaster destWR = WritableRaster.createBandedRaster(
				DataBuffer.TYPE_BYTE, w, h, JPEG_BAND_ARRAY.length, null);
		final int srcBandN = srcSM.getSampleSize().length;
		if (srcBandN == JPEG_BAND_ARRAY.length) {
			destWR.setRect(srcWR);
		} else {
			final int bandNum = Math.min(JPEG_BAND_ARRAY.length, srcBandN);
			final boolean opaque = !jpegBLPColorModel.hasAlpha()
					|| bandNum < JPEG_BAND_ARRAY.length;
			for (int y = 0; y < h; y += 1) {
				for (int x = 0; x < w; x += 1) {
					for (int b = 0; b < bandNum; b += 1) {
						destWR.setSample(x, y, b, srcWR.getSample(x, y, b));
					}
					if (opaque)
						destWR.setSample(x, y, 3, 255);
				}
			}
		}

		// prepare buffered JPEG file
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(100 << 10);
		final ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
		jpegWriter.setOutput(ios);

		// write JPEG file
		final ImageWriteParam jpegParam = jpegWriter.getDefaultWriteParam();
		jpegParam.setSourceBands(JPEG_BAND_ARRAY);
		jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		final String[] compressionTypes = jpegParam.getCompressionTypes();
		if (compressionTypes != null && compressionTypes.length > 0) {
			jpegParam.setCompressionType(compressionTypes[0]);
		}
		if (param != null && param.canWriteCompressed()
				&& param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
			jpegParam.setCompressionQuality(param.getCompressionQuality());
		} else {
			jpegParam.setCompressionQuality(BLPWriteParam.DEFAULT_QUALITY);
		}
		jpegWriter.addIIOWriteWarningListener(new IIOWriteWarningListener() {
			@Override
			public void warningOccurred(ImageWriter source, int imageIndex,
					String warning) {
				handler.accept(new LocalizedFormatedString(
						"com.hiveworkshop.text.blp", "JPEGWarning", warning));
			}
		});
		jpegWriter.write(null, new IIOImage(destWR, null, null), jpegParam);

		// cleanup
		jpegWriter.dispose();
		ios.close();
		bos.close();

		return bos.toByteArray();
	}

	@Override
	public BufferedImage decodeMipmap(byte[] mmData, ImageReadParam param,
			int width, int height, Consumer<LocalizedFormatedString> handler)
			throws IOException {
		final boolean directRead = param == null
				|| (param instanceof BLPReadParam && ((BLPReadParam) param)
						.isDirectRead());

		// resolve a JPEG ImageReader
		ImageReader jpegReader = null;
		if (param instanceof BLPReadParam
				&& ((BLPReadParam) param).getJPEGSpi() != null) {
			// use explicit JPEG reader
			jpegReader = ((BLPReadParam) param).getJPEGSpi()
					.createReaderInstance();
		} else {
			// find a JPEG reader
			Iterator<ImageReader> jpegReaders = ImageIO
					.getImageReadersByFormatName("jpeg");
			while (jpegReaders.hasNext()) {
				final ImageReader reader = jpegReaders.next();
				if (reader.canReadRaster()) {
					jpegReader = reader;
					break;
				}
			}
		}
		// validate JPEG reader
		if (jpegReader == null)
			throw new IIOException("No suitable JPEG ImageReader installed.");
		else if (!jpegReader.canReadRaster()) {
			throw new IIOException(String.format(
					"JPEG ImageReader cannot read raster: vendor = %s.",
					jpegReader.getOriginatingProvider().getVendorName()));
		}

		// create a buffered JPEG file in memory
		byte[] jpegBuffer = Arrays.copyOf(jpegHeader, jpegHeader.length
				+ mmData.length);
		System.arraycopy(mmData, 0, jpegBuffer, jpegHeader.length,
				mmData.length);

		// input buffered JPEG file
		InputStream bis = new ByteArrayInputStream(jpegBuffer);
		ImageInputStream iis = new MemoryCacheImageInputStream(bis);
		jpegReader.setInput(iis, true, true);

		// read source raster
		jpegReader.addIIOReadWarningListener(new IIOReadWarningListener() {
			@Override
			public void warningOccurred(ImageReader source, String warning) {
				handler.accept(new LocalizedFormatedString(
						"com.hiveworkshop.text.blp", "JPEGWarning", warning));
			}
		});
		ImageReadParam jpegParam = jpegReader.getDefaultReadParam();
		jpegParam.setSourceBands(JPEG_BAND_ARRAY);
		if (directRead) {
			// optimizations to improve direct read mode performance
			jpegParam.setSourceRegion(new Rectangle(width, height));
		}
		Raster srcRaster = jpegReader.readRaster(0, jpegParam);

		// cleanup
		iis.close();
		jpegReader.dispose();

		// direct read shortcut
		if (directRead && srcRaster instanceof WritableRaster
				&& srcRaster.getWidth() == width
				&& srcRaster.getHeight() == height) {
			WritableRaster destRaster = (WritableRaster) srcRaster;

			// enforce alpha band to match color model
			if (!jpegBLPColorModel.hasAlpha())
				destRaster = destRaster.createWritableChild(0, 0,
						destRaster.getWidth(), destRaster.getHeight(), 0, 0,
						new int[] { 0, 1, 2 });

			return new BufferedImage(jpegBLPColorModel, destRaster, false, null);
		}

		// alpha warning check
		if (!jpegBLPColorModel.hasAlpha()) {
			final int[] alphaSamples = srcRaster.getSamples(0, 0,
					srcRaster.getWidth(), srcRaster.getHeight(), 3,
					(int[]) null);
			for (int aSample : alphaSamples) {
				if (aSample != 255) {
					handler.accept(new LocalizedFormatedString(
							"com.hiveworkshop.text.blp", "BadPixelAlpha"));
					break;
				}
			}
		}

		// dimension check warning
		if (srcRaster.getWidth() != width || srcRaster.getHeight() != height)
			handler.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "JPEGDimensionMismatch",
					srcRaster.getWidth(), srcRaster.getHeight(), width, height));

		// create destination image
		BufferedImage destImg = new BufferedImage(
				jpegBLPColorModel,
				jpegBLPColorModel.createCompatibleWritableRaster(width, height),
				false, null);
		WritableRaster destRaster = destImg.getRaster();

		// copy data
		destRaster.setRect(srcRaster.createChild(
				0,
				0,
				srcRaster.getWidth(),
				srcRaster.getHeight(),
				0,
				0,
				Arrays.copyOf(new int[] { 0, 1, 2, 3 },
						jpegBLPColorModel.getNumComponents())));

		return destImg;
	}

	@Override
	public Iterator<ImageTypeSpecifier> getSupportedImageTypes(int width,
			int height) {
		return Arrays.asList(
				new ImageTypeSpecifier(jpegBLPColorModel, jpegBLPColorModel
						.createCompatibleSampleModel(width, height)))
				.iterator();
	}

	@Override
	public void readObject(ImageInputStream src,
			Consumer<LocalizedFormatedString> warning) throws IOException {
		// read JPEG header
		src.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		final int length = src.readInt();
		byte[] jpegh = new byte[length];
		src.readFully(jpegh, 0, jpegh.length);

		// process length
		if (length > MAX_SHARED_HEADER_LENGTH) {
			warning.accept(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "JPEGBigShared", length,
					MAX_SHARED_HEADER_LENGTH));
		}

		jpegHeader = jpegh;
		canDecode = true;
	}

	@Override
	public void writeObject(ImageOutputStream dst) throws IOException {
		byte[] jpegh = jpegHeader != null ? jpegHeader : new byte[0];

		// write JPEG header
		dst.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		dst.writeInt(jpegh.length);
		dst.write(jpegh);
	}
}
