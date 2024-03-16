package com.hiveworkshop.rms.ui.preferences;

import java.awt.event.MouseEvent;

public enum Nav3DMouseAction {
	CAMERA_SPIN(MouseEvent.BUTTON2_DOWN_MASK, false),
	CAMERA_PAN(MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK, false),
	SELECT(MouseEvent.BUTTON1_DOWN_MASK, false),
	MODIFY(MouseEvent.BUTTON3_DOWN_MASK, false),
	SNAP_TRANSFORM_MODIFIER(MouseEvent.CTRL_DOWN_MASK, true),
	ADD_SELECT_MODIFIER(MouseEvent.SHIFT_DOWN_MASK, true),
	REMOVE_SELECT_MODIFIER(MouseEvent.CTRL_DOWN_MASK, true);

	final Integer mouseEx;
	final boolean modifierSetting;

	Nav3DMouseAction(Integer mouseEx, boolean modifierSetting){
		this.mouseEx = mouseEx;
		this.modifierSetting = modifierSetting;
	}

	public Integer getInternalMouseEx(){
		return mouseEx;
	}

	public boolean isModifierSetting() {
		return modifierSetting;
	}

	public String getTextKeyString() {
		return name();
	}
}
