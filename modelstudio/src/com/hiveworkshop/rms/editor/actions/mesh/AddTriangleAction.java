package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Triangle;

import java.util.Collection;

public class AddTriangleAction implements UndoAction {
	private final Geoset geoset;
	private final Collection<Triangle> triangles;
	private final String actionName;

	public AddTriangleAction(Geoset geoset, Collection<Triangle> triangles) {
		this.geoset = geoset;
		this.triangles = triangles;
		actionName = triangles.size() == 1 ? "Add Face" : "Add Faces";
	}

	@Override
	public UndoAction undo() {
		for (Triangle triangle : triangles) {
			geoset.remove(triangle.removeFromVerts());
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Triangle triangle : triangles) {
			geoset.add(triangle.addToVerts());
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}

}
