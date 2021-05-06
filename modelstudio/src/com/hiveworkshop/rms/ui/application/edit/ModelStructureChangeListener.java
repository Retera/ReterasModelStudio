package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.MainPanel;
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
	public static ModelPanel displayFor(List<ModelPanel> modelPanels, EditableModel model) {
		ModelView tempDisplay;
		for (ModelPanel modelPanel : modelPanels) {
			tempDisplay = modelPanel.getModelViewManager();
			if (tempDisplay.getModel() == model) {
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
		ModelStructureChangeListener modelStructureChangeListener;
		modelStructureChangeListener = new ModelStructureChangeListener(mainPanel, () -> mainPanel.currentModelPanel().getModel());
		return modelStructureChangeListener;
	}

	public void nodesRemoved(List<IdObject> nodes) {
		// Tell program to set visibility after import
		ModelPanel modelPanel = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (modelPanel != null) {
			for (IdObject geoset : nodes) {
				modelPanel.getModelViewManager().makeIdObjectNotVisible(geoset);
			}
			reloadGeosetManagers(mainPanel, modelPanel);
		}
	}

	public void nodesAdded(List<IdObject> nodes) {
		// Tell program to set visibility after import
		ModelPanel modelPanel = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (modelPanel != null) {
			// modelPanel.setBeenSaved(false); // we edited the model
			// TODO notify been saved system, wherever that moves to
			for (IdObject geoset : nodes) {
				modelPanel.getModelViewManager().makeIdObjectVisible(geoset);
			}
			reloadGeosetManagers(mainPanel, modelPanel);
			modelPanel.getEditorRenderModel().refreshFromEditor(
//					mainPanel.getAnimatedRenderEnvironment(),
					IDENTITY,
					IDENTITY, IDENTITY, modelPanel.getPerspArea().getViewport().getParticleTextureInstance());
			modelPanel.getAnimationViewer().reload();
		}
	}

	public void geosetsRemoved(List<Geoset> geosets) {
		// Tell program to set visibility after import
		final ModelPanel panel = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (panel != null) {
			// panel.setBeenSaved(false); // we edited the model
			// TODO notify been saved system, wherever that moves to
			for (Geoset geoset : geosets) {
				panel.getModelViewManager().makeGeosetNotEditable(geoset);
				panel.getModelViewManager().makeGeosetNotVisible(geoset);
			}
			reloadGeosetManagers(mainPanel, panel);
		}
	}

	public void geosetsAdded(List<Geoset> geosets) {
		// Tell program to set visibility after import
		final ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (display != null) {
			// display.setBeenSaved(false); // we edited the model
			// TODO notify been saved system, wherever that moves to
			for (Geoset geoset : geosets) {
				display.getModelViewManager().makeGeosetEditable(geoset);
				// display.getModelViewManager().makeGeosetVisible(geoset);
			}
			reloadGeosetManagers(mainPanel, display);
		}
	}

	public void camerasAdded(List<Camera> cameras) {
		// Tell program to set visibility after import
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (display != null) {
			// display.setBeenSaved(false); // we edited the model
			// TODO notify been saved system, wherever that moves to
			for (Camera camera : cameras) {
				display.getModelViewManager().makeCameraVisible(camera);
				// display.getModelViewManager().makeGeosetVisible(geoset);
			}
			reloadGeosetManagers(mainPanel, display);
		}
	}

	public void camerasRemoved(List<Camera> cameras) {
		// Tell program to set visibility after import
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (display != null) {
			// display.setBeenSaved(false); // we edited the model
			// TODO notify been saved system, wherever that moves to
			for (Camera camera : cameras) {
				display.getModelViewManager().makeCameraNotVisible(camera);
				// display.getModelViewManager().makeGeosetVisible(geoset);
			}
			reloadGeosetManagers(mainPanel, display);
		}
	}

	public void timelineAdded(TimelineContainer node, AnimFlag<?> timeline) {

	}

	public void keyframeAdded(TimelineContainer node, AnimFlag<?> timeline, int trackTime) {
		mainPanel.getTimeSliderPanel().revalidateKeyframeDisplay();
	}

	public void timelineRemoved(TimelineContainer node, AnimFlag<?> timeline) {

	}

	public void keyframeRemoved(TimelineContainer node, AnimFlag<?> timeline, int trackTime) {
		mainPanel.getTimeSliderPanel().revalidateKeyframeDisplay();
	}

	public void animationsAdded(List<Animation> animation) {
		mainPanel.currentModelPanel().getAnimationViewer().reload();
		mainPanel.currentModelPanel().getAnimationController().reload();
		mainPanel.currentModelPanel().reloadComponentBrowser();
		mainPanel.getCreatorPanel().reloadAnimationList();
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
	}

	public void animationsRemoved(List<Animation> animation) {
		mainPanel.currentModelPanel().getAnimationViewer().reload();
		mainPanel.currentModelPanel().getAnimationController().reload();
		mainPanel.currentModelPanel().reloadComponentBrowser();
		mainPanel.getCreatorPanel().reloadAnimationList();
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
	}

	public void texturesChanged() {
		ModelPanel modelPanel = mainPanel.currentModelPanel();
		if (modelPanel != null) {
			modelPanel.getAnimationViewer().reloadAllTextures();
			modelPanel.getPerspArea().reloadAllTextures();
			modelPanel.reloadComponentBrowser();
		}
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
	}

	public void headerChanged() {
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (display != null) {
			display.reloadComponentBrowser();
		}
	}

	public void animationParamsChanged(Animation animation) {
		mainPanel.currentModelPanel().getAnimationViewer().reload();
		mainPanel.currentModelPanel().getAnimationController().reload();
		mainPanel.currentModelPanel().reloadComponentBrowser();
		mainPanel.getCreatorPanel().reloadAnimationList();
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
	}

	public void globalSequenceLengthChanged(int index, Integer newLength) {
		mainPanel.currentModelPanel().getAnimationViewer().reload();
		mainPanel.currentModelPanel().getAnimationController().reload();
		mainPanel.currentModelPanel().reloadComponentBrowser();
		mainPanel.getCreatorPanel().reloadAnimationList();
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
	}

	public void materialsListChanged() {
		ModelPanel modelPanel = mainPanel.currentModelPanel();
		if (modelPanel != null) {
			modelPanel.getAnimationViewer().reloadAllTextures();
			modelPanel.getPerspArea().reloadAllTextures();
			modelPanel.repaintSelfAndRelatedChildren();
			modelPanel.reloadComponentBrowser();
		}
//		reloadComponentBrowser(mainPanel.getGeoControlModelData(), display);
		ModelPanel display = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
	}

	public void nodeHierarchyChanged() {
		ModelPanel modelPanel = displayFor(mainPanel.getModelPanels(), modelReference.getModel());
		if (modelPanel != null) {
//			reloadComponentBrowser(mainPanel.getGeoControlModelData(), modelPanel);
			modelPanel.reloadModelEditingTree();
			modelPanel.reloadComponentBrowser();
		}
	}

	public interface ModelReference {
		EditableModel getModel();
	}
}
