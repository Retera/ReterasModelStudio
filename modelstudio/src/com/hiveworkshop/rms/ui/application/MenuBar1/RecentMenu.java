package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.MenuBarActions;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class RecentMenu extends JMenu {
	List<RecentItem> recentItems = new ArrayList<>();
	MainPanel mainPanel;

	public RecentMenu(MainPanel mainPanel) {
		super("Open Recent");
		setMnemonic(KeyEvent.VK_R);
		getAccessibleContext().setAccessibleDescription("Allows you to access recently opened files.");

		add(new JSeparator());
		add(createMenuItem("Clear", KeyEvent.VK_C, e -> MenuBarActions.clearRecent(mainPanel)));
		this.mainPanel = mainPanel;
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
				FileDialog fileDialog = new FileDialog(mainPanel);

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
}
