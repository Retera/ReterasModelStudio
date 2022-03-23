package com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.AbstractRotateManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class RotateTVertexManipulator extends AbstractRotateManipulator {

	public RotateTVertexManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		Vec3 center = new Vec3().setCoords(dim1, dim2, selectionManager.getUVCenter(0));
		byte planeDim1;
		byte planeDim2;
		nonRotAngle = 0;

		if (dir.containDirection(dim1)) {
			planeDim1 = getUnusedXYZ(dim1, dim2);
			planeDim2 = dim2;
		} else if (dir.containDirection(dim2)) {
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

//
//		byte planeDim1;
//		byte planeDim2;
		nonRotAngle = 0;

//		if (dir.containDirection(dim1)) {
//			planeDim1 = CoordSysUtils.getUnusedXYZ(dim1, dim2);
//			planeDim2 = dim2;
//		} else if (dir.containDirection(dim2)) {
//			planeDim1 = dim1;
//			planeDim2 = CoordSysUtils.getUnusedXYZ(dim1, dim2);
//		} else {
//			planeDim1 = dim1;
//			planeDim2 = dim2;
//		}
		rotationAction = modelEditor.beginRotation(center, axis);
	}

	protected Vec2 getVec2Center(byte portFirstXYZ, byte portSecondXYZ) {
		return selectionManager.getUVCenter(0);
	}

	protected double getRadius() {
//		double radius = selectionView.getCircumscribedSphereRadius(center, modelEditor.getUVLayerIndex());
		double radius = selectionManager.getCircumscribedSphereRadius(selectionManager.getUVCenter(0), 0);
		if (radius <= 0) {
			radius = 64;
		}
		return radius;
	}
}