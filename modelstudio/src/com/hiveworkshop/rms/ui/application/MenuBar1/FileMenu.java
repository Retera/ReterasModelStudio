package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class FileMenu extends JMenu {
	private final RecentMenu recentMenu;

	public FileMenu() {
		super("File");
		setMnemonic(KeyEvent.VK_F);
		getAccessibleContext().setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");

		add(new CreateNewModel().setMenuItemMnemonic(KeyEvent.VK_N).getMenuItem());
		add(new File.Open().setMenuItemMnemonic(KeyEvent.VK_O).getMenuItem());

		recentMenu = new RecentMenu();
		add(recentMenu);

		JMenu fetch = createMenu("Open Internal", KeyEvent.VK_F);
		add(fetch);

		fetch.add(new OpenInternalUnit().setMenuItemMnemonic(KeyEvent.VK_U).getMenuItem());
		fetch.add(new OpenInternalModel().setMenuItemMnemonic(KeyEvent.VK_M).getMenuItem());
		fetch.add(new OpenInternalObject().setMenuItemMnemonic(KeyEvent.VK_O).getMenuItem());
		fetch.add(new OpenInternalDoodad().setMenuItemMnemonic(KeyEvent.VK_D).getMenuItem());
		fetch.add(new OpenInternalDestructible().setMenuItemMnemonic(KeyEvent.VK_E).getMenuItem());
		fetch.add(new OpenFromInternal().setMenuItemMnemonic(KeyEvent.VK_I).getMenuItem());

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

		add(new Revert().getMenuItem());
		add(new CloseModel().setMenuItemMnemonic(KeyEvent.VK_C).getMenuItem());

		add(new JSeparator());

		add(createMenuItem("Exit", KeyEvent.VK_E, e -> closeProgram()));
	}

	public RecentMenu getRecentMenu() {
		return recentMenu;
	}

	private void closeProgram() {
		if (CloseModel.closeAll()) {
			MainFrame.frame.dispose();
		}
	}
}
