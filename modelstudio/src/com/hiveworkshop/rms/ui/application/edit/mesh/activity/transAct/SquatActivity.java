package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Vec3;

public class SquatActivity extends RotateActivity {
	public SquatActivity(ModelHandler modelHandler,
	                     AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	protected void startMat() {
		invRotMat.set(getRotMat()).invert();

		Vec3 selCenter = selectionManager.getCenter();
		center.set(selCenter).transform(viewProjectionMatrix, 1, true);
		realMouseStart.set(mouseStartPoint);
		radius = tempVec2.set(mouseStartPoint.x, mouseStartPoint.y).translate(center.x, center.y).length();
		realTotRotAngle = 0;
		Vec3 axis = getAxis();

		transformAction = modelEditor.beginSquatTool(selCenter, axis, rotMat);
	}
}
