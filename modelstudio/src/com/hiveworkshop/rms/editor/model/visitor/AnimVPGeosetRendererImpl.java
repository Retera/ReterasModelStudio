package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnimVPGeosetRendererImpl implements GeosetVisitor {
	RenderModel renderModel;
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private byte xDimension;
	private byte yDimension;
	private CoordinateSystem coordinateSystem;
	private AnimVPTriangleRendererImpl triangleRenderer;

	public AnimVPGeosetRendererImpl() {
		triangleRenderer = new AnimVPTriangleRendererImpl();
	}

	public AnimVPGeosetRendererImpl(Graphics2D graphics, ProgramPreferences programPreferences, byte xDimension, byte yDimension, CoordinateSystem coordinateSystem, RenderModel renderModel) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		triangleRenderer = new AnimVPTriangleRendererImpl(graphics, programPreferences, xDimension, yDimension, coordinateSystem, renderModel);
	}

	public AnimVPGeosetRendererImpl reset(Graphics2D graphics, ProgramPreferences programPreferences, byte xDimension, byte yDimension, CoordinateSystem coordinateSystem, RenderModel renderModel) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		return this;
	}

	@Override
	public TriangleVisitor beginTriangle() {
		return triangleRenderer.reset(graphics, programPreferences, xDimension, yDimension, coordinateSystem, renderModel);
	}

	@Override
	public void geosetFinished() {
	}
}

class AnimVPTriangleRendererImpl implements TriangleVisitor {
	private final java.util.List<Point> previousVertices = new ArrayList<>();
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private byte xDimension;
	private byte yDimension;
	private CoordinateSystem coordinateSystem;
	private RenderModel renderModel;

	AnimVPTriangleRendererImpl() {

	}

	AnimVPTriangleRendererImpl(Graphics2D graphics, ProgramPreferences programPreferences, byte xDimension, byte yDimension, CoordinateSystem coordinateSystem, RenderModel renderModel) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
	}

	public AnimVPTriangleRendererImpl reset(Graphics2D graphics, ProgramPreferences programPreferences, byte xDimension, byte yDimension, CoordinateSystem coordinateSystem, RenderModel renderModel) {
		previousVertices.clear();
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		return this;
	}

	@Override
	public VertexVisitor hdVertex(Vec3 vert, Vec3 normal, Bone[] skinBones, short[] skinBoneWeights) {
		Mat4 skinBonesMatrixSumHeap = ModelUtils.processHdBones(renderModel, skinBones, skinBoneWeights);

		processAndDraw(vert, normal, skinBonesMatrixSumHeap);

		return VertexVisitor.NO_ACTION;
	}

	@Override
	public VertexVisitor vertex(Vec3 vert, Vec3 normal, List<Bone> bones) {
		Mat4 bonesMatrixSumHeap = ModelUtils.processSdBones(renderModel, bones);

		processAndDraw(vert, normal, bonesMatrixSumHeap);
		return VertexVisitor.NO_ACTION;
	}

	public void processAndDraw(Vec3 v, Vec3 normal, Mat4 skinBonesMatrixSumHeap) {
		Vec4 vertexHeap = new Vec4(v, 1);
		Vec4 vertexSumHeap = Vec4.getTransformed(vertexHeap, skinBonesMatrixSumHeap);

		Vec4 normalHeap = new Vec4(normal, 0);
		Vec4 normalSumHeap = Vec4.getTransformed(normalHeap, skinBonesMatrixSumHeap);
		normalSumHeap.normalize();

		drawLineFromVert(normalSumHeap, vertexSumHeap);
	}

	public void drawLineFromVert(Vec4 normalSumHeap, Vec4 vertexSumHeap) {
		float firstCoord = vertexSumHeap.getVec3().getCoord(xDimension);
		float secondCoord = vertexSumHeap.getVec3().getCoord(yDimension);
		Point point = new Point((int) coordinateSystem.viewX(firstCoord), (int) coordinateSystem.viewY(secondCoord));

		if (previousVertices.size() > 0) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
		previousVertices.add(point);
		// graphics.setColor(programPreferences.getVertexColor());
		// graphics.fillRect((int) firstCoord - vertexSize / 2, (int)
		// secondCoord - vertexSize / 2, vertexSize, vertexSize);
		if (programPreferences.showNormals()) {
			drawNormal(firstCoord, secondCoord, point, normalSumHeap);
		}
	}

	@Override
	public void triangleFinished() {
		if (previousVertices.size() > 1) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			Point point = previousVertices.get(0);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
	}

	public void drawNormal(float firstCoord, float secondCoord, Point point, Vec4 normalSumHeap) {
		Color triangleColor = graphics.getColor();

		float firstNormalCoord = normalSumHeap.getVec3().getCoord(xDimension);
		float secondNormalCoord = normalSumHeap.getVec3().getCoord(yDimension);

		graphics.setColor(programPreferences.getNormalsColor());
		double zoom = CoordinateSystem.getZoom(coordinateSystem);

		double normXsize = (firstNormalCoord * 12) / zoom;
		double normYsize = (secondNormalCoord * 12) / zoom;
		double normEndX3 = coordinateSystem.viewX(firstCoord + normXsize);
		double normEndY3 = coordinateSystem.viewY(secondCoord + normYsize);
		Point endPoint3 = new Point((int) normEndX3, (int) normEndY3);

		double normEndX2 = coordinateSystem.viewX(firstCoord + (firstNormalCoord * 12 / zoom));
		double normEndY2 = coordinateSystem.viewY(secondCoord + (secondNormalCoord * 12 / zoom));
		Point endPoint2 = new Point((int) normEndX2, (int) normEndY2);

		double normEndX = firstCoord + (firstNormalCoord * 12 / zoom);
		double normEndY = secondCoord + (secondNormalCoord * 12 / zoom);
		Point endPoint = new Point((int) coordinateSystem.viewX(normEndX), (int) coordinateSystem.viewY(normEndY));

		graphics.drawLine(point.x, point.y, endPoint.x, endPoint.y);
		graphics.setColor(triangleColor);
	}

}