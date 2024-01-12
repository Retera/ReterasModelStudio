package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class RotateActivity extends TransformActivity {
	protected double realTotRotAngle;
	protected double totRotAngle;
	protected final Vec2 realMouseStart = new Vec2();
	protected final Vec3 center = new Vec3();
	protected final Mat4 invRotMat = new Mat4();
	protected double radius = 0;

	public RotateActivity(ModelHandler modelHandler,
	                      AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new RotatorWidget());
	}

	protected void startMat() {
		invRotMat.set(getRotMat()).invert();

		Vec3 selCenter = selectionManager.getCenter();
		center.set(selCenter).transform(viewProjectionMatrix, 1, true);
		realMouseStart.set(mouseStartPoint);
		radius = tempVec2.set(mouseStartPoint.x, mouseStartPoint.y).translate(center.x, center.y).length();
		realTotRotAngle = 0;
		Vec3 axis = getAxis();
		transformAction = modelEditor.beginRotation(selCenter, axis, rotMat);
	}

	protected Vec3 getAxis() {
		Vec3 axis = new Vec3();
		if (dir.containDim(MoveDimension.Z)) {
			axis.set(Vec3.Z_AXIS);
		} else if (dir.containDim(MoveDimension.X)) {
			axis.set(Vec3.X_AXIS);
		} else if (dir.containDim(MoveDimension.Y)) {
			axis.set(Vec3.Y_AXIS);
		} else {
			axis.set(Vec3.Z_AXIS);
		}
		return axis;
	}

	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock) {
		double radians = computeRotateRadians(isSnap, lastMousePoint, mouseEnd);
		transformAction.setRotation(radians);
	}

	protected void resetActivity() {
		totRotAngle = 0;
		realTotRotAngle = 0;
	}

	protected double computeRotateRadians(boolean isSnap, Vec2 mouseStart, Vec2 mouseEnd) {
		double deltaAngle = 0;

		if (dir.containDim(MoveDimension.Z)) {
			double startingAngle = -getThetaOfDiff(mouseStart, center);
			double endingAngle = -getThetaOfDiff(mouseEnd, center);

			deltaAngle = endingAngle - startingAngle;
		} else if (dir.containDim(MoveDimension.X)) {
			double dyEnd = mouseEnd.y - center.y;
			double dyStart = mouseStart.y - center.y;
			deltaAngle = (dyEnd - dyStart) / radius;
		} else if (dir.containDim(MoveDimension.Y)) {
			double dxEnd = mouseEnd.x - center.x;
			double dxStart = mouseStart.x - center.x;
			deltaAngle = (dxEnd - dxStart) / radius;
		}
		return getAngle(deltaAngle, isSnap);
	}


	protected double getAngle(double deltaAngle, boolean isSnap) {
		realTotRotAngle += deltaAngle;
		if (isSnap) {
			totRotAngle = Math.toRadians(((int) (Math.toDegrees(realTotRotAngle) / 15.0)) * 15);
		} else {
			totRotAngle = realTotRotAngle;
		}
		return totRotAngle;
	}
}
