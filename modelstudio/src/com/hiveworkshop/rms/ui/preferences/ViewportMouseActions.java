package com.hiveworkshop.rms.ui.preferences;

import java.awt.event.MouseEvent;

public enum ViewportMouseActions {
	ROTATE(MouseEvent.BUTTON2_DOWN_MASK),
	PAN(MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK),

	SELECT(MouseEvent.BUTTON1_DOWN_MASK),
	SELECT_ADD_MOD(MouseEvent.SHIFT_DOWN_MASK),
	SELECT_REMOVE_MOD(MouseEvent.CTRL_DOWN_MASK),

	MANIPULATE(MouseEvent.BUTTON3_DOWN_MASK),
	MANIPULATE_SNAP_MOD(MouseEvent.CTRL_DOWN_MASK),
	MANIPULATE_AXIS_LOC_MOD(MouseEvent.ALT_DOWN_MASK),
	MANIPULATE_PRECISION_MOD(MouseEvent.SHIFT_DOWN_MASK),
	;
	int modifiersEx;
	ViewportMouseActions(int modifiersEx){
		this.modifiersEx = modifiersEx;
	}

	public int getModifiersEx(){
		return modifiersEx;
	}

	public String getModifiersExText(){
		return MouseEvent.getModifiersExText(modifiersEx);
	}

	public String getTextKeyString() {
		return name();
	}
}
