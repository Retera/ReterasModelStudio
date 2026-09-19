package com.hiveworkshop.wc3.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * Generators for the Modeling panel's primitives.
 * <p>
 * Every shape is built in a local frame: {@code a} and {@code b} run along the
 * two axes of the viewport the user dragged in, {@code c} runs along the
 * remaining axis (the "height" axis). The drag rectangle gives the extents in
 * a and b; the height phase (or, for shapes without one, the average radius)
 * gives c. Faces are wound so that their facing vector points away from the
 * shape's centre, and vertex normals are averaged from the faces.
 */
public final class PrimitiveMeshes {
	public enum PrimitiveShape {
		SPHERE("Sphere", false, false), GEOSPHERE("Geosphere", false, false), CYLINDER("Cylinder", true, false),
		CONE("Cone", true, false), TORUS("Torus", false, false), CAPSULE("Capsule", true, true),
		TUBE("Tube", true, true);

		private final String displayName;
		private final boolean heightPhase;
		private final boolean extended;

		PrimitiveShape(final String displayName, final boolean heightPhase, final boolean extended) {
			this.displayName = displayName;
			this.heightPhase = heightPhase;
			this.extended = extended;
		}

		public String getDisplayName() {
			return displayName;
		}

		/** True when a second drag phase sets the extent along the third axis. */
		public boolean hasHeightPhase() {
			return heightPhase;
		}

		/** True for the "Extended Primitives" card. */
		public boolean isExtended() {
			return extended;
		}
	}

	/**
	 * Mutable options shared by the panel's spinners and the drawing action, so
	 * changing a spinner during a drag rebuilds the preview.
	 */
	public static final class PrimitiveOptions {
		/** Divisions around the axis. */
		public int segments = 16;
		/** Divisions along the axis (or latitude rings, or torus tube divisions). */
		public int rings = 8;
		/** Hole radius over outer radius for torus and tube, 0 to 0.95. */
		public double innerRatio = 0.5;

		public int clampedSegments() {
			return Math.max(3, Math.min(256, segments));
		}

		public int clampedRings() {
			return Math.max(1, Math.min(128, rings));
		}

		public double clampedInnerRatio() {
			return Math.max(0.0, Math.min(0.95, innerRatio));
		}
	}

	/** The generated geometry, not yet attached to a geoset. */
	public static final class PrimitiveMesh {
		public final List<GeosetVertex> vertices = new ArrayList<>();
		public final List<Triangle> triangles = new ArrayList<>();
	}

	private PrimitiveMeshes() {
	}

	/**
	 * @param dim1     viewport horizontal axis (0, 1 or 2)
	 * @param dim2     viewport vertical axis
	 * @param x1,y1    drag start in (dim1, dim2)
	 * @param x2,y2    drag end in (dim1, dim2)
	 * @param height   signed extent along the third axis (ignored by shapes
	 *                 without a height phase)
	 * @param uvLayers how many texture coordinate layers each vertex needs
	 */
	public static PrimitiveMesh build(final PrimitiveShape shape, final PrimitiveOptions options, final byte dim1,
			final byte dim2, final double x1, final double y1, final double x2, final double y2,
			final double height, final int uvLayers) {
		final byte axis = (byte) (3 - dim1 - dim2);
		final Builder b = new Builder(dim1, dim2, axis, Math.max(1, uvLayers));
		b.ca = (x1 + x2) / 2;
		b.cb = (y1 + y2) / 2;
		b.ra = Math.max(Math.abs(x2 - x1) / 2, 1e-6);
		b.rb = Math.max(Math.abs(y2 - y1) / 2, 1e-6);
		final int segments = options.clampedSegments();
		final int rings = options.clampedRings();
		final double inner = options.clampedInnerRatio();
		switch (shape) {
		case SPHERE:
			b.sphere(segments, Math.max(2, rings), (b.ra + b.rb) / 2, 0, 0, 1);
			break;
		case GEOSPHERE:
			b.geosphere(Math.max(0, Math.min(4, rings / 4)), (b.ra + b.rb) / 2);
			break;
		case CYLINDER:
			b.tube(segments, rings, height, 1.0, 0.0);
			break;
		case CONE:
			b.tube(segments, rings, height, 0.0, 0.0);
			break;
		case TUBE:
			b.tube(segments, rings, height, 1.0, inner);
			break;
		case CAPSULE:
			b.capsule(segments, Math.max(2, rings), height);
			break;
		case TORUS:
			b.torus(segments, Math.max(3, rings), inner);
			break;
		default:
			throw new IllegalArgumentException(shape.toString());
		}
		b.finish();
		return b.mesh;
	}

