package com.hiveworkshop.wc3.gui.modeledit.toolbar;

public interface ToolbarButtonListener<BUTTON_TYPE extends ToolbarButtonType> {
	void typeChanged(BUTTON_TYPE newType);
}
