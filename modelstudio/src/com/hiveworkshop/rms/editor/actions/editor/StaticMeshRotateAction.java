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

public final class StaticMeshRotateAction implements GenericRotateAction {
	private final Vec3 center;
	private final Vec3 axis;
	private final Quat rot = new Quat();

	private double radians;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<CameraNode> selectedCameraNodes;

	public StaticMeshRotateAction(ModelView modelView, Vec3 center, byte dim1, byte dim2) {
		this.center = center;
		radians = 0;
		axis = getPerpAxis(dim1, dim2);
		selectedVertices = new HashSet<>(modelView.getSelectedVertices());
		selectedIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		selectedCameraNodes = new HashSet<>(modelView.getSelectedCameraNodes());
	}

	@Override
	public UndoAction undo() {
		rotate(-radians);
		return this;
	}

	@Override
	public UndoAction redo() {
		rotate(radians);
		return this;
	}

	@Override
	public String actionName() {
		return "rotate";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		this.radians += radians;
		rotate(radians);
		return this;
	}

	public void rotate(double radians) {
		rot.setFromAxisAngle(axis, (float) radians);
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

	Vec3 getPerpAxis(byte dim1, byte dim2){
		return new Vec3(getAxis(dim1)).cross(getAxis(dim2));
	}
	private Vec3 getAxis(byte dim) {
		return switch (dim) {
			case 0 -> Vec3.X_AXIS;
			case 1 -> Vec3.Y_AXIS;
			case 2 -> Vec3.Z_AXIS;
			case -1 -> Vec3.NEGATIVE_X_AXIS;
			case -2 -> Vec3.NEGATIVE_Y_AXIS;
			case -3 -> Vec3.NEGATIVE_Z_AXIS;
			default -> Vec3.Z_AXIS;
		};
	}
}
