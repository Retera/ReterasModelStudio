package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetMaterialFlagAction implements UndoAction {
	private final Material material;
	private final Material.flag flag;
	private final boolean set;
	private final ModelStructureChangeListener changeListener;

	public SetMaterialFlagAction(Material material, Material.flag flag, boolean set, ModelStructureChangeListener changeListener) {
		this.material = material;
		this.flag = flag;
		this.set = set;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		material.setFlag(flag, !set);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		material.setFlag(flag, set);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Toggled Material " + flag;
	}
}
