//package com.hiveworkshop.rms.ui.application;
//
//import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
//import com.hiveworkshop.rms.util.Vec3;
//import net.infonode.docking.*;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class WindowHandler {
//	protected static final boolean OLDMODE = false;
//
//	static DockingWindowListener getDockingWindowListener2(Runnable fixit) {
//		return new DockingWindowAdapter() {
//
//			@Override
//			public void windowUndocking(final DockingWindow removedWindow) {
//				if (OLDMODE) {
//					setTitleBarVisibility(removedWindow, true, ": (windowUndocking removedWindow as view) title bar visible now");
//				} else {
//					SwingUtilities.invokeLater(fixit);
//				}
//			}
//
//			@Override
//			public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
//				if (OLDMODE) {
//					if (removedFromWindow instanceof TabWindow) {
//						setTitleBarVisibility(removedWindow, true, ": (removedWindow as view) title bar visible now");
//						final TabWindow tabWindow = (TabWindow) removedFromWindow;
//						if (tabWindow.getChildWindowCount() == 1) {
//							final DockingWindow childWindow = tabWindow.getChildWindow(0);
//							setTitleBarVisibility(childWindow, true, ": (singleChildView, windowRemoved()) title bar visible now");
//						} else if (tabWindow.getChildWindowCount() == 0) {
//							System.out.println(tabWindow.getTitle() + ": force close because 0 child windows in windowRemoved()");
//							//						tabWindow.close();
//						}
//					}
//				} else {
//					SwingUtilities.invokeLater(fixit);
//				}
//			}
//
//			@Override
//			public void windowClosing(final DockingWindow closingWindow) {
//				if (OLDMODE) {
//					if (closingWindow.getWindowParent() instanceof TabWindow) {
//						setTitleBarVisibility(closingWindow, true, ": (closingWindow as view) title bar visible now");
//						final TabWindow tabWindow = (TabWindow) closingWindow.getWindowParent();
//						if (tabWindow.getChildWindowCount() == 1) {
//							final DockingWindow childWindow = tabWindow.getChildWindow(0);
//							setTitleBarVisibility(childWindow, true, ": (singleChildView, windowClosing()) title bar visible now");
//						} else if (tabWindow.getChildWindowCount() == 0) {
//							System.out.println(tabWindow.getTitle() + ": force close because 0 child windows in windowClosing()");
//							tabWindow.close();
//						}
//					}
//				} else {
//					SwingUtilities.invokeLater(fixit);
//				}
//			}
//
//			@Override
//			public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
//				if (OLDMODE) {
//					if (addedToWindow instanceof TabWindow) {
//						final TabWindow tabWindow = (TabWindow) addedToWindow;
//						if (tabWindow.getChildWindowCount() == 2) {
//							for (int i = 0; i < 2; i++) {
//								final DockingWindow childWindow = tabWindow.getChildWindow(i);
//								setTitleBarVisibility(childWindow, false, ": (singleChildView as view, windowAdded()) title bar NOT visible now");
//							}
//						}
//						setTitleBarVisibility(addedWindow, false, ": (addedWindow as view) title bar NOT visible now");
//					}
//				} else {
//					SwingUtilities.invokeLater(fixit);
//				}
//			}
//		};
//	}
//
//	static DockingWindowListener getDockingWindowListener() {
//		return new DockingWindowAdapter() {
//			@Override
//			public void windowUndocked(final DockingWindow dockingWindow) {
//				SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
//					KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
//					if (dockingWindow instanceof View) {
//						final Component component = ((View) dockingWindow).getComponent();
//						if (component instanceof JComponent) {
//							JRootPane rootPane = ((JComponent) component).getRootPane();
////							mainPanel.linkActions(rootPane);
//							rootPane.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
//							rootPane.setActionMap(keyBindingPrefs.getActionMap());
//						}
//					}
//				}));
//			}
//		};
//	}
//
//	private static void setTitleBarVisibility(DockingWindow removedWindow, boolean setVisible, String s) {
//		if (removedWindow instanceof View) {
//			final View view = (View) removedWindow;
//			view.getViewProperties().getViewTitleBarProperties().setVisible(setVisible);
//			System.out.println(view.getTitle() + s);
//		}
//	}
//
//	public static void resetView() {
//		RootWindowUgg rootWindow = ProgramGlobals.getRootWindowUgg();
//		traverseAndReset(rootWindow);
////		final TabWindow startupTabWindow = MainLayoutCreator.createMainLayout();
////		final TabWindow startupTabWindow = mainPanel.getMainLayoutCreator().getStartupTabWindow();
//		final TabWindow startupTabWindow = rootWindow.getWindowHandler2().getStartupTabWindow();
//		startupTabWindow.setSelectedTab(0);
//		rootWindow.setWindow(startupTabWindow);
//		ModelLoader.setCurrentModel(ProgramGlobals.getCurrentModelPanel());
//		rootWindow.revalidate();
//		traverseAndFix(rootWindow);
//
////		MainPanel mainPanel = ProgramGlobals.getMainPanel();
////		traverseAndReset(mainPanel.rootWindow);
//////		final TabWindow startupTabWindow = MainLayoutCreator.createMainLayout();
//////		final TabWindow startupTabWindow = mainPanel.getMainLayoutCreator().getStartupTabWindow();
////		final TabWindow startupTabWindow = mainPanel.getWindowHandler2().getStartupTabWindow();
////		startupTabWindow.setSelectedTab(0);
////		mainPanel.rootWindow.setWindow(startupTabWindow);
////		ModelLoader.setCurrentModel(ProgramGlobals.getCurrentModelPanel());
////		mainPanel.rootWindow.revalidate();
////		traverseAndFix(mainPanel.rootWindow);
//	}
//
//	public static void traverseAndReset(DockingWindow window) {
//		int childWindowCount = window.getChildWindowCount();
//		for (int i = 0; i < childWindowCount; i++) {
//			DockingWindow childWindow = window.getChildWindow(i);
////			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());
////			if(childWindow instanceof SplitWindow){
////				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
////			}
//
//			traverseAndReset(childWindow);
//			if (childWindow instanceof View) {
//				View view = (View) childWindow;
//				view.getViewProperties().getViewTitleBarProperties().setVisible(true);
//			}
//		}
//	}
//
//	public static void traverseAndReset(DockingWindow window, Vec3 color) {
//		final int childWindowCount = window.getChildWindowCount();
//		for (int i = 0; i < childWindowCount; i++) {
//			final DockingWindow childWindow = window.getChildWindow(i);
//			traverseAndReset(childWindow, Vec3.getSum(color, new Vec3(.1, .1, .1)));
//			if (childWindow instanceof View) {
//				final View view = (View) childWindow;
//				view.getViewProperties().getViewTitleBarProperties().setVisible(true);
//				view.setBackground(color.asIntColor());
//			}
//		}
//	}
//
//	public static void traverseAndFix(final DockingWindow window) {
//		final boolean tabWindow = window instanceof TabWindow;
//		final int childWindowCount = window.getChildWindowCount();
//		for (int i = 0; i < childWindowCount; i++) {
//			final DockingWindow childWindow = window.getChildWindow(i);
//			traverseAndFix(childWindow);
//
//			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());
//			if(childWindow instanceof SplitWindow){
//				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
//			}
//
//			if (tabWindow && (childWindowCount != 1) && (childWindow instanceof View)) {
//				final View view = (View) childWindow;
//				view.getViewProperties().getViewTitleBarProperties().setVisible(false);
//			}
//		}
//	}
//}
