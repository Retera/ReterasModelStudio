package com.hiveworkshop.rms.ui.application.actions.mesh;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class SnapNormalsAction implements UndoAction {
	List<Vec3> oldSelLocs;
	List<Vec3> selection;
	Vec3 snapPoint;

	public SnapNormalsAction(final List<Vec3> selection, final List<Vec3> oldSelLocs,
			final Vec3 snapPoint) {
		this.selection = new ArrayList<>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vec3(snapPoint);
	}

	@Override
	public void undo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).set(oldSelLocs.get(i));
		}
	}

	@Override
	public void redo() {
		for (Vec3 vec3 : selection) {
			vec3.set(snapPoint);
		}
	}

	@Override
	public String actionName() {
		return "snap normals";
	}
}
