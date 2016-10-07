package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.ArrayList;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class SnapNormalsAction implements UndoAction {
	ArrayList<Vertex> oldSelLocs;
	ArrayList<Vertex> selection;
	Vertex snapPoint;

	public SnapNormalsAction(final ArrayList<Vertex> selection, final ArrayList<Vertex> oldSelLocs,
			final Vertex snapPoint) {
		this.selection = new ArrayList<Vertex>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vertex(snapPoint);
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
		return "snap normals";
	}
}
