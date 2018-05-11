package com.hiveworkshop.wc3.jworldedit.objects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.hiveworkshop.wc3.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.wc3.jworldedit.AbstractWorldEditorPanel;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders.AbilityFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders.BasicEditorFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders.DoodadFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders.ItemFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders.UnitFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders.UpgradesFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory.BasicSingleFieldFactory;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.UnitOptionPanel;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;
import com.hiveworkshop.wc3.util.IconUtils;

import de.wc3data.stream.BlizzardDataOutputStream;

public final class ObjectEditorPanel extends AbstractWorldEditorPanel {
	private static final War3ID UNIT_NAME = War3ID.fromString("unam");

	private final List<UnitEditorPanel> editors = new ArrayList<>();
	private JButton createNewButton;
	private JButton pasteButton;
	private JButton copyButton;
	private final JTabbedPane tabbedPane;

	private MutableObjectData unitData;

	public ObjectEditorPanel() {
		tabbedPane = new JTabbedPane() {
			@Override
			public void addTab(final String title, final Icon icon, final Component component) {
				super.addTab(title, icon, component);
				editors.add((UnitEditorPanel) component);
			}
		};
		final DataTable worldEditorData = DataTable.getWorldEditorData();
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_UNITS"),
				getIcon(worldEditorData, "ToolBarIcon_OE_NewUnit"), createUnitEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_ITEMS"),
				getIcon(worldEditorData, "ToolBarIcon_OE_NewItem"), createItemEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_DESTRUCTABLES"),
				getIcon(worldEditorData, "ToolBarIcon_OE_NewDest"), createDestructibleEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_DOODADS"),
				getIcon(worldEditorData, "ToolBarIcon_OE_NewDood"), createDoodadEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_ABILITIES"),
				getIcon(worldEditorData, "ToolBarIcon_OE_NewAbil"), createAbilityEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_BUFFS"),
				getIcon(worldEditorData, "ToolBarIcon_OE_NewBuff"), createAbilityBuffEditor());
		tabbedPane.addTab(WEString.getString("WESTRING_OBJTAB_UPGRADES"),
				getIcon(worldEditorData, "ToolBarIcon_OE_NewUpgr"), createUpgradeEditor());
		tabbedPane.addTab("Terrains", getIcon(worldEditorData, "ToolBarIcon_Module_Terrain"), createUpgradeEditor());

		final JToolBar toolBar = createToolbar(worldEditorData);
		toolBar.setFloatable(false);

		setLayout(new BorderLayout());

