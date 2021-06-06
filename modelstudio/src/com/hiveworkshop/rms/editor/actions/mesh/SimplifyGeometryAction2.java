package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.InexactHashVector;
import com.hiveworkshop.rms.util.Vec2;

import java.util.*;

public class SimplifyGeometryAction2 implements UndoAction {
	Set<GeosetVertex> affectedVertices = new HashSet<>();
	Map<Geoset, Map<GeosetVertex, GeosetVertex>> geosetToOldToNew = new HashMap<>();
	Map<Geoset, Map<GeosetVertex, List<Triangle>>> geosetToOrgVertToAffTris = new HashMap<>();
	Map<Geoset, Set<Triangle>> geosetToAffectedTris = new HashMap<>();
	Map<Geoset, Map<InexactHashVector, List<GeosetVertex>>> geosetToLocationToGVs = new HashMap<>();
	Set<Geoset> geosets = new HashSet<>();


	Map<Geoset, Set<Triangle>> removedTrisMap1 = new HashMap<>();

	public SimplifyGeometryAction2(Collection<GeosetVertex> selection) {
		affectedVertices.addAll(selection);
		System.out.println("looking at " + affectedVertices.size() + "vertices");

		for (GeosetVertex vertex : affectedVertices) {
			InexactHashVector location = new InexactHashVector(vertex, 100);
			geosets.add(vertex.getGeoset());
			geosetToLocationToGVs
					.computeIfAbsent(vertex.getGeoset(), m -> new HashMap<>())
					.computeIfAbsent(location, l -> new ArrayList<>())
					.add(vertex);
			geosetToOrgVertToAffTris
					.computeIfAbsent(vertex.getGeoset(), m -> new HashMap<>())
					.computeIfAbsent(vertex, l -> new ArrayList<>())
					.addAll(vertex.getTriangles());
			geosetToAffectedTris
					.computeIfAbsent(vertex.getGeoset(), m -> new HashSet<>())
					.addAll(vertex.getTriangles());
		}

	}

	private Set<Triangle> removeTriangles(List<Triangle> triangles, Geoset geoset) {
		Set<Triangle> trianglesToRemove = new HashSet<>();
		for (int i = 0; i < triangles.size(); i++) {
			Triangle triangleToKeep = triangles.get(i);
			if (!trianglesToRemove.contains(triangleToKeep)) {
				for (int j = i + 1; j < triangles.size(); j++) {
					Triangle triangle = triangles.get(j);

					if (triangleToKeep.sameVerts(triangle)) {
						trianglesToRemove.add(triangle);
						geoset.removeExtended(triangle);
					}
				}
			}
		}
		return trianglesToRemove;
	}

	public void removeVerts(List<GeosetVertex> vertices, Geoset geoset) {
		Map<GeosetVertex, GeosetVertex> oldToNew = geosetToOldToNew.computeIfAbsent(geoset, m -> new HashMap<>());
		for (int i = 0; i < vertices.size(); i++) {
			GeosetVertex vertexToKeep = vertices.get(i);

			if (!oldToNew.containsKey(vertexToKeep)) {

				for (int j = i + 1; j < vertices.size(); j++) {
					GeosetVertex vertex = vertices.get(j);
					boolean hasSameUvCoord = isSameUvCoord(vertexToKeep, vertex);
					boolean hasSameNormal = isSameNormal(vertexToKeep, vertex);
					boolean hasSameBones = isSameBones(vertexToKeep, vertex);


					if (hasSameUvCoord && hasSameNormal && hasSameBones) {
						oldToNew.put(vertex, vertexToKeep);
						geoset.remove(vertex);
						for (Triangle triangle : vertex.getTriangles()) {
							triangle.replace(vertex, vertexToKeep);
						}
					}
				}
			}
		}
	}

