package com.hiveworkshop.rms.ui.application.windowStuff;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.ViewSerializer;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabBorderSizePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class TempRootWindow  extends RootWindow {
	public TempRootWindow(ViewSerializer viewSerializer) {
		super(viewSerializer);

		TestWindow1 window1 = new TestWindow1("window 1", null, new JPanel(new MigLayout("fill")));
		TestWindow1 window2 = new TestWindow1("window 2", null, new JPanel(new MigLayout("fill")));
		TestWindow1 window3 = new TestWindow1("window 3", null, new JPanel(new MigLayout("fill")));

		TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {window1, window2, window3});
		setWindow(startupTabWindow);
		setRootProps(this);
	}

	private static void setRootProps(RootWindow rootWindow) {

//        UIManager.put("TabbedPane.contentBorderInsets", new Insets(-2, -2, -1, -1));
		UIManager.put("TabbedPane.contentBorderInsets", new Insets(-2, -2, -1, -1));
//        //			UIManager.put("TabbedPane.border", null);
////			UIManager.put("TabbedPane.borderWidth", 0);
////			UIManager.put("TabbedPane.labelShift", 20);
////			UIManager.put("InternalFrame.borderWidth", 0);
////			UIManager.put("TabbedPane.tabRunOverlay", 4);
////			UIManager.put("InternalFrame.border", null);
////			UIManager.put("Desktop.minOnScreenInsets", new Insets(0, 0, 0, 0));
////			UIManager.put("TabbedPane.tabInsets", new Insets(0, 0, 20, 0));
////			UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 20, 0));
////			UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(8, 0, 5, 0));
//////			UIManager.put("Table.highlight", Color.magenta);
//        UIManager.put("controlDkShadow", Color.magenta.brighter());
//
//			UIManager.put("SplitPane.highlight", Color.magenta);
//			UIManager.put("TabbedPane.tabAreaBackground", Color.magenta);
//			UIManager.put("Button.highlight", Color.magenta);
//			UIManager.put("TabbedPane.highlight", Color.green);
//			UIManager.put("TabbedPane.border", Color.magenta);
//			UIManager.put("SplitPane.darkShadow", Color.magenta);
//			UIManager.put("ToolBar.darkShadow", Color.magenta);
//			UIManager.put("InternalFrame.borderDarkShadow", Color.magenta);
//			UIManager.put("Separator.highlight", Color.blue.brighter().brighter());
//			UIManager.put("Slider.focus", Color.yellow);
//			UIManager.put("controlLtHighlight", Color.yellow);
//			UIManager.put("controlHighlight", Color.yellow);
//			UIManager.put("ToolBar.highlight", Color.yellow);
//			UIManager.put("Separator.shadow", Color.yellow.darker());
//			UIManager.put("SplitPaneDivider.draggingColor", Color.yellow.darker());
//			UIManager.put("SplitPane.dividerFocusColor", Color.ORANGE.darker());
//			UIManager.put("Slider.shadow", Color.ORANGE.darker());
//			UIManager.put("MenuBar.shadow", Color.magenta.brighter());
//        UIManager.put("SplitPane.shadow", Color.blue.brighter());
//        UIManager.put("Panel.background", Color.pink);
//        UIManager.put("EditorPane.inactiveBackground", Color.blue.brighter());
//        UIManager.put("ToolBar.shadow", Color.pink);
//        UIManager.put("controlShadow", Color.yellow);
//        UIManager.put("InternalFrame.borderShadow", Color.yellow);
//        UIManager.put("InternalFrame.borderColor ", Color.ORANGE.darker());
//        UIManager.put("InternalFrame.activeTitleBackground", Color.yellow);
//			UIManager.put("Separator.background", Color.red);
//			UIManager.put("ScrollBar.thumbHighlight", Color.red);
//			UIManager.put("MenuBar.highlight", Color.cyan.brighter().brighter());
//			UIManager.put("InternalFrame.borderHighlight", Color.cyan);
//			UIManager.put("EditorPane.disabledBackground", Color.cyan);

//		UIManager.put("TabbedPane.darkShadow", Color.green);

//			UIManager.put("TabbedPane.background", Color.cyan.brighter());
//			UIManager.put("SplitPane.background", Color.green);
//			UIManager.put("TabbedPane.shadow", Color.green.darker());
//			UIManager.put("Panel.alterBackground", Color.green.darker());
//
//			UIManager.put("window", Color.red.darker().darker());
//			UIManager.put("RootPane.background", Color.red.darker().darker());
//			UIManager.put("Panel.lightBackground", Color.red.darker().darker());
//			UIManager.put("control", Color.red.darker().darker());
////			UIManager.put("TabbedPane.tabsOverlapBorder", false);
////			UIManager.put("TabbedPane.border", null);
////			UIManager.put("TabbedPane.shadow", false);
////			UIManager.put("TabbedPane.contentBorder", null);


		rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setSizePolicy(TitledTabSizePolicy.EQUAL_SIZE);
		rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setBorderSizePolicy(TitledTabBorderSizePolicy.EQUAL_SIZE);

		rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties().setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);

		rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().setShadowEnabled(false);
		rootWindow.getRootWindowProperties().getWindowAreaProperties().getInsets().set(0, 0, 0, 0);
		rootWindow.getRootWindowProperties().getSplitWindowProperties().setDividerSize(2);
//        rootWindow.getRootWindowProperties().getDockingWindowProperties().getTabProperties().getTitledTabProperties().setHighlightedRaised(0);
		rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
		rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(true);

		rootWindow.setBackground(Color.GREEN.darker());
		rootWindow.setForeground(Color.GREEN.darker());
//    getjTable(UIManager.getDefaults());

//        KeyBindingPrefs keyBindingPrefs = new KeyBindingPrefs();
//        keyBindingPrefs.makeMap();
//        System.out.println("V_______________keyBindingPrefs_________________V");
//        keyBindingPrefs.parseString("KEYBOARDKEY=released ALT");
//        System.out.println(keyBindingPrefs.toString());
//
//        System.out.println("^_______________keyBindingPrefs_________________^");
	}
}
