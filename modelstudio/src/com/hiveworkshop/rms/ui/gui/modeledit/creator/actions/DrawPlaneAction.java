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

	private final byte firstDimension;
	private final byte secondDimension;
	private double x2;
	private double y2;
	private final double x;
	private final double y;
	private Mesh plane;
	private double planeMinX;
	private double planeMinY;
	private double planeMaxX;
	private double planeMaxY;
	private final Geoset planeGeoset;

	public DrawPlaneAction(final double x, final double y, final double x2, final double y2, final byte dim1,
						   final byte dim2, final Vec3 facingVector, final int numberOfWidthSegments,
						   final int numberOfHeightSegments, final Geoset planeGeoset) {
		this.x = x;
		this.y = y;
		this.x2 = x2;
		this.y2 = y2;
		this.planeGeoset = planeGeoset;
		firstDimension = dim1;
		secondDimension = dim2;

		makePlaneFromPoints(x, y, x2, y2, dim1, dim2, facingVector, numberOfWidthSegments, numberOfHeightSegments);
	}

	public void makePlaneFromPoints(final double x, final double y, final double x2, final double y2, final byte dim1,
									final byte dim2, final Vec3 facingVector, final int numberOfWidthSegments,
									final int numberOfHeightSegments) {
		planeMinX = Math.min(x, x2);
		planeMinY = Math.min(y, y2);
		planeMaxX = Math.max(x, x2);
		planeMaxY = Math.max(y, y2);

		plane = ModelUtils.createPlane(dim1, dim2, facingVector, 0, planeMinX, planeMinY, planeMaxX, planeMaxY,
				numberOfWidthSegments, numberOfHeightSegments);
		for (final GeosetVertex vertex : plane.getVertices()) {
			vertex.addTVertex(new Vec2(0, 0));
			vertex.setGeoset(planeGeoset);
		}
		for (final Triangle triangle : plane.getTriangles()) {
			triangle.setGeoset(planeGeoset);
			for (final GeosetVertex vertex : triangle.getVerts()) {
				vertex.getTriangles().add(triangle);
			}
		}
	}

	public void scalePlaneToPoints(final double x, final double y, final double x2, final double y2) {
		final double minX = Math.min(x, x2);
		final double minY = Math.min(y, y2);
		final double maxX = Math.max(x, x2);
		final double maxY = Math.max(y, y2);
		final double scaleX = (maxX - minX) / (planeMaxX - planeMinX);
		final double scaleY = (maxY - minY) / (planeMaxY - planeMinY);

		for (final GeosetVertex vertex : plane.getVertices()) {
			shiftPlanePoint(vertex, minX, minY, scaleX, scaleY);
		}

		planeMinX = minX;
		planeMinY = minY;
		planeMaxX = maxX;
		planeMaxY = maxY;
	}

	public void shiftPlanePoint(final Vec3 vertex, final double newMinX, final double newMinY, final double scaleX,
								final double scaleY) {
		final double vertexX = vertex.getCoord(firstDimension);
		vertex.setCoord(firstDimension, (vertexX - planeMinX) * scaleX + newMinX);
		final double vertexY = vertex.getCoord(secondDimension);
		vertex.setCoord(secondDimension, (vertexY - planeMinY) * scaleY + newMinY);
	}

	@Override
	public void undo() {
		for (final GeosetVertex vertex : plane.getVertices()) {
			planeGeoset.remove(vertex);
		}
		for (final Triangle triangle : plane.getTriangles()) {
			planeGeoset.remove(triangle);
		}
	}

	@Override
	public void redo() {
		for (final GeosetVertex vertex : plane.getVertices()) {
			planeGeoset.add(vertex);
		}
		for (final Triangle triangle : plane.getTriangles()) {
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
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		x2 += deltaX;
		y2 += deltaY;
		scalePlaneToPoints(x, y, x2, y2);
	}

}
