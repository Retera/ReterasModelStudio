package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.HashableVector;
import com.hiveworkshop.rms.util.Vec2;

import java.util.*;
import java.util.stream.Collectors;

public class SimplifyGeometryAction implements UndoAction {
	Set<GeosetVertex> affectedVertices = new HashSet<>();
	Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
	Map<GeosetVertex, List<Triangle>> orgVertToAffTris = new HashMap<>();
	Map<HashableVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
	Set<Geoset> geosets = new HashSet<>();

	Map<Geoset, Set<Triangle>> removedTrisMap1 = new HashMap<>();
	Map<Geoset, Set<Triangle>> removedTrisMap = new HashMap<>();
	Map<Geoset, Set<GeosetVertex>> removedVertsMap1 = new HashMap<>();
	Map<Geoset, Set<GeosetVertex>> removedVertsMap = new HashMap<>();


	Map<GeosetVertex, Set<Triangle>> orgVertToOrgTris = new HashMap<>();

	public SimplifyGeometryAction(Collection<GeosetVertex> selection) {
		affectedVertices.addAll(selection);
		System.out.println("looking at " + affectedVertices.size() + "vertices");

		for (GeosetVertex vertex : affectedVertices) {
			HashableVector location = new HashableVector(vertex);
			List<GeosetVertex> gvAtLocation = locationToGVs.computeIfAbsent(location, gvSet -> new ArrayList<>());
			gvAtLocation.add(vertex);
			geosets.add(vertex.getGeoset());

			List<Triangle> vertTris = orgVertToAffTris.computeIfAbsent(vertex, triSet -> new ArrayList<>());
			vertTris.addAll(vertex.getTriangles());
		}

		for (HashableVector location : locationToGVs.keySet()) {
			List<GeosetVertex> verticesAtLocation = locationToGVs.get(location);
			for (Geoset geoset : geosets) {
				findVertsToRemove(verticesAtLocation, geoset);
			}
		}

		for (Geoset geoset : geosets) {
			Set<Triangle> trianglesToRemove = new HashSet<>();
			List<Triangle> triangles = geoset.getTriangles().stream()
					.filter(t -> affectedVertices.containsAll(Arrays.asList(t.getVerts())))
					.collect(Collectors.toList());
			fintTrisToRemove(trianglesToRemove, triangles);
			removedTrisMap1.put(geoset, trianglesToRemove);
		}

	}

