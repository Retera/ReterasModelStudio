package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class SnapNormalsAction implements UndoAction {
	List<Vertex> oldSelLocs;
	List<Vertex> selection;
	Vertex snapPoint;

	public SnapNormalsAction(final List<Vertex> selection, final List<Vertex> oldSelLocs,
			final Vertex snapPoint) {
		this.selection = new ArrayList<Vertex>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vertex(snapPoint);
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
		return "snap normals";
	}
}
