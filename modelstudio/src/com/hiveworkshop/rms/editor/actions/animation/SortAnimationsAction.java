package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortAnimationsAction implements UndoAction {
	private final List<Animation> oldList = new ArrayList<>();
	private final EditableModel model;
	public SortAnimationsAction(EditableModel model){
		this.model = model;
		oldList.addAll(model.getAnims());
	}

	@Override
	public SortAnimationsAction undo() {
		model.clearAnimations();
		model.getAnims().addAll(oldList);
		return this;
	}

	@Override
	public SortAnimationsAction redo() {
		model.getAnims().sort(Comparator.comparingInt(Animation::getStart));
		return this;
	}

	@Override
	public String actionName() {
		return "Sort Animations";
	}
}
