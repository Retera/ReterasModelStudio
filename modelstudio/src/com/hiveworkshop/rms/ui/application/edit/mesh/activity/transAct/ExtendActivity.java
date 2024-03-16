package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class ExtendActivity extends MoveActivity {

	public ExtendActivity(ModelHandler modelHandler,
	                      AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	protected void startMat() {
		invRotMat.set(getRotMat()).invert();
		transformAction = modelEditor.beginExtend(rotMat);
		realMouseStart.set(mouseStartPoint);

		tempVec3.set(0,0,zDepth).transform(inverseViewProjectionMatrix, 1, true);

		moveVector.set(1, 0, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		lengthX = moveVector.sub(tempVec3).length();
		moveVector.set(0, 1, zDepth).transform(inverseViewProjectionMatrix, 1, true);
		lengthY = moveVector.sub(tempVec3).length();

		snapSize = getSnapSize();
	}

}
