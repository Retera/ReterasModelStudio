package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class AddSelectionAction2 implements UndoAction {

	private Set<GeosetVertex> newVerts;
	private Set<IdObject> newIdObjects;
	private Set<Camera> newCameras;
	private Set<GeosetVertex> previousVerts;
	private Set<IdObject> previousIdObjects;
	private Set<Camera> previousCameras;
	private ModelView modelView;

	public AddSelectionAction2(Collection<GeosetVertex> newVerts,
	                           Collection<IdObject> newIdObjects,
	                           Collection<Camera> newCameras,
	                           ModelView modelView) {
		this.modelView = modelView;

		this.previousVerts = new HashSet<>(modelView.getSelectedVertices());
		this.previousIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		this.previousCameras = new HashSet<>(modelView.getSelectedCameras());

		this.newVerts = new HashSet<>(newVerts);
		this.newIdObjects = new HashSet<>(newIdObjects);
		this.newCameras = new HashSet<>(newCameras);
	}

	@Override
	public void undo() {
		modelView.setSelectedVertices(previousVerts);
		modelView.setSelectedIdObjects(previousIdObjects);
		modelView.setSelectedCameras(previousCameras);
	}

	@Override
	public void redo() {
		modelView.addSelectedVertices(newVerts);
		modelView.addSelectedIdObjects(newIdObjects);
		modelView.addSelectedCameras(newCameras);
	}

	@Override
	public String actionName() {
		return "add selection";
	}
}
