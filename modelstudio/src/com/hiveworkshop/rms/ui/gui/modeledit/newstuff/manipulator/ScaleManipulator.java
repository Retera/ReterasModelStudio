package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public final class ScaleManipulator extends AbstractScaleManipulator {

	public ScaleManipulator(final ModelEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
	}

	@Override
	protected final void scaleWithFactor(final ModelEditor modelEditor, final Vec3 center, final double scaleFactor,
										 final byte dim1, final byte dim2) {
		getScaleAction().updateScale(scaleFactor, scaleFactor, scaleFactor);
	}

	@Override
	protected Vec3 buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		return new Vec3(scaleFactor, scaleFactor, scaleFactor);
	}
}