	private static final class Builder {
		private final byte dim1, dim2, axis;
		private final int uvLayers;
		private final PrimitiveMesh mesh = new PrimitiveMesh();
		double ca, cb, ra, rb;
		/** centre along the axis, for the outward test */
		double cc;

		Builder(final byte dim1, final byte dim2, final byte axis, final int uvLayers) {
			this.dim1 = dim1;
			this.dim2 = dim2;
			this.axis = axis;
			this.uvLayers = uvLayers;
		}

		/** Adds a vertex at local (a, b, c) with texture coordinate (u, v). */
		int vertex(final double a, final double b, final double c, final double u, final double v) {
			final Vertex position = new Vertex(0, 0, 0);
			position.setCoord(dim1, a);
			position.setCoord(dim2, b);
			position.setCoord(axis, c);
			final GeosetVertex vertex = new GeosetVertex(position.x, position.y, position.z, new Normal(0, 0, 1));
			vertex.addTVertex(new TVertex(u, v));
			for (int layer = 1; layer < uvLayers; layer++) {
				vertex.addTVertex(new TVertex(0, 0));
			}
			mesh.vertices.add(vertex);
			return mesh.vertices.size() - 1;
		}

		void tri(final int i0, final int i1, final int i2) {
			if ((i0 == i1) || (i1 == i2) || (i0 == i2)) {
				return;
			}
			mesh.triangles.add(new Triangle(mesh.vertices.get(i0), mesh.vertices.get(i1), mesh.vertices.get(i2)));
		}

		void quad(final int i0, final int i1, final int i2, final int i3) {
			tri(i0, i1, i2);
			tri(i0, i2, i3);
		}

		// ---- shapes ----

		/**
		 * Latitude/longitude sphere (or the part of one between two latitude
		 * fractions) with radius r along the axis, centred at c along the axis.
		 * Latitude runs from {@code latFrom} (0 = top pole) to {@code latTo} (1 =
		 * bottom pole).
		 */
		void sphere(final int segments, final int rings, final double r, final double c, final double latFrom,
				final double latTo) {
			cc = c;
			final int[][] grid = new int[rings + 1][segments + 1];
			for (int i = 0; i <= rings; i++) {
				final double t = i / (double) rings;
				final double theta = Math.PI * (latTo + ((latFrom - latTo) * t));
				// theta measured from the +axis pole
				for (int j = 0; j <= segments; j++) {
					final double phi = (2 * Math.PI * j) / segments;
					grid[i][j] = vertex(ca + (ra * Math.sin(theta) * Math.cos(phi)),
							cb + (rb * Math.sin(theta) * Math.sin(phi)), c + (r * Math.cos(theta)),
							j / (double) segments, t);
				}
			}
			for (int i = 0; i < rings; i++) {
				for (int j = 0; j < segments; j++) {
					quad(grid[i][j], grid[i][j + 1], grid[i + 1][j + 1], grid[i + 1][j]);
				}
			}
		}

