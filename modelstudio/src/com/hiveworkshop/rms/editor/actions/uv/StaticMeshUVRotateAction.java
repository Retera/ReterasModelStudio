package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public final class StaticMeshUVRotateAction extends AbstractTransformAction {
	private final ArrayList<Vec2> selectedTVerts;
	private final ArrayList<Vec2> orgTVerts;
	private final Vec3 center;
	private double radians;
	private final Vec3 axis;
	private final Quat rot = new Quat();
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	public StaticMeshUVRotateAction(Collection<GeosetVertex> selectedVertices,
	                                int uvLayerIndex, Vec3 center, Vec3 axis,
	                                double radians, Mat4 rotMat) {
		selectedTVerts = new ArrayList<>();
		orgTVerts = new ArrayList<>();
		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				Vec2 tVertex = vertex.getTVertex(uvLayerIndex);
				selectedTVerts.add(tVertex);
				orgTVerts.add(new Vec2(tVertex));
			}
		}
		this.center = center;
		this.axis = axis;
		this.radians = radians;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
	}
	@Override
	public StaticMeshUVRotateAction undo() {
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i));
		}
		return this;
	}

	@Override
	public StaticMeshUVRotateAction redo() {
		rawRot(radians);
		return this;
	}

	@Override
	public String actionName() {
		return "Rotate UVs";
	}

	@Override
	public StaticMeshUVRotateAction updateRotation(double radians) {
		this.radians += radians;
		rawRot(this.radians);
		return this;
	}

	Vec3 tempVec = new Vec3();
	@Override
	public StaticMeshUVRotateAction setRotation(double radians) {
		this.radians = radians;
		rawRot(this.radians);
		return this;
	}

	private void rawRot(double radians) {
		rot.setFromAxisAngle(axis, (float) radians).normalize();
		for (int i = 0; i<selectedTVerts.size(); i++) {
			tempVec.set(orgTVerts.get(i), 0)
					.sub(center)
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true)
					.add(center);
			selectedTVerts.get(i).set(tempVec.x, tempVec.y);
		}
	}
}
