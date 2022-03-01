package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ModelLoader;
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
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.awt.*;

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
		loadSelectorModel(getSelector());
	}

	public static void loadSelectorModel(BetterSelector selector) {
		if (selector != null) {
			EditableModel modelFetched = selector.getSelectedModel();
			ImageIcon icon = new ImageIcon(IconUtils
					.getIcon(selector.getSelection())
					.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
			ModelLoader.loadModel(true, true, new ModelPanel(new ModelHandler(modelFetched, icon)));
			if (ProgramGlobals.getPrefs().isLoadPortraits()) {
				String portrait = ModelUtils.getPortrait(selector.getCurrentFilePath());
				InternalFileLoader.loadFilepathMdx(portrait, true, false, icon, true);
			}
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
		tabbedPanel.add("Doodad", new BetterDoodadModelSelector(DoodadBrowserView.getDoodadData(), new UnitEditorSettings()));
		tabbedPanel.add("Destructible", new BetterDestructibleModelSelector(DestructibleBrowserView.getDestructibleData(), new UnitEditorSettings()));

		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), tabbedPanel, "Internal Models",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (x == JOptionPane.OK_OPTION) {
			return (BetterSelector) tabbedPanel.getSelectedComponent();
		}
		return null;
	}
}
