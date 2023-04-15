package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec2;

import java.util.ArrayList;

public class RemoveUVLayerAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;
	private final int uvLayer;
	private final ArrayList<Vec2> uvs = new ArrayList<>();

	public RemoveUVLayerAction(Geoset geoset, int uvLayer, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		this.uvLayer = uvLayer;
		for(GeosetVertex vertex : geoset.getVertices()){
			uvs.add(vertex.getTVertex(uvLayer));
		}
	}

	@Override
	public RemoveUVLayerAction undo() {
		for(int i = 0; i< geoset.numVerteces(); i++){
			geoset.getVertex(i).addTVertex(uvs.get(i), uvLayer);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public RemoveUVLayerAction redo() {
		for(GeosetVertex vertex : geoset.getVertices()){
			vertex.removeTVertex(uvLayer);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Remove UV Layer from " + geoset.getName();
	}
}
