package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public class StaticMeshUVScaleAction implements GenericScaleAction {
	private final ArrayList<Vec2> selectedTVerts;
	private final ArrayList<Vec2> orgTVerts;
	private final Vec2 center;
	private final Vec2 scale;
	private final Vec2 dScale = new Vec2(1, 1);
	int uvLayerIndex;

	public StaticMeshUVScaleAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Vec2 center, Vec3 scale) {
		selectedTVerts = new ArrayList<>();
		orgTVerts = new ArrayList<>();
		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				selectedTVerts.add(vertex.getTVertex(uvLayerIndex));
				orgTVerts.add(new Vec2(vertex.getTVertex(uvLayerIndex)));
			}
		}
		this.uvLayerIndex = uvLayerIndex;
		this.center = new Vec2(center);
		this.scale = new Vec2(scale.x, scale.y);
	}

	@Override
	public UndoAction undo() {
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec2 vertex : selectedTVerts) {
			vertex.scale(center, scale);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "scale";
	}

	@Override
	public GenericScaleAction updateScale(Vec3 scale) {
		dScale.set(scale.x, scale.y);
		this.scale.mul(dScale);
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i)).scale(center, this.scale);
		}
		return this;
	}

}
