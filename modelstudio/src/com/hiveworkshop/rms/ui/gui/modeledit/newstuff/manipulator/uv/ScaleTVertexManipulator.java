package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vertex2;
import com.hiveworkshop.rms.util.Vertex3;

public final class ScaleTVertexManipulator extends AbstractScaleTVertexManipulator {

	public ScaleTVertexManipulator(final TVertexEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
	}

	@Override
	protected final void scaleWithFactor(final TVertexEditor modelEditor, final Vertex2 center,
			final double scaleFactor, final byte dim1, final byte dim2) {
		getScaleAction().updateScale(scaleFactor, scaleFactor, scaleFactor);
	}

	@Override
	protected Vertex3 buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		return new Vertex3(scaleFactor, scaleFactor, scaleFactor);
	}
}
