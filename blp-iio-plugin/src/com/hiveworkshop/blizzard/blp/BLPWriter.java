package com.hiveworkshop.blizzard.blp;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import com.hiveworkshop.lang.LocalizedFormatedString;

import static com.hiveworkshop.blizzard.blp.BLPWriteParam.ScaleOptimization;
import static com.hiveworkshop.blizzard.blp.BLPStreamMetadata.LEGACY_MAX_DIMENSION;

public class BLPWriter extends ImageWriter {
	/**
	 * The mipmap level for the next written image.
	 */
	private int imageIndex = 0;

	/**
	 * The stream metadata to write.
	 */
	private BLPStreamMetadata streamMetadata = null;

	/**
	 * Mipmap manager adapter class. Turns varying manager interfaces into a
	 * standard writer interface.
	 */
	private static abstract class MipmapWriter {
		public void writeMipmapManager(ImageOutputStream ios)
				throws IOException {
		}

		public void startMipmapSequence(ImageOutputStream ios)
				throws IOException {
		}

		public abstract void setMipmapDataChunk(int mipmap, byte[] mmData)
				throws IOException;
	}

	/**
	 * Mipmap to place mipmap data with.
	 */
	private MipmapWriter mipmapWriter = null;

	/**
	 * The mipmapProcessor being used.
	 */
	private MipmapProcessor mipmapProcessor = null;

	/**
	 * Image output stream to write to.
	 */
	private ImageOutputStream iosOutput = null;

	/**
	 * Image output stream is internally managed.
	 */
	private boolean internalOutput = false;

	/**
	 * Output is unsuitable to write images.
	 */
	private boolean badOutput = false;

	/**
	 * List to hold mipmap data that cannot immediately be written.
	 */
	private List<byte[]> mmDataList = null;

	/**
	 * MipmapWriter is ready to write mipmap data.
	 */
	private boolean canWriteMipmaps = false;

	public BLPWriter(ImageWriterSpi originatingProvider) {
		super(originatingProvider);
	}