	private void fintTrisToRemove(Set<Triangle> trianglesToRemove, List<Triangle> triangles) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle triangleToKeep = triangles.get(i);
			if (!trianglesToRemove.contains(triangleToKeep)) {
				for (GeosetVertex vertex : triangleToKeep.getVerts()) {
					orgVertToOrgTris.computeIfAbsent(vertex, t -> new HashSet<>()).add(triangleToKeep);
				}
				Set<GeosetVertex> newTriVerts1 = getNewTriVertices(triangleToKeep);
				for (int j = i + 1; j < triangles.size(); j++) {
					Triangle triangle = triangles.get(j);

					Set<GeosetVertex> newTriVerts2 = getNewTriVertices(triangle);

					if (newTriVerts1.containsAll(newTriVerts2)) {
						trianglesToRemove.add(triangle);
					}
				}
			}
		}
	}

	public Set<GeosetVertex> getNewTriVertices(Triangle triangle) {
		Set<GeosetVertex> newTriVerts = new HashSet<>();
		for (GeosetVertex vertex : triangle.getVerts()) {
			newTriVerts.add(oldToNew.getOrDefault(vertex, vertex));
		}
		return newTriVerts;
	}

	public void findVertsToRemove(List<GeosetVertex> verticesAtLocation, Geoset geoset) {
		List<GeosetVertex> vertices = verticesAtLocation.stream()
				.filter(v -> v.getGeoset() == geoset)
				.collect(Collectors.toList());

		Set<GeosetVertex> verticesToRemove = removedVertsMap1.computeIfAbsent(geoset, o -> new HashSet<>());
		for (int i = 0; i < vertices.size(); i++) {
			GeosetVertex vertexToKeep = vertices.get(i);
			if (!oldToNew.containsKey(vertexToKeep)) {
				for (int j = i + 1; j < vertices.size(); j++) {
					GeosetVertex vertex = vertices.get(j);
					boolean hasSameUvCoord = isSameUvCoord(vertexToKeep, vertex);
					boolean hasSameNormal = isSameNormal(vertexToKeep, vertex);

					if (hasSameUvCoord && hasSameNormal) {
						oldToNew.put(vertex, vertexToKeep);
						verticesToRemove.add(vertex);
					}
				}
			}
		}
	}

	@Override
	public UndoAction redo() {

//		orgVertToOrgTris.clear();
		for (GeosetVertex vertex : orgVertToOrgTris.keySet()) {
			GeosetVertex newVertex = oldToNew.get(vertex);
			if (newVertex != null) {
				for (Triangle triangle : orgVertToOrgTris.get(vertex)) {
					triangle.replace(vertex, newVertex);
				}
			}
		}
		for (Geoset geoset : removedTrisMap1.keySet()) {
			for (Triangle triangle : removedTrisMap1.get(geoset)) {
				geoset.remove(triangle);
			}
		}
		for (Geoset geoset : removedVertsMap1.keySet()) {
			for (GeosetVertex vertex : removedVertsMap1.get(geoset)) {
				geoset.remove(vertex);
//				GeosetVertex vertexToKeep = oldToNew.get(vertex);
//				for(Triangle triangle : vertex.getTriangles()){
//					if(geoset.contains(triangle)){
//						triangle.replace(vertex, vertexToKeep);
//					}
//				}
			}
		}


//		for (Geoset geoset : geosets){
//			Set<Triangle> trianglesToRemove = new HashSet<>();
//			Set<GeosetVertex> verticesToRemove = new HashSet<>();
//			List<Triangle> triangles = geoset.getTriangles().stream()
//					.filter(t -> affectedVertices.containsAll(Arrays.asList(t.getVerts())))
//					.collect(Collectors.toList());
//			for (int i = 0; i< triangles.size(); i++){
//				Triangle triangleToKeep = triangles.get(i);
//				if (!trianglesToRemove.contains(triangleToKeep)){
//					for(int j = i+1; j < triangles.size(); j++){
//						Triangle triangle = triangles.get(j);
//						if (triangleToKeep.sameVerts(triangle)){
//							trianglesToRemove.add(triangle);
//							for (GeosetVertex vertex : triangle.getVerts()){
//								if (oldToNew.containsKey(vertex)){
//									verticesToRemove.add(vertex);
//								}
//							}
//						}
//					}
//				}
//			}
//			removedTrisMap.put(geoset, trianglesToRemove);
//			removedVertsMap.put(geoset, verticesToRemove);
//			geoset.removeTriangles(trianglesToRemove);
//			geoset.remove(verticesToRemove);
//
//			System.out.println("removed "+ trianglesToRemove + "triangles");
//		}

		return this;
	}


	private boolean isSameNormal(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		return vertexToKeep.getNormal() == null
				&& vertex.getNormal() == null
				|| vertexToKeep.getNormal() != null
				&& vertex.getNormal() != null
				&& vertexToKeep.getNormal().distance(vertex.getNormal()) < 0.0001f;
	}

	private boolean isSameUvCoord(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		List<Vec2> tverts1 = vertexToKeep.getTverts();
		List<Vec2> tverts2 = vertex.getTverts();
		for (int tvI = 0; tvI < tverts1.size() && tvI < tverts2.size(); tvI++) {
			if (tverts1.get(tvI).distance(tverts2.get(tvI)) > 0.0001f) {
				return false;
			}
		}
		return true;
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : removedVertsMap1.keySet()) {
			for (GeosetVertex vertex : removedVertsMap1.get(geoset)) {
				geoset.add(vertex);
			}
		}
		for (GeosetVertex vertex : orgVertToOrgTris.keySet()) {
			GeosetVertex newVertex = oldToNew.get(vertex);
			if (newVertex != null) {
				for (Triangle triangle : orgVertToOrgTris.get(vertex)) {
					triangle.replace(newVertex, vertex);
				}
			}
		}
		for (Geoset geoset : removedTrisMap1.keySet()) {
			for (Triangle triangle : removedTrisMap1.get(geoset)) {
				geoset.add(triangle);
			}
		}


//		for (Geoset geoset : removedTrisMap1.keySet()){
//			geoset.addTriangles(removedTrisMap1.get(geoset));
//		}
//		for (Geoset geoset : removedVertsMap1.keySet()){
//			geoset.addVerticies(removedVertsMap1.get(geoset));
//		}
//		for (GeosetVertex vertex : orgVertToAffTris.keySet()){
//			for (Triangle triangle : orgVertToAffTris.get(vertex)){
//				triangle.replace(oldToNew.get(vertex), vertex);
//			}
//			vertex.getGeoset().add(vertex);
//		}
		return this;
	}

	@Override
	public String actionName() {
		return "Simplify Geosets";
	}
}
