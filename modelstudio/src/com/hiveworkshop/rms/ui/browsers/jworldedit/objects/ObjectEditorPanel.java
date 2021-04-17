package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.browsers.jworldedit.AbstractWorldEditorPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.BasicSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.War3ID;
import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ObjectEditorPanel extends AbstractWorldEditorPanel {
	private static final War3ID UNIT_NAME = War3ID.fromString("unam");

	private final List<UnitEditorPanel> editors = new ArrayList<>();
	private JButton createNewButton;
	private JButton pasteButton;
	private JButton copyButton;
	private final JTabbedPane tabbedPane;

	private MutableObjectData unitData;

	private final JFileChooser jFileChooser;

	public ObjectEditorPanel() {
		tabbedPane = new JTabbedPane() {
			@Override
			public void addTab(final String title, final Icon icon, final Component component) {
				super.addTab(title, icon, component);
				editors.add((UnitEditorPanel) component);
			}
		};
		final DataTable worldEditorData = DataTable.getWorldEditorData();
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_UNITS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewUnit"), createUnitEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_ITEMS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewItem"), createItemEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_DESTRUCTABLES"), getIcon(worldEditorData, "ToolBarIcon_OE_NewDest"), createDestructibleEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_DOODADS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewDood"), createDoodadEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_ABILITIES"), getIcon(worldEditorData, "ToolBarIcon_OE_NewAbil"), createAbilityEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_BUFFS"), getIcon(worldEditorData, "ToolBarIcon_OE_NewBuff"), createAbilityBuffEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_UPGRADES"), getIcon(worldEditorData, "ToolBarIcon_OE_NewUpgr"), createUpgradeEditor());

		tabbedPane.addTab("Terrain", getIcon(worldEditorData, "ToolBarIcon_Module_Terrain"), createUpgradeEditor());
		tabbedPane.addTab("Lighting Effects", new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNChainLightning.blp"))), createUpgradeEditor());
		tabbedPane.addTab("Weather", new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNMonsoon.blp"))), createUpgradeEditor());
		tabbedPane.addTab("Soundsets", getIcon(worldEditorData, "ToolBarIcon_Module_Sound"), createUpgradeEditor());

		final JToolBar toolBar = createToolbar(worldEditorData);
		toolBar.setFloatable(false);

		setLayout(new BorderLayout());

		add(toolBar, BorderLayout.BEFORE_FIRST_LINE);
		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addChangeListener(e -> tabbedPaneChangeListener(worldEditorData));
		jFileChooser = new JFileChooser(new File(System.getProperty("user.home") + "/Documents/Warcraft III/Maps"));
	}

	private void tabbedPaneChangeListener(DataTable worldEditorData) {
		final UnitEditorPanel selectedEditorPanel = (UnitEditorPanel) tabbedPane.getSelectedComponent();
		final EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = selectedEditorPanel.getEditorTabCustomToolbarButtonData();
		createNewButton.setIcon(getIcon(worldEditorData, editorTabCustomToolbarButtonData.getIconKey()));
		createNewButton.setToolTipText(WEString.getString(editorTabCustomToolbarButtonData.getNewCustomObject()).replace("&", ""));
		copyButton.setToolTipText(WEString.getString(editorTabCustomToolbarButtonData.getCopyObject()).replace("&", ""));
		pasteButton.setToolTipText(WEString.getString(editorTabCustomToolbarButtonData.getPasteObject()).replace("&", ""));
	}

	private JToolBar createToolbar(final DataTable worldEditorData) {
		final JToolBar toolBar = new JToolBar();
		makeButton(worldEditorData, toolBar, "newMap", "ToolBarIcon_New", "WESTRING_TOOLBAR_NEW");

		final JButton openButton = makeButton(worldEditorData, toolBar, "openMap", "ToolBarIcon_Open", "WESTRING_TOOLBAR_OPEN");
		openButton.addActionListener(e -> openSpecificTabData());

		final JButton saveButton = makeButton(worldEditorData, toolBar, "saveMap", "ToolBarIcon_Save", "WESTRING_TOOLBAR_SAVE");
		saveButton.addActionListener(e -> saveSpecificTabData());

		toolBar.add(Box.createHorizontalStrut(8));

		final TransferActionListener transferActionListener = new TransferActionListener();

		copyButton = makeButton(worldEditorData, toolBar, "copy", "ToolBarIcon_Copy", "WESTRING_MENU_OE_UNIT_COPY");
		copyButton.addActionListener(transferActionListener);
		copyButton.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));

		pasteButton = makeButton(worldEditorData, toolBar, "paste", "ToolBarIcon_Paste", "WESTRING_MENU_OE_UNIT_PASTE");
		pasteButton.addActionListener(transferActionListener);
		pasteButton.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));

		toolBar.add(Box.createHorizontalStrut(8));
		createNewButton = makeButton(worldEditorData, toolBar, "createNew", "ToolBarIcon_OE_NewUnit", "WESTRING_MENU_OE_UNIT_NEW");
		createNewButton.addActionListener(e -> createNew());

		toolBar.add(Box.createHorizontalStrut(8));

		makeButton(worldEditorData, toolBar, "terrainEditor", "ToolBarIcon_Module_Terrain", "WESTRING_MENU_MODULE_TERRAIN");
		makeButton(worldEditorData, toolBar, "scriptEditor", "ToolBarIcon_Module_Script", "WESTRING_MENU_MODULE_SCRIPTS");
		makeButton(worldEditorData, toolBar, "soundEditor", "ToolBarIcon_Module_Sound", "WESTRING_MENU_MODULE_SOUND");

		// final JButton objectEditorButton = makeButton(worldEditorData, toolBar,
		// "objectEditor",
		// "ToolBarIcon_Module_ObjectEditor", "WESTRING_MENU_OBJECTEDITOR");
		final JToggleButton objectEditorButton = new JToggleButton(getIcon(worldEditorData, "ToolBarIcon_Module_ObjectEditor"));
		objectEditorButton.setToolTipText(WEString.getString("WESTRING_MENU_OBJECTEDITOR").replace("&", ""));
		objectEditorButton.setPreferredSize(new Dimension(24, 24));
		objectEditorButton.setMargin(new Insets(1, 1, 1, 1));
		objectEditorButton.setSelected(true);
		objectEditorButton.setEnabled(false);
		objectEditorButton.setDisabledIcon(objectEditorButton.getIcon());
		toolBar.add(objectEditorButton);

		makeButton(worldEditorData, toolBar, "campaignEditor", "ToolBarIcon_Module_Campaign", "WESTRING_MENU_MODULE_CAMPAIGN");
		makeButton(worldEditorData, toolBar, "aiEditor", "ToolBarIcon_Module_AIEditor", "WESTRING_MENU_MODULE_AI");
		makeButton(worldEditorData, toolBar, "objectEditor", "ToolBarIcon_Module_ObjectManager", "WESTRING_MENU_OBJECTMANAGER");

		final String legacyImportManagerIcon = worldEditorData.get("WorldEditArt").getField("ToolBarIcon_Module_ImportManager");

		String importManagerIconPath = "ToolBarIcon_Module_ImportManager";
		String importManagerMenuName = "WESTRING_MENU_IMPORTMANAGER";

		if ((legacyImportManagerIcon == null) || "".equals(legacyImportManagerIcon)) {
			importManagerIconPath = "ToolBarIcon_Module_AssetManager";
			importManagerMenuName = "WESTRING_MENU_ASSETMANAGER";
		}
		makeButton(worldEditorData, toolBar, "importEditor", importManagerIconPath, importManagerMenuName);

		toolBar.add(Box.createHorizontalStrut(8));

		makeButton(worldEditorData, toolBar, "testMap", new ImageIcon(IconUtils.worldEditStyleIcon(getIcon(worldEditorData, "ToolBarIcon_TestMap").getImage())), "WESTRING_TOOLBAR_TESTMAP").addActionListener(e -> ObjectEditorFrame.main(new String[] {}));
		return toolBar;
	}

	private void createNew() {
		final UnitEditorPanel selectedEditorPanel = (UnitEditorPanel) tabbedPane.getSelectedComponent();
		selectedEditorPanel.runCustomUnitPopup();
	}

	public void loadHotkeys() {
		final JRootPane root = getRootPane();
		getRootPane().getActionMap().put("displayAsRawData", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final UnitEditorPanel editor : editors) {
					editor.toggleDisplayAsRawData();
				}
			}
		});
		getRootPane().getActionMap().put("searchUnits", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				searchUnits();
			}
		});
		getRootPane().getActionMap().put("searchFindNextUnit", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				searchFindNextUnit();
			}
		});

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control D"), "displayAsRawData");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"), "searchUnits");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control G"), "searchFindNextUnit");

		for (final UnitEditorPanel editor : editors) {
			editor.loadHotkeys();
		}
	}

	private void searchFindNextUnit() {
		final int selectedIndex = tabbedPane.getSelectedIndex();
		final UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		unitEditorPanel.doSearchFindNextUnit();
	}

	private void searchUnits() {
		final int selectedIndex = tabbedPane.getSelectedIndex();
		final UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		unitEditorPanel.doSearchForUnit();
	}

	private UnitEditorPanel createUnitEditor() {
		final War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('u', "war3map.w3u");

		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		unitData = new MutableObjectData(WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(),
				standardUnitMeta, unitDataChangeset);

		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				unitData,
				standardUnitMeta,
				new UnitFieldBuilder(),
				new UnitTabTreeBrowserBuilder(),
				WorldEditorDataType.UNITS,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_UNIT_NEW", "ToolBarIcon_OE_NewUnit", "WESTRING_MENU_OE_UNIT_COPY", "WESTRING_MENU_OE_UNIT_PASTE"),
				new NewCustomUnitDialogRunner(unitData));
		return unitEditorPanel;
	}

	private UnitEditorPanel createItemEditor() {
		final War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('t', "war3map.w3t");

		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();

		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.ITEM, StandardObjectData.getStandardItems(), standardUnitMeta, unitDataChangeset),
				standardUnitMeta,
				new ItemFieldBuilder(),
				new ItemTabTreeBrowserBuilder(),
				WorldEditorDataType.ITEM,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_ITEM_NEW", "ToolBarIcon_OE_NewItem", "WESTRING_MENU_OE_ITEM_COPY", "WESTRING_MENU_OE_ITEM_PASTE"),
				() -> {});
		return unitEditorPanel;
	}

	private UnitEditorPanel createDestructibleEditor() {
		final War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('b', "war3map.w3b");

		final DataTable standardUnitMeta = StandardObjectData.getStandardDestructableMeta();

		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.DESTRUCTIBLES, StandardObjectData.getStandardDestructables(), standardUnitMeta, unitDataChangeset),
				standardUnitMeta,
				new BasicEditorFieldBuilder(BasicSingleFieldFactory.INSTANCE, WorldEditorDataType.DESTRUCTIBLES),
				new DestructableTabTreeBrowserBuilder(),
				WorldEditorDataType.DESTRUCTIBLES,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_DEST_NEW", "ToolBarIcon_OE_NewDest", "WESTRING_MENU_OE_DEST_COPY", "WESTRING_MENU_OE_DEST_PASTE"),
				() -> {});
		return unitEditorPanel;
	}

	private UnitEditorPanel createDoodadEditor() {
		final War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('d', "war3map.w3d");

		final DataTable standardUnitMeta = StandardObjectData.getStandardDoodadMeta();

		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(), standardUnitMeta, unitDataChangeset),
				standardUnitMeta,
				new DoodadFieldBuilder(),
				new DoodadTabTreeBrowserBuilder(),
				WorldEditorDataType.DOODADS,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_DOOD_NEW", "ToolBarIcon_OE_NewDood", "WESTRING_MENU_OE_DOOD_COPY", "WESTRING_MENU_OE_DOOD_PASTE"),
				() -> {});
		return unitEditorPanel;
	}

	private JComponent createAbilityEditor() {
		final War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('a', "war3map.w3a");
		final DataTable standardUnitMeta = StandardObjectData.getStandardAbilityMeta();

		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.ABILITIES, StandardObjectData.getStandardAbilities(), standardUnitMeta, unitDataChangeset),
				standardUnitMeta,
				new AbilityFieldBuilder(),
				new AbilityTabTreeBrowserBuilder(),
				WorldEditorDataType.ABILITIES,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_ABIL_NEW", "ToolBarIcon_OE_NewAbil", "WESTRING_MENU_OE_ABIL_COPY", "WESTRING_MENU_OE_ABIL_PASTE"),
				() -> {});
		return unitEditorPanel;
	}

	private UnitEditorPanel createAbilityBuffEditor() {
		final War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('f', "war3map.w3h");
		final DataTable standardUnitMeta = StandardObjectData.getStandardAbilityBuffMeta();

		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS, StandardObjectData.getStandardAbilityBuffs(), standardUnitMeta, unitDataChangeset),
				standardUnitMeta,
				new BasicEditorFieldBuilder(BasicSingleFieldFactory.INSTANCE, WorldEditorDataType.BUFFS_EFFECTS),
				new BuffTabTreeBrowserBuilder(),
				WorldEditorDataType.BUFFS_EFFECTS,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_BUFF_NEW", "ToolBarIcon_OE_NewBuff", "WESTRING_MENU_OE_BUFF_COPY", "WESTRING_MENU_OE_BUFF_PASTE"),
				() -> {});
		return unitEditorPanel;
	}

	private UnitEditorPanel createUpgradeEditor() {
		final War3ObjectDataChangeset unitDataChangeset = getWar3ObjectDataChangeset('g', "war3map.w3q");
		final DataTable standardMeta = StandardObjectData.getStandardUpgradeMeta();
		final DataTable standardUpgradeEffectMeta = StandardObjectData.getStandardUpgradeEffectMeta();

		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.UPGRADES, StandardObjectData.getStandardUpgrades(), standardMeta, unitDataChangeset),
				standardMeta,
				new UpgradesFieldBuilder(standardUpgradeEffectMeta),
				new UpgradeTabTreeBrowserBuilder(),
				WorldEditorDataType.UPGRADES,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_UPGR_NEW", "ToolBarIcon_OE_NewUpgr", "WESTRING_MENU_OE_UPGR_COPY", "WESTRING_MENU_OE_UPGR_PASTE"),
				() -> {});
		return unitEditorPanel;
	}

	private War3ObjectDataChangeset getWar3ObjectDataChangeset(char g, String s) {
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset(g);
		try {
			final CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();

			if (gameDataFileSystem.has(s)) {
				unitDataChangeset.load(new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream(s)), gameDataFileSystem.has("war3map.wts") ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts")) : null, true);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return unitDataChangeset;
	}

	public void saveSpecificTabData() {
		// jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jFileChooser.resetChoosableFileFilters();
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setDialogTitle("Export Data from this Tab");

		final int selectedIndex = tabbedPane.getSelectedIndex();

		final UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		WorldEditorDataType worldEditorDataType = unitEditorPanel.getUnitData().getWorldEditorDataType();
		JOptionPane.showMessageDialog(ObjectEditorPanel.this, "OK, friend, we are going to export " + worldEditorDataType);

		String fileType = getFileTypeName(worldEditorDataType);
		final String extension = worldEditorDataType.getExtension();
		jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(fileType, extension));

		if (jFileChooser.showSaveDialog(ObjectEditorPanel.this) == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = jFileChooser.getSelectedFile();
			if (selectedFile != null) {
				String path = selectedFile.getPath();
				if (!path.toLowerCase().endsWith("." + extension)) {
					path += "." + extension;
				}
				final File w3uFile = new File(path);

				if (w3uFile.exists()) {
					final int result = JOptionPane.showConfirmDialog(ObjectEditorPanel.this, w3uFile.getName() + " already exists. Ok to overwrite?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

					if (result != JOptionPane.OK_OPTION) {
						return;
					}
				}
				try {
					try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(w3uFile)) {
						unitEditorPanel.getUnitData().getEditorData().save(outputStream, false);
					}
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public String getFileTypeName(final WorldEditorDataType dataType) {
		return switch (dataType) {
			case ABILITIES -> WEString.getString("WESTRING_FILETYPE_ABILITYDATA");
			case BUFFS_EFFECTS -> WEString.getString("WESTRING_FILETYPE_BUFFDATA");
			case DESTRUCTIBLES -> WEString.getString("WESTRING_FILETYPE_DESTRUCTABLEDATA");
			case DOODADS -> WEString.getString("WESTRING_FILETYPE_DOODADDATA");
			case ITEM -> WEString.getString("WESTRING_FILETYPE_ITEMDATA");
			case UNITS -> WEString.getString("WESTRING_FILETYPE_UNITDATA");
			case UPGRADES -> WEString.getString("WESTRING_FILETYPE_UPGRADEDATA");
		};
	}

	public void openSpecificTabData() {
		// jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jFileChooser.resetChoosableFileFilters();
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setDialogTitle("Import Data to this Tab");

		final int selectedIndex = tabbedPane.getSelectedIndex();
		final UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		WorldEditorDataType worldEditorDataType = unitEditorPanel.getUnitData().getWorldEditorDataType();

		JOptionPane.showMessageDialog(ObjectEditorPanel.this,
				"OK, friend, we are going to import " + worldEditorDataType
						+ ". This will replace all settings, like WE.");

		final String extension = worldEditorDataType.getExtension();
		jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(getFileTypeName(worldEditorDataType), extension));


		if (jFileChooser.showOpenDialog(ObjectEditorPanel.this) == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = jFileChooser.getSelectedFile();

			if (selectedFile != null) {
				final String path = selectedFile.getPath();
				final File w3uFile = new File(path);
				if (!w3uFile.exists()) {
					JOptionPane.showMessageDialog(ObjectEditorPanel.this, "Error. Chosen file did not exist. Retry?");
					return;
				}
				try {
					try (BlizzardDataInputStream inputStream = new BlizzardDataInputStream(
							new FileInputStream(w3uFile))) {
						unitEditorPanel.getUnitData().dropCachesHack();
						unitEditorPanel.getUnitData().getEditorData().getCustom().clear();
						unitEditorPanel.getUnitData().getEditorData().getOriginal().clear();
						unitEditorPanel.getUnitData().getEditorData().load(inputStream, null, false);
						unitEditorPanel.reloadAllDataVerySlowly();
					}
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private final class NewCustomUnitDialogRunner implements Runnable {
		private final MutableObjectData unitData;

		private NewCustomUnitDialogRunner(final MutableObjectData unitData) {
			this.unitData = unitData;
		}

		@Override
		public void run() {
			final JLabel nameLabel = new JLabel(WEString.getString("WESTRING_UE_FIELDNAME") + ":");

			final JTextField nameField = new JTextField(30);
			nameField.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
			nameField.setPreferredSize(new Dimension(200, 18));
			nameField.setMaximumSize(new Dimension(200, 100));

			final JLabel baseUnitLabel = new JLabel(WEString.getString("WESTRING_UE_BASEUNIT").replace("&", "") + ":");

			final UnitOptionPanel unitOptionPanel = new UnitOptionPanel(StandardObjectData.getStandardUnits(), StandardObjectData.getStandardAbilities(), true, true);
			unitOptionPanel.setPreferredSize(new Dimension(416, 400));
			unitOptionPanel.setSize(new Dimension(416, 400));
			unitOptionPanel.doLayout();
			unitOptionPanel.relayout();

			final JPanel popupPanel = new JPanel(new MigLayout("ins 0"));
			popupPanel.add(nameLabel, "split 2, spanx");
			popupPanel.add(nameField, "wrap");
			popupPanel.add(baseUnitLabel, "spanx, wrap");
			popupPanel.add(unitOptionPanel, "wrap");

			final int response = JOptionPane.showConfirmDialog(ObjectEditorPanel.this, popupPanel,
					WEString.getString("WESTRING_UE_CREATECUSTOMUNIT"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (response == JOptionPane.OK_OPTION) {
				final GameObject selection = unitOptionPanel.getSelection();
				final War3ID sourceId = War3ID.fromString(selection.getId());
				final War3ID objectId = unitData.getNextDefaultEditorId(War3ID.fromString(sourceId.charAt(0) + "000"));
				final MutableGameObject newObject = unitData.createNew(objectId, sourceId);
				newObject.setField(UNIT_NAME, 0, nameField.getText());
			}
		}
	}
}
