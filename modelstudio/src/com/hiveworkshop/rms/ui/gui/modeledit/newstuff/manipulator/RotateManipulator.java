package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public class RotateManipulator extends AbstractRotateManipulator {

	public RotateManipulator(ModelEditor modelEditor, SelectionView selectionView, MoveDimension dir) {
		super(modelEditor, selectionView, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		Vec3 center = selectionView.getCenter();
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

		rotationAction = modelEditor.beginRotation(center, planeDim1, planeDim2);
	}

	protected Vec2 getVec2Center(byte portFirstXYZ, byte portSecondXYZ) {
		return selectionView.getCenter().getProjected(portFirstXYZ, portSecondXYZ);
	}

	protected double getRadius() {
		double radius = selectionView.getCircumscribedSphereRadius(selectionView.getCenter());
		if (radius <= 0) {
			radius = 64;
		}
		return radius;
	}
}