	@Override
	public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
		BLPStreamMetadata smd = new BLPStreamMetadata();
		if (param instanceof BLPWriteParam) {
			BLPWriteParam blpParam = (BLPWriteParam) param;
			smd.setMipmaps(blpParam.isAutoMipmap());
			ImageTypeSpecifier its = blpParam.getDestinationType();
			if (its != null) {
				ColorModel cm = its.getColorModel();
				if (cm instanceof BLPIndexColorModel) {
					smd.setEncoding(BLPEncodingType.INDEXED, (byte) cm
							.getComponentSize(cm.getNumColorComponents()));
				} else if (cm instanceof IndexColorModel) {
					smd.setEncoding(BLPEncodingType.INDEXED, (byte) 0);
				} else {
					smd.setEncoding(BLPEncodingType.JPEG,
							(byte) (cm.hasAlpha() ? 8 : 0));
				}
			}
		}
		return smd;
	}

	@Override
	public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,
			ImageWriteParam param) {
		return null;
	}

	@Override
	public IIOMetadata convertStreamMetadata(IIOMetadata inData,
			ImageWriteParam param) {
		return getDefaultStreamMetadata(param);
	}

	@Override
	public IIOMetadata convertImageMetadata(IIOMetadata inData,
			ImageTypeSpecifier imageType, ImageWriteParam param) {
		return null;
	}

	@Override
	public ImageWriteParam getDefaultWriteParam() {
		return new BLPWriteParam();
	}

	/**
	 * Sends all attached warning listeners a warning message. The messages will
	 * be localized for each warning listener.
	 * 
	 * @param msg
	 *            the warning message to send to all warning listeners.
	 * @param level
	 *            the mipmap level the warning occured for.
	 */
	protected void processWarningOccurred(LocalizedFormatedString msg, int level) {
		if (warningListeners == null)
			return;
		else if (msg == null)
			throw new IllegalArgumentException("msg is null.");
		int numListeners = warningListeners.size();
		for (int i = 0; i < numListeners; i++) {
			IIOWriteWarningListener listener = warningListeners.get(i);
			Locale locale = (Locale) warningLocales.get(i);
			if (locale == null) {
				locale = Locale.getDefault();
			}
			listener.warningOccurred(this, level, msg.toString(locale));
		}
	}

	@Override
	public void write(IIOMetadata streamMetadata, IIOImage image,
			ImageWriteParam param) throws IOException {
		// validate paramters
		if (image.hasRaster())
			throw new UnsupportedOperationException("Cannot encode raster.");
		else if (output == null)
			throw new IllegalStateException("No output.");
		else if (badOutput)
			throw new IIOException("Cannot write to stream.");

		// process output
		if (iosOutput == null) {
			// identify output type
			if (output instanceof File) {
				iosOutput = new FileImageOutputStream((File) output);
				internalOutput = true;
			} else if (output instanceof Path) {
				iosOutput = new FileImageOutputStream(((Path) output).toFile());
				internalOutput = true;
			} else if (output instanceof ImageOutputStream) {
				iosOutput = (ImageOutputStream) output;
			} else {
				throw new IllegalStateException("Unsupported output.");
			}

			// check stream is empty
			if (iosOutput.length() > 0) {
				badOutput = true;
				throw new IIOException("Stream not empty.");
			}
		}
		
		RenderedImage im = image.getRenderedImage();
		
		// Prepare default param if required.
		if (param == null) {
			param = getDefaultWriteParam();
			param.setDestinationType(new ImageTypeSpecifier(im));
		}

		// get image processing values
		Rectangle sourceRegion = new Rectangle(0, 0, im.getWidth(),
				im.getHeight());
		int sourceXSubsampling = 1;
		int sourceYSubsampling = 1;
		int[] sourceBands = null;
		Point destOff = new Point();
		Rectangle sourceRegionParam = param.getSourceRegion();
		if (sourceRegionParam != null)
			sourceRegion = sourceRegion.intersection(param
					.getSourceRegion());
		destOff = param.getDestinationOffset();
		sourceXSubsampling = param.getSourceXSubsampling();
		sourceYSubsampling = param.getSourceYSubsampling();
		sourceBands = param.getSourceBands();
		int subsampleXOffset = param.getSubsamplingXOffset();
		int subsampleYOffset = param.getSubsamplingYOffset();
		sourceRegion.x += subsampleXOffset;
		sourceRegion.y += subsampleYOffset;
		sourceRegion.width -= subsampleXOffset;
		sourceRegion.height -= subsampleYOffset;

		// create source Raster
		int width = sourceRegion.width;
		int height = sourceRegion.height;
		Raster imRas = im.getData(sourceRegion);
		int numBands = imRas.getNumBands();

		// validate source bands
		if (sourceBands != null) {
			for (int i = 0; i < sourceBands.length; i++) {
				final int bandOff = sourceBands[i];
				if (bandOff < 0 || numBands <= bandOff) {
					throw new IllegalArgumentException("Bad source bands.");
				}
			}
		}

		// translate raster and apply bands
		imRas = imRas.createChild(sourceRegion.x, sourceRegion.y, width,
				height, 0, 0, sourceBands);

		// apply subsampling to width and height
		width = (width + sourceXSubsampling - 1) / sourceXSubsampling;
		height = (height + sourceYSubsampling - 1) / sourceYSubsampling;

		// create and fill destination WritableRaster
		WritableRaster destWR = imRas.createCompatibleWritableRaster(destOff.x,
				destOff.y, width + destOff.x, height + destOff.y);
		Object transferCache = null;
		for (int y = 0; y < height; y += 1) {
			for (int x = 0; x < width; x += 1) {
				transferCache = imRas.getDataElements(x * sourceXSubsampling, y
						* sourceYSubsampling, transferCache);
				destWR.setDataElements(x, y, transferCache);
			}
		}

		// create destination BufferedImage
		ColorModel srcCM = im.getColorModel();
		BufferedImage destImg = new BufferedImage(srcCM, destWR,
				srcCM.isAlphaPremultiplied(), null);
		int destW = destImg.getWidth();
		int destH = destImg.getHeight();

		// stream setup
		if (imageIndex == 0) {
			// process stream metadata
			if (!(streamMetadata instanceof BLPStreamMetadata)) {
				streamMetadata = convertStreamMetadata(streamMetadata, param);
			}
			this.streamMetadata = (BLPStreamMetadata) streamMetadata;

			// resolve output image dimensions
			boolean rescaleDest = false;
			ScaleOptimization autoScale = ScaleOptimization.CLAMP;
			if (param instanceof BLPWriteParam)
				autoScale = ((BLPWriteParam) param).getScaleOptimization();
			int worst = Math.max(destW, destH);
			final int max_dimension = this.streamMetadata.getVersion() < 2 ? LEGACY_MAX_DIMENSION
					: this.streamMetadata.getDimensionMaximum();
			if (worst > max_dimension) {
				switch (autoScale) {
				case RATIO:
					destW = (int) (((long) destW * max_dimension + worst / 2) / worst);
					destH = (int) (((long) destH * max_dimension + worst / 2) / worst);
					rescaleDest = true;
					break;
				case CLAMP:
					destW = Math.min(destW, max_dimension);
					destH = Math.min(destH, max_dimension);
					rescaleDest = true;
					break;
				default:
					break;
				}
			}
			this.streamMetadata.setHeight(destH);
			this.streamMetadata.setWidth(destW);
			if (!(param instanceof BLPWriteParam)) {
				this.streamMetadata.setEncoding(BLPEncodingType.JPEG, srcCM.hasAlpha() ? (byte)8 : (byte)0);
			}

			// rescale output image if required
			if (rescaleDest) {
				processWarningOccurred(
						new LocalizedFormatedString(
								"com.hiveworkshop.text.blp", "WriteResize",
								destImg.getWidth(), destImg.getHeight(), destW,
								destH), imageIndex);
				BufferedImage destImgNew = new BufferedImage(srcCM, destImg
						.getRaster().createCompatibleWritableRaster(destW,
								destH), srcCM.isAlphaPremultiplied(), null);
				Graphics2D graphics = destImgNew.createGraphics();
				RenderingHints rh = new RenderingHints(
						RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				graphics.setRenderingHints(rh);
				graphics.drawImage(destImg.getScaledInstance(destW, destH,
						Image.SCALE_AREA_AVERAGING), 0, 0, destW, destH, null);
				graphics.dispose();
				destImg = destImgNew;
			}

			// construct mipmap manager
			if (this.streamMetadata.getVersion() < 1) {
				// external mipmaps
				Path path;
				if (output instanceof File)
					path = ((File) output).toPath();
				else if (output instanceof Path)
					path = (Path) output;
				else
					throw new IllegalStateException(
							"Version 0 can only be written to Path of File.");
				ExternalMipmapManager emm = new ExternalMipmapManager(path);
				mipmapWriter = new MipmapWriter() {
					@Override
					public void setMipmapDataChunk(int mipmap, byte[] mmData)
							throws IOException {
						emm.setMipmapDataChunk(mipmap, mmData);
					}
				};
			} else {
				// internal mipmaps
				InternalMipmapManager imm = new InternalMipmapManager();
				mipmapWriter = new MipmapWriter() {
					private long objectPos = -1l;

					@Override
					public void writeMipmapManager(ImageOutputStream ios)
							throws IOException {
						if (objectPos == -1l) {
							objectPos = ios.getStreamPosition();
						} else {
							ios.seek(objectPos);
						}
						imm.writeObject(ios);
					}

					@Override
					public void startMipmapSequence(ImageOutputStream ios)
							throws IOException {
						imm.setMipmapDataChunkBlockOffset(ios);
					}

					@Override
					public void setMipmapDataChunk(int mipmap, byte[] mmData)
							throws IOException {
						imm.setMipmapDataChunk(iosOutput, mipmap, mmData);
					}
				};
			}

			// construct mipmap processor
			BLPEncodingType encodingType = this.streamMetadata
					.getEncodingType();
			switch (encodingType) {
			case INDEXED:
				mipmapProcessor = new IndexedMipmapProcessor(
						this.streamMetadata.getAlphaBits());
				break;
			case JPEG:
				mipmapProcessor = new JPEGMipmapProcessor(
						this.streamMetadata.getAlphaBits());
				break;
			case UNKNOWN:
			default:
				throw new IIOException("Unsupported encoding type.");
			}

			// write out header
			iosOutput.seek(0);
			this.streamMetadata.writeObject(iosOutput);
			mipmapWriter.writeMipmapManager(iosOutput);

			mmDataList = new ArrayList<byte[]>(
					this.streamMetadata.getMipmapCount());
		}

		// mipmap count test
		final int mmCount = this.streamMetadata.getMipmapCount();
		if (imageIndex >= mmCount)
			throw new IIOException("Image limit reached.");

		// image scale test
		int mmH = this.streamMetadata.getHeight(imageIndex);
		int mmW = this.streamMetadata.getWidth(imageIndex);
		if (destW != mmW || destH != mmH)
			throw new IIOException(
					String.format(
							"Invalid image dimensions: Got %d*%d pixels requires %d*%d pixels.",
							destW, destH, mmW, mmH));
		
		// encode image
		processImageStarted(imageIndex);
		byte[] mmData = mipmapProcessor.encodeMipmap(destImg, param,
				warn -> this.processWarningOccurred(warn, imageIndex));

		// write out mipmap data
		if (mipmapProcessor.mustPostProcess()) {
			mmDataList.add(mmData);
		} else {
			if (!canWriteMipmaps && mipmapProcessor.canDecode()) {
				mipmapProcessor.writeObject(iosOutput);
				mipmapWriter.startMipmapSequence(iosOutput);
				canWriteMipmaps = true;
			}
			mipmapWriter.setMipmapDataChunk(imageIndex, mmData);
			mipmapWriter.writeMipmapManager(iosOutput);
		}
		imageIndex += 1;
		processImageComplete();

		// resolve auto mipmap
		boolean autoMipmap = true;
		if (param instanceof BLPWriteParam) {
			autoMipmap = ((BLPWriteParam) param).isAutoMipmap();
		}

		// apply auto mipmaps
		if (autoMipmap) {
			RenderingHints rh = new RenderingHints(
					RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			while (imageIndex < mmCount) {
				// create scaled image
				processImageStarted(imageIndex);
				mmH = this.streamMetadata.getHeight(imageIndex);
				mmW = this.streamMetadata.getWidth(imageIndex);
				BufferedImage mmImg = new BufferedImage(srcCM, destImg
						.getRaster().createCompatibleWritableRaster(mmW, mmH),
						srcCM.isAlphaPremultiplied(), null);
				Graphics2D graphics = mmImg.createGraphics();
				graphics.setRenderingHints(rh);
				graphics.drawImage(destImg.getScaledInstance(mmW, mmH,
						Image.SCALE_AREA_AVERAGING), 0, 0, mmW, mmH, null);
				graphics.dispose();

				// encode image
				mmData = mipmapProcessor.encodeMipmap(mmImg, param,
						warn -> this.processWarningOccurred(warn, imageIndex));

				// write out mipmap data
				if (mipmapProcessor.mustPostProcess()) {
					mmDataList.add(mmData);
				} else {
					mipmapWriter.setMipmapDataChunk(imageIndex, mmData);
					mipmapWriter.writeMipmapManager(iosOutput);
				}
				imageIndex += 1;
				processImageComplete();
			}
		}

		if (imageIndex == mmCount) {
			// post process mipmaps
			if (mipmapProcessor.mustPostProcess()) {
				mmDataList = mipmapProcessor.postProcessMipmapData(mmDataList,
						warn -> this.processWarningOccurred(warn, -1));
				mipmapProcessor.writeObject(iosOutput);
				mipmapWriter.startMipmapSequence(iosOutput);
				canWriteMipmaps = true;
				for (int i = 0; i < mmCount; i += 1) {
					mipmapWriter.setMipmapDataChunk(i, mmDataList.get(i));
				}
				mipmapWriter.writeMipmapManager(iosOutput);
				mmDataList.clear();
			}

			// close internal image output stream
			if (internalOutput) {
				iosOutput.close();
				internalOutput = false;
			}
		}
	}

	@Override
	public void setOutput(Object output) {
		super.setOutput(output);

		// close internal image output stream
		if (internalOutput) {
			try {
				iosOutput.close();
			} catch (IOException e) {
				processWarningOccurred(
						new LocalizedFormatedString(
								"com.hiveworkshop.text.blp", "ISCloseFail",
								e.getMessage()), -1);
			}
		}

		// warn if incomple file was written
		if (!badOutput && streamMetadata != null
				&& imageIndex != streamMetadata.getMipmapCount()) {
			processWarningOccurred(new LocalizedFormatedString(
					"com.hiveworkshop.text.blp", "IncompleteFile"), -1);
		}

		// reset state
		imageIndex = 0;
		streamMetadata = null;
		mipmapWriter = null;
		mipmapProcessor = null;
		iosOutput = null;
		internalOutput = false;
		badOutput = false;
		mmDataList = null;
		canWriteMipmaps = false;
	}

	@Override
	public void dispose() {
		setOutput(null);
	}

}
