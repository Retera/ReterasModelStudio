package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.util.List;

public class CloseModel extends ActionFunction {
	public CloseModel(){
		super(TextKey.CLOSE_CURRENT, () -> closeModelPanel(), "shift ESCAPE");
	}

	public static void closeModelPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.close()) {
			int oldIndex = ProgramGlobals.getModelPanels().indexOf(modelPanel);
			removePanel(modelPanel);
			if (ProgramGlobals.getModelPanels().size() > 0) {
				int newIndex = Math.min(ProgramGlobals.getModelPanels().size() - 1, oldIndex);
				ModelLoader.setCurrentModel(ProgramGlobals.getModelPanels().get(newIndex));
			} else {
				// TODO remove from notifiers to fix leaks
				ModelLoader.setCurrentModel(null);
			}
		}
	}

	public static boolean closeAll() {
		ModelPanel lastUnclosedModelPanel = closeAllModelPanelsExcept(null);
		if (ProgramGlobals.getCurrentModelPanel() == null && lastUnclosedModelPanel != null) {
			ModelLoader.setCurrentModel(lastUnclosedModelPanel);
		}
		return lastUnclosedModelPanel == null;
	}


	public static void closeUnalteredModels() {
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		for (int i = (modelPanels.size() - 2); i >= 0; i--) {
			ModelPanel openModelPanel = modelPanels.get(i);
			if (openModelPanel.getUndoManager().isRedoListEmpty() && openModelPanel.getUndoManager().isUndoListEmpty()) {
//				if (openModelPanel.close()) {
//				}
				removePanel(openModelPanel);
			}
		}
	}

	public static boolean closeOthers(ModelPanel modelPanel) {
		ModelPanel lastUnclosedModelPanel = closeAllModelPanelsExcept(modelPanel);
		if (modelPanel == null && lastUnclosedModelPanel != null) {
			ModelLoader.setCurrentModel(lastUnclosedModelPanel);
		} else {
			ModelLoader.setCurrentModel(modelPanel);
		}
		return lastUnclosedModelPanel == null;
	}

	public static ModelPanel closeAllModelPanelsExcept(ModelPanel modelPanel) {
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		ModelPanel lastUnclosedModelPanel = null;
		for (int i = modelPanels.size() - 1; i >= 0; i--) {
			ModelPanel panel = modelPanels.get(i);
			if (panel != modelPanel) {
				if (panel.close()) {
					removePanel(panel);
				} else {
					lastUnclosedModelPanel = panel;
					break;
				}
			}
		}
		return lastUnclosedModelPanel;
	}

	public static void removePanel(ModelPanel openModelPanel) {
		ProgramGlobals.removeModelPanel(openModelPanel);
	}

	public boolean save(ModelPanel modelPanel) {
		// returns true if closed successfully
		ModelHandler modelHandler = modelPanel.getModelHandler();
		if (!modelHandler.getUndoManager().isUndoListEmpty()) {
			final Object[] options = {"Yes", "No", "Cancel"};
			EditableModel model = modelHandler.getModel();
			final int n = JOptionPane.showOptionDialog(ProgramGlobals.getMainPanel(),
					"Would you like to save " + model.getName()
							+ " (\"" + model.getHeaderName() + "\") " +
							"before closing?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
					options[2]);
			return switch (n) {
				case JOptionPane.YES_OPTION -> File.onClickSaveAs(modelPanel, FileDialog.SAVE);
				case JOptionPane.NO_OPTION -> true;
				default -> false;
			};
		}
		return false;
	}
}
