package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.AbstractWorldEditorPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public final class ObjectEditorPanel extends AbstractWorldEditorPanel {
	private static final War3ID UNIT_NAME = War3ID.fromString("unam");

	private final List<UnitEditorPanel> editors = new ArrayList<>();
	private final JTabbedPane tabbedPane;
//	private MutableObjectData unitData;


	public ObjectEditorPanel() {
		tabbedPane = new JTabbedPane() {
			@Override
			public void addTab(final String title, final Icon icon, final Component component) {
				super.addTab(title, icon, component);
				editors.add((UnitEditorPanel) component);
			}
		};
		final DataTable worldEditorData = DataTableHolder.getWorldEditorData();
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_UNITS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewUnit"), createUnitEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_ITEMS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewItem"), createItemEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_DESTRUCTABLES"), getIcon(worldEditorData, "ToolBarIcon_OE_NewDest"), createDestructibleEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_DOODADS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewDood"), createDoodadEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_ABILITIES"), getIcon(worldEditorData, "ToolBarIcon_OE_NewAbil"), createAbilityEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_BUFFS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewBuff"), createAbilityBuffEditor());
		System.out.println("nextPanel : UpgradePanel!");
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_UPGRADES"), getIcon(worldEditorData, "ToolBarIcon_OE_NewUpgr"), createUpgradeEditor());
		System.out.println("UpgradePanel done!");

		tabbedPane.addTab("Terrain", getIcon(worldEditorData, "ToolBarIcon_Module_Terrain"), createUpgradeEditor());
		tabbedPane.addTab("Lighting Effects", new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.getGameTex("ReplaceableTextures\\CommandButtons\\BTNChainLightning.blp"))), createUpgradeEditor());
		tabbedPane.addTab("Weather", new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.getGameTex("ReplaceableTextures\\CommandButtons\\BTNMonsoon.blp"))), createUpgradeEditor());
		tabbedPane.addTab("Soundsets", getIcon(worldEditorData, "ToolBarIcon_Module_Sound"), createUpgradeEditor());

		final ObjectEditorToolbar toolBar = new ObjectEditorToolbar(worldEditorData, tabbedPane, editors, this);
		toolBar.setFloatable(false);

		setLayout(new BorderLayout());

		add(toolBar, BorderLayout.BEFORE_FIRST_LINE);
		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addChangeListener(e -> toolBar.tabbedPaneChangeListener(worldEditorData));
	}

	public void loadHotkeys() {
		JRootPane root = getRootPane();

		getRootPane().getActionMap().put("displayAsRawData", getAsAction("displayAsRawData", this::displayRaw));
		getRootPane().getActionMap().put("searchUnits", getAsAction("searchUnits", this::searchUnits));
		getRootPane().getActionMap().put("searchFindNextUnit", getAsAction("searchFindNextUnit", this::searchFindNextUnit));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control D"), "displayAsRawData");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"), "searchUnits");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control G"), "searchFindNextUnit");

		for (final UnitEditorPanel editor : editors) {
			editor.loadHotkeys();
		}
	}

	private void displayRaw() {
		for (UnitEditorPanel editor : editors) {
			editor.toggleDisplayAsRawData();
		}
	}

	private AbstractAction getAsAction(String name, Runnable runnable) {
		return new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent e) {
				runnable.run();
			}
		};
	}

	private void searchFindNextUnit() {
		int selectedIndex = tabbedPane.getSelectedIndex();
		UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		unitEditorPanel.doSearchFindNextUnit();
	}

	private void searchUnits() {
		int selectedIndex = tabbedPane.getSelectedIndex();
		UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		unitEditorPanel.doSearchForUnit();
	}

	private UnitEditorPanel createUnitEditor() {
		return new UnitEditorPanel(new UnitTabTreeBrowserBuilder());
	}

	private UnitEditorPanel createItemEditor() {
		return new UnitEditorPanel(new ItemTabTreeBrowserBuilder());
	}

	private UnitEditorPanel createDestructibleEditor() {
		return new UnitEditorPanel(new DestructableTabTreeBrowserBuilder());
	}

	private UnitEditorPanel createDoodadEditor() {
		return new UnitEditorPanel(new DoodadTabTreeBrowserBuilder());
	}

	private JComponent createAbilityEditor() {
		return new UnitEditorPanel(new AbilityTabTreeBrowserBuilder());
	}

	private UnitEditorPanel createAbilityBuffEditor() {
		return new UnitEditorPanel(new BuffTabTreeBrowserBuilder());
	}

	private UnitEditorPanel createUpgradeEditor() {
		return new UnitEditorPanel(new UpgradeTabTreeBrowserBuilder());
	}

}
