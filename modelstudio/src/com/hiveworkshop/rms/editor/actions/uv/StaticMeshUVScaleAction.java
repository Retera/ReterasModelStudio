package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public class StaticMeshUVScaleAction extends AbstractTransformAction {
	private final ArrayList<Vec2> selectedTVerts;
	private final ArrayList<Vec2> orgTVerts;
	private final Vec3 center;
	private final Vec3 scale;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();


	public StaticMeshUVScaleAction(Collection<GeosetVertex> selectedVertices,
	                               int uvLayerIndex, Vec3 center,
	                               Vec3 scale, Mat4 rotMat) {
		selectedTVerts = new ArrayList<>();
		orgTVerts = new ArrayList<>();
		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				selectedTVerts.add(vertex.getTVertex(uvLayerIndex));
				orgTVerts.add(new Vec2(vertex.getTVertex(uvLayerIndex)));
			}
		}
		this.center = new Vec3(center);
		this.scale = new Vec3(scale);
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
	}

	@Override
	public StaticMeshUVScaleAction undo() {
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i));
		}
		return this;
	}

	@Override
	public StaticMeshUVScaleAction redo() {
		rawScale(scale);
		return this;
	}

	@Override
	public String actionName() {
		return "Scale";
	}

	@Override
	public StaticMeshUVScaleAction updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		rawScale(this.scale);
		return this;
	}
	public StaticMeshUVScaleAction setScale(Vec3 scale) {
		this.scale.set(scale);
		rawScale(scale);
		return this;
	}


	Vec3 tempVec = new Vec3();
	private void rawScale(Vec3 scale) {
		for (int i = 0; i<selectedTVerts.size(); i++) {
			tempVec.set(orgTVerts.get(i), -1)
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);
			selectedTVerts.get(i).set(tempVec.x, tempVec.y);
		}
	}
}
