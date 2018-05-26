package com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.Vertex;

public class RotateHorizontalManipulator extends AbstractManipulator {
	private final ModelEditor modelEditor;
	private final SelectionView selectionView;
	private GenericRotateAction rotationAction;

	public RotateHorizontalManipulator(final ModelEditor modelEditor, final SelectionView selectionView) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
	}

	@Override
	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(mouseStart, dim1, dim2);
		final Vertex center = selectionView.getCenter();
		rotationAction = modelEditor.beginRotation(center.x, center.y, center.z, dim1,
				CoordinateSystem.Util.getUnusedXYZ(dim1, dim2));
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vertex center = selectionView.getCenter();
		final double radians = computeRotateRadians(mouseStart, mouseEnd, center, dim1, dim2);
		rotationAction.updateRotation(radians);
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		return rotationAction;
	}

	private double computeRotateRadians(final Double startingClick, final Double endingClick, final Vertex center,
			final byte portFirstXYZ, final byte portSecondXYZ) {
		double radius = selectionView.getCircumscribedSphereRadius(center);
		if (radius <= 0) {
			radius = 0.1;
		}
		final double deltaAngle = (endingClick.x - startingClick.x) / radius;
		return deltaAngle;
	}

}
