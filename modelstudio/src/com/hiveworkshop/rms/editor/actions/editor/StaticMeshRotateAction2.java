package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class StaticMeshRotateAction2 implements GenericRotateAction {
	private final Vec3 center;
	private final Vec3 axis;
	private final Quat rot = new Quat();
	private double radians;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<CameraNode> selectedCameraNodes;


	public StaticMeshRotateAction2(ModelView modelView, Vec3 center, Vec3 axis, double radians) {
		this(modelView.getSelectedVertices(),
				modelView.getSelectedIdObjects(),
				modelView.getSelectedCameraNodes(), center, axis, radians);
	}

	public StaticMeshRotateAction2(Collection<GeosetVertex> selectedVertices,
	                               Collection<IdObject> selectedIdObjects,
	                               Collection<CameraNode> selectedCameraNodes,
	                               Vec3 center, Vec3 axis, double radians) {
		this.center = center;
		this.axis = axis;
		this.radians = radians;
		this.selectedVertices = new HashSet<>(selectedVertices);
		this.selectedIdObjects = new HashSet<>(selectedIdObjects);
		this.selectedCameraNodes = new HashSet<>(selectedCameraNodes);
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
}
