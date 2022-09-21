package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewCanvas;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewUgg;
import com.hiveworkshop.rms.ui.application.viewer.PreviewView;
import com.hiveworkshop.rms.ui.application.viewer.PreviewViewCanv;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitBrowserView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.ModelingCreatorToolsView;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.*;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

public class WindowHandler2 {
	protected static final boolean OLDMODE = false;
	//	private static final Set<DisplayViewUgg> displayPanelViews = new HashSet<>();
//	private static final Set<PerspectiveViewUgg> perspectivePanelViews = new HashSet<>();
//	private static final Set<PreviewView> previewPanelViews = new HashSet<>();

	final ViewportTransferHandler viewportTransferHandler = new ViewportTransferHandler();

	private static final Set<TimeSliderView> timeSliders = new HashSet<>();
	private static final Set<ModelViewManagingView> modelViewManagingTrees = new HashSet<>();
	private static final Set<ModelingCreatorToolsView> editingToolChooserViews = new HashSet<>();
	private static final Set<ModelComponentsView> componentBrowserTreeViews = new HashSet<>();
	private static final Set<ModelDependentView> allViews = new HashSet<>();
	private static final Set<MPQBrowserView> mpqBrowserViews = new HashSet<>();
	private static final Set<UnitBrowserView> unitBrowserViews = new HashSet<>();

//	private final ViewportListener viewportListener = new ViewportListener();

	public TimeSliderView getTimeSliderView() {
		return timeSliders.stream().findFirst().orElse(null);
	}

	//http://www.infonode.net/documentation/idw/guide/IDW%20Developer%27s%20Guide%201.3.pdf
	public static void resetRMSViews() {

	}

	public ViewportTransferHandler getViewportTransferHandler() {
		return viewportTransferHandler;
	}

	public WindowHandler2 setAnimationMode() {
		boolean b = ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE;
		for (ModelingCreatorToolsView editingToolChooserView : editingToolChooserViews) {
			editingToolChooserView.setAnimationModeState(b);
		}
		for (TimeSliderView timeSlider : timeSliders) {
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

	public static Set<ModelDependentView> getAllViews() {
		return allViews;
	}

//	public ViewportListener getViewportListener() {
//		return viewportListener;
//	}

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

//		System.out.println("allViews.size()1: " + allViews.size());
//		allViews.removeIf(view -> !view.isVisible().isValid());
//		allViews.removeIf(view -> !view.getComponent().isVisible());
		allViews.removeIf(view -> !isStillInUse(view));
//		System.out.println("allViews.size()2: " + allViews.size());

		for (ModelDependentView view : allViews) {
//			System.out.println("updating: " + view);
//			System.out.println(view + "#ViewProp: " + view.getViewProperties());
//			System.out.println(view + "#toString: " + view.toString());
//			System.out.println(view + "#WindowPar: " + view.getWindowParent());
			view.setModelPanel(modelPanel);
		}

//		if (modelPanel != null) {
////			modelPanel.reloadComponentBrowser();
//			modelPanel.reloadModelEditingTree();
//		}

		return this;
	}

	private boolean isStillInUse(ModelDependentView view){
		if(view.getWindowParent() == null){
			view.close();
			return false;
		}
		return true;
	}

	public void addView(View view) {
		if (view instanceof ModelDependentView) {
			allViews.add((ModelDependentView) view);
			if (view instanceof TimeSliderView) {
				timeSliders.add((TimeSliderView) view);
			} else if (view instanceof ModelViewManagingView) {
				modelViewManagingTrees.add((ModelViewManagingView) view);
			} else if (view instanceof ModelingCreatorToolsView) {
				editingToolChooserViews.add((ModelingCreatorToolsView) view);
			} else if (view instanceof ModelComponentsView) {
				componentBrowserTreeViews.add((ModelComponentsView) view);
			}
		}
	}

	public WindowHandler2 reloadThings() {
//		System.out.println("WindowHandler2#reloadThings: allViews.size()1: " + allViews.size());
//		allViews.removeIf(view -> !view.isValid());
//		System.out.println("WindowHandler2#reloadThings: allViews.size()2: " + allViews.size());
		for (ModelDependentView view : allViews) {
			view.reload();
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

		DockingWindow viewingTab = getViewTab2();

//		SplitWindow modelTab = new SplitWindow(true, 0.2f, getPlaceholderView("Contents"), getTitledView("Component"));
		ModelComponentsView modelTab = new ModelComponentsView();
		modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");
		componentBrowserTreeViews.add(modelTab);
		allViews.add(modelTab);

//		TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {modelTab});
//		TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {editingTab, modelTab});
		TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {viewingTab, editingTab, modelTab});

//        TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {editingTab, viewingTab, modelTab});
		return startupTabWindow;
	}



	private DockingWindow getViewTab() {

		PreviewView previewView = new PreviewView();
		allViews.add(previewView);
		DockingWindow viewingTab;
		if(ProgramGlobals.getPrefs().loadBrowsersOnStartup()){
			UnitBrowserView unitBrowserView = new UnitBrowserView();
			unitBrowserViews.add(unitBrowserView);
			MPQBrowserView mpqBrowserView = new MPQBrowserView();
			mpqBrowserViews.add(mpqBrowserView);

			DockingWindow[] dockingWindow = new DockingWindow[] {unitBrowserView, mpqBrowserView};

			TabWindow tabWindow = new TabWindow(dockingWindow);

			tabWindow.setSelectedTab(0);
			viewingTab = new SplitWindow(true, 0.8f, previewView, tabWindow);
		} else {
			viewingTab = previewView;
		}

//		viewingTab = new SplitWindow(true, 0.8f, previewView, tabWindow);

		viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
		return viewingTab;
	}
	private DockingWindow getViewTab2() {

		PreviewViewCanv previewView = new PreviewViewCanv();
		allViews.add(previewView);
		DockingWindow viewingTab;
		if(ProgramGlobals.getPrefs().loadBrowsersOnStartup()){
			UnitBrowserView unitBrowserView = new UnitBrowserView();
			unitBrowserViews.add(unitBrowserView);
			MPQBrowserView mpqBrowserView = new MPQBrowserView();
			mpqBrowserViews.add(mpqBrowserView);

			DockingWindow[] dockingWindow = new DockingWindow[] {unitBrowserView, mpqBrowserView};

			TabWindow tabWindow = new TabWindow(dockingWindow);

			tabWindow.setSelectedTab(0);
			viewingTab = new SplitWindow(true, 0.8f, previewView, tabWindow);
		} else {
			viewingTab = previewView;
		}

//		viewingTab = new SplitWindow(true, 0.8f, previewView, tabWindow);

		viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
		return viewingTab;
	}

	private SplitWindow getEditTab() {
		ModelViewManagingView modelEditingTreeView = new ModelViewManagingView();
		modelViewManagingTrees.add(modelEditingTreeView);
		allViews.add(modelEditingTreeView);
		TabWindow leftHandTabWindow = new TabWindow(new DockingWindow[] {modelEditingTreeView, getTitledView("Tools")});
		leftHandTabWindow.setSelectedTab(0);

		DisplayViewCanvas front = new DisplayViewCanvas("Front", true, true);
		allViews.add(front);

//		DisplayViewUgg top = new DisplayViewUgg("Top");
//		allViews.add(top);
//
//		DisplayViewUgg side = new DisplayViewUgg("Side");
//		allViews.add(side);

//		PerspectiveViewUgg perspective = new PerspectiveViewUgg();
//		allViews.add(perspective);
//		SplitWindow frBt = new SplitWindow(true, front, top);
//		SplitWindow lfPs = new SplitWindow(true, side, perspective);
//		SplitWindow quadView = new SplitWindow(false, frBt, lfPs);

		DisplayViewCanvas top = new DisplayViewCanvas("Top", true, true);
		allViews.add(top);

		DisplayViewCanvas side = new DisplayViewCanvas("Side", true, true);
		allViews.add(side);

//		PerspectiveViewUgg perspective = new PerspectiveViewUgg();
//		allViews.add(perspective);

		DisplayViewCanvas perspective = new DisplayViewCanvas("Perspective", false, false);
		allViews.add(perspective);

		SplitWindow frBt = new SplitWindow(true, front, top);
		SplitWindow lfPs = new SplitWindow(true, side, perspective);
		SplitWindow quadView = new SplitWindow(false, frBt, lfPs);

		ModelingCreatorToolsView creatorView = new ModelingCreatorToolsView();
		editingToolChooserViews.add(creatorView);
		allViews.add(creatorView);
		SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, quadView, creatorView));
//		SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, front, creatorView));

