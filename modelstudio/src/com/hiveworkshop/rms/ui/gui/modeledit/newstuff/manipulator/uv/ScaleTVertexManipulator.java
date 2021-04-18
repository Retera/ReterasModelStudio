package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public final class ScaleTVertexManipulator extends AbstractScaleTVertexManipulator {

	public ScaleTVertexManipulator(TVertexEditor modelEditor, SelectionView selectionView, MoveDimension dir) {
		super(modelEditor, selectionView, dir);
	}

	@Override
	protected final void scaleWithFactor(TVertexEditor modelEditor, Vec2 center, double scaleFactor, byte dim1, byte dim2) {
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
