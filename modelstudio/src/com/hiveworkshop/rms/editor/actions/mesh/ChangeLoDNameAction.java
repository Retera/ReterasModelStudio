package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;


public class ChangeLoDNameAction implements UndoAction {
	private final String newName;
	private final String oldName;
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;


	public ChangeLoDNameAction(String newName, Geoset geoset, ModelStructureChangeListener changeListener) {
		this.newName = newName;
		this.oldName = geoset.getLevelOfDetailName();
		this.geoset = geoset;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction redo() {
		geoset.setLevelOfDetailName(newName);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		geoset.setLevelOfDetailName(oldName);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Change Geoset Name to \"" + newName + "\"";
	}
}
