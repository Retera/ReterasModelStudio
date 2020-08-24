package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;

public interface ModelViewStateListener {
	void geosetEditable(Geoset geoset);

	void geosetNotEditable(Geoset geoset);

	void geosetVisible(Geoset geoset);

	void geosetNotVisible(Geoset geoset);

	void idObjectVisible(IdObject bone);

	void idObjectNotVisible(IdObject bone);

	void cameraVisible(Camera camera);

	void cameraNotVisible(Camera camera);

	void highlightGeoset(Geoset geoset);

	void unhighlightGeoset(Geoset geoset);

	void highlightNode(IdObject node);

	void unhighlightNode(IdObject node);

}
