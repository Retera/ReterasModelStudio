package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;

import java.util.function.Consumer;

public class BoolAction implements UndoAction {
	private final Consumer<Boolean> consumer;
	private final boolean newValue;
	private final String name;
	private final Runnable changeListener;

	public BoolAction(Consumer<Boolean> consumer, boolean newValue, String name, Runnable changeListener) {
		this.consumer = consumer;
		this.newValue = newValue;
		this.name = name;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		consumer.accept(!newValue);
		if(changeListener != null){
			changeListener.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		consumer.accept(newValue);
		if(changeListener != null){
			changeListener.run();
		}
		return this;
	}

	@Override
	public String actionName() {
		return name;
	}
}
