package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;

public class VPGeosetRenderer extends GeosetVisitor {
	boolean isAnimated;

	public VPGeosetRenderer() {
	}

	public VPGeosetRenderer reset(Graphics2D graphics,
	                              ProgramPreferences programPreferences,
	                              CoordinateSystem coordinateSystem,
	                              RenderModel renderModel,
	                              Geoset geoset, boolean isAnimated) {
		this.isAnimated = isAnimated;
		this.geoset = geoset;
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		return this;
	}

	@Override
	public void beginTriangle(boolean isHD) {
		renderGeosetTries(geoset, isHD);
	}

	private void renderGeosetTries(Geoset geoset, boolean isHD) {
		for (Triangle triangle : geoset.getTriangles()) {
			previousVertices.clear();
			for (GeosetVertex vertex : triangle.getVerts()) {
				vertex(vertex, isHD);
			}
			triangleFinished();
		}
	}

	public void vertex(GeosetVertex vertex, Boolean isHd) {
		Vec3 vertexSumHeap = vertex;
		Vec3 normal = vertex.getNormal();
		Vec3 normalSumHeap = normal;
		if (isAnimated) {
			List<Bone> bones1 = vertex.getBoneAttachments();
			Mat4 bonesMatrixSumHeap;
			if (isHd) {
				bonesMatrixSumHeap = ModelUtils.processHdBones(renderModel, vertex.getSkinBones());
			} else {
				bonesMatrixSumHeap = ModelUtils.processSdBones(renderModel, bones1);
			}
			vertexSumHeap = Vec3.getTransformed(vertex, bonesMatrixSumHeap);
			if (normal != null) {
				normalSumHeap = Vec3.getTransformed(normal, bonesMatrixSumHeap);
				normalSumHeap.normalize();
			}

		}

		drawLineFromVert(vertexSumHeap, normalSumHeap);
	}

	public void triangleFinished() {
		if (previousVertices.size() > 1) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			Point point = previousVertices.get(0);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
	}

	public void processAndDraw(GeosetVertex vertex, Mat4 bonesMatrixSumHeap) {
		Vec3 vertexSumHeap = Vec3.getTransformed(vertex, bonesMatrixSumHeap);

		Vec3 normal = vertex.getNormal() == null ? new Vec3(0, 0, 0) : vertex.getNormal();
		Vec3 normalSumHeap = Vec3.getTransformed(normal, bonesMatrixSumHeap);
		normalSumHeap.normalize();

		drawLineFromVert(vertexSumHeap, normalSumHeap);
	}

	public void drawLineFromVert(Vec3 vertex, Vec3 normal) {
		float firstCoord = vertex.getCoord(coordinateSystem.getPortFirstXYZ());
		float secondCoord = vertex.getCoord(coordinateSystem.getPortSecondXYZ());

		Point point = new Point((int) coordinateSystem.viewX(firstCoord), (int) coordinateSystem.viewY(secondCoord));
		if (previousVertices.size() > 0) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
		previousVertices.add(point);

		if (programPreferences.showNormals() && normal != null) {
			drawNormal(firstCoord, secondCoord, point, normal);
		}
	}

	public void drawNormal(float firstCoord, float secondCoord, Point point, Vec3 normal) {
		Color triangleColor = graphics.getColor();
		graphics.setColor(programPreferences.getNormalsColor());

		float firstNormalCoord = normal.getCoord(coordinateSystem.getPortFirstXYZ());
		float secondNormalCoord = normal.getCoord(coordinateSystem.getPortSecondXYZ());

		double zoom = CoordSysUtils.getZoom(coordinateSystem);

		double normEndX = firstCoord + (firstNormalCoord * 12 / zoom);
		double normEndY = secondCoord + (secondNormalCoord * 12 / zoom);
		Point endPoint = new Point(
				(int) coordinateSystem.viewX(normEndX),
				(int) coordinateSystem.viewY(normEndY));

		graphics.drawLine(point.x, point.y, endPoint.x, endPoint.y);
		graphics.setColor(triangleColor);
	}

}