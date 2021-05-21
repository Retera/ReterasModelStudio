package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public final class ModelViewStateNotifier extends SubscriberSetNotifier<ModelViewStateListener> implements ModelViewStateListener {

	@Override
	public void geosetEditable(Geoset geoset) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.geosetEditable(geoset);
		}
	}

	@Override
	public void geosetNotEditable(Geoset geoset) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.geosetNotEditable(geoset);
		}
	}

	@Override
	public void geosetVisible(Geoset geoset) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.geosetVisible(geoset);
		}
	}

	@Override
	public void geosetNotVisible(Geoset geoset) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.geosetNotVisible(geoset);
		}
	}

	@Override
	public void idObjectVisible(IdObject bone) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.idObjectVisible(bone);
		}
	}

	@Override
	public void idObjectNotVisible(IdObject bone) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.idObjectNotVisible(bone);
		}
	}

	@Override
	public void cameraVisible(Camera camera) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.cameraVisible(camera);
		}
	}

	@Override
	public void cameraNotVisible(Camera camera) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.cameraNotVisible(camera);
		}
	}

	@Override
	public void highlightGeoset(Geoset geoset) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.highlightGeoset(geoset);
		}
	}

	@Override
	public void unhighlightGeoset(Geoset geoset) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.unhighlightGeoset(geoset);
		}
	}

	@Override
	public void highlightNode(IdObject node) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.highlightNode(node);
		}
	}

	@Override
	public void unhighlightNode(IdObject node) {
		for (ModelViewStateListener listener : listenerSet) {
			listener.unhighlightNode(node);
		}
	}

}
