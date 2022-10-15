package com.hiveworkshop.rms.ui.application.windowStuff;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import net.infonode.docking.*;

import javax.swing.*;
import java.awt.*;

public class RootWindowListener extends DockingWindowAdapter {
	private final DockingWindow window;

	public RootWindowListener(DockingWindow window){
		this.window = window;
	}
	@Override
	public void windowUndocking(final DockingWindow removedWindow) {
//		System.out.println("windowUndocking: " + removedWindow.getTitle());
		SwingUtilities.invokeLater(() -> fixit(window));
		SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
			setUpKeyBindings(removedWindow);
		}));
	}

	@Override
	public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
//		System.out.println("windowRemoved: " + removedWindow.getTitle() + " from " + removedFromWindow.getTitle());
		SwingUtilities.invokeLater(() -> fixit(window));
	}

	@Override
	public void windowClosing(final DockingWindow closingWindow) {
//		System.out.println("windowClosing: " + closingWindow.getTitle());
		SwingUtilities.invokeLater(() -> fixit(window));
	}

	@Override
	public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
//		System.out.println("windowAdded: " + addedWindow.getTitle() + " to " + addedToWindow.getTitle());

		SwingUtilities.invokeLater(() -> fixit(window));
	}

	private static void setUpKeyBindings(DockingWindow removedWindow) {
		KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
		if (removedWindow instanceof View) {
			final Component component = ((View) removedWindow).getComponent();
			if (component instanceof JComponent) {
				JRootPane rootPane = ((JComponent) component).getRootPane();
//							mainPanel.linkActions(rootPane);
				rootPane.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
				rootPane.setActionMap(keyBindingPrefs.getActionMap());
			}
		}
	}

	public static void fixit(DockingWindow window) {
//		traverseAndReset(window);
//		traverseAndFix(window);
		traverseAndFix2(window);
	}

	public static void traverseAndReset(DockingWindow window) {
//		System.out.println("WindowHandler2#traverseAndReset");
		int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			DockingWindow childWindow = window.getChildWindow(i);

			traverseAndReset(childWindow);
			if (childWindow instanceof View) {
				((View) childWindow).getViewProperties().getViewTitleBarProperties().setVisible(true);
			}
		}
	}


	public static void traverseAndFix(final DockingWindow window) {
//		System.out.println("WindowHandler2#traverseAndFix - " + window.getTitle());
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndFix(childWindow);

			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());

			if(childWindow instanceof SplitWindow){
				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
			}

			if (window instanceof TabWindow && (childWindowCount != 1) && (childWindow instanceof View)) {
				System.out.println(window.getTitle() + " was TabWin, invis titlebar: " + childWindow.getTitle());
				((View) childWindow).getViewProperties().getViewTitleBarProperties().setVisible(false);
			}
		}
	}
	public static void traverseAndFix2(final DockingWindow window) {
//		System.out.println("WindowHandler2#traverseAndFix - " + window.getTitle());
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndFix2(childWindow);

			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());

			if(childWindow instanceof SplitWindow){
				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
			}
			if (childWindow instanceof View) {
				if (window instanceof TabWindow && childWindowCount != 1) {
					System.out.println(window.getTitle() + " was TabWin, invis titlebar: " + childWindow.getTitle());
					((View) childWindow).getViewProperties().getViewTitleBarProperties().setVisible(false);
				} else {
					((View) childWindow).getViewProperties().getViewTitleBarProperties().setVisible(true);
				}
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

			if(childWindow instanceof SplitWindow){
				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
			}
		}
	}

	public static void traverseAndRemoveNull(final DockingWindow window) {
		final int childWindowCount = window.getChildWindowCount();
		traverseAndFix(window);
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
//			traverseAndFix(childWindow);

			int length = childWindow.getComponents().length;
			for(int j = length; j > 0; j--){
				if(childWindow.getComponent(j-1) == null){
					childWindow.remove(j-1);
				}
			}
		}
	}
}
