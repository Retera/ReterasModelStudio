package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public final class StaticMeshUVMoveAction extends AbstractTransformAction {
	private final ArrayList<Vec2> selectedTVerts;
	private final Vec3 moveVector;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	public StaticMeshUVMoveAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Vec3 moveVector, Mat4 rotMat) {
		selectedTVerts = new ArrayList<>();

		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				selectedTVerts.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		this.moveVector = new Vec3(moveVector);
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
	}

	@Override
	public StaticMeshUVMoveAction undo() {
		Vec3 invMove = new Vec3().sub(moveVector);
		rawMove(invMove);
		return this;
	}

	@Override
	public StaticMeshUVMoveAction redo() {
		rawMove(moveVector);
		return this;
	}

	@Override
	public String actionName() {
		return "Move UV";
	}

	Vec3 tempVec = new Vec3();
	@Override
	public StaticMeshUVMoveAction updateTranslation(Vec3 delta) {
		moveVector.add(delta);
		rawMove(delta);
		return this;
	}

	private void rawMove(Vec3 delta) {
		for (Vec2 vertex : selectedTVerts) {
			tempVec.set(vertex, 0)
					.transform(rotMat, 1, true)
					.add(delta)
					.transform(invRotMat, 1, true);
			vertex.set(tempVec.x, tempVec.y);
		}
	}

}
