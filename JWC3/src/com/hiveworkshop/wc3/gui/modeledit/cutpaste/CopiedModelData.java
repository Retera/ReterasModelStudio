package com.hiveworkshop.wc3.gui.modeledit.cutpaste;

import com.etheller.collections.ArrayList;
import com.etheller.collections.Collection;
import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;

public final class CopiedModelData {
	private final ListView<Geoset> geosets;
	private final ListView<IdObject> idObjects;
	private final ListView<Camera> cameras;

	public CopiedModelData(final Collection<Geoset> geosets, final Collection<IdObject> idObjects,
			final Collection<Camera> cameras) {
		this.geosets = new ArrayList<>(geosets);
		this.idObjects = new ArrayList<>(idObjects);
		this.cameras = new ArrayList<>(cameras);
	}

	public CopiedModelData(final java.util.Collection<Geoset> geosets, final java.util.Collection<IdObject> idObjects,
			final java.util.Collection<Camera> cameras) {
		this.geosets = ListView.Util.of(geosets.toArray(new Geoset[geosets.size()]));
		this.idObjects = ListView.Util.of(idObjects.toArray(new IdObject[idObjects.size()]));
		this.cameras = ListView.Util.of(cameras.toArray(new Camera[cameras.size()]));
	}

	public ListView<Geoset> getGeosets() {
		return geosets;
	}

	public ListView<IdObject> getIdObjects() {
		return idObjects;
	}

	public ListView<Camera> getCameras() {
		return cameras;
	}
}
