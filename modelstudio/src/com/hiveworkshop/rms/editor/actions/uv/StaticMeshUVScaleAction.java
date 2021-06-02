package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public class StaticMeshUVScaleAction implements GenericScaleAction {
	private final Collection<GeosetVertex> selectedVertices;
	private final Vec2 center;
	private final Vec2 scale = new Vec2(1, 1);
	private final Vec2 dScale = new Vec2(1, 1);
	int uvLayerIndex;


	public StaticMeshUVScaleAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Vec2 center) {
		this.selectedVertices = selectedVertices;
		this.uvLayerIndex = uvLayerIndex;
		this.center = new Vec2(center);
	}

	@Override
	public UndoAction undo() {
		Vec2 invScale = new Vec2(1, 1).div(scale);
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.scale(center, invScale);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
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
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.scale(center, dScale);
		}
		return this;
	}

}
