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

	@Override
	public StaticMeshMoveAction setTranslation(Vec3 transl) {
		deltaTranslate.set(transl).sub(moveVector);
		moveVector.set(transl);
		updateTransform(deltaTranslate);
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

			b.getBindPoseM4().mul(rotMat)
					.translate(vec3)
					.mul(invRotMat);// todo check if this is correct...
		}

		for (CameraNode cameraNode : selectedCameraNodes) {
			cameraNode.getPosition()
					.transform(rotMat, 1, true)
					.add(vec3)
					.transform(invRotMat, 1, true);
			if(cameraNode instanceof CameraNode.SourceNode){
				if (cameraNode.getParent().getBindPoseM4() != null) {

					cameraNode.getParent().getBindPoseM4().mul(rotMat)
							.translate(vec3)
							.mul(invRotMat);// todo check if this is correct...
				}
			}
		}
		return this;
	}

}
