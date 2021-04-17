package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class DeleteGeosetAction implements UndoAction {
	private final Geoset geoset;
	private final EditableModel model;
	private final GeosetAnim geosetAnim;

	public DeleteGeosetAction(final Geoset geoset) {
		this.geoset = geoset;
		model = geoset.getParentModel();
		geosetAnim = geoset.getGeosetAnim();

	}

	@Override
	public void redo() {
		model.remove(geoset);
		if (geosetAnim != null) {
			model.remove(geosetAnim);
		}
	}

	@Override
	public void undo() {
		model.add(geoset);
		if (geosetAnim != null) {
			model.add(geosetAnim);
		}
	}

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
