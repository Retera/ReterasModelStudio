package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportFromFile extends ActionFunction {
	private static final FileDialog fileDialog = new FileDialog();

	public ImportFromFile() {
		super(TextKey.IMPORT_FROM_FILE, ImportFromFile::importButtonActionRes, "control shift I");
	}

	public static void importButtonActionRes(ModelHandler modelHandler) {
		EditableModel model = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
		if (model != null) {
//			ImportPanel importPanel = new ImportPanel(modelHandler.getModel(), model, true);
			ImportPanelGui importPanel = new ImportPanelGui(modelHandler.getModel(), model);
		}
	}
}
