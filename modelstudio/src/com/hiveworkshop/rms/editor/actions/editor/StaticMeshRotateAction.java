package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class StaticMeshRotateAction extends AbstractTransformAction {
	private final Vec3 center;
	private final Vec3 axis;
	private final Quat rot = new Quat();
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	private double radians;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<CameraNode> selectedCameraNodes;

	public StaticMeshRotateAction(ModelView modelView, Vec3 center, Vec3 axis, double radians) {
		this(modelView.getSelectedVertices(),
				modelView.getSelectedIdObjects(),
				modelView.getSelectedCameraNodes(),
				center, axis, radians);
	}

	public StaticMeshRotateAction(Collection<GeosetVertex> selectedVertices,
	                              Collection<IdObject> selectedIdObjects,
	                              Collection<CameraNode> selectedCameraNodes,
	                              Vec3 center, Vec3 axis, double radians) {
		this(selectedVertices, selectedIdObjects, selectedCameraNodes, center, axis, radians, new Mat4().setIdentity());
	}
	public StaticMeshRotateAction(ModelView modelView, Vec3 center, Vec3 axis, double radians, Mat4 rotMat) {
		this(modelView.getSelectedVertices(),
				modelView.getSelectedIdObjects(),
				modelView.getSelectedCameraNodes(),
				center, axis, radians, rotMat);
	}

	public StaticMeshRotateAction(Collection<GeosetVertex> selectedVertices,
	                              Collection<IdObject> selectedIdObjects,
	                              Collection<CameraNode> selectedCameraNodes,
	                              Vec3 center, Vec3 axis, double radians, Mat4 rotMat) {
		this.center = center;
		this.axis = axis;
		this.radians = radians;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.selectedVertices = new HashSet<>(selectedVertices);
		this.selectedIdObjects = new HashSet<>(selectedIdObjects);
		this.selectedCameraNodes = new HashSet<>(selectedCameraNodes);
	}

	@Override
	public StaticMeshRotateAction undo() {
		updateTransform(-radians);
		return this;
	}

	@Override
	public StaticMeshRotateAction redo() {
		updateTransform(radians);
		return this;
	}

	@Override
	public String actionName() {
		return "Rotate";
	}

	@Override
	public StaticMeshRotateAction updateRotation(double radians) {
		this.radians += radians;
		return updateTransform(radians);
	}

	@Override
	public StaticMeshRotateAction setRotation(double radians) {
		double rotDiff = radians - this.radians;
		this.radians = radians;
		return updateTransform(rotDiff);
	}

	public StaticMeshRotateAction updateTransform(double radians) {
		rot.setFromAxisAngle(axis, (float) -radians).normalize();
		for (GeosetVertex vertex : selectedVertices) {
			vertex
					.sub(center)
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true)
					.add(center);

		}
		for (IdObject b : selectedIdObjects) {
			b.getPivotPoint().sub(center)
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true)
					.add(center);

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
			node.getPosition()
					.sub(center)
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true)
					.add(center);
		}
		return this;
	}
}
