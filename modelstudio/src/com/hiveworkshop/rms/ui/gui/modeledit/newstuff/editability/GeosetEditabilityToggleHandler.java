package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.editability;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;

import java.util.List;

public class GeosetEditabilityToggleHandler implements EditabilityToggleHandler {
	private final List<Geoset> geosets;
	private final ModelView modelViewManager;

	public GeosetEditabilityToggleHandler(final List<Geoset> geosets, final ModelView modelViewManager) {
		this.geosets = geosets;
		this.modelViewManager = modelViewManager;
	}

	@Override
	public void makeEditable() {
		for (final Geoset geoset : geosets) {
			modelViewManager.makeGeosetEditable(geoset);
		}
	}

	@Override
	public void makeNotEditable() {
		for (final Geoset geoset : geosets) {
			modelViewManager.makeGeosetNotEditable(geoset);
		}
	}

}
