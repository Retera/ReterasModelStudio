package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class StaticMeshMoveAction extends AbstractTransformAction {
	private final UndoAction setupAction;
	private final Vec3 moveVector;
	private final List<GeosetVertex> selectedVertices;
	private final List<IdObject> selectedIdObjects;
	private final List<CameraNode> selectedCameraNodes;
	private final List<Vec3> orgVerticesPos;
	private final List<Vec3> orgIdObjectsPos;
	private final List<Mat4> orgIdObjectsBP;
	private final List<Vec3> orgCameraNodesPos;
	private final List<Mat4> orgCameraNodesBP;
	private final String actionName;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final Vec3 deltaTranslate = new Vec3();

	public StaticMeshMoveAction(ModelView modelView, Vec3 moveVector, Mat4 rotMat) {
		this(modelView, moveVector, "Move", null, rotMat);
	}
	public StaticMeshMoveAction(ModelView modelView, Vec3 moveVector, String actionName, UndoAction setupAction, Mat4 rotMat) {
		this(modelView.getSelectedVertices(),
				modelView.getSelectedIdObjects(),
				modelView.getSelectedCameraNodes(),
				moveVector, actionName, setupAction, rotMat);
	}
	public StaticMeshMoveAction(Collection<GeosetVertex> selectedVertices,
	                            Collection<IdObject> selectedIdObjects,
	                            Collection<CameraNode> selectedCameraNodes,
	                            Vec3 moveVector, String actionName, UndoAction setupAction, Mat4 rotMat) {
		this.actionName = actionName;
		this.setupAction = setupAction;
		this.moveVector = new Vec3(moveVector);
		this.selectedVertices = new ArrayList<>(selectedVertices);
		this.selectedIdObjects = new ArrayList<>(selectedIdObjects);
		this.selectedCameraNodes = new ArrayList<>(selectedCameraNodes);
		this.orgVerticesPos = selectedVertices.stream().map(Vec3::new).toList();
		this.orgIdObjectsPos = selectedIdObjects.stream().map(o -> new Vec3(o.getPivotPoint())).toList();
		this.orgIdObjectsBP = selectedIdObjects.stream().map(o -> new Mat4(o.getBindPoseM4())).toList();
		this.orgCameraNodesPos = selectedCameraNodes.stream().map(o -> new Vec3(o.getPivotPoint())).toList();
		this.orgCameraNodesBP = selectedCameraNodes.stream().map(o -> o instanceof CameraNode.SourceNode ? new Mat4(o.getParent().getBindPoseM4()) : o.getParent().getBindPoseM4()).toList();
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
	}

	public StaticMeshMoveAction doSetup() {
		if (setupAction != null) {
			setupAction.redo();
		}
		return this;
	}
	@Override
	public StaticMeshMoveAction undo() {
		if (setupAction != null) {
			setupAction.undo();
		}

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
	public StaticMeshMoveAction redo() {
		updateTransform(moveVector);
		if (setupAction != null) {
			setupAction.redo();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}

	@Override
	public StaticMeshMoveAction updateTranslation(Vec3 delta) {
		moveVector.add(delta);
		updateTransform(moveVector);
		return this;
	}

	@Override
	public StaticMeshMoveAction setTranslation(Vec3 transl) {
		deltaTranslate.set(transl).sub(moveVector);
		moveVector.set(transl);
		updateTransform(moveVector);
		return this;
	}

	private StaticMeshMoveAction updateTransform(Vec3 totMove) {
		for (int i = 0; i < selectedVertices.size(); i++) {
			selectedVertices.get(i)
					.set(orgVerticesPos.get(i))
					.transform(rotMat, 1, true)
					.add(totMove)
					.transform(invRotMat, 1, true);
		}

		for (int i = 0; i < selectedIdObjects.size(); i++) {
			selectedIdObjects.get(i).getPivotPoint()
					.set(orgIdObjectsPos.get(i))
					.transform(rotMat, 1, true)
					.add(totMove)
					.transform(invRotMat, 1, true);

			selectedIdObjects.get(i).getBindPoseM4()
					.set(orgIdObjectsBP.get(i))
					.mul(rotMat)
					.translate(totMove)
					.mul(invRotMat);// todo check if this is correct...
		}

		for (int i = 0; i < selectedCameraNodes.size(); i++) {
			CameraNode cameraNode = selectedCameraNodes.get(i);
			cameraNode.getPosition()
					.set(orgCameraNodesPos.get(i))
					.transform(rotMat, 1, true)
					.add(totMove)
					.transform(invRotMat, 1, true);
			if (cameraNode instanceof CameraNode.SourceNode) {
				cameraNode.getParent().getBindPoseM4()
						.set(orgCameraNodesBP.get(i))
						.mul(rotMat)
						.translate(totMove)
						.mul(invRotMat);// todo check if this is correct...
			}
		}
		return this;
	}
	private StaticMeshMoveAction updateTransform1(Vec3 delta) {
		for (GeosetVertex vertex : selectedVertices) {
			vertex
					.transform(rotMat, 1, true)
					.add(delta)
					.transform(invRotMat, 1, true);
		}

		for (IdObject b : selectedIdObjects) {
			b.getPivotPoint()
					.transform(rotMat, 1, true)
					.add(delta)
					.transform(invRotMat, 1, true);

			b.getBindPoseM4().mul(rotMat)
					.translate(delta)
					.mul(invRotMat);// todo check if this is correct...
		}

		for (CameraNode cameraNode : selectedCameraNodes) {
			cameraNode.getPosition()
					.transform(rotMat, 1, true)
					.add(delta)
					.transform(invRotMat, 1, true);
			if (cameraNode instanceof CameraNode.SourceNode) {
				if (cameraNode.getParent().getBindPoseM4() != null) {

					cameraNode.getParent().getBindPoseM4().mul(rotMat)
							.translate(delta)
							.mul(invRotMat);// todo check if this is correct...
				}
			}
		}
		return this;
	}

}
