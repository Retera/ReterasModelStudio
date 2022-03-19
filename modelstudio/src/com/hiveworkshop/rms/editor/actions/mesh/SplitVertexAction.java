package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public final class SplitVertexAction implements UndoAction {

	private final ModelStructureChangeListener changeListener;

	private final Set<Triangle> mainEdgeTriangles = new HashSet<>();
	private final Set<Triangle> otherEdgeTriangles = new HashSet<>();
	private final Set<GeosetVertex> selectedVertices = new HashSet<>();
	private final Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();


	public SplitVertexAction(Collection<GeosetVertex> selectedVertices, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.selectedVertices.addAll(selectedVertices);

		collectEdgeTris();
	}

	private void collectEdgeTris(){
		Map<Geoset, List<LinkedList<GeosetVertex>>> geosetEdgeListMap = collectEdges(selectedVertices);

		// Collect all fully selected triangles
		for (GeosetVertex geosetVertex : selectedVertices) {
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (!otherEdgeTriangles.contains(triangle) && !mainEdgeTriangles.contains(triangle)) {
					int selVertCount = selVertCount(triangle);

					if (selVertCount == 3) {
						mainEdgeTriangles.add(triangle);
					}
				}
			}
		}

		// Go through and check all edges
		for (List<LinkedList<GeosetVertex>> listList : geosetEdgeListMap.values()){
			for (LinkedList<GeosetVertex> list : listList){
//				if(list.size() == 1){
//					singleVertStuff(list.get(0));
//				} else {
//				}
				collectEdgeTris1211(list);

			}
		}

	}

	private void collectEdgeTris1211(LinkedList<GeosetVertex> list) {
		Set<GeosetVertex> mainEdgeTriVerts = new HashSet<>();
		Set<GeosetVertex> otherEdgeTriVerts = new HashSet<>();
		Set<GeosetVertex> vertsToCheckAgain = new HashSet<>();

		// skipp first and last
		for (int i = 1; i<list.size()-1; i++){
			GeosetVertex geosetVertex = list.get(i);
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (!otherEdgeTriangles.contains(triangle) && !mainEdgeTriangles.contains(triangle)) {
					Set<Triangle> neighbourTris = getVertTrisOnOneSide(geosetVertex, triangle, new HashSet<>());
					for(Triangle tri : neighbourTris){
						Triangle neighbourOtherEdgeTri = selVertCount(tri) == 2 ? getNeighbourOtherEdgeTri(geosetVertex, tri) : null;
						if(otherEdgeTriangles.contains(neighbourOtherEdgeTri)){
							mainEdgeTriangles.addAll(neighbourTris);
							neighbourTris.forEach(t -> mainEdgeTriVerts.addAll(Arrays.asList(t.getVerts())));
							break;
						} else if(mainEdgeTriangles.contains(neighbourOtherEdgeTri)){
							otherEdgeTriangles.addAll(neighbourTris);
							neighbourTris.forEach(t -> otherEdgeTriVerts.addAll(Arrays.asList(t.getVerts())));
							break;
						}
					}
					if (mainEdgeTriVerts.isEmpty() && otherEdgeTriVerts.isEmpty()){
						mainEdgeTriangles.addAll(neighbourTris);
						neighbourTris.forEach(t -> mainEdgeTriVerts.addAll(Arrays.asList(t.getVerts())));
					}
				}
			}
			if(otherEdgeTriangles.containsAll(geosetVertex.getTriangles())){
				vertsToCheckAgain.add(geosetVertex);
			}
		}
		for(GeosetVertex vertex : vertsToCheckAgain){
			if(otherEdgeTriangles.containsAll(vertex.getTriangles())){
				checkFullyAdded(mainEdgeTriVerts, vertex);
			}
		}
		GeosetVertex first = list.getFirst();
		handleStartAndEnd(mainEdgeTriVerts, otherEdgeTriVerts, first);
		GeosetVertex last = list.getLast();
		if(last != first){
			handleStartAndEnd(mainEdgeTriVerts, otherEdgeTriVerts, last);
		}

	}

	private void handleStartAndEnd(Set<GeosetVertex> mainEdgeTriVerts, Set<GeosetVertex> otherEdgeTriVerts, GeosetVertex vertex) {
		List<Triangle> tempTriList = new ArrayList<>(vertex.getTriangles());
		int infStopper = tempTriList.size()*2;
		while (!tempTriList.isEmpty() && infStopper>=0){
			for (int i = tempTriList.size()-1; i>=0; i--) {
				Triangle triangle = tempTriList.get(i);
				if (!otherEdgeTriangles.contains(triangle) && !mainEdgeTriangles.contains(triangle)) {
					if(selVertCount(triangle) == 1){
						if (vertCountInSet(triangle, mainEdgeTriVerts) >= 2){
							mainEdgeTriangles.add(triangle);
							mainEdgeTriVerts.addAll(Arrays.asList(triangle.getVerts()));
							tempTriList.remove(triangle);
						} else if (vertCountInSet(triangle, otherEdgeTriVerts) >= 2){
							otherEdgeTriangles.add(triangle);
							otherEdgeTriVerts.addAll(Arrays.asList(triangle.getVerts()));
							tempTriList.remove(triangle);
						}
					} else if(selVertCount(triangle) == 2){
						Triangle otherEdgeTri = getNeighbourOtherEdgeTri(vertex, triangle);
						if(mainEdgeTriVerts.isEmpty() && otherEdgeTriVerts.isEmpty() || otherEdgeTriangles.contains(otherEdgeTri) || vertCountInSet(triangle, mainEdgeTriVerts) == 3){
							mainEdgeTriangles.add(triangle);
							mainEdgeTriVerts.addAll(Arrays.asList(triangle.getVerts()));
							tempTriList.remove(triangle);
						} else if (mainEdgeTriangles.contains(otherEdgeTri) || vertCountInSet(triangle, otherEdgeTriVerts) == 3){
							otherEdgeTriangles.add(triangle);
							otherEdgeTriVerts.addAll(Arrays.asList(triangle.getVerts()));
							tempTriList.remove(triangle);
						}
					}
				} else {
					tempTriList.remove(triangle);
				}
			}
			infStopper--;
		}
		otherEdgeTriangles.addAll(tempTriList);
		if(otherEdgeTriangles.containsAll(vertex.getTriangles())){
			checkFullyAdded(mainEdgeTriVerts, vertex);
		}
	}

	private void checkFullyAdded(Set<GeosetVertex> mainEdgeTriVerts, GeosetVertex vertex) {
		if (vertex.getTriangles().stream().anyMatch(t -> selVertCount(t) == 2)) {
			for (Triangle triangle : vertex.getTriangles()) {
				if (selVertCount(triangle) == 2) {
					Triangle neighbourOtherEdgeTri = getNeighbourOtherEdgeTri(vertex, triangle);
					if (neighbourOtherEdgeTri != null && selVertCount(neighbourOtherEdgeTri) == 2) {
						otherEdgeTriangles.remove(triangle);
						mainEdgeTriangles.add(triangle);
						mainEdgeTriVerts.addAll(Arrays.asList(triangle.getVerts()));
						break;
					}
				}
			}
		} else {
			Triangle tri = vertex.getTriangles().stream().findAny().orElse(null);
			if(tri != null){
				Set<Triangle> neighbourTris = new HashSet<>();
				for (GeosetVertex vert : tri.getVerts()) {
					if (vert != vertex) {
						vert.getTriangles().stream()
								.filter(t -> t != tri && t.containsRef(vertex))
								.forEach(neighbourTris::add);
					}
				}
				mainEdgeTriangles.add(tri);
				otherEdgeTriangles.remove(tri);
				if (neighbourTris.size() + 1 != vertex.getTriangles().size()) {
					mainEdgeTriangles.addAll(neighbourTris);
					otherEdgeTriangles.removeAll(neighbourTris);
				}
			}
		}
	}

	private Set<Triangle> getVertTrisOnOneSide(GeosetVertex geosetVertex, Triangle triangle, Set<Triangle> neighbourTris) {
		neighbourTris.add(triangle);
		for (GeosetVertex vertex : triangle.getVerts()) {
			if (vertex != geosetVertex && !selectedVertices.contains(vertex)) {
				for (Triangle tri : vertex.getTriangles()) {
					if (!neighbourTris.contains(tri) && tri.containsRef(geosetVertex)) {
						getVertTrisOnOneSide(geosetVertex, tri, neighbourTris);
					}
				}
			}
		}
		return neighbourTris;
	}

	private Triangle getNeighbourOtherEdgeTri(GeosetVertex geosetVertex, Triangle triangle) {
		int i = triangle.indexOf(geosetVertex);
		GeosetVertex v1 = triangle.get((i+1)%3);
		GeosetVertex v2 = triangle.get((i+2)%3);
		GeosetVertex otherSelected = selectedVertices.contains(v1) ? v1 : v2;

		for (Triangle tri : geosetVertex.getTriangles()) {
			if (tri != triangle && tri.containsRef(otherSelected) && tri.containsRef(geosetVertex)) {
				return tri;
			}
		}
		return null;
	}