		TimeSliderView timeSliderView = new TimeSliderView();
		timeSliders.add(timeSliderView);
		allViews.add(timeSliderView);
		SplitWindow editingTab = new SplitWindow(false, 0.875f, splitWindow, timeSliderView);

		editingTab.getWindowProperties().setCloseEnabled(false);
		editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
		return editingTab;
	}
	private SplitWindow getEditTab1() {
		ModelViewManagingView modelEditingTreeView = new ModelViewManagingView();
		modelViewManagingTrees.add(modelEditingTreeView);
		allViews.add(modelEditingTreeView);
		TabWindow leftHandTabWindow = new TabWindow(new DockingWindow[] {modelEditingTreeView, getTitledView("Tools")});
		leftHandTabWindow.setSelectedTab(0);

		DisplayViewUgg front = new DisplayViewUgg("Front");
		allViews.add(front);

//		DisplayViewUgg top = new DisplayViewUgg("Top");
//		allViews.add(top);
//
//		DisplayViewUgg side = new DisplayViewUgg("Side");
//		allViews.add(side);

//		PerspectiveViewUgg perspective = new PerspectiveViewUgg();
//		allViews.add(perspective);
//		SplitWindow frBt = new SplitWindow(true, front, top);
//		SplitWindow lfPs = new SplitWindow(true, side, perspective);
//		SplitWindow quadView = new SplitWindow(false, frBt, lfPs);

		ModelingCreatorToolsView creatorView = new ModelingCreatorToolsView();
		editingToolChooserViews.add(creatorView);
		allViews.add(creatorView);
//		SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, quadView, creatorView));
		SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, front, creatorView));

		TimeSliderView timeSliderView = new TimeSliderView();
		timeSliders.add(timeSliderView);
		allViews.add(timeSliderView);
		SplitWindow editingTab = new SplitWindow(false, 0.875f, splitWindow, timeSliderView);

		editingTab.getWindowProperties().setCloseEnabled(false);
		editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
		return editingTab;
	}

	public static View getTitledView(String title) {
		return new View(title, null, new JPanel());
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

		TabWindow startupTabWindow = getStartupTabWindow();
		startupTabWindow.setSelectedTab(0);
		setModelPanel(ProgramGlobals.getCurrentModelPanel());
		return startupTabWindow;
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
