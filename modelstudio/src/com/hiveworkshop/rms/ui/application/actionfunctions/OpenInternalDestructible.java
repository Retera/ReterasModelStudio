package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterDestructibleModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DestructibleBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalDestructible extends ActionFunction {
	public OpenInternalDestructible(){
		super(TextKey.DESTRUCTIBLE_BROWSER, () -> fetchDoodad());
	}

	public static void fetchDoodad() {
		EditableModel modelFetched = fetchDestructibleModel();
		if (modelFetched != null) {
			ModelLoader.loadModel(true, true, ModelLoader.newTempModelPanel(null, modelFetched));
		}
	}

	public static EditableModel fetchDestructibleModel() {
		BetterDestructibleModelSelector selector = new BetterDestructibleModelSelector(DestructibleBrowserView.getDestructibleData(), new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Object Editor - Select Unit",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if ((x == JOptionPane.OK_OPTION)) {
			return selector.getSelectedModel();
		}
		return null;
	}
}
