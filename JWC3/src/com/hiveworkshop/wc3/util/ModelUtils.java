package com.hiveworkshop.wc3.util;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class ModelUtils {
	public static String getPortrait(final String filepath) {
		final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
				+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
		return portrait;
	}

	/**
	 * @param model
	 * @param max
	 * @param min
	 */
	public static void createBox(final MDL model, final Vertex max, final Vertex min) {
		final Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("textures\\white.blp"))));
		final GeosetVertex minMinMin = new GeosetVertex(min.x, min.y, min.z, new Normal(-1, -1, -1).normalize());
		geoset.add(minMinMin);
		final GeosetVertex minMinMax = new GeosetVertex(min.x, min.y, max.z, new Normal(-1, -1, 1).normalize());
		geoset.add(minMinMax);
		final GeosetVertex minMaxMin = new GeosetVertex(min.x, max.y, min.z, new Normal(-1, 1, -1).normalize());
		geoset.add(minMaxMin);
		final GeosetVertex minMaxMax = new GeosetVertex(min.x, max.y, max.z, new Normal(-1, 1, 1).normalize());
		geoset.add(minMaxMax);
		final GeosetVertex maxMinMin = new GeosetVertex(max.x, min.y, min.z, new Normal(1, -1, -1).normalize());
		geoset.add(maxMinMin);
		final GeosetVertex maxMinMax = new GeosetVertex(max.x, min.y, max.z, new Normal(1, -1, 1).normalize());
		geoset.add(maxMinMax);
		final GeosetVertex maxMaxMin = new GeosetVertex(max.x, max.y, min.z, new Normal(1, 1, -1).normalize());
		geoset.add(maxMaxMin);
		final GeosetVertex maxMaxMax = new GeosetVertex(max.x, max.y, max.z, new Normal(1, 1, 1).normalize());
		geoset.add(maxMaxMax);
		for (final GeosetVertex vertex : geoset.getVertices()) {
			vertex.addTVertex(new TVertex(0, 0));
			vertex.setGeoset(geoset);
		}

		final List<GeosetVertex> verticesOnSide = new ArrayList<>();
		for (byte side = (byte) 0; side < 2; side++) {
			for (byte dimension = (byte) 0; dimension < 3; dimension++) {
				verticesOnSide.clear();
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

				GeosetVertex farthestFromFirstPoint = null;
				GeosetVertex firstPoint = null;
				double farthestDistanceFromFirst = Double.MIN_VALUE;
				for (final GeosetVertex vertex : geoset.getVertices()) {
					if (vertex.getCoord(dimension) == coordinateAtSide) {
						verticesOnSide.add(vertex);
						if (firstPoint == null) {
							firstPoint = vertex;
						} else {
							final double distance = vertex.distance(firstPoint);
							if (farthestFromFirstPoint == null || distance > farthestDistanceFromFirst) {
								farthestDistanceFromFirst = distance;
								farthestFromFirstPoint = vertex;
							}
						}
					}
				}
				if (farthestFromFirstPoint == verticesOnSide.get(1)) {
					geoset.add(new Triangle(verticesOnSide.get(0), verticesOnSide.get(2), farthestFromFirstPoint));
					geoset.add(new Triangle(verticesOnSide.get(0), farthestFromFirstPoint, verticesOnSide.get(3)));
				} else if (farthestFromFirstPoint == verticesOnSide.get(2)) {
					geoset.add(new Triangle(verticesOnSide.get(0), verticesOnSide.get(1), farthestFromFirstPoint));
					geoset.add(new Triangle(verticesOnSide.get(0), farthestFromFirstPoint, verticesOnSide.get(3)));
				} else {
					geoset.add(new Triangle(verticesOnSide.get(0), verticesOnSide.get(1), farthestFromFirstPoint));
					geoset.add(new Triangle(verticesOnSide.get(0), farthestFromFirstPoint, verticesOnSide.get(2)));
				}
			}
		}
		for (final Triangle triangle : geoset.getTriangles()) {
			triangle.setGeoset(geoset);
		}
		model.add(geoset);
	}

	private ModelUtils() {
	}
}
