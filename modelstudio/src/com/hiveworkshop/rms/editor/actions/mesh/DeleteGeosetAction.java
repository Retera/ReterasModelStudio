package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collections;
import java.util.List;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class DeleteGeosetAction implements UndoAction {
	private final List<Geoset> geosets;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public DeleteGeosetAction(EditableModel model, Geoset geoset, ModelStructureChangeListener changeListener) {
		this(model, Collections.singletonList(geoset), changeListener);
	}

	public DeleteGeosetAction(EditableModel model, List<Geoset> geosets, ModelStructureChangeListener changeListener) {
		this.geosets = geosets;
		this.model = model;
		this.changeListener = changeListener;
		actionName = "Delete "
				+ (geosets.size() == 1 ? "Geoset" : (geosets.size() + " Geosets"));
	}

	@Override
	public UndoAction redo() {
		for (Geoset geoset : geosets) {
			model.remove(geoset);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : geosets) {
			model.add(geoset);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
