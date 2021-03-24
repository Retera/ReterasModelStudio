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

    public ModelStructureChangeListenerImplementation(MainPanel mainPanel, final EditableModel model) {
        modelReference = () -> model;
        this.mainPanel = mainPanel;
    }

    /**
     * Returns the MDLDisplay associated with a given MDL, or null if one cannot be
     * found.
     */
    public static ModelPanel displayFor(List<ModelPanel> modelPanels, final EditableModel model) {
        ModelPanel output = null;
        ModelView tempDisplay;
        for (final ModelPanel modelPanel : modelPanels) {
            tempDisplay = modelPanel.getModelViewManager();
            if (tempDisplay.getModel() == model) {
                output = modelPanel;
                break;
            }
        }
        return output;
    }

    static void reloadComponentBrowser(JScrollPane geoControlModelData, final ModelPanel display) {
        geoControlModelData.repaint();
        display.getModelComponentBrowserTree().reloadFromModelView();
        geoControlModelData.setViewportView(display.getModelComponentBrowserTree());
    }

    static void reloadGeosetManagers(MainPanel mainPanel, final ModelPanel display) {
        mainPanel.geoControl.repaint();
        display.getModelViewManagingTree().reloadFromModelView();
        mainPanel.geoControl.setViewportView(display.getModelViewManagingTree());
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
        display.getPerspArea().reloadTextures();// .mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
        display.getAnimationViewer().reload();
        display.getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();

        display.getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, IDENTITY, IDENTITY, IDENTITY,
                display.getPerspArea().getViewport());
    }

    static ModelStructureChangeListener getModelStructureChangeListener(MainPanel mainPanel) {
        final ModelStructureChangeListener modelStructureChangeListener;
        modelStructureChangeListener = new ModelStructureChangeListenerImplementation(mainPanel, () -> mainPanel.currentModelPanel().getModel());
        return modelStructureChangeListener;
    }

    @Override
    public void nodesRemoved(final List<IdObject> nodes) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (final IdObject geoset : nodes) {
                display.getModelViewManager().makeIdObjectNotVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
        }
    }

    @Override
    public void nodesAdded(final List<IdObject> nodes) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (final IdObject geoset : nodes) {
                display.getModelViewManager().makeIdObjectVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
            display.getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, IDENTITY,
                    IDENTITY, IDENTITY, display.getPerspArea().getViewport());
            display.getAnimationViewer().reload();
        }
    }

    @Override
    public void geosetsRemoved(final List<Geoset> geosets) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (final Geoset geoset : geosets) {
                display.getModelViewManager().makeGeosetNotEditable(geoset);
                display.getModelViewManager().makeGeosetNotVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
        }
    }

    @Override
    public void geosetsAdded(final List<Geoset> geosets) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (final Geoset geoset : geosets) {
                display.getModelViewManager().makeGeosetEditable(geoset);
                // display.getModelViewManager().makeGeosetVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
        }
    }

    @Override
    public void camerasAdded(final List<Camera> cameras) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (final Camera camera : cameras) {
                display.getModelViewManager().makeCameraVisible(camera);
                // display.getModelViewManager().makeGeosetVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
        }
    }

    @Override
    public void camerasRemoved(final List<Camera> cameras) {
        // Tell program to set visibility after import
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            // display.setBeenSaved(false); // we edited the model
            // TODO notify been saved system, wherever that moves to
            for (final Camera camera : cameras) {
                display.getModelViewManager().makeCameraNotVisible(camera);
                // display.getModelViewManager().makeGeosetVisible(geoset);
            }
            reloadGeosetManagers(mainPanel, display);
        }
    }

    @Override
    public void timelineAdded(final TimelineContainer node, final AnimFlag<?> timeline) {

    }

    @Override
    public void keyframeAdded(final TimelineContainer node, final AnimFlag<?> timeline, final int trackTime) {
        mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
    }

    @Override
    public void timelineRemoved(final TimelineContainer node, final AnimFlag<?> timeline) {

    }

    @Override
    public void keyframeRemoved(final TimelineContainer node, final AnimFlag<?> timeline, final int trackTime) {
        mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
    }

    @Override
    public void animationsAdded(final List<Animation> animation) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }

    @Override
    public void animationsRemoved(final List<Animation> animation) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }

    @Override
    public void texturesChanged() {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getAnimationViewer().reloadAllTextures();
            modelPanel.getPerspArea().reloadAllTextures();
        }
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }

    @Override
    public void headerChanged() {
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }

    @Override
    public void animationParamsChanged(final Animation animation) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }

    @Override
    public void globalSequenceLengthChanged(final int index, final Integer newLength) {
        mainPanel.currentModelPanel().getAnimationViewer().reload();
        mainPanel.currentModelPanel().getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }

    @Override
    public void materialsListChanged() {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getAnimationViewer().reloadAllTextures();
            modelPanel.getPerspArea().reloadAllTextures();
            modelPanel.repaintSelfAndRelatedChildren();
        }
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        if (display != null) {
            reloadComponentBrowser(mainPanel.geoControlModelData, display);
        }
    }

    interface ModelReference {
        EditableModel getModel();
    }

    @Override
    public void nodeHierarchyChanged() {
        final ModelPanel display = displayFor(mainPanel.modelPanels, modelReference.getModel());
        display.getModelViewManagingTree().reloadFromModelView();
        reloadComponentBrowser(mainPanel.geoControlModelData, display);
    }
}
