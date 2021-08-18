package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;

public class ModelHandler {
	private EditableModel model;
	private UndoHandler undoHandler;
	private ModelView modelView;
	private UndoManager undoManager;
	private RenderModel renderModel;
	private RenderModel previewRenderModel;
//	private TimeEnvironmentImpl editTimeEnv;
//	private TimeEnvironmentImpl previewTimeEnv;

	public ModelHandler(EditableModel model) {
		this.model = model;
		this.undoHandler = ProgramGlobals.getUndoHandler();
		if (this.undoHandler != null) {
			undoManager = new UndoManager(this.undoHandler);
		}
//		editTimeEnv = new TimeEnvironmentImpl();
		modelView = new ModelView(model);
//		renderModel = modelView.getEditorRenderModel();

//		renderModel = new RenderModel(this.model, modelView, editTimeEnv);
//		renderModel.setShouldForceAnimation(true);
//
//		previewTimeEnv = new TimeEnvironmentImpl();
//		previewRenderModel = new RenderModel(model, modelView, previewTimeEnv);

		renderModel = new RenderModel(this.model, modelView);
		renderModel.setShouldForceAnimation(true);

//		previewTimeEnv = new TimeEnvironmentImpl();
		previewRenderModel = new RenderModel(model, modelView);
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
		return renderModel;
	}

	public RenderModel getPreviewRenderModel() {
		return previewRenderModel;
	}

	public TimeEnvironmentImpl getEditTimeEnv() {
//		return editTimeEnv;
		return renderModel.getTimeEnvironment();
	}

	public TimeEnvironmentImpl getPreviewTimeEnv() {
//		return previewTimeEnv;
		return previewRenderModel.getTimeEnvironment();
	}
}
