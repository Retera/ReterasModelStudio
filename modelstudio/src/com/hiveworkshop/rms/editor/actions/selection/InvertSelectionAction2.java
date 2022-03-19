package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

import java.util.HashSet;
import java.util.Set;

public final class InvertSelectionAction2 implements UndoAction {
	private final Set<GeosetVertex> previousVerts;
	private final Set<IdObject> previousIdObjects;
	private final Set<CameraNode> previousCameras;
	private final ModelView modelView;

	public InvertSelectionAction2(ModelView modelView) {
		this.modelView = modelView;
		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameraNodes());
	}

	@Override
	public UndoAction undo() {
		modelView.setSelectedVertices(previousVerts);
		modelView.setSelectedIdObjects(previousIdObjects);
		modelView.setSelectedCameraNodes(previousCameras);
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.invertVertSelection();
		modelView.invertIdObjSelection();
		modelView.invertCamSelection();
		return this;
	}

	@Override
	public String actionName() {
		return "invert selection";
	}
}
