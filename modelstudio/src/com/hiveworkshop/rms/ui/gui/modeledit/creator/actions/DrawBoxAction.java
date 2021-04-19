package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.ModelUtils.Mesh;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class DrawBoxAction implements GenericMoveAction {

	private final byte dim1;
	private final byte dim2;
	private final Vec2 p1;
	private final Vec2 p2;
	private Mesh box;
	private Vec2 planeMin;
	private Vec2 planeMax;
	private final Geoset planeGeoset;
	private double zHeight;

	public DrawBoxAction(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                     int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments,
	                     Geoset planeGeoset) {
		this.p1 = p1;
		this.p2 = p2;
		this.planeGeoset = planeGeoset;
		this.dim1 = dim1;
		this.dim2 = dim2;

		makePlaneFromPoints(p1, p2, dim1, dim2, facingVector, numberOfLengthSegments, numberOfWidthSegments, numberOfHeightSegments);
	}

	public void makePlaneFromPoints(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                                int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments) {

		planeMin = new Vec2(p1).minimize(p2);
		planeMax = new Vec2(p1).maximize(p2);

		Vec3 dummy1 = new Vec3(0, 0, 0);
		dummy1.setCoords(dim1, dim2, planeMin);

		Vec3 dummy2 = new Vec3(0, 0, 0);
		dummy2.setCoords(dim1, dim2, planeMax);
		dummy2.setCoord(CoordinateSystem.Util.getUnusedXYZ(dim1, dim2), 1);
		zHeight = 1;

		box = ModelUtils.createBox(dummy1, dummy2, numberOfLengthSegments, numberOfWidthSegments, numberOfHeightSegments, planeGeoset);
	}

	@Override
	public void undo() {
		for (GeosetVertex vertex : box.getVertices()) {
			planeGeoset.remove(vertex);
		}
		for (Triangle triangle : box.getTriangles()) {
			planeGeoset.remove(triangle);
		}
	}

	@Override
	public void redo() {
		for (GeosetVertex vertex : box.getVertices()) {
			planeGeoset.add(vertex);
		}
		for (Triangle triangle : box.getTriangles()) {
			planeGeoset.add(triangle);
		}
	}

	@Override
	public String actionName() {
		return "create box";
	}

	public Mesh getPlane() {
		return box;
	}

	@Override
	public void updateTranslation(double deltaX, double deltaY, double deltaZ) {
		p2.translate(deltaX, deltaY);
		scalePlaneToPoints(p1, p2, zHeight + deltaZ);
	}

	public void scalePlaneToPoints(Vec2 p1, Vec2 p2, double newZHeight) {
		Vec2 min = new Vec2(p1).minimize(p2);
		Vec2 max = new Vec2(p1).maximize(p2);

		Vec2 scale = Vec2.getDif(max, min).div(Vec2.getDif(planeMax, planeMin));

		double scaleZ = newZHeight / zHeight;

		for (GeosetVertex vertex : box.getVertices()) {
			shiftPlanePoint(vertex, min, scale, scaleZ);
		}
		zHeight = newZHeight;

		planeMin = min;
		planeMax = max;
	}

	public void shiftPlanePoint(Vec3 vertex, Vec2 newMin, Vec2 scale, double scaleZ) {
		Vec2 shift = vertex.getProjected(dim1, dim2);
		shift.sub(planeMin).mul(scale).add(newMin);

		vertex.setCoords(dim1, dim2, shift);

		byte unusedXYZ = CoordinateSystem.Util.getUnusedXYZ(dim1, dim2);
		vertex.setCoord(unusedXYZ, vertex.getCoord(unusedXYZ) * scaleZ);
	}
}
