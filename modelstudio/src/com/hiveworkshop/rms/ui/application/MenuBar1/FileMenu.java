package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class FileMenu extends JMenu {

	public FileMenu(RecentMenu recentMenu) {
		super("File");
		setMnemonic(KeyEvent.VK_F);
		getAccessibleContext().setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");

		FileDialog fileDialog = new FileDialog();

		add(new CreateNewModel().setMenuItemMnemonic(KeyEvent.VK_N).getMenuItem());
		add(new File.Open().setMenuItemMnemonic(KeyEvent.VK_O).getMenuItem());

		add(recentMenu);

		JMenu fetch = createMenu("Open Internal", KeyEvent.VK_F);
		add(fetch);

		fetch.add(new OpenInternalUnit().setMenuItemMnemonic(KeyEvent.VK_U).getMenuItem());
		fetch.add(new OpenInternalModel().setMenuItemMnemonic(KeyEvent.VK_M).getMenuItem());
		fetch.add(new OpenInternalObject().setMenuItemMnemonic(KeyEvent.VK_O).getMenuItem());
		fetch.add(new OpenInternalDoodad().setMenuItemMnemonic(KeyEvent.VK_D).getMenuItem());
		fetch.add(new OpenInternalDestructible().setMenuItemMnemonic(KeyEvent.VK_E).getMenuItem());

		fetch.add(new JSeparator());

		JCheckBoxMenuItem fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
		fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
		fetchPortraitsToo.addActionListener(e -> ProgramGlobals.getPrefs().setLoadPortraits(fetchPortraitsToo.isSelected()));
		fetch.add(fetchPortraitsToo);
		fetchPortraitsToo.setSelected(ProgramGlobals.getPrefs().isLoadPortraits());

		add(new JSeparator());

		JMenu importMenu = createMenu("Import", KeyEvent.VK_I);
		add(importMenu);

		importMenu.add(new ImportFromFile().setMenuItemMnemonic(KeyEvent.VK_I).getMenuItem());
		importMenu.add(new ImportFromUnit().setMenuItemMnemonic(KeyEvent.VK_U).getMenuItem());

		importMenu.add(new ImportWC3Model().setMenuItemMnemonic(KeyEvent.VK_M).getMenuItem());
		importMenu.add(new ImportFromObjectEditor().setMenuItemMnemonic(KeyEvent.VK_O).getMenuItem());
		importMenu.add(new ImportFromWorkspace().setMenuItemMnemonic(KeyEvent.VK_W).getMenuItem());


		add(new File.Save().setMenuItemMnemonic(KeyEvent.VK_S).getMenuItem());
		add(new File.SaveAs().setMenuItemMnemonic(KeyEvent.VK_A).getMenuItem());

		add(new JSeparator());

//		add(createMenuItem("Export Material as Texture", KeyEvent.VK_E, e -> ExportTexture.exportMaterialAsTextures()));
//		add(createMenuItem("Export Texture", KeyEvent.VK_E, e -> ExportTexture.exportTextures()));
		add(new ExportTexture().setMenuItemMnemonic(KeyEvent.VK_E).getMenuItem());

		add(new JSeparator());

//		add(createMenuItem("Revert", -1, e -> ModelLoader.revert()));
		add(new Revert().getMenuItem());
//		add(createMenuItem("Close", KeyEvent.VK_E, KeyStroke.getKeyStroke("control E"), e -> MenuBarActions.closeModelPanel()));
		add(new CloseModel().setMenuItemMnemonic(KeyEvent.VK_C).getMenuItem());

		add(new JSeparator());

		add(createMenuItem("Exit", KeyEvent.VK_E, e -> closeProgram()));
	}
//	public FileMenu(RecentMenu recentMenu) {
//		super("File");
//		setMnemonic(KeyEvent.VK_F);
//		getAccessibleContext().setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");
//
//		FileDialog fileDialog = new FileDialog();
//
//		add(createMenuItem("New", KeyEvent.VK_N, KeyStroke.getKeyStroke("control N"), e -> MenuBarActions.newModel()));
//		add(createMenuItem("Open", KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> fileDialog.onClickOpen()));
//
//		add(recentMenu);
//
//		JMenu fetch = createMenu("Open Internal", KeyEvent.VK_F);
//		add(fetch);
//
//		fetch.add(createMenuItem("Unit", KeyEvent.VK_U, KeyStroke.getKeyStroke("control U"), e -> InternalFileLoader.fetchUnit()));
//		fetch.add(createMenuItem("Model", KeyEvent.VK_M, KeyStroke.getKeyStroke("control M"), e -> InternalFileLoader.fetchModel()));
//		fetch.add(createMenuItem("Object Editor", KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> InternalFileLoader.fetchObject()));
//
//		fetch.add(new JSeparator());
//
//		JCheckBoxMenuItem fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
//		fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
//		fetchPortraitsToo.addActionListener(e -> ProgramGlobals.getPrefs().setLoadPortraits(fetchPortraitsToo.isSelected()));
//		fetch.add(fetchPortraitsToo);
//		fetchPortraitsToo.setSelected(ProgramGlobals.getPrefs().isLoadPortraits());
//
//		add(new JSeparator());
//
//		JMenu importMenu = createMenu("Import", KeyEvent.VK_I);
//		add(importMenu);
//
//		importMenu.add(createMenuItem("From File", KeyEvent.VK_I, KeyStroke.getKeyStroke("control shift I"), e -> ImportFileActions.importButtonActionRes()));
//		importMenu.add(createMenuItem("From Unit", KeyEvent.VK_U, KeyStroke.getKeyStroke("control shift U"), e -> ImportFileActions.importUnitActionRes()));
//		importMenu.add(createMenuItem("From WC3 Model", KeyEvent.VK_M, e -> ImportFileActions.importGameModelActionRes()));
//		importMenu.add(createMenuItem("From Object Editor", KeyEvent.VK_O, e -> ImportFileActions.importGameObjectActionRes()));
//		importMenu.add(createMenuItem("From Workspace", KeyEvent.VK_O, e -> ImportFileActions.importFromWorkspaceActionRes()));
//
//
//		add(createMenuItem("Save", KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"), e -> fileDialog.onClickSave()));
//		add(createMenuItem("Save as", KeyEvent.VK_A, KeyStroke.getKeyStroke("control Q"), e -> fileDialog.onClickSaveAs()));
//
//		add(new JSeparator());
//
//		add(createMenuItem("Export Material as Texture", KeyEvent.VK_E, e -> ExportTextureDialog.exportMaterialAsTextures()));
//		add(createMenuItem("Export Texture", KeyEvent.VK_E, e -> ExportTextureDialog.exportTextures()));
//
//		add(new JSeparator());
//
//		add(createMenuItem("Revert", -1, e -> ModelLoader.revert()));
//		add(createMenuItem("Close", KeyEvent.VK_E, KeyStroke.getKeyStroke("control E"), e -> MenuBarActions.closeModelPanel()));
//
//		add(new JSeparator());
//
//		add(createMenuItem("Exit", KeyEvent.VK_E, e -> closeProgram()));
//	}

	private void closeProgram() {
		if (closeAll()) {
			MainFrame.frame.dispose();
		}
	}

	public static boolean closeAll() {
		boolean success = true;
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		ModelPanel lastUnclosedModelPanel = null;
		for (int i = modelPanels.size() - 1; i >= 0; i--) {
			ModelPanel panel = modelPanels.get(i);
			if (success = panel.close()) {
				if (MenuBar.windowMenu != null) {
					MenuBar.windowMenu.remove(panel.getMenuItem());
				}
				ProgramGlobals.removeModelPanel(panel);
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (ProgramGlobals.getCurrentModelPanel() == null && lastUnclosedModelPanel != null) {
			ModelLoader.setCurrentModel(lastUnclosedModelPanel);
		}
		return success;
	}
}