		/**
		 * A cylinder-like body from c = 0 to c = height: the top ring is scaled by
		 * {@code topScale} (0 gives a cone) and a hole of {@code innerRatio} times the
		 * outer radius is cut through when it is greater than zero (a tube).
		 */
		void tube(final int segments, final int heightSegments, final double height, final double topScale,
				final double innerRatio) {
			final double h = height == 0 ? 1e-3 : height;
			cc = h / 2;
			final boolean cone = topScale <= 1e-9;
			final boolean hollow = innerRatio > 1e-9;
			// outer wall
			final int[][] outer = wall(segments, heightSegments, h, 1.0, topScale, cone);
			final int wallRows = cone ? heightSegments - 1 : heightSegments;
			for (int i = 0; i < wallRows; i++) {
				for (int j = 0; j < segments; j++) {
					quad(outer[i][j], outer[i][j + 1], outer[i + 1][j + 1], outer[i + 1][j]);
				}
			}
			if (cone) {
				final int apex = vertex(ca, cb, h, 0.5, 0);
				for (int j = 0; j < segments; j++) {
					tri(outer[heightSegments - 1][j], outer[heightSegments - 1][j + 1], apex);
				}
			}
			if (hollow) {
				final int[][] innerWall = wall(segments, heightSegments, h, innerRatio, topScale, cone);
				for (int i = 0; i < wallRows; i++) {
					for (int j = 0; j < segments; j++) {
						quad(innerWall[i][j + 1], innerWall[i][j], innerWall[i + 1][j], innerWall[i + 1][j + 1]);
					}
				}
				if (cone) {
					final int innerApex = vertex(ca, cb, h, 0.5, 0);
					for (int j = 0; j < segments; j++) {
						tri(innerWall[heightSegments - 1][j + 1], innerWall[heightSegments - 1][j], innerApex);
					}
				}
				ringCap(segments, 0, 1.0, innerRatio, false);
				if (!cone) {
					ringCap(segments, h, topScale, innerRatio * topScale, true);
				}
			} else {
				disc(segments, 0, 1.0, false);
				if (!cone) {
					disc(segments, h, topScale, true);
				}
			}
		}

		private int[][] wall(final int segments, final int heightSegments, final double h, final double scale,
				final double topScale, final boolean cone) {
			final int rows = cone ? heightSegments : heightSegments + 1;
			final int[][] grid = new int[rows][segments + 1];
			for (int i = 0; i < rows; i++) {
				final double t = i / (double) heightSegments;
				final double s = scale * (1 + ((topScale - 1) * t));
				for (int j = 0; j <= segments; j++) {
					final double phi = (2 * Math.PI * j) / segments;
					grid[i][j] = vertex(ca + (ra * s * Math.cos(phi)), cb + (rb * s * Math.sin(phi)), h * t,
							j / (double) segments, 1 - t);
				}
			}
			return grid;
		}

		/** A filled circle at height c with its own vertices (hard edge). */
		private void disc(final int segments, final double c, final double scale, final boolean top) {
			final int centre = vertex(ca, cb, c, 0.5, 0.5);
			final int[] ring = new int[segments + 1];
			for (int j = 0; j <= segments; j++) {
				final double phi = (2 * Math.PI * j) / segments;
				ring[j] = vertex(ca + (ra * scale * Math.cos(phi)), cb + (rb * scale * Math.sin(phi)), c,
						0.5 + (0.5 * Math.cos(phi)), 0.5 + (0.5 * Math.sin(phi)));
			}
			for (int j = 0; j < segments; j++) {
				if (top) {
					tri(centre, ring[j], ring[j + 1]);
				} else {
					tri(centre, ring[j + 1], ring[j]);
				}
			}
		}

		/** A flat ring (annulus) at height c between two radius scales. */
		private void ringCap(final int segments, final double c, final double outerScale, final double innerScale,
				final boolean top) {
			final int[] outer = new int[segments + 1];
			final int[] inner = new int[segments + 1];
			for (int j = 0; j <= segments; j++) {
				final double phi = (2 * Math.PI * j) / segments;
				outer[j] = vertex(ca + (ra * outerScale * Math.cos(phi)), cb + (rb * outerScale * Math.sin(phi)), c,
						0.5 + (0.5 * Math.cos(phi)), 0.5 + (0.5 * Math.sin(phi)));
				inner[j] = vertex(ca + (ra * innerScale * Math.cos(phi)), cb + (rb * innerScale * Math.sin(phi)), c,
						0.5 + (0.5 * innerScale * Math.cos(phi)), 0.5 + (0.5 * innerScale * Math.sin(phi)));
			}
			for (int j = 0; j < segments; j++) {
				if (top) {
					quad(inner[j], outer[j], outer[j + 1], inner[j + 1]);
				} else {
					quad(inner[j + 1], outer[j + 1], outer[j], inner[j]);
				}
			}
		}

