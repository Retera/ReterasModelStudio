package com.hiveworkshop.rms.parsers.twiImageStuff;

import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.Header;
import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.Type;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Locale;

public class TwiTGAReaderSpi extends ImageReaderSpi {
	@Override
	public boolean canDecodeInput(Object source) throws IOException {
		if (source instanceof ImageInputStream) {
			// Record stream state.
			final ImageInputStream src = (ImageInputStream) source;
			src.mark();
			ByteOrder order = src.getByteOrder();
			try {
				src.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				byte[] headerBytes = new byte[18];
				src.read(headerBytes);
				Header header = new Header(headerBytes);
				if(header.getType() != Type.UNKNOWN
						&& header.isValidColorMapPixelBits()
						&& header.isValidPixelDepth()){
					return true;
				}
			} finally {
				src.setByteOrder(order);
				src.reset();
			}
		}
		return false;
	}

	@Override
	public ImageReader createReaderInstance(Object extension) throws IOException {
		return new TwiTGAReader(this);
	}

	@Override
	public String getDescription(Locale locale) {
		return "TGA file image reader";
	}
}
