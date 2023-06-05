package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelFromFile;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportFromFile extends ActionFunction {

	public ImportFromFile() {
		super(TextKey.IMPORT_FROM_FILE, ImportFromFile::importButtonActionRes, "control shift I");
	}

	public static void importButtonActionRes(ModelHandler modelHandler) {
		EditableModel model = ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, null);
		if (model != null) {
			new ImportPanelGui(modelHandler.getModel(), model, ModelLoader::loadModel);
		}
	}
}
