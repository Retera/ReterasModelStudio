package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.TempStuffFromEditableModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ImportFromWorkspace extends ActionFunction {
	public ImportFromWorkspace() {
		super(TextKey.IMPORT_FROM_WORKSPACE, ImportFromWorkspace::importFromWorkspaceActionRes);
	}


	public static void importFromWorkspaceActionRes(ModelHandler modelHandler) {
		EditableModel choice = getWorkspaceModel();
		if (choice != null) {
			EditableModel donatingModel = TempStuffFromEditableModel.deepClone(choice, choice.getHeaderName());
			ImportPanelGui importPanel = new ImportPanelGui(modelHandler.getModel(), donatingModel);
		}
	}

	private static EditableModel getWorkspaceModel() {
		List<EditableModel> optionNames = new ArrayList<>();
		for (ModelPanel modelPanel : ProgramGlobals.getModelPanels()) {
			EditableModel model = modelPanel.getModel();
			optionNames.add(model);
		}
		return (EditableModel) JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Choose a workspace item to import data from:", "Import from Workspace",
				JOptionPane.PLAIN_MESSAGE, null, optionNames.toArray(), optionNames.get(0));
	}
}
