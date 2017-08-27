package com.hiveworkshop.wc3.mdl.v2;

import com.etheller.collections.ArrayList;
import com.etheller.collections.List;
import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public final class ModelViewImpl implements ModelView {
	private final MDL model;
	private final ModelViewStateNotifier modelViewStateNotifier;
	private final List<Geoset> editableGeosets;
	private final List<Geoset> visibleGeosets;
	private final List<IdObject> editableIdObjects;
	private final List<Camera> editableCameras;

	public ModelViewImpl(final MDL model) {
		this.model = model;
		this.modelViewStateNotifier = new ModelViewStateNotifier();
		this.editableGeosets = new ArrayList<>();
		this.visibleGeosets = new ArrayList<>();
		this.editableIdObjects = new ArrayList<>();
		this.editableCameras = new ArrayList<>();
	}

	@Override
	public void visit(final ModelVisitor visitor) {
		model.render(visitor);
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

}
