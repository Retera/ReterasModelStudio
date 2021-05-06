package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ModelView {
	private final EditableModel model;
	private RenderModel editorRenderModel;
	private final ModelViewStateNotifier modelViewStateNotifier;
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

	public ModelView(EditableModel model) {
		this.model = model;
//		editorRenderModel = new RenderModel(this.model, this);

		modelViewStateNotifier = new ModelViewStateNotifier();
		for (Geoset geoset : model.getGeosets()) {
			if (!ModelUtils.isLevelOfDetailSupported(model.getFormatVersion()) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
				visibleGeosets.add(geoset);
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
		modelViewStateNotifier.geosetEditable(geoset);
	}

	public void makeGeosetNotEditable(Geoset geoset) {
		editableGeosets.remove(geoset);
		modelViewStateNotifier.geosetNotEditable(geoset);
	}

	public void makeGeosetVisible(Geoset geoset) {
		visibleGeosets.add(geoset);
//		editableGeosets.add(geoset);
		hiddenGeosets.remove(geoset);
		modelViewStateNotifier.geosetVisible(geoset);
	}

	public void makeGeosetNotVisible(Geoset geoset) {
		visibleGeosets.remove(geoset);
//		editableGeosets.remove(geoset);
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
		return visibleGeosets.contains(ob);
	}

	public boolean isVisible(IdObject ob) {
		return visibleIdObjects.contains(ob);
	}

	public boolean isVisible(Camera ob) {
		return visibleCameras.contains(ob);
	}

	public boolean isEditable(Geoset ob) {
		return editableGeosets.contains(ob);
	}

	public boolean isEditable(IdObject ob) {
		return editableIdObjects.contains(ob);
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
		return editableGeosets.contains(ob) && geosetsVisible;
	}

	public boolean canSelect(IdObject ob) {
		return editableIdObjects.contains(ob) && idObjectsVisible;
	}

	public boolean canSelect(Camera ob) {
		return editableCameras.contains(ob) && camerasVisible;
	}
}
