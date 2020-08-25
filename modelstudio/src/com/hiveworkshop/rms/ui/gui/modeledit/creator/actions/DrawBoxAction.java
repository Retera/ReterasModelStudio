package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.ModelUtils.Mesh;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;

public class DrawBoxAction implements GenericMoveAction {

	private final byte firstDimension;
	private final byte secondDimension;
	private double x2;
	private double y2;
	private final double x;
	private final double y;
	private Mesh box;
	private double planeMinX;
	private double planeMinY;
	private double planeMaxX;
	private double planeMaxY;
	private final Geoset planeGeoset;
	private final Vertex dummy1, dummy2;
	private double zHeight;

	public DrawBoxAction(final double x, final double y, final double x2, final double y2, final byte dim1,
			final byte dim2, final Vertex facingVector, final int numberOfLengthSegments,
			final int numberOfWidthSegments, final int numberOfHeightSegments, final Geoset planeGeoset) {
		this.x = x;
		this.y = y;
		this.x2 = x2;
		this.y2 = y2;
		this.planeGeoset = planeGeoset;
		firstDimension = dim1;
		secondDimension = dim2;

		dummy1 = new Vertex(0, 0, 0);
		dummy2 = new Vertex(0, 0, 0);
		makePlaneFromPoints(x, y, x2, y2, dim1, dim2, facingVector, numberOfLengthSegments, numberOfWidthSegments,
				numberOfHeightSegments);
	}

	public void makePlaneFromPoints(final double x, final double y, final double x2, final double y2, final byte dim1,
			final byte dim2, final Vertex facingVector, final int numberOfLengthSegments,
			final int numberOfWidthSegments, final int numberOfHeightSegments) {
		planeMinX = Math.min(x, x2);
		planeMinY = Math.min(y, y2);
		planeMaxX = Math.max(x, x2);
		planeMaxY = Math.max(y, y2);

		dummy1.set(Vertex.ORIGIN);
		dummy1.setCoord(dim1, planeMinX);
		dummy1.setCoord(dim2, planeMinY);
		dummy2.set(Vertex.ORIGIN);
		dummy2.setCoord(dim1, planeMaxX);
		dummy2.setCoord(dim2, planeMaxY);
		dummy2.setCoord(CoordinateSystem.Util.getUnusedXYZ(dim1, dim2), 1);
		zHeight = 1;

		box = ModelUtils.createBox(dummy1, dummy2, numberOfLengthSegments, numberOfWidthSegments,
				numberOfHeightSegments, planeGeoset);
	}

	public void scalePlaneToPoints(final double x, final double y, final double x2, final double y2,
			final double newZHeight) {
		final double minX = Math.min(x, x2);
		final double minY = Math.min(y, y2);
		final double maxX = Math.max(x, x2);
		final double maxY = Math.max(y, y2);
		final double scaleX = (maxX - minX) / (planeMaxX - planeMinX);
		final double scaleY = (maxY - minY) / (planeMaxY - planeMinY);
		final double scaleZ = newZHeight / zHeight;

		for (final GeosetVertex vertex : box.getVertices()) {
			shiftPlanePoint(vertex, minX, minY, scaleX, scaleY, scaleZ);
		}
		zHeight = newZHeight;

		planeMinX = minX;
		planeMinY = minY;
		planeMaxX = maxX;
		planeMaxY = maxY;
	}

	public void shiftPlanePoint(final Vertex vertex, final double newMinX, final double newMinY, final double scaleX,
			final double scaleY, final double scaleZ) {
		final double vertexX = vertex.getCoord(firstDimension);
		vertex.setCoord(firstDimension, ((vertexX - planeMinX) * scaleX) + newMinX);
		final double vertexY = vertex.getCoord(secondDimension);
		vertex.setCoord(secondDimension, ((vertexY - planeMinY) * scaleY) + newMinY);
		final byte unusedXYZ = CoordinateSystem.Util.getUnusedXYZ(firstDimension, secondDimension);
		vertex.setCoord(unusedXYZ, vertex.getCoord(unusedXYZ) * scaleZ);
	}

	@Override
	public void undo() {
		for (final GeosetVertex vertex : box.getVertices()) {
			planeGeoset.remove(vertex);
		}
		for (final Triangle triangle : box.getTriangles()) {
			planeGeoset.remove(triangle);
		}
	}

	@Override
	public void redo() {
		for (final GeosetVertex vertex : box.getVertices()) {
			planeGeoset.add(vertex);
		}
		for (final Triangle triangle : box.getTriangles()) {
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
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		x2 += deltaX;
		y2 += deltaY;
		scalePlaneToPoints(x, y, x2, y2, zHeight + deltaZ);
	}

}
