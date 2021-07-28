package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.mesh.*;
import com.hiveworkshop.rms.editor.actions.model.RecalculateExtentsAction;
import com.hiveworkshop.rms.editor.actions.tools.MirrorModelAction;
import com.hiveworkshop.rms.editor.actions.tools.RigAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.InfoPopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
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
            if (existingTriangle.contains(verticesArray[0])
                    && existingTriangle.contains(verticesArray[1])
                    && existingTriangle.contains(verticesArray[2])) {
                throw new FaceCreationException("Triangle already exists");
            }
        }

        Triangle newTriangle = new Triangle(verticesArray[0], verticesArray[1], verticesArray[2], geoset);
        Vec3 facingVector = newTriangle.getNormal();
        double cosine = facingVector.dot(preferredFacingVector) / (facingVector.length() * preferredFacingVector.length());
        if (cosine < 0) {
            newTriangle.flip(false);
        }

        AddTriangleAction addTriangleAction = new AddTriangleAction(geoset, Collections.singletonList(newTriangle));
        addTriangleAction.redo();
        return addTriangleAction;
    }

    public static String getSelectedHDSkinningDescription(ModelView modelView) {
        Collection<? extends Vec3> selectedVertices = modelView.getSelectedVertices();
        Map<String, GeosetVertex.SkinBone[]> skinBonesArrayMap = new TreeMap<>();

        for (Vec3 vertex : selectedVertices) {
            if (vertex instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) vertex;
                GeosetVertex.SkinBone[] skinBones = gv.getSkinBones();

                String sbId = skinBonesId(skinBones);
                if (!skinBonesArrayMap.containsKey(sbId)) {
                    skinBonesArrayMap.put(sbId, skinBones);
                }
            }
        }

        StringBuilder output = new StringBuilder();
        String ugg = ":                            ";
        for (GeosetVertex.SkinBone[] skinBones : skinBonesArrayMap.values()) {
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
                    output.append(w);
                    output.append(" ( ");
                    String w2 = (Math.round(skinBones[i].getWeight() / .255) / 1000.0 + "000000").substring(0, 6);
                    output.append(w2);
                    output.append(" )\n");
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


    private static String skinBonesId(GeosetVertex.SkinBone[] skinBones) {
        // this creates an id-string from the memory addresses of the bones and the weights.
        // keeping weights and bones separated lets us use the string to sort on common bones
        // inverting the weight lets us sort highest weight first
        if (skinBones != null) {
            StringBuilder output = new StringBuilder();
            StringBuilder output2 = new StringBuilder();
            for (GeosetVertex.SkinBone skinBone : skinBones) {
//                output.append(skinBone.getBone()).append(skinBone.getWeight());
                output.append(skinBone.getBone());
                output2.append(255 - skinBone.getWeight());
            }
            return output.toString() + output2.toString();
        }
        return "null";
    }

    public static RigAction rig(ModelView modelView) {
        List<Bone> selectedBones = new ArrayList<>();
        for (IdObject object : modelView.getSelectedIdObjects()) {
            if (object instanceof Bone) {
                selectedBones.add((Bone) object);
            }
        }
        RigAction rigAction = new RigAction(modelView.getSelectedVertices(), selectedBones);
        rigAction.redo();
        return rigAction;
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

    public static void insideOutNormals() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            FlipNormalsAction flipNormalsAction = new FlipNormalsAction(modelPanel.getModelView().getSelectedVertices());
            flipNormalsAction.redo();
            modelPanel.getUndoManager().pushAction(flipNormalsAction);
        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static void insideOut() {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            FlipFacesAction flipFacesAction = new FlipFacesAction(modelPanel.getModelView().getSelectedVertices());
            flipFacesAction.redo();
            modelPanel.getUndoManager().pushAction(flipFacesAction);
        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static void snapVertices() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            SnapAction snapAction = new SnapAction(modelPanel.getModelView().getSelectedVertices());
            snapAction.redo();
            modelPanel.getUndoManager().pushAction(snapAction);
        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static void snapNormals() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            ModelView modelView = modelPanel.getModelView();
            SnapNormalsAction snapNormalsAction = new SnapNormalsAction(modelView.getSelectedVertices(), new Vec3(0, 0, 1));
            snapNormalsAction.redo();// a handy way to do the snapping!
            modelPanel.getUndoManager().pushAction(snapNormalsAction);

        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static void recalculateExtents() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            JPanel messagePanel = new JPanel(new MigLayout());
            messagePanel.add(new JLabel("This will calculate the extents of all model components. Proceed?"), "wrap");
            messagePanel.add(new JLabel("(It may destroy existing extents)"), "wrap");

            SmartButtonGroup buttonGroup2 = new SmartButtonGroup();
            buttonGroup2.addJRadioButton("Consider all geosets for calculation", null);
            buttonGroup2.addJRadioButton("Consider current editable geosets for calculation", null);
            buttonGroup2.setSelectedIndex(0);

            messagePanel.add(buttonGroup2.getButtonPanel(), "wrap");

//            JRadioButton considerAllBtn = new JRadioButton("Consider all geosets for calculation");
//            JRadioButton considerCurrentBtn = new JRadioButton("Consider current editable geosets for calculation");
//            ButtonGroup buttonGroup = new ButtonGroup();
//            buttonGroup.add(considerAllBtn);
//            buttonGroup.add(considerCurrentBtn);
//            considerAllBtn.setSelected(true);
//            messagePanel.add(considerAllBtn, "wrap");
//            messagePanel.add(considerCurrentBtn, "wrap");

            int userChoice = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), messagePanel, "Message",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (userChoice == JOptionPane.YES_OPTION) {
                ModelView modelView = modelPanel.getModelView();

                RecalculateExtentsAction recalculateExtentsAction;
                if (buttonGroup2.getSelectedIndex()==0) {
                    recalculateExtentsAction = new RecalculateExtentsAction(modelView, modelView.getEditableGeosets());
                } else {
                    recalculateExtentsAction = new RecalculateExtentsAction(modelView, modelView.getModel().getGeosets());
                }

                modelPanel.getUndoManager().pushAction(recalculateExtentsAction.redo());
            }
        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static void mirrorAxis(byte i, boolean mirrorFlip) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
//            final Vec3 selectionCenter = modelPanel.getModelEditorManager().getModelEditor().getSelectionCenter();
            final Vec3 selectionCenter = modelPanel.getModelView().getSelectionCenter();
            ModelView modelView = modelPanel.getModelView();

            UndoAction mirrorAction;
//            UndoAction mirror = modelPanel.getModelEditorManager().getModelEditor().mirror(i, mirrorFlip, selectionCenter);

            MirrorModelAction mirror = new MirrorModelAction(modelView.getSelectedVertices(), modelView.getEditableIdObjects(), i, selectionCenter);

            mirror.redo();
            if (mirrorFlip) {
                UndoAction flipFacesAction = flipSelectedFaces(modelView);
                mirrorAction =  new CompoundAction(mirror.actionName(), Arrays.asList(mirror, flipFacesAction));
            } else {
                mirrorAction = mirror;
            }

            modelPanel.getUndoManager().pushAction(mirrorAction);
        }
        ProgramGlobals.getMainPanel().repaint();
    }

    public static UndoAction flipSelectedFaces(ModelView modelView) {
        // TODO implement using faces for FaceModelEditor... probably?
        FlipFacesAction flipFacesAction = new FlipFacesAction(modelView.getSelectedVertices());
        flipFacesAction.redo();
        return flipFacesAction;
    }

    public static void linearizeAnimations() {
        final int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
                "This is an irreversible process that will lose some of your model data," +
                        "\nin exchange for making it a smaller storage size." +
                        "\n\nContinue and simplify animations?",
                "Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
            final List<AnimFlag<?>> allAnimFlags = modelPanel.getModel().getAllAnimFlags();
            List<UndoAction> interpTypActions = new ArrayList<>();
            for (final AnimFlag<?> flag : allAnimFlags) {
                interpTypActions.add(new ChangeInterpTypeAction<>(flag, InterpolationType.LINEAR, null));
//                flag.linearize();
            }

            UndoAction action = new CompoundAction("Liniarize Animations", interpTypActions, ModelStructureChangeListener.changeListener::materialsListChanged);
            modelPanel.getUndoManager().pushAction(action.redo());
        }
    }

//	public static void simplifyKeyframes() {
//        final int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
//                "This is an irreversible process that will lose some of your model data," +
//                        "\nin exchange for making it a smaller storage size." +
//                        "\n\nContinue and simplify keyframes?",
//                "Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
//        if (x == JOptionPane.OK_OPTION) {
//            simplifyKeyframes1();
//        }
//    }
//
//    public static void simplifyKeyframes1() {
//        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//        EditableModel model = modelPanel.getModel();
//        List<AnimFlag<?>> allAnimFlags = model.getAllAnimFlags();
//
//        SimplifyKeyframesAction action = new SimplifyKeyframesAction(allAnimFlags, model, 0.1f);
//        modelPanel.getUndoManager().pushAction(action.redo());
//    }

    public static void simplifyGeometry() {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();

        UndoAction action = new SimplifyGeometryAction2(modelPanel.getModelView().getSelectedVertices());
	    modelPanel.getUndoManager().pushAction(action.redo());
	    ModelStructureChangeListener.changeListener.geosetsUpdated();
    }

}
