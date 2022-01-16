package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;


public class ChangeLoDAction implements UndoAction {
	private final int newLoD;
	private final int oldLoD;
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;


	public ChangeLoDAction(int newLoD, Geoset geoset, ModelStructureChangeListener changeListener) {
		this.newLoD = newLoD;
		this.oldLoD = geoset.getLevelOfDetail();
		this.geoset = geoset;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction redo() {
		geoset.setLevelOfDetail(newLoD);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		geoset.setLevelOfDetail(oldLoD);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Change LoD to " + newLoD;
	}
}
