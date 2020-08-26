package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vertex2;
import com.hiveworkshop.rms.util.Vertex3;

public final class ScaleTVertexManipulatorUsesYMouseDrag extends AbstractScaleTVertexManipulator {

	public ScaleTVertexManipulatorUsesYMouseDrag(final TVertexEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
	}

	@Override
	protected final void scaleWithFactor(final TVertexEditor modelEditor, final Vertex2 center,
			final double scaleFactor, final byte dim1, final byte dim2) {
		getScaleAction().updateScale(scaleFactor, scaleFactor, scaleFactor);
	}

	@Override
	protected double computeScaleFactor(final Double startingClick, final Double endingClick, final Vertex2 center,
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
	protected Vertex3 buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		return new Vertex3(scaleFactor, scaleFactor, scaleFactor);
	}

}
