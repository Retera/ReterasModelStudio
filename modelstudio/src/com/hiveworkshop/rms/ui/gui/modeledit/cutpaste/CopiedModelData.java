package com.hiveworkshop.rms.ui.gui.modeledit.cutpaste;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;

public final class CopiedModelData {
	private final List<Geoset> geosets;
	private final List<IdObject> idObjects;
	private final List<Camera> cameras;

	public CopiedModelData(final Collection<Geoset> geosets, final Collection<IdObject> idObjects,
			final Collection<Camera> cameras) {
		this.geosets = new ArrayList<>(geosets);
		this.idObjects = new ArrayList<>(idObjects);
		this.cameras = new ArrayList<>(cameras);
	}

	public List<Geoset> getGeosets() {
		return geosets;
	}

	public List<IdObject> getIdObjects() {
		return idObjects;
	}

	public List<Camera> getCameras() {
		return cameras;
	}
}
