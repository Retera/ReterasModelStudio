package com.hiveworkshop.wc3.gui.modeledit;

import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.TVertex;

/**
 * Undoable snap action.
 * 
 * Eric Theller 6/11/2012
 */
public class UVSnapAction implements UndoAction {
	ArrayList<TVertex> oldSelLocs;
	ArrayList<TVertex> selection;
	TVertex snapPoint;

	public UVSnapAction(final ArrayList<TVertex> selection, final ArrayList<TVertex> oldSelLocs,
			final TVertex snapPoint) {
		this.selection = new ArrayList<TVertex>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new TVertex(snapPoint);
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
			selection.get(i).setTo(snapPoint);
		}
	}

	@Override
	public String actionName() {
		return "snap TVerteces";
	}
}
