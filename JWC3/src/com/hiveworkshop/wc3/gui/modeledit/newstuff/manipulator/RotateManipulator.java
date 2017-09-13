package com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor.RotateAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.Vertex;

public class RotateManipulator extends AbstractManipulator {
	private final ModelEditor modelEditor;
	private final SelectionView selectionView;

	public RotateManipulator(final ModelEditor modelEditor, final SelectionView selectionView) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vertex center = selectionView.getCenter();
		final double radians = computeRotateRadians(mouseStart, mouseEnd, center, dim1, dim2);
		modelEditor.rawRotate2d(center.x, center.y, center.z, radians, dim1, dim2);
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		final Vertex center = selectionView.getCenter();
		final double radians = computeRotateRadians(activityStart, mouseEnd, center, dim1, dim2);
		return new RotateAction(modelEditor, center, radians, dim1, dim2);
	}

	private static double computeRotateRadians(final Double startingClick, final Double endingClick,
			final Vertex center, final byte portFirstXYZ, final byte portSecondXYZ) {
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
