package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class MoveActivity extends TransformActivity {
	protected final Vec3 moveVector = new Vec3();
	private GenericMoveAction translationAction;

	public MoveActivity(ModelHandler modelHandler,
	                    AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new MoverWidget());
	}

	protected void startCoord(CoordinateSystem coordinateSystem) {
		translationAction = modelEditor.beginTranslation();
	}


	protected void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));

			buildMoveVector(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			translationAction.updateTranslation(moveVector);

			UndoAction undoAction = translationAction;
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint.set(0,0);
			lastDragPoint.set(0,0);
			isActing = false;
		}
	}

	protected void updateCoord(MouseEvent e, CoordinateSystem coordinateSystem, Vec2 mouseEnd) {
		buildMoveVector(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		translationAction.updateTranslation(moveVector);
	}

	protected void startMat() {
		translationAction = modelEditor.beginTranslation();
	}

	protected void finnishAction(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = getPoint(e);

			buildMoveVector(lastDragPoint, mouseEnd, inverseViewProjectionMatrix);
			translationAction.updateTranslation(moveVector);

			System.out.println("moved from " + mouseStartPoint + " to " + mouseEnd + ", canceled: " + wasCanceled + ", e: " + e);
			UndoAction undoAction = translationAction;
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint.set(0,0);
			lastDragPoint.set(0,0);
			isActing = false;
		}
	}

	protected void updateMat(MouseEvent e, Mat4 viewProjectionMatrix, Vec2 mouseEnd) {
		buildMoveVector(lastDragPoint, mouseEnd, viewProjectionMatrix);
		translationAction.updateTranslation(moveVector);
	}
	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock) {
		buildMoveVector(lastDragPoint, mouseEnd, viewProjectionMatrix);
		translationAction.updateTranslation(moveVector);
	}

	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		moveVector.set(0, 0, 0);
		if (dir.containDirection(dim1)) {
			moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
		}
		if (dir.containDirection(dim2)) {
			moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
		}
	}

	long time = 0;
	//	Vec2 heap = new Vec2();
	Vec2 v2Heap = new Vec2();
	Vec3 heapStart = new Vec3();
	Vec3 heapEnd = new Vec3();
	Vec3 heapDiff = new Vec3();
	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewProjectionMatrix) {
		moveVector.set(0, 0, 0);

//		if(time < System.currentTimeMillis()){
//			System.out.println("vZero: " + vZero);
//
////			v2Heap.setAsProjection(vZero, viewProjectionMatrix);
//
//
//			heapStart.set(mouseEnd.x, mouseEnd.y, -1).transform(inverseViewProjectionMatrix, 1, true);
//			heapEnd.set(mouseStart.x, mouseStart.y, -1).transform(inverseViewProjectionMatrix, 1, true);
//			heapDiff.set(heapEnd).sub(heapStart);
//			System.out.println("NearPlane");
//			System.out.println("p1: " + heapStart);
//			System.out.println("p2: " + heapEnd);
//			System.out.println("d:  " + heapDiff);
//
//			time = System.currentTimeMillis() + 500;
//		}

		tempVec3.set(mouseStart.x, mouseStart.y, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		moveVector.set(mouseEnd.x, mouseEnd.y, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		moveVector.sub(tempVec3);
	}
}
