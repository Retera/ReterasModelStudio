package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.model.visitor.TriangleVisitor;
import com.hiveworkshop.rms.editor.model.visitor.VertexVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnimatedViewportModelRenderer implements ModelVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final GeosetRendererImpl geosetRenderer;
	private final int vertexSize;
	private byte xDimension;
	private byte yDimension;
	private ViewportView viewportView;
	private CoordinateSystem coordinateSystem;
	private final ResettableAnimatedIdObjectRenderer idObjectRenderer;
	// TODO Now that I added modelView to this class, why does RenderByViewModelRenderer exist???
	private ModelView modelView;
	private RenderModel renderModel;

	public AnimatedViewportModelRenderer(int vertexSize) {
		this.vertexSize = vertexSize;
		geosetRenderer = new GeosetRendererImpl();
		idObjectRenderer = new ResettableAnimatedIdObjectRenderer(vertexSize);
	}

	public AnimatedViewportModelRenderer reset(Graphics2D graphics,
	                                           ProgramPreferences programPreferences,
	                                           byte xDimension, byte yDimension,
	                                           ViewportView viewportView,
	                                           CoordinateSystem coordinateSystem,
	                                           ModelView modelView,
	                                           RenderModel renderModel) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.viewportView = viewportView;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelView;
		this.renderModel = renderModel;
		idObjectRenderer.reset(coordinateSystem, graphics, programPreferences.getLightsColor(), programPreferences.getAnimatedBoneUnselectedColor(), NodeIconPalette.UNSELECTED, renderModel, programPreferences.isUseBoxesForPivotPoints());
		return this;
	}

	@Override
	public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
		graphics.setColor(programPreferences.getTriangleColor());
		if (modelView.getHighlightedGeoset() == modelView.getModel().getGeoset(geosetId)) {
			graphics.setColor(programPreferences.getHighlighTriangleColor());
		} else {
			Geoset geoset = modelView.getModel().getGeoset(geosetId);
			if (!modelView.getEditableGeosets().contains(geoset)) {
				graphics.setColor(programPreferences.getVisibleUneditableColor());
			}
		}
		return geosetRenderer.reset();
	}

	@Override
	public void bone(Bone object) {
		resetIdObjectRendererWithNode(object);
		idObjectRenderer.bone(object);
	}

	private void resetIdObjectRendererWithNode(IdObject object) {
		idObjectRenderer.reset(coordinateSystem, graphics,
				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor() : programPreferences.getLightsColor(),
				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor() : programPreferences.getAnimatedBoneUnselectedColor(),
				modelView.getHighlightedNode() == object ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED,
				renderModel, programPreferences.isUseBoxesForPivotPoints());
	}

	@Override
	public void light(Light light) {
		resetIdObjectRendererWithNode(light);
		idObjectRenderer.light(light);
	}

	@Override
	public void helper(Helper object) {
		resetIdObjectRendererWithNode(object);
		idObjectRenderer.helper(object);
	}

	@Override
	public void attachment(Attachment attachment) {
		resetIdObjectRendererWithNode(attachment);
		idObjectRenderer.attachment(attachment);
	}

	@Override
	public void particleEmitter(ParticleEmitter particleEmitter) {
		resetIdObjectRendererWithNode(particleEmitter);
		idObjectRenderer.particleEmitter(particleEmitter);
	}

	@Override
	public void particleEmitter2(ParticleEmitter2 particleEmitter) {
		resetIdObjectRendererWithNode(particleEmitter);
		idObjectRenderer.particleEmitter2(particleEmitter);
	}

	@Override
	public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
		resetIdObjectRendererWithNode(popcornFxEmitter);
		idObjectRenderer.popcornFxEmitter(popcornFxEmitter);
	}

	@Override
	public void ribbonEmitter(RibbonEmitter ribbonEmitter) {
		resetIdObjectRendererWithNode(ribbonEmitter);
		idObjectRenderer.ribbonEmitter(ribbonEmitter);
	}

	@Override
	public void eventObject(EventObject eventObject) {
		resetIdObjectRendererWithNode(eventObject);
		idObjectRenderer.eventObject(eventObject);
	}

	@Override
	public void collisionShape(CollisionShape collisionShape) {
		resetIdObjectRendererWithNode(collisionShape);
		idObjectRenderer.collisionShape(collisionShape);

	}

	@Override
	public void camera(Camera cam) {
		idObjectRenderer.camera(cam);
	}

	public void drawNormal(float firstCoord, float secondCoord, Point point, Vec4 normalSumHeap) {
		Color triangleColor = graphics.getColor();

		float firstNormalCoord = normalSumHeap.getVec3().getCoord(xDimension);
		float secondNormalCoord = normalSumHeap.getVec3().getCoord(yDimension);

		graphics.setColor(programPreferences.getNormalsColor());
		double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);

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

	private class GeosetRendererImpl implements GeosetVisitor {
		private TriangleRendererImpl triangleRenderer = new TriangleRendererImpl();

		public GeosetRendererImpl reset() {
			return this;
		}

		@Override
		public TriangleVisitor beginTriangle() {
			return triangleRenderer.reset();
		}

		@Override
		public void geosetFinished() {
		}
	}

	public void normalizeHeap(Vec4 heap) {
		if (heap.length() > 0) {
			heap.normalize();
		} else {
			heap.set(0, 1, 0, 0);
		}
	}

	private final class TriangleRendererImpl implements TriangleVisitor {
		private final List<Point> previousVertices = new ArrayList<>();

		public TriangleRendererImpl reset() {
			previousVertices.clear();
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
			normalizeHeap(normalSumHeap);

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

	}

}
