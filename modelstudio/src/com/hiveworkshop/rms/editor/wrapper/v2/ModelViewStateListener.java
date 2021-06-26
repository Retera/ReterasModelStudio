package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;

public class ModelViewStateListener {

	public ModelViewStateListener() {
	}

	public void repaintComponent() {
		ProgramGlobals.getMainPanel().repaint();
	}

}
