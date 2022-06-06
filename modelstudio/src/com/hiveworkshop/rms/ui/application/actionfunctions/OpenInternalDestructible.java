package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterDestructibleModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalDestructible extends ActionFunction {
	public OpenInternalDestructible(){
		super(TextKey.DESTRUCTIBLE_BROWSER, OpenInternalDestructible::fetchDestructible);
	}

	public static void fetchDestructible() {
		OpenFromInternal.loadSelectorModel(fetchDestructibleSelector());
	}

	public static EditableModel fetchDestructibleModel() {
		BetterDestructibleModelSelector selector = new BetterDestructibleModelSelector(new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Destructibles",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION) {
			return selector.getSelectedModel();
		}
		return null;
	}

	public static BetterSelector fetchDestructibleSelector() {
		BetterDestructibleModelSelector selector = new BetterDestructibleModelSelector(new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Destructibles",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION && selector.getSelection() != null) {
			return selector;
		}
		return null;
	}
}
