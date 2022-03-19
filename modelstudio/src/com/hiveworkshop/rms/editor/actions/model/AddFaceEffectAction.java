package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.FaceEffect;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collection;
import java.util.Collections;

public class AddFaceEffectAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final EditableModel model;
	private final Collection<FaceEffect> faceEffects;

	public AddFaceEffectAction(FaceEffect faceEffect, EditableModel model, ModelStructureChangeListener changeListener){
		this(Collections.singleton(faceEffect), model, changeListener);
	}
	public AddFaceEffectAction(Collection<FaceEffect> faceEffects, EditableModel model, ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.model = model;
		this.faceEffects = faceEffects;
	}

	@Override
	public UndoAction undo() {
		for (FaceEffect faceEffect : faceEffects){
			model.remove(faceEffect);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (FaceEffect faceEffect : faceEffects){
			model.addFaceEffect(faceEffect);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add FaceFX";
	}
}
