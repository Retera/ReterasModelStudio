package com.hiveworkshop.rms.ui.application.windowStuff;

import net.infonode.docking.*;
import net.infonode.docking.properties.ViewTitleBarProperties;
import net.infonode.gui.SimpleSplitPane;
import net.infonode.gui.shaped.panel.ShapedPanel;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;

public class TestWindow1 extends View {

	String title;


	public TestWindow1(String s, Icon icon, Component component) {
		super(s, icon, component);
		title = s;
		addListener(getListener2(this));
//		addAncestorListener(getAncestorListener());

//		addComponentListener(getComponentListener());
//		addContainerListener(getContainerListener());
//		addHierarchyListener(getHierarchyListener());
	}

	private HierarchyListener getHierarchyListener() {
		return new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
//				if(e.getID() != HierarchyEvent.DISPLAYABILITY_CHANGED){
//					System.out.println(title + ", hierarchyChanged: " + e);
//				}
//				if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != HierarchyEvent.DISPLAYABILITY_CHANGED){
//					System.out.println(title + ", hierarchyChanged: " + e);
//				}
				if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) == HierarchyEvent.DISPLAYABILITY_CHANGED) {
					System.out.println(title + ", hierarchyChanged: " + e);
					printInfo();
				}
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED) {
					System.out.println(title + ", hierarchyChanged: " + e);
					printInfo();
				}
				if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == HierarchyEvent.PARENT_CHANGED) {
					System.out.println(title + ", hierarchyChanged: " + e);
					Container changedParent = e.getChangedParent();
					System.out.println("getChangedParent: " + changedParent);
					printInfo();
					if (changedParent instanceof ShapedPanel || changedParent instanceof SimpleSplitPane) {
						SwingUtilities.invokeLater(() -> getViewProperties().getViewTitleBarProperties().setVisible(true));
//						getViewProperties().getViewTitleBarProperties().setVisible(true);
					} else {
						SwingUtilities.invokeLater(() -> getViewProperties().getViewTitleBarProperties().setVisible(false));
//						getViewProperties().getViewTitleBarProperties().setVisible(false);
					}
				}
			}
		};
	}

	private ContainerListener getContainerListener() {
		return new ContainerListener() {
			@Override
			public void componentAdded(ContainerEvent e) {
				System.out.println(title + ", componentAdded: " + e);
				printInfo();
			}

			@Override
			public void componentRemoved(ContainerEvent e) {
				System.out.println(title + ", componentRemoved: " + e);
				printInfo();
			}
		};
	}

	private ComponentListener getComponentListener() {
		return new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println(title + ", componentResized: " + e);
				printInfo();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				System.out.println(title + ", componentMoved: " + e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				System.out.println(title + ", componentShown: " + e);
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				System.out.println(title + ", componentHidden: " + e);
			}
		};
	}

	private AncestorListener getAncestorListener() {
		return new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				System.out.println(title + ", ancestorAdded: " + event);
				printInfo();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				System.out.println(title + ", ancestorRemoved: " + event);
				printInfo();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
//				System.out.println(title + ", ancestorMoved: " + event);
			}
		};
	}

	private void printInfo(){
		String parent = "null";
		String parentP = "null";
		String windParent = "null";
		String windParentP = "null";
		String windParentWP = "null";
		Container p1 = getParent();
		if(p1 != null){
			parent = p1.getClass().getSimpleName();
			Container p2 = p1.getParent();
			if (p2 != null){
				parentP = p2.getClass().getSimpleName();
			}
		}
		DockingWindow wp1 = getWindowParent();
		if(wp1 != null){
			windParent = wp1.getClass().getSimpleName();
			Container wp2 = wp1.getParent();
			if (wp2 != null){
				parentP = wp2.getClass().getSimpleName();
			}
			DockingWindow wwp2 = wp1.getWindowParent();
			if (wwp2 != null){
				windParentWP = wwp2.getClass().getSimpleName();
			}
		}

		System.out.println(title + " par: " + parent
				+ ", parpar: " + parentP
				+ ", windParent: " + windParent
				+ ", windParentP: " + windParentP
				+ ", windParentWP: " + windParentWP);
	}
	private void printInfo(String pre){
		String parent = "null";
		String parentP = "null";
		String windParent = "null";
		String windParentP = "null";
		String windParentWP = "null";
		Container p1 = getParent();
		if(p1 != null){
			parent = p1.getClass().getSimpleName();
			Container p2 = p1.getParent();
			if (p2 != null){
				parentP = p2.getClass().getSimpleName();
			}
		}
		DockingWindow wp1 = getWindowParent();
		if(wp1 != null){
			windParent = wp1.getClass().getSimpleName();
			Container wp2 = wp1.getParent();
			if (wp2 != null){
				parentP = wp2.getClass().getSimpleName();
			}
			DockingWindow wwp2 = wp1.getWindowParent();
			if (wwp2 != null){
				windParentWP = wwp2.getClass().getSimpleName();
			}
		}

		System.out.println(pre + " \t" + title + "\tpar: " + parent
//				+ ",\tchildInsideTab: " + childInsideTab()
//				+ ",\tinsideTab: " + insideTab()
				+ ",\tparpar: " + parentP
				+ ",\twindParent: " + windParent
				+ ",\twindParentWP: " + windParentWP
				+ ",\twindParentP: " + windParentP);
	}

	private void ugg(){
//		this.showsWindowTitle();
	}

	private DockingWindowAdapter getListener2(TestWindow1 testWindow){
		return new DockingWindowAdapter(){
			@Override
			public void windowUndocking(final DockingWindow removedWindow) {
				System.out.println(testWindow + " windowUndocking: " + removedWindow.getTitle());
				testWindow.printInfo("windowUndocking");
			}

			@Override
			public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
				System.out.println(testWindow
						+ " windowRemoved: \"" + removedWindow.getTitle()
						+ "\" ("
						+ removedFromWindow.getClass().getSimpleName()
						+ ") from \""
						+ removedFromWindow.getTitle()
						+ "\" (" + removedFromWindow.getClass().getSimpleName() + ")");
				testWindow.printInfo("windowRemoved");
			}

			@Override
			public void windowClosing(final DockingWindow closingWindow) {
				System.out.println(testWindow + " windowClosing: " + closingWindow.getTitle());
				testWindow.printInfo("windowClosing");
			}

			@Override
			public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
				System.out.println(testWindow + " windowAdded: \"" + addedWindow.getTitle()
						+ "\" ("
						+ addedWindow.getClass().getSimpleName()
						+ ") to \"" + addedToWindow.getTitle()
						+ "\" (" + addedToWindow.getClass().getSimpleName() + ")");
				testWindow.printInfo("windowAdded");
			}

			public void windowShown(DockingWindow var1) {
				Container parent = var1.getParent();
				DockingWindow windowParent = var1.getWindowParent();
//				System.out.println(testWindow + " windowShown: " + var1.getTitle() + ", parent: " + parent.getClass().getSimpleName() + ", W_parent: " + windowParent);
				testWindow.printInfo("windowShown");
			}

			public void windowHidden(DockingWindow var1) {
//				System.out.println(testWindow + " windowHidden: " + var1.getTitle());
				testWindow.printInfo("windowHidden");
			}

			public void viewFocusChanged(View var1, View var2) {
//				System.out.println("viewFocusChanged: " + var1.getTitle() + " to? " + var2.getTitle());
//				System.out.println(testWindow + " viewFocusChanged: " + var1 + " to? " + var2);
				testWindow.printInfo("viewFocusChanged");
			}

			public void windowClosed(DockingWindow var1) {
//				System.out.println(testWindow + " windowClosed: " + var1.getTitle());
				testWindow.printInfo("windowClosed");
			}

			public void windowUndocked(DockingWindow var1) {
				Container parent = var1.getParent();
				testWindow.printInfo("windowUndocked");
			}

			public void windowDocking(DockingWindow var1) throws OperationAbortedException {
				testWindow.printInfo("windowDocking");
			}

			public void windowDocked(DockingWindow var1) {
				Container parent = var1.getParent();
				testWindow.printInfo("windowDocked");
			}

			public void windowMinimized(DockingWindow var1) {
//				System.out.println(testWindow + " windowMinimized: " + var1.getTitle());
				testWindow.printInfo("windowMinimized");
			}

			public void windowMaximized(DockingWindow var1) {
//				System.out.println(testWindow + " windowMaximized: " + var1.getTitle());
				testWindow.printInfo("windowMaximized");
			}

			public void windowRestored(DockingWindow var1) {
//				System.out.println(testWindow + " windowRestored: " + var1.getTitle());
				testWindow.printInfo("windowRestored");
			}

			public void windowMaximizing(DockingWindow var1) throws OperationAbortedException {
//				System.out.println(testWindow + " windowMaximizing: " + var1.getTitle());
				testWindow.printInfo("windowUndocking");
			}

			public void windowMinimizing(DockingWindow var1) throws OperationAbortedException {
//				System.out.println(testWindow + " windowMinimizing: " + var1.getTitle());
				testWindow.printInfo("windowMaximizing");
			}

			public void windowRestoring(DockingWindow var1) throws OperationAbortedException {
//				System.out.println(testWindow + " windowRestoring: " + var1.getTitle());
				testWindow.printInfo("windowRestoring");
			}
		};
	}
	private DockingWindowAdapter getListener(TestWindow1 testWindow){
		return new DockingWindowAdapter(){
			@Override
			public void windowUndocking(final DockingWindow removedWindow) {
				System.out.println(testWindow + " windowUndocking: " + removedWindow.getTitle());
				testWindow.printInfo("windowUndocking");
			}

			@Override
			public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
				System.out.println(testWindow + " windowRemoved: \"" + removedWindow.getTitle() + "\" from \"" + removedFromWindow.getTitle() + "\"");
				testWindow.printInfo("windowRemoved");
			}

			@Override
			public void windowClosing(final DockingWindow closingWindow) {
//				System.out.println(testWindow + " windowClosing: " + closingWindow.getTitle());
				testWindow.printInfo("windowClosing");
			}

			@Override
			public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
				System.out.println(testWindow + " windowAdded: \"" + addedWindow.getTitle()
						+ "\" to \"" + addedToWindow.getTitle()
						+ "\", " + addedToWindow.getClass().getSimpleName());
				testWindow.printInfo("windowAdded");

//				Container parent = addedToWindow.getParent();
//				if(parent != null){
//					presentWindow(testWindow, addedToWindow, parent, "Added");
//				} else {
//					if(testWindow.getParent() != null && testWindow.getWindowParent() != null){
//
//						System.out.println("\tnull1-tw par: \"" + testWindow.getParent().getClass().getSimpleName()
//								+ "\", winPar: \"" + testWindow.getWindowParent().getClass().getSimpleName() + "\"");
//						System.out.println("\t\tnull1-tw par: \""
//								+ testWindow.getParent().getParent()
//								+ "\", winPar: \"" + testWindow.getWindowParent().getWindowParent() + "\""
//								+ "\", winParC: \"" + testWindow.getWindowParent().getParent() + "\"");
//					} else {
//						System.out.println("\tnull2-tw par: \"" + testWindow.getParent() + "\", winPar: \"" + testWindow.getWindowParent() + "\"");
//					}
//				}
//				DockingWindow windowParent = addedToWindow.getWindowParent();
////				if(windowParent != null) {
////					presentWindowParent(windowParent, "wa-");
////				}
//
//				if(windowParent != null){
//					presentWindowParent(windowParent, "wa-");
//					if (addedToWindow instanceof View && windowParent instanceof TabWindow) {
////						boolean showTitle = windowParent.getWindowParent() != null && (windowParent.getWindowParent() instanceof SplitWindow);
////						setTitleBar(var1, windowParent, "Dock - ", showTitle);
////						setTitleBar(var1, windowParent, "Dock - ", false);
////						((View) var1).getViewProperties().getViewTitleBarProperties().setVisible(false);
//						SwingUtilities.invokeLater(() -> {
////							boolean showTitle = windowParent.getWindowParent() != null && !(windowParent.getWindowParent() instanceof TabWindow);
//							DockingWindow windowParent1 = windowParent.getWindowParent();
////							boolean showTitle = windowParent1 != null && (windowParent1 instanceof SplitWindow || windowParent1 instanceof FloatingWindow);
//							boolean showTitle = windowParent1 != null && !(windowParent1 instanceof TabWindow);
//							setTitleBar(addedToWindow, windowParent, "Added-Later - ", showTitle);
////							setTitleBar(var1, windowParent, "Undock-Later - ", true);
//						});
//					}
//				}
			}

			public void windowShown(DockingWindow var1) {
				Container parent = var1.getParent();
				DockingWindow windowParent = var1.getWindowParent();
//				System.out.println(testWindow + " windowShown: " + var1.getTitle() + ", parent: " + parent.getClass().getSimpleName() + ", W_parent: " + windowParent);
				testWindow.printInfo("windowShown");
//				if(windowParent != null){
//					System.out.println("\tws-W_parent: " + windowParent);
//				}
			}

			public void windowHidden(DockingWindow var1) {
//				System.out.println(testWindow + " windowHidden: " + var1.getTitle());
				testWindow.printInfo("windowHidden");
			}

			public void viewFocusChanged(View var1, View var2) {
//				System.out.println("viewFocusChanged: " + var1.getTitle() + " to? " + var2.getTitle());
//				System.out.println(testWindow + " viewFocusChanged: " + var1 + " to? " + var2);
				testWindow.printInfo("viewFocusChanged");
			}

			public void windowClosed(DockingWindow var1) {
//				System.out.println(testWindow + " windowClosed: " + var1.getTitle());
				testWindow.printInfo("windowClosed");
			}

			public void windowUndocked(DockingWindow var1) {
				Container parent = var1.getParent();
//				System.out.println("windowUndocked: " + var1.getTitle() + ", parent: " + parent + ", parComps: " + parent.getComponentCount());
//				presentWindow(testWindow, var1, parent,  "Undocked");
				testWindow.printInfo("windowUndocked");
				DockingWindow windowParent = var1.getWindowParent();
//				if(windowParent != null){
//					presentWindowParent(windowParent, "wu-");
//					if (var1 instanceof View && windowParent instanceof TabWindow) {
//						setTitleBar(var1, windowParent, "Undock - ", true);
//						SwingUtilities.invokeLater(() -> {
////							boolean showTitle = windowParent.getWindowParent() != null && !(windowParent.getWindowParent() instanceof TabWindow);
//							DockingWindow windowParent1 = windowParent.getWindowParent();
//							boolean showTitle = windowParent1 != null && (windowParent1 instanceof SplitWindow || windowParent1 instanceof FloatingWindow);
//							setTitleBar(var1, windowParent, "Undock-Later - ", showTitle);
////							setTitleBar(var1, windowParent, "Undock-Later - ", true);
//						});
//					}
//				}
			}

			public void windowDocking(DockingWindow var1) throws OperationAbortedException {
//				System.out.println(testWindow + " windowDocking: " + var1.getTitle());
				testWindow.printInfo("windowDocking");
			}

			public void windowDocked(DockingWindow var1) {
				Container parent = var1.getParent();
//				System.out.println("windowDocked: " + var1.getTitle() + ", parent: " + parent + ", parComps: " + parent.getComponentCount());
//				presentWindow(testWindow, var1, parent, "Docked");
				testWindow.printInfo("windowDocked");
				DockingWindow windowParent = var1.getWindowParent();
//				if(windowParent != null){
//					presentWindowParent(windowParent, "wd-");
//					if (var1 instanceof View && windowParent instanceof TabWindow) {
////						boolean showTitle = windowParent.getWindowParent() != null && (windowParent.getWindowParent() instanceof SplitWindow);
////						setTitleBar(var1, windowParent, "Dock - ", showTitle);
////						setTitleBar(var1, windowParent, "Dock - ", false);
////						((View) var1).getViewProperties().getViewTitleBarProperties().setVisible(false);
//						SwingUtilities.invokeLater(() -> {
////							boolean showTitle = windowParent.getWindowParent() != null && !(windowParent.getWindowParent() instanceof TabWindow);
//							DockingWindow windowParent1 = windowParent.getWindowParent();
////							boolean showTitle = windowParent1 != null && (windowParent1 instanceof SplitWindow || windowParent1 instanceof FloatingWindow);
//							boolean showTitle = windowParent1 != null && !(windowParent1 instanceof TabWindow);
//							setTitleBar(var1, windowParent, "Dock-Later - ", showTitle);
////							setTitleBar(var1, windowParent, "Undock-Later - ", true);
//						});
//					}
//				}
			}

			public void windowMinimized(DockingWindow var1) {
//				System.out.println(testWindow + " windowMinimized: " + var1.getTitle());
				testWindow.printInfo("windowMinimized");
			}

			public void windowMaximized(DockingWindow var1) {
//				System.out.println(testWindow + " windowMaximized: " + var1.getTitle());
				testWindow.printInfo("windowMaximized");
			}

			public void windowRestored(DockingWindow var1) {
//				System.out.println(testWindow + " windowRestored: " + var1.getTitle());
				testWindow.printInfo("windowRestored");
			}

			public void windowMaximizing(DockingWindow var1) throws OperationAbortedException {
//				System.out.println(testWindow + " windowMaximizing: " + var1.getTitle());
				testWindow.printInfo("windowUndocking");
			}

			public void windowMinimizing(DockingWindow var1) throws OperationAbortedException {
//				System.out.println(testWindow + " windowMinimizing: " + var1.getTitle());
				testWindow.printInfo("windowMaximizing");
			}

			public void windowRestoring(DockingWindow var1) throws OperationAbortedException {
//				System.out.println(testWindow + " windowRestoring: " + var1.getTitle());
				testWindow.printInfo("windowRestoring");
			}
		};
	}

	private void presentWindow(TestWindow1 testWindow, DockingWindow var1, Container parent, String s) {
		System.out.println(testWindow + " (==var1:"+(testWindow == var1) + ") - " + s + ": " + var1.getTitle()
				+ ", parent: " + parent.getClass().getSimpleName()
				+ ", parComps: " + parent.getComponentCount()
				+ ", rootW: " + var1.getRootWindow());
		Container p2 = testWindow.getParent();
		if(p2 != null){

			System.out.println("\t" + s
//					+ ", tw par: " + p2
					+ ", parent: " + p2.getClass().getSimpleName()
					+ ", parComps: " + p2.getComponentCount()
					+ ", tw winPar: " + testWindow.getWindowParent());
		} else {
			System.out.println("\t" + s + ", tw par: " + p2);
		}
	}

	private void presentWindowParent(DockingWindow windowParent, String s) {
		System.out.println("\t" + s + "W_parent: " + windowParent + ", childs: " + windowParent.getChildWindowCount() + ", comps: " + windowParent.getComponentCount() + ", class: " + windowParent.getClass().getSimpleName());
		DockingWindow windowParent1 = windowParent.getWindowParent();
		if(windowParent1 != null){
			if(windowParent.getParent() != null){
				System.out.println("\t\t" + s + "ppW: "+ windowParent1
						+ ", class: "+ windowParent1.getClass().getSimpleName()
						+ ", ppW chc: "+ windowParent1.getChildWindowCount()
						+ ", ppW cc: "+ windowParent1.getComponentCount()
						+ ", ppC: " + windowParent.getParent().getClass().getSimpleName()
						+ ", ppC cc: "  + windowParent.getParent().getComponentCount());
			} else {
				System.out.println("\t\t" + s + "ppW: "+ windowParent1
						+ ", class: "+ windowParent1.getClass().getSimpleName()
						+ ", ppW chc: "+ windowParent1.getChildWindowCount()
						+ ", ppW cc: "+ windowParent1.getComponentCount()
						+ ", ppC: " + windowParent.getParent());
			}
			if(windowParent1 instanceof SplitWindow){
				for (int i = 0; i < windowParent1.getChildWindowCount(); i++){
					System.out.println("\t\t\tchild #" + i + ": " + windowParent1.getChildWindow(i));
//					if (windowParent1.getChildWindow(i) && )
				}
			}
		} else {
			if(windowParent.getParent() != null){
				System.out.println("\t\t" + s + "ppW: "+ windowParent1
						+ ", ppC: " + windowParent.getParent().getClass().getSimpleName()
						+ ", ppC cc: "  + windowParent.getParent().getComponentCount());
			} else {
				System.out.println("\t\t" + s + "ppW: "+ windowParent1
						+ ", ppC: " + windowParent.getParent());
			}
		}
	}

	private void setTitleBar(DockingWindow var1, DockingWindow windowParent, String s, boolean vis) {
		System.out.println("\t\t" + s + "var1.isVisible: " + var1.isVisible() + ", windowParent.isVisible: " + windowParent.isVisible() + ", windowParent.isShowing: " + windowParent.isShowing());
		ViewTitleBarProperties viewTitleBarProperties = ((View) var1).getViewProperties().getViewTitleBarProperties();
		if(viewTitleBarProperties != null && var1.isVisible() && windowParent.isVisible() && windowParent.isShowing()){

			DockingWindow windowParent1 = windowParent.getWindowParent();
			boolean showTitle = windowParent1 != null && (windowParent1 instanceof SplitWindow || windowParent1 instanceof FloatingWindow) && windowParent1.getComponentCount()<=1;
////			boolean showTitle = windowParent1 != null && !(windowParent1 instanceof TabWindow);
//			viewTitleBarProperties.setVisible(showTitle);
//			if(showTitle){
//				System.out.println("\t\t" + s + "set Visible! ");
//			} else {
//				System.out.println("\t\t" + s + "set not Visible! ");
//			}
			if(windowParent1 instanceof SplitWindow){
				for (int i = 0; i < windowParent1.getChildWindowCount(); i++){
					System.out.println("\t\t\tchild #" + i + ": " + windowParent1.getChildWindow(i));
					if (windowParent1.getChildWindow(i) instanceof View){
						((View)windowParent1.getChildWindow(i)).getViewProperties().getViewTitleBarProperties().setVisible(1<windowParent1.getChildWindowCount());
					}
				}
			} else {
				viewTitleBarProperties.setVisible(showTitle);
//				viewTitleBarProperties.setVisible(false);
			}
//			viewTitleBarProperties.setVisible(vis);
//			if(vis){
//				System.out.println("\t\t" + s + "set Visible! ");
//			} else {
//				System.out.println("\t\t" + s + "set not Visible! ");
//			}
		}
	}


//	public String getTitle() {
////		DockingWindowTitleProvider var1 = this.getWindowProperties().getTitleProvider();
////		return ((DockingWindowTitleProvider)(var1 == null ? SimpleDockingWindowTitleProvider.INSTANCE : var1)).getTitle(this);
//		return title + " overidden title!";
////		return null;
//	}
}
