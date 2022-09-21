package com.hiveworkshop.rms.ui.application.windowStuff;

import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TempToolBar extends JToolBar {
	private int extraWindows = 0;
	private RootWindow rootWindow;

	public TempToolBar(RootWindow rootWindow) {
		super(JToolBar.HORIZONTAL);
		setFloatable(false);
		this.rootWindow = rootWindow;

		add(getToolbarButton("New", "new.png", e -> openNewWindow()));
		add(getToolbarButton("Undo", "undo.png", e -> checkAllChildWindows()));
//		add(getToolbarButton("Redo", "redo.png", e -> checkAllChildWindows()));
		add(getToolbarButton("Open", "open.png", e -> System.out.println("")));
//		add(getToolbarButton("Save", "save.png", e -> checkAllChildWindows()));
//		add(getToolbarButton("Lock Layout", "lockLayout.png", e -> checkAllChildWindows()));

		setMaximumSize(new Dimension(80000, 48));
	}

	private JButton getToolbarButton(String hooverText, String icon, ActionListener action) {
		JButton button = new JButton(RMSIcons.loadToolBarImageIcon(icon));
		button.setToolTipText(hooverText);
		button.addActionListener(action);
		return button;
	}

	private void openNewWindow(){
		extraWindows++;
		TestWindow1 view = new TestWindow1("Extra window " + extraWindows, null, new JPanel(new MigLayout("fill")));
		if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
			FloatingWindow createFloatingWindow
					= rootWindow.createFloatingWindow(rootWindow.getLocation(), ScreenInfo.getSmallWindow(), view);
			createFloatingWindow.getTopLevelAncestor().setVisible(true);
		}
	}

	private void checkAllChildWindows(){
		final int childWindowCount = rootWindow.getChildWindowCount();
		System.out.println("checking child windows!");
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = rootWindow.getChildWindow(i);
			System.out.println("\tchild: \"" + childWindow + "\", class: " +  childWindow.getClass().getSimpleName());

			int length = childWindow.getComponents().length;
			for(int j = length; j > 0; j--){

				System.out.println("\t\t childWindowComp: " + childWindow.getComponent(j-1));
//				if(childWindow.getComponent(j-1) == null){
//					childWindow.remove(j-1);
//				}
			}
		}
	}
}
