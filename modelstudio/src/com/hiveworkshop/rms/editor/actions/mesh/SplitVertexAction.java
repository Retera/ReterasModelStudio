package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public final class SplitVertexAction implements UndoAction {

	private final ModelStructureChangeListener modelStructureChangeListener;
	private final EditableModel model;
	private final ModelView modelView;

	BiMap<Geoset, Geoset> oldGeoToNewGeo = new BiMap<>();


	List<Triangle> addedTriangles = new ArrayList<>();
	Set<Triangle> selectedEdgeTriangles = new HashSet<>();
	Set<Triangle> notSelectedEdgeTriangles = new HashSet<>();
	List<GeosetVertex> affectedVertices = new ArrayList<>();
	Set<GeosetVertex> orgEdgeVertices = new HashSet<>();
	Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
	Set<Pair<GeosetVertex, GeosetVertex>> edges;


	public SplitVertexAction(ModelStructureChangeListener modelStructureChangeListener,
	                         ModelView modelView) {
		this.modelView = modelView;
		this.model = modelView.getModel();
		this.modelStructureChangeListener = modelStructureChangeListener;

		affectedVertices.addAll(modelView.getSelectedVertices());

		findInternalEdgeVerts();
		edges = ModelUtils.getEdges(affectedVertices);
		findEdgeTris();
		makeVertCopies();

		findEdgeTris();
		makeVertCopies();
	}

	private void findInternalEdgeVerts() {
		for (GeosetVertex geosetVertex : affectedVertices) {
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (!affectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
					orgEdgeVertices.add(geosetVertex);
				}
			}
		}
	}

	private void findEdgeTris() {
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (!affectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
					notSelectedEdgeTriangles.add(triangle);
				} else {
					selectedEdgeTriangles.add(triangle);
				}
			}
		}
	}

	private void makeVertCopies() {
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			oldToNew.put(geosetVertex, geosetVertex.deepCopy());
		}
	}

	private void splitEdge() {
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			GeosetVertex newVertex = oldToNew.get(geosetVertex);
			List<Triangle> trisToRemove = new ArrayList<>();
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (selectedEdgeTriangles.contains(triangle)) {
					newVertex.removeTriangle(triangle);
				} else if (notSelectedEdgeTriangles.contains(triangle)) {
					trisToRemove.add(triangle);
					triangle.replace(geosetVertex, newVertex);
				}
			}
			trisToRemove.forEach(t -> geosetVertex.removeTriangle(t));
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

	@Override
	public UndoAction undo() {
		for (GeosetVertex newVert : oldToNew.values()) {
			newVert.getGeoset().remove(newVert);
		}
		for (Triangle triangle : addedTriangles) {
			triangle.getGeoset().remove(triangle);
		}
		unSplitEdge();

		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		splitEdge();
		for (GeosetVertex newVert : oldToNew.values()) {
			newVert.getGeoset().add(newVert);
			for (Triangle triangle : newVert.getTriangles()) {
				newVert.getGeoset().add(triangle);
			}
		}
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "split vertex";
	}

}
