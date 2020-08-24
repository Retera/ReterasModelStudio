package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.editor.model.Vertex;

public final class ScaleXTVertexManipulator extends AbstractScaleTVertexManipulator {
	private final Vertex resettableScaleFactors;

	public ScaleXTVertexManipulator(final TVertexEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
		this.resettableScaleFactors = new Vertex(0, 0, 0);
	}

	@Override
	protected final void scaleWithFactor(final TVertexEditor modelEditor, final TVertex center,
			final double scaleFactor, final byte dim1, final byte dim2) {
		resettableScaleFactors.x = 1;
		resettableScaleFactors.y = 1;
		resettableScaleFactors.z = 1;
		resettableScaleFactors.setCoord(dim1, scaleFactor);
		getScaleAction().updateScale(resettableScaleFactors.x, resettableScaleFactors.y, resettableScaleFactors.z);
	}

	@Override
	protected Vertex buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		resettableScaleFactors.x = 1;
		resettableScaleFactors.y = 1;
		resettableScaleFactors.z = 1;
		resettableScaleFactors.setCoord(dim1, scaleFactor);
		return resettableScaleFactors;
	}

}
