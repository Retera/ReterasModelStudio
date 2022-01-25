package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterDoodadModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalDoodad extends ActionFunction {
	public OpenInternalDoodad(){
		super(TextKey.DOODAD_BROWSER, () -> fetchDoodad());
	}

	public static void fetchDoodad() {
		EditableModel modelFetched = fetchDoodadModel();

		if (modelFetched != null) {
			ModelLoader.loadModel(true, true, ModelLoader.newTempModelPanel(null, modelFetched));
		}
	}

	public static EditableModel fetchDoodadModel() {
		BetterDoodadModelSelector selector = new BetterDoodadModelSelector(DoodadBrowserView.getDoodadData(), new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Object Editor - Select Unit",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if ((x == JOptionPane.OK_OPTION)) {
			return selector.getSelectedModel();
		}
		return null;
	}
}
