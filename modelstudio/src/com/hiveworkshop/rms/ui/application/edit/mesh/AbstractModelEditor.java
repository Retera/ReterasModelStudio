package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.actions.mesh.*;
import com.hiveworkshop.rms.ui.application.actions.model.RecalculateExtentsAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
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

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public abstract class AbstractModelEditor<T> extends AbstractSelectingEditor<T> {
    protected final ModelView model;
    protected final VertexSelectionHelper vertexSelectionHelper;
    protected final ModelStructureChangeListener structureChangeListener;

    public AbstractModelEditor(SelectionManager<T> selectionManager, ModelView model,
                               ModelStructureChangeListener structureChangeListener) {
        super(selectionManager);
        this.model = model;
        this.structureChangeListener = structureChangeListener;
        vertexSelectionHelper = this::selectByVertices;
    }

    public static boolean hitTest(Rectangle2D area, Vec3 vec3, CoordinateSystem coordinateSystem, double vertexSize) {
        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();

        double minX = coordinateSystem.viewX(area.getMinX());
        double minY = coordinateSystem.viewY(area.getMinY());
        double maxX = coordinateSystem.viewX(area.getMaxX());
        double maxY = coordinateSystem.viewY(area.getMaxY());

        double vertexX = vec3.getCoord(dim1);
        double x = coordinateSystem.viewX(vertexX);
        double vertexY = vec3.getCoord(dim2);
        double y = coordinateSystem.viewY(vertexY);

        return (distance(x, y, minX, minY) <= (vertexSize / 2.0))
                || (distance(x, y, maxX, maxY) <= (vertexSize / 2.0))
                || area.contains(vertexX, vertexY);
    }

    public static boolean hitTest(Vec3 vec3, Point2D point, CoordinateSystem coordinateSystem, double vertexSize) {
        double x = coordinateSystem.viewX(vec3.getCoord(coordinateSystem.getPortFirstXYZ()));
        double y = coordinateSystem.viewY(vec3.getCoord(coordinateSystem.getPortSecondXYZ()));
        double px = coordinateSystem.viewX(point.getX());
        double py = coordinateSystem.viewY(point.getY());
        return Point2D.distance(px, py, x, y) <= (vertexSize / 2.0);
    }

    public static double distance(double vpX, double vpY, double x, double y) {
        double dx = x - vpX;
        double dy = y - vpY;
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    public static boolean triHitTest(Triangle triangle, Rectangle2D area, CoordinateSystem coordinateSystem) {
        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();

        GeosetVertex[] verts = triangle.getVerts();
        Path2D.Double path = new Path2D.Double();
        path.moveTo(verts[0].getCoord(dim1), verts[0].getCoord(dim2));

        for (int i = 1; i < verts.length; i++) {
            path.lineTo(verts[i].getCoord(dim1), verts[i].getCoord(dim2));
        }
        return area.contains(verts[0].getCoord(dim1), verts[0].getCoord(dim2))
                || area.contains(verts[1].getCoord(dim1), verts[1].getCoord(dim2))
                || area.contains(verts[2].getCoord(dim1), verts[2].getCoord(dim2))
                || path.intersects(area);
    }

    public static boolean triHitTest(Triangle triangle, Point2D point, CoordinateSystem coordinateSystem) {
        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();

        GeosetVertex[] verts = triangle.getVerts();
        Path2D.Double path = new Path2D.Double();
        path.moveTo(verts[0].getCoord(dim1), verts[0].getCoord(dim2));

        for (int i = 1; i < verts.length; i++) {
            path.lineTo(verts[i].getCoord(dim1), verts[i].getCoord(dim2));
        } // TODO fix bad performance allocation

        path.closePath();
        return path.contains(point);
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
        List<Vec3> oldLocations = new ArrayList<>();
        List<GeosetVertex> selectedVertices = new ArrayList<>();
        Vec3 snapped = new Vec3(0, 0, 1);
        Collection<? extends Vec3> vertices = selectionManager.getSelectedVertices();

        if (vertices.isEmpty()) {
            Set<Geoset> editable = model.getEditableGeosets();
            List<GeosetVertex> edVert = new ArrayList<>();
            editable.forEach(geoset -> edVert.addAll(geoset.getVertices()));

            for (Vec3 vertex : edVert) {
//        for (Vec3 vertex : selectionManager.getSelectedVertices()) {
                addIfValidNormal(oldLocations, selectedVertices, (GeosetVertex) vertex);
            }
        } else {
            for (Vec3 vertex : vertices) {
                if (vertex instanceof GeosetVertex) {
                    addIfValidNormal(oldLocations, selectedVertices, (GeosetVertex) vertex);
                }
            }
        }
        RecalculateNormalsAction2 temp = new RecalculateNormalsAction2(selectedVertices, oldLocations, snapped, maxAngle, useTries);
        temp.redo();// a handy way to do the snapping!
        return temp;
    }

    public void addIfValidNormal(List<Vec3> oldLocations, List<GeosetVertex> selectedVertices, GeosetVertex vertex) {
        if (vertex.getNormal() != null) {
            oldLocations.add(new Vec3(vertex.getNormal()));
            selectedVertices.add(vertex);
        } // else no normal to snap!!!
    }

    @Override
    public UndoAction recalcExtents(boolean onlyIncludeEditableGeosets) {
        List<Geoset> geosetsToIncorporate = new ArrayList<>();
        if (onlyIncludeEditableGeosets) {
            geosetsToIncorporate.addAll(model.getEditableGeosets());
        } else {
            geosetsToIncorporate.addAll(model.getModel().getGeosets());
        }
        RecalculateExtentsAction recalculateExtentsAction = new RecalculateExtentsAction(model, geosetsToIncorporate);
        recalculateExtentsAction.redo();
        return recalculateExtentsAction;
    }

    @Override
    public UndoAction deleteSelectedComponents() {
        // TODO this code is RIPPED FROM MDLDispaly and is not good for general cases
        // TODO this code operates directly on MODEL
        List<Geoset> remGeosets = new ArrayList<>();// model.getGeosets()
        List<Triangle> deletedTris = new ArrayList<>();
        Collection<? extends Vec3> selection = new ArrayList<>(selectionManager.getSelectedVertices());

        removeSelectedTriVertsFromGeoset(deletedTris, selection);
        removeSelectedTrisFromGeosetVerts(deletedTris);
        removeEmptyGeosetsAndCorrGeoAnims(remGeosets);
        selectByVertices(new ArrayList<>());

        if (remGeosets.size() <= 0) {
            return new DeleteAction(selection, deletedTris, vertexSelectionHelper);
        } else {
            SpecialDeleteAction temp = new SpecialDeleteAction(selection, deletedTris, vertexSelectionHelper, remGeosets, model.getModel(), structureChangeListener);
            structureChangeListener.geosetsRemoved(remGeosets);
            return temp;
        }
    }

    private void removeEmptyGeosetsAndCorrGeoAnims(List<Geoset> remGeosets) {
        for (int i = model.getModel().getGeosets().size() - 1; i >= 0; i--) {
            if (model.getModel().getGeosets().get(i).isEmpty()) {
                Geoset g = model.getModel().getGeoset(i);
                remGeosets.add(g);
                model.getModel().remove(g);
                if (g.getGeosetAnim() != null) {
                    model.getModel().remove(g.getGeosetAnim());
                }
            }
        }
    }

    private void removeSelectedTrisFromGeosetVerts(List<Triangle> deletedTris) {
        for (Triangle t : deletedTris) {
            for (GeosetVertex vertex : t.getAll()) {
                vertex.removeTriangle(t);
            }
        }
    }

    private void removeSelectedTriVertsFromGeoset(List<Triangle> deletedTris, Collection<? extends Vec3> selection) {
        for (Vec3 vertex : selection) {
            if (vertex.getClass() == GeosetVertex.class) {
                GeosetVertex gv = (GeosetVertex) vertex;
                for (Triangle t : gv.getTriangles()) {
                    t.getGeoset().removeTriangle(t);
                    if (!deletedTris.contains(t)) {
                        deletedTris.add(t);
                    }
                }
                gv.getGeoset().remove(gv);
            }
        }
    }

    @Override
    public UndoAction mirror(byte dim, boolean flipModel, double centerX, double centerY, double centerZ) {
        MirrorModelAction mirror = new MirrorModelAction(selectionManager.getSelectedVertices(), model.getEditableIdObjects(), dim, centerX, centerY, centerZ);
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
        List<Vec3> selection = new ArrayList<>(selectionManager.getSelectedVertices());
        List<GeosetVertex> copies = new ArrayList<>();
        List<Triangle> selTris = new ArrayList<>();
        filterExtrudeVertSelection(selection, copies, selTris);
        filterExtrudeTriSelection(selection, copies, selTris);
        // System.out.println(selection.size() + " verteces cloned into " + copies.size() + " more.");
        List<Triangle> newTriangles = new ArrayList<>();
        for (Vec3 vert : selection) {
            if (vert.getClass() == GeosetVertex.class) {
                GeosetVertex gv = (GeosetVertex) vert;
                List<Triangle> gvTriangles = getGvTriangles(selection, gv);
                for (Triangle tri : gvTriangles) {
                    // for (GeosetVertex copyVer : copies) {
                    // if (copyVer != null) {
                    // if (tri.containsRef(copyVer)) {
                    // System.out.println("holy brejeezers!");}}}
                    for (GeosetVertex gvTemp : tri.getAll()) {
                        if (!gvTemp.equalLocs(gv) && (gvTemp.getGeoset() == gv.getGeoset())) {
                            Triangle tempTri = getCheckedTriangle(gv, gvTriangles, gvTemp);
                            if (tempTri != null && selection.contains(gvTemp)) {
                                GeosetVertex gvCopy = copies.get(selection.indexOf(gv));
                                GeosetVertex gvTempCopy = copies.get(selection.indexOf(gvTemp));

                                int indexA = tempTri.indexOf(gv);
                                int indexB = tempTri.indexOf(gvTemp);
                                int indexC = -1;

                                for (int i = 0; (i < 3) && (indexC == -1); i++) {
                                    if ((i != indexA) && (i != indexB)) {
                                        indexC = i;
                                    }
                                }
                                // System.out.println(" Indeces: " + indexA + "," + indexB + "," + indexC);

                                Triangle newFace = getNewFace(gv, gvTemp, gv, gvCopy, indexA, indexB, indexC);
                                // Make sure it's included later
                                boolean bad = false;
                                for (Triangle t : newTriangles) {
                                    // if( t.equals(newFace) )
                                    // {bad = true;break;}
                                    if (t.contains(gv) && t.contains(gvTemp)) {
                                        bad = true;
                                        break;
                                    }
                                }
                                if (!bad) {
                                    newTriangles.add(newFace);

                                    Triangle newFace2 = getNewFace(gv, gvTemp, gvCopy, gvTempCopy, indexA, indexB, indexC);
                                    // Make sure it's included later
                                    newTriangles.add(newFace2);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Triangle t : newTriangles) {
            for (GeosetVertex gv : t.getAll()) {
                if (!gv.hasTriangle(t)) {
                    gv.addTriangle(t);
                }
                if (!gv.getGeoset().contains(t)) {
                    gv.getGeoset().addTriangle(t);
                }
            }
        }
        for (GeosetVertex cgv : copies) {
            if (cgv != null) {
                boolean inGeoset = false;
                for (Triangle t : cgv.getGeoset().getTriangles()) {
                    if (t.containsRef(cgv)) {
                        inGeoset = true;
                        break;
                    }
                }
                if (inGeoset) {
                    cgv.getGeoset().addVertex(cgv);
                }
            }
        }
        int probs = 0;
        for (Vec3 vert : selection) {
            if (vert.getClass() == GeosetVertex.class) {
                GeosetVertex gv = (GeosetVertex) vert;
                for (Triangle t : gv.getTriangles()) {
                    // System.out.println("SHOULD be one: " +
                    // Collections.frequency(gv.getTriangles(), t));
                    if (!t.containsRef(gv)) {
                        probs++;
                    }
                }
            }
        }
        // System.out.println("Extrude finished with " + probs + " inexplicable
        // errors.");
        ExtrudeAction tempe = new ExtrudeAction(); // TODO better code
        tempe.storeSelection(selection);
        tempe.setType(true);
        tempe.storeBaseMovement(new Vec3(0, 0, 0));
        tempe.setAddedTriangles(newTriangles);
        tempe.setAddedVerts(copies);
        return tempe;
    }

    public Triangle getNewFace(GeosetVertex gv, GeosetVertex gvTemp, GeosetVertex gvCopy, GeosetVertex gvTempCopy, int indexA, int indexB, int indexC) {
        Triangle newFace = new Triangle(gv.getGeoset());
        newFace.set(indexA, gvCopy);
        newFace.set(indexB, gvTemp);
        newFace.set(indexC, gvTempCopy);
        return newFace;
    }

    private Triangle getCheckedTriangle(GeosetVertex gv, List<Triangle> gvTriangles, GeosetVertex gvTemp) {
        int ctCount = 0;
        Triangle tempTri = null;
        boolean okay = false;
        for (Triangle triTest : gvTriangles) {
            if (triTest.contains(gvTemp)) {
                ctCount++;
                tempTri = triTest;
                if (tempTri.containsRef(gvTemp) && tempTri.containsRef(gv)) {
                    okay = true;
                }
            }
        }
        if (okay && (ctCount == 1)) {
            tempTri = null;
        }
        return tempTri;
    }

    private List<Triangle> getGvTriangles(List<Vec3> selection, GeosetVertex gv) {
        List<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
        // WHY IS GV.TRIANGLES WRONG????
        for (Triangle tri : gv.getGeoset().getTriangles()) {
            if (tri.contains(gv)) {
                boolean good = true;
                for (Vec3 vTemp : tri.getAll()) {
                    if (!selection.contains(vTemp)) {
                        good = false;
                        break;
                    }
                }
                if (good) {
                    gvTriangles.add(tri);
                }
            }
        }
        return gvTriangles;
    }

    private void filterExtrudeTriSelection(List<Vec3> selection, List<GeosetVertex> copies, List<Triangle> selTris) {
        for (Triangle tri : selTris) {
            if (!selection.contains(tri.get(0)) || !selection.contains(tri.get(1)) || !selection.contains(tri.get(2))) {
                for (int i = 0; i < 3; i++) {
                    GeosetVertex a = tri.get(i);
                    if (selection.contains(a)) {
                        GeosetVertex b = copies.get(selection.indexOf(a));
                        tri.set(i, b);
                        a.removeTriangle(tri);
                        // if (a.hasInTriangle(tri)) {
                        // System.out.println("It's a bloody war!");}
                        b.addTriangle(tri);
                    }
                }
            }
        }
    }

    private void filterExtrudeVertSelection(List<Vec3> selection, List<GeosetVertex> copies, List<Triangle> selTris) {
        for (Vec3 vert : selection) {
            if (vert.getClass() == GeosetVertex.class) {
                GeosetVertex gv = (GeosetVertex) vert;
                copies.add(new GeosetVertex(gv));

                for (Triangle tempTr : gv.getTriangles()) {
                    if (!selTris.contains(tempTr)) {
                        selTris.add(tempTr);
                    }
                }
            } else {
                copies.add(null);
                // System.out.println("GeosetVertex " + i + " was not found.");
            }
        }
    }

    @Override
    public UndoAction beginExtendingSelection() {
        List<Vec3> selection = new ArrayList<>(selectionManager.getSelectedVertices());
        List<GeosetVertex> copies = new ArrayList<>();
        List<Triangle> selTris = new ArrayList<>();
        List<Triangle> newTriangles = new ArrayList<>();

        List<Triangle> edges = new ArrayList<>();
        List<Triangle> brokenFaces = new ArrayList<>();

        filterExtendingSelection(selection, selTris);
        System.out.println(selection.size() + " verteces cloned into " + copies.size() + " more.");
        List<GeosetVertex> copiedGroup = new ArrayList<>();
        for (Triangle tri : selTris) {
            if (!selection.contains(tri.get(0)) || !selection.contains(tri.get(1)) || !selection.contains(tri.get(2))) {
                int selVerts = 0;
                GeosetVertex gv = null;
                GeosetVertex gvTemp = null;
                GeosetVertex gvCopy = null;// copies.get(selection.indexOf(gv));
                GeosetVertex gvTempCopy = null;// copies.get(selection.indexOf(gvTemp));
                for (int i = 0; i < 3; i++) {
                    GeosetVertex a = tri.get(i);
                    if (selection.contains(a)) {
                        selVerts++;
                        GeosetVertex b = new GeosetVertex(a);
                        copies.add(b);
                        copiedGroup.add(a);
                        tri.set(i, b);
                        a.removeTriangle(tri);
                        b.addTriangle(tri);
                        if (gv == null) {
                            gv = a;
                            gvCopy = b;
                        } else if (gvTemp == null) {
                            gvTemp = a;
                            gvTempCopy = b;
                        }
                    }
                }
                if (selVerts == 2) {
                    // if (gvCopy == null) {
                    // System.out.println("Vertex (gvCopy) copy found as null!"); }
                    // if (gvTempCopy == null) {
                    // System.out.println("Vertex (gvTempCopy) copy found as null!"); }

                    int indexA = tri.indexOf(gvCopy);
                    int indexB = tri.indexOf(gvTempCopy);
                    int indexC = -1;

                    for (int i = 0; (i < 3) && (indexC == -1); i++) {
                        if ((i != indexA) && (i != indexB)) {
                            indexC = i;
                        }
                    }

                    Triangle newFace = getNewFace(gv, gvTemp, gv, gvCopy, indexA, indexB, indexC);

                    // Make sure it's included later
                    gvTemp.addTriangle(newFace);
                    gv.addTriangle(newFace);
                    gvCopy.addTriangle(newFace);
                    gv.getGeoset().addTriangle(newFace);
                    newTriangles.add(newFace);

                    Triangle newFace2 = getNewFace(gv, gvTemp, gvCopy, gvTempCopy, indexA, indexB, indexC);

                    // Make sure it's included later
                    gvCopy.addTriangle(newFace2);
                    gvTemp.addTriangle(newFace2);
                    gvTempCopy.addTriangle(newFace2);
                    gv.getGeoset().addTriangle(newFace2);
                    newTriangles.add(newFace2);
                }
            }
        }

        for (GeosetVertex cgv : copies) {
            if (cgv != null) {
                cgv.getGeoset().addVertex(cgv);
            }
        }

        ExtrudeAction tempe = new ExtrudeAction();
        tempe.storeSelection(selection);
        tempe.setType(false);
        tempe.storeBaseMovement(new Vec3(0, 0, 0));
        tempe.setAddedTriangles(newTriangles);
        tempe.setAddedVerts(copies);
        tempe.setCopiedGroup(copiedGroup);
        return tempe;
    }

    private void filterExtendingSelection(List<Vec3> selection, List<Triangle> selTris) {
        for (Vec3 vert : selection) {
            if (vert.getClass() == GeosetVertex.class) {
                GeosetVertex gv = (GeosetVertex) vert;
                // copies.add(new GeosetVertex(gv));

                // selTris.addAll(gv.getTriangles());
                for (Triangle tempTr : gv.getTriangles()) {
                    if (!selTris.contains(tempTr)) {
                        selTris.add(tempTr);
                    }
                }
            } else {
                // copies.add(null);
                // System.out.println("GeosetVertex " + i + " was not found.");
            }
        }
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
        for (IdObject b : model.getEditableIdObjects()) {
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
        CloneAction cloneAction = new CloneAction(model, source, structureChangeListener, vertexSelectionHelper, selBones, newVerticesWithoutNulls, newTriangles, newBones, newSelection);
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
        if (!model.getModel().contains(solidWhiteGeoset)) {
            NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, model.getModel(), structureChangeListener);
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

        if (!model.getModel().contains(solidWhiteGeoset)) {
            NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, model.getModel(), structureChangeListener);
            action = new CompoundMoveAction("Add Box", Arrays.asList(new DoNothingMoveActionAdapter(newGeosetAction), drawVertexAction));
        } else {
            action = drawVertexAction;
        }
        action.redo();
        return action;
    }

    public Geoset getSolidWhiteGeoset() {
        List<Geoset> geosets = model.getModel().getGeosets();
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

    protected Rectangle2D getArea(Rectangle2D region) {
        double startingClickX = region.getX();
        double startingClickY = region.getY();
        double endingClickX = region.getX() + region.getWidth();
        double endingClickY = region.getY() + region.getHeight();

        double minX = Math.min(startingClickX, endingClickX);
        double minY = Math.min(startingClickY, endingClickY);
        double maxX = Math.max(startingClickX, endingClickX);
        double maxY = Math.max(startingClickY, endingClickY);

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
}
