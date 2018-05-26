package com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.Vertex;

public abstract class AbstractScaleManipulator extends AbstractManipulator {
	private final ModelEditor modelEditor;
	private final SelectionView selectionView;
	private GenericScaleAction scaleAction;

	public AbstractScaleManipulator(final ModelEditor modelEditor, final SelectionView selectionView) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
	}

	@Override
	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(mouseStart, dim1, dim2);
		final Vertex center = selectionView.getCenter();
		scaleAction = modelEditor.beginScaling(center.x, center.y, center.z);
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vertex center = selectionView.getCenter();
		final double scaleFactor = computeScaleFactor(mouseStart, mouseEnd, center, dim1, dim2);
		scaleWithFactor(modelEditor, center, scaleFactor, dim1, dim2);
	}

	protected final GenericScaleAction getScaleAction() {
		return scaleAction;
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		return scaleAction;
	}

	protected abstract void scaleWithFactor(final ModelEditor modelEditor, final Vertex center,
			final double scaleFactor, byte dim1, byte dim2);

	protected abstract Vertex buildScaleVector(final double scaleFactor, byte dim1, byte dim2);

	protected double computeScaleFactor(final Double startingClick, final Double endingClick, final Vertex center,
			final byte dim1, final byte dim2) {
		double dxs = endingClick.x - center.getCoord(dim1);
		double dys = endingClick.y - center.getCoord(dim2);
		final double endDist = Math.sqrt(dxs * dxs + dys * dys);
		dxs = startingClick.x - center.getCoord(dim1);
		dys = startingClick.y - center.getCoord(dim2);
		final double startDist = Math.sqrt(dxs * dxs + dys * dys);
		final double distRatio = endDist / startDist;
		return distRatio;
	}

}
