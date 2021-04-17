package de.wc3data.stream;

import java.io.*;

public class BlizzardDataOutputStream extends BufferedOutputStream {

	public BlizzardDataOutputStream(final File file) throws FileNotFoundException {
		super(new FileOutputStream(file));
	}

	public BlizzardDataOutputStream(final File file, final boolean append) throws FileNotFoundException {
		super(new FileOutputStream(file, append));
	}

	public BlizzardDataOutputStream(final OutputStream outputStream) {
		super(outputStream);
	}

	static byte[] convertIntToByteArray(final int i) {
		final byte[] result = new byte[4];
		long l = i >= 0 ? i : 4294967296L + i;
		for (int j = 0; j < 4; j++) {
			result[j] = ((byte) (int) (l % 256L));
			l /= 256L;
		}
		return result;
	}

	static byte[] convertIntToNByteArray(final int i, final int numBytes) {
		final byte[] result = new byte[numBytes];
		long l = i >= 0 ? i : 4294967296L + i;
		for (int j = 0; j < numBytes; j++) {
			result[j] = ((byte) (int) (l % 256L));
			l /= 256L;
		}
		return result;
	}

	public void writeChars(final char[] toWrite) throws IOException {
		final byte[] b = new byte[toWrite.length];
		for (int i = 0; i < toWrite.length; i++) {
			b[i] = ((byte) toWrite[i]);
		}
		write(b);
	}

	public void writeInt(final int toWrite) throws IOException {
		write(convertIntToByteArray(toWrite));
	}

	public void writeNByteInt(final int toWrite, final int numBytes) throws IOException {
		write(convertIntToNByteArray(toWrite, numBytes));
	}

	public void writeBool(final boolean toWrite) throws IOException {
		writeInt(toWrite ? 1 : 0);
	}

	public void writeByte(final int toWrite) throws IOException {
		write(new byte[] { (byte) toWrite });
	}

	public void writeFloat(final float toWrite) throws IOException {
		writeInt(Float.floatToIntBits(toWrite));
	}

	public void writeFourByteString(final String toWrite) throws IOException {
		final byte[] result = new byte[4];
		for (int i = 0; i < 4; i++) {
			if ((toWrite != null) && (i < toWrite.length())) {
				result[i] = ((byte) toWrite.charAt(i));
			} else {
				result[i] = 0;
			}
		}
		write(result);
	}

	public void writeNByteString(final String toWrite, final int charCount) throws IOException {
		final byte[] result = new byte[charCount];
		for (int i = 0; i < charCount; i++) {
			if ((toWrite != null) && (i < toWrite.length())) {
				result[i] = ((byte) toWrite.charAt(i));
			} else {
				result[i] = 0;
			}
		}
		write(result);
	}

	public void writeString(final String toWrite) throws IOException {
		int length;
		if (toWrite == null) {
			length = 0;
		} else {
			length = toWrite.length();
		}
		final byte[] result = new byte[length + 1];
		for (int i = 0; i < length; i++) {
			result[i] = ((byte) toWrite.charAt(i));
		}
		result[length] = 0;
		write(result);
	}
}
