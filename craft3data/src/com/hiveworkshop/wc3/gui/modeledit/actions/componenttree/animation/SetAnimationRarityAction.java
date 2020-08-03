package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Animation;

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
	public void undo() {
		animation.setRarity(prevRarity);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public void redo() {
		animation.setRarity(newRarity);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public String actionName() {
		return "set animation Rarity to " + newRarity;
	}
}
