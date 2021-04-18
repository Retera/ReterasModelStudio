package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec3;

public final class ScaleManipulator extends AbstractScaleManipulator {

	public ScaleManipulator(ModelEditor modelEditor, SelectionView selectionView, MoveDimension dir) {
		super(modelEditor, selectionView, dir);
	}

	@Override
	protected final void scaleWithFactor(ModelEditor modelEditor, Vec3 center, double scaleFactor, byte dim1, byte dim2) {
		Vec3 resettableScaleFactors = new Vec3(1, 1, 1);
		if (dir == MoveDimension.XYZ) {
			resettableScaleFactors.set(scaleFactor, scaleFactor, scaleFactor);
		} else {
			if (dir.containDirection(dim1)) {
				resettableScaleFactors.setCoord(dim1, scaleFactor);
			}
			if (dir.containDirection(dim2)) {
				resettableScaleFactors.setCoord(dim2, scaleFactor);
			}
		}
		getScaleAction().updateScale(resettableScaleFactors.x, resettableScaleFactors.y, resettableScaleFactors.z);
	}

	@Override
	protected Vec3 buildScaleVector(double scaleFactor, byte dim1, byte dim2) {
		Vec3 resettableScaleFactors = new Vec3(1, 1, 1);
		if (dir == MoveDimension.XYZ) {
			resettableScaleFactors.set(scaleFactor, scaleFactor, scaleFactor);
		} else {
			if (dir.containDirection(dim1)) {
				resettableScaleFactors.setCoord(dim1, scaleFactor);
			}
			if (dir.containDirection(dim2)) {
				resettableScaleFactors.setCoord(dim2, scaleFactor);
			}
		}
		return resettableScaleFactors;
	}
}
