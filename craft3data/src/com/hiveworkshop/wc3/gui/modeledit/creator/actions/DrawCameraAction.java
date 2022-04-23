package com.hiveworkshop.wc3.gui.modeledit.creator.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class DrawCameraAction implements UndoAction {
	private final ModelView modelView;
	private final Camera camera;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final List<Camera> cameraAsList;

	public DrawCameraAction(final ModelView modelView, final ModelStructureChangeListener modelStructureChangeListener,
			final Camera camera) {
		this.modelView = modelView;
		this.camera = camera;
		this.modelStructureChangeListener = modelStructureChangeListener;
		cameraAsList = new ArrayList<>();
		cameraAsList.add(camera);
	}

	@Override
	public void undo() {
		modelView.getModel().remove(camera);
		modelStructureChangeListener.camerasRemoved(cameraAsList);
	}

	@Override
	public void redo() {
		modelView.getModel().add(camera);
		modelStructureChangeListener.camerasAdded(cameraAsList);
	}

	@Override
	public String actionName() {
		return "add " + camera.getName();
	}

}
