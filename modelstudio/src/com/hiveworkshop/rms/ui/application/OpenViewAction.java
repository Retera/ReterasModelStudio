package com.hiveworkshop.rms.ui.application;

import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class OpenViewAction extends AbstractAction {
	private final Supplier<View> openViewGetter;
	private final RootWindow rootWindow;

	public OpenViewAction(RootWindow rootWindow, final String name, Supplier<View> openViewGetter) {
		super(name);
		this.openViewGetter = openViewGetter;
		this.rootWindow = rootWindow;
	}

	public static OpenViewAction getOpenViewAction(RootWindow rootWindow, String s, View view) {
		return new OpenViewAction(rootWindow, s, () -> view);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		View view = openViewGetter.get();
		if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
			FloatingWindow createFloatingWindow
					= rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
			createFloatingWindow.getTopLevelAncestor().setVisible(true);
		}
	}
}
