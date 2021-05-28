package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.event.MouseEvent;

public abstract class AbstractRotateManipulator extends Manipulator {
	protected final ModelEditor modelEditor;
	protected final SelectionView selectionView;
	protected GenericRotateAction rotationAction;
	protected MoveDimension dir;
	protected double nonRotAngle;

	public AbstractRotateManipulator(ModelEditor modelEditor, SelectionView selectionView, MoveDimension dir) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
		this.dir = dir;
	}

	@Override
	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		double radians = computeRotateRadians(e, mouseStart, mouseEnd, dim1, dim2);
		rotationAction.updateRotation(radians);
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		update(e, mouseStart, mouseEnd, dim1, dim2);
		nonRotAngle = 0;
		return rotationAction;
	}

	protected double computeRotateRadians(MouseEvent e, Vec2 startingClick, Vec2 endingClick, byte portFirstXYZ, byte portSecondXYZ) {
		double deltaAngle = 0;
		Vec2 center = getVec2Center(portFirstXYZ, portSecondXYZ);
		if (dir == MoveDimension.XYZ) {
			Vec2 startingDelta = Vec2.getDif(startingClick, center);
			Vec2 endingDelta = Vec2.getDif(endingClick, center);

			double startingAngle = Math.atan2(startingDelta.y, startingDelta.x);
			double endingAngle = Math.atan2(endingDelta.y, endingDelta.x);

			deltaAngle = endingAngle - startingAngle;

		} else {
			if (dir.containDirection(portFirstXYZ)) {
				double radius = getRadius();
				deltaAngle = (endingClick.y - startingClick.y) / radius;
			}
			if (dir.containDirection(portSecondXYZ)) {
				double radius = getRadius();
				deltaAngle = (endingClick.x - startingClick.x) / radius;
			}
			if (dir.containDirection(CoordSysUtils.getUnusedXYZ(portFirstXYZ, portSecondXYZ))) {
				Vec2 startingDelta = Vec2.getDif(startingClick, center);
				Vec2 endingDelta = Vec2.getDif(endingClick, center);

				double startingAngle = Math.atan2(startingDelta.y, startingDelta.x);
				double endingAngle = Math.atan2(endingDelta.y, endingDelta.x);

				deltaAngle = endingAngle - startingAngle;
			}
		}
		if (e.isControlDown()) {
			nonRotAngle += deltaAngle;
			deltaAngle = getSnappedAngle(nonRotAngle, 15);
			nonRotAngle -= deltaAngle;
		} else {
//			deltaAngle += nonRotAngle;
			nonRotAngle = 0;
		}
		return deltaAngle;
	}

	protected abstract Vec2 getVec2Center(byte portFirstXYZ, byte portSecondXYZ);

	protected abstract double getRadius();

	protected double getSnappedAngle(double angleToSnap, int snapDeg) {
		double angleDeg = Math.toDegrees(angleToSnap);
		int snapAngleDeg = ((int) angleDeg / snapDeg) * snapDeg;
		return Math.toRadians(snapAngleDeg);
	}

}