//	private void singleVertStuff1(GeosetVertex geosetVertex) {
//		boolean hasAddedToMain = false;
//		for (Triangle triangle : geosetVertex.getTriangles()) {
//			if(!otherEdgeTriangles.contains(triangle) && !mainEdgeTriangles.contains(triangle)){
//				Set<Triangle> neighbourTris = getNeighbourTris(geosetVertex, triangle);
//
//				if(!hasAddedToMain){
//					mainEdgeTriangles.add(triangle);
//					mainEdgeTriangles.addAll(neighbourTris);
//					hasAddedToMain = true;
//				} else {
//					otherEdgeTriangles.add(triangle);
//					otherEdgeTriangles.addAll(neighbourTris);
//				}
//			}
//		}
//	}
//	private void singleVertStuff(GeosetVertex geosetVertex) {
//		System.out.println("single " + geosetVertex.getTVertex(0));
//		boolean hasAddedToMain = false;
//		for (Triangle triangle : geosetVertex.getTriangles()) {
//			if(!otherEdgeTriangles.contains(triangle) && !mainEdgeTriangles.contains(triangle)){
////				Set<Triangle> neighbourTris = new HashSet<>();
////				Arrays.stream(triangle.getVerts())
////						.filter(v -> v != geosetVertex)
////						.forEach(v -> neighbourTris.addAll(v.getTriangles()));
////				neighbourTris.removeIf(t -> t == triangle || !t.containsRef(geosetVertex));
//
//				Set<Triangle> neighbourTris = new HashSet<>();
//				for (GeosetVertex vertex : triangle.getVerts()) {
//					if (vertex != geosetVertex) {
//						vertex.getTriangles().stream()
//								.filter(t -> t != triangle && t.containsRef(geosetVertex))
//								.forEach(neighbourTris::add);
//					}
//				}
//
//				if(!hasAddedToMain){
//					mainEdgeTriangles.add(triangle);
//					if(neighbourTris.size()+1 != geosetVertex.getTriangles().size()){
//						mainEdgeTriangles.addAll(neighbourTris);
//					} else {
//						otherEdgeTriangles.addAll(neighbourTris);
//					}
//					hasAddedToMain = true;
//				} else {
//					otherEdgeTriangles.add(triangle);
//					otherEdgeTriangles.addAll(neighbourTris);
//				}
//			}
//		}
//	}
//
//	private Set<Triangle> getNeighbourTris(GeosetVertex geosetVertex, Triangle triangle) {
//		Set<Triangle> neighbourTris = new HashSet<>();
//		for (GeosetVertex vertex : triangle.getVerts()) {
//			if (vertex != geosetVertex && !selectedVertices.contains(vertex)) {
//				for (Triangle tri : vertex.getTriangles()) {
//					if (tri != triangle && tri.containsRef(geosetVertex)) {
//						neighbourTris.add(tri);
//					}
//				}
//			}
//		}
//		return neighbourTris;
//	}

	private int selVertCount(Triangle triangle){
		int count = 0;
		for(GeosetVertex vertex : triangle.getVerts()){
			if(selectedVertices.contains(vertex)){
				count++;
			}
		}
		return count;
	}

	private int vertCountInSet(Triangle triangle, Set<GeosetVertex> vertexSet){
		int count = 0;
		for(GeosetVertex vertex : triangle.getVerts()){
			if(vertexSet.contains(vertex)){
				count++;
			}
		}
		return count;
	}


	private Map<Geoset, List<LinkedList<GeosetVertex>>> collectEdges(Collection<GeosetVertex> selection) {
		Set<GeosetVertex> vertexPool = new HashSet<>(selection);
		Map<Geoset, List<LinkedList<GeosetVertex>>> edgeMap = new HashMap<>();
		while (!vertexPool.isEmpty()){
			GeosetVertex entryVertex = vertexPool.stream().findFirst().get();
			vertexPool.remove(entryVertex);
			List<LinkedList<GeosetVertex>> edges = edgeMap.computeIfAbsent(entryVertex.getGeoset(), k -> new ArrayList<>());

			LinkedList<GeosetVertex> currEdge = new LinkedList<>();
			edges.add(currEdge);

			currEdge.add(entryVertex);

			GeosetVertex validNeighbour;
			do {
				validNeighbour = getValidNeighbour(vertexPool, currEdge.getLast());
				if(validNeighbour != null){
					currEdge.addLast(validNeighbour);
					vertexPool.remove(validNeighbour);
				}
			} while (validNeighbour != null);

			do {
				validNeighbour = getValidNeighbour(vertexPool, currEdge.getFirst());
				if(validNeighbour != null){
					currEdge.addFirst(validNeighbour);
					vertexPool.remove(validNeighbour);
				}
			} while (validNeighbour != null);
		}
		return edgeMap;
	}

	private GeosetVertex getValidNeighbour(Set<GeosetVertex> vertexPool, GeosetVertex vertex) {
		for(Triangle triangle : vertex.getTriangles()){
			for(GeosetVertex vert : triangle.getVerts()){
				if(vertexPool.contains(vert)){
					return vert;
				}
			}
		}
		return null;
	}

	private void splitEdge() {
		for (GeosetVertex geosetVertex : selectedVertices) {
			GeosetVertex newVertex = oldToNew.computeIfAbsent(geosetVertex, k -> geosetVertex.deepCopy());
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if(otherEdgeTriangles.contains(triangle)){
					triangle.replace(geosetVertex, newVertex);
				} else {
					newVertex.removeTriangle(triangle);
				}
			}
			geosetVertex.removeTriangles(newVertex.getTriangles());
			if(!newVertex.getTriangles().isEmpty()){
				newVertex.getGeoset().add(newVertex);
			}
		}
	}

	private void unSplitEdge() {
		for (GeosetVertex geosetVertex : selectedVertices) {
			GeosetVertex newVertex = oldToNew.get(geosetVertex);
			for (Triangle triangle : newVertex.getTriangles()) {

				geosetVertex.addTriangle(triangle);
				triangle.replace(newVertex, geosetVertex);
			}
			newVertex.getGeoset().remove(newVertex);
		}
	}

	@Override
	public UndoAction undo() {
		unSplitEdge();

		changeListener.geosetsUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		splitEdge();
		changeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "split vertex";
	}

}
