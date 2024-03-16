package com.hiveworkshop.rms.ui.application.windowStuff;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import net.infonode.docking.*;

import javax.swing.*;

public class RootWindowListener extends DockingWindowAdapter {
	private final DockingWindow window;

	public RootWindowListener(DockingWindow window) {
		this.window = window;
	}
	@Override
	public void windowUndocking(final DockingWindow removedWindow) {
//		System.out.println("windowUndocking: " + removedWindow.getTitle());
		doFix = true;
		SwingUtilities.invokeLater(() -> fixit(window));
		SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> setUpKeyBindings(removedWindow)));
	}

	@Override
	public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
//		System.out.println("windowRemoved: [" + removedWindow.getTitle() + "] from [" + removedFromWindow.getTitle() + "]");
		doFix = true;
		SwingUtilities.invokeLater(() -> fixit(window));
	}

	@Override
	public void windowClosing(final DockingWindow closingWindow) {
//		System.out.println("windowClosing: " + closingWindow.getTitle());
		doFix = true;
		SwingUtilities.invokeLater(() -> fixit(window));
	}

	@Override
	public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
//		System.out.println("windowAdded: [" + addedWindow.getTitle() + "] to [" + addedToWindow.getTitle() + "], rootW: [" + addedWindow.getRootWindow() + "]");
//		System.out.println("TopLevelAncestor: " + addedWindow.getTopLevelAncestor());
		if (addedWindow.getTopLevelAncestor() instanceof JFrame frame) {
			frame.setIconImage(RMSIcons.MAIN_PROGRAM_ICON);
		}

		doFix = true;
		SwingUtilities.invokeLater(() -> fixit(window));
	}

	private static void setUpKeyBindings(DockingWindow removedWindow) {
		if (removedWindow != null) {
			ProgramGlobals.linkActions(removedWindow);
		}
	}

	static boolean doFix = false;
	public static void fixit(DockingWindow window) {
//		System.out.println("fixit, doFix: " + doFix);
		if (doFix) {
			traverseAndFix(window);
			doFix = false;
		}
	}

	/**
	 * Set title bars to not display under tab for TabWindow
	 */
	public static void traverseAndFix(final DockingWindow window) {
//		System.out.println("WindowHandler2#traverseAndFix - " + window.getTitle());
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndFix(childWindow);

			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());

			if (childWindow instanceof SplitWindow splitWindow) {
				splitWindow.getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
			}
			if (childWindow instanceof View childView) {
//				System.out.println(window.getTitle() + " was TabWin, "
//						+ "title: " + childWindow.getTitle()
//						+ ", title Invis: " + (!(window instanceof TabWindow) || childWindowCount == 1));
				childView.getViewProperties().getViewTitleBarProperties().setVisible(!(window instanceof TabWindow) || childWindowCount == 1);
			}

		}
	}


	public static void traverseAndSetLock(final DockingWindow window) {
//		System.out.println("WindowHandler2#traverseAndSetLock");
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndSetLock(childWindow);

			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());

			if (childWindow instanceof SplitWindow) {
				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
			}
		}
	}

	public static void traverseAndRemoveNull(final DockingWindow window) {
		final int childWindowCount = window.getChildWindowCount();
//		traverseAndFix(window);
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);

			int length = childWindow.getComponents().length;
			for (int j = length; 0 < j; j--) {
				if (childWindow.getComponent(j - 1) == null) {
					childWindow.remove(j - 1);
				}
			}
		}
	}
}
