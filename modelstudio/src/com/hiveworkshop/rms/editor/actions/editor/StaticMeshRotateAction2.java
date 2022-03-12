package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.HashSet;
import java.util.Set;

public final class StaticMeshRotateAction2 implements GenericRotateAction {
	private final Vec3 center;
	private double radians;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<CameraNode> selectedCameraNodes;
	Vec3 axis;
	Quat rot = new Quat();


	public StaticMeshRotateAction2(ModelView modelView, Vec3 center, Vec3 axis) {
		this.center = center;
		this.axis = axis;
		radians = 0;
		selectedVertices = new HashSet<>(modelView.getSelectedVertices());
		selectedIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		selectedCameraNodes = new HashSet<>(modelView.getSelectedCameraNodes());
	}

	@Override
	public UndoAction undo() {
		rot.setFromAxisAngle(axis, (float) -radians);
		rotate();
		return this;
	}

	@Override
	public UndoAction redo() {
		rot.setFromAxisAngle(axis, (float) radians);
		rotate();
		return this;
	}

	@Override
	public String actionName() {
		return "rotate";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
//		System.out.println("isRotating!");
		this.radians += radians;
		rot.setFromAxisAngle(axis, (float) radians);
		rotate();
		return this;
	}

	public void rotate() {
		for (GeosetVertex vertex : selectedVertices) {
			vertex.rotate(center, rot);
		}
		for (IdObject b : selectedIdObjects) {
			b.getPivotPoint().rotate(center, rot);
			float[] bindPose = b.getBindPose();
			if (bindPose != null) {
				bindPose[9] = b.getPivotPoint().x;
				bindPose[10] = b.getPivotPoint().y;
				bindPose[11] = b.getPivotPoint().z;
				if(b.getBindPoseM4() != null){
					b.getBindPoseM4().setFromBindPose(bindPose);
				}
			}
		}

		for (CameraNode node : selectedCameraNodes) {
			node.getPosition().rotate(center, rot);
		}
	}
}
