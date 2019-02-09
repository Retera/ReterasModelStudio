package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.TVertex;

public final class MirrorTVerticesAction implements UndoAction {
	private final char[] DIMENSION_NAMES = { 'X', 'Y' };
	private final List<TVertex> selection;
	private final byte mirrorDim;
	private final double centerX;
	private final double centerY;

	public MirrorTVerticesAction(final Collection<? extends TVertex> selection, final byte mirrorDim,
			final double centerX, final double centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.selection = new ArrayList<>(selection);
		this.mirrorDim = mirrorDim;
	}

	@Override
	public void undo() {
		doMirror();
	}

	@Override
	public void redo() {
		doMirror();
	}

	private void doMirror() {
		final TVertex center = new TVertex(centerX, centerY);
		for (final TVertex vert : selection) {
			vert.setCoord(mirrorDim, 2 * center.getCoord(mirrorDim) - vert.getCoord(mirrorDim));
		}
	}

	@Override
	public String actionName() {
		return "mirror UV " + DIMENSION_NAMES[mirrorDim];
	}

}
