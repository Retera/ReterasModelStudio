package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class ScaleActivity extends TransformActivity {
	private final Vec2 mouseStartPoint = new Vec2();
	private final Vec2 lastDragPoint = new Vec2();
	protected Widget widget;
	protected MoveDimension dir;
	protected final Mat4 inverseViewProjectionMatrix = new Mat4();
	protected final Vec3 scaleVector = new Vec3(1,1,1);
	protected GenericScaleAction scaleAction;
	protected boolean isNeg = false;
	protected boolean isActing = false;

	public ScaleActivity(ModelHandler modelHandler,
	                     AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new ScalerWidget());
		widget = new ScalerWidget();
	}

	protected void startCoord(CoordinateSystem coordinateSystem) {
		Vec3 center;
		if(selectionManager instanceof TVertSelectionManager){
			center = new Vec3().setCoords(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ(), selectionManager.getUVCenter(0));
		} else {
			center = selectionManager.getCenter();
		}
		resetScaleVector();
		scaleAction = modelEditor.beginScaling(center);
	}


	protected void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));

			resetScaleVector();
			buildScaleVector(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			scaleAction.updateScale(scaleVector);

			resetScaleVector();
			isNeg = false;
			UndoAction undoAction = scaleAction;
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
		resetScaleVector();
		buildScaleVector(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		scaleAction.updateScale(scaleVector);
	}

	protected void startMat() {
		Vec3 center;
		if(selectionManager instanceof TVertSelectionManager){
			center = new Vec3().setCoords((byte) 0, (byte) 1, selectionManager.getUVCenter(0));
		} else {
			center = selectionManager.getCenter();
		}
		resetScaleVector();
		scaleAction = modelEditor.beginScaling(center);
	}

	protected void finnishAction(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = getPoint(e);

			resetScaleVector();
			buildScaleVector(lastDragPoint, mouseEnd, viewProjectionMatrix);
			scaleAction.updateScale(scaleVector);
			resetScaleVector();
			isNeg = false;

			UndoAction undoAction = scaleAction;
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
		resetScaleVector();
		buildScaleVector(lastDragPoint, mouseEnd, viewProjectionMatrix);
		scaleAction.updateScale(scaleVector);
	}

	protected final void buildScaleVector(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		double scaleFactor = computeScaleFactor(mouseStart, mouseEnd, dim1, dim2);
		if (dir == MoveDimension.XYZ) {
			scaleVector.set(scaleFactor, scaleFactor, scaleFactor);
		} else {
			if (dir.containDirection(dim1)) {
				scaleVector.setCoord(dim1, scaleFactor);
			}
			if (dir.containDirection(dim2)) {
				scaleVector.setCoord(dim2, scaleFactor);
			}
		}
	}

	protected final void buildScaleVector(Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewProjectionMatrix) {
		double scaleFactor = computeScaleFactor(mouseStart, mouseEnd, viewProjectionMatrix);
		if (dir == MoveDimension.XYZ) {
			scaleVector.set(scaleFactor, scaleFactor, scaleFactor);
		} else {
//			if (dir.containDirection(dim1)) {
//				scaleVector.setCoord(dim1, scaleFactor);
//			}
//			if (dir.containDirection(dim2)) {
//				scaleVector.setCoord(dim2, scaleFactor);
//			}
		}
	}


	protected int getFlipNeg(double dEnd) {
		int flipNeg;
		flipNeg = (!isNeg && dEnd < 0) || (isNeg && dEnd > 0) ? -1 : 1;
		isNeg = (flipNeg < 0) != isNeg;
		return flipNeg;
	}

	protected void resetScaleVector() {
		scaleVector.set(1, 1, 1);
	}

	protected double computeScaleFactor(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		System.out.println("computeScaleFactor!");
		Vec2 center;
		if(selectionManager instanceof TVertSelectionManager){
			center = selectionManager.getUVCenter(0);
		} else {
			center = selectionManager.getCenter().getProjected(dim1,dim2);
		}
		double dxEnd = 0;
		double dyEnd = 0;
		double dxStart = 0;
		double dyStart = 0;
		int flipNeg = 1;

		if (dir.containDirection(dim1)) {
			dxEnd = mouseEnd.x - center.x;
			dxStart = mouseStart.x - center.x;
			flipNeg = getFlipNeg(dxEnd);
		}
		if (dir.containDirection(dim2)) {
			dyEnd = mouseEnd.y - center.y;
			dyStart = mouseStart.y - center.y;
			if (!dir.containDirection(dim1)) {
				// up is -y
				if (selectionManager instanceof TVertSelectionManager){
					flipNeg = getFlipNeg(-dyEnd);
				} else {
					flipNeg = getFlipNeg(dyEnd);
				}
			}
		}
		double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
		double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));

		return flipNeg * endDist / startDist;
	}

	protected double computeScaleFactor(Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewProjectionMatrix) {
		System.out.println("computeScaleFactor!");
//		Vec3 center = selectionManager.getCenter().transform(cameraHandler.getViewPortAntiRotMat());
		Vec2 center = selectionManager.getCenter().transform(viewProjectionMatrix).getProjected((byte) 1, (byte) 2);
		double dxEnd = 0;
		double dyEnd = 0;
		double dxStart = 0;
		double dyStart = 0;
		int flipNeg = 1;

		Vec2 dStart = new Vec2(mouseStart).sub(center);
		Vec2 dEnd = new Vec2(mouseEnd).sub(center);

//		if (dir.containDirection(dim1)) {
//			dxEnd = mouseEnd.x - center.getCoord(dim1);
//			dxStart = mouseStart.x - center.getCoord(dim1);
//			flipNeg = getFlipNeg(dxEnd);
//		}
//		if (dir.containDirection(dim2)) {
//			dyEnd = mouseEnd.y - center.getCoord(dim2);
//			dyStart = mouseStart.y - center.getCoord(dim2);
//			if (!dir.containDirection(dim1)) {
//				// up is -y
////				flipNeg = getFlipNeg(-dyEnd);
//				flipNeg = getFlipNeg(dyEnd);
//			}
//		}
//		double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
//		double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));
		double endDist = dEnd.length();
		double startDist = dStart.length();

		return flipNeg * endDist / startDist;
	}


}
