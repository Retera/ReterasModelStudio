package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class RotateManipulator extends AbstractRotateManipulator {

	public RotateManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
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

		rotationAction = modelEditor.beginRotation(center, planeDim1, planeDim2);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, CameraHandler cameraHandler) {
		Vec3 center = selectionManager.getCenter();
		nonRotAngle = 0;
		Vec3 axis = new Vec3(Vec3.X_AXIS);
		axis.transform(cameraHandler.getViewPortAntiRotMat2());

		rotationAction = modelEditor.beginRotation(center, axis);
	}

	protected Vec2 getVec2Center(byte portFirstXYZ, byte portSecondXYZ) {
		return selectionManager.getCenter().getProjected(portFirstXYZ, portSecondXYZ);
	}

	protected Vec2 getVec2Center(CameraHandler cameraHandler) {
		Vec3 flatCenter = new Vec3();
		flatCenter.set(selectionManager.getCenter()).transform(cameraHandler.getViewPortAntiRotMat());
		return new Vec2(flatCenter.y, flatCenter.z);
	}
}
