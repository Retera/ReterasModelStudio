package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public final class ScaleXYManipulator extends AbstractScaleManipulator {
	private final Vec3 resettableScaleFactors;

	public ScaleXYManipulator(final ModelEditor modelEditor, final SelectionView selectionView) {
		super(modelEditor, selectionView);
		resettableScaleFactors = new Vec3(0, 0, 0);
	}

	@Override
	protected final void scaleWithFactor(final ModelEditor modelEditor, final Vec3 center, final double scaleFactor,
			final byte dim1, final byte dim2) {
		resettableScaleFactors.x = 1;
		resettableScaleFactors.y = 1;
		resettableScaleFactors.z = 1;
		resettableScaleFactors.setCoord(dim1, scaleFactor);
		resettableScaleFactors.setCoord(dim2, scaleFactor);
		getScaleAction().updateScale(resettableScaleFactors.x, resettableScaleFactors.y, resettableScaleFactors.z);
	}

	@Override
	protected Vec3 buildScaleVector(final double scaleFactor, final byte dim1, final byte dim2) {
		resettableScaleFactors.x = 1;
		resettableScaleFactors.y = 1;
		resettableScaleFactors.z = 1;
		resettableScaleFactors.setCoord(dim1, scaleFactor);
		resettableScaleFactors.setCoord(dim2, scaleFactor);
		return resettableScaleFactors;
	}

}
