package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.AddTriangleAction;
import com.hiveworkshop.rms.editor.actions.mesh.FlipFacesAction;
import com.hiveworkshop.rms.editor.actions.mesh.TeamColorAddAction;
import com.hiveworkshop.rms.editor.actions.tools.MirrorModelAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.InfoPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class ModelEditActions {

    public static void viewMatrices() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            InfoPopup.show(ProgramGlobals.getMainPanel(), getSelectedMatricesDescription(modelPanel.getModelView()));
        }
    }

    public static String getSelectedMatricesDescription(ModelView modelView) {
        List<Bone> boneRefs = new ArrayList<>();
//        for (Vec3 ver : selectionManager.getSelectedVertices()) {
        for (Vec3 ver : modelView.getSelectedVertices()) {
            if (ver instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) ver;
                for (Bone b : gv.getBones()) {
                    if (!boneRefs.contains(b)) {
                        boneRefs.add(b);
                    }
                }
            }
        }
        StringBuilder boneList = new StringBuilder();
        for (int i = 0; i < boneRefs.size(); i++) {
            if (i == (boneRefs.size() - 2)) {
                boneList.append(boneRefs.get(i).getName()).append(" and ");
            } else if (i == (boneRefs.size() - 1)) {
                boneList.append(boneRefs.get(i).getName());
            } else {
                boneList.append(boneRefs.get(i).getName()).append(", ");
            }
        }
        if (boneRefs.size() == 0) {
            boneList = new StringBuilder("Nothing was selected that was attached to any bones.");
        }
        return boneList.toString();
    }

    public static UndoAction createFaceFromSelection(ModelView modelView, Vec3 preferredFacingVector) {
        Set<GeosetVertex> selection = modelView.getSelectedVertices();
        if (selection.size() != 3) {
            throw new FaceCreationException(
                    "A face can only be created from exactly 3 vertices (you have " + selection.size() + " selected)");
        }
        int index = 0;
        GeosetVertex[] verticesArray = new GeosetVertex[3];
        Geoset geoset = null;
        for (GeosetVertex vertex : selection) {
            verticesArray[index++] = vertex;
            if (geoset == null) {
                geoset = vertex.getGeoset();
            } else if (geoset != vertex.getGeoset()) {
                throw new FaceCreationException(
                        "All three vertices to create a face must be a part of the same Geoset");
            }
        }
        for (Triangle existingTriangle : verticesArray[0].getTriangles()) {
            if (existingTriangle.containsLoc(verticesArray[0])
                    && existingTriangle.containsLoc(verticesArray[1])
                    && existingTriangle.containsLoc(verticesArray[2])) {
                throw new FaceCreationException("Triangle already exists");
            }
        }

        Triangle newTriangle = new Triangle(verticesArray[0], verticesArray[1], verticesArray[2], geoset);
        Vec3 facingVector = newTriangle.getNormal();
        double cosine = facingVector.dot(preferredFacingVector) / (facingVector.length() * preferredFacingVector.length());
        if (cosine < 0) {
            newTriangle.flip(false);
        }

        return new AddTriangleAction(geoset, Collections.singletonList(newTriangle));
    }

    public static String getSelectedHDSkinningDescription(ModelView modelView) {
        Collection<? extends Vec3> selectedVertices = modelView.getSelectedVertices();
        Map<String, SkinBone[]> skinBonesArrayMap = new TreeMap<>();

        for (Vec3 vertex : selectedVertices) {
            if (vertex instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) vertex;
                SkinBone[] skinBones = gv.getSkinBones();

                String sbId = skinBonesId(skinBones);
                if (!skinBonesArrayMap.containsKey(sbId)) {
                    skinBonesArrayMap.put(sbId, skinBones);
                }
            }
        }

        StringBuilder output = new StringBuilder();
        String ugg = ":                            ";
        for (SkinBone[] skinBones : skinBonesArrayMap.values()) {
            for (int i = 0; i < 4; i++) {
                if (skinBones == null) {
                    output.append("null");
                } else {
                    String s;
                    if (skinBones[i].getBone() == null) {
                        s = "null";
                    } else {
                        s = skinBones[i].getBone().getName();
                    }
                    s = (s + ugg).substring(0, ugg.length());
                    output.append(s);
                    String w = "   " + skinBones[i].getWeight();
                    w = w.substring(w.length() - 3);
                    String w2 = (Math.round(skinBones[i].getWeight() / .255) / 1000.0 + "000000").substring(0, 6);
                    output.append(w).append(" ( ").append(w2).append(" )\n");
                }
            }
            output.append("\n");
        }
        return output.toString();
    }


//    public static UndoAction addTeamColor(ModelPanel modelPanel) {
    public static UndoAction addTeamColor(ModelView modelView, ModelStructureChangeListener structureChangeListener) {
//        ModelView modelView = modelPanel.getModelView();
//        ModelStructureChangeListener structureChangeListener = modelPanel.getModelStructureChangeListener();
        TeamColorAddAction teamColorAddAction = new TeamColorAddAction(modelView.getSelectedVertices(), modelView, structureChangeListener);
        teamColorAddAction.redo();
        return teamColorAddAction;
    }


    private static String skinBonesId(SkinBone[] skinBones) {
        // this creates an id-string from the memory addresses of the bones and the weights.
        // keeping weights and bones separated lets us use the string to sort on common bones
        // inverting the weight lets us sort highest weight first
        if (skinBones != null) {
            StringBuilder output = new StringBuilder();
            StringBuilder output2 = new StringBuilder();
            for (SkinBone skinBone : skinBones) {
//                output.append(skinBone.getBone()).append(skinBone.getWeight());
                output.append(skinBone.getBone());
                output2.append(255 - skinBone.getWeight());
            }
            return output.toString() + output2.toString();
        }
        return "null";
    }

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
