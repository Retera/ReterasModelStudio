package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class RecalculateNormalsAction2 implements UndoAction {
	List<Vec3> oldNormals;
	List<Vec3> newNormals;
	List<GeosetVertex> affectedVertices;
	double maxAngle;
	boolean useTries;

	public RecalculateNormalsAction2(List<GeosetVertex> affectedVertices, double maxAngle, boolean useTries) {
		this.affectedVertices = new ArrayList<>(affectedVertices);
		this.oldNormals = new ArrayList<>();
		this.newNormals = new ArrayList<>();
		for (GeosetVertex vertex : affectedVertices) {
			this.oldNormals.add(new Vec3(vertex.getNormal()));
		}
		this.maxAngle = maxAngle;
		this.useTries = useTries;
		makeNormals();

	}

	public static Vec3 createNormal(GeosetVertex geosetVertex, List<GeosetVertex> matches, double maxAngle) {
		Vec3 sum = new Vec3();
		Vec3 normal = createNormal(geosetVertex.getTriangles());
		List<Vec3> uniqueNormals = new ArrayList<>();
		for (GeosetVertex match : matches) {
			Vec3 matchNormal = createNormal(match.getTriangles());
			uniqueNormals.add(matchNormal);
		}
		uniqueNormals.stream().filter(n -> normal.degAngleTo(n) < maxAngle).forEach(sum::add);

		return sum.normalize();
	}

	public static Vec3 createNormalFromFaces(GeosetVertex geosetVertex, List<GeosetVertex> matches, double maxAngle) {
		Vec3 sum = new Vec3();
		Vec3 normal = createNormal(geosetVertex.getTriangles());
		for (GeosetVertex match : matches) {
			for (Triangle triangle : match.getTriangles()) {
				Vec3 matchNormal = triangle.getNormal().normalize();
				double angle = normal.degAngleTo(matchNormal);
				if (angle < maxAngle) {
					sum.add(matchNormal);
				}
			}
		}

		return sum.normalize();
	}

	@Override
	public String actionName() {
		return "recalculate normals";
	}

	public static Vec3 createNormal(List<Triangle> triangles) {
		Vec3 sum = new Vec3();
		for (Triangle triangle : triangles) {
			sum.add(triangle.getNormal());
		}
		return sum.normalize();
	}

	@Override
	public void undo() {
		for (int i = 0; i < affectedVertices.size(); i++) {
			affectedVertices.get(i).setNormalValue(oldNormals.get(i));
		}
	}

	@Override
	public void redo() {
		for (int i = 0; i < affectedVertices.size(); i++) {
			affectedVertices.get(i).setNormalValue(newNormals.get(i));
		}
	}

	private void makeNormals() {
		Map<Location, List<GeosetVertex>> locationToGVs = new HashMap<>();
		for (GeosetVertex geosetVertex : affectedVertices) {
			Location location = new Location(geosetVertex);
			List<GeosetVertex> gvAtLocation = locationToGVs.computeIfAbsent(location, gvList -> new ArrayList<>());
			gvAtLocation.add(geosetVertex);
		}
		for (GeosetVertex geosetVertex : affectedVertices) {
			Location location = new Location(geosetVertex);
			List<GeosetVertex> gvAtLocation = locationToGVs.get(location);
			if (useTries) {
				newNormals.add(createNormalFromFaces(geosetVertex, gvAtLocation, maxAngle));
			} else {
				newNormals.add(createNormal(geosetVertex, gvAtLocation, maxAngle));
			}
		}
	}

	private static final class Location {
		private final double x, y, z;

		public Location(Vec3 vec3) {
			super();
			this.x = vec3.x;
			this.y = vec3.y;
			this.z = vec3.z;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(x);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(z);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Location other = (Location) obj;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
				return false;
			}
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
				return false;
			}
			return Double.doubleToLongBits(z) == Double.doubleToLongBits(other.z);
		}

	}
}
