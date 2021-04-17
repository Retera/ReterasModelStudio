package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;

public abstract class AbstractScaleManipulator extends Manipulator {
	private final ModelEditor modelEditor;
	private final SelectionView selectionView;
	private GenericScaleAction scaleAction;
	MoveDimension dir;
	boolean isNeg = false;

	public AbstractScaleManipulator(final ModelEditor modelEditor, final SelectionView selectionView, MoveDimension dir) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
		this.dir = dir;
	}

	@Override
	protected void onStart(MouseEvent e, final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(e, mouseStart, dim1, dim2);
		final Vec3 center = selectionView.getCenter();
		scaleAction = modelEditor.beginScaling(center.x, center.y, center.z);
	}

	@Override
	public void update(MouseEvent e, final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vec3 center = selectionView.getCenter();
		final double scaleFactor = computeScaleFactor(mouseStart, mouseEnd, center, dim1, dim2);
		scaleWithFactor(modelEditor, center, scaleFactor, dim1, dim2);
	}

	protected final GenericScaleAction getScaleAction() {
		return scaleAction;
	}

	@Override
	public UndoAction finish(MouseEvent e, final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(e, mouseStart, mouseEnd, dim1, dim2);
		isNeg = false;
		return scaleAction;
	}

	protected abstract void scaleWithFactor(final ModelEditor modelEditor, final Vec3 center, final double scaleFactor, byte dim1, byte dim2);

	protected abstract Vec3 buildScaleVector(final double scaleFactor, byte dim1, byte dim2);

	protected double computeScaleFactor(final Double startingClick, final Double endingClick, final Vec3 center, final byte dim1, final byte dim2) {
		double dxEnd = 0;
		double dyEnd = 0;
		double dxStart = 0;
		double dyStart = 0;
		int flipNeg = 1;

		if (dir.containDirection(dim1)) {
			dxEnd = endingClick.x - center.getCoord(dim1);
			dxStart = startingClick.x - center.getCoord(dim1);
			flipNeg = getFlipNeg(dxEnd);
		}
		if (dir.containDirection(dim2)) {
			dyEnd = endingClick.y - center.getCoord(dim2);
			dyStart = startingClick.y - center.getCoord(dim2);
			if (!dir.containDirection(dim1)) {
				// up is -y
//				flipNeg = getFlipNeg(-dyEnd);
				flipNeg = getFlipNeg(dyEnd);
			}
		}
		final double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
		final double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));

		return flipNeg * endDist / startDist;
	}

	private int getFlipNeg(double dEnd) {
		int flipNeg;
		flipNeg = (!isNeg && dEnd < 0) || (isNeg && dEnd > 0) ? -1 : 1;
		isNeg = (flipNeg < 0) != isNeg;
		return flipNeg;
	}

}
