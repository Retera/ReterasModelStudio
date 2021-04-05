package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;

import java.util.Objects;

public abstract class CheckableDisplayElement<T> implements SelectableComponent {
	protected final ModelViewManager modelViewManager;
	protected final T item;

	public CheckableDisplayElement(ModelViewManager modelViewManager, T item) {
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

	protected abstract void setChecked(T item, ModelViewManager modelViewManager, boolean checked);

	@Override
	public void visit(final SelectableComponentVisitor visitor) {
	}

	@Override
	public String toString() {
		return getName(item, modelViewManager);
	}

	protected abstract String getName(T item, ModelViewManager modelViewManager);

	public boolean hasSameItem(CheckableDisplayElement<?> other) {
		return Objects.equals(item, other.item);
	}

	public T getItem() {
		return item;
	}
}