		/** A cylinder body with hemispherical ends, from c = 0 to c = height. */
		void capsule(final int segments, final int rings, final double height) {
			final double h = Math.abs(height) < 1e-3 ? 1e-3 : height;
			final double sign = Math.signum(h);
			final double capRadius = Math.min(Math.min(ra, rb), Math.abs(h) / 2);
			final double bodyBottom = sign * capRadius;
			final double bodyTop = h - (sign * capRadius);
			cc = h / 2;
			// bottom hemisphere: equator down to the bottom pole (rows run top to bottom)
			sphere(segments, Math.max(1, rings / 2), capRadius * sign, bodyBottom, 0.5, 1);
			// top hemisphere: top pole down to the equator
			sphere(segments, Math.max(1, rings / 2), capRadius * sign, bodyTop, 0, 0.5);
			cc = h / 2;
			// body between the two equators
			final int[][] grid = new int[2][segments + 1];
			for (int i = 0; i < 2; i++) {
				final double c = i == 0 ? bodyBottom : bodyTop;
				for (int j = 0; j <= segments; j++) {
					final double phi = (2 * Math.PI * j) / segments;
					grid[i][j] = vertex(ca + (ra * Math.cos(phi)), cb + (rb * Math.sin(phi)), c,
							j / (double) segments, 1 - i);
				}
			}
			for (int j = 0; j < segments; j++) {
				quad(grid[0][j], grid[0][j + 1], grid[1][j + 1], grid[1][j]);
			}
		}

		/** A torus lying in the drag plane; innerRatio is hole radius over outer radius. */
		void torus(final int segments, final int rings, final double innerRatio) {
			cc = 0;
			final double tubeA = (ra * (1 - innerRatio)) / 2;
			final double tubeB = (rb * (1 - innerRatio)) / 2;
			final double centreA = ra - tubeA;
			final double centreB = rb - tubeB;
			final double tubeC = (tubeA + tubeB) / 2;
			final int[][] grid = new int[segments + 1][rings + 1];
			for (int i = 0; i <= segments; i++) {
				final double phi = (2 * Math.PI * i) / segments;
				for (int j = 0; j <= rings; j++) {
					final double theta = (2 * Math.PI * j) / rings;
					final double radialA = centreA + (tubeA * Math.cos(theta));
					final double radialB = centreB + (tubeB * Math.cos(theta));
					grid[i][j] = vertex(ca + (radialA * Math.cos(phi)), cb + (radialB * Math.sin(phi)),
							tubeC * Math.sin(theta), i / (double) segments, j / (double) rings);
				}
			}
			for (int i = 0; i < segments; i++) {
				for (int j = 0; j < rings; j++) {
					quad(grid[i][j], grid[i + 1][j], grid[i + 1][j + 1], grid[i][j + 1]);
				}
			}
		}

		/** Subdivided icosahedron projected to the ellipsoid, centred on the plane. */
		void geosphere(final int subdivisions, final double r) {
			cc = 0;
			final double t = (1 + Math.sqrt(5)) / 2;
			final List<double[]> points = new ArrayList<>();
			final double[][] base = { { -1, t, 0 }, { 1, t, 0 }, { -1, -t, 0 }, { 1, -t, 0 }, { 0, -1, t },
					{ 0, 1, t }, { 0, -1, -t }, { 0, 1, -t }, { t, 0, -1 }, { t, 0, 1 }, { -t, 0, -1 },
					{ -t, 0, 1 } };
			for (final double[] p : base) {
				points.add(unit(p));
			}
			int[][] faces = { { 0, 11, 5 }, { 0, 5, 1 }, { 0, 1, 7 }, { 0, 7, 10 }, { 0, 10, 11 }, { 1, 5, 9 },
					{ 5, 11, 4 }, { 11, 10, 2 }, { 10, 7, 6 }, { 7, 1, 8 }, { 3, 9, 4 }, { 3, 4, 2 }, { 3, 2, 6 },
					{ 3, 6, 8 }, { 3, 8, 9 }, { 4, 9, 5 }, { 2, 4, 11 }, { 6, 2, 10 }, { 8, 6, 7 }, { 9, 8, 1 } };
			for (int level = 0; level < subdivisions; level++) {
				final Map<Long, Integer> midpoints = new HashMap<>();
				final List<int[]> next = new ArrayList<>();
				for (final int[] f : faces) {
					final int a = midpoint(points, midpoints, f[0], f[1]);
					final int b = midpoint(points, midpoints, f[1], f[2]);
					final int c = midpoint(points, midpoints, f[2], f[0]);
					next.add(new int[] { f[0], a, c });
					next.add(new int[] { f[1], b, a });
					next.add(new int[] { f[2], c, b });
					next.add(new int[] { a, b, c });
				}
				faces = next.toArray(new int[0][]);
			}
			final int[] indices = new int[points.size()];
			for (int i = 0; i < points.size(); i++) {
				final double[] p = points.get(i);
				final double u = 0.5 + (Math.atan2(p[1], p[0]) / (2 * Math.PI));
				final double v = 0.5 - (Math.asin(Math.max(-1, Math.min(1, p[2]))) / Math.PI);
				indices[i] = vertex(ca + (ra * p[0]), cb + (rb * p[1]), r * p[2], u, v);
			}
			for (final int[] f : faces) {
				tri(indices[f[0]], indices[f[1]], indices[f[2]]);
			}
		}

