package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
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
	public void undo() {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.translate(-moveVector.x, -moveVector.y);
		}
	}

	@Override
	public void redo() {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.translate(moveVector.x, moveVector.y);
		}
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
	public void updateTranslation(Vec3 delta) {
		moveVector.x += delta.x;
		moveVector.y += delta.y;
		for (Vec2 vertex : TVertexUtils.getTVertices(selectedVertices, uvLayerIndex)) {
			vertex.translate(delta.x, delta.y);
		}
	}

}
