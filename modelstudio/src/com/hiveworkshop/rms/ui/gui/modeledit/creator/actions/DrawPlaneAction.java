package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.ModelUtils.Mesh;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class DrawPlaneAction implements GenericMoveAction {

	private final byte dim1;
	private final byte dim2;
	private final Vec2 p1;
	private final Vec2 p2;
	private Mesh plane;
	private Vec2 planeMin;
	private Vec2 planeMax;
	private final Geoset planeGeoset;

	public DrawPlaneAction(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                       int numberOfWidthSegments, int numberOfHeightSegments,
	                       Geoset planeGeoset) {
		this.p1 = p1;
		this.p2 = p2;
		this.planeGeoset = planeGeoset;
		this.dim1 = dim1;
		this.dim2 = dim2;

		makePlaneFromPoints(p1, p2, dim1, dim2, facingVector, numberOfWidthSegments, numberOfHeightSegments);
	}

	public void makePlaneFromPoints(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                                int numberOfWidthSegments, int numberOfHeightSegments) {
		planeMin = new Vec2(p1).minimize(p2);
		planeMax = new Vec2(p1).maximize(p2);

		plane = ModelUtils.createPlane(dim1, dim2, facingVector, 0, planeMin, planeMax, numberOfWidthSegments, numberOfHeightSegments);

		for (GeosetVertex vertex : plane.getVertices()) {
//			vertex.addTVertex(new Vec2(0, 0));
			vertex.setGeoset(planeGeoset);
		}
		for (Triangle triangle : plane.getTriangles()) {
			triangle.setGeoset(planeGeoset);
//			for (GeosetVertex vertex : triangle.getVerts()) {
//				vertex.addTriangle(triangle);
//			}
		}
	}

	@Override
	public void undo() {
		for (GeosetVertex vertex : plane.getVertices()) {
			planeGeoset.remove(vertex);
		}
		for (Triangle triangle : plane.getTriangles()) {
			planeGeoset.remove(triangle);
		}
	}

	@Override
	public void redo() {
		for (GeosetVertex vertex : plane.getVertices()) {
			planeGeoset.add(vertex);
		}
		for (Triangle triangle : plane.getTriangles()) {
			planeGeoset.add(triangle);
		}
	}

	@Override
	public String actionName() {
		return "create plane";
	}

	public Mesh getPlane() {
		return plane;
	}

	@Override
	public void updateTranslation(double deltaX, double deltaY, double deltaZ) {
		p2.translate(deltaX, deltaY);
		scalePlaneToPoints(p1, p2);
	}

	public void scalePlaneToPoints(Vec2 p1, Vec2 p2) {
		Vec2 min = new Vec2(p1).minimize(p2);
		Vec2 max = new Vec2(p1).maximize(p2);
		Vec2 scale = Vec2.getDif(max, min).div(Vec2.getDif(planeMax, planeMin));

		for (GeosetVertex vertex : plane.getVertices()) {
			shiftPlanePoint(vertex, min, scale);
		}

		planeMin = min;
		planeMax = max;
	}

	public void shiftPlanePoint(Vec3 vertex, Vec2 newMin, Vec2 scale) {
		Vec2 shift = vertex.getProjected(dim1, dim2);
		shift.sub(planeMin).mul(scale).add(newMin);

		vertex.setCoords(dim1, dim2, shift);
	}
}
