package com.hiveworkshop.wc3.mdl.v2;

import com.etheller.collections.ArrayList;
import com.etheller.collections.List;
import com.etheller.collections.ListView;
import com.etheller.util.CollectionUtils;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.render.RenderByViewModelRenderer;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public final class ModelViewManager implements ModelView {
	private final MDL model;
	private final ModelViewStateNotifier modelViewStateNotifier;
	private final List<Geoset> editableGeosets;// TODO should be a set
	private final List<Geoset> visibleGeosets;
	private final List<IdObject> editableIdObjects;
	private final List<Camera> editableCameras;
	private Geoset highlightedGeoset;
	private final RenderByViewModelRenderer renderByViewModelRenderer;

	public ModelViewManager(final MDL model) {
		this.model = model;
		this.modelViewStateNotifier = new ModelViewStateNotifier();
		this.editableGeosets = new ArrayList<>(CollectionUtils.asList(model.getGeosets()));
		this.visibleGeosets = new ArrayList<>();
		this.editableIdObjects = new ArrayList<>();
		this.editableCameras = new ArrayList<>();
		this.renderByViewModelRenderer = new RenderByViewModelRenderer(this);
	}

	public void setHighlightedGeoset(final Geoset highlightedGeoset) {
		this.highlightedGeoset = highlightedGeoset;
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
	public ListView<Geoset> getVisibleGeosets() {
		return visibleGeosets;
	}

	@Override
	public ListView<Geoset> getEditableGeosets() {
		return editableGeosets;
	}

	@Override
	public ListView<IdObject> getEditableIdObjects() {
		return editableIdObjects;
	}

	@Override
	public ListView<Camera> getEditableCameras() {
		return editableCameras;
	}

	@Override
	public MDL getModel() {
		return model;
	}

	@Override
	public Geoset getHighlightedGeoset() {
		return highlightedGeoset;
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

}
