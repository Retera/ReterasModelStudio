package com.hiveworkshop.wc3.util;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class ModelUtils {
	public static final class Mesh {
		private final List<GeosetVertex> vertices;
		private final List<Triangle> triangles;

		private Mesh(final List<GeosetVertex> vertices, final List<Triangle> triangles) {
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

	public static String getPortrait(final String filepath) {
		final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
				+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
		return portrait;
	}

	public static Mesh createPlane(final byte planeDimension, final boolean outward, final double planeHeight,
			final double minFirst, final double minSecond, final double maxFirst, final double maxSecond,
			final int numberOfSegments) {
		return createPlane(planeDimension, outward, planeHeight, minFirst, minSecond, maxFirst, maxSecond,
				numberOfSegments, numberOfSegments);
	}

	public static Mesh createPlane(final byte planeDimension, final boolean outward, final double planeHeight,
			final double minFirst, final double minSecond, final double maxFirst, final double maxSecond,
			final int numberOfSegmentsX, final int numberOfSegmentsY) {
		byte firstDimension, secondDimension;
		switch (planeDimension) {
		case 0:
			firstDimension = (byte) 1;
			secondDimension = (byte) 2;
			break;
		case 1:
			firstDimension = (byte) 0;
			secondDimension = (byte) 2;
			break;
		case 2:
			firstDimension = (byte) 0;
			secondDimension = (byte) 1;
			break;
		default:
			throw new IllegalStateException();
		}
		boolean flipFacesForIterationDesignFlaw = false;
		if (planeDimension == 1) {
			flipFacesForIterationDesignFlaw = true;
		}
		final Vertex normal = new Vertex(0, 0, 0);
		normal.setCoord(planeDimension, outward ? 1 : -1);
		return createPlane(firstDimension, secondDimension, normal, planeHeight, minFirst, minSecond, maxFirst,
				maxSecond, numberOfSegmentsX, numberOfSegmentsY);
	}

	public static Mesh createPlane(final byte firstDimension, final byte secondDimension, final Vertex facingVector,
			final double planeHeight, final double minFirst, final double minSecond, final double maxFirst,
			final double maxSecond, final int numberOfSegments) {
		return createPlane(firstDimension, secondDimension, facingVector, planeHeight, minFirst, minSecond, maxFirst,
				maxSecond, numberOfSegments, numberOfSegments);
	}

	public static Mesh createPlane(final byte firstDimension, final byte secondDimension, final Vertex facingVector,
			final double planeHeight, final double minFirst, final double minSecond, final double maxFirst,
			final double maxSecond, final int numberOfSegmentsX, final int numberOfSegmentsY) {
		final byte planeDimension = CoordinateSystem.Util.getUnusedXYZ(firstDimension, secondDimension);
		final List<GeosetVertex> vertices = new ArrayList<>();
		final List<Triangle> triangles = new ArrayList<>();
		final double firstDimensionSegmentWidth = (maxFirst - minFirst) / numberOfSegmentsX;
		final double secondDimensionSegmentWidth = (maxSecond - minSecond) / numberOfSegmentsY;
		final double segmentWidthUV1 = 1. / numberOfSegmentsX;
		final double segmentWidthUV2 = 1. / numberOfSegmentsY;
		GeosetVertex[] previousRow = null;
		for (int y = 0; y < (numberOfSegmentsY + 1); y++) {
			final GeosetVertex[] currentRow = new GeosetVertex[numberOfSegmentsX + 1];
			for (int x = 0; x < (numberOfSegmentsX + 1); x++) {
				final Normal normal = new Normal(facingVector.x, facingVector.y, facingVector.z);
				final GeosetVertex vertex = new GeosetVertex(0, 0, 0, normal);
				currentRow[x] = vertex;
				vertex.setCoord(planeDimension, planeHeight);
				vertex.setCoord(firstDimension, minFirst + (x * firstDimensionSegmentWidth));
				vertex.setCoord(secondDimension, minSecond + (y * secondDimensionSegmentWidth));
				vertex.addTVertex(new TVertex(x * segmentWidthUV1, y * segmentWidthUV2));
				vertices.add(vertex);
				if (y > 0) {
					if (x > 0) {
						final GeosetVertex lowerLeft = previousRow[x - 1];
						final GeosetVertex lowerRight = previousRow[x];
						final GeosetVertex upperLeft = currentRow[x - 1];
						final Triangle firstFace = new Triangle(vertex, upperLeft, lowerLeft);
						triangles.add(firstFace);
						final Triangle secondFace = new Triangle(vertex, lowerLeft, lowerRight);
						triangles.add(secondFace);
						final boolean flip = firstFace.getFacingVector().dotProduct(facingVector) < 0;
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

	/**
	 * @param model
	 * @param max
	 * @param min
	 */
	public static void createBox(final EditableModel model, final Vertex max, final Vertex min, final int segments) {
		final Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("textures\\white.blp"))));

		for (byte side = (byte) 0; side < 2; side++) {
			for (byte dimension = (byte) 0; dimension < 3; dimension++) {
				Vertex sideMaxima;
				switch (side) {
				case 0:
					sideMaxima = min;
					break;
				case 1:
					sideMaxima = max;
					break;
				default:
					throw new IllegalStateException();
				}
				final double coordinateAtSide = sideMaxima.getCoord(dimension);

				byte firstDimension, secondDimension;
				switch (dimension) {
				case 0:
					firstDimension = (byte) 1;
					secondDimension = (byte) 2;
					break;
				case 1:
					firstDimension = (byte) 0;
					secondDimension = (byte) 2;
					break;
				case 2:
					firstDimension = (byte) 0;
					secondDimension = (byte) 1;
					break;
				default:
					throw new IllegalStateException();
				}
				final double minFirst = min.getCoord(firstDimension);
				final double minSecond = min.getCoord(secondDimension);
				final double maxFirst = max.getCoord(firstDimension);
				final double maxSecond = max.getCoord(secondDimension);

				final Mesh sidedPlane = createPlane(dimension, side == 1, coordinateAtSide, minFirst, minSecond,
						maxFirst, maxSecond, segments);
				for (final GeosetVertex vertex : sidedPlane.vertices) {
					geoset.add(vertex);
				}
				for (final Triangle triangle : sidedPlane.triangles) {
					geoset.add(triangle);
				}
			}
		}
		for (final GeosetVertex vertex : geoset.getVertices()) {
			vertex.addTVertex(new TVertex(0, 0));
			vertex.setGeoset(geoset);
		}
		for (final Triangle triangle : geoset.getTriangles()) {
			triangle.setGeoset(geoset);
			for (final GeosetVertex vertex : triangle.getVerts()) {
				vertex.getTriangles().add(triangle);
			}
		}
		model.add(geoset);
	}

	/**
	 * Creates a box ready to add to the dataGeoset, but does not actually modify
	 * the geoset itself
	 *
	 * @param max
	 * @param min
	 * @param segments
	 * @param dataGeoset
	 * @return
	 */
	public static Mesh createBox(final Vertex max, final Vertex min, final int lengthSegs, final int widthSegs,
			final int heightSegs, final Geoset dataGeoset) {
		final Mesh box = new Mesh(new ArrayList<>(), new ArrayList<>());
		for (byte side = (byte) 0; side < 2; side++) {
			for (byte dimension = (byte) 0; dimension < 3; dimension++) {
				Vertex sideMaxima;
				switch (side) {
				case 0:
					sideMaxima = min;
					break;
				case 1:
					sideMaxima = max;
					break;
				default:
					throw new IllegalStateException();
				}
				final double coordinateAtSide = sideMaxima.getCoord(dimension);

				int segsX, segsY;
				byte firstDimension, secondDimension;
				switch (dimension) {
				case 0:
					firstDimension = (byte) 1;
					secondDimension = (byte) 2;
					segsX = widthSegs;
					segsY = heightSegs;
					break;
				case 1:
					firstDimension = (byte) 0;
					secondDimension = (byte) 2;
					segsX = lengthSegs;
					segsY = heightSegs;
					break;
				case 2:
					firstDimension = (byte) 0;
					secondDimension = (byte) 1;
					segsX = lengthSegs;
					segsY = widthSegs;
					break;
				default:
					throw new IllegalStateException();
				}
				final double minFirst = min.getCoord(firstDimension);
				final double minSecond = min.getCoord(secondDimension);
				final double maxFirst = max.getCoord(firstDimension);
				final double maxSecond = max.getCoord(secondDimension);

				final Mesh sidedPlane = createPlane(dimension, side != 1, coordinateAtSide, minFirst, minSecond,
						maxFirst, maxSecond, segsX, segsY);
				for (final GeosetVertex vertex : sidedPlane.vertices) {
					box.vertices.add(vertex);
				}
				for (final Triangle triangle : sidedPlane.triangles) {
					box.triangles.add(triangle);
				}
			}
		}
		for (final GeosetVertex vertex : box.getVertices()) {
			// createPlane already gave each vertex its texture coordinate layer
			vertex.setGeoset(dataGeoset);
		}
		for (final Triangle triangle : box.getTriangles()) {
			triangle.setGeoset(dataGeoset);
			for (final GeosetVertex vertex : triangle.getVerts()) {
				vertex.getTriangles().add(triangle);
			}
		}
		return box;
	}

	/**
	 * @param model
	 * @param max
	 * @param min
	 */
	public static void createGroundPlane(final EditableModel model, final Vertex max, final Vertex min,
			final int segments) {
		final Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("textures\\white.blp"))));

		final Mesh sidedPlane = createPlane((byte) 2, true, 0, min.x, min.y, max.x, max.y, segments);
		for (final GeosetVertex vertex : sidedPlane.vertices) {
			geoset.add(vertex);
		}
		for (final Triangle triangle : sidedPlane.triangles) {
			geoset.add(triangle);
		}
		for (final GeosetVertex vertex : geoset.getVertices()) {
			vertex.addTVertex(new TVertex(0, 0));
			vertex.setGeoset(geoset);
		}
		for (final Triangle triangle : geoset.getTriangles()) {
			triangle.setGeoset(geoset);
			for (final GeosetVertex vertex : triangle.getVerts()) {
				vertex.getTriangles().add(triangle);
			}
		}
		model.add(geoset);
	}

	/**
	 * Format version introduced by Warcraft III: Reforged - Forsaken Kingdom (Patch 3.0.0, build 24268). Every
	 * stock model (Classic, Reforged and Definitive Edition layers) shipped with 3.0 uses this version.
	 */
	public static final int FORMAT_VERSION_1800 = 1800;
	/**
	 * Lowest format version that uses the Forsaken Kingdom layouts. Two stock day/night-cycle environment models
	 * (cinematics/.../environment/*_dnc.mdx and dark.mdx) are versions 1600 and 1700 and already carry the new
	 * 72-byte light block, so everything from 1600 upwards is treated like 1800.
	 */
	public static final int FORMAT_VERSION_FORSAKEN_KINGDOM_MIN = 1600;

	public static boolean isForsakenKingdomFormat(final int formatVersion) {
		return formatVersion >= FORMAT_VERSION_FORSAKEN_KINGDOM_MIN;
	}

	public static boolean isLevelOfDetailSupported(final int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000) || (formatVersion == 1100) || (formatVersion == 1200)
				|| isForsakenKingdomFormat(formatVersion);
	}

	public static boolean isShaderStringSupported(final int formatVersion) {
		return formatVersion >= 900 && formatVersion <= 1000;
	}

	public static boolean isTangentAndSkinSupported(final int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000) || (formatVersion == 1100) || (formatVersion == 1200)
				|| isForsakenKingdomFormat(formatVersion);
	}

	/**
	 * Since MDX 1800 (Forsaken Kingdom), the geoset SKIN block stores 16-bit bone indices and 16-bit weights
	 * (4 + 4 per vertex, weights still sum to 255). The SKIN count field is unchanged (vertexCount * 8 elements),
	 * but each element is 2 bytes instead of 1.
	 */
	public static boolean isSkin16BitSupported(final int formatVersion) {
		return isForsakenKingdomFormat(formatVersion);
	}

	/**
	 * Since MDX 1800 the LITE chunk carries a "ShadowCasting" flag after the light type, and
	 * ShadowCastingStart/ShadowCastingEnd/QuadraticFalloff/LinearFalloff/Damping after ShadowIntensity.
	 */
	public static boolean isExtendedLightSupported(final int formatVersion) {
		return isForsakenKingdomFormat(formatVersion);
	}

	/**
	 * Since MDX 1800 the CAMS chunk stores a header byte in the top 8 bits of each camera's inclusive size
	 * (observed always 3 in stock data) and supports the depth-of-field focus distance track "IDUF".
	 */
	public static boolean isCameraDepthOfFieldSupported(final int formatVersion) {
		return isForsakenKingdomFormat(formatVersion);
	}

	/**
	 * Since MDX 1800 an optional "DILG" chunk (the "Glider" section in the MDL text keyword table) may follow BPOS.
	 */
	public static boolean isGliderSupported(final int formatVersion) {
		return isForsakenKingdomFormat(formatVersion);
	}

	public static boolean isBindPoseSupported(final int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000) || (formatVersion == 1100) || (formatVersion == 1200)
				|| isForsakenKingdomFormat(formatVersion);
	}

	public static boolean isEmissiveLayerSupported(final int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000) || (formatVersion == 1100) || (formatVersion == 1200)
				|| isForsakenKingdomFormat(formatVersion);
	}

	public static boolean isFresnelColorLayerSupported(final int formatVersion) {
		return (formatVersion == 1000) || (formatVersion == 1100) || (formatVersion == 1200)
				|| isForsakenKingdomFormat(formatVersion);
	}

	public static boolean isCornSupported(final int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000) || (formatVersion == 1100) || (formatVersion == 1200)
				|| isForsakenKingdomFormat(formatVersion);
	}

	public static boolean isCombinedHDLayerSupported(final int formatVersion) {
		return (formatVersion == 1100) || (formatVersion == 1200) || isForsakenKingdomFormat(formatVersion);
	}

	public static boolean isLightShadowIntensitySupported(final int formatVersion) {
		return formatVersion >= 1200;
	}

	private ModelUtils() {
	}
}
