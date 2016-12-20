package com.hiveworkshop.wc3.gui.modeledit.selection;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;

public interface SelectionItem extends SelectionItemView {
	void translate(float x, float y, CoordinateSystem coordinateSystem);

	void scale(float centerX, float centerY, float x, float y, CoordinateSystem coordinateSystem);

	void rotate(float centerX, float centerY, float radians, CoordinateSystem coordinateSystem);

	void delete();

}
