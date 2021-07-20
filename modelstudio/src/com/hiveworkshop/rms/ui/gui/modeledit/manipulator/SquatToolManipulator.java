package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class SquatToolManipulator extends AbstractRotateManipulator {

	public SquatToolManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		Vec3 center = selectionManager.getCenter();
		byte planeDim1;
		byte planeDim2;
		nonRotAngle = 0;

		if (dir != MoveDimension.XYZ && dir.containDirection(dim1)) {
			planeDim1 = CoordSysUtils.getUnusedXYZ(dim1, dim2);
			planeDim2 = dim2;
		} else if (dir != MoveDimension.XYZ && dir.containDirection(dim2)) {
			planeDim1 = dim1;
			planeDim2 = CoordSysUtils.getUnusedXYZ(dim1, dim2);
		} else {
			planeDim1 = dim1;
			planeDim2 = dim2;
		}

		rotationAction = modelEditor.beginSquatTool(center, planeDim1, planeDim2);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, CameraHandler cameraHandler) {
		Vec3 center = selectionManager.getCenter();
		byte planeDim1;
		byte planeDim2;
		nonRotAngle = 0;

//		if (dir != MoveDimension.XYZ && dir.containDirection(dim1)) {
//			planeDim1 = CoordSysUtils.getUnusedXYZ(dim1, dim2);
//			planeDim2 = dim2;
//		} else if (dir != MoveDimension.XYZ && dir.containDirection(dim2)) {
//			planeDim1 = dim1;
//			planeDim2 = CoordSysUtils.getUnusedXYZ(dim1, dim2);
//		} else {
//			planeDim1 = dim1;
//			planeDim2 = dim2;
//		}

//		rotationAction = modelEditor.beginSquatTool(center, planeDim1, planeDim2);
		rotationAction = modelEditor.beginSquatTool(center, (byte) 0, (byte) 1);
	}

	protected Vec2 getVec2Center(byte portFirstXYZ, byte portSecondXYZ) {
		return selectionManager.getCenter().getProjected(portFirstXYZ, portSecondXYZ);
	}

	protected double getRadius() {
		double radius = selectionManager.getCircumscribedSphereRadius(selectionManager.getCenter());
		if (radius <= 0) {
			radius = 64;
		}
		return radius;
	}
}
