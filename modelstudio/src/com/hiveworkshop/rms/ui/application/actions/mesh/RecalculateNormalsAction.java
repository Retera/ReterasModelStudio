package com.hiveworkshop.rms.ui.application.actions.mesh;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Vertex;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class RecalculateNormalsAction implements UndoAction {
	List<Vertex> oldSelLocs;
	List<GeosetVertex> selection;
	Vertex snapPoint;

	public RecalculateNormalsAction(final List<GeosetVertex> selection, final List<Vertex> oldSelLocs,
			final Vertex snapPoint) {
		this.selection = new ArrayList<>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vertex(snapPoint);
	}

	@Override
	public void undo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).getNormal().set(oldSelLocs.get(i));
		}
	}

	@Override
	public void redo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).getNormal().set(selection.get(i).createNormal());
		}
	}

	@Override
	public String actionName() {
		return "recalculate normals";
	}
}
