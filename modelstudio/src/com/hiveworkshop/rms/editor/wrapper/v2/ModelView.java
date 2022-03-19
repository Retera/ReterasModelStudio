package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ModelView {
	private final EditableModel model;

	private final Set<GeosetVertex> selectedTVertices = new HashSet<>();
	private final Set<GeosetVertex> selectedVertices = new HashSet<>();
	private final Set<IdObject> selectedIdObjects = new HashSet<>();
	private final Set<CameraNode> selectedCameraNodes = new HashSet<>();

	private final Set<GeosetVertex> selEdTVertices = new HashSet<>();
	private final Set<GeosetVertex> selEdVertices = new HashSet<>();
	private final Set<IdObject> selEdIdObjects = new HashSet<>();
	private final Set<Camera> selEdCameras = new HashSet<>();
	private final Set<CameraNode> selEdCameraNodes = new HashSet<>();

	private final Set<GeosetVertex> hiddenVertices = new HashSet<>();
	private final Set<GeosetVertex> editableVertices = new HashSet<>();
	private final Set<GeosetVertex> notEditableVertices = new HashSet<>();

	private final Set<Geoset> editableGeosets = new HashSet<>();
	private final Set<Geoset> notEditableGeosets = new HashSet<>();
	private final Set<Geoset> visibleGeosets = new HashSet<>();
	private final Set<Geoset> hiddenGeosets = new HashSet<>();

	private final Set<IdObject> editableIdObjects = new HashSet<>();
	private final Set<IdObject> notEditableIdObjects = new HashSet<>();
	private final Set<IdObject> visibleIdObjects = new HashSet<>();
	private final Set<IdObject> hiddenIdObjects = new HashSet<>();

	private final Set<Camera> editableCameras = new HashSet<>();
	private final Set<Camera> notEditableCameras = new HashSet<>();
	private final Set<Camera> visibleCameras = new HashSet<>();
	private final Set<Camera> hiddenCameras = new HashSet<>();

	private final Set<CameraNode> editableCameraNodes = new HashSet<>();
	private final Set<CameraNode> notEditableCameraNodes = new HashSet<>();
	private final Set<CameraNode> visibleCameraNodes = new HashSet<>();
	private final Set<CameraNode> hiddenCameraNodes = new HashSet<>();

	private Geoset highlightedGeoset;
	private IdObject highlightedNode;
	private Camera highlightedCamera;
	private boolean vetoParticles = false;

	private boolean geosetsVisible = true;
	private boolean idObjectsVisible = true;
	private boolean camerasVisible = true;
	private boolean geosetsEditable = true;
	private boolean idObjectsEditable = true;
	private boolean camerasEditable = true;

	// Cameras got target and position. they should be posible to atleast edit separate
	// CollosionShapes got some kind of verts (or points) that might should get selected together with them...
	public ModelView(EditableModel model) {
		this.model = model;

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

		editableCameras.forEach(c -> {
			editableCameraNodes.add(c.getSourceNode());
			editableCameraNodes.add(c.getTargetNode());
		});
		visibleCameras.forEach(c -> {
			visibleCameraNodes.add(c.getSourceNode());
			visibleCameraNodes.add(c.getTargetNode());
		});
	}

	public Set<Geoset> getVisibleGeosets() {
//		return visibleGeosets;
		if (geosetsVisible) return visibleGeosets;
		return Collections.emptySet();
	}

	public Set<Geoset> getEditableGeosets() {
//		return editableGeosets;
		if (geosetsVisible && geosetsEditable) return editableGeosets;
		return Collections.emptySet();
	}

	public Set<IdObject> getVisibleIdObjects() {
//		return visibleIdObjects;
		if (idObjectsVisible) return visibleIdObjects;
		return Collections.emptySet();
	}

	public Set<IdObject> getEditableIdObjects() {
//		return editableIdObjects;
		if (idObjectsVisible && idObjectsEditable) return editableIdObjects;
		return Collections.emptySet();
	}

	public Set<Camera> getVisibleCameras() {
//		return visibleCameras;
		if (camerasVisible) return visibleCameras;
		return Collections.emptySet();
	}

	public Set<Camera> getEditableCameras() {
//		return editableCameras;
		if (camerasVisible && camerasEditable) return editableCameras;
		return Collections.emptySet();
	}

	public Set<CameraNode> getVisibleCameraNodes() {
//		return visibleCameras;
		if (camerasVisible) return visibleCameraNodes;
		return Collections.emptySet();
	}

	public Set<CameraNode> getEditableCameraNodes() {
//		return editableCameras;
		if (camerasVisible && camerasEditable) return editableCameraNodes;
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

	public Camera getHighlightedCamera() {
		return highlightedCamera;
	}

	public Set<GeosetVertex> getHiddenVertices(){
		return hiddenVertices;
	}

	public void makeGeosetEditable(Geoset geoset) {
		editableGeosets.add(geoset);
		notEditableGeosets.remove(geoset);
//		visibleGeosets.add(geoset);
//		hiddenGeosets.remove(geoset);
//		hiddenVertices.removeAll(geoset.getVertices());
		editableVertices.addAll(geoset.getVertices());
		notEditableVertices.removeAll(geoset.getVertices());
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeGeosetNotEditable(Geoset geoset) {
		editableGeosets.remove(geoset);
		notEditableGeosets.add(geoset);
		editableVertices.removeAll(geoset.getVertices());
		notEditableVertices.addAll(geoset.getVertices());
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeGeosetVisible(Geoset geoset) {
		visibleGeosets.add(geoset);
		hiddenGeosets.remove(geoset);
		hiddenVertices.removeAll(geoset.getVertices());
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeGeosetNotVisible(Geoset geoset) {
		visibleGeosets.remove(geoset);
//		editableGeosets.remove(geoset);
		hiddenGeosets.add(geoset);
		hiddenVertices.addAll(geoset.getVertices());
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeIdObjectVisible(IdObject bone) {
		visibleIdObjects.add(bone);
		hiddenIdObjects.remove(bone);
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeIdObjectEditable(IdObject bone) {
//		visibleIdObjects.add(bone);
		editableIdObjects.add(bone);
		hiddenIdObjects.remove(bone);
		notEditableIdObjects.remove(bone);
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeIdObjectNotVisible(IdObject bone) {
//		editableIdObjects.remove(bone);
		visibleIdObjects.remove(bone);
		hiddenIdObjects.add(bone);
//		notEditableIdObjects.add(bone);
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeIdObjectNotEditable(IdObject bone) {
		editableIdObjects.remove(bone);
		notEditableIdObjects.add(bone);
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeCameraVisible(Camera camera) {
		visibleCameras.add(camera);
		hiddenCameras.remove(camera);
		visibleCameraNodes.add(camera.getSourceNode());
		visibleCameraNodes.add(camera.getTargetNode());
		hiddenCameraNodes.remove(camera.getTargetNode());
		hiddenCameraNodes.remove(camera.getSourceNode());
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeCameraEditable(Camera camera) {
		editableCameras.add(camera);
		notEditableCameras.remove(camera);
		editableCameraNodes.add(camera.getSourceNode());
		editableCameraNodes.add(camera.getTargetNode());
		notEditableCameraNodes.remove(camera.getTargetNode());
		notEditableCameraNodes.remove(camera.getSourceNode());
		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeCameraNotVisible(Camera camera) {
		visibleCameras.remove(camera);
		hiddenCameras.add(camera);

		visibleCameraNodes.remove(camera.getSourceNode());
		visibleCameraNodes.remove(camera.getTargetNode());
		hiddenCameraNodes.add(camera.getTargetNode());
		hiddenCameraNodes.add(camera.getSourceNode());

		ProgramGlobals.getMainPanel().repaint();
	}

	public void makeCameraNotEditable(Camera camera) {
		editableCameras.remove(camera);
		notEditableCameras.add(camera);
		editableCameraNodes.remove(camera.getSourceNode());
		editableCameraNodes.remove(camera.getTargetNode());
		notEditableCameraNodes.add(camera.getTargetNode());
		notEditableCameraNodes.add(camera.getSourceNode());
		ProgramGlobals.getMainPanel().repaint();
	}

	public void highlightGeoset(Geoset geoset) {
		highlightedGeoset = geoset;
		ProgramGlobals.getMainPanel().repaint();
	}

	public void unhighlightGeoset(Geoset geoset) {
		if (highlightedGeoset == geoset) {
			highlightedGeoset = null;
		}
		ProgramGlobals.getMainPanel().repaint();
	}

	public void highlightNode(IdObject node) {
		highlightedNode = node;
		ProgramGlobals.getMainPanel().repaint();
	}

	public void unhighlightNode(IdObject node) {
		if (highlightedNode == node) {
			highlightedNode = null;
		}
		ProgramGlobals.getMainPanel().repaint();
	}

	public void highlightCamera(Camera camera) {
		highlightedCamera = camera;
		ProgramGlobals.getMainPanel().repaint();
	}

	public void unhighlightCamera(Camera camera) {
		if (highlightedCamera == camera) {
			highlightedCamera = null;
		}
		ProgramGlobals.getMainPanel().repaint();
	}

	public void updateElements() {
		Set<Geoset> modelGeosets = new HashSet<>(model.getGeosets());

		Set<Geoset> geosetsToRemove = Stream.of(visibleGeosets, hiddenGeosets, notEditableGeosets, editableGeosets)
				.flatMap(Collection::stream)
				.filter(g -> !modelGeosets.contains(g))
				.collect(Collectors.toSet());
		visibleGeosets.removeAll(geosetsToRemove);
		hiddenGeosets.removeAll(geosetsToRemove);
		notEditableGeosets.removeAll(geosetsToRemove);
		editableGeosets.removeAll(geosetsToRemove);

		for (Geoset geoset : geosetsToRemove) {
			hiddenVertices.removeAll(geoset.getVertices());
			editableVertices.removeAll(geoset.getVertices());
			notEditableVertices.removeAll(geoset.getVertices());
			selectedVertices.removeAll(geoset.getVertices());
		}

		for (Geoset geoset : modelGeosets) {
			if (!visibleGeosets.contains(geoset) && !hiddenGeosets.contains(geoset) && !notEditableGeosets.contains(geoset) && !editableGeosets.contains(geoset)){
				visibleGeosets.add(geoset);
				editableGeosets.add(geoset);
				editableVertices.addAll(geoset.getVertices());
			} else {
				geoset.getVertices().stream()
						.filter(v -> !editableVertices.contains(v) || !notEditableVertices.contains(v))
						.forEach(editableVertices::add);
			}
		}

		hiddenVertices.removeIf(g -> !g.getGeoset().contains(g));
		editableVertices.removeIf(g -> !g.getGeoset().contains(g));
		notEditableVertices.removeIf(g -> !g.getGeoset().contains(g));
		selectedVertices.removeIf(g -> !g.getGeoset().contains(g));


//		editableVertices.clear();
//		hiddenVertices.clear();
//		notEditableVertices.clear();
//		for (Geoset geoset : editableGeosets) {
//			editableVertices.addAll(geoset.getVertices());
//		}
//		for (Geoset geoset : hiddenGeosets) {
//			hiddenVertices.addAll(geoset.getVertices());
//		}
//		for (Geoset geoset : notEditableGeosets) {
//			notEditableVertices.addAll(geoset.getVertices());
//		}
//
//		selectedVertices.removeIf(v -> !editableVertices.contains(v) && !notEditableVertices.contains(v));

		Set<IdObject> modelIdObjects = new HashSet<>(model.getIdObjects());
		visibleIdObjects.removeIf(object -> !modelIdObjects.contains(object));
		hiddenIdObjects.removeIf(object -> !modelIdObjects.contains(object));
		editableIdObjects.removeIf(object -> !modelIdObjects.contains(object));
		notEditableIdObjects.removeIf(object -> !modelIdObjects.contains(object));
		selectedIdObjects.removeIf(object -> !modelIdObjects.contains(object));


		for (IdObject object : modelIdObjects) {
			if (!visibleIdObjects.contains(object)
					&& !hiddenIdObjects.contains(object)
					&& !editableIdObjects.contains(object)
					&& !notEditableIdObjects.contains(object)){
				visibleIdObjects.add(object);
				editableIdObjects.add(object);
			}
		}

//		modelIdObjects.removeAll(visibleIdObjects);
//		modelIdObjects.removeAll(hiddenIdObjects);
//		visibleIdObjects.addAll(modelIdObjects);
//		editableIdObjects.addAll(modelIdObjects);

		Set<Camera> modelCameras = new HashSet<>(model.getCameras());
		visibleCameras.removeIf(camera -> !modelCameras.contains(camera));
		hiddenCameras.removeIf(camera -> !modelCameras.contains(camera));
		editableCameras.removeIf(camera -> !modelCameras.contains(camera));
		notEditableCameras.removeIf(camera -> !modelCameras.contains(camera));


		visibleCameraNodes.removeIf(node -> !modelCameras.contains(node.getParent()));
		hiddenCameraNodes.removeIf(node -> !modelCameras.contains(node.getParent()));
		editableCameraNodes.removeIf(node -> !modelCameras.contains(node.getParent()));
		notEditableCameraNodes.removeIf(node -> !modelCameras.contains(node.getParent()));
		selectedCameraNodes.removeIf(node -> !modelCameras.contains(node.getParent()));

		for (Camera camera : modelCameras) {
			if (!visibleCameras.contains(camera)
					&& !hiddenCameras.contains(camera)
					&& !editableCameras.contains(camera)
					&& !notEditableCameras.contains(camera)){
				visibleCameras.add(camera);
				editableCameras.add(camera);
				editableCameraNodes.add(camera.getSourceNode());
				editableCameraNodes.add(camera.getTargetNode());
				visibleCameraNodes.add(camera.getSourceNode());
				visibleCameraNodes.add(camera.getTargetNode());
			}
		}
//		modelCameras.removeAll(visibleCameras);
//		modelCameras.removeAll(hiddenCameras);
//		visibleCameras.addAll(modelCameras);
//		editableCameras.addAll(modelCameras);

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

	public boolean isGeosetsVisible() {
		return geosetsVisible;
	}

	public void setGeosetsEditable(boolean editable) {
		geosetsEditable = editable;
	}

	public boolean isGeosetsEditable() {
		return geosetsEditable;
	}

	public void setIdObjectsVisible(boolean visible) {
		idObjectsVisible = visible;
	}

	public boolean isIdObjectsVisible() {
		return idObjectsVisible;
	}

	public void setIdObjectsEditable(boolean editable) {
		idObjectsEditable = editable;
	}

	public boolean isIdObjectsEditable() {
		return idObjectsEditable;
	}

	public void setCamerasVisible(boolean visible) {
		camerasVisible = visible;
	}

	public boolean isCamerasVisible() {
		return camerasVisible;
	}

	public void setCamerasEditable(boolean editable) {
		camerasEditable = editable;
	}

	public boolean isCamerasEditable() {
		return camerasEditable;
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
		return geosetsEditable && geosetsVisible && editableGeosets.contains(ob) && visibleGeosets.contains(ob);
	}

	public boolean isEditable(GeosetVertex ob) {
		return geosetsEditable && geosetsVisible && editableVertices.contains(ob) && !hiddenVertices.contains(ob);
	}

	public boolean isEditable(Triangle ob) {
		return geosetsEditable && geosetsVisible && editableVertices.containsAll(Arrays.asList(ob.getVerts())) && !hiddenVertices.contains(ob.get(0)) && !hiddenVertices.contains(ob.get(1)) && !hiddenVertices.contains(ob.get(2));
	}

	public boolean isEditable(IdObject ob) {
		return idObjectsEditable && idObjectsVisible && editableIdObjects.contains(ob) && visibleIdObjects.contains(ob);
	}

	public boolean isEditable(Camera ob) {
		return camerasEditable && camerasVisible && editableCameras.contains(ob) && visibleCameras.contains(ob);
	}

	public boolean isEditable(CameraNode ob) {
		return camerasEditable && camerasVisible && editableCameraNodes.contains(ob) && visibleCameraNodes.contains(ob);
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
		return idObjectsVisible && idObjectsEditable && editableIdObjects.contains(ob) && visibleIdObjects.contains(ob);
	}

	public boolean canSelect(Camera ob) {
		return camerasEditable && camerasVisible && editableCameras.contains(ob) && visibleCameras.contains(ob);
	}

	public boolean canSelect(CameraNode ob) {
		return camerasEditable && camerasVisible && editableCameraNodes.contains(ob) && visibleCameraNodes.contains(ob);
	}

	public Vec3 getSelectionCenter() {
		Set<Vec3> selectedPoints = new HashSet<>();
		selectedVertices.stream().filter(editableVertices::contains).filter(v -> !hiddenVertices.contains(v)).forEach(selectedPoints::add);
//		selectedVertices.stream().forEach(selectedPoints::add);
		selectedIdObjects.stream().filter(editableIdObjects::contains).filter(visibleIdObjects::contains).forEach(o -> selectedPoints.add(o.getPivotPoint()));
		selectedCameraNodes.stream().filter(editableCameraNodes::contains).filter(visibleCameraNodes::contains).forEach(n -> selectedPoints.add(n.getPosition()));

		return Vec3.centerOfGroup(selectedPoints);
	}

	public Set<GeosetVertex> getSelectedVertices() {
		// ToDo not editable stuff should not be in the selection (but maybe be added back once editable again)
//		return selectedVertices;
		if (!geosetsEditable || !geosetsVisible) return Collections.emptySet();
		selEdVertices.clear();
		selEdVertices.addAll(selectedVertices);
		selEdVertices.removeAll(hiddenVertices);
		selEdVertices.removeAll(notEditableVertices);
		return selEdVertices;
	}

	public void setSelectedVertices(Collection<GeosetVertex> geosetVertices) {
		selectedVertices.clear();
		selectedVertices.addAll(geosetVertices);
	}

	public Set<IdObject> getSelectedIdObjects() {
//		return selectedIdObjects;
		if (!idObjectsEditable || !idObjectsVisible) return Collections.emptySet();
		selEdIdObjects.clear();
		selEdIdObjects.addAll(selectedIdObjects);
		selEdIdObjects.removeAll(hiddenIdObjects);
		selEdIdObjects.removeAll(notEditableIdObjects);
		return selEdIdObjects;
	}

	public void setSelectedIdObjects(Collection<IdObject> idObjects) {
		selectedIdObjects.clear();
		selectedIdObjects.addAll(idObjects);
	}

	public Set<Camera> getSelectedCameras() {
//		return selectedCameras;
		if (!camerasEditable || !camerasVisible) return Collections.emptySet();
		selEdCameras.clear();
		for (CameraNode node : selectedCameraNodes){
			Camera camera = node.getParent();
			if(editableCameras.contains(camera) && visibleCameras.contains(camera)){
				selEdCameras.add(camera);
			}
		}
		return selEdCameras;
	}

	public void setSelectedCameras(Collection<Camera> cameras) {
		selectedCameraNodes.clear();
		cameras.forEach(c -> {
			selectedCameraNodes.add(c.getSourceNode());
			selectedCameraNodes.add(c.getTargetNode());
		});
	}

	public Set<CameraNode> getSelectedCameraNodes() {
//		return selectedCameras;
		if (!camerasEditable || !camerasVisible) return Collections.emptySet();
		selEdCameraNodes.clear();
		selEdCameraNodes.addAll(selectedCameraNodes);
		selEdCameraNodes.removeAll(hiddenCameraNodes);
		selEdCameraNodes.removeAll(notEditableCameraNodes);
		return selEdCameraNodes;
	}

	public void setSelectedCameraNodes(Collection<CameraNode> cameraNodes) {
		selectedCameraNodes.clear();
		selectedCameraNodes.addAll(cameraNodes);
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
		return hiddenVertices.contains(vertex) || hiddenGeosets.contains(vertex.getGeoset());
	}

	public void addSelectedCamera(Camera camera) {
		selectedCameraNodes.add(camera.getSourceNode());
		selectedCameraNodes.add(camera.getTargetNode());
	}

	public void addSelectedCameras(Collection<Camera> cameras) {
		cameras.forEach(c -> {
			selectedCameraNodes.add(c.getSourceNode());
			selectedCameraNodes.add(c.getTargetNode());
		});
	}

	public void clearSelectedCameras() {
		selectedCameraNodes.clear();
	}

	public void removeSelectedCameras(Collection<Camera> cameras) {
		cameras.forEach(c -> {
			selectedCameraNodes.remove(c.getSourceNode());
			selectedCameraNodes.remove(c.getTargetNode());
		});
	}

	public void removeSelectedCamera(Camera camera) {
		selectedCameraNodes.remove(camera.getSourceNode());
		selectedCameraNodes.remove(camera.getTargetNode());
	}

	public void addSelectedCameraNode(CameraNode camera) {
		selectedCameraNodes.add(camera);
	}

	public void addSelectedCameraNodes(Collection<CameraNode> cameras) {
		selectedCameraNodes.addAll(cameras);
	}

	public void clearSelectedCameraNodes() {
		selectedCameraNodes.clear();
	}

	public void removeSelectedCameraNodes(Collection<CameraNode> cameras) {
		selectedCameraNodes.removeAll(cameras);
	}

	public void removeSelectedCameraNode(CameraNode camera) {
		selectedCameraNodes.remove(camera);
	}

	public boolean isSelected(GeosetVertex geosetVertex) {
		return selectedVertices.contains(geosetVertex);
	}

	public boolean isSelected(IdObject idObject) {
		return selectedIdObjects.contains(idObject);
	}

	public boolean isSelected(Camera camera) {
		return selectedCameraNodes.contains(camera.getSourceNode()) || selectedCameraNodes.contains(camera.getTargetNode());
	}

	public boolean isSelected(CameraNode node) {
		return selectedCameraNodes.contains(node);
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
		Set<CameraNode> tempCamNodes = new HashSet<>(editableCameraNodes);
		tempCamNodes.removeAll(selectedCameraNodes);
		setSelectedCameraNodes(tempCamNodes);
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
		selectedCameraNodes.addAll(editableCameraNodes);
	}
	public void selectAllCamNodes() {
		selectedCameraNodes.addAll(editableCameraNodes);
	}

	public void selectAll() {
		if (geosetsVisible && geosetsEditable) {
			for (Geoset geoset : editableGeosets) {
				if (isEditable(geoset)) {
					selectedVertices.addAll(geoset.getVertices());
					selectedVertices.removeIf(this::isHidden);
				}
			}
		}
		if (idObjectsVisible && idObjectsEditable) {
			selectedIdObjects.addAll(editableIdObjects);
		}
		if (camerasVisible && camerasEditable) {
			selectedCameraNodes.addAll(editableCameraNodes);
		}
	}

	public void addSelectedTVertex(GeosetVertex geosetVertex) {
		selectedTVertices.add(geosetVertex);
	}

	public void addSelectedTVertices(Collection<GeosetVertex> geosetVertices) {
		selectedTVertices.addAll(geosetVertices);
	}

	public void hideVertices(Collection<GeosetVertex> geosetVertices) {
		hiddenVertices.addAll(geosetVertices);
	}
	public void showVertices(Collection<GeosetVertex> geosetVertices) {
		hiddenVertices.removeAll(geosetVertices);
	}

	public void unHideAllVertices() {
		hiddenVertices.clear();
		for (Geoset geoset : hiddenGeosets) {
			hiddenVertices.addAll(geoset.getVertices());
		}
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

	public <T> boolean isInEditable(T obj) {
		if (obj instanceof GeosetVertex) {
			return editableVertices.contains(obj);
		} else if (obj instanceof Geoset) {
			return editableGeosets.contains(obj);
		} else if (obj instanceof IdObject) {
			return editableIdObjects.contains(obj);
		} else if (obj instanceof Camera) {
			return editableCameras.contains(obj);
		} else if (obj instanceof CameraNode) {
			return editableCameraNodes.contains(obj);
		}
		return false;
	}

	public <T> boolean isInVisible(T obj) {
		if (obj instanceof GeosetVertex) {
//			System.out.println("GeosetVertex inVissible:" + hiddenVertices.contains(obj));
			return !hiddenVertices.contains(obj);
		} else if (obj instanceof Geoset) {
//			System.out.println("Geoset inVissible:" + visibleGeosets.contains(obj));
			return visibleGeosets.contains(obj);
		} else if (obj instanceof IdObject) {
//			System.out.println("IdObject inVissible:" + visibleIdObjects.contains(obj));
			return visibleIdObjects.contains(obj);
		} else if (obj instanceof Camera) {
//			System.out.println("Camera inVissible:" + visibleCameras.contains(obj));
			return visibleCameras.contains(obj);
		} else if (obj instanceof CameraNode) {
//			System.out.println("Camera inVissible:" + visibleCameras.contains(obj));
			return visibleCameraNodes.contains(obj);
		}
		return false;
	}

	public <T> ModelView makeVisible(T obj, boolean b) {
		System.out.println("ModelView#makeVisible: " + b);
		if (b) {
			if (obj instanceof Geoset) {
				System.out.println("Geoset Visible!");
				makeGeosetVisible((Geoset) obj);
			} else if (obj instanceof IdObject) {
				System.out.println("IdObject Visible!");
				makeIdObjectVisible((IdObject) obj);
			} else if (obj instanceof Camera) {
				System.out.println("Camera Visible!");
				makeCameraVisible((Camera) obj);
			}
		} else {
			if (obj instanceof Geoset) {
				System.out.println("Geoset NotVisible!");
				makeGeosetNotVisible((Geoset) obj);
			} else if (obj instanceof IdObject) {
				System.out.println("IdObject NotVisible!");
				makeIdObjectNotVisible((IdObject) obj);
			} else if (obj instanceof Camera) {
				System.out.println("Camera NotVisible!");
				makeCameraNotVisible((Camera) obj);
			}
		}
		return this;
	}

	public <T> ModelView makeEditable(T obj, boolean b) {
		if (b) {
			if (obj instanceof Geoset) {
				System.out.println("Geoset Editable!");
				makeGeosetEditable((Geoset) obj);
			} else if (obj instanceof IdObject) {
				System.out.println("IdObject Editable!");
				makeIdObjectEditable((IdObject) obj);
			} else if (obj instanceof Camera) {
				System.out.println("Camera Editable!");
				makeCameraEditable((Camera) obj);
			}
		} else {
			if (obj instanceof Geoset) {
				System.out.println("Geoset NotEditable!");
				makeGeosetNotEditable((Geoset) obj);
			} else if (obj instanceof IdObject) {
				System.out.println("IdObject NotEditable!");
				makeIdObjectNotEditable((IdObject) obj);
			} else if (obj instanceof Camera) {
				System.out.println("Camera NotEditable!");
				makeCameraNotEditable((Camera) obj);
			}
		}
		return this;
	}

	public <T> ModelView higthlight(T obj) {
		if (obj instanceof Geoset) {
//			System.out.println("Geoset Higlighted!");
			highlightedGeoset = (Geoset) obj;
			highlightedNode = null;
			highlightedCamera = null;
		} else if (obj instanceof IdObject) {
//			System.out.println("IdObject Higlighted!");
			highlightedGeoset = null;
			highlightedNode = (IdObject) obj;
			highlightedCamera = null;
		} else if (obj instanceof Camera) {
//			System.out.println("Camera (not) Higlighted!");
			highlightedGeoset = null;
			highlightedNode = null;
			highlightedCamera = (Camera) obj;
		} else {
			highlightedGeoset = null;
			highlightedNode = null;
			highlightedCamera = null;
		}
		ProgramGlobals.getMainPanel().repaint();
		return this;
	}

	public boolean sameSelection(Collection<GeosetVertex> verts, Collection<IdObject> objs, Collection<CameraNode> cams) {
		return (selectedVertices.size() == verts.size() && selectedVertices.containsAll(verts)
				&& selectedIdObjects.size() == objs.size() && selectedIdObjects.containsAll(objs)
				&& selectedCameraNodes.size() == cams.size() && selectedCameraNodes.containsAll(cams));
	}

	public boolean isEmpty() {
		return selectedVertices.isEmpty() && selectedIdObjects.isEmpty() && selectedCameraNodes.isEmpty();
	}
}
