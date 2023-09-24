package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public class BridgeEdgeAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final List<Triangle> addedTriangles;
	private List<LinkedList<GeosetVertex>> edges = new ArrayList<>();
	private Geoset geoset;

	public BridgeEdgeAction(Collection<GeosetVertex> selection, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;

		if (!selection.isEmpty()) {
			geoset = selection.stream().findFirst().get().getGeoset();
			edges = collectEdges(selection);

			if (1 < edges.size() && 1 < edges.get(0).size() && 1 < edges.get(1).size()) {
				LinkedList<GeosetVertex> firstEdge = edges.get(0);
				LinkedList<GeosetVertex> secondEdge = edges.get(1);
				if (firstEdge.getFirst().getGeoset() == secondEdge.getFirst().getGeoset()) {
					alignEdges();
				}
			}
		}

		addedTriangles = getFillTris();
	}

	private List<LinkedList<GeosetVertex>> collectEdges(Collection<GeosetVertex> selection) {
		Set<GeosetVertex> vertexPool = new HashSet<>(selection);
		List<LinkedList<GeosetVertex>> edges = new ArrayList<>();
		while (!vertexPool.isEmpty()) {
			GeosetVertex entryVertex = vertexPool.stream().findFirst().get();
			vertexPool.remove(entryVertex);

			if (entryVertex.getGeoset() == geoset) {
				LinkedList<GeosetVertex> currEdge = new LinkedList<>();
				edges.add(currEdge);

				currEdge.add(entryVertex);

				GeosetVertex validNeighbour;
				do {
					validNeighbour = getValidNeighbour(vertexPool, currEdge.getLast());
					if (validNeighbour != null) {
						currEdge.addLast(validNeighbour);
						vertexPool.remove(validNeighbour);
					}
				} while (validNeighbour != null);

				do {
					validNeighbour = getValidNeighbour(vertexPool, currEdge.getFirst());
					if (validNeighbour != null) {
						currEdge.addFirst(validNeighbour);
						vertexPool.remove(validNeighbour);
					}
				} while (validNeighbour != null);
			}
		}
		return edges;
	}

	private GeosetVertex getValidNeighbour(Set<GeosetVertex> vertexPool, GeosetVertex vertex) {
		for (Triangle triangle : vertex.getTriangles()) {
			for (GeosetVertex vert : triangle.getVerts()) {
				if (vertexPool.contains(vert)) {
					return vert;
				}
			}
		}
		return null;
	}

	private void alignEdges() {
		LinkedList<GeosetVertex> firstEdge = edges.get(0);
		LinkedList<GeosetVertex> secondEdge = edges.get(1);
		if (isCircular(secondEdge)) {
			alignStart(firstEdge.getFirst(), secondEdge);
		} else if (isCircular(firstEdge)) {
			alignStart(secondEdge.getFirst(), firstEdge);
		}

		if (firstEdge.get(1).distance(secondEdge.getLast()) < firstEdge.get(1).distance(secondEdge.get(1))) {
			LinkedList<GeosetVertex> tempList = new LinkedList<>();

			for (GeosetVertex vertex : secondEdge) {
				tempList.addFirst(vertex);
			}
			edges.set(1, tempList);
		}
	}

	public void alignStart(GeosetVertex first, LinkedList<GeosetVertex> circularEdge) {
		float shortestDistance = Float.MAX_VALUE;
		GeosetVertex closestVert = null;
		for (GeosetVertex vertex : circularEdge) {
			float currDist = first.distance(vertex);
			if (currDist < shortestDistance) {
				closestVert = vertex;
				shortestDistance = currDist;
			}
		}
		while (circularEdge.getFirst() != closestVert) {
			circularEdge.addLast(circularEdge.removeFirst());
		}
	}

	private boolean isCircular(LinkedList<GeosetVertex> edgeList) {

		return getValidNeighbour(new HashSet<>(Collections.singletonList(edgeList.getLast())), edgeList.getFirst()) != null;
	}


	private List<Triangle> getFillTris() {
		List<Triangle> addedTriangles = new ArrayList<>();
		if (1 < edges.size()) {
			LinkedList<GeosetVertex> firstEdge = edges.get(0);
			LinkedList<GeosetVertex> secondEdge = edges.get(1);
			GeosetVertex last_e1 = firstEdge.pollFirst();
			GeosetVertex last_e2 = secondEdge.pollFirst();
			GeosetVertex cand_e1 = firstEdge.pollFirst();
			GeosetVertex cand_e2 = secondEdge.pollFirst();
			while (last_e1 != null && last_e2 != null || !firstEdge.isEmpty() && !secondEdge.isEmpty()) {

				if (cand_e2 != null && (cand_e1 == null || last_e1.distance(cand_e2) < last_e2.distance(cand_e1))) {
					addedTriangles.add(new Triangle(last_e1, last_e2, cand_e2, geoset));
					last_e2 = cand_e2;
					cand_e2 = secondEdge.pollFirst();
				} else if (cand_e1 != null) {
					addedTriangles.add(new Triangle(last_e1, last_e2, cand_e1, geoset));
					last_e1 = cand_e1;
					cand_e1 = firstEdge.pollFirst();
				} else {
					break;
				}
			}
		}
		return addedTriangles;
	}

	@Override
	public BridgeEdgeAction redo() {
		if (geoset != null) {
			for (Triangle triangle : addedTriangles) {
				geoset.add(triangle.addToVerts());
			}

			if (changeListener != null) {
				changeListener.geosetsUpdated();
			}
		}
		return this;
	}

	@Override
	public BridgeEdgeAction undo() {
		if (geoset != null) {
			for (Triangle triangle : addedTriangles) {
				geoset.remove(triangle.removeFromVerts());
			}

			if (changeListener != null) {
				changeListener.geosetsUpdated();
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Bridge Edges";
	}
}
