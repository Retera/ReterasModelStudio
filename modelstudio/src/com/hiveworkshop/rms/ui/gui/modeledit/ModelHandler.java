package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;

import javax.swing.*;

public class ModelHandler {
	private final EditableModel model;
	private final UndoHandler undoHandler;
	private final ModelView modelView;
	private UndoManager undoManager;
	private final RenderModel renderModel;
	private final RenderModel previewRenderModel;

	private final Icon icon;

	public ModelHandler(EditableModel model) {
		this(model, null);
	}
	public ModelHandler(EditableModel model, Icon icon) {
		this.model = model;
		this.icon = icon;
		this.undoHandler = ProgramGlobals.getUndoHandler();
		if (this.undoHandler != null) {
			undoManager = new UndoManager(this.undoHandler);
		}
		modelView = new ModelView(model);

		renderModel = new RenderModel(this.model, modelView);
		renderModel.setShouldForceAnimation(true);

		previewRenderModel = new RenderModel(this.model, modelView);
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
		return renderModel.getTimeEnvironment();
	}

	public TimeEnvironmentImpl getPreviewTimeEnv() {
		return previewRenderModel.getTimeEnvironment();
	}

	public Icon getIcon() {
		return icon;
	}
}
