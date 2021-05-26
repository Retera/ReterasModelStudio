package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class RemoveSelectionAction2 implements UndoAction {

	private Set<GeosetVertex> deSelVerts;
	private Set<IdObject> deSelIdObjects;
	private Set<Camera> deSelCameras;
	private Set<GeosetVertex> previousVerts;
	private Set<IdObject> previousIdObjects;
	private Set<Camera> previousCameras;
	private ModelView modelView;

	public RemoveSelectionAction2(Collection<GeosetVertex> deSelVerts,
	                           Collection<IdObject> deSelIdObjects,
	                           Collection<Camera> deSelCameras,
	                           ModelView modelView) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.deSelVerts = new HashSet<>(deSelVerts);
		this.deSelIdObjects = new HashSet<>(deSelIdObjects);
		this.deSelCameras = new HashSet<>(deSelCameras);
	}

	public RemoveSelectionAction2(Collection<GeosetVertex> deSelVerts, ModelView modelView) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.deSelVerts = new HashSet<>(deSelVerts);
		this.deSelIdObjects = new HashSet<>();
		this.deSelCameras = new HashSet<>();
	}

	@Override
	public void undo() {
		modelView.setSelectedVertices(previousVerts);
		modelView.setSelectedIdObjects(previousIdObjects);
		modelView.setSelectedCameras(previousCameras);

	}

	@Override
	public void redo() {
		modelView.removeSelectedVertices(deSelVerts);
		modelView.removeSelectedIdObjects(deSelIdObjects);
		modelView.removeSelectedCameras(deSelCameras);
	}

	@Override
	public String actionName() {
		return "deselect";
	}
}
