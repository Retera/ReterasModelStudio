package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Vec3;

import java.util.HashSet;
import java.util.Set;

public final class StaticMeshMoveAction implements GenericMoveAction {
	private final ModelView modelView;
	private final Vec3 moveVector;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<Camera> selectedCameras;

	public StaticMeshMoveAction(ModelView modelView, Vec3 moveVector) {
		this.modelView = modelView;
		this.moveVector = new Vec3(moveVector);
		selectedVertices = new HashSet<>(modelView.getSelectedVertices());
		selectedIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		selectedCameras = new HashSet<>(modelView.getSelectedCameras());
	}

	@Override
	public UndoAction undo() {
		Vec3 antiMove = Vec3.getScaled(moveVector, -1);
		rawTranslate(antiMove);
		return this;
	}

	@Override
	public UndoAction redo() {
		rawTranslate(moveVector);
		return this;
	}

	public void rawTranslate(Vec3 vec3) {
		for (GeosetVertex vertex : selectedVertices) {
			vertex.add(vec3);
		}

		for (IdObject b : selectedIdObjects) {
			b.getPivotPoint().add(vec3);
				float[] bindPose = b.getBindPose();
				if (bindPose != null) {
					bindPose[9] += vec3.x;
					bindPose[10] += vec3.y;
					bindPose[11] += vec3.z;
				}
		}

		for (Camera camera : selectedCameras) {
			camera.getPosition().add(vec3);
		}
	}

	@Override
	public String actionName() {
		return "move";
	}

	@Override
	public void updateTranslation(double deltaX, double deltaY, double deltaZ) {
		Vec3 delta = new Vec3(deltaX, deltaY, deltaZ);
		moveVector.add(delta);
//		moveVector.x += deltaX;
//		moveVector.y += deltaY;
//		moveVector.z += deltaZ;
//		modelEditor.rawTranslate(delta);
		rawTranslate(delta);
	}

	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
		moveVector.add(delta);
//		moveVector.x += deltaX;
//		moveVector.y += deltaY;
//		moveVector.z += deltaZ;
//		modelEditor.rawTranslate(delta);
		rawTranslate(delta);
		return this;
	}

}
