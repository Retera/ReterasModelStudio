package com.hiveworkshop.wc3.gui.modeledit.useractions;

public interface SelectionItem extends SelectionItemView {
	void translate(float x, float y, float z);

	void scale(float x, float y, float z);

	void rotate(float xRadians, float yRadians, float zRadians);

	void delete();
}
