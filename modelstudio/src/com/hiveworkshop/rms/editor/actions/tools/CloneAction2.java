package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public final class CloneAction2 implements UndoAction {
	private final ModelView modelView;
	private final ModelStructureChangeListener changeListener;
	private final Set<GeosetVertex> newVertices = new HashSet<>();
	private final Set<Triangle> newTriangles = new HashSet<>();
	private final Set<IdObject> newIdObjects = new HashSet<>();
	private final Set<Camera> newCameras = new HashSet<>();
	private final Set<GeosetVertex> selectedVertices = new HashSet<>();
	private final Set<IdObject> selectedIdObjects = new HashSet<>();
	private final Set<Camera> selectedCameras = new HashSet<>();
	private final Map<IdObject, IdObject> oldToNewObjMap = new HashMap<>();
	private final Map<GeosetVertex, GeosetVertex> oldToNewVertMap = new HashMap<>();
	private final Map<Geoset, Geoset> oldToNewGeosetsMap = new HashMap<>();
	private final boolean vertsToNewGeoset = false;

	public CloneAction2(ModelView modelView,
	                    ModelStructureChangeListener changeListener,
	                    Collection<GeosetVertex> vertices,
	                    Collection<IdObject> idObjects,
	                    Collection<Camera> cameras) {
		this.modelView = modelView;
		this.changeListener = changeListener;

		selectedVertices.addAll(vertices);
		selectedIdObjects.addAll(idObjects);
		selectedCameras.addAll(cameras);

		for (IdObject idObject : selectedIdObjects) {
			IdObject copy = idObject.copy();
			copy.setName(getCopyName(copy.getName()));
			oldToNewObjMap.put(idObject, copy);
			newIdObjects.add(copy);
		}
		for (IdObject idObject : newIdObjects) {
			idObject.setParent(oldToNewObjMap.get(idObject.getParent()));
		}

		for (GeosetVertex vertex : selectedVertices) {
			GeosetVertex newVert = vertex.deepCopy();
			newVertices.add(newVert);
			newVert.clearTriangles();
			oldToNewVertMap.put(vertex, newVert);
			if (vertsToNewGeoset) {
				Geoset geoset = oldToNewGeosetsMap.computeIfAbsent(vertex.getGeoset(), k -> vertex.getGeoset().emptyCopy());
				newVert.setGeoset(geoset);
				geoset.add(newVert);
			}
		}
		for (GeosetVertex newVert : newVertices) {
			newVert.replaceBones(oldToNewObjMap, false);
		}

		Set<Triangle> selectedTriangles = new HashSet<>();
		for (GeosetVertex vertex : selectedVertices) {
			for (Triangle triangle : vertex.getTriangles()) {
				if (selectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
					selectedTriangles.add(triangle);
				}
			}
		}
		for(Triangle triangle : selectedTriangles){
			Triangle newTriangle = new Triangle(
					oldToNewVertMap.get(triangle.get(0)),
					oldToNewVertMap.get(triangle.get(1)),
					oldToNewVertMap.get(triangle.get(2)));
			newTriangles.add(newTriangle);
			if(vertsToNewGeoset){
				Geoset geoset = oldToNewGeosetsMap.get(triangle.get(0).getGeoset());
				newTriangle.setGeoset(geoset);
				geoset.add(newTriangle);
			} else {
				newTriangle.setGeoset(oldToNewVertMap.get(triangle.get(0)).getGeoset());
			}
		}

		for (Camera camera : selectedCameras) {
			newCameras.add(camera.deepCopy());
		}
	}

	private String getCopyName(String copyName) {
		String name = copyName + " copy";
		if (modelView.getModel().getObject(name) != null) {
			for (int i = 2; i < 100; i++) {
				if (modelView.getModel().getObject(name + i) == null) {
					return name + i;
				}
			}
		}
		return name;
	}

	@Override
	public UndoAction undo() {
		EditableModel model = modelView.getModel();
		if(vertsToNewGeoset){
			for(Geoset geoset : oldToNewGeosetsMap.values()){
				model.remove(geoset);
				if(geoset.getGeosetAnim() != null){
					model.remove(geoset.getGeosetAnim());
				}
			}
		} else {
			for (GeosetVertex gv : newVertices) {
				gv.getGeoset().remove(gv);
			}
			for (Triangle tri : newTriangles) {
				tri.getGeoset().remove(tri);
			}
		}
		for (IdObject b : newIdObjects) {
			model.remove(b);
		}
		for (Camera camera : newCameras) {
			model.remove(camera);
		}
		modelView.setSelectedVertices(selectedVertices);
		modelView.setSelectedIdObjects(selectedIdObjects);
		modelView.setSelectedCameras(selectedCameras);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		EditableModel model = modelView.getModel();
		if(vertsToNewGeoset){
			for(Geoset geoset : oldToNewGeosetsMap.values()){
				model.add(geoset);
				if(geoset.getGeosetAnim() != null){
					model.add(geoset.getGeosetAnim());
				}
			}
		} else {
			for (GeosetVertex gv : newVertices) {
				gv.getGeoset().add(gv);
			}
			for (Triangle tri : newTriangles) {
				tri.getGeoset().add(tri);
			}
		}
		for (IdObject b : newIdObjects) {
			model.add(b);
		}
		for (Camera camera : newCameras) {
			model.add(camera);
		}
		modelView.setSelectedVertices(newVertices);
		modelView.setSelectedIdObjects(newIdObjects);
		modelView.setSelectedCameras(newCameras);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "clone";
	}

}
