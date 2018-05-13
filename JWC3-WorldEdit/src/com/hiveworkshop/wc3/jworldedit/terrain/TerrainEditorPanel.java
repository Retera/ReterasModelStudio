package com.hiveworkshop.wc3.jworldedit.terrain;

import javax.swing.JToolBar;

import com.hiveworkshop.wc3.jworldedit.AbstractWorldEditorPanel;
import com.hiveworkshop.wc3.units.DataTable;

public class TerrainEditorPanel extends AbstractWorldEditorPanel {
	public TerrainEditorPanel() {
	}

	private JToolBar createToolbar(final DataTable worldEditorData) {
		final JToolBar toolBar = new JToolBar();
		// makeButton(worldEditorData, toolBar, "newMap", "ToolBarIcon_New", "WESTRING_TOOLBAR_NEW");
		// makeButton(worldEditorData, toolBar, "openMap", "ToolBarIcon_Open", "WESTRING_TOOLBAR_OPEN");
		// final JButton saveButton = makeButton(worldEditorData, toolBar, "saveMap", "ToolBarIcon_Save",
		// "WESTRING_TOOLBAR_SAVE");
		// saveButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e) {
		// final JFileChooser jFileChooser = new JFileChooser(
		// new File(System.getProperty("user.home") + "/Documents/Warcraft III/Maps"));
		// jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// jFileChooser.setDialogTitle("Save Map");
		// if (jFileChooser.showSaveDialog(TriggerEditor.this) == JFileChooser.APPROVE_OPTION) {
		//
		// }
		//
		// }
		// });
		// toolBar.add(Box.createHorizontalStrut(8));
		// final TransferActionListener transferActionListener = new TransferActionListener();
		// copyButton = makeButton(worldEditorData, toolBar, "copy", "ToolBarIcon_Copy", "WESTRING_MENU_OE_UNIT_COPY");
		// copyButton.addActionListener(transferActionListener);
		// copyButton.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
		// pasteButton = makeButton(worldEditorData, toolBar, "paste", "ToolBarIcon_Paste",
		// "WESTRING_MENU_OE_UNIT_PASTE");
		// pasteButton.addActionListener(transferActionListener);
		// pasteButton.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
		// toolBar.add(Box.createHorizontalStrut(8));
		// createNewCategoryButton = makeButton(worldEditorData, toolBar, "createNewCategory",
		// "ToolBarIcon_SE_NewCategory", "WESTRING_TOOLBAR_SE_NEWCAT");
		// createNewCategoryButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e) {
		// final TriggerCategory category = triggerTree.getController().createCategory();
		// triggerTree.select(category);
		// triggerTree.startEditingAtPath(triggerTree.getSelectionPath());
		// }
		// });
		// createNewTriggerButton = makeButton(worldEditorData, toolBar, "createNewTrigger",
		// "ToolBarIcon_SE_NewTrigger",
		// "WESTRING_TOOLBAR_SE_NEWTRIG");
		// createNewTriggerButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e) {
		// final Trigger trigger = triggerTree.createTrigger();
		// triggerTree.select(trigger);
		// final TreePath selectionPath = triggerTree.getSelectionPath();
		// triggerTree.startEditingAtPath(selectionPath);
		// }
		// });
		// createNewCommentButton = makeButton(worldEditorData, toolBar, "createNewTriggerComment",
		// "ToolBarIcon_SE_NewTriggerComment", "WESTRING_TOOLBAR_SE_NEWTRIGCOM");
		// createNewCommentButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e) {
		// final Trigger trigger = triggerTree.createTriggerComment();
		// triggerTree.select(trigger);
		// triggerTree.startEditingAtPath(triggerTree.getSelectionPath());
		// }
		// });
		// toolBar.add(Box.createHorizontalStrut(8));
		// makeButton(worldEditorData, toolBar, "terrainEditor", "ToolBarIcon_Module_Terrain",
		// "WESTRING_MENU_MODULE_TERRAIN");
		// final JToggleButton scriptEditorButton = new JToggleButton(
		// getIcon(worldEditorData, "ToolBarIcon_Module_Script"));
		// scriptEditorButton.setToolTipText(WEString.getString("WESTRING_MENU_MODULE_SCRIPTS").replace("&", ""));
		// scriptEditorButton.setPreferredSize(new Dimension(24, 24));
		// scriptEditorButton.setMargin(new Insets(1, 1, 1, 1));
		// scriptEditorButton.setSelected(true);
		// scriptEditorButton.setEnabled(false);
		// scriptEditorButton.setDisabledIcon(scriptEditorButton.getIcon());
		// toolBar.add(scriptEditorButton);
		// makeButton(worldEditorData, toolBar, "soundEditor", "ToolBarIcon_Module_Sound",
		// "WESTRING_MENU_MODULE_SOUND");
		// makeButton(worldEditorData, toolBar, "objectEditor", "ToolBarIcon_Module_ObjectEditor",
		// "WESTRING_MENU_OBJECTEDITOR");
		// makeButton(worldEditorData, toolBar, "campaignEditor", "ToolBarIcon_Module_Campaign",
		// "WESTRING_MENU_MODULE_CAMPAIGN");
		// makeButton(worldEditorData, toolBar, "aiEditor", "ToolBarIcon_Module_AIEditor", "WESTRING_MENU_MODULE_AI");
		// makeButton(worldEditorData, toolBar, "objectEditor", "ToolBarIcon_Module_ObjectManager",
		// "WESTRING_MENU_OBJECTMANAGER");
		// makeButton(worldEditorData, toolBar, "importEditor", "ToolBarIcon_Module_ImportManager",
		// "WESTRING_MENU_IMPORTMANAGER");
		// toolBar.add(Box.createHorizontalStrut(8));
		// makeButton(worldEditorData, toolBar, "testMap",
		// new ImageIcon(IconUtils.worldEditStyleIcon(getIcon(worldEditorData, "ToolBarIcon_TestMap").getImage())),
		// "WESTRING_TOOLBAR_TESTMAP");
		return toolBar;
	}
}
