package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.AbstractWorldEditorPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.*;
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
//		War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('u', "war3map.w3u");
//		DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();

//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(), standardUnitMeta, unitDataChangeset);
		MutableObjectData unitData = new MutableUnitData();

		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("UNIT", "Unit");
		return new UnitEditorPanel(
				unitData,
				new UnitFieldBuilder(),
				new UnitTabTreeBrowserBuilder(),
				editorTabCustomToolbarButtonData,
				new NewCustomUnitDialogRunner(this, unitData));
	}

	private UnitEditorPanel createItemEditor() {
//		War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('t', "war3map.w3t");
//		DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();

//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.ITEM, StandardObjectData.getStandardItems(), standardUnitMeta, unitDataChangeset);
		MutableObjectData unitData = new MutableItemData();
		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("ITEM", "Item");
		return new UnitEditorPanel(
				unitData,
				new ItemFieldBuilder(),
				new ItemTabTreeBrowserBuilder(),
				editorTabCustomToolbarButtonData,
				() -> {});
	}

	private UnitEditorPanel createDestructibleEditor() {
//		War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('b', "war3map.w3b");
//		DataTable standardUnitMeta = StandardObjectData.getStandardDestructableMeta();

//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.DESTRUCTIBLES, StandardObjectData.getStandardDestructables(), standardUnitMeta, unitDataChangeset);
		MutableObjectData unitData = new MutableDestructibleData();
//		BasicEditorFieldBuilder editorFieldBuilder = new BasicEditorFieldBuilder(new BasicSingleFieldFactory(WorldEditorDataType.DESTRUCTIBLES), WorldEditorDataType.DESTRUCTIBLES);
//		BasicEditorFieldBuilder editorFieldBuilder = new BasicEditorFieldBuilder(new BasicSingleFieldFactory(WorldEditorDataType.DESTRUCTIBLES));
		BasicEditorFieldBuilder editorFieldBuilder = new BasicEditorFieldBuilder(WorldEditorDataType.DESTRUCTIBLES);
		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("DEST", "Dest");
		return new UnitEditorPanel(
				unitData,
				editorFieldBuilder,
				new DestructableTabTreeBrowserBuilder(),
				editorTabCustomToolbarButtonData,
				() -> {});
	}

	private UnitEditorPanel createDoodadEditor() {
//		War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('d', "war3map.w3d");
//		DataTable standardUnitMeta = StandardObjectData.getStandardDoodadMeta();

//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(), standardUnitMeta, unitDataChangeset);
		MutableObjectData unitData = new MutableDoodadData();
		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("DOOD", "Dood");
		return new UnitEditorPanel(
				unitData,
				new DoodadFieldBuilder(),
				new DoodadTabTreeBrowserBuilder(),
				editorTabCustomToolbarButtonData,
				() -> {});
	}

	private JComponent createAbilityEditor() {
//		War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('a', "war3map.w3a");
//		DataTable standardUnitMeta = StandardObjectData.getStandardAbilityMeta();

//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.ABILITIES, StandardObjectData.getStandardAbilities(), standardUnitMeta, unitDataChangeset);
		MutableObjectData unitData = new MutableAbilityData();
		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("ABIL", "Abil");
		return new UnitEditorPanel(
				unitData,
				new AbilityFieldBuilder(),
				new AbilityTabTreeBrowserBuilder(),
				editorTabCustomToolbarButtonData,
				() -> {});
	}

	private UnitEditorPanel createAbilityBuffEditor() {
//		War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('f', "war3map.w3h");
//		DataTable standardUnitMeta = StandardObjectData.getStandardAbilityBuffMeta();

//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS, StandardObjectData.getStandardAbilityBuffs(), standardUnitMeta, unitDataChangeset);
		MutableObjectData unitData = new MutableBuffData();
//		BasicEditorFieldBuilder editorFieldBuilder = new BasicEditorFieldBuilder(new BasicSingleFieldFactory(WorldEditorDataType.BUFFS_EFFECTS), WorldEditorDataType.BUFFS_EFFECTS);
//		BasicEditorFieldBuilder editorFieldBuilder = new BasicEditorFieldBuilder(new BasicSingleFieldFactory(WorldEditorDataType.BUFFS_EFFECTS));
		BasicEditorFieldBuilder editorFieldBuilder = new BasicEditorFieldBuilder(WorldEditorDataType.BUFFS_EFFECTS);
		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("BUFF", "Buff");
		return new UnitEditorPanel(
				unitData,
				editorFieldBuilder,
				new BuffTabTreeBrowserBuilder(),
				editorTabCustomToolbarButtonData,
				() -> {});
	}

	private UnitEditorPanel createUpgradeEditor() {
//		War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('g', "war3map.w3q");
//		DataTable standardMeta = StandardObjectData.getStandardUpgradeMeta();
		DataTable standardUpgradeEffectMeta = StandardObjectData.getStandardUpgradeEffectMeta();

//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UPGRADES, StandardObjectData.getStandardUpgrades(), standardMeta, unitDataChangeset);
		MutableObjectData unitData = new MutableUpgradeData();
		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("UPGR", "Upgr");
//		System.out.println("new UnitEditorPanel (" + "UPGR" + "ADES)");
		return new UnitEditorPanel(
				unitData,
				new UpgradesFieldBuilder(standardUpgradeEffectMeta),
				new UpgradeTabTreeBrowserBuilder(),
				editorTabCustomToolbarButtonData,
				() -> {});
	}


//	private War3ObjectDataChangeset getWar3ObjectDataChangeset(char expectedkind, String fileName) {
//		War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset(expectedkind);
//		try {
//			CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
//
//			if (gameDataFileSystem.has(fileName)) {
//				BlizzardDataInputStream stream = new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream(fileName));
//				WTS wts = gameDataFileSystem.has("war3map.wts") ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts")) : null;
//				unitDataChangeset.load(stream, wts, true);
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
//		return unitDataChangeset;
//	}

}
