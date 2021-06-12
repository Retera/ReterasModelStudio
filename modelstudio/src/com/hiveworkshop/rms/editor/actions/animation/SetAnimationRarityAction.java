package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationRarityAction implements UndoAction {
	private final float prevRarity;
	private final float newRarity;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;

	public SetAnimationRarityAction(float newRarity, Animation animation, ModelStructureChangeListener changeListener) {
		this.animation = animation;
		this.prevRarity = animation.getRarity();
		this.newRarity = newRarity;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setRarity(prevRarity);
		if (changeListener != null) {
			changeListener.animationParamsChanged(animation);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setRarity(newRarity);
		if (changeListener != null) {
			changeListener.animationParamsChanged(animation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set animation Rarity to " + newRarity;
	}
}
