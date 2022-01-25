package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterDestructibleModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterDoodadModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DestructibleBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenFromInternal extends ActionFunction {
	public OpenFromInternal(){
		super(TextKey.INTERNAL_BROWSER, OpenFromInternal::loadInternalModel);
	}

	public static MutableGameObject getSelectedObject() {
		BetterSelector selector = getSelector();

		if (selector != null) {
			return selector.getSelection();
		}

		return null;
	}

	public static void loadInternalModel() {
		BetterSelector selector = getSelector();
		if (selector != null && selector.getSelection() != null) {
			InternalFileLoader.loadMdxStream(selector.getSelection(), selector.getCurrentFilePath(), true);
		}
	}

	public static EditableModel getInternalModel() {
		BetterSelector selector = getSelector();
		if (selector != null) {
			return selector.getSelectedModel();
		}

		return null;
	}

	public static BetterSelector getSelector() {
		JTabbedPane tabbedPanel = new JTabbedPane();
		tabbedPanel.add("Unit", new BetterUnitEditorModelSelector(UnitBrowserView.getUnitData(), new UnitEditorSettings()));
		tabbedPanel.add("Doodad", new BetterDestructibleModelSelector(DestructibleBrowserView.getDestructibleData(), new UnitEditorSettings()));
		tabbedPanel.add("Destructible", new BetterDoodadModelSelector(DoodadBrowserView.getDoodadData(), new UnitEditorSettings()));

		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), tabbedPanel, "Object Editor - Select Unit",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION) {
			return (BetterSelector) tabbedPanel.getSelectedComponent();
		}
		return null;
	}
}
