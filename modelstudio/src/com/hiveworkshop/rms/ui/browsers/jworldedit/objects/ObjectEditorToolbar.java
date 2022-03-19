package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.ui.browsers.jworldedit.ToolbarButtonAction;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ObjectEditorToolbar extends JToolBar {

	private final JFileChooser jFileChooser;
	List<UnitEditorPanel> editors;
	JTabbedPane tabbedPane;
	Component popupParent;

	private JButton createNewButton;
	private JButton pasteButton;
	private JButton copyButton;

	public ObjectEditorToolbar(DataTable worldEditorData, JTabbedPane tabbedPane, List<UnitEditorPanel> editors, Component popupParent) {
		this.tabbedPane = tabbedPane;
		this.popupParent = popupParent;
		this.editors = editors;
		jFileChooser = new JFileChooser(new File(System.getProperty("user.home") + "/Documents/Warcraft III/Maps"));

		addButton(worldEditorData, "newMap", "ToolBarIcon_New", "WESTRING_TOOLBAR_NEW");

		JButton openButton = addButton(worldEditorData, "openMap", "ToolBarIcon_Open", "WESTRING_TOOLBAR_OPEN");
		openButton.addActionListener(e -> openSpecificTabData());

		JButton saveButton = addButton(worldEditorData, "saveMap", "ToolBarIcon_Save", "WESTRING_TOOLBAR_SAVE");
		saveButton.addActionListener(e -> saveSpecificTabData());

		add(Box.createHorizontalStrut(8));

		TransferActionListener transferActionListener = new TransferActionListener();

		copyButton = addButton(worldEditorData, "copy", "ToolBarIcon_Copy", "WESTRING_MENU_OE_UNIT_COPY");
		copyButton.addActionListener(transferActionListener);
		copyButton.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));

		pasteButton = addButton(worldEditorData, "paste", "ToolBarIcon_Paste", "WESTRING_MENU_OE_UNIT_PASTE");
		pasteButton.addActionListener(transferActionListener);
		pasteButton.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));

		add(Box.createHorizontalStrut(8));
		createNewButton = addButton(worldEditorData, "createNew", "ToolBarIcon_OE_NewUnit", "WESTRING_MENU_OE_UNIT_NEW");
