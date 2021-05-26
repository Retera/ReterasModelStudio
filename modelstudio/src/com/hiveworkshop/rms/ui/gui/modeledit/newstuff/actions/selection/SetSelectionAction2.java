package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SetSelectionAction2 implements UndoAction {

	private final Set<GeosetVertex> newVerts;
	private final Set<IdObject> newIdObjects;
	private final Set<Camera> newCameras;
	private final Set<GeosetVertex> previousVerts;
	private final Set<IdObject> previousIdObjects;
	private final Set<Camera> previousCameras;
	private final ModelView modelView;
	private final String actionName;

	public SetSelectionAction2(Collection<GeosetVertex> newVerts,
	                           Collection<IdObject> newIdObjects,
	                           Collection<Camera> newCameras,
	                           ModelView modelView, String actionName) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.newVerts = new HashSet<>(newVerts);
		this.newIdObjects = new HashSet<>(newIdObjects);
		this.newCameras = new HashSet<>(newCameras);
		this.actionName = actionName;
	}
	public SetSelectionAction2(Collection<GeosetVertex> newVerts,
	                           ModelView modelView, String actionName) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.newVerts = new HashSet<>(newVerts);
		this.newIdObjects = new HashSet<>();
		this.newCameras = new HashSet<>();
		this.actionName = actionName;
	}

	@Override
	public void undo() {
		modelView.setSelectedVertices(previousVerts);
		modelView.setSelectedIdObjects(previousIdObjects);
		modelView.setSelectedCameras(previousCameras);
	}

	@Override
	public void redo() {
		modelView.setSelectedVertices(newVerts);
		modelView.setSelectedIdObjects(newIdObjects);
		modelView.setSelectedCameras(newCameras);
	}

//	@Override
//	public void undo() {
//		selectionManager.setSelection(previousSelection);
//
//	}
//
//	@Override
//	public void redo() {
//		selectionManager.setSelection(selection);
//	}

	@Override
	public String actionName() {
		return actionName;
	}
}
