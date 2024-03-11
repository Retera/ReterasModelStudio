package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.AbstractWorldEditorPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.icons.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public final class ObjectEditorPanel extends AbstractWorldEditorPanel {

	private final List<UnitEditorPanel> editors = new ArrayList<>();
	private final JTabbedPane tabbedPane;
//	private MutableObjectData unitData;

	//// Field categories for Object Editor
	//// These are referenced from the various meta data SLKs
	// [ObjectEditorCategories]
	// abil=WESTRING_OE_CAT_ABILITIES
	// art=WESTRING_OE_CAT_ART
	// combat=WESTRING_OE_CAT_COMBAT
	// data=WESTRING_OE_CAT_DATA
	// editor=WESTRING_OE_CAT_EDITOR
	// move=WESTRING_OE_CAT_MOVEMENT
	// path=WESTRING_OE_CAT_PATHING
	// sound=WESTRING_OE_CAT_SOUND
	// stats=WESTRING_OE_CAT_STATS
	// tech=WESTRING_OE_CAT_TECHTREE
	// text=WESTRING_OE_CAT_TEXT

	private String unitTabTitle;
	private String itemTabTitle;
	private String destTabTitle;
	private String doodTabTitle;
	private String abilTabTitle;
	private String buffTabTitle;
	private String upgrTabTitle;
	private String terrTabTitle;
	private String lighTabTitle;
	private String weatTabTitle;
	private String sounTabTitle;

	public ObjectEditorPanel() {
		tabbedPane = new JTabbedPane() {
			@Override
			public void addTab(final String title, final Icon icon, final Component component) {
				super.addTab(title, icon, component);
				editors.add((UnitEditorPanel) component);
			}
		};
		setTabNames();
		final DataTable worldEditorData = DataTableHolder.getWorldEditorData();
		tabbedPane.addTab(unitTabTitle, getIcon(worldEditorData, "ToolBarIcon_OE_NewUnit"), createUnitEditor());
		tabbedPane.addTab(itemTabTitle, getIcon(worldEditorData, "ToolBarIcon_OE_NewItem"), createItemEditor());
		tabbedPane.addTab(destTabTitle, getIcon(worldEditorData, "ToolBarIcon_OE_NewDest"), createDestructibleEditor());
		tabbedPane.addTab(doodTabTitle, getIcon(worldEditorData, "ToolBarIcon_OE_NewDood"), createDoodadEditor());
		tabbedPane.addTab(abilTabTitle, getIcon(worldEditorData, "ToolBarIcon_OE_NewAbil"), createAbilityEditor());
		tabbedPane.addTab(buffTabTitle, getIcon(worldEditorData, "ToolBarIcon_OE_NewBuff"), createAbilityBuffEditor());
		tabbedPane.addTab(upgrTabTitle, getIcon(worldEditorData, "ToolBarIcon_OE_NewUpgr"), createUpgradeEditor());
		System.out.println("UpgradePanel done!");
//		tabbedPane.addTab("Terrain", getIcon(worldEditorData, "ToolBarIcon_Module_Terrain"), createUpgradeEditor());
//		tabbedPane.addTab("Lighting Effects", new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.getImage("ReplaceableTextures\\CommandButtons\\BTNChainLightning.blp"))), createUpgradeEditor());
//		tabbedPane.addTab("Weather", new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.getImage("ReplaceableTextures\\CommandButtons\\BTNMonsoon.blp"))), createUpgradeEditor());
//		tabbedPane.addTab("Soundsets", getIcon(worldEditorData, "ToolBarIcon_Module_Sound"), createUpgradeEditor());
		tabbedPane.addTab(terrTabTitle, getIcon(worldEditorData, "ToolBarIcon_Module_Terrain"), createTerrainEditor());
		tabbedPane.addTab(lighTabTitle, new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.getImage("ReplaceableTextures\\CommandButtons\\BTNChainLightning.blp"))), createLightEditor());
		tabbedPane.addTab(weatTabTitle, new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.getImage("ReplaceableTextures\\CommandButtons\\BTNMonsoon.blp"))), createWeatherEditor());
		tabbedPane.addTab(sounTabTitle, getIcon(worldEditorData, "ToolBarIcon_Module_Sound"), createSoundEditor());

		final ObjectEditorToolbar toolBar = new ObjectEditorToolbar(worldEditorData, tabbedPane, editors, this);
		toolBar.setFloatable(false);

		setLayout(new BorderLayout());

		add(toolBar, BorderLayout.BEFORE_FIRST_LINE);
		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addChangeListener(e -> toolBar.tabbedPaneChangeListener(worldEditorData));
	}

	private void setTabNames() {
		unitTabTitle = WEString.getString("WESTRING_OBJTAB_UNITS");
		itemTabTitle = WEString.getString("WESTRING_OBJTAB_ITEMS");
		destTabTitle = WEString.getString("WESTRING_OBJTAB_DESTRUCTABLES");
		doodTabTitle = WEString.getString("WESTRING_OBJTAB_DOODADS");
		abilTabTitle = WEString.getString("WESTRING_OBJTAB_ABILITIES");
		buffTabTitle = WEString.getString("WESTRING_OBJTAB_BUFFS");
		upgrTabTitle = WEString.getString("WESTRING_OBJTAB_UPGRADES");
		terrTabTitle = WEString.getString("WESTRING_TERRAIN");
		lighTabTitle = "Lighting Effects";
		weatTabTitle = "Weather";
		sounTabTitle = "Soundsets";
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


	private JPanel createSoundEditor() {

		// WESTRING_CE_SOUND
		// WESTRING_IMP_TYPE_SOUND
		// WESTRING_MENU_MODULE_SOUND
		// WESTRING_MENU_OM_SOUNDS
		// WESTRING_MENU_VIEWSOUNDPROPS
		// WESTRING_MODULE_SOUND
		// WESTRING_OE_CAT_SOUND
		// WESTRING_OM_VIEW_SOUNDS
		// WESTRING_PREFS_EDITOR_UI_SOUNDS
		// WESTRING_PREFTAB_SOUND
		// WESTRING_SNE_SOUNDS
		// WESTRING_TOOLBAR_EXPORTSOUND
		// WESTRING_TOOLBAR_IMPORTSOUND
		// WESTRING_TOOLBAR_IM_EXPORT
		// WESTRING_TOOLBAR_USESOUND
		// WESTRING_TRIGCAT_SOUND
		// WESTRING_TOOLBAR_USESOUND

		// WESTRING_MENU_MODULE_SOUND
		// WESTRING_MENU_USEMUSIC
		// WESTRING_MENU_USESOUND
		// WESTRING_MODULE_SOUND
		return new JPanel();
	}

	private JPanel createLightEditor() {
		// WESTRING_LIGHTNINGEFFECT
		// WESTRING_WEATHER
		// WESTRING_COD_TYPE_LIGHTNINGEFFECT
		// WESTRING_COD_TYPE_LIGHTNINGLIST
		// WESTRING_EFFECT_TYPE_LIGHTNING
		// WESTRING_DTYPE_ENVIRONMENT
		// WESTRING_MENU_SKY"
		// WESTRING_MENU_LIGHTING"
		return new JPanel();
	}

	private JPanel createTerrainEditor() {
		// WESTRING_DTYPE_TERRAIN
		// WESTRING_MENU_MODULE_TERRAIN
		// WESTRING_MENU_TERRAIN
		// WESTRING_MODULE_TERRAIN
		// WESTRING_TERRAIN
		return new JPanel();
	}

	private JPanel createWeatherEditor() {
		return new JPanel();
	}

}
