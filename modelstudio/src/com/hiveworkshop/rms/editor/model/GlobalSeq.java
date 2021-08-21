package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundProvider;

public class GlobalSeq implements TimeBoundProvider{
	int length = 0;

	public GlobalSeq(int length){
		this.length = length;
	}
	@Override
	public int getStart() {
		return 0;
	}

	@Override
	public int getEnd() {
		return length;
	}

	public int getLength() {
		return length;
	}

	@Override
	public int compareTo(TimeBoundProvider o) {
		if(o instanceof GlobalSeq){
			return length - o.getEnd();
		}
		return -1;
	}
}
