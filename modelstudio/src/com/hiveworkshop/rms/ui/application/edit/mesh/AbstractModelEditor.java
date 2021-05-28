package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.SimpleRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Arrays;

public class AbstractModelEditor extends AbstractSelectingEditor {
    protected final ModelStructureChangeListener structureChangeListener;
    protected ModelHandler modelHandler;
    protected SelectionItemTypes selectionMode;

	public AbstractModelEditor(SelectionView selectionManager,
	                           ModelStructureChangeListener structureChangeListener,
	                           ModelHandler modelHandler, SelectionItemTypes selectionMode) {
		super(selectionManager, modelHandler.getModelView());
		this.modelHandler = modelHandler;
		this.structureChangeListener = structureChangeListener;
		this.selectionMode = selectionMode;
	}

    public static boolean hitTest(Vec2 min, Vec2 max, Vec3 vec3, CoordinateSystem coordinateSystem, double vertexSize) {
        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();

        Vec2 minView = new Vec2(min).minimize(max);
        Vec2 maxView = new Vec2(max).maximize(min);

        Vec2 vertexV2 = vec3.getProjected(dim1, dim2);


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
    public UndoAction translate(Vec3 v) {
        Vec3 delta = new Vec3(v);
	    return new StaticMeshMoveAction(modelView, delta).redo();
    }

    @Override
    public UndoAction scale(Vec3 center, Vec3 scale) {
	    return new StaticMeshScaleAction(modelView, center).updateScale(scale).redo();
    }

    @Override
    public UndoAction setPosition(Vec3 center, Vec3 v) {
        Vec3 delta = Vec3.getDiff(v, center);
	    return new StaticMeshMoveAction(modelView, delta).redo();
    }

    @Override
    public UndoAction rotate(Vec3 center, Vec3 rotate) {
	    return new CompoundAction("rotate", Arrays.asList(
			    new SimpleRotateAction(modelView, center, rotate.x, (byte) 2, (byte) 1),
			    new SimpleRotateAction(modelView, center, rotate.y, (byte) 0, (byte) 2),
			    new SimpleRotateAction(modelView, center, rotate.z, (byte) 1, (byte) 0)))
			    .redo();
    }

    @Override
    public boolean editorWantsAnimation() {
        return false;
    }

    @Override
    public GenericMoveAction beginTranslation() {
        return new StaticMeshMoveAction(modelView, Vec3.ORIGIN);
    }

    @Override
    public GenericRotateAction beginRotation(Vec3 center, byte dim1, byte dim2) {
        return new StaticMeshRotateAction(modelView, new Vec3(center), dim1, dim2);
    }

    @Override
    public GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ) {
        throw new WrongModeException("Unable to use squat tool outside animation editor mode");
    }

    @Override
    public GenericScaleAction beginScaling(Vec3 center) {
        return new StaticMeshScaleAction(modelView, center);
    }

    @Override
    public boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes) {
        for (IdObject object : modelView.getEditableIdObjects()) {
            double vertexSize1 = object.getClickRadius(axes) * axes.getZoom() * 2;
            if (AbstractModelEditor.hitTest(object.getPivotPoint(), CoordSysUtils.geomV2(axes, point), axes, vertexSize1)) {
                return true;
            }
            if (object instanceof CollisionShape) {
                for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
                    int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
                    if (AbstractModelEditor.hitTest(vertex, CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
                        return true;
                    }
                }
            }
        }
        for (Camera camera : modelView.getEditableCameras()) {
            int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
            if (AbstractModelEditor.hitTest(camera.getPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
                return true;
            }
            if (AbstractModelEditor.hitTest(camera.getTargetPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
                return true;
            }
        }

        if (selectionMode == SelectionItemTypes.VERTEX){
            for (Geoset geoset : modelView.getEditableGeosets()) {
                for (GeosetVertex geosetVertex : geoset.getVertices()) {
                    if (hitTest(geosetVertex, CoordSysUtils.geomV2(axes, point), axes, ProgramGlobals.getPrefs().getVertexSize())) {
                        return true;
                    }
                }
            }
        }


        if(selectionMode == SelectionItemTypes.CLUSTER){
            for (Geoset geoset : modelView.getEditableGeosets()) {
                for (Triangle triangle : geoset.getTriangles()) {
                    if (triHitTest(triangle, CoordSysUtils.geomV2(axes, point), axes)) {
                        return true;
                    }
                }
                for (GeosetVertex geosetVertex : geoset.getVertices()) {
                    if (hitTest(geosetVertex, CoordSysUtils.geomV2(axes, point), axes, ProgramGlobals.getPrefs().getVertexSize())) {
                        return true;
                    }
                }
            }
        }

        if(selectionMode == SelectionItemTypes.GROUP){
            for (Geoset geoset : modelView.getEditableGeosets()) {
                for (Triangle triangle : geoset.getTriangles()) {
                    if (triHitTest(triangle, CoordSysUtils.geomV2(axes, point), axes)) {
                        return true;
                    }
                }
                for (GeosetVertex geosetVertex : geoset.getVertices()) {
                    if (hitTest(geosetVertex, CoordSysUtils.geomV2(axes, point), axes, ProgramGlobals.getPrefs().getVertexSize())) {
                        return true;
                    }
                }
            }
        }

        if(selectionMode == SelectionItemTypes.FACE){
            for (Geoset geoset : modelView.getEditableGeosets()) {
                for (Triangle triangle : geoset.getTriangles()) {
                    if (triHitTest(triangle, CoordSysUtils.geomV2(axes, point), axes)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