	private void sanitizeGeoset(Geoset geoset) {
		List<GeosetVertex> geosetVertexList = geoset.getVertices();
		Set<GeosetVertex> vertexSet = new HashSet<>(geosetVertexList);
		List<Triangle> geosetTriangleList = geoset.getTriangles();
		Set<Triangle> triangleSetSet = new HashSet<>(geosetTriangleList);

		for (Triangle triangle : geosetTriangleList) {
			if (triangle.get(0) == null || triangle.get(1) == null || triangle.get(2) == null) {
				System.out.println("triangle missing vert!!!!!");
			}
			if (!vertexSet.contains(triangle.get(0))) {
				if (true) {
					System.out.println("triangle contained missing vert!");
					geoset.add(triangle.get(0));
				}

			}
			if (!vertexSet.contains(triangle.get(1))) {
				if (true) {
					System.out.println("triangle contained missing vert!");
					geoset.add(triangle.get(1));
				}

			}
			if (!vertexSet.contains(triangle.get(2))) {
				if (true) {
					System.out.println("triangle contained missing vert!");
					geoset.add(triangle.get(2));
				}

			}
			for (GeosetVertex vertex : triangle.getVerts()) {
				if (!vertex.hasTriangle(triangle)) {
					System.out.println("vertex was missing triangle!");
					vertex.addTriangle(triangle);
				}
			}
		}

		for (GeosetVertex vertex : vertexSet) {
			if (!triangleSetSet.containsAll(vertex.getTriangles())) {
				System.out.println("Vert contained missing tri");
				vertex.getTriangles().removeIf(t -> !triangleSetSet.contains(t) || !t.contains(vertex));
			}
		}

	}


	@Override
	public UndoAction redo() {
		for (Geoset geoset : geosetToLocationToGVs.keySet()) {
			Map<InexactHashVector, List<GeosetVertex>> locationVertMap = geosetToLocationToGVs.get(geoset);
			for (InexactHashVector location : locationVertMap.keySet()) {
				List<GeosetVertex> verticesAtLocation = locationVertMap.get(location);
				removeVerts(verticesAtLocation, geoset);
			}
		}
		for (Geoset geoset : geosetToAffectedTris.keySet()) {
			Set<Triangle> trianglesToRemove = removeTriangles(new ArrayList<>(geosetToAffectedTris.get(geoset)), geoset);

			removedTrisMap1.put(geoset, trianglesToRemove);
//			removeVertTris(geoset);
		}
		for (Geoset geoset : geosets) {
			sanitizeGeoset(geoset);
		}

		return this;
	}


	private boolean isSameNormal(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		return vertexToKeep.getNormal() == null
				&& vertex.getNormal() == null
				|| vertexToKeep.getNormal() != null
				&& vertex.getNormal() != null
				&& vertexToKeep.getNormal().distance(vertex.getNormal()) < 0.001f;
	}

	private boolean isSameBones(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		if (vertexToKeep.getSkinBones() != null && vertex.getSkinBones() != null) {
			return Arrays.equals(vertexToKeep.getSkinBones(), vertex.getSkinBones());
		} else if (vertexToKeep.getBones().size() > 0 && vertexToKeep.getBones().size() == vertexToKeep.getBones().size()) {
			return vertexToKeep.getBones().containsAll(vertex.getBones());
		}
		return false;
	}

	private boolean isSameUvCoord(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		List<Vec2> tverts1 = vertexToKeep.getTverts();
		List<Vec2> tverts2 = vertex.getTverts();
		for (int tvI = 0; tvI < tverts1.size() && tvI < tverts2.size(); tvI++) {
			if (tverts1.get(tvI).distance(tverts2.get(tvI)) > 0.00001f) {
				return false;
			}
		}
		return true;
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : geosetToOldToNew.keySet()) {
			Map<GeosetVertex, GeosetVertex> oldToNew = geosetToOldToNew.get(geoset);
			for (GeosetVertex vertex : oldToNew.keySet()) {
				geoset.add(vertex);
			}
		}

		for (Geoset geoset : geosetToOrgVertToAffTris.keySet()) {
			Map<GeosetVertex, List<Triangle>> orgVertToTris = geosetToOrgVertToAffTris.get(geoset);
			Map<GeosetVertex, GeosetVertex> oldToNew = geosetToOldToNew.get(geoset);
			for (GeosetVertex vertex : orgVertToTris.keySet()) {
				List<Triangle> triangles = orgVertToTris.get(vertex);
				if (oldToNew.get(vertex) != null) {
					for (Triangle triangle : triangles) {
						triangle.replace(oldToNew.get(vertex), vertex);
//						vertex.addTriangle(triangle);
					}
				}
			}
		}

		for (Geoset geoset : removedTrisMap1.keySet()) {
			for (Triangle triangle : removedTrisMap1.get(geoset)) {
				geoset.addExtended(triangle);
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Simplify Geosets";
	}
}
