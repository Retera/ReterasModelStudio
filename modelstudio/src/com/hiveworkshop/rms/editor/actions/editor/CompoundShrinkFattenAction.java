package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.List;
import java.util.Locale;

public class CompoundShrinkFattenAction extends AbstractShrinkFattenAction {
	private final ModelStructureChangeListener changeListener;
	private final AbstractShrinkFattenAction[] actions;


	public CompoundShrinkFattenAction(float amount, final List<? extends AbstractShrinkFattenAction> actions, ModelStructureChangeListener changeListener) {
		this(amount, changeListener, actions.toArray(new AbstractShrinkFattenAction[0]));
	}

	public CompoundShrinkFattenAction(float amount, final List<? extends AbstractShrinkFattenAction> actions) {
		this(amount, null, actions.toArray(new AbstractShrinkFattenAction[0]));
	}

	public CompoundShrinkFattenAction(float amount, AbstractShrinkFattenAction... actions) {
		this(amount, null, actions);
	}

	public CompoundShrinkFattenAction(float amount, ModelStructureChangeListener changeListener, AbstractShrinkFattenAction... actions) {
		this.amount = amount;
		this.changeListener = changeListener;
		this.actions = actions;
	}


	@Override
	public CompoundShrinkFattenAction undo() {
		for (final AbstractShrinkFattenAction action : actions) {
			action.undo();
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public CompoundShrinkFattenAction redo() {
		for (final AbstractShrinkFattenAction action : actions) {
			action.redo();
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return (amount < 0 ? "Shrink by " : "Fatten by ") + String.format(Locale.US, "%3.1f", amount);
	}


	public CompoundShrinkFattenAction setAmount(float amount) {
		this.amount = amount;
		for (final AbstractShrinkFattenAction action : actions) {
			action.setAmount(amount);
		}
		return this;
	}


	public CompoundShrinkFattenAction updateAmount(float deltaAmount) {
		this.amount += deltaAmount;
		for (final AbstractShrinkFattenAction action : actions) {
			action.updateAmount(deltaAmount);
		}
		return this;
	}

	protected void rawScale(float amountDelta) {
	}

	protected void rawSetScale(float amount) {
	}

}
