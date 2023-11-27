package com.hiveworkshop.rms.util.sound.SimpleGuiFlacPlayer.tempStuff;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.util.Arrays;

public class FlacInputStream extends AudioInputStream {
	private final FlacFileReader.Stream stream;
	private final FlacFileReader.FlacMetaData flacMetaData;

	public FlacInputStream(FlacFileReader.Stream stream, AudioFormat format, FlacFileReader.FlacMetaData flacMetaData) {
		super(stream, format, flacMetaData.numSamples);
		this.stream = stream;
		this.flacMetaData = flacMetaData;
	}


	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		byte[] sampleBytes = getSampleBytes();
		if (sampleBytes != null) {
			int pushBackLen = sampleBytes.length;

			System.arraycopy(sampleBytes, 0, b, off, pushBackLen);
			return pushBackLen;
		}
		return -1;
	}

	@Override
	public long skip(long n) throws IOException {
		System.out.println("skip() -> " + n);
		return super.skip(n);
	}

	private byte[] getSampleBytes(double seekReq, SourceDataLine line) throws IOException {
		return getSampleBytes(getSamples(seekReq, line));
	}
	private byte[] getSampleBytes() throws IOException {
		Object[] temp = readNextBlock();
		if (temp != null) {
			return getSampleBytes((long[][]) temp[0]);
		}
//		return getSampleBytes(null);
		return null;
	}
	private byte[] getSampleBytes(long[][] samples) {
		// Convert samples to channel-interleaved bytes in little endian
		int bytesPerSample = format.getSampleSizeInBits() / 8;
		byte[] sampleBytes = new byte[samples[0].length * samples.length * bytesPerSample];
		for (int i = 0, k = 0; i < samples[0].length; i++) {
			for (long[] sample : samples) {
				long val = sample[i];
				for (int j = 0; j < bytesPerSample; j++, k++) {
					sampleBytes[k] = (byte) (val >>> (j << 3));
				}
			}
		}
		return sampleBytes;
	}



	private long[][] getSamples(double seekReq, SourceDataLine line) throws IOException {
		// Decode next audio block, or seek and decode
		long[][] samples = null;
		if (seekReq == -1) {
			Object[] temp = readNextBlock();
			if (temp != null) {
				samples = (long[][]) temp[0];
			}
		} else {
			long samplePos = Math.round(seekReq * frameLength);
			samples = seekAndReadBlock(samplePos);
			line.flush();
		}
		return samples;
	}


	public long[][] seekAndReadBlock(long samplePos) throws IOException {
		// Binary search to find a frame slightly before requested position
		long startFilePos = flacMetaData.metaDataSize;
		long endFilePos = stream.getLength();
		long curSamplePos = 0;
		while (endFilePos - startFilePos > 100000) {
			long middle = (startFilePos + endFilePos) / 2;
			long[] offsets = findNextDecodableFrame(middle);
			if (offsets == null || offsets[1] > samplePos) {
				endFilePos = middle;
			} else {
				startFilePos = offsets[0];
				curSamplePos = offsets[1];
			}
		}

		stream.seekTo(startFilePos);
		while (true) {
			Object[] temp = readNextBlock();
			if (temp == null) {
				return null;
			}
			long[][] samples = (long[][]) temp[0];
			int blockSize = samples[0].length;
			long nextSamplePos = curSamplePos + blockSize;
			if (nextSamplePos > samplePos) {
				long[][] result = new long[samples.length][];
				for (int ch = 0; ch < format.getChannels(); ch++) {
					result[ch] = Arrays.copyOfRange(samples[ch], (int) (samplePos - curSamplePos), blockSize);
				}
				return result;
			}
			curSamplePos = nextSamplePos;
		}
	}


	// Returns (filePosition, sampleOffset) or null.
	private long[] findNextDecodableFrame(long filePos) throws IOException {
		while (true) {
			stream.seekTo(filePos);
			int state = 0;
			while (true) {
				int b = stream.readByte();
				if (b == -1) {
					return null;
				} else if (b == 0xFF) {
					state = 1;
				} else if (state == 1 && (b & 0xFE) == 0xF8) {
					break;
				} else {
					state = 0;
				}
			}
			filePos = stream.getPosition() - 2;
			stream.seekTo(filePos);
			try {
				Object[] temp = readNextBlock();
				if (temp == null) {
					return null;
				} else {
					return new long[] {filePos, (long) temp[1]};
				}
			} catch (FlacFileReader.FormatException e) {
				filePos += 2;
			}
		}
	}


	// Returns (long[][] blockSamples, long sampleOffsetAtStartOfBlock)
	// if a block is decoded, or null if the end of stream is reached.
	public Object[] readNextBlock() throws IOException {
		// Find next sync code
		int byteVal = stream.readByte();
		if (byteVal == -1) {
			return null;
		}
		int sync = byteVal << 6 | stream.readUint(6);
		if (sync != 0x3FFE) {
			throw new FlacFileReader.FormatException("Sync code expected");
		}
		if (stream.readUint(1) != 0) {
			throw new FlacFileReader.FormatException("Reserved bit");
		}
		int blockStrategy = stream.readUint(1);

		// Read numerous header fields, and ignore some of them
		int blockSizeCode = stream.readUint(4);

		int sampleRateCode = stream.readUint(4);
		int chanAsgn = stream.readUint(4);
		int sampleSize = stream.readUint(3);
		validateSampleSize(sampleSize);
		if (stream.readUint(1) != 0) {
			throw new FlacFileReader.FormatException("Reserved bit");
		}

		long rawPosition = getRawPosition();

		int blockSize = getBlockSize(blockSizeCode);

		if (sampleRateCode == 12) {
			stream.readUint(8);
		} else if (sampleRateCode == 13 || sampleRateCode == 14) {
			stream.readUint(16);
		}

		stream.readUint(8);

		// Decode each channel's subframe, then skip footer
		long[][] samples = decodeSubframes(blockSize, format.getSampleSizeInBits(), chanAsgn);
		stream.alignToByte();
		stream.readUint(16);
		return new Object[] {samples, rawPosition * (blockStrategy == 0 ? flacMetaData.constantBlockSize : 1)};
	}

	private long getRawPosition() throws IOException {
		int byteVal = stream.readUint(8);
		if (byteVal < 0x80) {
			return byteVal;
		} else {
			int rawPosNumBytes = Integer.numberOfLeadingZeros(~(byteVal << 24)) - 1;
			long rawPosition = byteVal & (0x3F >>> rawPosNumBytes);
			for (int i = 0; i < rawPosNumBytes; i++) {
				rawPosition = (rawPosition << 6) | (stream.readUint(8) & 0x3F);
			}
			return rawPosition;
		}
	}

	private int getBlockSize(int blockSizeCode) throws IOException {

		if (blockSizeCode == 1) {
			return 192;
		} else if (2 <= blockSizeCode && blockSizeCode <= 5) {
			return 576 << (blockSizeCode - 2);
		} else if (blockSizeCode == 6) {
			return stream.readUint(8) + 1;
		} else if (blockSizeCode == 7) {
			return stream.readUint(16) + 1;
		} else if (8 <= blockSizeCode && blockSizeCode <= 15) {
			return 256 << (blockSizeCode - 8);
		} else {
			throw new FlacFileReader.FormatException("Reserved block size");
		}
	}


	private long[][] decodeSubframes(int blockSize, int sampleDepth, int chanAsgn) throws IOException {
		long[][] result;
		if (0 <= chanAsgn && chanAsgn <= 7) {
			result = new long[chanAsgn + 1][blockSize];
			for (long[] longs : result) {
				decodeSubframe(sampleDepth, longs);
			}
		} else if (8 <= chanAsgn && chanAsgn <= 10) {
			result = new long[2][blockSize];
			decodeSubframe(sampleDepth + (chanAsgn == 9 ? 1 : 0), result[0]);
			decodeSubframe(sampleDepth + (chanAsgn == 9 ? 0 : 1), result[1]);
			if (chanAsgn == 8) {
				for (int i = 0; i < blockSize; i++) {
					result[1][i] = result[0][i] - result[1][i];
				}
			} else if (chanAsgn == 9) {
				for (int i = 0; i < blockSize; i++) {
					result[0][i] += result[1][i];
				}
			} else {
				for (int i = 0; i < blockSize; i++) {
					long side = result[1][i];
					long right = result[0][i] - (side >> 1);
					result[1][i] = right;
					result[0][i] = right + side;
				}
			}
		} else {
			throw new FlacFileReader.FormatException("Reserved channel assignment");
		}
		return result;
	}


	private void decodeSubframe(int sampleDepth, long[] result) throws IOException {
		if (stream.readUint(1) != 0) {
			throw new FlacFileReader.FormatException("Invalid padding bit");
		}
		int type = stream.readUint(6);
		int shift = stream.readUint(1);
		if (shift == 1) {
			while (stream.readUint(1) == 0) {
				shift++;
			}
		}
		sampleDepth -= shift;

		if (type == 0) { // Constant coding
			Arrays.fill(result, 0, result.length, stream.readSignedInt(sampleDepth));
		} else if (type == 1) {  // Verbatim coding
			for (int i = 0; i < result.length; i++) {
				result[i] = stream.readSignedInt(sampleDepth);
			}
		} else if (8 <= type && type <= 12 || 32 <= type && type <= 63) {
			int predOrder;
			int[] lpcCoefs;
			int lpcShift;
			if (type <= 12) {  // Fixed prediction
				predOrder = type - 8;
				for (int i = 0; i < predOrder; i++) {
					result[i] = stream.readSignedInt(sampleDepth);
				}
				lpcCoefs = FIXED_PREDICTION_COEFFICIENTS[predOrder];
				lpcShift = 0;
			} else {  // Linear predictive coding
				predOrder = type - 31;
				for (int i = 0; i < predOrder; i++) {
					result[i] = stream.readSignedInt(sampleDepth);
				}
				int precision = stream.readUint(4) + 1;
				lpcShift = stream.readSignedInt(5);
				lpcCoefs = new int[predOrder];
				for (int i = 0; i < predOrder; i++) {
					lpcCoefs[i] = stream.readSignedInt(precision);
				}
			}
			decodeRiceResiduals(predOrder, result);
			for (int i = predOrder; i < result.length; i++) {  // LPC restoration
				long sum = 0;
				for (int j = 0; j < lpcCoefs.length; j++) {
					sum += result[i - 1 - j] * lpcCoefs[j];
				}
				result[i] += sum >> lpcShift;
			}
		} else {
			throw new FlacFileReader.FormatException("Reserved subframe type");
		}

		for (int i = 0; i < result.length; i++) {
			result[i] <<= shift;
		}
	}


	private void decodeRiceResiduals(int warmup, long[] result) throws IOException {
		int method = stream.readUint(2);
		if (2 <= method) {
			throw new FlacFileReader.FormatException("Reserved residual coding method");
		}
		int paramBits = method == 0 ? 4 : 5;
		int escapeParam = method == 0 ? 0xF : 0x1F;
		int partitionOrder = stream.readUint(4);
		int numPartitions = 1 << partitionOrder;
		if (result.length % numPartitions != 0) {
			throw new FlacFileReader.FormatException("Block size not divisible by number of Rice partitions");
		}
		int partitionSize = result.length / numPartitions;

		for (int i = 0; i < numPartitions; i++) {
			int start = i * partitionSize + (i == 0 ? warmup : 0);
			int end = (i + 1) * partitionSize;
			int param = stream.readUint(paramBits);
			if (param < escapeParam) {
				for (int j = start; j < end; j++) {  // Read Rice signed integers
					long val = 0;
					while (stream.readUint(1) == 0) {
						val++;
					}
					val = (val << param) | stream.readUint(param);
					result[j] = (val >>> 1) ^ -(val & 1);
				}
			} else {
				int numBits = stream.readUint(5);
				for (int j = start; j < end; j++) {
					result[j] = stream.readSignedInt(numBits);
				}
			}
		}
	}

	public int readByte(byte[] bytes, int bitOffset, int bits) throws IOException {
		int byteOffset = bitOffset/8;
		int bitInByte = bitOffset%8;
		int firstBitByte = bytes[byteOffset];
		int bitsInFirst = 8 - bitInByte;
		int overFlowingBits = bits - bitsInFirst;

		int result = 0;
		firstBitByte = firstBitByte << bitInByte;
		for (int i = bitInByte; i<Math.min(8, bitInByte+bits); i++) {
			result = result << 1;
			result |= (firstBitByte & 1);
			firstBitByte = firstBitByte << 1;
		}
		if (0 < overFlowingBits) {
			int secondBitByte = bytes[byteOffset+1];
			for (int i = 0; i<overFlowingBits; i++) {
				result = result << 1;
				result |= (secondBitByte>>8 & 1);
				secondBitByte = secondBitByte << 1;
			}
		}
		return result;
	}

	public int readUint(byte[] bytes, int bitOffset, int bits) throws IOException {
		int byteOffset = bitOffset/8;
		int bitInByte = bitOffset%8;
		int firstBitByte = bytes[byteOffset];
		int bitsInFirst = 8 - bitInByte;
		int overFlowingBits = bits - bitsInFirst;

		int result = 0;
		firstBitByte = firstBitByte << bitInByte;
		for (int i = bitInByte; i<Math.min(8, bitInByte+bits); i++) {
			result = result << 1;
			result |= (firstBitByte & 1);
			firstBitByte = firstBitByte << 1;
		}
		if (0 < overFlowingBits) {
			int secondBitByte = bytes[byteOffset+1];
			for (int i = 0; i<overFlowingBits; i++) {
				result = result << 1;
				result |= (secondBitByte>>8 & 1);
				secondBitByte = secondBitByte << 1;
			}
		}
		return result;
	}

	public int readSignedInt(byte[] bytes, int bitOffset, int bits) throws IOException {
		return (readUint(bytes, bitOffset, bits) << (32 - bits)) >> (32 - bits);
	}


	private static final int[][] FIXED_PREDICTION_COEFFICIENTS = {
			{},
			{1},
			{2, -1},
			{3, -3, 1},
			{4, -6, 4, -1},
	};

	private void validateSampleSize(int sampleSize) throws FlacFileReader.FormatException {
		switch (sampleSize) {
			case 1 -> { if (format.getSampleSizeInBits() !=  8) throw new FlacFileReader.FormatException("Sample depth mismatch");}
			case 2 -> { if (format.getSampleSizeInBits() != 12) throw new FlacFileReader.FormatException("Sample depth mismatch");}
			case 4 -> { if (format.getSampleSizeInBits() != 16) throw new FlacFileReader.FormatException("Sample depth mismatch");}
			case 5 -> { if (format.getSampleSizeInBits() != 20) throw new FlacFileReader.FormatException("Sample depth mismatch");}
			case 6 -> { if (format.getSampleSizeInBits() != 24) throw new FlacFileReader.FormatException("Sample depth mismatch");}
			default -> throw new FlacFileReader.FormatException("Reserved/invalid sample depth");
		}
	}
}
