package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VPGeosetRendererImpl implements GeosetVisitor {
	private final VPTriangleRendererImpl triangleRenderer = new VPTriangleRendererImpl();
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private byte xDimension;
	private byte yDimension;
	private CoordinateSystem coordinateSystem;

	public VPGeosetRendererImpl() {

	}

	public VPGeosetRendererImpl reset(Graphics2D graphics, ProgramPreferences programPreferences, byte xDimension, byte yDimension, CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.coordinateSystem = coordinateSystem;
		this.triangleRenderer.reset(graphics, programPreferences, xDimension, yDimension, coordinateSystem);
		return this;
	}

	@Override
	public TriangleVisitor beginTriangle() {
		return triangleRenderer.reset(graphics, programPreferences, xDimension, yDimension, coordinateSystem);
	}

	@Override
	public void geosetFinished() {
	}
}

class VPTriangleRendererImpl implements TriangleVisitor {
	private final java.util.List<Point> previousVertices = new ArrayList<>();
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private byte xDimension;
	private byte yDimension;
	private CoordinateSystem coordinateSystem;

	VPTriangleRendererImpl() {

	}

	VPTriangleRendererImpl(Graphics2D graphics, ProgramPreferences programPreferences, byte xDimension, byte yDimension, CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.coordinateSystem = coordinateSystem;
	}

	public VPTriangleRendererImpl reset(Graphics2D graphics, ProgramPreferences programPreferences, byte xDimension, byte yDimension, CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.coordinateSystem = coordinateSystem;
		previousVertices.clear();
		return this;
	}

	@Override
	public VertexVisitor vertex(Vec3 vert, Vec3 normal, List<Bone> bones) {
		double firstCoord = vert.getCoord(xDimension);
		double secondCoord = vert.getCoord(yDimension);
		Point point = new Point((int) coordinateSystem.viewX(firstCoord), (int) coordinateSystem.viewY(secondCoord));

		if (previousVertices.size() > 0) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
		previousVertices.add(point);

		if (programPreferences.showNormals()) {
			Color triangleColor = graphics.getColor();

			graphics.setColor(programPreferences.getNormalsColor());
			double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);

			double firstNormalCoord = (normal.getCoord(xDimension) * 12) / zoom;
			double secondNormalCoord = (normal.getCoord(yDimension) * 12) / zoom;

			Point endPoint = new Point(
					(int) coordinateSystem.viewX(firstCoord + firstNormalCoord),
					(int) coordinateSystem.viewY(secondCoord + secondNormalCoord));
			graphics.drawLine(point.x, point.y, endPoint.x, endPoint.y);
			graphics.setColor(triangleColor);
		}
		return VertexVisitor.NO_ACTION;
	}


	@Override
	public VertexVisitor hdVertex(Vec3 vert, Vec3 normal, Bone[] skinBones, short[] skinBoneWeights) {
		return vertex(vert, normal, null);
	}

	@Override
	public void triangleFinished() {
		if (previousVertices.size() > 1) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			Point point = previousVertices.get(0);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
	}

}