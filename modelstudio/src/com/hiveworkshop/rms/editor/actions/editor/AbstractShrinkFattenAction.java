package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;

import java.util.Locale;

public abstract class AbstractShrinkFattenAction implements UndoAction {
	protected float amount;

	@Override
	public String actionName() {
		return (amount < 0 ? "Shrink by " : "Fatten by ") + String.format(Locale.US, "%3.1f", amount);
	}


	public AbstractShrinkFattenAction updateAmount(float deltaAmount) {
		this.amount += deltaAmount;
		rawScale(deltaAmount);
		return this;
	}
	public AbstractShrinkFattenAction setAmount(float amount) {
		this.amount = amount;
		rawSetScale(amount);
		return this;
	}


	protected abstract void rawScale(float amountDelta);

	protected abstract void rawSetScale(float amount);
}
