package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeMaterialAction implements UndoAction {
	Geoset geoset;
	Material oldMaterial;
	Material newMaterial;
	ModelStructureChangeListener changeListener;

	public ChangeMaterialAction(Geoset geoset, Material newMaterial, ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		oldMaterial = geoset.getMaterial();
		this.newMaterial = newMaterial;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		geoset.setMaterial(oldMaterial);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		geoset.setMaterial(newMaterial);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change material";
	}
}
