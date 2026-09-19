package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.util.Callback;

/**
 * One undoable change of a single property on a model component. The setter
 * writes the value into the model; the notifier tells the surrounding editor
 * that the component changed so trees, viewports and cards refresh.
 */
public final class SetComponentPropertyAction<T> implements UndoAction {
	private final String actionName;
	private final T oldValue;
	private final T newValue;
	private final Callback<T> setter;
	private final Runnable notifier;

	public SetComponentPropertyAction(final String actionName, final T oldValue, final T newValue,
			final Callback<T> setter, final Runnable notifier) {
		this.actionName = actionName;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.setter = setter;
		this.notifier = notifier;
	}

	@Override
	public void undo() {
		setter.run(oldValue);
		notifier.run();
	}

	@Override
	public void redo() {
		setter.run(newValue);
		notifier.run();
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
