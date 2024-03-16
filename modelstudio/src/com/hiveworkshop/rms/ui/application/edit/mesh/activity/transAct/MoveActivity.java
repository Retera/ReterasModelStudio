package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class MoveActivity extends TransformActivity {
	protected static float smallestSnapPixels = 4f;
	protected static float largestSnapPixels = 100f;
	protected final Vec3 moveVector = new Vec3();
	protected final Mat4 invRotMat = new Mat4();
	protected final Vec2 realMouseStart = new Vec2();
	protected float lengthX;
	protected float lengthY;
	protected float snapSize;

	public MoveActivity(ModelHandler modelHandler,
	                    AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager, new MoverWidget());
	}

	protected void startMat() {
		invRotMat.set(getRotMat()).invert();
		transformAction = modelEditor.beginTranslation(rotMat);
		realMouseStart.set(mouseStartPoint);

		tempVec3.set(0,0, zDepth).transform(inverseViewProjectionMatrix, 1, true);

		moveVector.set(1, 0, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		lengthX = moveVector.sub(tempVec3).length();
		moveVector.set(0, 1, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		lengthY = moveVector.sub(tempVec3).length();

		snapSize = getSnapSize();
	}

	protected float getSnapSize() {
		float vpWidth = lengthX * 2f; // with in units

		float smallestSnapUnits = smallestSnapPixels * sizeAdj;
		float largestSnapUnits = largestSnapPixels * sizeAdj;

		float logVP = (float) Math.log10(vpWidth);
		logVP -= logVP < 1 ? .5f : 0f;
		float snapSize = (float) Math.pow(10f, (int)(logVP-1f));
		if (snapSize < smallestSnapUnits) snapSize *= 10f;
		if (largestSnapUnits < snapSize) snapSize /= 10f;

		return snapSize;
	}

	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock) {
		transformAction.setTranslation(buildMoveVector(realMouseStart, mouseEnd, isSnap));
	}

	protected Vec3 buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, boolean isSnap) {
		moveVector.set(0, 0, 0);
		if (dir.containDim(MoveDimension.X)) {
			float scaleX = (mouseEnd.x - mouseStart.x) * lengthX;
			scaleX = isSnap ? (((int)(scaleX / snapSize + .5f)) * snapSize) : scaleX;
			moveVector.addScaled(Vec3.X_AXIS, scaleX);
		}
		if (dir.containDim(MoveDimension.Y)) {
			float scaleY = (mouseEnd.y - mouseStart.y) * lengthY;
			scaleY = isSnap ? (((int)(scaleY / snapSize + .5f)) * snapSize) : scaleY;
			moveVector.addScaled(Vec3.Y_AXIS, scaleY);
		}
		return moveVector;
	}
}
