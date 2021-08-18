package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewUgg;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.PerspectiveViewUgg;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.viewer.PreviewView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitBrowserView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.ModelingCreatorToolsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.*;
import net.infonode.docking.util.ViewMap;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WindowHandler2 {
	protected static final boolean OLDMODE = false;
	//	private static final Set<DisplayViewUgg> displayPanelViews = new HashSet<>();
//	private static final Set<PerspectiveViewUgg> perspectivePanelViews = new HashSet<>();
//	private static final Set<PreviewView> previewPanelViews = new HashSet<>();
	private static final Set<TimeSliderView> timeSliders = new HashSet<>();
	private static final Set<ModelViewManagingView> modelViewManagingTrees = new HashSet<>();
	private static final Set<ModelingCreatorToolsView> editingToolChooserViews = new HashSet<>();
	private static final Set<ModelComponentsView> componentBrowserTreeViews = new HashSet<>();
	private static final Set<ModelDependentView> allViews = new HashSet<>();
	private static final Set<MPQBrowserView> mpqBrowserViews = new HashSet<>();
	private static final Set<UnitBrowserView> unitBrowserViews = new HashSet<>();

	final ViewportListener viewportListener = new ViewportListener();

	public TimeSliderView getTimeSliderView() {
		return timeSliders.stream().findFirst().orElse(null);
	}

	//http://www.infonode.net/documentation/idw/guide/IDW%20Developer%27s%20Guide%201.3.pdf
	public static void resetRMSViews(){

	}

	public WindowHandler2 setAnimationMode(){
		boolean b = ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE;
		for(ModelingCreatorToolsView editingToolChooserView : editingToolChooserViews){
			editingToolChooserView.setAnimationModeState(b);
		}
		for(TimeSliderView timeSlider : timeSliders){
			timeSlider.setAnimationMode(b);
		}
		return this;
	}
	public WindowHandler2 reValidateKeyframes(){
		for(TimeSliderView timeSlider : timeSliders){
			timeSlider.getTimeSliderPanel().revalidateKeyframeDisplay();
		}
		return this;
	}
	public WindowHandler2 reloadAnimationList(){
		for(ModelingCreatorToolsView editingToolChooserView : editingToolChooserViews){
			editingToolChooserView.reloadAnimationList();
		}

		return this;
	}

	public ViewportListener getViewportListener() {
		return viewportListener;
	}

	public WindowHandler2 setModelPanel(ModelPanel modelPanel) {
//		for(DisplayViewUgg displayPanelView : displayPanelViews){
//			displayPanelView.setModelPanel(modelPanel);
//		}
//		for(PerspectiveViewUgg perspectivePanelView : perspectivePanelViews){
//			perspectivePanelView.setModelPanel(modelPanel);
//		}
//		for(PreviewView previewPanelView : previewPanelViews){
//			previewPanelView.setModelPanel(modelPanel);
//		}
//		for(TimeSliderView timeSlider : timeSliders){
//			timeSlider.setModelPanel(modelPanel);
//		}
//		for(ModelViewManagingView modelViewManagingTree : modelViewManagingTrees){
//			modelViewManagingTree.setModelPanel(modelPanel);
//		}
//		for(ModelingCreatorToolsView editingToolChooserView : editingToolChooserViews){
//			editingToolChooserView.setModelPanel(modelPanel);
//		}
//		for(ModelComponentsView componentBrowserTreeView : componentBrowserTreeViews){
//			componentBrowserTreeView.setModelPanel(modelPanel);
//		}

		allViews.removeIf(view -> !view.isValid());
		for (ModelDependentView view : allViews) {
			view.setModelPanel(modelPanel);
		}

		if (modelPanel != null) {
//			modelPanel.reloadComponentBrowser();
			modelPanel.reloadModelEditingTree();
		}

		return this;
	}

	public WindowHandler2 showModelPanel(ModelPanel modelPanel) {
		for(ModelViewManagingView modelViewManagingTree : modelViewManagingTrees){
			modelViewManagingTree.setModelPanel(modelPanel);
			modelViewManagingTree.repaint();
		}

		for(ModelComponentsView componentBrowserTreeView : componentBrowserTreeViews){
			componentBrowserTreeView.setModelPanel(modelPanel);
		}
		return this;
	}

	public void dataSourcesChanged(){
		for(MPQBrowserView view : mpqBrowserViews){
			view.prefsUpdated();
		}
	}

	public TabWindow getStartupTabWindow() {
		SplitWindow editingTab = getEditTab();

		SplitWindow viewingTab = getViewTab();

//		SplitWindow modelTab = new SplitWindow(true, 0.2f, getPlaceholderView("Contents"), getTitledView("Component"));
		ModelComponentsView modelTab = new ModelComponentsView();
		modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");
		componentBrowserTreeViews.add(modelTab);
		allViews.add(modelTab);

		TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {viewingTab, editingTab, modelTab});
//        TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {editingTab, viewingTab, modelTab});
		WindowHandler.traverseAndFix(startupTabWindow);
		return startupTabWindow;
	}



	private SplitWindow getViewTab() {
		UnitBrowserView unitBrowserView = new UnitBrowserView();
		unitBrowserViews.add(unitBrowserView);
		MPQBrowserView mpqBrowserView = new MPQBrowserView();
		mpqBrowserViews.add(mpqBrowserView);

		DockingWindow[] dockingWindow = new DockingWindow[] {unitBrowserView, mpqBrowserView};

		TabWindow tabWindow = new TabWindow(dockingWindow);

		tabWindow.setSelectedTab(0);

//		View preview = getTitledView("Preview");
		PreviewView previewView = new PreviewView();
//		previewPanelViews.add(previewView);
		allViews.add(previewView);
//		View animation_controller = getTitledView("Animation Controller");
//		animationControllerViews.add(animation_controller);
//
//		SplitWindow animPersp = new SplitWindow(true, 0.8f, preview, animation_controller);

		SplitWindow viewingTab = new SplitWindow(true, 0.8f, previewView, tabWindow);

		viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
//		viewingTab.getWindowProperties().setCloseEnabled(false);
//		viewingTab.getWindowProperties().setDragEnabled(false);
//		viewingTab.getSplitWindowProperties().setDividerLocationDragEnabled(false);
		return viewingTab;
	}

	private SplitWindow getEditTab() {
		ModelViewManagingView modelEditingTreeView = new ModelViewManagingView();
		modelViewManagingTrees.add(modelEditingTreeView);
		allViews.add(modelEditingTreeView);
		TabWindow leftHandTabWindow = new TabWindow(new DockingWindow[] {modelEditingTreeView, getTitledView("Tools")});
		leftHandTabWindow.setSelectedTab(0);

		DisplayViewUgg front = new DisplayViewUgg("Front");
		DisplayViewUgg bottom = new DisplayViewUgg("Bottom");
		DisplayViewUgg side = new DisplayViewUgg("Side");
//		displayPanelViews.add(front);
//		displayPanelViews.add(bottom);
//		displayPanelViews.add(side);
		allViews.add(front);
		allViews.add(bottom);
		allViews.add(side);

		PerspectiveViewUgg perspective = new PerspectiveViewUgg();
//		perspectivePanelViews.add(perspective);
		allViews.add(perspective);
		SplitWindow frBt = new SplitWindow(true, front, bottom);
		SplitWindow lfPs = new SplitWindow(true, side, perspective);
		SplitWindow quadView = new SplitWindow(false, frBt, lfPs);

		ModelingCreatorToolsView creatorView = new ModelingCreatorToolsView(viewportListener);
		editingToolChooserViews.add(creatorView);
		allViews.add(creatorView);
		SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, quadView, creatorView));

		TimeSliderView timeSliderView = new TimeSliderView();
		timeSliders.add(timeSliderView);
		allViews.add(timeSliderView);
		SplitWindow editingTab = new SplitWindow(false, 0.875f, splitWindow, timeSliderView);

		editingTab.getWindowProperties().setCloseEnabled(false);
		editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
		return editingTab;
	}

