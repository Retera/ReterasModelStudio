package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalObject extends ActionFunction {
	public OpenInternalObject(){
		super(TextKey.OBJECT_EDITOR, () -> fetchObject(), "control O");
	}

	public static void fetchObject() {
		MutableGameObject objectFetched = fetchObject1();
		if (objectFetched != null) {
			String filepath = ImportFileActions.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
			String iconPath = objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0);
			InternalFileLoader.loadFromStream(filepath, iconPath);
		}
	}

	public static MutableGameObject fetchObject1() {
		BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(UnitBrowserView.getUnitData(), new UnitEditorSettings());
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Object Editor - Select Unit",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if ((x == JOptionPane.OK_OPTION)) {
			return selector.getSelection();
		}

		return null;
	}
}
