package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
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
			ProgramGlobals.removeModelPanel(modelPanel);
			MenuBar.removeModelPanel(modelPanel);
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
		boolean success = true;
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		ModelPanel lastUnclosedModelPanel = null;
		for (int i = modelPanels.size() - 1; i >= 0; i--) {
			ModelPanel panel = modelPanels.get(i);
			if (success = panel.close()) {
				MenuBar.removeModelPanel(panel);
				ProgramGlobals.removeModelPanel(panel);
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (ProgramGlobals.getCurrentModelPanel() == null && lastUnclosedModelPanel != null) {
			ModelLoader.setCurrentModel(lastUnclosedModelPanel);
		}
		return success;
	}


	public static void closeUnalteredModels() {
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		for (int i = (modelPanels.size() - 2); i >= 0; i--) {
			ModelPanel openModelPanel = modelPanels.get(i);
			if (openModelPanel.getUndoManager().isRedoListEmpty() && openModelPanel.getUndoManager().isUndoListEmpty()) {
//				if (openModelPanel.close()) {
//				}
				ProgramGlobals.removeModelPanel(openModelPanel);
				MenuBar.removeModelPanel(openModelPanel);
			}
		}
	}
//	}

	public static boolean closeOthers(ModelPanel modelPanel) {
		boolean success = true;
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		ModelPanel lastUnclosedModelPanel = null;
		for (int i = modelPanels.size() - 1; i > 0; i--) {
			ModelPanel panel = modelPanels.get(i);
			if(panel != modelPanel){
				if (panel.close()) {
					MenuBar.removeModelPanel(panel);
					ProgramGlobals.removeModelPanel(panel);
				} else {
					lastUnclosedModelPanel = panel;
					success = false;
					break;
				}
			}
		}
		if (modelPanel == null && lastUnclosedModelPanel != null) {
			ModelLoader.setCurrentModel(lastUnclosedModelPanel);
		} else {
			ModelLoader.setCurrentModel(modelPanel);
		}
		return success;
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
				case JOptionPane.YES_OPTION -> {
					FileDialog fileDialog = new FileDialog(modelPanel);
					yield fileDialog.onClickSaveAs();
				}
				case JOptionPane.NO_OPTION -> true;
				default -> false;
			};
		}
		return false;
	}
}
