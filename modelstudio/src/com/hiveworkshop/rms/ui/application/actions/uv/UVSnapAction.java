package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vector2;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class UVSnapAction implements UndoAction {
	private final List<Vector2> oldSelLocs;
	private final List<Vector2> selection;
	private final Vector2 snapPoint;

	public UVSnapAction(final Collection<? extends Vector2> selection, final List<Vector2> oldSelLocs,
			final Vector2 snapPoint) {
		this.selection = new ArrayList<Vector2>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vector2(snapPoint);
	}

	@Override
	public void undo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).set(oldSelLocs.get(i));
		}
	}

	@Override
	public void redo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).set(snapPoint);
		}
	}

	@Override
	public String actionName() {
		return "snap TVerteces";
	}
}
