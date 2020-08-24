package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public final class ScaleXManipulator extends AbstractScaleManipulator {
	private final Vertex resettableScaleFactors;

	public ScaleXManipulator(final ModelEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
		resettableScaleFactors = new Vertex(0, 0, 0);
	}

	@Override
	protected final void scaleWithFactor(final ModelEditor modelEditor, final Vertex center, final double scaleFactor,
			final byte dim1, final byte dim2) {
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
