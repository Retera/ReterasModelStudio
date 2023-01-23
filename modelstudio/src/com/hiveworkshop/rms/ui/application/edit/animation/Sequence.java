package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Named;

public abstract class Sequence implements Comparable<Sequence>, Named {
	protected int start;
	protected int length;

	public Sequence(int start, int length) {
		this.start = start;
		this.length = length;
	}

	public Sequence(int length) {
		this.length = length;
		this.start = 0;
	}

	public int getStart() {
		return start;
	}

	public Sequence setStart(int start) {
		this.start = start;
		return this;
	}

	public int getLength() {
		return length;
	}

	public Sequence setLength(int length) {
		this.length = length;
		return this;
	}

	public int getEnd() {
		return start + length;
	}

	public abstract String getName();
	@Override
	public void setName(String text) {
	}

	@Override
	public abstract int compareTo(Sequence o);

	public abstract Sequence deepCopy();
//	@Override
//	public int compareTo(Sequence o) {
//		int startDiff = getStart() - o.getStart();
//		if (startDiff == 0) {
//			return getLength() - o.getLength();
//		}
//		return startDiff;
//	}
}
