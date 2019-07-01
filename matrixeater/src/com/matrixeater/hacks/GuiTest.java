package com.matrixeater.hacks;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import com.hiveworkshop.wc3.gui.GlobalIcons;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.StringViewMap;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;

public class GuiTest {
	public static void main(final String[] args) {

		try {
			// Set cross-platform Java L&F (also called "Metal")
			MetalLookAndFeel.setCurrentTheme(new OceanTheme());
			UIManager.setLookAndFeel(new MetalLookAndFeel());
//			UIManager.getLookAndFeel().getDefaults().put("Viewport.background", Color.WHITE);
			UIManager.put("desktop", Color.WHITE);
			System.out.println(UIManager.get("desktop"));
			UIManager.put("Viewport.background", Color.WHITE);
		} catch (final UnsupportedLookAndFeelException e) {
			// handle exception
//		} catch (final ClassNotFoundException e) {
//			// handle exception
//		} catch (final InstantiationException e) {
//			// handle exception
//		} catch (final IllegalAccessException e) {
//			// handle exception
		}
//		try {
//			final InfoNodeLookAndFeelTheme theme = new InfoNodeLookAndFeelTheme("Matrix Eater", Color.RED, Color.GREEN,
//					Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW);
//			theme.setShadingFactor(-0.8);
////			theme.setDesktopColor(Color.WHITE);
//
//			UIManager.setLookAndFeel(new InfoNodeLookAndFeel(theme));
//		} catch (final UnsupportedLookAndFeelException e) {
//			e.printStackTrace();
//		}

		final JFrame frame = new JFrame();
		final RootWindow rootWindow = new RootWindow(new StringViewMap());
		final JPanel jPanel = new JPanel();
		jPanel.add(new JLabel("content"));
		rootWindow.setWindow(new View("ASDF", GlobalIcons.MDLIcon, jPanel));
		rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(true);
		rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
				.getComponentProperties().setBackgroundColor(Color.GREEN);
		rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
				.setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
		frame.setContentPane(rootWindow);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
