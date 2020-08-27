package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

import java.awt.geom.Point2D.Double;

public final class ScaleManipulatorUsesYMouseDrag extends AbstractScaleManipulator {

	public ScaleManipulatorUsesYMouseDrag(final ModelEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
	}

	@Override
	protected final void scaleWithFactor(final ModelEditor modelEditor, final Vec3 center, final double scaleFactor,
			final byte dim1, final byte dim2) {
		getScaleAction().updateScale(scaleFactor, scaleFactor, scaleFactor);
	}

	@Override
	protected double computeScaleFactor(final Double startingClick, final Double endingClick, final Vec3 center,
			final byte dim1, final byte dim2) {
		// TODO not use an override
		// final double dye = Math.abs(endingClick.y - center.getCoord(dim2));
		// final double dys = Math.abs(startingClick.y - center.getCoord(dim2));
		// final double distRatio = dye / dys;
		// return distRatio;
		final double deltaY = endingClick.y - startingClick.y;
		return Math.exp(deltaY / 100.00);

	}

	@Override
	protected Vec3 buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		return new Vec3(scaleFactor, scaleFactor, scaleFactor);
	}

}
