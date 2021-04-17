package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.editability;

import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;

public class CameraEditabilityToggleHandler implements EditabilityToggleHandler {
	private final List<Camera> cameras;
	private final ModelViewManager modelViewManager;

	public CameraEditabilityToggleHandler(final List<Camera> cameras, final ModelViewManager modelViewManager) {
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
