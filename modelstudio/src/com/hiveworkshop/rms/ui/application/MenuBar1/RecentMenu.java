package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;

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
		List<String> recent = SaveProfile.get().getRecent();
		for (RecentItem recentItem : recentItems) {
			remove(recentItem);
		}
		recentItems.clear();
		for (int i = 0; i < recent.size(); i++) {
			final String fp = recent.get(recent.size() - i - 1);
			if ((recentItems.size() <= i) || (!recentItems.get(i).filepath.equals(fp))) {
				FileDialog fileDialog = new FileDialog();

				RecentItem item = new RecentItem(new File(fp).getName());
				item.filepath = fp;
				recentItems.add(item);
				item.addActionListener(e -> fileDialog.openFile(new File(item.filepath)));
				add(item, getItemCount() - 2);
			}
		}
	}

	static class RecentItem extends JMenuItem {
		String filepath;

		public RecentItem(final String what) {
			super(what);
		}
	}

	public static void clearRecent() {
		int dialogResult = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				"Are you sure you want to clear the Recent history?", "Confirm Clear",
				JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			SaveProfile.get().clearRecent();
			ProgramGlobals.getMenuBar().updateRecent();
		}
	}
}
