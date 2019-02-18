package com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.AbstractManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexSelectionView;
import com.hiveworkshop.wc3.mdl.TVertex;

public class RotateManipulator extends AbstractManipulator {
	private final TVertexEditor modelEditor;
	private final TVertexSelectionView selectionView;
	private GenericRotateAction rotationAction;

	public RotateManipulator(final TVertexEditor modelEditor, final TVertexSelectionView selectionView) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
	}

	@Override
	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(mouseStart, dim1, dim2);
		final TVertex center = selectionView.getCenter();
		rotationAction = modelEditor.beginRotation(center.x, center.y, dim1, dim2);
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final TVertex center = selectionView.getCenter();
		final double radians = computeRotateRadians(mouseStart, mouseEnd, center, dim1, dim2);
		rotationAction.updateRotation(radians);
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		return rotationAction;
	}

	private static double computeRotateRadians(final Double startingClick, final Double endingClick,
			final TVertex center, final byte portFirstXYZ, final byte portSecondXYZ) {
		final double startingDeltaX = startingClick.x - center.getCoord(portFirstXYZ);
		final double startingDeltaY = startingClick.y - center.getCoord(portSecondXYZ);
		final double endingDeltaX = endingClick.x - center.getCoord(portFirstXYZ);
		final double endingDeltaY = endingClick.y - center.getCoord(portSecondXYZ);
		final double startingAngle = Math.atan2(startingDeltaY, startingDeltaX);
		final double endingAngle = Math.atan2(endingDeltaY, endingDeltaX);
		final double deltaAngle = endingAngle - startingAngle;
		return deltaAngle;
	}

}