//		createNewButton.addActionListener(e -> createNew());
		createNewButton.addActionListener(e -> ((UnitEditorPanel) tabbedPane.getSelectedComponent()).runCustomUnitPopup());

		add(Box.createHorizontalStrut(8));

		addButton(worldEditorData, "terrainEditor", "ToolBarIcon_Module_Terrain", "WESTRING_MENU_MODULE_TERRAIN");
		addButton(worldEditorData, "scriptEditor", "ToolBarIcon_Module_Script", "WESTRING_MENU_MODULE_SCRIPTS");
		addButton(worldEditorData, "soundEditor", "ToolBarIcon_Module_Sound", "WESTRING_MENU_MODULE_SOUND");

		// final JButton objectEditorButton = addButton(worldEditorData, toolBar, objectEditor", "ToolBarIcon_Module_ObjectEditor", "WESTRING_MENU_OBJECTEDITOR");
		JToggleButton objectEditorButton = getObjectEditorButton(worldEditorData);
		add(objectEditorButton);

		addButton(worldEditorData, "campaignEditor", "ToolBarIcon_Module_Campaign", "WESTRING_MENU_MODULE_CAMPAIGN");
		addButton(worldEditorData, "aiEditor", "ToolBarIcon_Module_AIEditor", "WESTRING_MENU_MODULE_AI");
		addButton(worldEditorData, "objectEditor", "ToolBarIcon_Module_ObjectManager", "WESTRING_MENU_OBJECTMANAGER");

		String legacyImportManagerIcon = worldEditorData.get("WorldEditArt").getField("ToolBarIcon_Module_ImportManager");

		String importManagerIconPath = "ToolBarIcon_Module_ImportManager";
		String importManagerMenuName = "WESTRING_MENU_IMPORTMANAGER";

		if ((legacyImportManagerIcon == null) || "".equals(legacyImportManagerIcon)) {
			importManagerIconPath = "ToolBarIcon_Module_AssetManager";
			importManagerMenuName = "WESTRING_MENU_ASSETMANAGER";
		}
		addButton(worldEditorData, "importEditor", importManagerIconPath, importManagerMenuName);

		add(Box.createHorizontalStrut(8));

		addButton("testMap", new ImageIcon(IconUtils.worldEditStyleIcon(getIcon(worldEditorData, "ToolBarIcon_TestMap").getImage())), "WESTRING_TOOLBAR_TESTMAP").addActionListener(e -> ObjectEditorFrame.main(new String[] {}));

	}

	public void tabbedPaneChangeListener(DataTable worldEditorData) {
		UnitEditorPanel selectedEditorPanel = (UnitEditorPanel) tabbedPane.getSelectedComponent();
		EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = selectedEditorPanel.getEditorTabCustomToolbarButtonData();
		createNewButton.setIcon(getIcon(worldEditorData, editorTabCustomToolbarButtonData.getIconKey()));
		createNewButton.setToolTipText(WEString.getString(editorTabCustomToolbarButtonData.getNewCustomObject()).replace("&", ""));
		copyButton.setToolTipText(WEString.getString(editorTabCustomToolbarButtonData.getCopyObject()).replace("&", ""));
		pasteButton.setToolTipText(WEString.getString(editorTabCustomToolbarButtonData.getPasteObject()).replace("&", ""));
	}

	private JToggleButton getObjectEditorButton(DataTable worldEditorData) {
		JToggleButton objectEditorButton = new JToggleButton(getIcon(worldEditorData, "ToolBarIcon_Module_ObjectEditor"));
		objectEditorButton.setToolTipText(WEString.getString("WESTRING_MENU_OBJECTEDITOR").replace("&", ""));
		objectEditorButton.setPreferredSize(new Dimension(24, 24));
		objectEditorButton.setMargin(new Insets(1, 1, 1, 1));
		objectEditorButton.setSelected(true);
		objectEditorButton.setEnabled(false);
		objectEditorButton.setDisabledIcon(objectEditorButton.getIcon());
		return objectEditorButton;
	}


	public JButton addButton(DataTable worldEditorData, String actionName, String iconKey, String tooltipKey) {
		return addButton(actionName, getIcon(worldEditorData, iconKey), tooltipKey);
	}

	public JButton addButton(String actionName, ImageIcon icon, String tooltipKey) {
		JButton button = add(new ToolbarButtonAction(actionName, icon));
		button.setToolTipText(WEString.getString(tooltipKey).replace("&", ""));
		button.setPreferredSize(new Dimension(24, 24));
		button.setMargin(new Insets(1, 1, 1, 1));
		button.setFocusable(false);
		return button;
	}


	public static ImageIcon getIcon(DataTable worldEditorData, String iconName) {
		String iconTexturePath = worldEditorData.get("WorldEditArt").getField(iconName);
		if (!iconTexturePath.endsWith(".blp")) {
			iconTexturePath += ".blp";
		}
		return new ImageIcon(BLPHandler.getGameTex(iconTexturePath));
	}


	public void openSpecificTabData() {
		// jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jFileChooser.resetChoosableFileFilters();
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setDialogTitle("Import Data to this Tab");

		int selectedIndex = tabbedPane.getSelectedIndex();
		UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		WorldEditorDataType worldEditorDataType = unitEditorPanel.getUnitData().getWorldEditorDataType();

		JOptionPane.showMessageDialog(popupParent,
				"OK, friend, we are going to import " + worldEditorDataType
						+ ". This will replace all settings, like WE.");

		String extension = worldEditorDataType.getExtension();
		jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(getFileTypeName(worldEditorDataType), extension));


		if (jFileChooser.showOpenDialog(popupParent) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jFileChooser.getSelectedFile();

			if (selectedFile != null) {
				String path = selectedFile.getPath();
				File w3uFile = new File(path);
				if (!w3uFile.exists()) {
					JOptionPane.showMessageDialog(popupParent, "Error. Chosen file did not exist. Retry?");
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

	public void saveSpecificTabData() {
		// jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jFileChooser.resetChoosableFileFilters();
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setDialogTitle("Export Data from this Tab");

		int selectedIndex = tabbedPane.getSelectedIndex();

		UnitEditorPanel unitEditorPanel = editors.get(selectedIndex);
		WorldEditorDataType worldEditorDataType = unitEditorPanel.getUnitData().getWorldEditorDataType();
		JOptionPane.showMessageDialog(popupParent, "OK, friend, we are going to export " + worldEditorDataType);

		String fileType = getFileTypeName(worldEditorDataType);
		String extension = worldEditorDataType.getExtension();
		jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(fileType, extension));

		if (jFileChooser.showSaveDialog(popupParent) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jFileChooser.getSelectedFile();
			if (selectedFile != null) {
				String path = selectedFile.getPath();
				if (!path.toLowerCase().endsWith("." + extension)) {
					path += "." + extension;
				}
				File w3uFile = new File(path);

				if (w3uFile.exists()) {
					int result = JOptionPane.showConfirmDialog(popupParent, w3uFile.getName() + " already exists. Ok to overwrite?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

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
}
