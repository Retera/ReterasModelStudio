package com.hiveworkshop.rms.parsers.simsstuff;

public class LineReaderThingi {
	String[] lines;
	int currentIndex = 0;

	public LineReaderThingi(String s) {
		this.lines = s.split("\n");
	}

	public LineReaderThingi(String[] lines) {
		this.lines = lines;
	}

	public String readString() {
		return this.lines[this.currentIndex++];
	}

	public String[] readStrings() {
		return this.lines[this.currentIndex++].split(" ");
	}

	public int readInt() {
		String string = this.readString();
		return Integer.parseInt(string);
	}

	public int[] readInts() {
		String[] strings = this.readStrings();
		int[] ints = new int[strings.length];

		for(int i = 0; i < strings.length; ++i) {
			ints[i] = Integer.parseInt(strings[i]);
		}

		return ints;
	}

	public float[] readFloats() {
		String[] strings = this.readStrings();
		float[] floats = new float[strings.length];

		for(int i = 0; i < strings.length; ++i) {
			floats[i] = Float.parseFloat(strings[i]);
		}

		return floats;
	}
}
