package com.hiveworkshop.rms.ui.application.actions.uv;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel.UnwrapDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UVRemapAction implements UndoAction {

	private List<Vec2> tVertices;
	private final List<Vec2> newValueHolders;
	private final List<Vec2> oldValueHolders;
	private UnwrapDirection direction;
	Collection<? extends Vec3> selectedVertices;
	byte dim1;
	byte dim2;
	int uvLayerIndex;

	public UVRemapAction(List<Vec2> tVertices, List<Vec2> newValueHolders, List<Vec2> oldValueHolders, UVPanel.UnwrapDirection direction) {
		this.tVertices = tVertices;
		this.newValueHolders = newValueHolders;
		this.oldValueHolders = oldValueHolders;
		this.direction = direction;
	}
	public UVRemapAction(Collection<? extends Vec3> selectedVertices, int uvLayerIndex, byte dim1, byte dim2, UVPanel.UnwrapDirection direction) {
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.selectedVertices = selectedVertices;
		this.uvLayerIndex = uvLayerIndex;
		this.tVertices = new ArrayList<>();
		this.newValueHolders = new ArrayList<>();
		this.oldValueHolders = new ArrayList<>();
		this.direction = direction;
		remapUVs();
	}

	@Override
	public UndoAction undo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).set(oldValueHolders.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).set(newValueHolders.get(i));
		}
		return this;
	}

	@Override
	public String actionName() {
		return "remap TVertices " + direction;
	}


	private void remapUVs(){
		Vec2 min = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
		Vec2 max = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);

		for (Vec3 vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (uvLayerIndex < geosetVertex.getTverts().size()) {
					Vec2 modelDataTVertex = geosetVertex.getTVertex(uvLayerIndex);
					tVertices.add(modelDataTVertex);
					oldValueHolders.add(new Vec2(modelDataTVertex.x, modelDataTVertex.y));
					Vec2 newCoordValue = vertex.getProjected(dim1, dim2);

					max.maximize(newCoordValue);
					min.minimize(newCoordValue);

					newValueHolders.add(newCoordValue);
				}
			}
		}
		Vec2 uvIlandSpan = Vec2.getDif(max, min);

		if (uvIlandSpan.x == 0) {
			uvIlandSpan.x = 0.01f;
		}
		if (uvIlandSpan.y == 0) {
			uvIlandSpan.y = 0.01f;
		}
		for (Vec2 tv : newValueHolders) {
			tv.sub(min).div(uvIlandSpan);
		}
	}
}
