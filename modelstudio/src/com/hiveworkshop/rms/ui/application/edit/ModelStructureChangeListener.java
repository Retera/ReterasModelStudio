package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.WindowHandler2;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.PerspectiveViewUgg;
import com.hiveworkshop.rms.ui.application.viewer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.application.viewer.PreviewView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import java.util.Map;
import java.util.function.Consumer;

public class ModelStructureChangeListener {
	public static final ModelStructureChangeListener changeListener = new ModelStructureChangeListener();

	private Map<ModelPanel, Consumer<Boolean>> changeListeners;

	public ModelStructureChangeListener() {
	}

	public static void reloadGeosetManagers(MainPanel mainPanel, ModelPanel modelPanel) {

		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
//		modelPanel.reloadModelEditingTree();
//		modelPanel.reloadComponentBrowser();
//		modelPanel.getComponentBrowserTreePane().repaint();

//		modelPanel.getPerspArea().reloadTextures();
//		modelPanel.getAnimationViewer().reload();
//		modelPanel.getAnimationController().reload();
//		mainPanel.getMainLayoutCreator().getCreatorView().reloadAnimationList();
		mainPanel.getWindowHandler2().reloadAnimationList();

//		Particle2TextureInstance particleTextureInstance = modelPanel.getPerspArea().getViewport().getParticleTextureInstance();
		refreshFromEditor(modelPanel);
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
		updateElementsAndRefreshFromEditor();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	private void updateElementsAndRefreshFromEditor() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			refreshFromEditor(modelPanel);
		}
	}

	public static void refreshFromEditor(ModelPanel modelPanel) {
		System.out.println("refreshFromEditor");
		ModelHandler modelHandler = modelPanel.getModelHandler();
		PerspectiveViewUgg modelDependentView = (PerspectiveViewUgg) WindowHandler2.getAllViews().stream().filter(v -> v instanceof PerspectiveViewUgg).findFirst().orElse(null);
		if (modelDependentView != null && modelDependentView.getPerspectiveViewport() != null) {
			PerspectiveViewport viewport = modelDependentView.getPerspectiveViewport();
			updateRenderModel(viewport, modelHandler.getRenderModel());

		}
		PreviewView previewView = (PreviewView) WindowHandler2.getAllViews().stream().filter(v -> v instanceof PreviewView).findFirst().orElse(null);
		if (previewView != null && previewView.getPerspectiveViewport() != null) {
			PerspectiveViewport viewport = previewView.getPerspectiveViewport();
			updateRenderModel(viewport, modelHandler.getPreviewRenderModel());
		}
	}

	public static void updateRenderModel(PerspectiveViewport viewport, RenderModel renderModel) {
		renderModel.refreshFromEditor(viewport.getTextureThing());
		TimeEnvironmentImpl timeEnv = renderModel.getTimeEnvironment();
		int animationTime = timeEnv.getAnimationTime();
		Sequence currentSequence = timeEnv.getCurrentSequence();
		timeEnv.setSequence(currentSequence);
		timeEnv.setAnimationTime(animationTime);
	}

	public void geosetsUpdated() {
		// Tell program to set visibility after import
		updateElementsAndRefreshFromEditor();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void camerasUpdated() {
		// Tell program to set visibility after import
		updateElementsAndRefreshFromEditor();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void keyframesUpdated() {
//		ProgramGlobals.getMainPanel().getMainLayoutCreator().getTimeSliderView().getTimeSliderPanel().revalidateKeyframeDisplay();
//		ProgramGlobals.getMainPanel().getWindowHandler2().reValidateKeyframes();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reValidateKeyframes();
	}

	public void animationParamsChanged() {
		System.out.println("animationParamsChanged");
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			refreshFromEditor(modelPanel);
		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadAnimationList();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void texturesChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			refreshFromEditor(modelPanel);
		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void headerChanged() {
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), modelPanel);
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		if (modelPanel != null) {
////			modelPanel.reloadComponentBrowser();
//		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void globalSequenceLengthChanged() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			refreshFromEditor(modelPanel);
		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void materialsListChanged() {
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		if (modelPanel != null) {
////			modelPanel.getAnimationViewer().reloadAllTextures();
////			modelPanel.getPerspArea().reloadAllTextures();
//			modelPanel.repaintSelfAndRelatedChildren();
//			modelPanel.reloadGeosetManagers();
//		}
		updateElementsAndRefreshFromEditor();
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
	}

	public void nodeHierarchyChanged() {
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		if (modelPanel != null) {
//			modelPanel.reloadModelEditingTree();
////			modelPanel.reloadComponentBrowser();
//		}
	}

	public void selectionChanged() {
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reValidateKeyframes();
	}
}
