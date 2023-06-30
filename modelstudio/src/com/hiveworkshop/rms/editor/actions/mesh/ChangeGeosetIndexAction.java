package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeGeosetIndexAction implements UndoAction {
	private final EditableModel model;
	private final Geoset geoset;
	private final int newIndex;
	private final int oldIndex;
	private final ModelStructureChangeListener changeListener;

	public ChangeGeosetIndexAction(EditableModel model, Geoset geoset, int newIndex, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.geoset = geoset;
		this.newIndex = newIndex;
		this.oldIndex = model.getGeosetId(geoset);
		this.changeListener = changeListener;
	}

	@Override
	public ChangeGeosetIndexAction undo() {
		model.remove(geoset);
		model.add(geoset, oldIndex);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public ChangeGeosetIndexAction redo() {
		model.remove(geoset);
		model.add(geoset, newIndex);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Geoset Order";
	}
}
