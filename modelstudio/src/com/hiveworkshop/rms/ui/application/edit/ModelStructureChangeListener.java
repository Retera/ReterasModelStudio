package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ModelStructureChangeListener {
	public static final ModelStructureChangeListener changeListener = new ModelStructureChangeListener();

	private Map<ModelPanel, Consumer<Boolean>> changeListeners;
	private final Map<Object, Runnable> selectionListeners = new HashMap<>();
	private final Map<Object, Runnable> stateChangeListeners = new HashMap<>();

	public ModelStructureChangeListener() {
	}

	public static ModelStructureChangeListener getModelStructureChangeListener() {
		return changeListener;
	}

	// The methods below is not static to make it less confusing where
	// UndoActions take a nullable ModelStructureChangeListener as parameter.
	// This is done to allow for compound actions where the inner actions
	// takes does not call any update function
	// It would be possible to let those actions take a boolean instead...

	public void nodesUpdated() {
		System.out.println("nodesUpdated");
		// Tell program to set visibility after import
		resetGeosetTempNames();
		updateElementsAndRefreshFromEditor();
		stateChanged();
	}

	private void updateElementsAndRefreshFromEditor() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			modelPanel.refreshFromEditor();
		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}


	public static void refreshRenderGeosets() {
//		System.out.println("refreshRenderGeosets");
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			modelHandler.getRenderModel().updateGeosets();
			modelHandler.getPreviewRenderModel().updateGeosets();
			modelPanel.getModelView().updateElements();
			modelPanel.refreshFromEditor();
		}
	}

	public void geosetsUpdated() {
		// Tell program to set visibility after import
		resetGeosetTempNames();
		refreshRenderGeosets();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		stateChanged();
	}

	public void camerasUpdated() {
		// Tell program to set visibility after import
		updateElementsAndRefreshFromEditor();
	}

	public void keyframesUpdated() {
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reValidateKeyframes();
	}

	public void animationParamsChanged() {
		System.out.println("animationParamsChanged");
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.refreshFromEditor();
		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadAnimationList();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void texturesChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.refreshFromEditor();
			modelPanel.getThumbnailProvider().reload();
		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void headerChanged() {
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void materialsListChanged() {
		updateElementsAndRefreshFromEditor();
	}

	public void nodeHierarchyChanged() {
		resetGeosetTempNames();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void selectionChanged() {
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reValidateKeyframes();
		for(Runnable listener : selectionListeners.values()){
			listener.run();
		}
	}
	public void stateChanged() {
//		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reValidateKeyframes();
		for(Runnable listener : stateChangeListeners.values()){
			listener.run();
		}
	}

	private void resetGeosetTempNames(){
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			for (Geoset geoset : modelPanel.getModel().getGeosets()) {
				geoset.resetTempName();
			}

		}
	}

	public void addSelectionListener(Object owner, Runnable listener){
		selectionListeners.put(owner, listener);
	}
	public void removeSelectionListener(Object owner){
		selectionListeners.remove(owner);
	}

	public void addStateChangeListener(Object owner, Runnable listener){
		stateChangeListeners.put(owner, listener);
	}
	public void removeStateChangeListener(Object owner){
		stateChangeListeners.remove(owner);
	}
}
