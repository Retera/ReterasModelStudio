package com.hiveworkshop.wc3.gui;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.TabWindow;

public class GUIUtils {
	public static void bringToFront(final DockingWindow dockingWindow) {
		DockingWindow lastWindow = dockingWindow;
		DockingWindow windowParent = dockingWindow.getWindowParent();
		while (windowParent != null) {
			if (windowParent instanceof TabWindow) {
				final TabWindow window = (TabWindow) windowParent;
				window.setSelectedTab(window.getChildWindowIndex(lastWindow));
			}
			lastWindow = windowParent;
			windowParent = windowParent.getWindowParent();
		}
	}

	public static void disposeAnyJOptionPanes() {
		final Window[] windows = Window.getWindows();
		for (final Window window : windows) {
			if (window instanceof JDialog) {
				final JDialog dialog = (JDialog) window;
				if ((dialog.getContentPane().getComponentCount() == 1)
						&& (dialog.getContentPane().getComponent(0) instanceof JOptionPane)) {
					dialog.dispose();
				}
			}
		}
	}
}
