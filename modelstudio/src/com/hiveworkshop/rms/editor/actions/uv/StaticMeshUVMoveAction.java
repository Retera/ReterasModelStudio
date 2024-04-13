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
	private final ArrayList<Vec2> orgTVerts;
	private final Vec3 totTranslate;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final Vec3 deltaTranslate = new Vec3();

	public StaticMeshUVMoveAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Vec3 moveVector, Mat4 rotMat) {
		selectedTVerts = new ArrayList<>();
		orgTVerts = new ArrayList<>();

		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				selectedTVerts.add(vertex.getTVertex(uvLayerIndex));
				orgTVerts.add(new Vec2(vertex.getTVertex(uvLayerIndex)));
			}
		}
		this.totTranslate = new Vec3(moveVector);
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
	}

	@Override
	public StaticMeshUVMoveAction undo() {
		for (int i = 0; i < selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i));
		}
		return this;
	}

	@Override
	public StaticMeshUVMoveAction redo() {
		rawMove(totTranslate);
		return this;
	}

	@Override
	public String actionName() {
		return "Move UV";
	}

	Vec3 tempVec = new Vec3();
	@Override
	public StaticMeshUVMoveAction updateTranslation(Vec3 delta) {
		totTranslate.add(delta);
		rawMove(totTranslate);
		return this;
	}

	@Override
	public StaticMeshUVMoveAction setTranslation(Vec3 transl) {
		deltaTranslate.set(transl).sub(totTranslate);
		totTranslate.set(transl);
		rawMove(transl);
		return this;
	}

	private void rawMove(Vec3 delta) {
		for (int i = 0; i < selectedTVerts.size(); i++) {
			tempVec.set(orgTVerts.get(i), 0)
					.transform(rotMat, 1, true)
					.add(delta)
					.transform(invRotMat, 1, true);
			selectedTVerts.get(i).set(tempVec.x, tempVec.y);
		}
	}

}
