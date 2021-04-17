package com.hiveworkshop.rms.ui.application;

import javax.swing.*;

public class ClosePopup {
	static JPopupMenu contextMenu;

	static void createContextMenuPopup(MainPanel mainPanel) {
		contextMenu = new JPopupMenu();
		JMenuItem contextClose = new JMenuItem("Close");
		contextClose.addActionListener(mainPanel);
		contextMenu.add(contextClose);

		JMenuItem contextCloseOthers = new JMenuItem("Close Others");
		contextCloseOthers.addActionListener(e -> MenuBarActions.closeOthers(mainPanel, mainPanel.currentModelPanel));
		contextMenu.add(contextCloseOthers);

		JMenuItem contextCloseAll = new JMenuItem("Close All");
		contextCloseAll.addActionListener(e -> MenuBar.closeAll(mainPanel));
		contextMenu.add(contextCloseAll);
	}
}
