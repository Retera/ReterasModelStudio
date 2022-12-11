package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.util.Pair;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class ExtendAction implements UndoAction {
	List<Vec3> selection;
	List<Triangle> addedTriangles = new ArrayList<>();
	Set<Triangle> notSelectedEdgeTriangles;
	Set<GeosetVertex> affectedVertices = new HashSet<>();
	Set<GeosetVertex> orgEdgeVertices;
	Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
	Set<Pair<GeosetVertex, GeosetVertex>> edges;

	public ExtendAction(Collection<GeosetVertex> selection) {
		affectedVertices.addAll(selection);
		this.selection = new ArrayList<>(selection);

		edges = ModelUtils.getEdges(affectedVertices);
		orgEdgeVertices = collectEdgeVerts(edges);
		notSelectedEdgeTriangles = getNotSelectedEdgeTris(getAllEdgeTris(orgEdgeVertices), affectedVertices);
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			oldToNew.put(geosetVertex, geosetVertex.deepCopy());
		}
	}

	private Set<GeosetVertex> collectEdgeVerts(Set<Pair<GeosetVertex, GeosetVertex>> edges) {
		Set<GeosetVertex> orgEdgeVertices = new HashSet<>();
		for (Pair<GeosetVertex, GeosetVertex> edge : edges) {
			orgEdgeVertices.add(edge.getFirst());
			orgEdgeVertices.add(edge.getSecond());
		}
		return orgEdgeVertices;
	}

	private Set<Triangle> getAllEdgeTris(Set<GeosetVertex> orgEdgeVertices) {
		Set<Triangle> edgeTriangles = new HashSet<>();
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			edgeTriangles.addAll(geosetVertex.getTriangles());
		}
		return edgeTriangles;
	}
	private Set<Triangle> getNotSelectedEdgeTris(Set<Triangle> edgeTriangles, Set<GeosetVertex> affectedVertices) {
		Set<Triangle> notSelectedEdgeTriangles = new HashSet<>();
		for (Triangle triangle : edgeTriangles) {
			if (!affectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
				notSelectedEdgeTriangles.add(triangle);
			}
		}
		return notSelectedEdgeTriangles;
	}

	private void splitEdge() {
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			GeosetVertex newVertex = oldToNew.get(geosetVertex);
			List<Triangle> trisToRemove = new ArrayList<>();
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (notSelectedEdgeTriangles.contains(triangle)) {
					trisToRemove.add(triangle);
					triangle.replace(geosetVertex, newVertex);
					newVertex.addTriangle(triangle);
				}
			}
			trisToRemove.forEach(geosetVertex::removeTriangle);
		}
	}

	private void unSplitEdge() {
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			GeosetVertex newVertex = oldToNew.get(geosetVertex);
			for (Triangle triangle : newVertex.getTriangles()) {

				geosetVertex.addTriangle(triangle);
				triangle.replace(newVertex, geosetVertex);
			}
		}
	}

	private void fillGap() {
		for (Pair<GeosetVertex, GeosetVertex> edge : edges) {
			GeosetVertex org1 = edge.getFirst();
			GeosetVertex org2 = edge.getSecond();
			GeosetVertex new1 = oldToNew.get(edge.getFirst());
			GeosetVertex new2 = oldToNew.get(edge.getSecond());

			Triangle tri1 = new Triangle(org1, new1, org2);
			tri1.setGeoset(org1.getGeoset());
			Triangle tri2 = new Triangle(new1, new2, org2);
			tri2.setGeoset(org1.getGeoset());
			addedTriangles.add(tri1);
			addedTriangles.add(tri2);
		}
	}

	private void removeGapFill() {
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			geosetVertex.getTriangles().removeAll(addedTriangles);
			oldToNew.get(geosetVertex).getTriangles().removeAll(addedTriangles);
		}
	}


	@Override
	public UndoAction redo() {
		splitEdge();
		fillGap();
		for (GeosetVertex newVert : oldToNew.values()) {
			newVert.getGeoset().add(newVert);
			for (Triangle triangle : newVert.getTriangles()) {
				newVert.getGeoset().add(triangle);
			}
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex newVert : oldToNew.values()) {
			newVert.getGeoset().remove(newVert);
		}
		for (Triangle triangle : addedTriangles) {
			triangle.getGeoset().remove(triangle);
		}
		removeGapFill();
		unSplitEdge();
		return this;
	}

	@Override
	public String actionName() {
		return "extend";
	}
}
