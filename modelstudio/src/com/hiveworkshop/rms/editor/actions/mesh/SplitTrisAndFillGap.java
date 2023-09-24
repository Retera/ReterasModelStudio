package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public class SplitTrisAndFillGap implements UndoAction {
	private final List<Triangle> gapFillTriangles;
	private final Set<Triangle> notSelectedEdgeTriangles;
	private final BiMap<GeosetVertex, GeosetVertex> oldToNew = new BiMap<>();
	private final Set<Pair<GeosetVertex, GeosetVertex>> edges;
	private final ModelStructureChangeListener changeListener;

	public SplitTrisAndFillGap(Collection<GeosetVertex> selection, ModelStructureChangeListener changeListener) {
		Set<GeosetVertex> affectedVertices = new HashSet<>(selection);
		this.changeListener = changeListener;

		edges = ModelUtils.getEdges(affectedVertices);
		Set<GeosetVertex> orgEdgeVertices = collectEdgeVerts(edges);
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
			if (!affectedVertices.contains(triangle.get(0))
					|| !affectedVertices.contains(triangle.get(1))
					|| !affectedVertices.contains(triangle.get(2))) {
				notSelectedEdgeTriangles.add(triangle);
			}
		}
		return notSelectedEdgeTriangles;
	}

	private void splitEdge() {
		for (Triangle triangle : notSelectedEdgeTriangles) {
			for (int i = 0; i < triangle.getVerts().length; i++) {
				GeosetVertex oldVertex = triangle.get(i);
				GeosetVertex newVertex = oldToNew.get(oldVertex);
				if (newVertex != null) {
					triangle.set(i, newVertex);
					newVertex.addTriangle(triangle);
					oldVertex.removeTriangle(triangle);
				}
			}
		}
	}

	private void unSplitEdge() {
		for (Triangle triangle : notSelectedEdgeTriangles) {
			for (int i = 0; i < triangle.getVerts().length; i++) {
				GeosetVertex newVertex = triangle.get(i);
				GeosetVertex oldVertex = oldToNew.getByValue(newVertex);
				if (oldVertex != null) {
					triangle.set(i, oldVertex);
					oldVertex.addTriangle(triangle);
					newVertex.removeTriangle(triangle);
				}
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
	public SplitTrisAndFillGap redo() {
		oldToNew.values().forEach(vertex -> vertex.getGeoset().add(vertex));
		splitEdge();
		gapFillTriangles.forEach(triangle -> triangle.getGeoset().add(triangle.addToVerts()));

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public SplitTrisAndFillGap undo() {
		gapFillTriangles.forEach(triangle -> triangle.getGeoset().remove(triangle.removeFromVerts()));
		unSplitEdge();
		oldToNew.values().forEach(vertex -> vertex.getGeoset().remove(vertex));

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
