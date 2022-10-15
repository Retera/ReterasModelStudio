package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class SquatActivity extends TransformActivity {
	protected GenericRotateAction rotationAction;
	protected double nonRotAngle;
	protected double totRotAngle;

	public SquatActivity(ModelHandler modelHandler,
	                     AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new RotatorWidget());
		widget = new RotatorWidget();
	}

	protected void startCoord(CoordinateSystem coordinateSystem) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();

		Vec3 center = selectionManager.getCenter();
		byte planeDim1;
		byte planeDim2;
		nonRotAngle = 0;
		if (dir != MoveDimension.XYZ && dir.containDirection(dim1)) {
			planeDim1 = getUnusedXYZ(dim1, dim2);
			planeDim2 = dim2;
		} else if (dir != MoveDimension.XYZ && dir.containDirection(dim2)) {
			planeDim1 = dim1;
			planeDim2 = getUnusedXYZ(dim1, dim2);
		} else {
			planeDim1 = dim1;
			planeDim2 = dim2;
		}
		if(selectionManager instanceof TVertSelectionManager){
			rotationAction = modelEditor.beginRotation(center, planeDim1, planeDim2);
		} else {
			rotationAction = modelEditor.beginSquatTool(center, planeDim1, planeDim2);
		}
	}

	protected void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));

			double radians = computeRotateRadians(e, lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			rotationAction.updateRotation(radians);
			nonRotAngle = 0;
			totRotAngle = 0;
			UndoAction undoAction = rotationAction;
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
		double radians = computeRotateRadians(e, lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		rotationAction.updateRotation(radians);
	}

	protected void startMat() {
		Vec3 center = selectionManager.getCenter();
		nonRotAngle = 0;
		Vec3 axis = getAxis();
		if(selectionManager instanceof TVertSelectionManager){
			rotationAction = modelEditor.beginRotation(center, axis);
		} else {
			rotationAction = modelEditor.beginSquatTool(center, axis);
		}
	}

	protected void finnishAction(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj, boolean wasCanceled) {
		if (isActing && rotationAction != null) {
			Vec2 mouseEnd = getPoint(e);

			double radians = computeRotateRadians(e.isControlDown(), lastDragPoint, mouseEnd, viewProjectionMatrix);
			rotationAction.updateRotation(radians);
			nonRotAngle = 0;
			totRotAngle = 0;
			UndoAction undoAction = rotationAction;
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint.set(0,0);
			lastDragPoint.set(0,0);
		}
		isActing = false;
	}

	protected void updateMat(MouseEvent e, Mat4 viewProjectionMatrix, Vec2 mouseEnd) {
		double radians = computeRotateRadians(e.isControlDown(), lastDragPoint, mouseEnd, viewProjectionMatrix);
		rotationAction.updateRotation(radians);
	}
	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock) {
		double radians = computeRotateRadians(isSnap, lastDragPoint, mouseEnd, viewProjectionMatrix);
		rotationAction.updateRotation(radians);
	}



	protected Vec3 getAxis(){
		tempVec3.set(0, 0, -1).transform(inverseViewProjectionMatrix, 1, true);
		Vec3 axis = new Vec3(0, 0, 1).transform(inverseViewProjectionMatrix, 1, true);
		axis.sub(tempVec3).normalize();
		return axis;
	}

	protected Vec2 getVec2Center(byte portFirstXYZ, byte portSecondXYZ) {
		if(selectionManager instanceof TVertSelectionManager){
			return selectionManager.getUVCenter(0);
		} else {
			return selectionManager.getCenter().getProjected(portFirstXYZ, portSecondXYZ);
		}
	}

	protected Vec2 getVec2Center(Mat4 viewProjectionMatrix) {
		if(selectionManager instanceof TVertSelectionManager){
			return selectionManager.getUVCenter(0);
		} else {
//			Vec3 flatCenter = new Vec3();
//			flatCenter.set(selectionManager.getCenter()).transform(viewProjectionMatrix);
//			return new Vec2(flatCenter.y, flatCenter.z);
			return new Vec2().setAsProjection(selectionManager.getCenter(), viewProjectionMatrix);
		}
	}

	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		double radians = computeRotateRadians(e, mouseStart, mouseEnd, dim1, dim2);
		rotationAction.updateRotation(radians);
		nonRotAngle = 0;
		totRotAngle = 0;
		return rotationAction;
	}

	protected double computeRotateRadians(MouseEvent e, Vec2 startingClick, Vec2 endingClick, byte portFirstXYZ, byte portSecondXYZ) {
		double deltaAngle = 0;
		Vec2 center = getVec2Center(portFirstXYZ, portSecondXYZ);
		if (dir == MoveDimension.XYZ) {
			Vec2 startingDelta = Vec2.getDif(startingClick, center);
			Vec2 endingDelta = Vec2.getDif(endingClick, center);

			double startingAngle = Math.atan2(startingDelta.y, startingDelta.x);
			double endingAngle = Math.atan2(endingDelta.y, endingDelta.x);

			deltaAngle = endingAngle - startingAngle;

		} else {
			if (dir.containDirection(portFirstXYZ)) {
				double radius = getRadius();
				deltaAngle = (endingClick.y - startingClick.y) / radius;
			}
			if (dir.containDirection(portSecondXYZ)) {
				double radius = getRadius();
				deltaAngle = (endingClick.x - startingClick.x) / radius;
			}
			if (dir.containDirection(getUnusedXYZ(portFirstXYZ, portSecondXYZ))) {
				Vec2 startingDelta = Vec2.getDif(startingClick, center);
				Vec2 endingDelta = Vec2.getDif(endingClick, center);

				double startingAngle = Math.atan2(startingDelta.y, startingDelta.x);
				double endingAngle = Math.atan2(endingDelta.y, endingDelta.x);

				deltaAngle = endingAngle - startingAngle;
			}
		}
		if (e.isControlDown()) {
			nonRotAngle += deltaAngle;
			deltaAngle = getSnappedAngle(nonRotAngle, 15);
			nonRotAngle -= deltaAngle;
		} else {
			nonRotAngle = 0;
		}
		totRotAngle += deltaAngle;
		return deltaAngle;
	}


	protected double computeRotateRadians(boolean isSnap, Vec2 startingClick, Vec2 endingClick, Mat4 viewProjectionMatrix) {
		double deltaAngle = 0;
		Vec2 center = getViewportSelectionCenter();
		if (dir == MoveDimension.XYZ) {
//			Vec2 startingDelta = Vec2.getDif(startingClick, center);
//			Vec2 endingDelta = Vec2.getDif(endingClick, center);
//
//			double startingAngle = Math.atan2(-startingDelta.y, startingDelta.x);
//			double endingAngle = Math.atan2(-endingDelta.y, endingDelta.x);
//
//			deltaAngle = endingAngle - startingAngle;


			double startingAngle = -getThetaOfDiff(startingClick, center);
			double endingAngle = -getThetaOfDiff(endingClick, center);

			deltaAngle = endingAngle - startingAngle;
		}
		if (isSnap) {
			nonRotAngle += deltaAngle;
			deltaAngle = getSnappedAngle(nonRotAngle, 15);
			nonRotAngle -= deltaAngle;
		} else {
			nonRotAngle = 0;
		}
		totRotAngle += deltaAngle;
		return deltaAngle;
	}

	protected double getSnappedAngle(double angleToSnap, int snapDeg) {
		double angleDeg = Math.toDegrees(angleToSnap);
		int snapAngleDeg = ((int) angleDeg / snapDeg) * snapDeg;
		return Math.toRadians(snapAngleDeg);
	}

	protected double getRadius() {
		double radius = selectionManager.getCircumscribedSphereRadius(selectionManager.getCenter(), 0);
		if (radius <= 0) {
			radius = 64;
		}
		return radius;
	}

	public static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
		if (portFirstXYZ < 0) {
			portFirstXYZ = (byte) (-portFirstXYZ - 1);
		}
		if (portSecondXYZ < 0) {
			portSecondXYZ = (byte) (-portSecondXYZ - 1);
		}
		return (byte) (3 - portFirstXYZ - portSecondXYZ);
	}
}
