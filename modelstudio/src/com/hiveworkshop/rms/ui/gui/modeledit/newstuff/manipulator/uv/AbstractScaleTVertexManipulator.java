package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.AbstractManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.geom.Point2D.Double;

public abstract class AbstractScaleTVertexManipulator extends AbstractManipulator {
	private final TVertexEditor modelEditor;
	private final SelectionView selectionView;
	private GenericScaleAction scaleAction;
	MoveDimension dir;
	boolean isNeg = false;

	public AbstractScaleTVertexManipulator(final TVertexEditor modelEditor, final SelectionView selectionView, MoveDimension dir) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
		this.dir = dir;
	}

	@Override
	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(mouseStart, dim1, dim2);
		final Vec2 center = selectionView.getUVCenter(modelEditor.getUVLayerIndex());
		scaleAction = modelEditor.beginScaling(center.x, center.y);
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vec2 center = selectionView.getUVCenter(modelEditor.getUVLayerIndex());
		final double scaleFactor = computeScaleFactor(mouseStart, mouseEnd, center, dim1, dim2);
		scaleWithFactor(modelEditor, center, scaleFactor, dim1, dim2);
	}

	protected final GenericScaleAction getScaleAction() {
		return scaleAction;
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		isNeg = false;
		return scaleAction;
	}

	protected abstract void scaleWithFactor(final TVertexEditor modelEditor, final Vec2 center, final double scaleFactor, byte dim1, byte dim2);

	protected abstract Vec3 buildScaleVector(final double scaleFactor, byte dim1, byte dim2);


	protected double computeScaleFactor(final Double startingClick, final Double endingClick, final Vec2 center,
	                                    final byte dim1, final byte dim2) {
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
				flipNeg = getFlipNeg(-dyEnd);
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
