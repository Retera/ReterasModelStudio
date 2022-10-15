package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public final class StaticMeshUVMoveAction implements GenericMoveAction {
	private final ArrayList<Vec2> selectedTVerts;
	private final Vec2 moveVector;
	int uvLayerIndex;

	public StaticMeshUVMoveAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Vec3 moveVector) {
		selectedTVerts = new ArrayList<>();

		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				selectedTVerts.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		this.moveVector = new Vec2(moveVector.x, moveVector.y);
		this.uvLayerIndex = uvLayerIndex;
	}

	@Override
	public UndoAction undo() {
		for (Vec2 vertex : selectedTVerts) {
			vertex.sub(moveVector);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec2 vertex : selectedTVerts) {
			vertex.add(moveVector);
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
		for (Vec2 vertex : selectedTVerts) {
			vertex.translate(deltaX, deltaY);
		}
	}

	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
		moveVector.x += delta.x;
		moveVector.y += delta.y;
		for (Vec2 vertex : selectedTVerts) {
			vertex.translate(delta.x, delta.y);
		}
		return this;
	}

}
