package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

import java.util.HashSet;
import java.util.Set;

public final class ModelView {
	private final EditableModel model;
	private RenderModel editorRenderModel;
	private final ModelViewStateNotifier modelViewStateNotifier;
	private final Set<Geoset> editableGeosets;
	private final Set<Geoset> visibleGeosets;
	private final Set<IdObject> editableIdObjects;
	private final Set<Camera> editableCameras;
	private Geoset highlightedGeoset;
	private IdObject highlightedNode;
	private boolean vetoParticles = false;

	public ModelView(EditableModel model) {
		this.model = model;
//		editorRenderModel = new RenderModel(this.model, this);

		modelViewStateNotifier = new ModelViewStateNotifier();
		editableGeosets = new HashSet<>();
		for (Geoset geoset : model.getGeosets()) {
			if (!ModelUtils.isLevelOfDetailSupported(model.getFormatVersion()) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
			}
		}
		visibleGeosets = new HashSet<>();
		editableIdObjects = new HashSet<>();
		editableCameras = new HashSet<>();
	}

	public ModelView(EditableModel model, TimeEnvironmentImpl timeEnvironment) {
		this.model = model;
		editorRenderModel = new RenderModel(this.model, this, timeEnvironment);

		modelViewStateNotifier = new ModelViewStateNotifier();
		editableGeosets = new HashSet<>();
		for (Geoset geoset : model.getGeosets()) {
			if (!ModelUtils.isLevelOfDetailSupported(model.getFormatVersion()) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
			}
		}
		visibleGeosets = new HashSet<>();
		editableIdObjects = new HashSet<>();
		editableCameras = new HashSet<>();
	}

	public RenderModel getEditorRenderModel() {
		return editorRenderModel;
	}

	public void addStateListener(ModelViewStateListener listener) {
		modelViewStateNotifier.subscribe(listener);
	}

	public Set<Geoset> getVisibleGeosets() {
		return visibleGeosets;
	}

	public Set<Geoset> getEditableGeosets() {
		return editableGeosets;
	}

	public Set<IdObject> getEditableIdObjects() {
		return editableIdObjects;
	}

	public Set<Camera> getEditableCameras() {
		return editableCameras;
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
		modelViewStateNotifier.geosetEditable(geoset);
	}

	public void makeGeosetNotEditable(Geoset geoset) {
		editableGeosets.remove(geoset);
		modelViewStateNotifier.geosetNotEditable(geoset);
	}

	public void makeGeosetVisible(Geoset geoset) {
		visibleGeosets.add(geoset);
		modelViewStateNotifier.geosetVisible(geoset);
	}

	public void makeGeosetNotVisible(Geoset geoset) {
		visibleGeosets.remove(geoset);
		modelViewStateNotifier.geosetNotVisible(geoset);
	}

	public void makeIdObjectVisible(IdObject bone) {
		editableIdObjects.add(bone);
		modelViewStateNotifier.idObjectVisible(bone);
	}

	public void makeIdObjectNotVisible(IdObject bone) {
		editableIdObjects.remove(bone);
		modelViewStateNotifier.idObjectNotVisible(bone);
	}

	public void makeCameraVisible(Camera camera) {
		editableCameras.add(camera);
		modelViewStateNotifier.cameraVisible(camera);
	}

	public void makeCameraNotVisible(Camera camera) {
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

	public boolean isVetoOverrideParticles() {
		return vetoParticles;
	}

	public void setVetoOverrideParticles(boolean override) {
		vetoParticles = override;
	}
}
