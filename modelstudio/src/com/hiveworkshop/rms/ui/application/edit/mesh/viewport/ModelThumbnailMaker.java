package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

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
			boundSize = getBoundBoxSize(model, Vec3.Y_AXIS, Vec3.Z_AXIS);
		}

		scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(SIZE, SIZE), boundSize);

		for (Geoset geo : model.getGeosets()) {
			drawGeosetFlat(graphics, Vec3.Y_AXIS, Vec3.Z_AXIS, geo, Color.GRAY);
		}
		graphics.dispose();
		return image;
	}

	public static void drawGeosetFlat(Graphics g, Vec3 right, Vec3 up, Geoset geo, Color color) {
		g.setColor(color);
		for (Triangle t : geo.getTriangles()) {
			drawTriangle(g, right, up, t);
		}
	}

	public static void drawFilteredTriangles2(EditableModel model, Graphics g,
	                                          Vec3 right, Vec3 up,
	                                          Map<Geoset, Map<Bone, List<GeosetVertex>>> boneMap,
	                                          Bone bone) {
		List<Triangle> triangles = getBoneParentedTriangles(model, boneMap, bone);

		g.setColor(Color.RED);
		for (Triangle t : triangles) {
			drawTriangle(g, right, up, t);
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

		Vec2 boxOffset = delta.scale(-.5f).translate(boxSize / 2f, boxSize / 2f).add(realBounds[1]);

		g.scale(bounds.getWidth() / boxSize, bounds.getHeight() / boxSize);
		g.translate(boxOffset.x, boxOffset.y);
	}

	public static Vec2[] getBoundBoxSize(EditableModel model, Vec3 right, Vec3 up) {
		Vec3 maxBound = new Vec3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		Vec3 minBound = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

		for (Geoset geo : model.getGeosets()) {
			for (Triangle t : geo.getTriangles()) {
				for (Vec3 v : t.getVerts()) {
					minBound.minimize(v);
					maxBound.maximize(v);
				}
			}
		}

		Vec2 maxBoundV2 = new Vec2(maxBound.dot(right), maxBound.dot(up));
		Vec2 minBoundV2 = new Vec2(minBound.dot(right), minBound.dot(up));

		return new Vec2[]{minBoundV2, maxBoundV2};
	}

	private static Set<Triangle> getBoneParentedTriangles(Map<Geoset, Map<Bone, List<GeosetVertex>>> boneMap, Bone bone) {
		Set<Triangle> triangles = new LinkedHashSet<>();
		for (Geoset geo : boneMap.keySet()) {
			List<GeosetVertex> boneVerts = boneMap.get(geo).get(bone);
			if (boneVerts != null) {
				for (GeosetVertex vertex : boneVerts) {
					for (Triangle t : vertex.getTriangles()) {
						triangles.add(t);
					}
				}
			}
		}
		return triangles;
	}
	public static Vec2[] getBoundBoxSize(Collection<Geoset> geosets, Vec3 right, Vec3 up) {
		Vec3 maxBound = new Vec3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		Vec3 minBound = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

		for (Geoset geo : geosets) {
			for (Triangle t : geo.getTriangles()) {
				for (Vec3 v : t.getVerts()) {
					minBound.minimize(v);
					maxBound.maximize(v);
				}
			}
		}

		Vec2 maxBoundV2 = new Vec2(maxBound.dot(right), maxBound.dot(up));
		Vec2 minBoundV2 = new Vec2(minBound.dot(right), minBound.dot(up));

		return new Vec2[]{minBoundV2, maxBoundV2};
	}

	private static void drawTriangle(Graphics g, Vec3 right, Vec3 up, Triangle t) {
		int[] xInt = getTriPoints(t, right, 1);
		int[] yInt = getTriPoints(t, up, -1);
		g.drawPolyline(xInt, yInt, 4);
	}

	private static int[] getTriPoints(Triangle t, Vec3 dim, int flip){
		int[] output = new int[4];
		for (int i = 0; i < 3; i++) {
			output[i] = Math.round(t.get(i).dot(dim)) * flip;
		}
		output[3] = output[0];
		return output;
	}

	public static void drawBoneMarker(Graphics g, Vec3 right, Vec3 up, Vec3 location) {
		g.setColor(Color.YELLOW);
		if (location != null) {
			drawCrossHair(g, Color.YELLOW, right, up, location);
		}
	}

	public static void drawCrossHair(Graphics g, Color color, Vec3 right, Vec3 up, Vec3 location) {
		g.setColor(color);
		int x = (int) location.dot(right);
		int y = (int) -location.dot(up);
		g.drawOval(x - 5, y - 5, 10, 10);
		g.drawLine(x, y - 10, x, y + 10);
		g.drawLine(x - 10, y, x + 10, y);
	}
}
