package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.ScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;
import com.hiveworkshop.wc3.mdl.Vertex;

public abstract class AbstractBetterScaleActivityListener extends AbstractBetterActivityListener {
	private final ModelEditor modelEditor;
	private final UndoManager undoManager;
	private final SelectionView selectionView;

	public AbstractBetterScaleActivityListener(final ModelEditor modelEditor, final UndoManager undoManager,
			final SelectionView selectionView) {
		this.modelEditor = modelEditor;
		this.undoManager = undoManager;
		this.selectionView = selectionView;
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final Vertex center = selectionView.getCenter();
		final double scaleFactor = computeScaleFactor(mouseStart, mouseEnd, center, dim1, dim2);
		scaleWithFactor(modelEditor, center, scaleFactor, dim1, dim2);
	}

	@Override
	public void finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		final Vertex center = selectionView.getCenter();
		final double scaleFactor = computeScaleFactor(activityStart, mouseEnd, center, dim1, dim2);
		undoManager.pushAction(new ScaleAction(modelEditor, center, scaleFactor, scaleFactor, scaleFactor));
	}

	protected abstract void scaleWithFactor(final ModelEditor modelEditor, final Vertex center,
			final double scaleFactor, byte dim1, byte dim2);

	private double computeScaleFactor(final Double startingClick, final Double endingClick, final Vertex center,
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
