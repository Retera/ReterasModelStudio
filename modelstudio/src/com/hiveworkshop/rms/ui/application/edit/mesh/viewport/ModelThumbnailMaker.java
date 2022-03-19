package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelThumbnailMaker {

	public static BufferedImage getBufferedImage(Color backgroundColor, EditableModel model, int SIZE, Vec2[] boundSize) {

		BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(backgroundColor);
		graphics.fill3DRect(0, 0, SIZE, SIZE, true);
		graphics.setColor(backgroundColor.brighter());
		graphics.fill3DRect(SIZE / 8, SIZE / 8, SIZE - SIZE / 4, SIZE - SIZE / 4, true);
//			System.out.println("creating icon for model: " + model.getName());
//			System.out.println("nr geosets: " + model.getGeosets().size());
//			System.out.println("bounds: " + Arrays.toString(getModelBoundsSize(model)));
		if(boundSize == null){
			boundSize = getBoundBoxSize(model, (byte) 1, (byte) 2);
		}

		scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(SIZE, SIZE), boundSize);

		drawGeosetsFlat(model, graphics, (byte) 1, (byte) 2, Color.GRAY);
		graphics.dispose();
		return image;
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

	public static void drawBoneMarker(Graphics g, byte a, byte b, Vec3 boneMarker) {
		g.setColor(Color.YELLOW);
		if (boneMarker != null) {
			drawCrossHair(g, a, b, boneMarker);
		}
	}
}
