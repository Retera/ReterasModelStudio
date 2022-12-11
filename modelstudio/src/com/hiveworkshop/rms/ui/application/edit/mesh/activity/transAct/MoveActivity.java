package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class MoveActivity extends TransformActivity {
	protected final Vec3 moveVector = new Vec3();
	protected final Mat4 invRotMat = new Mat4();
	protected final Vec2 realMouseStart = new Vec2();
	protected float lengthX;
	protected float lengthY;

	public MoveActivity(ModelHandler modelHandler,
	                    AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new MoverWidget());
	}

	protected void startMat() {
		invRotMat.set(getRotMat()).invert();
		transformAction = modelEditor.beginTranslation(rotMat);
		realMouseStart.set(mouseStartPoint);

		tempVec3.set(0,0,zDepth).transform(inverseViewProjectionMatrix, 1, true);

		moveVector.set(1, 0, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		lengthX = moveVector.sub(tempVec3).length();
		moveVector.set(0, 1, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		lengthY = moveVector.sub(tempVec3).length();
	}

	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock) {
		transformAction.updateTranslation(buildMoveVector(lastMousePoint, mouseEnd));
	}

	protected Vec3 buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd) {
		moveVector.set(0, 0, 0);
		if (dir.containDim(MoveDimension.X)) {
			moveVector.addScaled(Vec3.X_AXIS, (mouseEnd.x - mouseStart.x)*lengthX);
		}
		if (dir.containDim(MoveDimension.Y)) {
			moveVector.addScaled(Vec3.Y_AXIS, (mouseEnd.y - mouseStart.y)*lengthY);
		}
		return moveVector;
	}
}
