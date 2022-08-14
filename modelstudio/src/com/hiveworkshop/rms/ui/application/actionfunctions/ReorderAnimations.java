package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.tools.ReorderAnimationsPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.FramePopup;

public class ReorderAnimations extends ActionFunction {
	public ReorderAnimations() {
		super(TextKey.REORDER_ANIMATIONS, ReorderAnimations::showPanel);
	}

	private static void showPanel(ModelHandler modelHandler) {
		ReorderAnimationsPanel panel = new ReorderAnimationsPanel(modelHandler);
		FramePopup.show(panel, ProgramGlobals.getMainPanel(), "Re-order Animations");
	}
}
