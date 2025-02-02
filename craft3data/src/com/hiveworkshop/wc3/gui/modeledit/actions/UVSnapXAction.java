package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.TVertex;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class UVSnapXAction implements UndoAction {
	private final List<TVertex> oldSelLocs;
	private final List<TVertex> selection;
	private final TVertex snapPoint;
	private final byte dim;

	public UVSnapXAction(final Collection<? extends TVertex> selection, final List<TVertex> oldSelLocs,
			final TVertex snapPoint, final byte dim) {
		this.selection = new ArrayList<TVertex>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new TVertex(snapPoint);
		this.dim = dim;
	}

	@Override
	public void undo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).setTo(oldSelLocs.get(i));
		}
	}

	@Override
	public void redo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).setCoord(dim, snapPoint.getCoord(dim));
		}
	}

	@Override
	public String actionName() {
		if (dim == 0) {
			return "snapX TVertices";
		}
		else if (dim == 1) {
			return "snapY TVertices";
		}
		return "snap TVerteces";
	}
}
