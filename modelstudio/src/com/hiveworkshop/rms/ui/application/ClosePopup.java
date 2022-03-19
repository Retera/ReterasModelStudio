package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.actionfunctions.CloseModel;

import javax.swing.*;

public class ClosePopup {
	static JPopupMenu contextMenu;

	static void createContextMenuPopup() {
		contextMenu = new JPopupMenu();
		JMenuItem contextClose = new JMenuItem("Close");
		contextClose.addActionListener(e -> ProgramGlobals.getUndoHandler().refreshUndo());
		contextMenu.add(contextClose);

		JMenuItem contextCloseOthers = new JMenuItem("Close Others");
		contextCloseOthers.addActionListener(e -> CloseModel.closeOthers(ProgramGlobals.getCurrentModelPanel()));
		contextMenu.add(contextCloseOthers);

		JMenuItem contextCloseAll = new JMenuItem("Close All");
		contextCloseAll.addActionListener(e -> CloseModel.closeAll());
		contextMenu.add(contextCloseAll);
	}
}
