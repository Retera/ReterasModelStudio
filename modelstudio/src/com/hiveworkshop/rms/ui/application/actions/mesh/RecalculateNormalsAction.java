package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.HashableVector;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class RecalculateNormalsAction implements UndoAction {
	List<Vec3> oldNormals;
	List<Vec3> newNormals;
	List<GeosetVertex> affectedVertices;
	double maxAngle;
	boolean useTries;

	public RecalculateNormalsAction(Collection<GeosetVertex> affectedVertices, double maxAngle, boolean useTries) {
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

	public static Vec3 createNormal(Collection<Triangle> triangles) {
		Vec3 sum = new Vec3();
		for (Triangle triangle : triangles) {
			sum.add(triangle.getNormal());
		}
		return sum.normalize();
	}

	@Override
	public String actionName() {
		return "recalculate normals";
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
		Map<HashableVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
		for (GeosetVertex geosetVertex : affectedVertices) {
			HashableVector location = new HashableVector(geosetVertex);
			List<GeosetVertex> gvAtLocation = locationToGVs.computeIfAbsent(location, gvList -> new ArrayList<>());
			gvAtLocation.add(geosetVertex);
		}
		for (GeosetVertex geosetVertex : affectedVertices) {
			HashableVector location = new HashableVector(geosetVertex);
			List<GeosetVertex> gvAtLocation = locationToGVs.get(location);
			if (useTries) {
				newNormals.add(createNormalFromFaces(geosetVertex, gvAtLocation, maxAngle));
			} else {
				newNormals.add(createNormal(geosetVertex, gvAtLocation, maxAngle));
			}
		}
	}
}
