package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.io.File;

public class Revert extends ActionFunction {
	public Revert(){
		super(TextKey.REVERT, () -> revert());
	}

	public static void revert() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		int oldIndex = ProgramGlobals.getModelPanels().indexOf(modelPanel);
		if (modelPanel != null) {
			if(modelPanel.getModel().getFile() == null || !modelPanel.getModel().getFile().exists()){
//			if(!modelPanel.getModel().getFile().exists()){
				System.out.println("Original file not found...");
			}
			if (modelPanel.close()) {
				ProgramGlobals.removeModelPanel(modelPanel);
				// TODO remove from notifiers to fix leaks
				ModelLoader.setCurrentModel(null);
				File fileToRevert = modelPanel.getModel().getFile();
				ModelLoader.loadFile(fileToRevert);
			}
			// Open any other open model if reopening of the file fails
			if (ProgramGlobals.getCurrentModelPanel() == null && ProgramGlobals.getModelPanels().size() > 0) {
				int newIndex = Math.min(ProgramGlobals.getModelPanels().size() - 1, oldIndex);
				ModelLoader.setCurrentModel(ProgramGlobals.getModelPanels().get(newIndex));
			}
		}
	}
}
