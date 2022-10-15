package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public final class StaticMeshUVRotateAction2 implements GenericRotateAction {
	private final ArrayList<Vec2> selectedTVerts;
	private final ArrayList<Vec2> orgTVerts;
	private final Vec3 center;
	private final Vec2 centerV2;
	private double radians;
	int uvLayerIndex;
	private final Vec3 axis;
	private final Quat rot = new Quat();


	public StaticMeshUVRotateAction2(Collection<GeosetVertex> selectedVertices,
	                                 int uvLayerIndex, Vec3 center, Vec3 axis,
	                                 double radians) {
		selectedTVerts = new ArrayList<>();
		orgTVerts = new ArrayList<>();
		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				Vec2 tVertex = vertex.getTVertex(uvLayerIndex);
				selectedTVerts.add(tVertex);
				orgTVerts.add(new Vec2(tVertex));
			}
		}
		this.uvLayerIndex = uvLayerIndex;
		this.center = center;
		this.axis = axis;
		this.centerV2 = new Vec2(center.x, center.y);
		this.radians = radians;
	}

	@Override
	public UndoAction undo() {
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		rot.setFromAxisAngle(axis, (float) radians);
		for (Vec2 vertex : selectedTVerts) {
			vertex.rotate(centerV2, rot);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Rotate UVs";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		this.radians += radians;
		rot.setFromAxisAngle(axis, (float) radians);
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i)).rotate(centerV2, rot);
		}
		return this;
	}

}
