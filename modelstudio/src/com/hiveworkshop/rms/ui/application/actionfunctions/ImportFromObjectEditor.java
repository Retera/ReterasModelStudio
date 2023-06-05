package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class ImportFromObjectEditor extends ActionFunction{
	public ImportFromObjectEditor(){
		super(TextKey.IMPORT_FROM_OBJECT_EDITOR, ImportFromObjectEditor::importGameObjectActionRes);
	}

	public static void importGameObjectActionRes(ModelHandler modelHandler){
		EditableModel animationSource = fetchObjectModel();
		if (animationSource != null) {
			new ImportPanelGui(modelHandler.getModel(), animationSource, ModelLoader::loadModel);
		}
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}

	public static EditableModel fetchObjectModel() {
		BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Select Unit",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION) {
			return selector.getSelectedModel();
		}

		return null;
	}
}
