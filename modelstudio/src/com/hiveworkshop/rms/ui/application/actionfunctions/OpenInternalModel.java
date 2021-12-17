package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.awt.*;

public class OpenInternalModel extends ActionFunction {
	public OpenInternalModel(){
		super(TextKey.MODEL, () -> fetchModel(), "control M");
	}



	public static void fetchModel() {
		ModelOptionPane.ModelElement model = ModelOptionPane.fetchModelElement(ProgramGlobals.getMainPanel());
		if (model != null) {

			String filepath = ImportFileActions.convertPathToMDX(model.getFilepath());
			ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST)) : ModelLoader.MDLIcon;

			InternalFileLoader.loadFromStream(filepath, icon);
		}
	}
}
