package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;

public class DrawVertexAction implements UndoAction {
	private final GeosetVertex vertex;

	public DrawVertexAction(final GeosetVertex vertex) {
		this.vertex = vertex;
	}

	@Override
	public UndoAction undo() {
		vertex.getGeoset().remove(vertex);
		return this;
	}

	@Override
	public UndoAction redo() {
		vertex.getGeoset().add(vertex);
		return this;
	}

	@Override
	public String actionName() {
		return "add vertex";
	}

}
