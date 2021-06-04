package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Quat;

import java.util.List;

public class ModelStructureChangeListener {
	public static final Quat IDENTITY = new Quat();

	public ModelStructureChangeListener(final ModelReference modelReference) {
	}

	public ModelStructureChangeListener() {
	}

	public static void reloadGeosetManagers(MainPanel mainPanel, ModelPanel modelPanel) {

		modelPanel.reloadModelEditingTree();
		modelPanel.reloadComponentBrowser();
		modelPanel.getComponentBrowserTreePane().repaint();

		modelPanel.getPerspArea().reloadTextures();
		modelPanel.getAnimationViewer().reload();
		modelPanel.getAnimationController().reload();
		mainPanel.getCreatorPanel().reloadAnimationList();

		modelPanel.getEditorRenderModel().refreshFromEditor(
				IDENTITY, IDENTITY, IDENTITY,
				modelPanel.getPerspArea().getViewport().getParticleTextureInstance());
	}

	public static ModelStructureChangeListener getModelStructureChangeListener() {
		return new ModelStructureChangeListener();
	}

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
		ProgramGlobals.getMainPanel().getTimeSliderPanel().revalidateKeyframeDisplay();
	}

	public void animationsAdded(List<Animation> animation) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void animationsRemoved(List<Animation> animation) {
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

	public void animationParamsChanged(Animation animation) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void globalSequenceLengthChanged(int index, Integer newLength) {
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

	public interface ModelReference {
		EditableModel getModel();
	}
}
