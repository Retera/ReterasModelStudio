package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;

import java.util.function.Consumer;

public class ConsumerAction<T> implements UndoAction {
	Consumer<T> consumer;
	T newValue;
	T oldValue;
	String name;

	public ConsumerAction(Consumer<T> consumer, T newValue, T oldValue, String name) {
		this.consumer = consumer;
		this.newValue = newValue;
		this.oldValue = oldValue;
		this.name = name;
	}

	@Override
	public UndoAction undo() {
		consumer.accept(oldValue);
		return this;
	}

	@Override
	public UndoAction redo() {
		consumer.accept(newValue);
		return this;
	}

	@Override
	public String actionName() {
		return "Edit " + name;
	}
}
