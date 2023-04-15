package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec2;

public class MoveUVLayerAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;
	private final int oldIndex;
	private final int newIndex;

	public MoveUVLayerAction(Geoset geoset, int oldIndex, int newIndex, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		this.oldIndex = oldIndex;
		this.newIndex = newIndex;
	}

	@Override
	public MoveUVLayerAction undo() {
		for(GeosetVertex vertex : geoset.getVertices()){
			Vec2 tVertex = vertex.getTVertex(newIndex);
			vertex.removeTVertex(newIndex);
			vertex.addTVertex(tVertex, oldIndex);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public MoveUVLayerAction redo() {
		for(GeosetVertex vertex : geoset.getVertices()){
			Vec2 tVertex = vertex.getTVertex(oldIndex);
			vertex.removeTVertex(oldIndex);
			vertex.addTVertex(tVertex, newIndex);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Move UV Layer in " + geoset.getName();
	}
}
