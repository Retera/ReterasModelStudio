package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class ModelView {
	private final EditableModel model;
	private RenderModel editorRenderModel;
	private final ModelViewStateNotifier modelViewStateNotifier;

	private final Set<GeosetVertex> selectedTVertices = new HashSet<>();
	private final Set<GeosetVertex> selectedVertices = new HashSet<>();
	private final Set<IdObject> selectedIdObjects = new HashSet<>();
	private final Set<Camera> selectedCameras = new HashSet<>();

	private final Set<GeosetVertex> hiddenVertices = new HashSet<>();
	private final Set<GeosetVertex> editableVertices = new HashSet<>();

	private final Set<Geoset> editableGeosets = new HashSet<>();
	private final Set<Geoset> visibleGeosets = new HashSet<>();
	private final Set<Geoset> hiddenGeosets = new HashSet<>();

	private final Set<IdObject> editableIdObjects = new HashSet<>();
	private final Set<IdObject> visibleIdObjects = new HashSet<>();
	private final Set<IdObject> hiddenIdObjects = new HashSet<>();

	private final Set<Camera> editableCameras = new HashSet<>();
	private final Set<Camera> visibleCameras = new HashSet<>();
	private final Set<Camera> hiddenCameras = new HashSet<>();

	private Geoset highlightedGeoset;
	private IdObject highlightedNode;
	private boolean vetoParticles = false;

	private boolean geosetsVisible = true;
	private boolean idObjectsVisible = false;
	private boolean camerasVisible = false;
	private boolean geosetsEditable = true;
	private boolean idObjectsEditable = true;
	private boolean camerasEditable = true;

	// Cameras got target and position. they should be posible to atleast edit separate
	// CollosionShapes got some kind of verts (or points) that might should get selected together with them...
	public ModelView(EditableModel model) {
		this.model = model;
//		editorRenderModel = new RenderModel(this.model, this);

		modelViewStateNotifier = new ModelViewStateNotifier();
		for (Geoset geoset : model.getGeosets()) {
			if (!ModelUtils.isLevelOfDetailSupported(model.getFormatVersion()) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
				visibleGeosets.add(geoset);
				editableVertices.addAll(geoset.getVertices());
			} else {
				hiddenGeosets.add(geoset);
			}
		}
		editableIdObjects.addAll(model.getIdObjects());
		visibleIdObjects.addAll(model.getIdObjects());
		editableCameras.addAll(model.getCameras());
		visibleCameras.addAll(model.getCameras());
	}

	public ModelView(EditableModel model, TimeEnvironmentImpl timeEnvironment) {
		this.model = model;
		editorRenderModel = new RenderModel(this.model, this, timeEnvironment);

		modelViewStateNotifier = new ModelViewStateNotifier();
		for (Geoset geoset : model.getGeosets()) {
			if (!ModelUtils.isLevelOfDetailSupported(model.getFormatVersion()) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
				visibleGeosets.add(geoset);
				editableVertices.addAll(geoset.getVertices());
			} else {
				hiddenGeosets.add(geoset);
			}
		}
		editableIdObjects.addAll(model.getIdObjects());
		visibleIdObjects.addAll(model.getIdObjects());
		editableCameras.addAll(model.getCameras());
		visibleCameras.addAll(model.getCameras());
	}

	public RenderModel getEditorRenderModel() {
		return editorRenderModel;
	}

	public void addStateListener(ModelViewStateListener listener) {
		modelViewStateNotifier.subscribe(listener);
	}

	public Set<Geoset> getVisibleGeosets() {
//		return visibleGeosets;
		if (geosetsVisible) return visibleGeosets;
		return Collections.emptySet();
	}

	public Set<Geoset> getEditableGeosets() {
//		return editableGeosets;
		if (geosetsVisible) return editableGeosets;
		return Collections.emptySet();
	}

	public Set<IdObject> getVisibleIdObjects() {
//		return visibleIdObjects;
		if (idObjectsVisible) return visibleIdObjects;
		return Collections.emptySet();
	}

	public Set<IdObject> getEditableIdObjects() {
//		return editableIdObjects;
		if (idObjectsVisible) return editableIdObjects;
		return Collections.emptySet();
	}

	public Set<Camera> getVisibleCameras() {
//		return visibleCameras;
		if (camerasVisible) return visibleCameras;
		return Collections.emptySet();
	}

	public Set<Camera> getEditableCameras() {
//		return editableCameras;
		if (camerasVisible) return editableCameras;
		return Collections.emptySet();
	}

	public EditableModel getModel() {
		return model;
	}

	public Geoset getHighlightedGeoset() {
		return highlightedGeoset;
	}

	public IdObject getHighlightedNode() {
		return highlightedNode;
	}

	public void makeGeosetEditable(Geoset geoset) {
		editableGeosets.add(geoset);
		visibleGeosets.add(geoset);
		hiddenGeosets.remove(geoset);
		editableVertices.addAll(geoset.getVertices());
		modelViewStateNotifier.geosetEditable(geoset);
	}

	public void makeGeosetNotEditable(Geoset geoset) {
		editableGeosets.remove(geoset);
		editableVertices.removeAll(geoset.getVertices());
		modelViewStateNotifier.geosetNotEditable(geoset);
	}

	public void makeGeosetVisible(Geoset geoset) {
		visibleGeosets.add(geoset);
		editableGeosets.add(geoset);
		hiddenGeosets.remove(geoset);
		modelViewStateNotifier.geosetVisible(geoset);
	}

	public void makeGeosetNotVisible(Geoset geoset) {
		visibleGeosets.remove(geoset);
		editableGeosets.remove(geoset);
		hiddenGeosets.add(geoset);
		modelViewStateNotifier.geosetNotVisible(geoset);
	}

	public void makeIdObjectVisible(IdObject bone) {
		visibleIdObjects.add(bone);
		editableIdObjects.add(bone);
		hiddenIdObjects.remove(bone);
		modelViewStateNotifier.idObjectVisible(bone);
	}

	public void makeIdObjectEditable(IdObject bone) {
		visibleIdObjects.add(bone);
		editableIdObjects.add(bone);
		hiddenIdObjects.remove(bone);
		modelViewStateNotifier.idObjectVisible(bone);
	}

	public void makeIdObjectNotVisible(IdObject bone) {
		editableIdObjects.remove(bone);
		visibleIdObjects.remove(bone);
		hiddenIdObjects.add(bone);
		modelViewStateNotifier.idObjectNotVisible(bone);
	}

	public void makeIdObjectNotEditable(IdObject bone) {
		editableIdObjects.remove(bone);
		modelViewStateNotifier.idObjectNotVisible(bone);
	}

	public void makeCameraVisible(Camera camera) {
		editableCameras.add(camera);
		visibleCameras.add(camera);
		hiddenCameras.remove(camera);
		modelViewStateNotifier.cameraVisible(camera);
	}

	public void makeCameraEditable(Camera camera) {
		editableCameras.add(camera);
		visibleCameras.add(camera);
		hiddenCameras.remove(camera);
		modelViewStateNotifier.cameraVisible(camera);
	}

	public void makeCameraNotVisible(Camera camera) {
		editableCameras.remove(camera);
		visibleCameras.remove(camera);
		hiddenCameras.add(camera);
		modelViewStateNotifier.cameraNotVisible(camera);
	}

	public void makeCameraNotEditable(Camera camera) {
		editableCameras.remove(camera);
		modelViewStateNotifier.cameraNotVisible(camera);
	}

	public void highlightGeoset(Geoset geoset) {
		highlightedGeoset = geoset;
		modelViewStateNotifier.highlightGeoset(geoset);
	}

	public void unhighlightGeoset(Geoset geoset) {
		if (highlightedGeoset == geoset) {
			highlightedGeoset = null;
		}
		modelViewStateNotifier.unhighlightGeoset(geoset);
	}

	public void highlightNode(IdObject node) {
		highlightedNode = node;
		modelViewStateNotifier.highlightNode(node);
	}

	public void unhighlightNode(IdObject node) {
		if (highlightedNode == node) {
			highlightedNode = null;
		}
		modelViewStateNotifier.unhighlightNode(node);

	}

	public void updateElements() {
		Set<Geoset> modelGeosets = new HashSet<>(model.getGeosets());
		editableVertices.clear();
		if (!modelGeosets.containsAll(visibleGeosets)) {
			visibleGeosets.removeIf(geoset -> !modelGeosets.contains(geoset));
		}
		if (!modelGeosets.containsAll(hiddenGeosets)) {
			hiddenGeosets.removeIf(geoset -> !modelGeosets.contains(geoset));
		}
		modelGeosets.removeAll(visibleGeosets);
		modelGeosets.removeAll(hiddenGeosets);
		visibleGeosets.addAll(modelGeosets);
		editableGeosets.addAll(modelGeosets);
		for (Geoset geoset : editableGeosets) {
			editableVertices.addAll(geoset.getVertices());
		}

		Set<IdObject> modelIdObjects = new HashSet<>(model.getIdObjects());
		if (!modelIdObjects.containsAll(visibleIdObjects)) {
			visibleIdObjects.removeIf(object -> !modelIdObjects.contains(object));
		}
		if (!modelIdObjects.containsAll(hiddenIdObjects)) {
			hiddenIdObjects.removeIf(object -> !modelIdObjects.contains(object));
		}
		modelIdObjects.removeAll(visibleIdObjects);
		modelIdObjects.removeAll(hiddenIdObjects);
		visibleIdObjects.addAll(modelIdObjects);
		editableIdObjects.addAll(modelIdObjects);

		Set<Camera> modelCameras = new HashSet<>(model.getCameras());
		if (!modelCameras.containsAll(visibleCameras)) {
			visibleCameras.removeIf(camera -> !modelCameras.contains(camera));
		}
		if (!modelCameras.containsAll(hiddenCameras)) {
			hiddenCameras.removeIf(camera -> !modelCameras.contains(camera));
		}
		modelCameras.removeAll(visibleCameras);
		modelCameras.removeAll(hiddenCameras);
		visibleCameras.addAll(modelCameras);
		editableCameras.addAll(modelCameras);
	}

	public boolean isVetoOverrideParticles() {
		return vetoParticles;
	}

	public void setVetoOverrideParticles(boolean override) {
		vetoParticles = override;
	}

	public void setGeosetsVisible(boolean visible) {
		geosetsVisible = visible;
	}

	public void setIdObjectsVisible(boolean visible) {
		idObjectsVisible = visible;
	}

	public void setCamerasVisible(boolean visible) {
		camerasVisible = visible;
	}

	public boolean isVisible(Geoset ob) {
		return visibleGeosets.contains(ob) && geosetsVisible;
	}

	public boolean isVisible(IdObject ob) {
		return visibleIdObjects.contains(ob) && idObjectsVisible;
	}

	public boolean isVisible(Camera ob) {
		return visibleCameras.contains(ob) && camerasVisible;
	}

	public boolean isEditable(Geoset ob) {
		return editableGeosets.contains(ob) && geosetsEditable && geosetsVisible;
	}

	public boolean isEditable(GeosetVertex ob) {
		return editableVertices.contains(ob) && !hiddenVertices.contains(ob) && geosetsEditable && geosetsVisible;
	}

	public boolean isEditable(IdObject ob) {
		return editableIdObjects.contains(ob) && idObjectsEditable && idObjectsVisible;
	}

	public boolean isEditable(Camera ob) {
		return editableCameras.contains(ob);
	}

	public boolean shouldRender(Geoset ob) {
		return visibleGeosets.contains(ob) && geosetsVisible;
	}

	public boolean shouldRender(IdObject ob) {
		return visibleIdObjects.contains(ob) && idObjectsVisible;
	}

	public boolean shouldRender(Camera ob) {
		return visibleCameras.contains(ob) && camerasVisible;
	}

	public boolean canSelect(Geoset ob) {
		return editableGeosets.contains(ob) && geosetsVisible && geosetsEditable;
	}

	public boolean canSelect(IdObject ob) {
		return editableIdObjects.contains(ob) && idObjectsVisible && idObjectsEditable;
	}

	public boolean canSelect(Camera ob) {
		return editableCameras.contains(ob) && camerasVisible;
	}

	public Vec3 getSelectionCenter(){
		Set<Vec3> selectedPoints = new HashSet<>();
		selectedVertices.stream().filter(editableVertices::contains).forEach(selectedPoints::add);
//		selectedVertices.stream().forEach(selectedPoints::add);
		selectedIdObjects.stream().filter(editableIdObjects::contains).forEach(o -> selectedPoints.add(o.getPivotPoint()));
		selectedCameras.stream().filter(editableCameras::contains).forEach(c -> selectedPoints.add(c.getPosition()));

		return Vec3.centerOfGroup(selectedPoints);
	}

	public Set<GeosetVertex> getSelectedVertices() {
		// ToDo not editable stuff should not be in the selection (but maybe be added back once editable again)
		return selectedVertices;
	}

	public void setSelectedVertices(Collection<GeosetVertex> geosetVertices) {
		selectedVertices.clear();
		selectedVertices.addAll(geosetVertices);
	}

	public Set<IdObject> getSelectedIdObjects() {
		return selectedIdObjects;
	}

	public void setSelectedIdObjects(Collection<IdObject> idObjects) {
		selectedIdObjects.clear();
		selectedIdObjects.addAll(idObjects);
	}

	public Set<Camera> getSelectedCameras() {
		return selectedCameras;
	}

	public void setSelectedCameras(Collection<Camera> cameras) {
		selectedCameras.clear();
		selectedCameras.addAll(cameras);
	}

	public Set<Triangle> getSelectedTriangles() {
		Set<Triangle> selTris = new HashSet<>();
		for (GeosetVertex vertex : selectedVertices) {
			for (Triangle triangle : vertex.getTriangles()) {
				if (selectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
					selTris.add(triangle);
				}
			}
		}
		return selTris;
	}

	public void addSelectedTris(Collection<Triangle> triangles) {
		for (Triangle triangle : triangles) {
			selectedVertices.addAll(Arrays.asList(triangle.getVerts()));
		}
	}

	public void setSelectedTris(Collection<Triangle> triangles) {
		selectedVertices.clear();
		for (Triangle triangle : triangles) {
			selectedVertices.addAll(Arrays.asList(triangle.getVerts()));
		}
	}

	public void removeSelectedTris(Collection<Triangle> triangles) {
		for (Triangle triangle : triangles) {
			selectedVertices.removeAll(Arrays.asList(triangle.getVerts()));
		}
	}

	public void addSelectedVertex(GeosetVertex geosetVertex) {
		selectedVertices.add(geosetVertex);
	}

	public void addSelectedVertices(Collection<GeosetVertex> geosetVertices) {
		selectedVertices.addAll(geosetVertices);
	}

	public void clearSelectedVertices() {
		selectedVertices.clear();
	}

	public void removeSelectedVertices(Collection<GeosetVertex> geosetVertices) {
		selectedVertices.removeAll(geosetVertices);
	}

	public void removeSelectedVertex(GeosetVertex geosetVertex) {
		selectedVertices.remove(geosetVertex);
	}

	public void addSelectedIdObject(IdObject idObject) {
		selectedIdObjects.add(idObject);
	}

	public void addSelectedIdObjects(Collection<IdObject> idObjects) {
		selectedIdObjects.addAll(idObjects);
	}

	public void clearSelectedIdObjects() {
		selectedIdObjects.clear();
	}

	public void removeSelectedIdObjects(Collection<IdObject> idObjects) {
		selectedIdObjects.removeAll(idObjects);
	}

	public void removeSelectedIdObject(IdObject idObject) {
		selectedIdObjects.remove(idObject);
	}

	public boolean isHidden(GeosetVertex vertex) {
		return hiddenVertices.contains(vertex);
	}

	public void addSelectedCamera(Camera camera) {
		selectedCameras.add(camera);
	}

	public void addSelectedCameras(Collection<Camera> cameras) {
		selectedCameras.addAll(cameras);
	}

	public void clearSelectedCameras() {
		selectedCameras.clear();
	}

	public void removeSelectedCameras(Collection<Camera> cameras) {
		selectedCameras.removeAll(cameras);
	}

	public void removeSelectedCamera(Camera camera) {
		selectedCameras.remove(camera);
	}

	public boolean isSelected(GeosetVertex geosetVertex) {
		return selectedVertices.contains(geosetVertex);
	}

	public boolean isSelected(IdObject idObject) {
		return selectedIdObjects.contains(idObject);
	}

	public boolean isSelected(Camera camera) {
		return selectedCameras.contains(camera);
	}

	public void invertVertSelection() {
		Set<GeosetVertex> tempVerts = new HashSet<>();
		for (Geoset geoset : editableGeosets) {
			tempVerts.addAll(geoset.getVertices());
		}
		tempVerts.removeAll(selectedVertices);
		setSelectedVertices(tempVerts);
	}

	public void invertIdObjSelection() {
		Set<IdObject> tempObjs = new HashSet<>(editableIdObjects);
		tempObjs.removeAll(selectedIdObjects);
		setSelectedIdObjects(tempObjs);
	}

	public void invertCamSelection() {
		Set<Camera> tempCams = new HashSet<>(editableCameras);
		tempCams.removeAll(selectedCameras);
		setSelectedCameras(tempCams);
	}

	public void invertSelection() {
		invertVertSelection();
		invertIdObjSelection();
		invertCamSelection();
	}

	public void selectAllVerts() {
		for (Geoset geoset : editableGeosets) {
			selectedVertices.addAll(geoset.getVertices());
		}
	}

	public void selectAllIdObjs() {
		selectedIdObjects.addAll(editableIdObjects);
	}

	public void selectAllCams() {
		selectedCameras.addAll(editableCameras);
	}

	public void selectAll() {
		for (Geoset geoset : editableGeosets) {
			selectedVertices.addAll(geoset.getVertices());
		}
		selectedIdObjects.addAll(editableIdObjects);
		selectedCameras.addAll(editableCameras);
	}

	public void addSelectedTVertex(GeosetVertex geosetVertex) {
		selectedTVertices.add(geosetVertex);
	}

	public void addSelectedTVertices(Collection<GeosetVertex> geosetVertices) {
		selectedTVertices.addAll(geosetVertices);
	}

	public void clearSelectedTVertices() {
		selectedTVertices.clear();
	}

	public void removeSelectedTVertices(Collection<GeosetVertex> geosetVertices) {
		selectedTVertices.removeAll(geosetVertices);
	}

	public boolean isTSelected(GeosetVertex geosetVertex) {
		return selectedTVertices.contains(geosetVertex);
	}

	public void selectAllTVerts() {
		for (Geoset geoset : editableGeosets) {
			selectedTVertices.addAll(geoset.getVertices());
		}
	}

	public Vec2 getTSelectionCenter(){
		Set<Vec2> selectedPoints = new HashSet<>();
		selectedTVertices.stream().filter(editableVertices::contains).forEach(v -> selectedPoints.add(v.getTVertex(0)));

		return Vec2.centerOfGroup(selectedPoints);
	}

	public Set<GeosetVertex> getSelectedTVertices() {
		// ToDo not editable stuff should not be in the selection (but maybe be added back once editable again)
		return selectedTVertices;
	}

	public void setSelectedTVertices(Collection<GeosetVertex> geosetVertices) {
		selectedTVertices.clear();
		selectedTVertices.addAll(geosetVertices);
	}
}
