package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.ExtrudeAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class ExtrudeActivity extends TransformActivity {
	protected final Vec3 moveVector = new Vec3();
	private GenericMoveAction translationAction;
	private UndoAction beginExtrudingSelection;

	public ExtrudeActivity(ModelHandler modelHandler,
	                       AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new MoverWidget());
	}


	protected void startCoord(CoordinateSystem coordinateSystem) {
		translationAction = modelEditor.beginTranslation();
		if (selectionManager instanceof SelectionManager){
			ModelView modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
			beginExtrudingSelection = new ExtrudeAction(modelView.getSelectedVertices(), new Vec3(0, 0, 0));
			beginExtrudingSelection.redo();
		}
	}

	protected void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));

			buildMoveVector(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			translationAction.updateTranslation(moveVector);
			UndoAction undoAction;
			if(selectionManager instanceof TVertSelectionManager){
				undoAction = translationAction;
			} else {
				ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
				undoAction = new CompoundAction("extrude", changeListener::geosetsUpdated, beginExtrudingSelection, translationAction);
			}

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
		if (selectionManager instanceof SelectionManager) {
			ModelView modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
			beginExtrudingSelection = new ExtrudeAction(modelView.getSelectedVertices(), new Vec3(0, 0, 0));
			beginExtrudingSelection.redo();
		}
	}

	protected void finnishAction(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = getPoint(e);

			buildMoveVector(lastDragPoint, mouseEnd, inverseViewProjectionMatrix);
			translationAction.updateTranslation(moveVector);
			System.out.println("moved from " + mouseStartPoint + " to " + mouseEnd);
			UndoAction undoAction;
			if(selectionManager instanceof TVertSelectionManager){
				undoAction = translationAction;
			} else {
				ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
				undoAction = new CompoundAction("extrude", changeListener::geosetsUpdated, beginExtrudingSelection, translationAction);
			}
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


	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewProjectionMatrix) {
		moveVector.set(0, 0, 0);
		tempVec3.set(mouseStart.x, mouseStart.y, zDepth).transform(viewProjectionMatrix, 1, true);
		moveVector.set(mouseEnd.x, mouseEnd.y, zDepth).transform(viewProjectionMatrix, 1, true);
		moveVector.sub(tempVec3);
	}
}
