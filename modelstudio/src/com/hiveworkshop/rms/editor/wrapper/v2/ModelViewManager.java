package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.MeshVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.render.RenderByViewMeshRenderer;
import com.hiveworkshop.rms.editor.wrapper.v2.render.RenderByViewModelRenderer;

import java.util.HashSet;
import java.util.Set;

public final class ModelViewManager implements ModelView {
	private final EditableModel model;
	private final RenderModel editorRenderModel;
	private final ModelViewStateNotifier modelViewStateNotifier;
	private final Set<Geoset> editableGeosets;
	private final Set<Geoset> visibleGeosets;
	private final Set<IdObject> editableIdObjects;
	private final Set<Camera> editableCameras;
	private Geoset highlightedGeoset;
	private IdObject highlightedNode;
	private final RenderByViewModelRenderer renderByViewModelRenderer;
	private final RenderByViewMeshRenderer renderByViewMeshRenderer;
	private boolean vetoParticles = false;

	public ModelViewManager(final EditableModel model) {
		this.model = model;
		editorRenderModel = new RenderModel(this.model, this);

		modelViewStateNotifier = new ModelViewStateNotifier();
		editableGeosets = new HashSet<>();
		for (final Geoset geoset : model.getGeosets()) {
			if (!ModelUtils.isLevelOfDetailSupported(model.getFormatVersion()) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
			}
		}
		visibleGeosets = new HashSet<>();
		editableIdObjects = new HashSet<>();
		editableCameras = new HashSet<>();
		renderByViewModelRenderer = new RenderByViewModelRenderer(this);
		renderByViewMeshRenderer = new RenderByViewMeshRenderer(this);
	}

	@Override
	public RenderModel getEditorRenderModel() {
		return editorRenderModel;
	}

	@Override
	public void visit(final ModelVisitor visitor) {
		model.render(renderByViewModelRenderer.reset(visitor));
	}

	@Override
	public void addStateListener(final ModelViewStateListener listener) {
		modelViewStateNotifier.subscribe(listener);
	}

	@Override
	public Set<Geoset> getVisibleGeosets() {
		return visibleGeosets;
	}

	@Override
	public Set<Geoset> getEditableGeosets() {
		return editableGeosets;
	}

	@Override
	public Set<IdObject> getEditableIdObjects() {
		return editableIdObjects;
	}

	@Override
	public Set<Camera> getEditableCameras() {
		return editableCameras;
	}

	@Override
	public EditableModel getModel() {
		return model;
	}

	@Override
	public Geoset getHighlightedGeoset() {
		return highlightedGeoset;
	}

	@Override
	public IdObject getHighlightedNode() {
		return highlightedNode;
	}

	public void makeGeosetEditable(final Geoset geoset) {
		editableGeosets.add(geoset);
		modelViewStateNotifier.geosetEditable(geoset);
	}

	public void makeGeosetNotEditable(final Geoset geoset) {
		editableGeosets.remove(geoset);
		modelViewStateNotifier.geosetNotEditable(geoset);
	}

	public void makeGeosetVisible(final Geoset geoset) {
		visibleGeosets.add(geoset);
		modelViewStateNotifier.geosetVisible(geoset);
	}

	public void makeGeosetNotVisible(final Geoset geoset) {
		visibleGeosets.remove(geoset);
		modelViewStateNotifier.geosetNotVisible(geoset);
	}

	public void makeIdObjectVisible(final IdObject bone) {
		editableIdObjects.add(bone);
		modelViewStateNotifier.idObjectVisible(bone);
	}

	public void makeIdObjectNotVisible(final IdObject bone) {
		editableIdObjects.remove(bone);
		modelViewStateNotifier.idObjectNotVisible(bone);
	}

	public void makeCameraVisible(final Camera camera) {
		editableCameras.add(camera);
		modelViewStateNotifier.cameraVisible(camera);
	}

	public void makeCameraNotVisible(final Camera camera) {
		editableCameras.remove(camera);
		modelViewStateNotifier.cameraNotVisible(camera);
	}

	public void highlightGeoset(final Geoset geoset) {
		highlightedGeoset = geoset;
		modelViewStateNotifier.highlightGeoset(geoset);
	}

	public void unhighlightGeoset(final Geoset geoset) {
		if (highlightedGeoset == geoset) {
			highlightedGeoset = null;
		}
		modelViewStateNotifier.unhighlightGeoset(geoset);
	}

	public void highlightNode(final IdObject node) {
		highlightedNode = node;
		modelViewStateNotifier.highlightNode(node);
	}

	public void unhighlightNode(final IdObject node) {
		if (highlightedNode == node) {
			highlightedNode = null;
		}
		modelViewStateNotifier.unhighlightNode(node);

	}

	@Override
	public boolean isVetoOverrideParticles() {
		return vetoParticles;
	}

	@Override
	public void setVetoOverrideParticles(boolean override) {
		vetoParticles = override;
	}

	@Override
	public void visitMesh(final MeshVisitor visitor) {
		model.visit(renderByViewMeshRenderer.reset(visitor));
	}
}