//	public static View getPlaceholderView(String title) {
//		JPanel jPanel = new JPanel();
//		jPanel.add(new JLabel("..."));
//		return new View(title, null, jPanel);
//	}
	public static View getTitledView(String title) {
		return new View(title, null, new JPanel());
	}

//	public static View getCreatorView() {
//		CreatorModelingPanel creatorPanel = new CreatorModelingPanel(ProgramGlobals.getMainPanel().viewportListener);
//		return new View("Modeling", null, creatorPanel);
//	}

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

	static DockingWindowListener getDockingWindowListener() {
		return new DockingWindowAdapter() {
			@Override
			public void windowUndocked(final DockingWindow dockingWindow) {
				SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
					KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
					if (dockingWindow instanceof View) {
						final Component component = ((View) dockingWindow).getComponent();
						if (component instanceof JComponent) {
							JRootPane rootPane = ((JComponent) component).getRootPane();
//							mainPanel.linkActions(rootPane);
							rootPane.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
							rootPane.setActionMap(keyBindingPrefs.getActionMap());
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

	public TabWindow resetView() {
		clearAll();

		final TabWindow startupTabWindow = getStartupTabWindow();
		startupTabWindow.setSelectedTab(0);
		return startupTabWindow;
	}

	public void resetView1() {
		RootWindowUgg rootWindow = ProgramGlobals.getRootWindowUgg();
//		traverseAndReset(rootWindow);
		clearAll();

		final TabWindow startupTabWindow = getStartupTabWindow();
		startupTabWindow.setSelectedTab(0);
		rootWindow.setWindow(startupTabWindow);
		ModelLoader.setCurrentModel(ProgramGlobals.getCurrentModelPanel());
		rootWindow.revalidate();
		traverseAndFix(rootWindow);
	}

	public WindowHandler2 clearAll() {
//		for(DisplayViewUgg displayPanelView : displayPanelViews){
//			displayPanelView.setVisible(false);
//		}
//		for(PerspectiveViewUgg perspectivePanelView : perspectivePanelViews){
//			perspectivePanelView.setVisible(false);
//		}
//		for(PreviewView previewPanelView : previewPanelViews){
//			previewPanelView.setVisible(false);
//		}
//		for(TimeSliderView timeSlider : timeSliders){
//			timeSlider.setVisible(false);
//		}
//		for(ModelViewManagingView modelViewManagingTree : modelViewManagingTrees){
//			modelViewManagingTree.setVisible(false);
//		}
//		for(ModelingCreatorToolsView editingToolChooserView : editingToolChooserViews){
//			editingToolChooserView.setVisible(false);
//		}
//		for(ModelComponentsView componentBrowserTreeView : componentBrowserTreeViews){
//			componentBrowserTreeView.setVisible(false);
//		}
		for (ModelDependentView view : allViews) {
			view.setVisible(false);
		}
//		displayPanelViews.clear();
//		perspectivePanelViews.clear();
//		previewPanelViews.clear();
		timeSliders.clear();
		modelViewManagingTrees.clear();
		editingToolChooserViews.clear();
		componentBrowserTreeViews.clear();
		allViews.clear();

		return this;
	}

	public static void traverseAndReset(DockingWindow window) {
		int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			DockingWindow childWindow = window.getChildWindow(i);
//			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());
//			if(childWindow instanceof SplitWindow){
//				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
//			}

			traverseAndReset(childWindow);
			if (childWindow instanceof View) {
				((View) childWindow).getViewProperties().getViewTitleBarProperties().setVisible(true);
			}
		}
	}

	public static void traverseAndReset(DockingWindow window, Vec3 color) {
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
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndFix(childWindow);

			childWindow.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());

			if(childWindow instanceof SplitWindow){
				((SplitWindow)childWindow).getSplitWindowProperties().setDividerLocationDragEnabled(!ProgramGlobals.isLockLayout());
			}

			if (window instanceof TabWindow && (childWindowCount != 1) && (childWindow instanceof View)) {
				((View) childWindow).getViewProperties().getViewTitleBarProperties().setVisible(false);
			}
		}
	}
	public static void traverseAndRemoveNull(final DockingWindow window) {
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndFix(childWindow);

			int length = childWindow.getComponents().length;
			for(int j = length; j > 0; j--){
				if(childWindow.getComponent(j-1) == null){
					childWindow.remove(j-1);
				}
			}
		}
	}

	public static void traverseAndStuff(View view, ViewMap viewMap, List<View> viewList) {
		viewMap.addView(viewList.size(), view);
		viewList.add(view);
		for (int i = 0; i < view.getChildWindowCount(); i++) {
			DockingWindow childWindow = view.getChildWindow(i);
			if(childWindow instanceof View){
				traverseAndStuff((View) childWindow, viewMap, viewList);
			}
		}
	}

	public ViewSerializer getViewSerilizer(){
		ViewSerializer viewSerializer = new ViewSerializer(){

			@Override
			public void writeView(View view, ObjectOutputStream objectOutputStream) throws IOException {
				String str = view.getClass().getName() + "%" + view.hashCode();
//				String str = view.getClass().getPackageName() + "%" + view.hashCode();
				System.out.println(str);
				objectOutputStream.writeUTF(str);
			}

			@Override
			public View readView(ObjectInputStream objectInputStream) throws IOException {
				String s = objectInputStream.readUTF();
				String[] split = s.split("%")[0].split("\\.");

//				View titledView = getTitledView(split[split.length - 1]);
//				System.out.println(titledView);
//				return titledView;
				System.out.println(split[split.length - 1] + " (" + s + ")");

				ModelDependentView view =  switch (split[split.length - 1]){
//					case "DisplayViewUgg" -> new DisplayViewUgg("Ortho");
//					case "PerspectiveViewUgg" -> new PerspectiveViewUgg();
//					case "PreviewView" -> new PreviewView();
//					case "TimeSliderView" -> new TimeSliderView();
//					case "ModelViewManagingView" -> new ModelViewManagingView();
//					case "ModelingCreatorToolsView" -> new ModelingCreatorToolsView(viewportListener);
//					case "ModelComponentsView" -> new ModelComponentsView();
//					default -> new DisplayViewUgg(split[split.length - 1]);
					default -> null;
				};
				if(view != null){
					allViews.add(view);
				} else {
					System.out.println("not one of those views: " + split[split.length - 1] + " (" + s + ")");
					return getTitledView(split[split.length - 1]);
				}

				System.out.println(split[split.length - 1] + " (" + s + ")");
				return view;
			}
		};

		return viewSerializer;
	}
	public ViewSerializer getViewSerilizer2(byte[] viewMap1){
		ViewSerializer viewSerializer = new ViewSerializer(){

			@Override
			public void writeView(View view, ObjectOutputStream objectOutputStream) throws IOException {
				String str = view.getClass().getName() + "%" + view.hashCode();
//				String str = view.getClass().getPackageName() + "%" + view.hashCode();
				System.out.println(str);
				objectOutputStream.writeUTF(str);
			}

			@Override
			public View readView(ObjectInputStream objectInputStream) throws IOException {
				String s = objectInputStream.readUTF();
				String[] split = s.split("%")[0].split("\\.");

//				View titledView = getTitledView(split[split.length - 1]);
//				System.out.println(titledView);
//				return titledView;
				System.out.println(split[split.length - 1] + " (" + s + ")");

				ModelDependentView view =  switch (split[split.length - 1]){
//					case "DisplayViewUgg" -> new DisplayViewUgg("Ortho");
//					case "PerspectiveViewUgg" -> new PerspectiveViewUgg();
//					case "PreviewView" -> new PreviewView();
//					case "TimeSliderView" -> new TimeSliderView();
//					case "ModelViewManagingView" -> new ModelViewManagingView();
//					case "ModelingCreatorToolsView" -> new ModelingCreatorToolsView(viewportListener);
//					case "ModelComponentsView" -> new ModelComponentsView();
//					default -> new DisplayViewUgg(split[split.length - 1]);
					default -> null;
				};
				if(view != null){
					allViews.add(view);
				} else {
					System.out.println("not one of those views: " + split[split.length - 1] + " (" + s + ")");
					return getTitledView(split[split.length - 1]);
				}

				System.out.println(split[split.length - 1] + " (" + s + ")");
				return view;
			}
		};

		try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(viewMap1))) {
//				getViewSerializer().readView(objectInputStream);
				System.out.println("loading views");
				viewSerializer.readView(objectInputStream);
				System.out.println("done loading views?");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("failed.. loading internal original view");
//				extracted();
			}

		return viewSerializer;
	}
}
