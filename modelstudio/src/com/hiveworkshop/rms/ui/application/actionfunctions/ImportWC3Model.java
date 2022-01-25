package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportWC3Model extends ActionFunction{
	public ImportWC3Model(){
		super(TextKey.IMPORT_FROM_WC3_MODEL, () -> importGameModelActionRes());
	}

	public static void importGameModelActionRes(){
		ImportFileActions.importMdxObject(fetchModelPath());
	}

	public static String fetchModelPath() {
		ModelOptionPanel uop = ModelOptionPanel.getModelOptionPanel(ProgramGlobals.getMainPanel());
		if (uop != null) {
			return uop.getSelection();
		}
		return null;
	}
}
