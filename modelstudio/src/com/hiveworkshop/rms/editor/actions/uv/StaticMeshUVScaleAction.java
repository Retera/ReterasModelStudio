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
	private final Vec2 center;
	private final Vec2 scale = new Vec2(1, 1);
	private final Vec2 dScale = new Vec2(1, 1);
	int uvLayerIndex;


	public StaticMeshUVScaleAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Vec2 center) {
		selectedTVerts = new ArrayList<>();
		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				selectedTVerts.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		this.uvLayerIndex = uvLayerIndex;
		this.center = new Vec2(center);
	}

	@Override
	public UndoAction undo() {
		Vec2 invScale = new Vec2(1, 1).div(scale);
		for (Vec2 vertex : selectedTVerts) {
			vertex.scale(center, invScale);
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
		for (Vec2 vertex : selectedTVerts) {
			vertex.scale(center, dScale);
		}
		return this;
	}

}
