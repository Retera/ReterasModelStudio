package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewCanvas;
import com.hiveworkshop.rms.ui.application.viewer.EditUVsView;
import com.hiveworkshop.rms.ui.application.viewer.PreviewViewCanv;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitBrowserView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.ModelingCreatorToolsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class WindowHandler2 {
	private static final Set<TimeSliderView> timeSliders = new HashSet<>();
	private static final Set<ModelViewManagingView> modelViewManagingTrees = new HashSet<>();
	private static final Set<ModelingCreatorToolsView> editingToolChooserViews = new HashSet<>();
	private static final Set<ModelComponentsView> componentBrowserTreeViews = new HashSet<>();
	private static final Set<ModelDependentView> allViews = new HashSet<>();

	public TimeSliderView getTimeSliderView() {
		return timeSliders.stream().findFirst().orElse(null);
	}

	//http://www.infonode.net/documentation/idw/guide/IDW%20Developer%27s%20Guide%201.3.pdf

	public WindowHandler2 setAnimationMode() {
		SelectionItemTypes selectionItemType = ProgramGlobals.getSelectionItemType();
		for (ModelingCreatorToolsView editingToolChooserView : editingToolChooserViews) {
			editingToolChooserView.setAnimationModeState(selectionItemType);
		}

		boolean b = selectionItemType == SelectionItemTypes.ANIMATE;
		for (TimeSliderView timeSlider : timeSliders) {
			timeSlider.setAnimationMode(b);
		}
		return this;
	}
	public WindowHandler2 reValidateKeyframes() {
		for (TimeSliderView timeSlider : timeSliders) {
			timeSlider.getTimeSliderPanel().revalidateKeyframeDisplay();
		}
		return this;
	}
	public WindowHandler2 reloadAnimationList() {
		for (ModelingCreatorToolsView editingToolChooserView : editingToolChooserViews) {
			editingToolChooserView.reloadAnimationList();
		}

		return this;
	}

	public static Set<ModelDependentView> getAllViews() {
		return allViews;
	}

	public WindowHandler2 setModelPanel(ModelPanel modelPanel) {
		allViews.removeIf(view -> !isStillInUse(view));

		for (ModelDependentView view : allViews) {
			view.setModelPanel(modelPanel);
		}

		return this;
	}

	private boolean isStillInUse(ModelDependentView view) {
		if (view.getWindowParent() == null) {
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
		for (ModelDependentView view : allViews) {
			view.reload();
		}
		return this;
	}


	public WindowHandler2 showModelPanel(ModelPanel modelPanel) {
		for (ModelViewManagingView modelViewManagingTree : modelViewManagingTrees) {
			modelViewManagingTree.setModelPanel(modelPanel);
			modelViewManagingTree.repaint();
		}

		for (ModelComponentsView componentBrowserTreeView : componentBrowserTreeViews) {
			componentBrowserTreeView.setModelPanel(modelPanel);
		}
		return this;
	}

	public TabWindow getStartupTabWindow() {
		DockingWindow[] dockingWindows = {getViewTab(), getEditTab(), getModelComponentsView()};
//		DockingWindow[] dockingWindows = {getModelComponentsView()};
//		DockingWindow[] dockingWindows = {getEditTab(), getModelComponentsView()};
//		DockingWindow[] dockingWindows = {getEditTab(), getViewTab2(), getModelComponentsView()};
//		DockingWindow[] dockingWindows = {getViewTab(), getEditTab(), getModelComponentsView(), getEditUVsView()};
//		DockingWindow[] dockingWindows = {getEditUVsView(), getViewTab(), getEditTab(), getModelComponentsView()};
//		DockingWindow[] dockingWindows = {getEditUVsView(), getEditTab(), getViewTab(), getModelComponentsView()};

		return getStartupTabWindow(dockingWindows);
	}

	private TabWindow getStartupTabWindow(DockingWindow... windows) {
		for (DockingWindow window : windows) {
			if (window instanceof ModelDependentView) {
				allViews.add((ModelDependentView) window);
			}
		}
		return new TabWindow(windows);
	}

	private ModelComponentsView getModelComponentsView() {
		ModelComponentsView modelTab = new ModelComponentsView();
		modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");
		componentBrowserTreeViews.add(modelTab);
		return modelTab;
	}

	private EditUVsView getEditUVsView() {
		EditUVsView editUVsView = new EditUVsView();
		editUVsView.getWindowProperties().setTitleProvider(arg0 -> "UV");
		return editUVsView;
	}

	private DockingWindow getViewTab() {
		PreviewViewCanv previewView = new PreviewViewCanv();
		allViews.add(previewView);
		DockingWindow viewingTab;
		if (ProgramGlobals.getPrefs().loadBrowsersOnStartup()) {
			UnitBrowserView unitBrowserView = new UnitBrowserView();
			MPQBrowserView mpqBrowserView = new MPQBrowserView();

			DockingWindow[] dockingWindow = new DockingWindow[] {unitBrowserView, mpqBrowserView};

			TabWindow tabWindow = new TabWindow(dockingWindow);

			tabWindow.setSelectedTab(0);
			viewingTab = new SplitWindow(true, 0.8f, previewView, tabWindow);
		} else {
			viewingTab = previewView;
		}

		viewingTab.getWindowProperties().setTitleProvider(window -> "View");
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

		DisplayViewCanvas top = new DisplayViewCanvas("Top", true, true);
		allViews.add(top);

		DisplayViewCanvas side = new DisplayViewCanvas("Side", true, true);
		allViews.add(side);

		DisplayViewCanvas perspective = new DisplayViewCanvas("Perspective", false, false);
		allViews.add(perspective);

		SplitWindow frBt = new SplitWindow(true, front, top);
		SplitWindow lfPs = new SplitWindow(true, side, perspective);
		SplitWindow quadView = new SplitWindow(false, frBt, lfPs);

		ModelingCreatorToolsView creatorView = new ModelingCreatorToolsView();
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

	public static View getTitledView(String title) {
		return new View(title, null, new JPanel());
	}

	public TabWindow resetView() {
		clearAll();

		TabWindow startupTabWindow = getStartupTabWindow();
		startupTabWindow.setSelectedTab(0);
		setModelPanel(ProgramGlobals.getCurrentModelPanel());
		return startupTabWindow;
	}

	public WindowHandler2 clearAll() {
		for (ModelDependentView view : allViews) {
			view.setVisible(false);
		}
		timeSliders.clear();
		modelViewManagingTrees.clear();
		editingToolChooserViews.clear();
		componentBrowserTreeViews.clear();
		allViews.clear();

		return this;
	}
}
