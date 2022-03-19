package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterDoodadModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalDoodad extends ActionFunction {
	public OpenInternalDoodad(){
		super(TextKey.DOODAD_BROWSER, OpenInternalDoodad::fetchDoodad);
	}

	public static void fetchDoodad() {
		OpenFromInternal.loadSelectorModel(fetchDoodadSelector());
	}

	public static EditableModel fetchDoodadModel() {
		BetterDoodadModelSelector selector = new BetterDoodadModelSelector(DoodadBrowserView.getDoodadData(), new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Doodads",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION) {
			return selector.getSelectedModel();
		}
		return null;
	}

	public static BetterSelector fetchDoodadSelector() {
		BetterDoodadModelSelector selector = new BetterDoodadModelSelector(DoodadBrowserView.getDoodadData(), new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Doodads",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION && selector.getSelection() != null) {
			return selector;
		}
		return null;
	}
}
