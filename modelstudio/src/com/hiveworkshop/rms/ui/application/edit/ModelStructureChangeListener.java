package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.ui.application.MainLayoutCreator;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Quat;

public class ModelStructureChangeListener {
	public static final Quat IDENTITY = new Quat();
	public static final ModelStructureChangeListener changeListener = new ModelStructureChangeListener();

	public ModelStructureChangeListener() {
	}

	public static void reloadGeosetManagers(MainPanel mainPanel, ModelPanel modelPanel) {

		modelPanel.reloadModelEditingTree();
		modelPanel.reloadComponentBrowser();
		modelPanel.getComponentBrowserTreePane().repaint();

		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		modelPanel.getPerspArea().reloadTextures();
		modelPanel.getAnimationViewer().reload();
		modelPanel.getAnimationController().reload();
		mainLayoutCreator.getCreatorPanel().reloadAnimationList();

		modelPanel.getEditorRenderModel().refreshFromEditor(
				IDENTITY, IDENTITY, IDENTITY,
				modelPanel.getPerspArea().getViewport().getParticleTextureInstance());
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
		ProgramGlobals.getMainPanel().getMainLayoutCreator().getTimeSliderPanel().revalidateKeyframeDisplay();
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
			modelPanel.reloadComponentBrowser();
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
			modelPanel.reloadComponentBrowser();
		}
	}
}
