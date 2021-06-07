package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.*;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class FileMenu extends JMenu {

	public FileMenu(RecentMenu recentMenu) {
		super("File");
		setMnemonic(KeyEvent.VK_F);
		getAccessibleContext().setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");

		FileDialog fileDialog = new FileDialog();

		add(createMenuItem("New", KeyEvent.VK_N, KeyStroke.getKeyStroke("control N"), e -> MenuBarActions.newModel()));
		add(createMenuItem("Open", KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> fileDialog.onClickOpen()));

		add(recentMenu);

		JMenu fetch = createMenu("Open Internal", KeyEvent.VK_F);
		add(fetch);

		fetch.add(createMenuItem("Unit", KeyEvent.VK_U, KeyStroke.getKeyStroke("control U"), e -> InternalFileLoader.fetchUnit()));
		fetch.add(createMenuItem("Model", KeyEvent.VK_M, KeyStroke.getKeyStroke("control M"), e -> InternalFileLoader.fetchModel()));
		fetch.add(createMenuItem("Object Editor", KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> InternalFileLoader.fetchObject()));

		fetch.add(new JSeparator());

		JCheckBoxMenuItem fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
		fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
		fetchPortraitsToo.addActionListener(e -> ProgramGlobals.getPrefs().setLoadPortraits(fetchPortraitsToo.isSelected()));
		fetch.add(fetchPortraitsToo);
		fetchPortraitsToo.setSelected(ProgramGlobals.getPrefs().isLoadPortraits());

		add(new JSeparator());

		JMenu importMenu = createMenu("Import", KeyEvent.VK_I);
		add(importMenu);

		importMenu.add(createMenuItem("From File", KeyEvent.VK_I, KeyStroke.getKeyStroke("control shift I"), e -> ImportFileActions.importButtonActionRes()));
		importMenu.add(createMenuItem("From Unit", KeyEvent.VK_U, KeyStroke.getKeyStroke("control shift U"), e -> ImportFileActions.importUnitActionRes()));
		importMenu.add(createMenuItem("From WC3 Model", KeyEvent.VK_M, e -> ImportFileActions.importGameModelActionRes()));
		importMenu.add(createMenuItem("From Object Editor", KeyEvent.VK_O, e -> ImportFileActions.importGameObjectActionRes()));
		importMenu.add(createMenuItem("From Workspace", KeyEvent.VK_O, e -> ImportFileActions.importFromWorkspaceActionRes()));


		add(createMenuItem("Save", KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"), e -> fileDialog.onClickSave()));
		add(createMenuItem("Save as", KeyEvent.VK_A, KeyStroke.getKeyStroke("control Q"), e -> fileDialog.onClickSaveAs()));

		add(new JSeparator());

		add(createMenuItem("Export Material as Texture", KeyEvent.VK_E, e -> ExportTextureDialog.exportMaterialAsTextures()));
		add(createMenuItem("Export Texture", KeyEvent.VK_E, e -> ExportTextureDialog.exportTextures()));

		add(new JSeparator());

		add(createMenuItem("Revert", -1, e -> ModelLoader.revert()));
		add(createMenuItem("Close", KeyEvent.VK_E, KeyStroke.getKeyStroke("control E"), e -> MenuBarActions.closeModelPanel()));

		add(new JSeparator());

		add(createMenuItem("Exit", KeyEvent.VK_E, e -> closeProgram()));
	}

	private void closeProgram() {
		if (closeAll()) {
			MainFrame.frame.dispose();
		}
	}

	public boolean closeAll() {
		boolean success = true;
		final Iterator<ModelPanel> iterator = ProgramGlobals.getModelPanels().iterator();
		boolean closedCurrentPanel = false;
		ModelPanel lastUnclosedModelPanel = null;
		while (iterator.hasNext()) {
			final ModelPanel panel = iterator.next();
			if (success = panel.close()) {
				if (MenuBar.windowMenu != null) {
					MenuBar.windowMenu.remove(panel.getMenuItem());
				}
				iterator.remove();
				if (panel == ProgramGlobals.getCurrentModelPanel()) {
					closedCurrentPanel = true;
				}
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (closedCurrentPanel) {
			ModelLoader.setCurrentModel(lastUnclosedModelPanel);
		}
		return success;
	}
}
