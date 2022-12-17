package com.hiveworkshop.rms.parsers.twiImageStuff;

import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.ColorMap;
import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.Header;
import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.ImageData;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Iterator;

public class TwiTGAReader extends ImageReader {
	/**
	 * Constructs an {@code ImageReader} and sets its
	 * {@code originatingProvider} field to the supplied value.
	 *
	 * <p> Subclasses that make use of extensions should provide a
	 * constructor with signature {@code (ImageReaderSpi,Object)}
	 * in order to retrieve the extension object.  If
	 * the extension object is unsuitable, an
	 * {@code IllegalArgumentException} should be thrown.
	 *
	 * @param originatingProvider the {@code ImageReaderSpi} that is
	 *                            invoking this constructor, or {@code null}.
	 */

	private ImageInputStream imageInputStream;
	private Header header;
	protected TwiTGAReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	public void setInput(final Object input, final boolean seekForwardOnly,
	                     final boolean ignoreMetadata) {
		super.setInput(input, seekForwardOnly, ignoreMetadata);
		if(input instanceof ImageInputStream){
			imageInputStream = (ImageInputStream) input;
			imageInputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		}
	}

	public Header getHeader() throws IOException {
		if(header == null && imageInputStream != null){
			byte[] headerBytes = new byte[18];
			imageInputStream.read(headerBytes);
			header = new Header(headerBytes);
		}
		return header;
	}

	@Override
	public int getNumImages(boolean allowSearch) throws IOException {
		return 1;
	}

	@Override
	public int getWidth(int imageIndex) throws IOException {
		Header header = getHeader();
		if(header != null){
			return header.getWidth();
		}
		return 0;
	}

	@Override
	public int getHeight(int imageIndex) throws IOException {
		Header header = getHeader();
		if(header != null){
			return header.getHeigth();
		}
		return 0;
	}

	@Override
	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
		return null;
	}

	@Override
	public IIOMetadata getStreamMetadata() throws IOException {
		return null;
	}

	@Override
	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		return null;
	}

	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
		Header header = getHeader();
		if(header != null){
			byte[] headerBytes = new byte[18];
			imageInputStream.read(headerBytes);
			byte[] imageId = new byte[header.getIdFieldLength()];
			imageInputStream.read(imageId);
			byte[] colorData = new byte[header.getColorMapLength()];
			imageInputStream.read(colorData);
			ColorMap colorMap = new ColorMap(header.getColorMapFirstIndex(), header.getColorMapLength(), header.getColorMapPixelBits(), colorData);
			int numPixels = header.getWidth() * header.getHeigth();
			int imageDataSize = numPixels * header.getPixelDepth();
			byte[] pixelData = new byte[imageDataSize];
			imageInputStream.read(pixelData);
			ImageData imageData = new ImageData(header.getWidth(), header.getHeigth(), header.getPixelDepth(), header.getAttributeBits(), pixelData, header.getType());
			return new TwiTGAFile(header, imageId, colorMap, imageData).getAsBufferedImage();
		}
		return null;
	}
}
