package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class StaticMeshMoveAction extends AbstractTransformAction {
	private final UndoAction setupAction;
	private final Vec3 moveVector;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<CameraNode> selectedCameraNodes;
	private final String actionName;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

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
		this.selectedVertices = new HashSet<>(selectedVertices);
		this.selectedIdObjects = new HashSet<>(selectedIdObjects);
		this.selectedCameraNodes = new HashSet<>(selectedCameraNodes);
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
	}

	public StaticMeshMoveAction doSetup(){
		if(setupAction != null){
			setupAction.redo();
		}
		return this;
	}
	@Override
	public StaticMeshMoveAction undo() {
		if(setupAction != null){
			setupAction.undo();
		}
		Vec3 antiMove = Vec3.getScaled(moveVector, -1);
		updateTransform(antiMove);
		return this;
	}

	@Override
	public StaticMeshMoveAction redo() {
		updateTransform(moveVector);
		if(setupAction != null){
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
		updateTransform(delta);
		return this;
	}

	private StaticMeshMoveAction updateTransform(Vec3 vec3) {
		for (GeosetVertex vertex : selectedVertices) {
			vertex
					.transform(rotMat, 1, true)
					.add(vec3)
					.transform(invRotMat, 1, true);
		}

		for (IdObject b : selectedIdObjects) {
			b.getPivotPoint()
					.transform(rotMat, 1, true)
					.add(vec3)
					.transform(invRotMat, 1, true);
			float[] bindPose = b.getBindPose();
			if (bindPose != null) {
				bindPose[ 9] = b.getPivotPoint().x;
				bindPose[10] = b.getPivotPoint().y;
				bindPose[11] = b.getPivotPoint().z;
//				bindPose[9] += vec3.x;
//				bindPose[10] += vec3.y;
//				bindPose[11] += vec3.z;
				if (b.getBindPoseM4() != null){
					b.getBindPoseM4().translate(vec3); // todo check if this is correct...
				}
			}
		}

		for (CameraNode cameraNode : selectedCameraNodes) {
			cameraNode.getPosition()
					.transform(rotMat, 1, true)
					.add(vec3)
					.transform(invRotMat, 1, true);
			if(cameraNode instanceof CameraNode.SourceNode){
				float[] bindPose = cameraNode.getParent().getBindPose();

				if (bindPose != null) {
					bindPose[ 9] = cameraNode.getPivotPoint().x;
					bindPose[10] = cameraNode.getPivotPoint().y;
					bindPose[11] = cameraNode.getPivotPoint().z;
//					if (cameraNode.getBindPoseM4() != null){
//						cameraNode.getBindPoseM4().translate(vec3); // todo check if this is correct...
//					}
				}
			}
		}
		return this;
	}

}