		private static double[] unit(final double[] p) {
			final double len = Math.sqrt((p[0] * p[0]) + (p[1] * p[1]) + (p[2] * p[2]));
			return new double[] { p[0] / len, p[1] / len, p[2] / len };
		}

		private static int midpoint(final List<double[]> points, final Map<Long, Integer> cache, final int i,
				final int j) {
			final long key = (((long) Math.min(i, j)) << 32) | Math.max(i, j);
			final Integer existing = cache.get(key);
			if (existing != null) {
				return existing;
			}
			final double[] a = points.get(i);
			final double[] b = points.get(j);
			points.add(unit(new double[] { (a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2 }));
			cache.put(key, points.size() - 1);
			return points.size() - 1;
		}

		// ---- orientation and normals ----

		void finish() {
			// pole rows and tiny drags produce zero-area faces; drop them
			for (int i = mesh.triangles.size() - 1; i >= 0; i--) {
				if (mesh.triangles.get(i).getFacingVector().vectorMagnitude() < 1e-9) {
					mesh.triangles.remove(i);
				}
			}
			// wind faces outward: the signed volume of a closed mesh is positive when
			// every face's facing vector points out of it (works for tori and tubes,
			// where a centre-based test would not)
			double signedVolume = 0;
			for (final Triangle triangle : mesh.triangles) {
				final GeosetVertex[] v = triangle.getAll();
				final Vertex origin = new Vertex(v[0].x, v[0].y, v[0].z);
				signedVolume += origin.dotProduct(new Vertex(v[1].x, v[1].y, v[1].z)
						.crossProduct(new Vertex(v[2].x, v[2].y, v[2].z)));
			}
			if (signedVolume < 0) {
				for (final Triangle triangle : mesh.triangles) {
					triangle.flip(false);
				}
			}
			// smooth normals from the faces each vertex belongs to
			final Map<GeosetVertex, double[]> sums = new HashMap<>();
			for (final Triangle triangle : mesh.triangles) {
				final Vertex facing = triangle.getFacingVector();
				final double len = facing.vectorMagnitude();
				if (len < 1e-12) {
					continue;
				}
				for (final GeosetVertex vertex : triangle.getAll()) {
					double[] sum = sums.get(vertex);
					if (sum == null) {
						sum = new double[3];
						sums.put(vertex, sum);
					}
					sum[0] += facing.x / len;
					sum[1] += facing.y / len;
					sum[2] += facing.z / len;
				}
			}
			for (final GeosetVertex vertex : mesh.vertices) {
				final double[] sum = sums.get(vertex);
				if (sum == null) {
					continue;
				}
				final double len = Math.sqrt((sum[0] * sum[0]) + (sum[1] * sum[1]) + (sum[2] * sum[2]));
				if (len > 1e-12) {
					vertex.setNormal(new Normal(sum[0] / len, sum[1] / len, sum[2] / len));
				}
			}
		}
	}
}
