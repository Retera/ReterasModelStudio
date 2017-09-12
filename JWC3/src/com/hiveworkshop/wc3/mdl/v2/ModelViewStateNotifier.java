package com.hiveworkshop.wc3.mdl.v2;

import com.etheller.util.SubscriberSetNotifier;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;

public final class ModelViewStateNotifier extends SubscriberSetNotifier<ModelViewStateListener>
		implements ModelViewStateListener {

	@Override
	public void geosetEditable(final Geoset geoset) {
		for (final ModelViewStateListener listener : set) {
			listener.geosetEditable(geoset);
		}
	}

	@Override
	public void geosetNotEditable(final Geoset geoset) {
		for (final ModelViewStateListener listener : set) {
			listener.geosetNotEditable(geoset);
		}
	}

	@Override
	public void geosetVisible(final Geoset geoset) {
		for (final ModelViewStateListener listener : set) {
			listener.geosetVisible(geoset);
		}
	}

	@Override
	public void geosetNotVisible(final Geoset geoset) {
		for (final ModelViewStateListener listener : set) {
			listener.geosetNotVisible(geoset);
		}
	}

	@Override
	public void idObjectVisible(final IdObject bone) {
		for (final ModelViewStateListener listener : set) {
			listener.idObjectVisible(bone);
		}
	}

	@Override
	public void idObjectNotVisible(final IdObject bone) {
		for (final ModelViewStateListener listener : set) {
			listener.idObjectNotVisible(bone);
		}
	}

	@Override
	public void cameraVisible(final Camera camera) {
		for (final ModelViewStateListener listener : set) {
			listener.cameraVisible(camera);
		}
	}

	@Override
	public void cameraNotVisible(final Camera camera) {
		for (final ModelViewStateListener listener : set) {
			listener.cameraNotVisible(camera);
		}
	}

	@Override
	public void highlightGeoset(final Geoset geoset) {
		for (final ModelViewStateListener listener : set) {
			listener.highlightGeoset(geoset);
		}
	}

}
