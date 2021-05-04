package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

import java.util.Objects;

public abstract class CheckableDisplayElement<T> {
	protected final ModelView modelViewManager;
	protected final T item;

	public CheckableDisplayElement(ModelView modelViewManager, T item) {
		this.modelViewManager = modelViewManager;
		this.item = item;
	}

	public void setChecked(boolean checked) {
		setChecked(item, modelViewManager, checked);
	}

	public void mouseEntered() {
	}

	public void mouseExited() {
	}

	protected abstract void setChecked(T item, ModelView modelViewManager, boolean checked);

	@Override
	public String toString() {
		return getName(item, modelViewManager);
	}

	protected abstract String getName(T item, ModelView modelViewManager);

	public boolean hasSameItem(CheckableDisplayElement<?> other) {
		return Objects.equals(item, other.item);
	}

	public T getItem() {
		return item;
	}
}