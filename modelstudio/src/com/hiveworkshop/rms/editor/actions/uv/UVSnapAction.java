package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class UVSnapAction implements UndoAction {
	private final List<Vec2> oldSelLocs;
	private final List<Vec2> selection;
	private final Vec2 snapPoint;

	public UVSnapAction(Collection<Vec2> selection, List<Vec2> oldSelLocs, Vec2 snapPoint) {
		this.selection = new ArrayList<>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vec2(snapPoint);
	}

	@Override
	public UndoAction undo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).set(oldSelLocs.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec2 vec2 : selection) {
			vec2.set(snapPoint);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "snap TVerteces";
	}
}
