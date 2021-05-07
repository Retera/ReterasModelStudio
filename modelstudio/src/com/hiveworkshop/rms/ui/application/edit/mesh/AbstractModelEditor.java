package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.actions.mesh.*;
import com.hiveworkshop.rms.ui.application.actions.model.RecalculateExtentsAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawBoxAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawPlaneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.NewGeosetAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.CloneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public abstract class AbstractModelEditor<T> extends AbstractSelectingEditor<T> {
    protected final ModelView modelView;
    protected final VertexSelectionHelper vertexSelectionHelper;
    protected final ModelStructureChangeListener structureChangeListener;
    protected ModelHandler modelHandler;

    public AbstractModelEditor(SelectionManager<T> selectionManager,
                               ModelStructureChangeListener structureChangeListener,
                               ModelHandler modelHandler) {
        super(selectionManager);
        this.modelHandler = modelHandler;
        this.modelView = modelHandler.getModelView();
        this.structureChangeListener = structureChangeListener;
        vertexSelectionHelper = this::selectByVertices;
    }

    public static boolean hitTest(Vec2 min, Vec2 max, Vec3 vec3, CoordinateSystem coordinateSystem, double vertexSize) {
        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();

//        double minX = coordinateSystem.viewX(area.getMinX());
//        double minY = coordinateSystem.viewY(area.getMinY());
//        double maxX = coordinateSystem.viewX(area.getMaxX());
//        double maxY = coordinateSystem.viewY(area.getMaxY());
        Vec2 minView = new Vec2(min).minimize(max);
        Vec2 maxView = new Vec2(max).maximize(min);

        Vec2 vertexV2 = vec3.getProjected(dim1, dim2);

//        double vertexX = vec3.getCoord(dim1);
//        double x = coordinateSystem.viewX(vertexX);
//        double vertexY = vec3.getCoord(dim2);
//        double y = coordinateSystem.viewY(vertexY);

        return (vertexV2.distance(min) <= (vertexSize / 2.0))
                || (vertexV2.distance(max) <= (vertexSize / 2.0))
                || within(vertexV2, min, max);
    }

    public static boolean hitTest(Vec3 vec3, Vec2 point, CoordinateSystem coordinateSystem, double vertexSize) {
        Vec2 vertexV2 = CoordSysUtils.convertToViewVec2(coordinateSystem, vec3);
        return vertexV2.distance(point) <= (vertexSize / 2.0);
    }

    private static boolean within(Vec2 point, Vec2 min, Vec2 max) {
        boolean xIn = max.x >= point.x && point.x >= min.x;
        boolean yIn = max.y >= point.y && point.y >= min.y;
        return xIn && yIn;
    }

    public static boolean triHitTest(Triangle triangle, Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();

        GeosetVertex[] verts = triangle.getVerts();

        return within(verts[0].getProjected(dim1, dim2), min, max)
                || within(verts[1].getProjected(dim1, dim2), min, max)
                || within(verts[2].getProjected(dim1, dim2), min, max);
    }

    public static boolean triHitTest(Triangle triangle, Vec2 point, CoordinateSystem coordinateSystem) {
        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();

        Vec2[] triPoints = triangle.getProjectedVerts(dim1, dim2);

        return pointInTriangle(point, triPoints[0], triPoints[1], triPoints[2]);
    }

    private static boolean pointInTriangle(Vec2 point, Vec2 v1, Vec2 v2, Vec2 v3) {
        float d1 = (point.x - v2.x) * (v1.y - v2.y) - (v1.x - v2.x) * (point.y - v2.y);
        float d2 = (point.x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (point.y - v3.y);
        float d3 = (point.x - v1.x) * (v3.y - v1.y) - (v3.x - v1.x) * (point.y - v1.y);
        ;
//        float d1 = sign(point, v1, v2);
//        float d2 = sign(point, v2, v3);
//        float d3 = sign(point, v3, v1);

        boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    float sign(Vec2 point, Vec2 p2, Vec2 p3) {
        Vec2 diff1 = Vec2.getDif(point, p3);
        Vec2 diff2 = Vec2.getDif(p2, p3);

        return diff1.x * diff2.y - diff2.x * diff1.y;
//        return (point.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (point.y - p3.y);
    }

    @Override
    public UndoAction setMatrix(Collection<Bone> bones) {
//        System.out.println("setMatrix");
        Matrix mx = new Matrix();
        mx.setBones(new ArrayList<>());
        for (Bone bone : bones) {
            mx.add(bone);
        }
        Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences = new HashMap<>();
        Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
        Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
        System.out.println("selected verts: " + selectionManager.getSelectedVertices().size());
        for (Vec3 vert : selectionManager.getSelectedVertices()) {
            if (vert instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) vert;
                List<Bone> matrixBones = mx.getBones();
                if (gv.getSkinBoneBones() != null) {
                    vertexToOldSkinBoneReferences.put(gv, gv.getSkinBoneBones().clone());
                    vertexToOldSkinBoneWeightReferences.put(gv, gv.getSkinBoneWeights().clone());
                } else {
                    vertexToOldBoneReferences.put(gv, new ArrayList<>(gv.getBoneAttachments()));
                }
                gv.rigBones(matrixBones);
            }
        }
        return new SetMatrixAction(vertexToOldBoneReferences, vertexToOldSkinBoneReferences, vertexToOldSkinBoneWeightReferences, bones);
    }

    @Override
    public UndoAction setHDSkinning(Bone[] bones, short[] skinWeights) {
//        System.out.println("setHDSkinning");
        Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences = new HashMap<>();
        Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
        Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
//        System.out.println("sel Verts: "+ selectionManager.getSelectedVertices().size());
        for (Vec3 vert : selectionManager.getSelectedVertices()) {
            if (vert instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) vert;
                if (gv.getSkinBoneBones() != null) {
                    vertexToOldSkinBoneReferences.put(gv, gv.getSkinBoneBones().clone());
                    vertexToOldSkinBoneWeightReferences.put(gv, gv.getSkinBoneWeights().clone());
                    for (int i = 0; i < bones.length; i++) {
                        gv.setSkinBone(bones[i], skinWeights[i], i);
                    }
                } else {
                    throw new IllegalStateException("Attempted to manipulate HD Skinning while SD mesh is selected!");
                }
            }
        }
        return new SetMatrixAction(vertexToOldBoneReferences, vertexToOldSkinBoneReferences, vertexToOldSkinBoneWeightReferences, Collections.emptyList());
    }

    @Override
    public UndoAction snapNormals() {
        List<Vec3> oldLocations = new ArrayList<>();
        List<Vec3> selectedNormals = new ArrayList<>();
        Vec3 snapped = new Vec3(0, 0, 1);

        for (Vec3 vertex : selectionManager.getSelectedVertices()) {
            if (vertex instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) vertex;
                if (gv.getNormal() != null) {
                    oldLocations.add(new Vec3(gv.getNormal()));
                    selectedNormals.add(gv.getNormal());
                } // else no normal to snap!!!
            }
        }
        SnapNormalsAction temp = new SnapNormalsAction(selectedNormals, oldLocations, snapped);
        temp.redo();// a handy way to do the snapping!
        return temp;
    }

    @Override
    public UndoAction recalcNormals(double maxAngle, boolean useTries) {
        List<GeosetVertex> selectedVertices = new ArrayList<>();
        Collection<? extends Vec3> vertices = selectionManager.getSelectedVertices();
        if (vertices.isEmpty()) {
            modelView.getEditableGeosets().forEach(geoset -> selectedVertices.addAll(geoset.getVertices()));
        } else {
            vertices.forEach(vert -> selectedVertices.add((GeosetVertex) vert));
        }

        RecalculateNormalsAction temp = new RecalculateNormalsAction(selectedVertices, maxAngle, useTries);
        temp.redo();
        return temp;
    }

    @Override
    public UndoAction recalcExtents(boolean onlyIncludeEditableGeosets) {
        List<Geoset> geosetsToIncorporate = new ArrayList<>();
        if (onlyIncludeEditableGeosets) {
            geosetsToIncorporate.addAll(modelView.getEditableGeosets());
        } else {
            geosetsToIncorporate.addAll(modelHandler.getModel().getGeosets());
        }
        RecalculateExtentsAction recalculateExtentsAction = new RecalculateExtentsAction(modelView, geosetsToIncorporate);
        recalculateExtentsAction.redo();
        return recalculateExtentsAction;
    }

    @Override
    public UndoAction deleteSelectedComponents() {
        // TODO this code is RIPPED FROM MDLDispaly and is not good for general cases
        DeleteAction deleteAction = new DeleteAction(modelHandler.getModel(), selectionManager.getSelectedVertices(), structureChangeListener, vertexSelectionHelper);
        deleteAction.redo();
        return deleteAction;
    }

    @Override
    public UndoAction mirror(byte dim, boolean flipModel, double centerX, double centerY, double centerZ) {
        MirrorModelAction mirror = new MirrorModelAction(selectionManager.getSelectedVertices(), modelView.getEditableIdObjects(), dim, centerX, centerY, centerZ);
        // super weird passing of currently editable id Objects, works because mirror action
        // checks selected vertices against pivot points from this list
        mirror.redo();
        if (flipModel) {
            UndoAction flipFacesAction = flipSelectedFaces();
            return new CompoundAction(mirror.actionName(), Arrays.asList(mirror, flipFacesAction));
        }
        return mirror;
    }

    @Override
    public UndoAction flipSelectedFaces() {
        // TODO implement using faces for FaceModelEditor... probably?
        FlipFacesAction flipFacesAction = new FlipFacesAction(selectionManager.getSelectedVertices());
        flipFacesAction.redo();
        return flipFacesAction;
    }

    @Override
    public UndoAction flipSelectedNormals() {
        FlipNormalsAction flipNormalsAction = new FlipNormalsAction(selectionManager.getSelectedVertices());
        flipNormalsAction.redo();
        return flipNormalsAction;
    }

    @Override
    public UndoAction snapSelectedNormals() {
        Collection<? extends Vec3> selection = selectionManager.getSelectedVertices();
        List<Vec3> oldLocations = new ArrayList<>();
        List<Vec3> selectedNormals = new ArrayList<>();
        Vec3 snapped = new Vec3(0, 0, 1);
        for (Vec3 vertex : selection) {
            if (vertex instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) vertex;
                if (gv.getNormal() != null) {
                    oldLocations.add(new Vec3(gv.getNormal()));
                    selectedNormals.add(gv.getNormal());
                } // else no normal to snap!!!
            }
        }
        SnapNormalsAction temp = new SnapNormalsAction(selectedNormals, oldLocations, snapped);
        temp.redo();// a handy way to do the snapping!
        return temp;
    }

    @Override
    public UndoAction beginExtrudingSelection() {
        ExtendAction extendAction = new ExtendAction(selectionManager.getSelectedVertices(), new Vec3(0, 0, 0));
        extendAction.redo();
        return extendAction;
    }

    @Override
    public UndoAction beginExtendingSelection() {
        ExtrudeAction extendAction = new ExtrudeAction(selectionManager.getSelectedVertices(), new Vec3(0, 0, 0));
        extendAction.redo();
        return extendAction;
    }

    @Override
    public UndoAction snapSelectedVertices() {
        Collection<? extends Vec3> selection = selectionManager.getSelectedVertices();
        List<Vec3> oldLocations = new ArrayList<>();
        Vec3 cog = Vec3.centerOfGroup(selection);
        for (Vec3 vertex : selection) {
            oldLocations.add(new Vec3(vertex));
        }
        SnapAction temp = new SnapAction(selection, oldLocations, cog);
        temp.redo();// a handy way to do the snapping!
        return temp;
    }

    @Override
    public CloneAction cloneSelectedComponents(ClonedNodeNamePicker clonedNodeNamePicker) {
        List<Vec3> source = new ArrayList<>(selectionManager.getSelectedVertices());
        List<Triangle> selTris = new ArrayList<>();
        List<IdObject> selBones = new ArrayList<>();
        List<IdObject> newBones = new ArrayList<>();
        List<GeosetVertex> newVertices = new ArrayList<>();
        List<Triangle> newTriangles = new ArrayList<>();
        for (Vec3 vert : source) {
            if (vert.getClass() == GeosetVertex.class) {
                GeosetVertex gv = (GeosetVertex) vert;
                newVertices.add(new GeosetVertex(gv));
            } else {
                newVertices.add(null);
            }
        }
        for (IdObject b : modelView.getEditableIdObjects()) {
            if (source.contains(b.getPivotPoint()) && !selBones.contains(b)) {
                selBones.add(b);
                newBones.add(b.copy());
            }
        }
        if (newBones.size() > 0) {
            java.util.Map<IdObject, String> nodeToNamePicked = clonedNodeNamePicker.pickNames(newBones);
            if (nodeToNamePicked == null) {
                throw new RuntimeException(
                        "user does not wish to continue so we put in an error to interrupt clone so model is OK");
            }
            for (IdObject node : nodeToNamePicked.keySet()) {
                node.setName(nodeToNamePicked.get(node));
            }
        }
        filterSelectedCloneSelected(source, selTris);
        cloneTris(source, selTris, newVertices, newTriangles);
        Set<Vec3> newSelection = new HashSet<>();
        cloneVerts(selBones, newBones, newVertices, newSelection);
        for (IdObject b : newBones) {
            newSelection.add(b.getPivotPoint());
            if (selBones.contains(b.getParent())) {
                b.setParent(newBones.get(selBones.indexOf(b.getParent())));
            }
        }
        List<GeosetVertex> newVerticesWithoutNulls = new ArrayList<>();
        for (GeosetVertex vertex : newVertices) {
            if (vertex != null) {
                newVerticesWithoutNulls.add(vertex);
            }
        }
        // TODO cameras
        CloneAction cloneAction = new CloneAction(modelView, source, structureChangeListener, vertexSelectionHelper, selBones, newVerticesWithoutNulls, newTriangles, newBones, newSelection);
        cloneAction.redo();
        return cloneAction;
    }

    private void cloneVerts(List<IdObject> selBones, List<IdObject> newBones, List<GeosetVertex> newVertices, Set<Vec3> newSelection) {
        for (Vec3 ver : newVertices) {
            if (ver != null) {
                newSelection.add(ver);
                if (ver.getClass() == GeosetVertex.class) {
                    GeosetVertex gv = (GeosetVertex) ver;
                    for (int i = 0; i < gv.getBones().size(); i++) {
                        Bone b = gv.getBones().get(i);
                        if (selBones.contains(b)) {
                            gv.getBones().set(i, (Bone) newBones.get(selBones.indexOf(b)));
                        }
                    }
                }
            }
        }
    }

    private void cloneTris(List<Vec3> source, List<Triangle> selTris, List<GeosetVertex> newVertices, List<Triangle> newTriangles) {
        for (Triangle tri : selTris) {
            GeosetVertex a = newVertices.get(source.indexOf(tri.get(0)));
            GeosetVertex b = newVertices.get(source.indexOf(tri.get(1)));
            GeosetVertex c = newVertices.get(source.indexOf(tri.get(2)));
            Triangle newTriangle = new Triangle(a, b, c, a.getGeoset());
            newTriangles.add(newTriangle);
//            a.addTriangle(newTriangle);
//            b.addTriangle(newTriangle);
//            c.addTriangle(newTriangle);
        }
    }

    private void filterSelectedCloneSelected(List<Vec3> source, List<Triangle> selTris) {
        for (int k = 0; k < source.size(); k++) {
            Vec3 vert = source.get(k);
            if (vert.getClass() == GeosetVertex.class) {
                GeosetVertex gv = (GeosetVertex) vert;
                List<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
                for (Triangle tri : gv.getGeoset().getTriangles()) {
                    if (tri.contains(gv)) {
                        boolean good = true;
                        for (Vec3 vTemp : tri.getAll()) {
                            if (!source.contains(vTemp)) {
                                good = false;
                                break;
                            }
                        }
                        if (good) {
                            gvTriangles.add(tri);
                            if (!selTris.contains(tri)) {
                                selTris.add(tri);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void rawTranslate(double x, double y, double z) {
        for (Vec3 vertex : selectionManager.getSelectedVertices()) {
            vertex.translate(x, y, z);
        }
    }

    @Override
    public void rawScale(double centerX, double centerY, double centerZ,
                         double scaleX, double scaleY, double scaleZ) {
        for (Vec3 vertex : selectionManager.getSelectedVertices()) {
            vertex.scale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
        }
    }

    @Override
    public void rawScale(Vec3 center, Vec3 scale) {
        for (Vec3 vertex : selectionManager.getSelectedVertices()) {
            vertex.scale(center, scale);
        }
    }

    @Override
    public void rawRotate2d(double centerX, double centerY, double centerZ, double radians,
                            byte firstXYZ, byte secondXYZ) {
        for (Vec3 vertex : selectionManager.getSelectedVertices()) {
            vertex.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
        }
    }

    @Override
    public void rawRotate3d(Vec3 center, Vec3 axis, double radians) {
        for (Vec3 vertex : selectionManager.getSelectedVertices()) {
            Vec3.rotateVertex(center, axis, radians, vertex);
        }
    }

    @Override
    public UndoAction translate(double x, double y, double z) {
        Vec3 delta = new Vec3(x, y, z);
        StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
        moveAction.redo();
        return moveAction;
    }

    @Override
    public UndoAction translate(Vec3 v) {
        Vec3 delta = new Vec3(v);
        StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
        moveAction.redo();
        return moveAction;
    }

    @Override
    public UndoAction setPosition(Vec3 center, double x, double y, double z) {
        Vec3 delta = new Vec3(x - center.x, y - center.y, z - center.z);
        StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
        moveAction.redo();
        return moveAction;
    }

    @Override
    public UndoAction setPosition(Vec3 center, Vec3 v) {
        Vec3 delta = Vec3.getDiff(v, center);
        StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
        moveAction.redo();
        return moveAction;
    }

    @Override
    public UndoAction rotate(Vec3 center, double rotateX, double rotateY, double rotateZ) {

        CompoundAction compoundAction = new CompoundAction("rotate", Arrays.asList(
                new SimpleRotateAction(this, center, rotateX, (byte) 2, (byte) 1),
                new SimpleRotateAction(this, center, rotateY, (byte) 0, (byte) 2),
                new SimpleRotateAction(this, center, rotateZ, (byte) 1, (byte) 0)));
        compoundAction.redo();
        return compoundAction;
    }

    @Override
    public UndoAction rotate(Vec3 center, Vec3 rotate) {

        CompoundAction compoundAction = new CompoundAction("rotate", Arrays.asList(
                new SimpleRotateAction(this, center, rotate.x, (byte) 2, (byte) 1),
                new SimpleRotateAction(this, center, rotate.y, (byte) 0, (byte) 2),
                new SimpleRotateAction(this, center, rotate.z, (byte) 1, (byte) 0)));
        compoundAction.redo();
        return compoundAction;
    }

    @Override
    public Vec3 getSelectionCenter() {
        return selectionManager.getCenter();
    }

    @Override
    public boolean editorWantsAnimation() {
        return false;
    }

    @Override
    public GenericMoveAction beginTranslation() {
        return new StaticMeshMoveAction(this, Vec3.ORIGIN);
    }

    @Override
    public GenericRotateAction beginRotation(double centerX, double centerY, double centerZ,
                                             byte dim1, byte dim2) {
        return new StaticMeshRotateAction(this, new Vec3(centerX, centerY, centerZ), dim1, dim2);
    }

    @Override
    public GenericRotateAction beginSquatTool(double centerX, double centerY, double centerZ,
                                              byte firstXYZ, byte secondXYZ) {
        throw new WrongModeException("Unable to use squat tool outside animation editor mode");
    }

    @Override
    public GenericScaleAction beginScaling(double centerX, double centerY, double centerZ) {
        return new StaticMeshScaleAction(this, centerX, centerY, centerZ);
    }

    @Override
    public GenericScaleAction beginScaling(Vec3 center) {
        return new StaticMeshScaleAction(this, center);
    }

    @Override
    public UndoAction createKeyframe(ModelEditorActionType actionType) {
        throw new UnsupportedOperationException("Cannot create keyframe outside of animation mode");
    }

    @Override
    public UndoAction addBone(double x, double y, double z) {
        throw new WrongModeException("Unable to add bone outside of pivot point editor");
    }

    @Override
    public GenericMoveAction addPlane(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
                                      int numberOfWidthSegments, int numberOfHeightSegments) {
        Geoset solidWhiteGeoset = getSolidWhiteGeoset();

        DrawPlaneAction drawVertexAction = new DrawPlaneAction(p1, p2, dim1, dim2, facingVector, numberOfWidthSegments, numberOfHeightSegments, solidWhiteGeoset);

        GenericMoveAction action;
        if (!modelView.getModel().contains(solidWhiteGeoset)) {
            NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, modelView.getModel(), structureChangeListener);
            action = new CompoundMoveAction("Add Plane", Arrays.asList(new DoNothingMoveActionAdapter(newGeosetAction), drawVertexAction));
        } else {
            action = drawVertexAction;
        }
        action.redo();
        return action;
    }

    @Override
    public GenericMoveAction addBox(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector, int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments) {
        Geoset solidWhiteGeoset = getSolidWhiteGeoset();

        GenericMoveAction action;
        DrawBoxAction drawVertexAction = new DrawBoxAction(p1, p2, dim1, dim2, facingVector, numberOfLengthSegments, numberOfWidthSegments, numberOfHeightSegments, solidWhiteGeoset);

        if (!modelView.getModel().contains(solidWhiteGeoset)) {
            NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, modelView.getModel(), structureChangeListener);
            action = new CompoundMoveAction("Add Box", Arrays.asList(new DoNothingMoveActionAdapter(newGeosetAction), drawVertexAction));
        } else {
            action = drawVertexAction;
        }
        action.redo();
        return action;
    }

    public Geoset getSolidWhiteGeoset() {
        List<Geoset> geosets = modelView.getModel().getGeosets();
        Geoset solidWhiteGeoset = null;
        for (Geoset geoset : geosets) {
            Layer firstLayer = geoset.getMaterial().firstLayer();
            if ((geoset.getMaterial() != null) && (firstLayer != null)
                    && (firstLayer.getFilterMode() == FilterMode.NONE)
                    && "Textures\\white.blp".equalsIgnoreCase(firstLayer.getTextureBitmap().getPath())) {
                solidWhiteGeoset = geoset;
            }
        }

        if (solidWhiteGeoset == null) {
            solidWhiteGeoset = new Geoset();
            solidWhiteGeoset.setMaterial(new Material(new Layer("None", new Bitmap("Textures\\white.blp"))));
        }
        return solidWhiteGeoset;
    }

    @Override
    public RigAction rig() {
        System.out.println("rig, sel vert: " + this.selectionManager.getSelectedVertices().size());
        return new RigAction(this.selectionManager.getSelectedVertices(), Collections.emptyList());
    }

    @Override
    public String getSelectedMatricesDescription() {
        List<Bone> boneRefs = new ArrayList<>();
        for (Vec3 ver : selectionManager.getSelectedVertices()) {
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

    @Override
    public String getSelectedHDSkinningDescription() {
        Collection<? extends Vec3> selectedVertices = selectionManager.getSelectedVertices();
        Map<String, GeosetVertex.SkinBone[]> skinBonesArrayMap = new TreeMap<>();

        boolean selectionIsNotUniform = false;
        for (Vec3 vertex : selectedVertices) {
            if (vertex instanceof GeosetVertex) {
                GeosetVertex gv = (GeosetVertex) vertex;
                GeosetVertex.SkinBone[] skinBones = gv.getSkinBones(); //Arrays.equals(skinBones, gv.getSSkinBones())

                String sbId = skinBonesId(skinBones);
                if (!skinBonesArrayMap.containsKey(sbId)) {
                    skinBonesArrayMap.put(sbId, skinBones);
                }
            }
        }
//        if (selectionIsNotUniform) {
//            return "The skinning of the selection is not uniform. Please select only one vertex, or a group of vertices that are exactly sharing their animation skin bindings.";
//        }

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

    private String skinBonesId(GeosetVertex.SkinBone[] skinBones) {
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
}
