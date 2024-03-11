package com.hiveworkshop.rms.ui.application;

import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class FloatingWindowFactory {
	public static void openNewWindow(View view, RootWindow rootWindow) {
		if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
			FloatingWindow createFloatingWindow = rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
			createFloatingWindow.getTopLevelAncestor().setVisible(true);
		}
	}

	public static void openNewWindowWithKB(View view, RootWindow rootWindow) {
		if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
			FloatingWindow createFloatingWindow = rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
			createFloatingWindow.getTopLevelAncestor().setVisible(true);
			ProgramGlobals.linkActions(createFloatingWindow);
		}
	}

	public static void packFrame(View view) {
		JFrame frame = (JFrame) view.getTopLevelAncestor();
		if (frame != null) {
			frame.pack();
			frame.setLocationRelativeTo(ProgramGlobals.getMainPanel());
		}
	}

//	public static void openNewWindow(String title, Component component, RootWindow rootWindow) {
//		openNewWindow(title, component, null, rootWindow);
//	}
//
//	public static void openNewWindow(String title, Component component, ImageIcon icon, RootWindow rootWindow) {
//		View view = new View(title, icon, component);
//		if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
//			FloatingWindow createFloatingWindow = rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
//			createFloatingWindow.getTopLevelAncestor().setVisible(true);
//		}
//	}

}
