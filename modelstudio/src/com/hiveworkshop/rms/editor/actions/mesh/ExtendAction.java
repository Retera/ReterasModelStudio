package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public class ExtendAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final List<Triangle> gapFillTriangles;
	private final Set<Triangle> notSelectedEdgeTriangles;
	private final Set<GeosetVertex> orgEdgeVertices;
	private final Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
	private final Set<Pair<GeosetVertex, GeosetVertex>> edges;

	public ExtendAction(Collection<GeosetVertex> selection, ModelStructureChangeListener changeListener) {
		Set<GeosetVertex> affectedVertices = new HashSet<>(selection);
		this.changeListener = changeListener;

		edges = ModelUtils.getEdges(affectedVertices);
		orgEdgeVertices = collectEdgeVerts(edges);
		notSelectedEdgeTriangles = getNotSelectedEdgeTris(getAllEdgeTris(orgEdgeVertices), affectedVertices);
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			oldToNew.put(geosetVertex, geosetVertex.deepCopy());
		}
		gapFillTriangles = getGapFillTriangles();
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
			List<Triangle> trisToSplitOff = new ArrayList<>();
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (notSelectedEdgeTriangles.contains(triangle)) {
					trisToSplitOff.add(triangle);
					triangle.replace(geosetVertex, newVertex);
					newVertex.addTriangle(triangle);
				}
			}
			trisToSplitOff.forEach(geosetVertex::removeTriangle);
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

	private List<Triangle> getGapFillTriangles() {
		List<Triangle> gapFillTriangles = new ArrayList<>();
		for (Pair<GeosetVertex, GeosetVertex> edge : edges) {
			GeosetVertex org1 = edge.getFirst();
			GeosetVertex org2 = edge.getSecond();
			GeosetVertex new1 = oldToNew.get(edge.getFirst());
			GeosetVertex new2 = oldToNew.get(edge.getSecond());

			gapFillTriangles.add(new Triangle(org1, new1, org2, org1.getGeoset()));
			gapFillTriangles.add(new Triangle(new1, new2, org2, org1.getGeoset()));
		}
		return gapFillTriangles;
	}


	@Override
	public ExtendAction redo() {
		splitEdge();
		gapFillTriangles.forEach(triangle -> triangle.getGeoset().add(triangle.addToVerts()));
		oldToNew.values().forEach(vertex -> vertex.getGeoset().add(vertex));

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public ExtendAction undo() {
		oldToNew.values().forEach(vertex -> vertex.getGeoset().remove(vertex));
		gapFillTriangles.forEach(triangle -> triangle.getGeoset().remove(triangle.removeFromVerts()));

		unSplitEdge();
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Extend";
	}
}
