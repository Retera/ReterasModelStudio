package com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexSelectionView;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class ScaleTVertexManipulatorUsesYMouseDrag extends AbstractScaleTVertexManipulator {

	public ScaleTVertexManipulatorUsesYMouseDrag(final TVertexEditor modelEditor,
			final TVertexSelectionView selectionView) {
		super(modelEditor, selectionView);
	}

	@Override
	protected final void scaleWithFactor(final TVertexEditor modelEditor, final TVertex center,
			final double scaleFactor, final byte dim1, final byte dim2) {
		getScaleAction().updateScale(scaleFactor, scaleFactor, scaleFactor);
	}

	@Override
	protected double computeScaleFactor(final Double startingClick, final Double endingClick, final TVertex center,
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
	protected Vertex buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		return new Vertex(scaleFactor, scaleFactor, scaleFactor);
	}

}
