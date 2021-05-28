package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public final class ScaleManipulator extends AbstractScaleManipulator {

	public ScaleManipulator(ModelEditor modelEditor, SelectionView selectionView, MoveDimension dir) {
		super(modelEditor, selectionView, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		Vec3 center = selectionView.getCenter();
		resetScaleVector();
		scaleAction = modelEditor.beginScaling(center);
	}

	protected double computeScaleFactor(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		Vec3 center = selectionView.getCenter();
		double dxEnd = 0;
		double dyEnd = 0;
		double dxStart = 0;
		double dyStart = 0;
		int flipNeg = 1;

		if (dir.containDirection(dim1)) {
			dxEnd = mouseEnd.x - center.getCoord(dim1);
			dxStart = mouseStart.x - center.getCoord(dim1);
			flipNeg = getFlipNeg(dxEnd);
		}
		if (dir.containDirection(dim2)) {
			dyEnd = mouseEnd.y - center.getCoord(dim2);
			dyStart = mouseStart.y - center.getCoord(dim2);
			if (!dir.containDirection(dim1)) {
				// up is -y
//				flipNeg = getFlipNeg(-dyEnd);
				flipNeg = getFlipNeg(dyEnd);
			}
		}
		double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
		double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));

		return flipNeg * endDist / startDist;
	}
}
