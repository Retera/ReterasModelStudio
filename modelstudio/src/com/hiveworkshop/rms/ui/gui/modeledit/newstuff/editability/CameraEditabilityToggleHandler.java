package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.editability;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;

import java.util.List;

public class CameraEditabilityToggleHandler implements EditabilityToggleHandler {
	private final List<Camera> cameras;
	private final ModelView modelViewManager;

	public CameraEditabilityToggleHandler(final List<Camera> cameras, final ModelView modelViewManager) {
		this.cameras = cameras;
		this.modelViewManager = modelViewManager;
	}

	@Override
	public void makeEditable() {
		for (final Camera camera : cameras) {
			modelViewManager.makeCameraVisible(camera);
		}
	}

	@Override
	public void makeNotEditable() {
		for (final Camera camera : cameras) {
			modelViewManager.makeCameraNotVisible(camera);
		}
	}

}
