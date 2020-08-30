package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec2;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class UVSnapAction implements UndoAction {
	private final List<Vec2> oldSelLocs;
	private final List<Vec2> selection;
	private final Vec2 snapPoint;

	public UVSnapAction(final Collection<? extends Vec2> selection, final List<Vec2> oldSelLocs,
			final Vec2 snapPoint) {
		this.selection = new ArrayList<>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vec2(snapPoint);
	}

	@Override
	public void undo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).set(oldSelLocs.get(i));
		}
	}

	@Override
	public void redo() {
		for (Vec2 vec2 : selection) {
			vec2.set(snapPoint);
		}
	}

	@Override
	public String actionName() {
		return "snap TVerteces";
	}
}
