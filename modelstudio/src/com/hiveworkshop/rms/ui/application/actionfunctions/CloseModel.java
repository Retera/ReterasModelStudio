package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

public class CloseModel extends ActionFunction {
	public CloseModel(){
		super(TextKey.CLOSE, () -> closeModelPanel(), "control E");
	}

	public static void closeModelPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		int oldIndex = ProgramGlobals.getModelPanels().indexOf(modelPanel);
		if (modelPanel != null) {
			if (modelPanel.close()) {
				ProgramGlobals.removeModelPanel(modelPanel);
				com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar.removeModelPanel(modelPanel);
				if (ProgramGlobals.getModelPanels().size() > 0) {
					int newIndex = Math.min(ProgramGlobals.getModelPanels().size() - 1, oldIndex);
					ModelLoader.setCurrentModel(ProgramGlobals.getModelPanels().get(newIndex));
				} else {
					// TODO remove from notifiers to fix leaks
					ModelLoader.setCurrentModel(null);
				}
			}
		}
	}
}
