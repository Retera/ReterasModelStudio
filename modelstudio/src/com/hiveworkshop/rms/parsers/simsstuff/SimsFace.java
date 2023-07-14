package com.hiveworkshop.rms.parsers.simsstuff;

public class SimsFace {
	private int vert0;
	private int vert1;
	private int vert2;

	public SimsFace(int vert0, int vert1, int vert2) {
		this.vert0 = vert0;
		this.vert1 = vert1;
		this.vert2 = vert2;
	}

	public SimsFace(String s) {
		String[] split = s.split(" ");
		this.vert0 = Integer.parseInt(split[0]);
		this.vert1 = Integer.parseInt(split[1]);
		this.vert2 = Integer.parseInt(split[2]);
	}

	public SimsFace(LineReaderThingi lineReaderThingi) {
		int[] ints = lineReaderThingi.readInts();
		this.vert0 = ints[0];
		this.vert1 = ints[1];
		this.vert2 = ints[2];
	}

	public int getVert0() {
		return this.vert0;
	}

	public SimsFace setVert0(int vert0) {
		this.vert0 = vert0;
		return this;
	}

	public int getVert1() {
		return this.vert1;
	}

	public SimsFace setVert1(int vert1) {
		this.vert1 = vert1;
		return this;
	}

	public int getVert2() {
		return this.vert2;
	}

	public SimsFace setVert2(int vert2) {
		this.vert2 = vert2;
		return this;
	}

	public String asString() {
		return this.vert0 + " " + this.vert1 + " " + this.vert2;
	}
}
