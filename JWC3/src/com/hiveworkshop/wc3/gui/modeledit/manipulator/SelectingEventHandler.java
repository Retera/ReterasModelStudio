package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;

public interface SelectingEventHandler {
	void setSelectedRegion(Rectangle region, CoordinateSystem coordinateSystem);

	void removeSelectedRegion(Rectangle region, CoordinateSystem coordinateSystem);

	void addSelectedRegion(Rectangle region, CoordinateSystem coordinateSystem);

	void expandSelection();

	void invertSelection();

	void selectAll();
}
