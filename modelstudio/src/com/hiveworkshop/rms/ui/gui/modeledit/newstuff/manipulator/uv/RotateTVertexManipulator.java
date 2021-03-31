package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;

public class RotateTVertexManipulator extends Manipulator {
	private final TVertexEditor modelEditor;
	private final SelectionView selectionView;
	private GenericRotateAction rotationAction;
	MoveDimension dir;

	public RotateTVertexManipulator(final TVertexEditor modelEditor, final SelectionView selectionView, MoveDimension dir) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
		this.dir = dir;
	}

	@Override
	protected void onStart(MouseEvent e, final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(e, mouseStart, dim1, dim2);
		final Vec2 center = selectionView.getUVCenter(modelEditor.getUVLayerIndex());
		byte planeDim1;
		byte planeDim2;

		if (dir.containDirection(dim1)) {
			planeDim1 = CoordinateSystem.Util.getUnusedXYZ(dim1, dim2);
			planeDim2 = dim2;
		} else if (dir.containDirection(dim2)) {
			planeDim1 = dim1;
			planeDim2 = CoordinateSystem.Util.getUnusedXYZ(dim1, dim2);
		} else {
			planeDim1 = dim1;
			planeDim2 = dim2;
		}
		rotationAction = modelEditor.beginRotation(center.x, center.y, planeDim1, planeDim2);
	}

	@Override
	public void update(MouseEvent e, final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vec2 center = selectionView.getUVCenter(modelEditor.getUVLayerIndex());
		final double radians = computeRotateRadians(mouseStart, mouseEnd, center, dim1, dim2);
		rotationAction.updateRotation(radians);
	}

	@Override
	public UndoAction finish(MouseEvent e, final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(e, mouseStart, mouseEnd, dim1, dim2);
		return rotationAction;
	}

	private double computeRotateRadians(final Double startingClick, final Double endingClick, final Vec2 center, final byte portFirstXYZ, final byte portSecondXYZ) {
		double deltaAngle = 0;
		if (dir == MoveDimension.XYZ) {
			final double startingDeltaX = startingClick.x - center.getCoord(portFirstXYZ);
			final double startingDeltaY = startingClick.y - center.getCoord(portSecondXYZ);
			final double endingDeltaX = endingClick.x - center.getCoord(portFirstXYZ);
			final double endingDeltaY = endingClick.y - center.getCoord(portSecondXYZ);
			final double startingAngle = Math.atan2(startingDeltaY, startingDeltaX);
			final double endingAngle = Math.atan2(endingDeltaY, endingDeltaX);
			deltaAngle = endingAngle - startingAngle;

		} else {
			if (dir.containDirection(portFirstXYZ)) {
				double radius = selectionView.getCircumscribedSphereRadius(center, modelEditor.getUVLayerIndex());
				if (radius <= 0) {
					radius = 64;
				}
				deltaAngle = (endingClick.y - startingClick.y) / radius;
			}
			if (dir.containDirection(portSecondXYZ)) {
				double radius = selectionView.getCircumscribedSphereRadius(center, modelEditor.getUVLayerIndex());
				if (radius <= 0) {
					radius = 64;
				}
				deltaAngle = (endingClick.x - startingClick.x) / radius;
			}
			if (dir.containDirection(CoordinateSystem.Util.getUnusedXYZ(portFirstXYZ, portSecondXYZ))) {
				final double startingDeltaX = startingClick.x - center.getCoord(portFirstXYZ);
				final double startingDeltaY = startingClick.y - center.getCoord(portSecondXYZ);
				final double endingDeltaX = endingClick.x - center.getCoord(portFirstXYZ);
				final double endingDeltaY = endingClick.y - center.getCoord(portSecondXYZ);
				final double startingAngle = Math.atan2(startingDeltaY, startingDeltaX);
				final double endingAngle = Math.atan2(endingDeltaY, endingDeltaX);
				deltaAngle = endingAngle - startingAngle;
			}
		}
		return deltaAngle;
	}

}
