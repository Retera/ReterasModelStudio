package com.hiveworkshop.wc3.gui.animedit;

public interface TimeBoundProvider {
	int getStart();

	int getEnd();

	void addChangeListener(TimeBoundChangeListener listener);
}
