package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

public class OpenInternalModel extends ActionFunction {
	public OpenInternalModel(){
		super(TextKey.MODEL, () -> fetchModel(), "control M");
	}

	public static void fetchModel() {
		ModelOptionPanel uop = ModelOptionPanel.getModelOptionPanel(ProgramGlobals.getMainPanel());
		if (uop != null) {
			String filepath = ImportFileActions.convertPathToMDX(uop.getSelection());
			InternalFileLoader.loadFromStream(filepath, uop.getCachedIconPath());
		}
	}
}
