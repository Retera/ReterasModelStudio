package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.SaveProfileNew;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class RecentMenu extends JMenu {
	List<RecentItem> recentItems = new ArrayList<>();

	public RecentMenu() {
		super("Open Recent");
		setMnemonic(KeyEvent.VK_R);
		getAccessibleContext().setAccessibleDescription("Allows you to access recently opened files.");

		add(new JSeparator());
		add(createMenuItem("Clear", KeyEvent.VK_C, e -> clearRecent()));
	}

	public void updateRecent() {
		for (RecentItem recentItem : recentItems) {
			remove(recentItem);
		}
		recentItems.clear();

		List<File> recent = new ArrayList<>(SaveProfileNew.get().getRecent().getFiles());

		for (int i = 0; i < recent.size(); i++) {
			File file = recent.get(recent.size() - i - 1);
			if (recentItems.size() <= i || !recentItems.get(i).samePath(file.getPath())) {
				RecentItem item = new RecentItem(file);
				recentItems.add(item);
				add(item, getItemCount() - 2);
			}
		}
	}

	static class RecentItem extends JMenuItem {
		private final File file;
		public RecentItem(File file) {
			super(file.getName());
			this.file = file;
			addActionListener(e -> openFile());
		}

		private void openFile(){
			new FileDialog().openFile(file);
		}
		public String getFilepath() {
			return file.getPath();
		}
		public boolean samePath(String filepath){
			return file.getPath().equals(filepath);
		}

		public File getFile() {
			return file;
		}
	}

	public static void clearRecent() {
		int dialogResult = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				"Are you sure you want to clear the Recent history?", "Confirm Clear",
				JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			SaveProfileNew.get().clearRecent();
			ProgramGlobals.getMenuBar().updateRecent();
		}
	}
}
