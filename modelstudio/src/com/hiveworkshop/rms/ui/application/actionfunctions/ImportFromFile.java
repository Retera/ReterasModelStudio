package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportFromFile extends ActionFunction{
	private static final FileDialog fileDialog = new FileDialog();
	public ImportFromFile(){
		super(TextKey.IMPORT_FROM_FILE, () -> importButtonActionRes(), "control shift I");
	}

	public static void importButtonActionRes() {
		EditableModel model = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
		if (model != null) {
			ImportFileActions.importFile(model);
		}
		if (ProgramGlobals.getCurrentModelPanel() != null) {
//			ProgramGlobals.getCurrentModelPanel().repaintModelTrees();
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}
}
