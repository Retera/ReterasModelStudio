package com.hiveworkshop.rms.parsers.mdlx.mdl;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class MdlTokenInputStream {
	private final ByteBuffer buffer;
	private int index;
	private int line = 0;
	private String lastComment;

	public MdlTokenInputStream(final ByteBuffer buffer) {
		this.buffer = buffer;
		index = 0;
	}

	public String read() {
//		return read1();
//		return read2();
		return read3();
//		return read4();
//		return read5();
	}

	public String read1() {
		boolean inComment = false;
		boolean inString = false;
		final StringBuilder token = new StringBuilder();
		final StringBuilder comment = new StringBuilder();
		final int length = buffer.remaining();

		while (index < length) {
			// Note: cast from 'byte' to 'char' will cause Java incompatibility with Chinese and Russian/Cyrillic and others
			final char c = (char) buffer.get(buffer.position() + index++);

			if (inComment) {
				if (c == '\n') {
					printToke(token, line);
					inComment = false;
					line++;
					return token.toString();
				} else {
					token.append(c);
				}
			}
			else if (inString) {
				if (c == '"') {
					printToke(token, line);
					return token.toString();
				}
				else {
					token.append(c);
				}
			}
			else if ((c == ' ') || (c == ',') || (c == '\t') || (c == '\n') || (c == ':') || (c == '\r')) {
				if (c == '\n') {
					line++;
				}
				if (0 < token.length()) {
					printToke(token, line);
					return token.toString();
				}
			} else if (c == '{' || c == '}') {
				printToke(token, line);
				if (0 < token.length()) {
					index--;
					return token.toString();
				} else {
					return Character.toString(c);
				}
			} else if ((c == '/') && (buffer.get(buffer.position() + index) == '/')) {
				if (0 < token.length()) {
					index--;
					printToke(token, line);
					return token.toString();
				} else {
					inComment = true;
					token.append(c);
				}
			} else if (c == '"') {
				if (0 < token.length()) {
					printToke(token, line);
					index--;
					return token.toString();
				} else {
					inString = true;
				}
			} else {
				token.append(c);
			}
		}
		return null;
	}

	private void printToke(StringBuilder token, int line) {
		if(759 <= line && line <= 761 || 18870 <= line && line <= 18900  || 251067 <= line && line <= 251070 ){
			System.out.println(line + " current token: " + token);
		}
	}

	public String read2() {
		boolean inComment = false;
		boolean inString = false;
		final StringBuilder token = new StringBuilder();
		final StringBuilder comment = new StringBuilder();
		final int length = buffer.remaining();

		while (index < length) {
			// Note: cast from 'byte' to 'char' will cause Java incompatibility with Chinese and Russian/Cyrillic and others
			final char c = (char) buffer.get(buffer.position() + index++);

			if (c == '\n') {
				line++;
			}

			if(c == '"'){
				if (0 < token.length()) {
					index--;
					printToke(token, line);
					return token.toString();
				} else if (inString) {
					printToke(token, line);
					return token.toString();
				} else {
					inString = true;
				}
			} else if (c == '{' || c == '}') {
				if (0 < token.length()) {
					index--;
					printToke(token, line);
					return token.toString();
				} else {
					comment.append(c);
					printToke(comment, line);
					return Character.toString(c);
				}
			} else if ((c == '/') && (buffer.get(buffer.position() + index) == '/')) {
				if (0 < token.length()) {
					index--;
					printToke(token, line);
					return token.toString();
				} else {
					inComment = true;
					token.append(c);
				}
			}
			else if (!inString && !inComment && (c == ' ' || c == ',' || c == ':' || c == '\t' || c == '\r') || !inString && c == '\n') {
				if (0 < token.length()) {
					printToke(token, line);
					return token.toString();
				}
			} else {
				token.append(c);
			}

		}
		return null;
	}
	public String read3() {
		final StringBuilder token = new StringBuilder();
		final int length = buffer.remaining();

		while (index < length) {
			// Note: cast from 'byte' to 'char' will cause Java incompatibility with Chinese and Russian/Cyrillic and others
			final char c = (char) buffer.get(buffer.position() + index++);

			if (c == '\n') {
				line++;
			}

			if(c == '"'){
				if (0 < token.length()) {
					index--;
				} else {
//					char sc = (char) buffer.get(buffer.position() + index++);
					char sc;
					while (index < length && (sc = (char) buffer.get(buffer.position() + index++)) != '"'){
						token.append(sc);
					}

				}
				printToke(token, line);
				return token.toString();
			} else if (c == '{' || c == '}') {
				if (0 < token.length()) {
					index--;
					printToke(token, line);
					return token.toString();
				} else {
					final StringBuilder comment = new StringBuilder();
					comment.append(c);
					printToke(comment, line);
					return Character.toString(c);
				}
			} else if (c == '/' && buffer.get(buffer.position() + index) == '/') {
				if (0 < token.length()) {
					index--;
					printToke(token, line);
					return token.toString();
				} else {
					final StringBuilder comment = new StringBuilder();
					comment.append(c);
//					char cc = (char) buffer.get(buffer.position() + index++);
					char cc = ' ';
					while (index < length && (cc = (char) buffer.get(buffer.position() + index++)) != '\n'){
						comment.append(cc);
					}
					if(cc == '\n'){
						line++;
					}
					lastComment = comment.toString();
					printToke(comment, line);
//					return comment.toString();
				}
			} else if (c == ' ' || c == ',' || c == ':' || c == '\t' || c == '\r' || c == '\n') {
				if (0 < token.length()) {
					printToke(token, line);
					return token.toString();
				}
			} else {
				token.append(c);
			}

		}
		return null;
	}
	public String read4() {
		final StringBuilder token = new StringBuilder();
		final int length = buffer.remaining();

		while (index < length) {
			// Note: cast from 'byte' to 'char' will cause Java incompatibility with Chinese and Russian/Cyrillic and others
			final char c = (char) buffer.get(buffer.position() + index++);

			if (c == '\n') {
				line++;
			}


			if (c == '"' && 0 < token.length()) {
				index--;
				return token.toString();
			} else if (c == '"') {
				char sc = (char) buffer.get(buffer.position() + index++);
				while (index < length && sc != '"'){
					token.append(sc);
				}
				return token.toString();

			} else if ((c == '{' || c == '}') && 0 < token.length()) {
				index--;
				return token.toString();
			} else if (c == '{' || c == '}') {
				return Character.toString(c);
			} else if (c == '/' && buffer.get(buffer.position() + index) == '/' && 0 < token.length()) {
				index--;
				return token.toString();
			} else if (c == '/' && buffer.get(buffer.position() + index) == '/') {
				final StringBuilder comment = new StringBuilder();
				comment.append(c);
				char cc = (char) buffer.get(buffer.position() + index++);
				while (index < length && cc != '\n'){
					comment.append(cc);
					index++;
				}
				lastComment = comment.toString();
					return comment.toString();
			} else if (c == ' ' || c == ',' || c == ':' || c == '\t' || c == '\r' || c == '\n') {
				if (0 < token.length()) {
					return token.toString();
				}
			} else {
				token.append(c);
			}
		}
		return null;
	}

	public String readValue(){
		String value;
		while ((value = read()) != null){
			if (!value.startsWith("//")){
				return value;
			}
		}
		return null;
	}
	public String peek() {
		final int index = this.index;
		final int line = this.line;
		final String value = readValue();

		this.index = index;
		this.line = line;
		return value;
	}

	public long readUInt32() {
		return Long.parseLong(readValue());
	}

	public int readInt() {
		return Integer.parseInt(readValue());
	}

	public float readFloat() {
		return Float.parseFloat(readValue());
	}

	public void readIntArray(final long[] values) {
		skipToken("{"); // {

		for (int i = 0, l = values.length; i < l; i++) {
			values[i] = readInt();
		}

		skipToken("}"); // }
	}

	public float[] readFloatArray(final float[] values) {
		skipToken("{"); // {

		for (int i = 0, l = values.length; i < l; i++) {
			values[i] = readFloat();
		}

		skipToken("}"); // }
		return values;
	}

	/**
	 * Read an MDL keyframe value. If the value is a scalar, it is just the number.
	 * If the value is a vector, it is enclosed with curly braces.
	 *
	 * @param values {Float32Array|Uint32Array}
	 */
	public void readKeyframe(final float[] values) {
		if (values.length == 1) {
			values[0] = readFloat();
		}
		else {
			readFloatArray(values);
		}
	}

	public float[] readVectorArray(final float[] array, final int vectorLength) {
		skipToken("{"); // {

		for (int i = 0, l = array.length / vectorLength; i < l; i++) {
			skipToken("{");; // {

			for (int j = 0; j < vectorLength; j++) {
				array[(i * vectorLength) + j] = readFloat();
			}

			skipToken("}"); // }
		}

		skipToken("}"); // }
		return array;
	}

	public Iterable<String> readBlock() {
		read(); // {
		return () -> new Iterator<>() {
			String current;
			private boolean hasLoaded = false;

			@Override
			public String next() {
				if (!hasLoaded) {
					hasNext();
				}
				hasLoaded = false;
				return current;
			}

			@Override
			public boolean hasNext() {
				current = read();
				hasLoaded = true;
				return (current != null) && !current.equals("}");
			}
		};
	}

	public int[] readUInt16Array(final int[] values) {
		return readUInt16Array(values, values.length);
	}

	public int[] readUInt16Array(final int[] values, final int vectorLength) {
//		read(); // {
		skipToken("{");
//		for (int i = 0, l = values.length; i < l; i++) {
//			values[i] = readInt();
//		}
		for (int i = 0; i < values.length; i += vectorLength) {
			skipToken("{");
			for (int j = 0; j < vectorLength; j++) {
				values[i + j] = readInt();
			}
			skipToken("}");
		}
		skipToken("}");
//		read(); // }

		return values;
	}

	public short[] readUInt8Array(final short[] values) {
		return readUInt8Array(values, values.length);
	}

	public short[] readUInt8Array(final short[] values, final int vectorLength) {
		skipToken("{"); // {

//		for (int i = 0, l = values.length; i < l; i++) {
//			values[i] = Short.parseShort(read());
//		}

		for (int i = 0; i < values.length; i += vectorLength) {
			skipToken("{");
			for (int j = 0; j < vectorLength; j++) {
				values[i + j] = Short.parseShort(read());
//				String ugg = read();
//				values[i+j] = Short.parseShort(ugg);
//				System.out.println("read(): " + ugg + " i: " + i);
			}
			skipToken("}");
		}
		skipToken("}");
//		read(); // }

		return values;
	}

	private void skipToken(String token) {
		int index = this.index;
		int line = this.line;
		String peek = readValue();
		if (!peek.equals(token)) {
			System.out.println(line + " did not skip, " + peek + " != " + token);
			this.index = index;
			this.line = line;
		} else {

			System.out.println(line + " skipped, " + peek + " == " + token);
		}
	}

	public void readColor(final float[] color) {
		skipToken("{");; // {

		color[2] = readFloat();
		color[1] = readFloat();
		color[0] = readFloat();

		skipToken("}"); // }
	}

	public int getLineNumber() {
		return line;
	}
}
