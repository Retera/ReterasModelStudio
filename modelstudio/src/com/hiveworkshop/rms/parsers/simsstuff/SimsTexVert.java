package com.hiveworkshop.rms.parsers.simsstuff;

import com.hiveworkshop.rms.util.Vec2;

public class SimsTexVert {
	private int index;
	private Vec2 point;

	public SimsTexVert(int index, Vec2 point) {
		this.index = index;
		this.point = point;
	}

	public SimsTexVert(int index, String s) {
		this.index = index;
		String[] split = s.split(" ");
		this.point = new Vec2((double)Float.parseFloat(split[0]), (double)Float.parseFloat(split[1]));
	}

	public SimsTexVert(int index, LineReaderThingi lineReaderThingi) {
		this.index = index;
		this.point = new Vec2(lineReaderThingi.readFloats());
	}

	public Vec2 getPoint() {
		return this.point;
	}

	public SimsTexVert setPoint(Vec2 point) {
		this.point = point;
		return this;
	}

	public int getIndex() {
		return this.index;
	}

	public SimsTexVert setIndex(int index) {
		this.index = index;
		return this;
	}

	public String asString() {
		return this.point.x + " " + this.point.y;
	}
}