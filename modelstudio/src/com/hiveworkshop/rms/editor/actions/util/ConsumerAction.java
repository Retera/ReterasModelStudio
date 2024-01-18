package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;

import java.util.function.Consumer;

public class ConsumerAction<T> implements UndoAction {
	private final Consumer<T> consumer;
	private final T newValue;
	private final T oldValue;
	private final String name;

	public ConsumerAction(Consumer<T> consumer, T newValue, T oldValue, String name) {
		this.consumer = consumer;
		this.newValue = newValue;
		this.oldValue = oldValue;
		this.name = name;
	}

	@Override
	public ConsumerAction<T> undo() {
		consumer.accept(oldValue);
		return this;
	}

	@Override
	public ConsumerAction<T> redo() {
		consumer.accept(newValue);
		return this;
	}

	@Override
	public String actionName() {
		return "Edit " + name;
	}
}
