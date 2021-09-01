package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public final class SplitGeosetAction implements UndoAction {

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


	public SplitGeosetAction(EditableModel model,
	                         ModelStructureChangeListener modelStructureChangeListener,
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

		createGeosetCopies(model);
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
			GeosetVertex newVertex = new GeosetVertex(geosetVertex);
			oldToNew.put(geosetVertex, newVertex);
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

	private void addGeosets() {
		for (GeosetVertex vertex : affectedVertices) {
			Geoset oldGeoset = vertex.getGeoset();
			oldGeoset.remove(vertex);
			Geoset newGeoset = oldGeoToNewGeo.get(oldGeoset);
			vertex.setGeoset(newGeoset);
			newGeoset.add(vertex);
			for (Triangle triangle : vertex.getTriangles()) {
				oldGeoset.remove(triangle);
				triangle.setGeoset(newGeoset);
				newGeoset.add(triangle);
			}
		}

		for (Geoset geoset : oldGeoToNewGeo.values()) {
			model.add(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.add(geoset.getGeosetAnim());
			}
		}
	}

	private void removeGeosets() {
		for (Geoset geoset : oldGeoToNewGeo.values()) {
			model.remove(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.remove(geoset.getGeosetAnim());
			}
		}

		for (GeosetVertex vertex : affectedVertices) {
			Geoset new2Geoset = vertex.getGeoset();
			new2Geoset.remove(vertex);
			Geoset old2Geoset = oldGeoToNewGeo.getByValue(new2Geoset);
			vertex.setGeoset(old2Geoset);
			old2Geoset.add(vertex);
			for (Triangle triangle : vertex.getTriangles()) {
				new2Geoset.remove(triangle);
				triangle.setGeoset(old2Geoset);
				old2Geoset.add(triangle);
			}
		}
	}

	@Override
	public UndoAction undo() {
		removeGeosets();

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
//		for (Geoset geoset : oldGeoToNewGeo.values()) {
//			model.add(geoset);
//			if (geoset.getGeosetAnim() != null) {
//				model.add(geoset.getGeosetAnim());
//			}
//		}
		splitEdge();
		for (GeosetVertex newVert : oldToNew.values()) {
			newVert.getGeoset().add(newVert);
			for (Triangle triangle : newVert.getTriangles()) {
				newVert.getGeoset().add(triangle);
			}
		}
		addGeosets();
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "split geoset";
	}

	private void createGeosetCopies(EditableModel model) {

		Set<Geoset> geosetsToCopy = new HashSet<>();
		for (GeosetVertex vert : affectedVertices) {
			geosetsToCopy.add(vert.getGeoset());
		}

		for (Geoset geoset : geosetsToCopy) {
			Geoset geosetCreated = new Geoset();
			if (geoset.getExtents() != null) {
				geosetCreated.setExtents(geoset.getExtents().deepCopy());
			}
			for (Animation anim : geoset.getAnims()) {
				geosetCreated.add(anim.deepCopy());
			}
			geosetCreated.setUnselectable(geoset.getUnselectable());
			geosetCreated.setSelectionGroup(geoset.getSelectionGroup());
			GeosetAnim geosetAnim = geoset.getGeosetAnim();
			if (geosetAnim != null) {
				GeosetAnim createdGeosetAnim = new GeosetAnim(geosetCreated, geosetAnim);
				geosetCreated.setGeosetAnim(createdGeosetAnim);
			}
			geosetCreated.setParentModel(model);
			geosetCreated.setMaterial(geoset.getMaterial());
			oldGeoToNewGeo.put(geoset, geosetCreated);
		}
	}

}
