package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportWC3Model extends ActionFunction{
	public ImportWC3Model(){
		super(TextKey.IMPORT_FROM_WC3_MODEL, ImportWC3Model::importGameModelActionRes);
	}

	public static void importGameModelActionRes(ModelHandler modelHandler){
		EditableModel animationSource = fetchModel();
		if (animationSource != null) {
			ImportPanelGui importPanel = new ImportPanelGui(modelHandler.getModel(), animationSource);
		}
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}

	public static EditableModel fetchModel() {
		ModelOptionPanel uop = ModelOptionPanel.getModelOptionPanel(ProgramGlobals.getMainPanel());
		if (uop != null) {
			return uop.getSelectedModel();
		}
		return null;
	}
}