		add(toolBar, BorderLayout.BEFORE_FIRST_LINE);
		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final UnitEditorPanel selectedEditorPanel = (UnitEditorPanel) tabbedPane.getSelectedComponent();
				final EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData = selectedEditorPanel
						.getEditorTabCustomToolbarButtonData();
				createNewButton.setIcon(getIcon(worldEditorData, editorTabCustomToolbarButtonData.getIconKey()));
				createNewButton.setToolTipText(
						WEString.getString(editorTabCustomToolbarButtonData.getNewCustomObject()).replace("&", ""));
				copyButton.setToolTipText(
						WEString.getString(editorTabCustomToolbarButtonData.getCopyObject()).replace("&", ""));
				pasteButton.setToolTipText(
						WEString.getString(editorTabCustomToolbarButtonData.getPasteObject()).replace("&", ""));
			}
		});
	}

	private JToolBar createToolbar(final DataTable worldEditorData) {
		final JToolBar toolBar = new JToolBar();
		makeButton(worldEditorData, toolBar, "newMap", "ToolBarIcon_New", "WESTRING_TOOLBAR_NEW");
		makeButton(worldEditorData, toolBar, "openMap", "ToolBarIcon_Open", "WESTRING_TOOLBAR_OPEN");
		final JButton saveButton = makeButton(worldEditorData, toolBar, "saveMap", "ToolBarIcon_Save",
				"WESTRING_TOOLBAR_SAVE");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser jFileChooser = new JFileChooser(
						new File(System.getProperty("user.home") + "/Documents/Warcraft III/Maps"));
				jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jFileChooser.setDialogTitle("Save Map");
				if (jFileChooser.showSaveDialog(ObjectEditorPanel.this) == JFileChooser.APPROVE_OPTION) {
					final File selectedFile = jFileChooser.getSelectedFile();
					if (selectedFile != null) {
						final File w3uFile = new File(selectedFile.getPath() + ".w3u");
						try {
							try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(w3uFile)) {
								unitData.getEditorData().save(outputStream, false);
							}
						} catch (final FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (final IOException e1) {
							e1.printStackTrace();
						}
					}
				}

			}
		});
		toolBar.add(Box.createHorizontalStrut(8));
		final TransferActionListener transferActionListener = new TransferActionListener();
		copyButton = makeButton(worldEditorData, toolBar, "copy", "ToolBarIcon_Copy", "WESTRING_MENU_OE_UNIT_COPY");
		copyButton.addActionListener(transferActionListener);
		copyButton.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
		pasteButton = makeButton(worldEditorData, toolBar, "paste", "ToolBarIcon_Paste", "WESTRING_MENU_OE_UNIT_PASTE");
		pasteButton.addActionListener(transferActionListener);
		pasteButton.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
		toolBar.add(Box.createHorizontalStrut(8));
		createNewButton = makeButton(worldEditorData, toolBar, "createNew", "ToolBarIcon_OE_NewUnit",
				"WESTRING_MENU_OE_UNIT_NEW");
		createNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final UnitEditorPanel selectedEditorPanel = (UnitEditorPanel) tabbedPane.getSelectedComponent();
				selectedEditorPanel.runCustomUnitPopup();
			}
		});
		toolBar.add(Box.createHorizontalStrut(8));
		makeButton(worldEditorData, toolBar, "terrainEditor", "ToolBarIcon_Module_Terrain",
				"WESTRING_MENU_MODULE_TERRAIN");
		makeButton(worldEditorData, toolBar, "scriptEditor", "ToolBarIcon_Module_Script",
				"WESTRING_MENU_MODULE_SCRIPTS");
		makeButton(worldEditorData, toolBar, "soundEditor", "ToolBarIcon_Module_Sound", "WESTRING_MENU_MODULE_SOUND");
		// final JButton objectEditorButton = makeButton(worldEditorData, toolBar, "objectEditor",
		// "ToolBarIcon_Module_ObjectEditor", "WESTRING_MENU_OBJECTEDITOR");
		final JToggleButton objectEditorButton = new JToggleButton(
				getIcon(worldEditorData, "ToolBarIcon_Module_ObjectEditor"));
		objectEditorButton.setToolTipText(WEString.getString("WESTRING_MENU_OBJECTEDITOR").replace("&", ""));
		objectEditorButton.setPreferredSize(new Dimension(24, 24));
		objectEditorButton.setMargin(new Insets(1, 1, 1, 1));
		objectEditorButton.setSelected(true);
		objectEditorButton.setEnabled(false);
		objectEditorButton.setDisabledIcon(objectEditorButton.getIcon());
		toolBar.add(objectEditorButton);
		makeButton(worldEditorData, toolBar, "campaignEditor", "ToolBarIcon_Module_Campaign",
				"WESTRING_MENU_MODULE_CAMPAIGN");
		makeButton(worldEditorData, toolBar, "aiEditor", "ToolBarIcon_Module_AIEditor", "WESTRING_MENU_MODULE_AI");
		makeButton(worldEditorData, toolBar, "objectEditor", "ToolBarIcon_Module_ObjectManager",
				"WESTRING_MENU_OBJECTMANAGER");
		makeButton(worldEditorData, toolBar, "importEditor", "ToolBarIcon_Module_ImportManager",
				"WESTRING_MENU_IMPORTMANAGER");
		toolBar.add(Box.createHorizontalStrut(8));
		makeButton(worldEditorData, toolBar, "testMap",
				new ImageIcon(IconUtils.worldEditStyleIcon(getIcon(worldEditorData, "ToolBarIcon_TestMap").getImage())),
				"WESTRING_TOOLBAR_TESTMAP");
		return toolBar;
	}

	public void loadHotkeys() {
		final JRootPane root = getRootPane();
		this.getRootPane().getActionMap().put("displayAsRawData", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final UnitEditorPanel editor : editors) {
					editor.toggleDisplayAsRawData();
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control D"),
				"displayAsRawData");
	}

	private UnitEditorPanel createUnitEditor() {
		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset();
		unitData = new MutableObjectData(WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(),
				standardUnitMeta, unitDataChangeset);
		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(unitData, standardUnitMeta, new UnitFieldBuilder(),
				new UnitTabTreeBrowserBuilder(), WorldEditorDataType.UNITS,
				new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_UNIT_NEW", "ToolBarIcon_OE_NewUnit",
						"WESTRING_MENU_OE_UNIT_COPY", "WESTRING_MENU_OE_UNIT_PASTE"),
				new NewCustomUnitDialogRunner(unitData));
		return unitEditorPanel;
	}

	private UnitEditorPanel createItemEditor() {
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset();
		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.ITEM, StandardObjectData.getStandardItems(), standardUnitMeta,
						unitDataChangeset),
				standardUnitMeta, new ItemFieldBuilder(), new ItemTabTreeBrowserBuilder(),
				WorldEditorDataType.ITEM, new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_ITEM_NEW",
						"ToolBarIcon_OE_NewItem", "WESTRING_MENU_OE_ITEM_COPY", "WESTRING_MENU_OE_ITEM_PASTE"),
				new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
					}
				});
		return unitEditorPanel;
	}

	private UnitEditorPanel createDestructibleEditor() {
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset('b');
		final DataTable standardUnitMeta = StandardObjectData.getStandardDestructableMeta();
		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.DESTRUCTIBLES, StandardObjectData.getStandardDestructables(),
						standardUnitMeta, unitDataChangeset),
				standardUnitMeta,
				new BasicEditorFieldBuilder(BasicSingleFieldFactory.INSTANCE, WorldEditorDataType.DESTRUCTIBLES),
				new DestructableTabTreeBrowserBuilder(),
				WorldEditorDataType.DESTRUCTIBLES, new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_DEST_NEW",
						"ToolBarIcon_OE_NewDest", "WESTRING_MENU_OE_DEST_COPY", "WESTRING_MENU_OE_DEST_PASTE"),
				new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub

					}
				});
		return unitEditorPanel;
	}

	private UnitEditorPanel createDoodadEditor() {
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset('d');
		final DataTable standardUnitMeta = StandardObjectData.getStandardDoodadMeta();
		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(),
						standardUnitMeta, unitDataChangeset),
				standardUnitMeta, new DoodadFieldBuilder(), new DoodadTabTreeBrowserBuilder(),
				WorldEditorDataType.DOODADS, new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_DOOD_NEW",
						"ToolBarIcon_OE_NewDood", "WESTRING_MENU_OE_DOOD_COPY", "WESTRING_MENU_OE_DOOD_PASTE"),
				new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub

					}
				});
		return unitEditorPanel;
	}

	private UnitEditorPanel createAbilityEditor() {
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset('a');
		final DataTable standardUnitMeta = StandardObjectData.getStandardAbilityMeta();
		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.ABILITIES, StandardObjectData.getStandardAbilities(),
						standardUnitMeta, unitDataChangeset),
				standardUnitMeta, new AbilityFieldBuilder(), new AbilityTabTreeBrowserBuilder(),
				WorldEditorDataType.ABILITIES, new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_ABIL_NEW",
						"ToolBarIcon_OE_NewAbil", "WESTRING_MENU_OE_ABIL_COPY", "WESTRING_MENU_OE_ABIL_PASTE"),
				new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub

					}
				});
		return unitEditorPanel;
	}

	private UnitEditorPanel createAbilityBuffEditor() {
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset('f');
		final DataTable standardUnitMeta = StandardObjectData.getStandardAbilityBuffMeta();
		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS, StandardObjectData.getStandardAbilityBuffs(),
						standardUnitMeta, unitDataChangeset),
				standardUnitMeta,
				new BasicEditorFieldBuilder(BasicSingleFieldFactory.INSTANCE, WorldEditorDataType.BUFFS_EFFECTS),
				new BuffTabTreeBrowserBuilder(),
				WorldEditorDataType.BUFFS_EFFECTS, new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_BUFF_NEW",
						"ToolBarIcon_OE_NewBuff", "WESTRING_MENU_OE_BUFF_COPY", "WESTRING_MENU_OE_BUFF_PASTE"),
				new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub

					}
				});
		return unitEditorPanel;
	}

	private UnitEditorPanel createUpgradeEditor() {
		final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset('g');
		final DataTable standardMeta = StandardObjectData.getStandardUpgradeMeta();
		final DataTable standardUpgradeEffectMeta = StandardObjectData.getStandardUpgradeEffectMeta();
		final UnitEditorPanel unitEditorPanel = new UnitEditorPanel(
				new MutableObjectData(WorldEditorDataType.UPGRADES, StandardObjectData.getStandardUpgrades(),
						standardMeta, unitDataChangeset),
				standardMeta, new UpgradesFieldBuilder(standardUpgradeEffectMeta), new UpgradeTabTreeBrowserBuilder(),
				WorldEditorDataType.UPGRADES, new EditorTabCustomToolbarButtonData("WESTRING_MENU_OE_UPGR_NEW",
						"ToolBarIcon_OE_NewUpgr", "WESTRING_MENU_OE_UPGR_COPY", "WESTRING_MENU_OE_UPGR_PASTE"),
				new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub

					}
				});
		return unitEditorPanel;
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
			nameField.setMaximumSize(new Dimension(200, 1000));
			final JLabel baseUnitLabel = new JLabel(WEString.getString("WESTRING_UE_BASEUNIT").replace("&", "") + ":");
			final UnitOptionPanel unitOptionPanel = new UnitOptionPanel(StandardObjectData.getStandardUnits(),
					StandardObjectData.getStandardAbilities(), 2, true, true);
			unitOptionPanel.setPreferredSize(new Dimension(416, 400));
			unitOptionPanel.setSize(new Dimension(416, 400));
			unitOptionPanel.doLayout();
			unitOptionPanel.relayout();
			final JPanel popupPanel = new JPanel();
			final GroupLayout groupLayout = new GroupLayout(popupPanel);
			popupPanel.setLayout(groupLayout);
			groupLayout.setHorizontalGroup(groupLayout
					.createParallelGroup().addGroup(groupLayout.createSequentialGroup().addComponent(nameLabel)
							.addGap(4).addComponent(nameField))
					.addComponent(baseUnitLabel).addComponent(unitOptionPanel));
			groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup().addComponent(nameLabel).addComponent(nameField))
					.addComponent(baseUnitLabel).addComponent(unitOptionPanel));

			final int response = JOptionPane.showConfirmDialog(ObjectEditorPanel.this, popupPanel,
					WEString.getString("WESTRING_UE_CREATECUSTOMUNIT"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (response == JOptionPane.OK_OPTION) {
				final GameObject selection = unitOptionPanel.getSelection();
				final War3ID sourceId = War3ID.fromString(selection.getId());
				final MutableGameObject newObject = unitData.createNew(
						unitData.getNextDefaultEditorId(War3ID.fromString(sourceId.charAt(0) + "000")), sourceId);
				newObject.setField(UNIT_NAME, 0, nameField.getText());
			}
		}
	}
}
