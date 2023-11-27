package com.hiveworkshop.rms.util.sound.SimpleGuiFlacPlayer.tempStuff;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.*;
import java.net.URL;

public class FlacFileReader extends AudioFileReader {
	@Override
	public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
		return null;
	}

	@Override
	public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
		return null;
	}

	@Override
	public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
		return null;
	}

	@Override
	public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
		return null;
	}

	@Override
	public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
		return null;
	}

	@Override
	public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
		final InputStream fileStream = new FileInputStream(file);
		try {
			Stream stream = new Stream(file);
			if (stream.readUint(32) != 0x664C6143) {
				throw new FormatException("Invalid magic string");
			}
			FlacMetaData flacMetaData = readAndGetMetaData(stream);
			AudioFormat audioFormat = new AudioFormat(flacMetaData.sampleRate, flacMetaData.sampleDepth, flacMetaData.numChannels, true, false);
			return new FlacInputStream(stream, audioFormat, flacMetaData);
		} catch (final Throwable e) {
			closeSilently(fileStream);
			throw e;
		}
	}
	private static void closeSilently(final InputStream is) {
		try {
			is.close();
		} catch (final IOException ignored) {
			// IOException is ignored
		}
	}


	private FlacMetaData readAndGetMetaData(Stream input) throws IOException {
		// Handle metadata blocks
		FlacMetaData flacMetaData = new FlacMetaData();
		for (boolean last = false; !last; ) {
			last = input.readUint(1) != 0;
			int type = input.readUint(7);
			int length = input.readUint(24);
			if (type == 0) {  // Parse stream info block
				int minBlockSize = input.readUint(16);
				int maxBlockSize = input.readUint(16);
				if (minBlockSize == maxBlockSize) {
					flacMetaData.constantBlockSize = minBlockSize;
				}
				input.readUint(24);
				input.readUint(24);
				flacMetaData.sampleRate = input.readUint(20);
				flacMetaData.numChannels = input.readUint(3) + 1;
				flacMetaData.sampleDepth = input.readUint(5) + 1;
				flacMetaData.numSamples = (long) input.readUint(18) << 18 | input.readUint(18);

				// MD5 signature
				for (int i = 0; i < 16; i++) {
					input.readUint(8);
				}
			} else {  // Skip other blocks
				for (int i = 0; i < length; i++) {
					input.readUint(8);
				}
			}
		}
		if (flacMetaData.sampleRate == -1) {
			throw new RuntimeException("Stream info metadata block absent");
		}
		flacMetaData.metaDataSize = input.getPosition();
		return flacMetaData;
	}

	public static class FlacMetaData {
		public int sampleRate = -1;
		public int numChannels = -1;
		public int sampleDepth = -1;
		public long numSamples = -1;
		public int constantBlockSize = -1;
		public long metaDataSize = 0;
	}

	// Provides low-level bit/byte reading of a file.
	public static final class Stream extends InputStream {

		private RandomAccessFile raf;
		private long bytePosition;
		private InputStream byteBuffer;
		private long bitBuffer = 0;
		private int bitBufferLen;

		public Stream(File file) throws IOException {
			raf = new RandomAccessFile(file, "r");
			seekTo(0);
		}


		@Override
		public int read() throws IOException {
			return readByte();
		}

		public void close() throws IOException {
			raf.close();
		}

		public long getLength() throws IOException {
			return raf.length();
		}

		public long getPosition() {
			return bytePosition;
		}

		@Override
		public long skip(long n) throws IOException {
			long currPos = bytePosition;
			long newPos = bytePosition + n;
			if (newPos < 0) {
				newPos = 0;
			}
			seekTo(newPos);

			return bytePosition - currPos;
		}
		@Override
		public void skipNBytes(long n) throws IOException {
			long skipped = skip(n);
			if (skipped != n) {
				throw new IOException("Unable to skip exactly");
			}

		}

		public void seekTo(long pos) throws IOException {
			raf.seek(pos);
			bytePosition = pos;
			byteBuffer = new BufferedInputStream(new InputStream() {
				public int read() throws IOException {
					return raf.read();
				}

				public int read(byte[] b, int off, int len) throws IOException {
					return raf.read(b, off, len);
				}
			});
			bitBufferLen = 0;
		}

		public int readByte() throws IOException {
			if (bitBufferLen >= 8) {
				return readUint(8);
			} else {
				int result = byteBuffer.read();
				if (result != -1) {
					bytePosition++;
				}
				return result;
			}
		}

		public int readUint(int n) throws IOException {
			while (bitBufferLen < n) {
				int temp = byteBuffer.read();
				if (temp == -1)
					throw new EOFException();
				bytePosition++;
				bitBuffer = (bitBuffer << 8) | temp;
				bitBufferLen += 8;
			}
			bitBufferLen -= n;
			int result = (int) (bitBuffer >>> bitBufferLen);
			if (n < 32)
				result &= (1 << n) - 1;
			return result;
		}

		public int readSignedInt(int n) throws IOException {
			return (readUint(n) << (32 - n)) >> (32 - n);
		}

		public void alignToByte() {
			bitBufferLen -= bitBufferLen % 8;
		}

	}

	// Thrown when non-conforming FLAC data is read.
	@SuppressWarnings("serial")
	public static class FormatException extends IOException {
		public FormatException(String msg) {
			super(msg);
		}
	}
}
