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
	private final ModelReference modelReference;
	private final MainPanel mainPanel;

	public ModelStructureChangeListener(MainPanel mainPanel, final ModelReference modelReference) {
		this.modelReference = modelReference;
		this.mainPanel = mainPanel;
	}

	public ModelStructureChangeListener(MainPanel mainPanel, EditableModel model) {
		modelReference = () -> model;
		this.mainPanel = mainPanel;
	}

	/**
	 * Returns the MDLDisplay associated with a given MDL, or null if one cannot be
	 * found.
	 */
	public static ModelPanel displayFor(EditableModel model) {
		for (ModelPanel modelPanel : ProgramGlobals.getModelPanels()) {
			if (modelPanel.getModel() == model) {
				return modelPanel;
			}
		}
		return null;
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

	public static ModelStructureChangeListener getModelStructureChangeListener(MainPanel mainPanel) {
		return new ModelStructureChangeListener(mainPanel, () -> ProgramGlobals.getCurrentModelPanel().getModel());
	}

	public void nodesUpdated() {
		// Tell program to set visibility after import
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void geosetsUpdated() {
		// Tell program to set visibility after import
		final ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void camerasUpdated() {
		// Tell program to set visibility after import
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.getModelView().updateElements();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void keyframesUpdated() {
		mainPanel.getTimeSliderPanel().revalidateKeyframeDisplay();
	}

	public void animationsAdded(List<Animation> animation) {
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void animationsRemoved(List<Animation> animation) {
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void texturesChanged() {
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void headerChanged() {
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(modelReference.getModel());
		if (display != null) {
			display.reloadComponentBrowser();
		}
	}

	public void animationParamsChanged(Animation animation) {
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void globalSequenceLengthChanged(int index, Integer newLength) {
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.reloadGeosetManagers();
		}
	}

	public void materialsListChanged() {
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.getAnimationViewer().reloadAllTextures();
			modelPanel.getPerspArea().reloadAllTextures();
			modelPanel.repaintSelfAndRelatedChildren();
			modelPanel.reloadGeosetManagers();
		}
	}

	public void nodeHierarchyChanged() {
		ModelPanel modelPanel = displayFor(modelReference.getModel());
		if (modelPanel != null) {
			modelPanel.reloadModelEditingTree();
			modelPanel.reloadComponentBrowser();
		}
	}

	public interface ModelReference {
		EditableModel getModel();
	}
}
