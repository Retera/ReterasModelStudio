package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vertex2;
import com.hiveworkshop.rms.util.Vertex3;

public final class ScaleXYTVertexManipulator extends AbstractScaleTVertexManipulator {
	private final Vertex3 resettableScaleFactors;

	public ScaleXYTVertexManipulator(final TVertexEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
		this.resettableScaleFactors = new Vertex3(0, 0, 0);
	}

	@Override
	protected final void scaleWithFactor(final TVertexEditor modelEditor, final Vertex2 center,
			final double scaleFactor, final byte dim1, final byte dim2) {
		resettableScaleFactors.x = 1;
		resettableScaleFactors.y = 1;
		resettableScaleFactors.z = 1;
		resettableScaleFactors.setCoord(dim1, scaleFactor);
		resettableScaleFactors.setCoord(dim2, scaleFactor);
		getScaleAction().updateScale(resettableScaleFactors.x, resettableScaleFactors.y, resettableScaleFactors.z);
	}

	@Override
	protected Vertex3 buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		resettableScaleFactors.x = 1;
		resettableScaleFactors.y = 1;
		resettableScaleFactors.z = 1;
		resettableScaleFactors.setCoord(dim1, scaleFactor);
		resettableScaleFactors.setCoord(dim2, scaleFactor);
		return resettableScaleFactors;
	}

}
