package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.RotateAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;
import com.hiveworkshop.wc3.mdl.Vertex;

public class BetterRotateHorizontalActivityListener extends AbstractBetterActivityListener {
	private final ModelEditor modelEditor;
	private final UndoManager undoManager;
	private final SelectionView selectionView;

	public BetterRotateHorizontalActivityListener(final ModelEditor modelEditor, final UndoManager undoManager,
			final SelectionView selectionView) {
		this.modelEditor = modelEditor;
		this.undoManager = undoManager;
		this.selectionView = selectionView;
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vertex center = selectionView.getCenter();
		final double radians = computeRotateRadians(mouseStart, mouseEnd, center, dim1, dim2);
		modelEditor.rotate2d(center.x, center.y, center.z, radians, dim1,
				CoordinateSystem.Util.getUnusedXYZ(dim1, dim2));
	}

	@Override
	public void finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		final Vertex center = selectionView.getCenter();
		final double radians = computeRotateRadians(activityStart, mouseEnd, center, dim1, dim2);
		undoManager.pushAction(
				new RotateAction(modelEditor, center, radians, dim1, CoordinateSystem.Util.getUnusedXYZ(dim1, dim2)));
	}

	private double computeRotateRadians(final Double startingClick, final Double endingClick, final Vertex center,
			final byte portFirstXYZ, final byte portSecondXYZ) {
		final double radius = selectionView.getCircumscribedSphereRadius(center);
		final double deltaAngle = (endingClick.x - startingClick.x) / radius;
		return deltaAngle;
	}

}
