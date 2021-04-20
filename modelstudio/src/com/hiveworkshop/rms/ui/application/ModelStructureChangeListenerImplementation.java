package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Quat;

import javax.swing.*;
import java.util.List;

class ModelStructureChangeListenerImplementation implements ModelStructureChangeListener {
    static final Quat IDENTITY = new Quat();
    private final ModelReference modelReference;
    private final MainPanel mainPanel;

    public ModelStructureChangeListenerImplementation(MainPanel mainPanel, final ModelReference modelReference) {
        this.modelReference = modelReference;
        this.mainPanel = mainPanel;
    }

    public ModelStructureChangeListenerImplementation(MainPanel mainPanel, EditableModel model) {
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

    static void reloadComponentBrowser(JScrollPane geoControlModelData, ModelPanel display) {
        if (display != null) {
            geoControlModelData.repaint();
            display.getModelComponentBrowserTree().reloadFromModelView();
            geoControlModelData.setViewportView(display.getModelComponentBrowserTree());
        }
    }

    static void reloadGeosetManagers(MainPanel mainPanel, ModelPanel display) {
        mainPanel.geoControl.repaint();
        mainPanel.geoControl.setViewportView(display.getModelViewManagingTree().reloadFromModelView());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
        display.getPerspArea().reloadTextures();// .mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
        display.getAnimationViewer().reload();
        display.getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();

        display.getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, IDENTITY, IDENTITY, IDENTITY,
                display.getPerspArea().getViewport());
    }

    static ModelStructureChangeListener getModelStructureChangeListener(MainPanel mainPanel) {
        ModelStructureChangeListener modelStructureChangeListener;
        modelStructureChangeListener = new ModelStructureChangeListenerImplementation(mainPanel, () -> mainPanel.currentModelPanel().getModel());
        return modelStructureChangeListener;
    }

    @Override
    public void nodesRemoved(List<IdObject> nodes) {
        // Tell program to set visibility after import
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (IdObject geoset : nodes) {
                display.getModelViewManager().makeIdObjectNotVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
        }
    }

    @Override
    public void nodesAdded(List<IdObject> nodes) {
        // Tell program to set visibility after import
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (IdObject geoset : nodes) {
                display.getModelViewManager().makeIdObjectVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
            display.getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, IDENTITY,
                    IDENTITY, IDENTITY, display.getPerspArea().getViewport());
            display.getAnimationViewer().reload();
        }
    }

    @Override
    public void geosetsRemoved(List<Geoset> geosets) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (Geoset geoset : geosets) {
                display.getModelViewManager().makeGeosetNotEditable(geoset);
                display.getModelViewManager().makeGeosetNotVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
        }
    }

    @Override
    public void geosetsAdded(List<Geoset> geosets) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
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

    @Override
    public void camerasAdded(List<Camera> cameras) {
        // Tell program to set visibility after import
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
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

    @Override
    public void camerasRemoved(List<Camera> cameras) {
        // Tell program to set visibility after import
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
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

    @Override
    public void timelineAdded(TimelineContainer node, AnimFlag<?> timeline) {

    }

    @Override
    public void keyframeAdded(TimelineContainer node, AnimFlag<?> timeline, int trackTime) {
        mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
    }

    @Override
    public void timelineRemoved(TimelineContainer node, AnimFlag<?> timeline) {

    }

    @Override
    public void keyframeRemoved(TimelineContainer node, AnimFlag<?> timeline, int trackTime) {
        mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
    }

    @Override
    public void animationsAdded(List<Animation> animation) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }

    @Override
    public void animationsRemoved(List<Animation> animation) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }

    @Override
    public void texturesChanged() {
        ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getAnimationViewer().reloadAllTextures();
            modelPanel.getPerspArea().reloadAllTextures();
        }
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }

    @Override
    public void headerChanged() {
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }

    @Override
    public void animationParamsChanged(Animation animation) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }

    @Override
    public void globalSequenceLengthChanged(int index, Integer newLength) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }

    @Override
    public void materialsListChanged() {
        ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getAnimationViewer().reloadAllTextures();
            modelPanel.getPerspArea().reloadAllTextures();
            modelPanel.repaintSelfAndRelatedChildren();
        }
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }

    interface ModelReference {
        EditableModel getModel();
    }

    @Override
    public void nodeHierarchyChanged() {
        ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            display.getModelViewManagingTree().reloadFromModelView();
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }
}
