package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public final class StaticMeshUVMoveAction implements GenericMoveAction {
	private Collection<? extends Vec3> selectedVertices;
	private final Vec2 moveVector;
	int uvLayerIndex;

	public StaticMeshUVMoveAction(Collection<? extends Vec3> selectedVertices, int uvLayerIndex, Vec2 moveVector) {
		this.selectedVertices = selectedVertices;
		this.moveVector = new Vec2(moveVector);
		this.uvLayerIndex = uvLayerIndex;
	}

	@Override
	public UndoAction undo() {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.translate(-moveVector.x, -moveVector.y);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.translate(moveVector.x, moveVector.y);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "move UV";
	}

	@Override
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		moveVector.x += deltaX;
		moveVector.y += deltaY;
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.translate(deltaX, deltaY);
		}
	}

	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
		moveVector.x += delta.x;
		moveVector.y += delta.y;
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.translate(delta.x, delta.y);
		}
		return this;
	}

}
