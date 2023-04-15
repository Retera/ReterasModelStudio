package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec2;

import java.util.ArrayList;

public class AddUVLayerAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;
	private final int uvLayer;
	private final ArrayList<Vec2> uvs = new ArrayList<>();

	public AddUVLayerAction(Geoset geoset, int layerDest, int layerToCopy, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		uvLayer = layerDest;
		boolean copyExisting = 0 <= layerToCopy;
		for(GeosetVertex vertex : geoset.getVertices()){
			if(copyExisting){
				uvs.add(new Vec2(vertex.getTVertex(layerToCopy)));
			} else {
				uvs.add(new Vec2());
			}
		}
	}

	@Override
	public AddUVLayerAction undo() {
		for(GeosetVertex vertex : geoset.getVertices()){
			vertex.removeTVertex(uvLayer);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public AddUVLayerAction redo() {
		for(int i = 0; i< geoset.numVerteces(); i++){
			geoset.getVertex(i).addTVertex(uvs.get(i), uvLayer);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add UV Layer to " + geoset.getName();
	}
}
