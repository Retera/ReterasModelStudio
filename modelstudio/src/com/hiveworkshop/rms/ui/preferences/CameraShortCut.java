package com.hiveworkshop.rms.ui.preferences;

import javax.swing.*;
import java.awt.event.KeyEvent;

public enum CameraShortCut {
	FRONT(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0)),
	BACK(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, KeyEvent.CTRL_DOWN_MASK)),
	LEFT(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0)),
	RIGHT(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, KeyEvent.CTRL_DOWN_MASK)),
	TOP(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0)),
	BOTTOM(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, KeyEvent.CTRL_DOWN_MASK)),
	LOC_ZOOM_RESET(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0)),
	ROTATE_LEFT(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0)),
	ROTATE_RIGHT(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0)),
	ROTATE_UP(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0)),
	ROTATE_DOWN(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0)),
	PAN_LEFT(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, KeyEvent.CTRL_DOWN_MASK)),
	PAN_RIGHT(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, KeyEvent.CTRL_DOWN_MASK)),
	PAN_UP(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, KeyEvent.CTRL_DOWN_MASK)),
	PAN_DOWN(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, KeyEvent.CTRL_DOWN_MASK)),
	TOGGLE_ORTHO(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0)),
	FOCUS_SELECTED(KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, 0));

	final KeyStroke keyStroke;

	CameraShortCut(KeyStroke keyStroke){
		this.keyStroke = keyStroke;
	}

	public KeyStroke getInternalKeyStroke(){
		return keyStroke;
	}

	public String getTextKeyString() {
		return name();
	}
}
