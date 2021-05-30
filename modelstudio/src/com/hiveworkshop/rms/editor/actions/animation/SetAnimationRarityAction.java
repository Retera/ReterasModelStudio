package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationRarityAction implements UndoAction {
	private final float prevRarity;
	private final float newRarity;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationRarityAction(final float prevRarity, final float newRarity, final Animation animation,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevRarity = prevRarity;
		this.newRarity = newRarity;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setRarity(prevRarity);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setRarity(newRarity);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public String actionName() {
		return "set animation Rarity to " + newRarity;
	}
}
