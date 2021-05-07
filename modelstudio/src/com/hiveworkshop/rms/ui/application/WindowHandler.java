package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.*;

import javax.swing.*;
import java.awt.*;

public class WindowHandler {
	protected static final boolean OLDMODE = false;

	static DockingWindowListener getDockingWindowListener2(Runnable fixit) {
		return new DockingWindowAdapter() {

			@Override
			public void windowUndocking(final DockingWindow removedWindow) {
				if (OLDMODE) {
					setTitleBarVisibility(removedWindow, true, ": (windowUndocking removedWindow as view) title bar visible now");
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}

			@Override
			public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
				if (OLDMODE) {
					if (removedFromWindow instanceof TabWindow) {
						setTitleBarVisibility(removedWindow, true, ": (removedWindow as view) title bar visible now");
						final TabWindow tabWindow = (TabWindow) removedFromWindow;
						if (tabWindow.getChildWindowCount() == 1) {
							final DockingWindow childWindow = tabWindow.getChildWindow(0);
							setTitleBarVisibility(childWindow, true, ": (singleChildView, windowRemoved()) title bar visible now");
						} else if (tabWindow.getChildWindowCount() == 0) {
							System.out.println(tabWindow.getTitle() + ": force close because 0 child windows in windowRemoved()");
							//						tabWindow.close();
						}
					}
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}

			@Override
			public void windowClosing(final DockingWindow closingWindow) {
				if (OLDMODE) {
					if (closingWindow.getWindowParent() instanceof TabWindow) {
						setTitleBarVisibility(closingWindow, true, ": (closingWindow as view) title bar visible now");
						final TabWindow tabWindow = (TabWindow) closingWindow.getWindowParent();
						if (tabWindow.getChildWindowCount() == 1) {
							final DockingWindow childWindow = tabWindow.getChildWindow(0);
							setTitleBarVisibility(childWindow, true, ": (singleChildView, windowClosing()) title bar visible now");
						} else if (tabWindow.getChildWindowCount() == 0) {
							System.out.println(tabWindow.getTitle() + ": force close because 0 child windows in windowClosing()");
							tabWindow.close();
						}
					}
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}

			@Override
			public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
				if (OLDMODE) {
					if (addedToWindow instanceof TabWindow) {
						final TabWindow tabWindow = (TabWindow) addedToWindow;
						if (tabWindow.getChildWindowCount() == 2) {
							for (int i = 0; i < 2; i++) {
								final DockingWindow childWindow = tabWindow.getChildWindow(i);
								setTitleBarVisibility(childWindow, false, ": (singleChildView as view, windowAdded()) title bar NOT visible now");
							}
						}
						setTitleBarVisibility(addedWindow, false, ": (addedWindow as view) title bar NOT visible now");
					}
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}
		};
	}

	static DockingWindowListener getDockingWindowListener(final MainPanel mainPanel) {
		return new DockingWindowAdapter() {
			@Override
			public void windowUndocked(final DockingWindow dockingWindow) {
				SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
					if (dockingWindow instanceof View) {
						final Component component = ((View) dockingWindow).getComponent();
						if (component instanceof JComponent) {
							MainPanelLinkActions.linkActions(mainPanel, ((JComponent) component).getRootPane());
						}
					}
				}));
			}
		};
	}

	private static void setTitleBarVisibility(DockingWindow removedWindow, boolean setVisible, String s) {
		if (removedWindow instanceof View) {
			final View view = (View) removedWindow;
			view.getViewProperties().getViewTitleBarProperties().setVisible(setVisible);
			System.out.println(view.getTitle() + s);
		}
	}

	public static void resetView(MainPanel mainPanel) {
		traverseAndReset(mainPanel.rootWindow);
		final TabWindow startupTabWindow = MainLayoutCreator.createMainLayout(mainPanel);
		startupTabWindow.setSelectedTab(0);
		mainPanel.rootWindow.setWindow(startupTabWindow);
		ModelLoader.setCurrentModel(mainPanel, ProgramGlobals.getCurrentModelPanel());
		mainPanel.rootWindow.revalidate();
		traverseAndFix(mainPanel.rootWindow);
	}

	public static void traverseAndReset(final DockingWindow window) {
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndReset(childWindow);
			if (childWindow instanceof View) {
				final View view = (View) childWindow;
				view.getViewProperties().getViewTitleBarProperties().setVisible(true);
			}
		}
	}

	public static void traverseAndReset(final DockingWindow window, Vec3 color) {
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndReset(childWindow, Vec3.getSum(color, new Vec3(.1, .1, .1)));
			if (childWindow instanceof View) {
				final View view = (View) childWindow;
				view.getViewProperties().getViewTitleBarProperties().setVisible(true);
				view.setBackground(color.asIntColor());
			}
		}
	}

	public static void traverseAndFix(final DockingWindow window) {
		final boolean tabWindow = window instanceof TabWindow;
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndFix(childWindow);
			if (tabWindow && (childWindowCount != 1) && (childWindow instanceof View)) {
				final View view = (View) childWindow;
				view.getViewProperties().getViewTitleBarProperties().setVisible(false);
			}
		}
	}
}
