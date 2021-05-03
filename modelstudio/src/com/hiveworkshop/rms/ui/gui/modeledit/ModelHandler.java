package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManagerImpl;

public class ModelHandler {
	private EditableModel model;
	private UndoHandler undoHandler;
	private ModelView modelView;
	private UndoManager undoManager;
	private RenderModel renderModel;
	private RenderModel previewRenderModel;
	private TimeEnvironmentImpl editTimeEnv;
	private TimeEnvironmentImpl previewTimeEnv;

	public ModelHandler(EditableModel model, UndoHandler undoHandler) {
		this.model = model;
		this.undoHandler = undoHandler;
		if (undoHandler != null) {
			undoManager = new UndoManagerImpl(this.undoHandler);
		}
		editTimeEnv = new TimeEnvironmentImpl();
		modelView = new ModelView(model, editTimeEnv);
		renderModel = modelView.getEditorRenderModel();

		previewTimeEnv = new TimeEnvironmentImpl();
		previewRenderModel = new RenderModel(model, modelView, previewTimeEnv);
	}

	public EditableModel getModel() {
		return model;
	}

	public UndoHandler getUndoHandler() {
		return undoHandler;
	}

	public ModelView getModelView() {
		return modelView;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public RenderModel getRenderModel() {
		return modelView.getEditorRenderModel();
	}

	public RenderModel getPreviewRenderModel() {
		return previewRenderModel;
	}

	public TimeEnvironmentImpl getEditTimeEnv() {
		return editTimeEnv;
	}

	public TimeEnvironmentImpl getPreviewTimeEnv() {
		return previewTimeEnv;
	}
}
