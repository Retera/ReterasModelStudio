package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.util.Vec3;

public final class StaticMeshMoveAction implements GenericMoveAction {
	private final ModelView modelView;
	private final Vec3 moveVector;

	public StaticMeshMoveAction(ModelView modelView, Vec3 moveVector) {
		this.modelView = modelView;
		this.moveVector = new Vec3(moveVector);
	}

	@Override
	public void undo() {
		Vec3 antiMove = Vec3.getScaled(moveVector, -1);
		rawTranslate(antiMove);
	}

	@Override
	public void redo() {
		rawTranslate(moveVector);
	}

	public void rawTranslate(Vec3 vec3) {
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.add(vec3);
		}

		for (IdObject b : modelView.getEditableIdObjects()) {
			b.getPivotPoint().add(vec3);
			if (modelView.isSelected(b)) {
				float[] bindPose = b.getBindPose();
				if (bindPose != null) {
					bindPose[9] += vec3.x;
					bindPose[10] += vec3.y;
					bindPose[11] += vec3.z;
				}
			}
		}

		for (Camera camera : modelView.getSelectedCameras()) {
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
	public void updateTranslation(Vec3 delta1) {
		Vec3 delta = new Vec3(delta1);
		moveVector.add(delta);
//		moveVector.x += deltaX;
//		moveVector.y += deltaY;
//		moveVector.z += deltaZ;
//		modelEditor.rawTranslate(delta);
		rawTranslate(delta);
	}

}
