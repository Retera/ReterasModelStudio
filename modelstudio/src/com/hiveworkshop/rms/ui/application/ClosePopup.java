package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;

import javax.swing.*;

public class ClosePopup {
	static JPopupMenu contextMenu;

	static void createContextMenuPopup(MainPanel mainPanel) {
		contextMenu = new JPopupMenu();
		JMenuItem contextClose = new JMenuItem("Close");
		contextClose.addActionListener(e -> ProgramGlobals.getUndoHandler().refreshUndo());
		contextMenu.add(contextClose);

		JMenuItem contextCloseOthers = new JMenuItem("Close Others");
		contextCloseOthers.addActionListener(e -> MenuBarActions.closeOthers(mainPanel));
		contextMenu.add(contextCloseOthers);

		JMenuItem contextCloseAll = new JMenuItem("Close All");
		contextCloseAll.addActionListener(e -> MenuBar.closeAll());
		contextMenu.add(contextCloseAll);
	}
}
