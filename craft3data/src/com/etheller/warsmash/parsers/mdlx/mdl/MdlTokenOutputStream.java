package com.etheller.warsmash.parsers.mdlx.mdl;

import java.io.IOException;

public class MdlTokenOutputStream {
	private final Appendable buffer;
	private final int index;
	private int ident;
	private final int fractionDigits;

	public MdlTokenOutputStream(final Appendable appendable) {
		this.buffer = appendable;
		this.index = 0;
		this.ident = 0; // Used for writing blocks nicely.
		this.fractionDigits = 6; // The number of fraction digits when writing floats.
	}

	public void writeKeyframe(final String prefix, final long uInt32Value) {
		writeAttribUInt32(prefix, uInt32Value);
	}

	public void writeKeyframe(final String prefix, final float floatValue) {
		writeFloatAttrib(prefix, floatValue);
	}

	public void writeKeyframe(final String prefix, final float[] floatArrayValues) {
		writeFloatArrayAttrib(prefix, floatArrayValues);
	}

	public void indent() {
		this.ident += 1;
	}

	public void unindent() {
		this.ident -= 1;
	}

	public void startObjectBlock(final String name, final String objectName) {
		this.writeLine(name + " \"" + objectName + "\" {");
		this.ident += 1;
	}

	public void startBlock(final String name, final int blockSize) {
		this.writeLine(name + " " + blockSize + " {" + "");
		this.ident += 1;
	}

	public void startBlock(final String name) {
		this.writeLine(name + " {" + "");
		this.ident += 1;
	}

	public void writeFlag(final String token) {
		this.writeLine(token + ",");
	}

	public void writeFlagUInt32(final long flag) {
		this.writeLine(flag + ",");
	}

	public void writeAttrib(final String string, final int globalSequenceId) {
		writeLine(string + " " + globalSequenceId + ",");
	}

	public void writeAttribUInt32(final String attribName, final long uInt) {
		writeLine(attribName + " " + uInt + ",");
	}

	public void writeAttrib(final String string, final String value) {
		writeLine(string + " " + value + ",");
	}

	public void writeFloatAttrib(final String attribName, final float value) {
		writeLine(attribName + " " + value + ",");
	}

	public void writeStringAttrib(final String attribName, final String value) {
		writeLine(attribName + " \"" + value + "\",");

	}

	public void writeFloatArrayAttrib(final String attribName, final float[] floatArray) {
		this.writeLine(attribName + " { " + formatFloatArray(floatArray) + " },");
	}

	public void writeLongSubArrayAttrib(final String attribName, final long[] array, final int startIndexInclusive,
			final int endIndexExclusive) {
		this.writeLine(attribName + " { " + formatLongSubArray(array, startIndexInclusive, endIndexExclusive) + " },");
	}

	public void writeFloatArray(final float[] floatArray) {
		this.writeLine("{ " + formatFloatArray(floatArray) + " },");
	}

	public void writeShortArray(final short[] shortArray) {
		this.writeLine("{ " + formatShortArray(shortArray) + " },");
	}

	public void writeFloatSubArray(final float[] floatArray, final int startIndexInclusive,
			final int endIndexExclusive) {
		this.writeLine("{ " + formatFloatSubArray(floatArray, startIndexInclusive, endIndexExclusive) + " },");
	}

	public void writeVectorArray(final String token, final float[] vectors, final int vectorLength) {
		this.startBlock(token, vectors.length / vectorLength);

		for (int i = 0, l = vectors.length; i < l; i += vectorLength) {
			this.writeFloatSubArray(vectors, i, i + vectorLength);
		}

		this.endBlock();
	}

	public void endBlock() {
		this.ident -= 1;
		this.writeLine("}");
	}

	public void endBlockComma() {
		this.ident -= 1;
		this.writeLine("},");
	}

	public void writeLine(final String string) {
		try {
			for (int i = 0; i < this.ident; i++) {
				this.buffer.append('\t');
			}
			this.buffer.append(string);
			this.buffer.append('\n');
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void startBlock(final String tokenFaces, final int sizeNumberProbably, final int length) {
		this.writeLine(tokenFaces + " " + sizeNumberProbably + " " + length + " {" + "");
		this.ident += 1;
	}

	public void writeColor(final String tokenStaticColor, final float[] color) {
		this.writeLine(tokenStaticColor + " { " + color[2] + ", " + color[1] + ", " + color[0] + " },");
	}

	public void writeArrayAttrib(final String tokenAlpha, final short[] uint8Array) {
		this.writeLine(tokenAlpha + " { " + formatShortArray(uint8Array) + " },");
	}

	public void writeArrayAttrib(final String tokenAlpha, final int[] uint16Array) {
		this.writeLine(tokenAlpha + " { " + formatIntArray(uint16Array) + " },");
	}

	public void writeArrayAttrib(final String tokenAlpha, final long[] uint32Array) {
		this.writeLine(tokenAlpha + " { " + formatLongArray(uint32Array) + " },");
	}

	private String formatFloat(final float value) {
		final String s = Float.toString(value);
		final String f = String.format("%." + this.fractionDigits + "f", value);
		if (s.length() > f.length()) {
			return f;
		}
		else {
			return s;
		}
	}

	private String formatFloatArray(final float[] value) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0, l = value.length; i < l; i++) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(formatFloat(value[i]));
		}
		return stringBuilder.toString();
	}

	private String formatLongArray(final long[] value) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0, l = value.length; i < l; i++) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(value[i]);
		}
		return stringBuilder.toString();
	}

	private String formatShortArray(final short[] value) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0, l = value.length; i < l; i++) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(value[i]);
		}
		return stringBuilder.toString();
	}

	private String formatIntArray(final int[] value) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0, l = value.length; i < l; i++) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(value[i]);
		}
		return stringBuilder.toString();
	}

	private String formatLongSubArray(final long[] value, final int startIndexInclusive, final int endIndexExclusive) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(value[i]);
		}
		return stringBuilder.toString();
	}

	private String formatFloatSubArray(final float[] value, final int startIndexInclusive,
			final int endIndexExclusive) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(formatFloat(value[i]));
		}
		return stringBuilder.toString();
	}
}
