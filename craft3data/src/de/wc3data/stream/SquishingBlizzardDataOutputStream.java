package de.wc3data.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SquishingBlizzardDataOutputStream extends BlizzardDataOutputStream {

	public SquishingBlizzardDataOutputStream(final File file, final boolean append) throws FileNotFoundException {
		super(file, append);
	}

	public SquishingBlizzardDataOutputStream(final File file) throws FileNotFoundException {
		super(file);
	}

	@Override
	public void writeFloat(final float toWrite) throws IOException {
		writeInt(Float.floatToIntBits(toWrite) & 0xFFFF0000);
	}

}
