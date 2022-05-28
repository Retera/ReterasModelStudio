package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.actionfunctions.CreateNewModel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ToolBar extends JToolBar {

	public ToolBar() {
		super(JToolBar.HORIZONTAL);
		setFloatable(false);

		add(getToolbarButton("New", "new.png", e -> CreateNewModel.newModel()));
//		System.out.println("loaded [New]");
		add(getToolbarButton("Open", "open.png", e -> new FileDialog().onClickOpen()));
		add(getToolbarButton("Save", "save.png", e -> new FileDialog().onClickSave()));

		addSeparator();

		add(getToolbarButton("Undo", "undo.png", e -> ProgramGlobals.getUndoHandler().undo()));
		add(getToolbarButton("Redo", "redo.png", e -> ProgramGlobals.getUndoHandler().redo()));

		addSeparator();
		add(getToolbarButton("Lock Layout", "lockLayout.png", e -> toggleLockLayout()));
		addSeparator();

		setMaximumSize(new Dimension(80000, 48));
	}

	private JButton getToolbarButton(String hooverText, String icon, ActionListener action) {
		JButton button = new JButton(RMSIcons.loadToolBarImageIcon(icon));
		button.setToolTipText(hooverText);
		button.addActionListener(action);
		return button;
	}

	private void toggleLockLayout() {
		ProgramGlobals.setLockLayout(!ProgramGlobals.isLockLayout());
		WindowHandler2.traverseAndFix(ProgramGlobals.getRootWindowUgg());
	}

}
