package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

public interface ToolbarButtonListener<BUTTON_TYPE extends ToolbarButtonType> {
	void typeChanged(BUTTON_TYPE newType);
}
