package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import java.util.Map;
import java.util.function.Consumer;

public class ModelStructureChangeListener {
	public static final ModelStructureChangeListener changeListener = new ModelStructureChangeListener();

	private Map<ModelPanel, Consumer<Boolean>> changeListeners;

	public ModelStructureChangeListener() {
	}

	public static void reloadGeosetManagers(MainPanel mainPanel, ModelPanel modelPanel) {

		modelPanel.reloadModelEditingTree();
//		modelPanel.reloadComponentBrowser();
//		modelPanel.getComponentBrowserTreePane().repaint();

		modelPanel.getPerspArea().reloadTextures();
		modelPanel.getAnimationViewer().reload();
		modelPanel.getAnimationController().reload();
//		mainPanel.getMainLayoutCreator().getCreatorView().reloadAnimationList();
		mainPanel.getWindowHandler2().reloadAnimationList();

		modelPanel.getEditorRenderModel().refreshFromEditor(modelPanel.getPerspArea().getViewport().getParticleTextureInstance());
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
		// Tell program to set visibility after import
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void geosetsUpdated() {
		// Tell program to set visibility after import
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void camerasUpdated() {
		// Tell program to set visibility after import
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void keyframesUpdated() {
//		ProgramGlobals.getMainPanel().getMainLayoutCreator().getTimeSliderView().getTimeSliderPanel().revalidateKeyframeDisplay();
//		ProgramGlobals.getMainPanel().getWindowHandler2().reValidateKeyframes();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reValidateKeyframes();
	}

	public void animationParamsChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void texturesChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void headerChanged() {
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), modelPanel);
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
//			modelPanel.reloadComponentBrowser();
		}
	}

	public void globalSequenceLengthChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void materialsListChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.getAnimationViewer().reloadAllTextures();
			modelPanel.getPerspArea().reloadAllTextures();
			modelPanel.repaintSelfAndRelatedChildren();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void nodeHierarchyChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.reloadModelEditingTree();
//			modelPanel.reloadComponentBrowser();
		}
	}
}
