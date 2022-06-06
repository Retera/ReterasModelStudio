package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.parsers.slk.WarcraftObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;


public class BetterUnitEditorModelSelector extends BetterSelector {

	public BetterUnitEditorModelSelector(UnitEditorSettings unitEditorSettings) {
		super(new UnitTabTreeBrowserBuilder(), unitEditorSettings, "umdl", null);
	}

	protected JPanel getRightPanel() {
		JPanel rightPanel = new JPanel(new MigLayout("fill, ins 0", "", ""));
		rightPanel.add(perspDisplayPanel, "growx, growy");
		return rightPanel;
	}

	protected void loadUnitPreview() {
		String filepath = currentUnit.getFieldAsString(UnitFields.MODEL_FILE, 0);
		String gameObjectName = currentUnit.getName();

		openModel(filepath, gameObjectName);
	}

	public void loadRaceData(final DefaultMutableTreeNode folder, final RaceData data) {
		addDataToFolder(folder, "WESTRING_UNITS", data.units);
		addDataToFolder(folder, "WESTRING_UTYPE_BUILDINGS", data.buildings);
		addDataToFolder(folder, "WESTRING_UTYPE_HEROES", data.heroes);
		addDataToFolder(folder, "WESTRING_UTYPE_SPECIAL", data.special);
	}

	private void addDataToFolder(DefaultMutableTreeNode folder, String weType, List<WarcraftObject> objects) {
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(WEString.getString(weType));
		for (final WarcraftObject u : objects) {
			node.add(new DefaultMutableTreeNode(u));
		}
		if (objects.size() > 0) {
			folder.add(node);
			if (defaultSelection == null) {
				defaultSelection = node.getFirstLeaf();
			}
		}
	}

	public String getCurrentFilePath() {
		if(currentUnit != null){
			return getFilePath(currentUnit, 0);
		} else {
			return null;
		}
	}
}
