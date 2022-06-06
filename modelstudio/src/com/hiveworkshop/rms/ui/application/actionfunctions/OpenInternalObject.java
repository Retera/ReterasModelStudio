package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalObject extends ActionFunction {
	public OpenInternalObject(){
		super(TextKey.OBJECT_EDITOR, OpenInternalObject::fetchObject, "control O");
	}

	public static void fetchObject() {
		OpenFromInternal.loadSelectorModel(fetchObjectSelector());
	}

	public static EditableModel getInternalModel() {
		BetterSelector selector = fetchObjectSelector();
		if (selector != null) {
			return selector.getSelectedModel();
		}

		return null;
	}

	public static BetterSelector fetchObjectSelector() {
		BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(new UnitEditorSettings());
		// Old title: "Object Editor - Select Unit"
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Units",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION && selector.getSelection() != null) {
			return selector;
		}

		return null;
	}
}
