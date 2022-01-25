package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.FlipFacesAction;
import com.hiveworkshop.rms.editor.actions.mesh.TeamColorAddAction;
import com.hiveworkshop.rms.editor.actions.tools.MirrorModelAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.SkinBone;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.InfoPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class ModelEditActions {


    public static void inverseAllUVs() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            for (final Geoset geo : ProgramGlobals.getCurrentModelPanel().getModel().getGeosets()) {
                for (final GeosetVertex vertex : geo.getVertices()) {
                    for (final Vec2 tvert : vertex.getTverts()) {
                        final float temp = tvert.x;
                        tvert.x = tvert.y;
                        tvert.y = temp;
                    }
                }
            }
        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static void flipAllUVsV() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            for (final Geoset geo : ProgramGlobals.getCurrentModelPanel().getModel().getGeosets()) {
                for (final GeosetVertex vertex : geo.getVertices()) {
                    for (final Vec2 tvert : vertex.getTverts()) {
                        tvert.y = 1.0f - tvert.y;
                    }
                }
            }
        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static void flipAllUVsU() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            for (final Geoset geo : modelPanel.getModel().getGeosets()) {
                for (final GeosetVertex vertex : geo.getVertices()) {
                    for (final Vec2 tvert : vertex.getTverts()) {
                        tvert.x = 1.0f - tvert.x;
                    }
                }
            }
        }
        ProgramGlobals.getMainPanel().repaint();
    }



    public static void mirrorAxis(byte i, boolean mirrorFlip) {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            Vec3 selectionCenter = modelPanel.getModelView().getSelectionCenter();
            ModelView modelView = modelPanel.getModelView();

            List<UndoAction> undoActions =  new ArrayList<>();
            MirrorModelAction mirror = new MirrorModelAction(modelView.getSelectedVertices(), modelView.getEditableIdObjects(), i, selectionCenter);
            undoActions.add(mirror);
            if (mirrorFlip) {
                undoActions.add(new FlipFacesAction(modelView.getSelectedVertices()));
            }

            modelPanel.getUndoManager().pushAction(new CompoundAction(mirror.actionName(), undoActions).redo());
        }
        ProgramGlobals.getMainPanel().repaint();
    }

}
