package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

public class OpenInternalModel extends ActionFunction {
	public OpenInternalModel(){
		super(TextKey.MODEL, OpenInternalModel::fetchModel, "control M");
	}

	public static void fetchModel() {
		ModelOptionPanel uop = ModelOptionPanel.getModelOptionPanel(ProgramGlobals.getMainPanel());
		if (uop != null) {
			InternalFileLoader.loadFromStream(uop.getSelection(), uop.getIconForSelected(), true);
		}
	}
}
