package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;

public class DrawVertexAction implements UndoAction {
	private final GeosetVertex vertex;

	public DrawVertexAction(final GeosetVertex vertex) {
		this.vertex = vertex;
	}

	@Override
	public void undo() {
		vertex.getGeoset().remove(vertex);
	}

	@Override
	public void redo() {
		vertex.getGeoset().add(vertex);
	}

	@Override
	public String actionName() {
		return "add vertex";
	}

}
