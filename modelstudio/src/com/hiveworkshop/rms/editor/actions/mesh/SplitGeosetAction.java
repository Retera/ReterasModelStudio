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


	public SplitGeosetAction(Collection<GeosetVertex> vertsToSep, EditableModel model,
	                         ModelStructureChangeListener modelStructureChangeListener,
	                         ModelView modelView) {
		this.modelView = modelView;
		this.model = modelView.getModel();
		this.modelStructureChangeListener = modelStructureChangeListener;

		affectedVertices.addAll(vertsToSep);

		edges = ModelUtils.getEdges(affectedVertices);

		collectEdgeVerts();
		findEdgeTris();
		makeVertCopies();

		createGeosetCopies(model);

	}

	private void createGeosetCopies(EditableModel model) {

		Set<Geoset> geosetsToCopy = new HashSet<>();
		for (GeosetVertex vert : affectedVertices) {
			geosetsToCopy.add(vert.getGeoset());
		}

		for (Geoset geoset : geosetsToCopy) {
			Geoset geosetCreated = new Geoset();
			if (geoset.getExtents() != null) {
				geosetCreated.setExtents(new ExtLog(geoset.getExtents()));
			}
			for (Animation anim : geoset.getAnims()) {
				geosetCreated.add(new Animation(anim));
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

	private void collectEdgeVerts() {
		for (Pair<GeosetVertex, GeosetVertex> edge : edges) {
			orgEdgeVertices.add(edge.getFirst());
			orgEdgeVertices.add(edge.getSecond());
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

	private void splitGeosets() {
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
			trisToRemove.forEach(geosetVertex::removeTriangle);
		}
		for (GeosetVertex vertex : affectedVertices){
			Geoset oldGeoset = vertex.getGeoset();
			Geoset newGeoset = oldGeoToNewGeo.get(oldGeoset);
			vertex.setGeoset(newGeoset);
			oldGeoset.remove(vertex);
			newGeoset.add(vertex);
			for (Triangle triangle : vertex.getTriangles()){
				triangle.setGeoset(newGeoset);
				oldGeoset.remove(triangle);
				newGeoset.add(triangle);
			}
		}
	}

	private void unSplitGeosets() {
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			GeosetVertex newVertex = oldToNew.get(geosetVertex);
			for (Triangle triangle : newVertex.getTriangles()) {
				geosetVertex.addTriangle(triangle);
				triangle.replace(newVertex, geosetVertex);
			}
		}
		for (GeosetVertex vertex : affectedVertices){
			Geoset newGeoset = vertex.getGeoset();
			Geoset oldGeoset = oldGeoToNewGeo.getByValue(newGeoset);
			vertex.setGeoset(oldGeoset);
			newGeoset.remove(vertex);
			oldGeoset.add(vertex);
			for (Triangle triangle : vertex.getTriangles()){
				triangle.setGeoset(oldGeoset);
				newGeoset.remove(triangle);
				oldGeoset.add(triangle);
			}
		}
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : oldGeoToNewGeo.values()) {
			model.remove(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.remove(geoset.getGeosetAnim());
			}
		}

		for (GeosetVertex newVert : oldToNew.values()) {
			newVert.getGeoset().remove(newVert);
		}
		for (Triangle triangle : addedTriangles) {
			triangle.getGeoset().remove(triangle);
		}
		unSplitGeosets();
//		for (Triangle tri : trisToSeparate1) {
//			Geoset geoset = tri.getGeoset();
//			for (GeosetVertex gv : tri.getVerts()) {
//				gv.addTriangle(tri);
//				if (!geoset.getVertices().contains(gv)) {
//					geoset.add(gv);
//				}
//			}
//			geoset.add(tri);
//		}


		modelStructureChangeListener.geosetsUpdated();
//		modelView.setSelectedVertices(affectedVertices);
		return this;

	}

	@Override
	public UndoAction redo() {
		for (Geoset geoset : oldGeoToNewGeo.values()) {
			model.add(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.add(geoset.getGeosetAnim());
			}
		}
//		for (Triangle tri : trisToSeparate1) {
//			Geoset geoset = tri.getGeoset();
//			for (GeosetVertex gv : tri.getVerts()) {
//				gv.removeTriangle(tri);
//				if (gv.getTriangles().isEmpty()) {
//					geoset.remove(gv);
//				}
//			}
//			geoset.removeTriangle(tri);
//		}

		splitGeosets();
		for (GeosetVertex newVert : oldToNew.values()) {
			newVert.getGeoset().add(newVert);
			for (Triangle triangle : newVert.getTriangles()) {
				newVert.getGeoset().add(triangle);
			}
		}

		modelStructureChangeListener.geosetsUpdated();
//		modelView.setSelectedVertices(affectedVertices);
		return this;
	}

	@Override
	public String actionName() {
		return "split geoset";
	}

}
