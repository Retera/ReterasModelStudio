package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vertex2;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class UVSnapAction implements UndoAction {
	private final List<Vertex2> oldSelLocs;
	private final List<Vertex2> selection;
	private final Vertex2 snapPoint;

	public UVSnapAction(final Collection<? extends Vertex2> selection, final List<Vertex2> oldSelLocs,
			final Vertex2 snapPoint) {
		this.selection = new ArrayList<Vertex2>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vertex2(snapPoint);
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
