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
		resetMoveVector();
		translationAction = modelEditor.beginTranslation();
	}


	protected void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));

			resetMoveVector();
			buildMoveVector(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			translationAction.updateTranslation(moveVector);
			resetMoveVector();

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
		resetMoveVector();
		buildMoveVector(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		translationAction.updateTranslation(moveVector);
	}

	protected void startMat() {
		resetMoveVector();
		translationAction = modelEditor.beginTranslation();
	}

	protected void finnishAction(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = getPoint(e);

			resetMoveVector();
			buildMoveVector(lastDragPoint, mouseEnd, inverseViewProjectionMatrix);
//			buildMoveVector(lastDragPoint, mouseEnd, viewProjectionMatrix);
			translationAction.updateTranslation(moveVector);
			resetMoveVector();
			System.out.println("moved from " + mouseStartPoint + " to " + mouseEnd);
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
		resetMoveVector();
		buildMoveVector(lastDragPoint, mouseEnd, viewProjectionMatrix);
		translationAction.updateTranslation(moveVector);
	}

	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
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
//
//			heapStart.set(mouseEnd.x, mouseEnd.y, 0).transform(inverseViewProjectionMatrix, 1, true);
//			heapEnd.set(mouseStart.x, mouseStart.y, 0).transform(inverseViewProjectionMatrix, 1, true);
//			heapDiff.set(heapEnd).sub(heapStart);
//			System.out.println("MidPlane");
//			System.out.println("p1: " + heapStart);
//			System.out.println("p2: " + heapEnd);
//			System.out.println("d:  " + heapDiff);
//
//
//			heapStart.set(mouseEnd.x, mouseEnd.y, 1).transform(inverseViewProjectionMatrix, 1, true);
//			heapEnd.set(mouseStart.x, mouseStart.y, 1).transform(inverseViewProjectionMatrix, 1, true);
//			heapDiff.set(heapEnd).sub(heapStart);
//			System.out.println("FarPlane");
//			System.out.println("p1: " + heapStart);
//			System.out.println("p2: " + heapEnd);
//			System.out.println("d:  " + heapDiff);
//
//
//			float zPlane = vZero.z;
//			heapStart.set(mouseEnd.x, mouseEnd.y, zPlane).transform(inverseViewProjectionMatrix, 1, true);
//			heapEnd.set(mouseStart.x, mouseStart.y, zPlane).transform(inverseViewProjectionMatrix, 1, true);
//			heapDiff.set(heapEnd).sub(heapStart);
//			System.out.println("vZeroPlane");
//			System.out.println("p1: " + heapStart);
//			System.out.println("p2: " + heapEnd);
//			System.out.println("d:  " + heapDiff);
//
//			time = System.currentTimeMillis() + 500;
//		}


//		heap.set(mouseStart.x, mouseStart.y, zDepth).transform(viewProjectionMatrix, 1, true);
//		moveVector.set(mouseEnd.x, mouseEnd.y, zDepth).transform(viewProjectionMatrix, 1, true);
//		moveVector.sub(heap);
		heap.set(mouseStart.x, mouseStart.y, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		moveVector.set(mouseEnd.x, mouseEnd.y, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		moveVector.sub(heap);
	}

	private void resetMoveVector() {
		moveVector.set(0, 0, 0);
	}
}
