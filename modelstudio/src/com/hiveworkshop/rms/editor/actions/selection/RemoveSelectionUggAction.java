package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectoinUgg;

import java.util.HashSet;
import java.util.Set;

public final class RemoveSelectionUggAction implements UndoAction {

	private final ModelView modelView;
	private final Set<GeosetVertex> previousVerts;
	private final Set<IdObject> previousIdObjects;
	private final Set<Camera> previousCameras;
	private final Set<GeosetVertex> affectedVerts;
	private final Set<IdObject> affectedIdObjects;
	private final Set<Camera> affectedCameras;

	public RemoveSelectionUggAction(SelectoinUgg newSelection, ModelView modelView) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.affectedVerts = new HashSet<>(newSelection.getSelectedVertices());
		this.affectedIdObjects = new HashSet<>(newSelection.getSelectedIdObjects());
		this.affectedCameras = new HashSet<>(newSelection.getSelectedCameras());
	}

	@Override
	public UndoAction undo() {
		modelView.setSelectedVertices(previousVerts);
		modelView.setSelectedIdObjects(previousIdObjects);
		modelView.setSelectedCameras(previousCameras);
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.removeSelectedVertices(affectedVerts);
		modelView.removeSelectedIdObjects(affectedIdObjects);
		modelView.removeSelectedCameras(affectedCameras);
		return this;
	}

	@Override
	public String actionName() {
		return "deselect";
	}
}
