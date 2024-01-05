package com.hiveworkshop.rms.ui.preferences;

import java.awt.event.MouseEvent;

public enum Nav3DMouseAction {
	CAMERA_SPIN(MouseEvent.BUTTON2_DOWN_MASK),
	CAMERA_PAN(MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK),
	SELECT(MouseEvent.BUTTON1_DOWN_MASK),
	MODIFY(MouseEvent.BUTTON3_DOWN_MASK),
	SNAP_TRANSFORM_MODIFIER(MouseEvent.CTRL_DOWN_MASK),
	ADD_SELECT_MODIFIER(MouseEvent.SHIFT_DOWN_MASK),
	REMOVE_SELECT_MODIFIER(MouseEvent.CTRL_DOWN_MASK);

	final Integer mouseEx;

	Nav3DMouseAction(Integer mouseEx){
		this.mouseEx = mouseEx;
	}

	public Integer getInternalMouseEx(){
		return mouseEx;
	}

	public String getTextKeyString() {
		return name();
	}
}
