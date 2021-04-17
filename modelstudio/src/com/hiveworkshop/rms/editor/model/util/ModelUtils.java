package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ModelUtils {
	public static String getPortrait(String filepath) {
		String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait" + filepath.substring(filepath.lastIndexOf('.'));
		return portrait;
	}

	public static Mesh createPlane(byte planeDimension, boolean outward, double planeHeight,
	                               double minFirst, double minSecond,
	                               double maxFirst, double maxSecond,
	                               int numberOfSegments) {
		return createPlane(planeDimension, outward, planeHeight, minFirst, minSecond, maxFirst, maxSecond, numberOfSegments, numberOfSegments);
	}

	public static Mesh createPlane(byte planeDimension, boolean outward, double planeHeight,
	                               double minFirst, double minSecond,
	                               double maxFirst, double maxSecond,
	                               int numberOfSegmentsX, int numberOfSegmentsY) {
		byte[] dimensions = getBytesDimensions(planeDimension);

		byte firstDimension = dimensions[0];
		byte secondDimension = dimensions[1];

		boolean flipFacesForIterationDesignFlaw = false;
		if (planeDimension == 1) {
			flipFacesForIterationDesignFlaw = true;
		}
		Vec3 normal = new Vec3(0, 0, 0);
		normal.setCoord(planeDimension, outward ? 1 : -1);
		return createPlane(firstDimension, secondDimension, normal, planeHeight, minFirst, minSecond, maxFirst, maxSecond, numberOfSegmentsX, numberOfSegmentsY);
	}

	public static Mesh createPlane(byte firstDimension, byte secondDimension,
	                               Vec3 facingVector, double planeHeight,
	                               double minFirst, double minSecond,
	                               double maxFirst, double maxSecond,
	                               int numberOfSegments) {
		return createPlane(firstDimension, secondDimension, facingVector, planeHeight, minFirst, minSecond, maxFirst, maxSecond, numberOfSegments, numberOfSegments);
	}

	public static Mesh createPlane(byte firstDimension, byte secondDimension,
	                               Vec3 facingVector, double planeHeight,
	                               double minFirst, double minSecond,
	                               double maxFirst, double maxSecond,
	                               int numberOfSegmentsX, int numberOfSegmentsY) {
		byte planeDimension = CoordinateSystem.Util.getUnusedXYZ(firstDimension, secondDimension);

		List<GeosetVertex> vertices = new ArrayList<>();
		List<Triangle> triangles = new ArrayList<>();

		double firstDimensionSegmentWidth = (maxFirst - minFirst) / numberOfSegmentsX;
		double secondDimensionSegmentWidth = (maxSecond - minSecond) / numberOfSegmentsY;
		double segmentWidthUV1 = 1. / numberOfSegmentsX;
		double segmentWidthUV2 = 1. / numberOfSegmentsY;
		GeosetVertex[] previousRow = null;

		for (int y = 0; y < (numberOfSegmentsY + 1); y++) {
			GeosetVertex[] currentRow = new GeosetVertex[numberOfSegmentsX + 1];
			for (int x = 0; x < (numberOfSegmentsX + 1); x++) {
				Vec3 normal = new Vec3(facingVector.x, facingVector.y, facingVector.z);
				GeosetVertex vertex = new GeosetVertex(0, 0, 0, normal);
				currentRow[x] = vertex;
				vertex.setCoord(planeDimension, planeHeight);
				vertex.setCoord(firstDimension, minFirst + (x * firstDimensionSegmentWidth));
				vertex.setCoord(secondDimension, minSecond + (y * secondDimensionSegmentWidth));
				vertex.addTVertex(new Vec2(x * segmentWidthUV1, y * segmentWidthUV2));
				vertices.add(vertex);
				if (y > 0) {
					if (x > 0) {
						GeosetVertex lowerLeft = previousRow[x - 1];
						GeosetVertex lowerRight = previousRow[x];
						GeosetVertex upperLeft = currentRow[x - 1];

						Triangle firstFace = new Triangle(vertex, upperLeft, lowerLeft);
						triangles.add(firstFace);
						Triangle secondFace = new Triangle(vertex, lowerLeft, lowerRight);
						triangles.add(secondFace);

						boolean flip = firstFace.getNormal().dot(facingVector) < 0;
						if (flip) {
							firstFace.flip(false);
							secondFace.flip(false);
						}
					}
				}
			}
			previousRow = currentRow;
		}
		return new Mesh(vertices, triangles);
	}

	public static void createBox(EditableModel model, Vec3 max, Vec3 min, int segments) {
		Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("textures\\white.blp"))));

		for (byte side = (byte) 0; side < 2; side++) {
			for (byte dimension = (byte) 0; dimension < 3; dimension++) {
				Vec3 sideMaxima = switch (side) {
					case 0 -> min;
					case 1 -> max;
					default -> throw new IllegalStateException();
				};
				double coordinateAtSide = sideMaxima.getCoord(dimension);

				byte[] dimensions = getBytesDimensions(dimension);

				byte firstDimension = dimensions[0];
				byte secondDimension = dimensions[1];

				double minFirst = min.getCoord(firstDimension);
				double minSecond = min.getCoord(secondDimension);
				double maxFirst = max.getCoord(firstDimension);
				double maxSecond = max.getCoord(secondDimension);

				Mesh sidedPlane = createPlane(dimension, side == 1, coordinateAtSide, minFirst, minSecond, maxFirst, maxSecond, segments);
				for (GeosetVertex vertex : sidedPlane.vertices) {
					geoset.add(vertex);
				}
				for (Triangle triangle : sidedPlane.triangles) {
					geoset.add(triangle);
				}
			}
		}
		for (final GeosetVertex vertex : geoset.getVertices()) {
			vertex.addTVertex(new Vec2(0, 0));
			vertex.setGeoset(geoset);
		}
		for (final Triangle triangle : geoset.getTriangles()) {
			triangle.setGeoset(geoset);
			for (GeosetVertex vertex : triangle.getVerts()) {
				vertex.getTriangles().add(triangle);
			}
		}
		model.add(geoset);
	}

	/**
	 * Creates a box ready to add to the dataGeoset, but does not actually modify
	 * the geoset itself
	 */
	public static Mesh createBox(Vec3 max, Vec3 min, int lengthSegs, int widthSegs, int heightSegs, Geoset dataGeoset) {
		Mesh box = new Mesh(new ArrayList<>(), new ArrayList<>());
		for (byte side = (byte) 0; side < 2; side++) {
			for (byte dimension = (byte) 0; dimension < 3; dimension++) {
				Vec3 sideMaxima = switch (side) {
					case 0 -> min;
					case 1 -> max;
					default -> throw new IllegalStateException();
				};
				double coordinateAtSide = sideMaxima.getCoord(dimension);


				byte[] dimensions = getBytesDimensions(dimension);

				byte firstDimension = dimensions[0];
				byte secondDimension = dimensions[1];

				int segsX = firstDimension == 0 ? lengthSegs : widthSegs;
				int segsY = secondDimension == 2 ? heightSegs : widthSegs;

				double minFirst = min.getCoord(firstDimension);
				double minSecond = min.getCoord(secondDimension);
				double maxFirst = max.getCoord(firstDimension);
				double maxSecond = max.getCoord(secondDimension);

				Mesh sidedPlane = createPlane(dimension, side != 1, coordinateAtSide, minFirst, minSecond, maxFirst, maxSecond, segsX, segsY);
				box.vertices.addAll(sidedPlane.vertices);
				box.triangles.addAll(sidedPlane.triangles);
			}
		}
		for (GeosetVertex vertex : box.getVertices()) {
			vertex.addTVertex(new Vec2(0, 0));
			vertex.setGeoset(dataGeoset);
		}
		for (Triangle triangle : box.getTriangles()) {
			triangle.setGeoset(dataGeoset);
			for (GeosetVertex vertex : triangle.getVerts()) {
				vertex.getTriangles().add(triangle);
			}
		}
		return box;
	}

	private static byte[] getBytesDimensions(byte dimension) {
		byte[] dimensions = new byte[2];
		switch (dimension) {
			case 0 -> {
				dimensions[0] = (byte) 1;
				dimensions[1] = (byte) 2;
			}
			case 1 -> {
				dimensions[0] = (byte) 0;
				dimensions[1] = (byte) 2;
			}
			case 2 -> {
				dimensions[0] = (byte) 0;
				dimensions[1] = (byte) 1;
			}
			default -> throw new IllegalStateException();
		}
		return dimensions;
	}

	public static void createGroundPlane(EditableModel model, Vec3 max, Vec3 min, int segments) {
		Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("textures\\white.blp"))));

		Mesh sidedPlane = createPlane((byte) 2, true, 0, min.x, min.y, max.x, max.y, segments);
		for (GeosetVertex vertex : sidedPlane.vertices) {
			geoset.add(vertex);
		}
		for (Triangle triangle : sidedPlane.triangles) {
			geoset.add(triangle);
		}
		for (GeosetVertex vertex : geoset.getVertices()) {
			vertex.addTVertex(new Vec2(0, 0));
			vertex.setGeoset(geoset);
		}
		for (Triangle triangle : geoset.getTriangles()) {
			triangle.setGeoset(geoset);
			for (GeosetVertex vertex : triangle.getVerts()) {
				vertex.getTriangles().add(triangle);
			}
		}
		model.add(geoset);
	}

	public static float[] flipRGBtoBGR(float[] rgb) {
		float[] bgr = new float[3];
		for (int i = 0; i < 3; i++) {
			bgr[i] = rgb[2 - i];
		}
		return bgr;
	}

	public static boolean isLevelOfDetailSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isShaderStringSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isTangentAndSkinSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isBindPoseSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isEmissiveLayerSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isFresnelColorLayerSupported(int formatVersion) {
		return formatVersion == 1000;
	}

	public static boolean isCornSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static final class Mesh {
		private final List<GeosetVertex> vertices;
		private final List<Triangle> triangles;

		private Mesh(List<GeosetVertex> vertices, List<Triangle> triangles) {
			this.vertices = vertices;
			this.triangles = triangles;
		}

		public List<GeosetVertex> getVertices() {
			return vertices;
		}

		public List<Triangle> getTriangles() {
			return triangles;
		}

	}

	private ModelUtils() {
	}
}
