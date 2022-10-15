package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewUgg;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.PerspectiveViewUgg;
import com.hiveworkshop.rms.ui.application.viewer.PreviewView;
import com.hiveworkshop.rms.ui.application.windowStuff.RootWindowListener;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.ModelingCreatorToolsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingView;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.infonode.docking.*;
import net.infonode.docking.util.ViewMap;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabBorderSizePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

public class RootWindowUgg extends RootWindow {
	private static ViewMap viewMap = new ViewMap();
	private static final WindowHandler2 windowHandler2 = new WindowHandler2();

	public RootWindowUgg(byte[] viewMap1) {
		super(windowHandler2.getViewSerilizer());

		addListener(new RootWindowListener(this));
		loadDefaultLayout();

		setRootProps(this);
	}

	private void loadDefaultLayout() {
		final TabWindow startupTabWindow = windowHandler2.getStartupTabWindow();
		setWindow(startupTabWindow);
		startupTabWindow.setSelectedTab(0);
	}

	public WindowHandler2 getWindowHandler2() {
		return windowHandler2;
	}

	public FloatingWindow newWindow(View view){
		if (view.getTopLevelAncestor() == null || !view.getTopLevelAncestor().isVisible()) {
			windowHandler2.addView(view);
			FloatingWindow newWindow = createFloatingWindow(getLocation(), ScreenInfo.getSmallWindow(), view);
			newWindow.getTopLevelAncestor().setVisible(true);

			KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
			newWindow.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
			newWindow.setActionMap(keyBindingPrefs.getActionMap());
			return newWindow;
		}
		return null;
	}

	public FloatingWindow newWindow2(View view){
		if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
			windowHandler2.addView(view);
			System.out.println("WindowHandler2: opening new window, creating floating window");
			FloatingWindow newWindow
//					= newWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
					= createFloatingWindow(getLocation(), ScreenInfo.getSmallWindow(), view);
			System.out.println("WindowHandler2: setting window visible");
			newWindow.getTopLevelAncestor().setVisible(true);

			System.out.println("WindowHandler2: getting keybindings");
			KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
//            view.getRootPane().setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
			System.out.println("WindowHandler2: setting input map");
			newWindow.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
			System.out.println("WindowHandler2: setting action map");
			newWindow.setActionMap(keyBindingPrefs.getActionMap());
			System.out.println("WindowHandler2: action map set");
			return newWindow;
		}
		return null;
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



	private static JTable getjTable(UIDefaults defaults) {
		System.out.println(defaults.size() + " properties defined !");
		String[] colName = {"Key", "Value"};
		String[][] rowData = new String[defaults.size()][2];
		int i = 0;
		for (Enumeration e = defaults.keys(); e.hasMoreElements(); i++) {
			Object key = e.nextElement();
			rowData[i][0] = key.toString();
			rowData[i][1] = "" + defaults.get(key);
			System.out.println(rowData[i][0] + " ,, " + rowData[i][1]);
		}
		JTable t = new JTable(rowData, colName);
		return t;
	}

	public void resetView() {

//		final TabWindow startupTabWindow = rootWindow.getWindowHandler2().getStartupTabWindow();
//		windowHandler2.resetView();
		final TabWindow startupTabWindow = windowHandler2.resetView();;
//		startupTabWindow.setSelectedTab(0);
		setWindow(startupTabWindow);
		ModelLoader.setCurrentModel(ProgramGlobals.getCurrentModelPanel());
		revalidate();
	}

	public ViewMap compileViewMap(){
		RootWindowListener.traverseAndRemoveNull(getWindow());
		for(int i = viewMap.getViewCount(); i>0; i--){
			View view = viewMap.getView(i - 1);
			if(!view.isValid() || !view.isVisible() || view.getParent() == null){
				if(!(view instanceof DisplayViewUgg)
						|| !(view instanceof PerspectiveViewUgg)
						|| !(view instanceof PreviewView)
						|| !(view instanceof TimeSliderView)
						|| !(view instanceof ModelViewManagingView)
						|| !(view instanceof ModelingCreatorToolsView)
						|| !(view instanceof ModelComponentsView)
				){
					viewMap.removeView(i-1);
				}
			}
		}

//		extracted1(this.getAncestors());


//		traverseAndStuff(getWindow(), viewMap, viewList);
//		for(JComponent component : this.getAncestors()){
//			if (component instanceof View){
//				WindowHandler2.traverseAndStuff((View) component, viewMap, viewList);
//				viewMap.addView(viewList.size(), (View) component);
//				viewList.add((View) component);
//				System.out.println("was View!");
//			} else {
//				System.out.println();
//			}
//		}
//		viewList.clear();
//		for(JComponent component : this.getAncestors()){
//			if (component instanceof View){
//				WindowHandler2.traverseAndStuff((View) component, viewMap, viewList);
//				viewMap.addView(viewList.size(), (View) component);
//				viewList.add((View) component);
//				System.out.println("was View!");
//			} else {
//				System.out.println();
//			}
//		}

		return viewMap;
	}

	private void extracted1(DockingWindow[] components) {
		for(DockingWindow component : components){
			System.out.println("Component! e1");
			if (component instanceof View){
				System.out.println("was View!");
			}
			if(component != null){
				extracted2(component);
			}
		}
	}
	private void extracted2(DockingWindow component) {
		for(int i = 0; i< component.getChildWindowCount(); i++){
			System.out.println("Component! e2");
			if (component instanceof View){
				System.out.println("was View!");
			}

			extracted2(component.getChildWindow(i));
		}
	}

//	public ViewMap compileViewMap2(DockingWindow window){
//		for(JComponent component : this.getAncestors()){
//			if (component instanceof View){
//				WindowHandler2.traverseAndStuff((View) component, viewMap, viewList);
//				viewMap.addView(viewList.size(), (View) component);
//				viewList.add((View) component);
//				System.out.println("was View!");
//			} else {
//				System.out.println();
//			}
//		}
//
//		return viewMap;
//	}
//	private void traverseAndStuff(DockingWindow window, ViewMap viewMap, List<View> viewList) {
//		if(window instanceof View){
//			viewMap.addView(viewList.size(), (View) window);
//			viewList.add((View) window);
//			System.out.println("was View!");
//		}
//		for (int i = 0; i < window.getChildWindowCount(); i++) {
//			DockingWindow childWindow = window.getChildWindow(i);
//			if(childWindow instanceof View){
//				traverseAndStuff(childWindow, viewMap, viewList);
//			}
//		}
//	}


//	public void setViewMap1(ViewMap viewMap) {
//		this.viewMap = viewMap;
//	}


	public static ViewMap getViewMap() {
		return viewMap;
	}

	public DockingWindow[] getDockingWindows(){
		return this.getAncestors();
	}
	public byte[] ugg(){
		try {

//			RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, true);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
//			rootWindow.write(out);
			write(out);
			out.close();
//			rootWindow.close();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[]{};
	}

//	public List<View> getViewList() {
//		return viewList;
//	}

	//	public ViewMap compileViewMap(){
//		viewMap = new ViewMap();
//		viewList.clear();
//		for(JComponent component : this.getAncestors()){
//			if (component instanceof View){
//				WindowHandler2.traverseAndStuff((View) component, viewMap, viewList);
//			}
//		}
//
//		return viewMap;
//	}
}
