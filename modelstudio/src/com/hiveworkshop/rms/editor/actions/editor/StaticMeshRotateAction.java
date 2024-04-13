package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class StaticMeshRotateAction extends AbstractTransformAction {
	private final Vec3 center;
	private final Vec3 axis;
	private final Quat rot = new Quat();
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	private double radians;
	private final List<GeosetVertex> selectedVertices;
	private final List<IdObject> selectedIdObjects;
	private final List<CameraNode> selectedCameraNodes;
	private final List<Vec3> orgVerticesPos;
	private final List<Vec3> orgIdObjectsPos;
	private final List<Mat4> orgIdObjectsBP;
	private final List<Vec3> orgCameraNodesPos;
	private final List<Mat4> orgCameraNodesBP;

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
		this.selectedVertices = new ArrayList<>(selectedVertices);
		this.selectedIdObjects = new ArrayList<>(selectedIdObjects);
		this.selectedCameraNodes = new ArrayList<>(selectedCameraNodes);

		this.orgVerticesPos = selectedVertices.stream().map(Vec3::new).toList();
		this.orgIdObjectsPos = selectedIdObjects.stream().map(o -> new Vec3(o.getPivotPoint())).toList();
		this.orgIdObjectsBP = selectedIdObjects.stream().map(o -> new Mat4(o.getBindPoseM4())).toList();
		this.orgCameraNodesPos = selectedCameraNodes.stream().map(o -> new Vec3(o.getPivotPoint())).toList();
		this.orgCameraNodesBP = selectedCameraNodes.stream().map(o -> o instanceof CameraNode.SourceNode ? new Mat4(o.getParent().getBindPoseM4()) : o.getParent().getBindPoseM4()).toList();
	}

	@Override
	public StaticMeshRotateAction undo() {
		for (int i = 0; i < selectedVertices.size(); i++) {
			selectedVertices.get(i).set(orgVerticesPos.get(i));
		}

		for (int i = 0; i < selectedIdObjects.size(); i++) {
			selectedIdObjects.get(i).getPivotPoint().set(orgIdObjectsPos.get(i));
			selectedIdObjects.get(i).getBindPoseM4().set(orgIdObjectsBP.get(i));
		}

		for (int i = 0; i < selectedCameraNodes.size(); i++) {
			CameraNode cameraNode = selectedCameraNodes.get(i);
			cameraNode.getPosition().set(orgCameraNodesPos.get(i));
			if (cameraNode instanceof CameraNode.SourceNode) {
				cameraNode.getParent().getBindPoseM4().set(orgCameraNodesBP.get(i));
			}
		}
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
		Vec3 temp = new Vec3();
		for (GeosetVertex vertex : selectedVertices) {
			vertex
					.sub(center)
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true)
					.add(center);
			vertex.getNormal()
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true).normalize();
			if (vertex.getTangent() != null) {
				temp.set(vertex.getTangent())
						.transform(rotMat, 1, true)
						.transform(rot)
						.transform(invRotMat, 1, true).normalize();
				vertex.getTangent().set(temp);
			}

		}

		for (IdObject b : selectedIdObjects) {
			temp.set(b.getPivotPoint());
			b.getPivotPoint().sub(center)
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true)
					.add(center);

			if (b.getBindPoseM4() != null) {
				temp.sub(b.getPivotPoint());
				b.getBindPoseM4().translateScaled(temp, -1f);
			}
		}

		for (CameraNode node : selectedCameraNodes) {
			temp.set(node.getPosition());
			node.getPosition()
					.sub(center)
					.transform(rotMat, 1, true)
					.transform(rot)
					.transform(invRotMat, 1, true)
					.add(center);

			if (node instanceof CameraNode.SourceNode) {
				if (node.getParent().getBindPoseM4() != null) {
					temp.sub(node.getPosition());
					node.getParent().getBindPoseM4().translate(temp);
				}
			}
		}
		return this;
	}
}
