package com.hiveworkshop.rms.editor.actions.model.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collections;

public class DeleteAnimationAction implements UndoAction {
	//	private final int prevIntervalStart;
//	private final int newIntervalStart;
	private final EditableModel model;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public DeleteAnimationAction(final EditableModel model, final Animation animation, final ModelStructureChangeListener structureChangeListener) {
//		this.prevIntervalStart = prevIntervalStart;
//		this.newIntervalStart = newIntervalStart;
		this.model = model;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
//		model.getAnims();
		model.addAnimation(animation);
		structureChangeListener.animationsAdded(Collections.singletonList(animation));
		return this;
	}

	@Override
	public UndoAction redo() {
//		animation.setIntervalStart(newIntervalStart);
		model.remove(animation);
		structureChangeListener.animationsRemoved(Collections.singletonList(animation));
		return this;
	}

	@Override
	public String actionName() {
		return "Animation Deleted";
	}
}
