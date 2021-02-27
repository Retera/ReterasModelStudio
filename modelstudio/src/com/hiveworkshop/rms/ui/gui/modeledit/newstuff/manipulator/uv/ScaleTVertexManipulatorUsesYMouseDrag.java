package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.geom.Point2D.Double;

public final class ScaleTVertexManipulatorUsesYMouseDrag extends AbstractScaleTVertexManipulator {

	public ScaleTVertexManipulatorUsesYMouseDrag(final TVertexEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
	}

	@Override
	protected final void scaleWithFactor(final TVertexEditor modelEditor, final Vec2 center,
			final double scaleFactor, final byte dim1, final byte dim2) {
		getScaleAction().updateScale(scaleFactor, scaleFactor, scaleFactor);
	}

	@Override
	protected double computeScaleFactor(final Double startingClick, final Double endingClick, final Vec2 center,
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
