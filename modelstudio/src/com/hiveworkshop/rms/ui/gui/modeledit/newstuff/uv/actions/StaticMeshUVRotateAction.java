package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public final class StaticMeshUVRotateAction implements GenericRotateAction {
	private final Collection<? extends Vec3> selectedVertices;
	private final Vec3 center;
	private double radians;
	private final byte dim1;
	private final byte dim2;
	int uvLayerIndex;

	public StaticMeshUVRotateAction(Collection<? extends Vec3> selectedVertices, int uvLayerIndex, Vec2 center, byte dim1, byte dim2) {
		this.selectedVertices = selectedVertices;
		this.uvLayerIndex = uvLayerIndex;
		this.center = new Vec3(center.x, center.y, 0);
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.radians = 0;
	}

	public StaticMeshUVRotateAction(Collection<? extends Vec3> selectedVertices, int uvLayerIndex, Vec3 center, byte dim1, byte dim2) {
		this.selectedVertices = selectedVertices;
		this.uvLayerIndex = uvLayerIndex;
		this.center = center;
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.radians = 0;
	}

	@Override
	public UndoAction undo() {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.rotate(center.x, center.y, -radians, dim1, dim2);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.rotate(center.x, center.y, radians, dim1, dim2);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "rotate";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		this.radians += radians;
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.rotate(center.x, center.y, radians, dim1, dim2);
		}
		return this;
	}

}
