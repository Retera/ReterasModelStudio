package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public abstract class AbstractMoveManipulator extends Manipulator {
	protected final ModelEditor modelEditor;
	protected final Vec3 moveVector;
	private GenericMoveAction translationAction;
	protected MoveDimension dir;

	public AbstractMoveManipulator(ModelEditor modelEditor, MoveDimension dir) {
		this.modelEditor = modelEditor;
		moveVector = new Vec3(0, 0, 0);
		this.dir = dir;
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		resetMoveVector();
		translationAction = modelEditor.beginTranslation();
	}

	@Override
	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		resetMoveVector();
		buildMoveVector(mouseStart, mouseEnd, dim1, dim2);
		translationAction.updateTranslation(moveVector);
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		update(e, mouseStart, mouseEnd, dim1, dim2);
		resetMoveVector();
		return translationAction;
	}

	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		if (dir.containDirection(dim1)) {
			moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
		}
		if (dir.containDirection(dim2)) {
			moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
		}
	}


	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, CameraHandler cameraHandler) {
		resetMoveVector();
		translationAction = modelEditor.beginTranslation();
	}

	@Override
	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat) {
		resetMoveVector();
		buildMoveVector(mouseStart, mouseEnd, viewPortAntiRotMat);
		translationAction.updateTranslation(moveVector);
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat, double sizeAdj) {
		update(e, mouseStart, mouseEnd, viewPortAntiRotMat);
		resetMoveVector();
		return translationAction;
	}

	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat) {
		moveVector.y = (mouseEnd.x - mouseStart.x);
		moveVector.z = (mouseEnd.y - mouseStart.y);

		moveVector.transform(viewPortAntiRotMat);

	}


	private void resetMoveVector() {
		moveVector.set(0, 0, 0);
	}
}
