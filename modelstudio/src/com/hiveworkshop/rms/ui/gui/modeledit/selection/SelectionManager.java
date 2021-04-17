package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class SelectionManager<T> implements SelectionView {
	protected final Set<T> selection = new HashSet<>();
	private final Set<SelectionListener> listeners = new HashSet<>();

	private void fireChangeListeners() {
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(this);
		}
	}

	public Set<T> getSelection() {
		return selection;
	}

	public void setSelection(final Collection<? extends T> selectionItem) {
		selection.clear();
		selection.addAll(selectionItem);
		fireChangeListeners();
	}

	public void addSelection(final Collection<? extends T> selectionItem) {
		selection.addAll(selectionItem);
		fireChangeListeners();
	}

	public void removeSelection(final Collection<? extends T> selectionItem) {
		for (final T item : selectionItem) {
			selection.remove(item);
		}
		fireChangeListeners();
	}

	public void addSelectionListener(final SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(final SelectionListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean isEmpty() {
		return selection.isEmpty();
	}
}
