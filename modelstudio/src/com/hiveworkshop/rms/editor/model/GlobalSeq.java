package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class GlobalSeq extends Sequence {

	public GlobalSeq(int length) {
		super(length);
	}

	public GlobalSeq deepCopy(){
		return new GlobalSeq(length);
	}

	@Override
	public int compareTo(Sequence o) {
		if (o instanceof GlobalSeq) {
			return length - o.getLength();
		}
		return -1;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GlobalSeq && ((GlobalSeq) obj).length == length;
	}

	@Override
	public String toString() {
		return "Global Sequence " + length;
	}

	public String getName() {
		return "Global Sequence " + length;
	}
}
