package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class ScaleActivity extends TransformActivity {
	protected final Vec3 scaleVector = new Vec3(1,1,1);
	protected boolean isNeg = false;
	protected final Vec2 realMouseStart = new Vec2();
	protected final Vec3 center = new Vec3();
	protected final Mat4 invRotMat = new Mat4();
	public ScaleActivity(ModelHandler modelHandler,
	                     AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new ScalerWidget());
	}

	protected void startMat() {
		invRotMat.set(getRotMat()).invert();
		Vec3 selCenter = selectionManager.getCenter();
		transformAction = modelEditor.beginScaling(selCenter, rotMat);
		realMouseStart.set(mouseStartPoint);
		center.set(selCenter).transform(viewProjectionMatrix, 1, true);
	}

	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock) {
		buildScaleVector(lastMousePoint, mouseEnd);
		transformAction.setScale(scaleVector);
	}

	protected void finnishAction() {
		isNeg = false;
	}

	protected double computeScaleFactor(Vec2 mouseStart, Vec2 mouseEnd) {
		double dxEnd = 0;
		double dyEnd = 0;

		double dxStart = 0;
		double dyStart = 0;

		if (dir.containDim(MoveDimension.X)) {
			dxEnd = mouseEnd.x - center.x;
			dxStart = mouseStart.x - center.x;
		}
		if (dir.containDim(MoveDimension.Y)) {
			dyEnd = mouseEnd.y - center.y;
			dyStart = mouseStart.y - center.y;
		}

		double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
		double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));

		int flipNeg = (int) Math.copySign(1, (dxEnd*dxStart) + (dyEnd*dyStart));

		return flipNeg * (endDist / startDist);
	}

	protected final void buildScaleVector(Vec2 mouseStart, Vec2 mouseEnd) {
		scaleVector.set(0,0,0);

		double scaleFactor = computeScaleFactor(realMouseStart, mouseEnd);
		if (dir.containDim(MoveDimension.X)) {
			scaleVector.addScaled(Vec3.X_AXIS, (float) (scaleFactor));
		} else {
			scaleVector.add(Vec3.X_AXIS);
		}
		if (dir.containDim(MoveDimension.Y)) {
			scaleVector.addScaled(Vec3.Y_AXIS, (float) scaleFactor);
		} else {
			scaleVector.add(Vec3.Y_AXIS);
		}
		if (dir.containDim(MoveDimension.Z)) {
			scaleVector.addScaled(Vec3.Z_AXIS, (float) scaleFactor);
		} else {
			scaleVector.add(Vec3.Z_AXIS);
		}
	}
}
