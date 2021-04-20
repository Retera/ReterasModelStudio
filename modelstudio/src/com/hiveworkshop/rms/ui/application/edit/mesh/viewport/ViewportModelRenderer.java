package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.model.visitor.TriangleVisitor;
import com.hiveworkshop.rms.editor.model.visitor.VertexVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableIdObjectRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewportModelRenderer implements ModelVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final GeosetRendererImpl geosetRenderer;
	private final int vertexSize;
	private byte xDimension;
	private byte yDimension;
	private ViewportView viewportView;
	private CoordinateSystem coordinateSystem;
	private final ResettableIdObjectRenderer idObjectRenderer;
	private ModelView modelView;

	public ViewportModelRenderer(final int vertexSize) {
		this.vertexSize = vertexSize;
		geosetRenderer = new GeosetRendererImpl();
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public static void drawGeosetsFlat(EditableModel model, Graphics g, byte a, byte b, Color color) {
//		g.setColor(color);
		for (Geoset geo : model.getGeosets()) {
			drawGeosetFlat(g, a, b, geo, color);
		}
	}

	public static void drawGeosetFlat(Graphics g, byte a, byte b, Geoset geo, Color color) {
		g.setColor(color);
		for (Triangle t : geo.getTriangles()) {
			drawTriangle(g, a, b, t);
		}
	}

	public static void drawFilteredTriangles2(EditableModel model, Graphics g,
	                                          byte a, byte b, Map<Geoset, Map<Bone, List<GeosetVertex>>> boneMap, Bone bone) {
		List<Triangle> triangles = getBoneParentedTriangles(model, boneMap, bone);

		g.setColor(Color.RED);
		for (Triangle t : triangles) {
			drawTriangle(g, a, b, t);
		}
	}

	private static List<Triangle> getBoneParentedTriangles(EditableModel model, Map<Geoset, Map<Bone, List<GeosetVertex>>> boneMap, Bone bone) {
		List<Triangle> triangles = new ArrayList<>();
		for (Geoset geo : model.getGeosets()) {
			if (boneMap.containsKey(geo) && boneMap.get(geo).containsKey(bone)) {
				for (GeosetVertex vertex : boneMap.get(geo).get(bone)) {
					for (Triangle t : vertex.getTriangles()) {
						if (!triangles.contains(t)) {
							triangles.add(t);
						}
					}
				}
			}
		}
		return triangles;
	}

	public static void scaleAndTranslateGraphic(Graphics2D g, Rectangle bounds, Vec2[] realBounds) {
		Vec2 delta = Vec2.getDif(realBounds[1], realBounds[0]);
		double boxSize = Math.max(delta.x, delta.y);

		Vec2 boxOffset = Vec2.getScaled(delta, -.5f).add(new Vec2(boxSize / 2f, boxSize / 2f)).add(realBounds[1]);

		g.scale(bounds.getWidth() / boxSize, bounds.getHeight() / boxSize);
		g.translate(boxOffset.x, boxOffset.y);
	}

	public static Vec2[] getBoundBoxSize(EditableModel model, byte a, byte b) {
		Vec2[] realBoxBounds = {new Vec2(Float.MAX_VALUE, Float.MAX_VALUE), new Vec2(Float.MIN_VALUE, Float.MIN_VALUE)};

		for (Geoset geo : model.getGeosets()) {
			for (Triangle t : geo.getTriangles()) {
				Vec2[] projectedVerts = t.getProjectedVerts(a, b);
				for (Vec2 v : projectedVerts) {
					realBoxBounds[0].minimize(v);
					realBoxBounds[1].maximize(v);
				}
			}
		}
		return realBoxBounds;
	}

	public static void drawCrossHair(Graphics g, byte a, byte b, Vec3 extraHighlightPoint) {
		int x = (int) extraHighlightPoint.getCoord(a);
		int y = (int) -extraHighlightPoint.getCoord(b);
		g.drawOval(x - 5, y - 5, 10, 10);
		g.drawLine(x, y - 10, x, y + 10);
		g.drawLine(x - 10, y, x + 10, y);
	}

	private static void drawTriangle(Graphics g, byte a, byte b, Triangle t) {
		double[] x = t.getCoords(a);
		double[] y = t.getCoords(b);
		int[] xInt = new int[4];
		int[] yInt = new int[4];
		for (int ix = 0; ix < 3; ix++) {
			xInt[ix] = (int) Math.round(x[ix]);
			yInt[ix] = (int) Math.round(-y[ix]);
		}
		xInt[3] = xInt[0];
		yInt[3] = yInt[0];
		g.drawPolyline(xInt, yInt, 4);
	}

	public ViewportModelRenderer reset(Graphics2D graphics, ProgramPreferences programPreferences,
	                                   byte xDimension, byte yDimension, ViewportView viewportView,
	                                   CoordinateSystem coordinateSystem, ModelView modelView) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.viewportView = viewportView;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelView;
		idObjectRenderer.reset(coordinateSystem, graphics, programPreferences.getLightsColor(),
				programPreferences.getPivotPointsColor(), NodeIconPalette.UNSELECTED,
				programPreferences.isUseBoxesForPivotPoints());
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
				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor() : programPreferences.getPivotPointsColor(),
				modelView.getHighlightedNode() == object ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED,
				programPreferences.isUseBoxesForPivotPoints());
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

	private final class GeosetRendererImpl implements GeosetVisitor {
		private final TriangleRendererImpl triangleRenderer = new TriangleRendererImpl();

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

	public static void drawBoneMarker(Graphics g, byte a, byte b, Vec3 boneMarker) {
		g.setColor(Color.YELLOW);
		if (boneMarker != null) {
			drawCrossHair(g, a, b, boneMarker);
		}
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

	private final class TriangleRendererImpl implements TriangleVisitor {
		private final List<Point> previousVertices = new ArrayList<>();

		public TriangleRendererImpl reset() {
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
}
