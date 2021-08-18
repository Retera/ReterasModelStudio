package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class AddSelectionUggAction implements UndoAction {

	private final Set<GeosetVertex> affectedVerts;
	private final Set<IdObject> affectedIdObjects;
	private final Set<Camera> affectedCameras;
	private final Set<GeosetVertex> previousVerts;
	private final Set<IdObject> previousIdObjects;
	private final Set<Camera> previousCameras;
	private final ModelView modelView;

	public AddSelectionUggAction(SelectionBundle newSelection,
	                             ModelView modelView) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.affectedVerts = new HashSet<>(newSelection.getSelectedVertices());
		this.affectedIdObjects = new HashSet<>(newSelection.getSelectedIdObjects());
		this.affectedCameras = new HashSet<>(newSelection.getSelectedCameras());
	}

	public AddSelectionUggAction(Collection<GeosetVertex> newVerts, ModelView modelView) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.affectedVerts = new HashSet<>(newVerts);
		this.affectedIdObjects = new HashSet<>();
		this.affectedCameras = new HashSet<>();
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
		modelView.addSelectedVertices(affectedVerts);
		modelView.addSelectedIdObjects(affectedIdObjects);
		modelView.addSelectedCameras(affectedCameras);
		return this;
	}

	@Override
	public String actionName() {
		return "add selection";
	}
}
