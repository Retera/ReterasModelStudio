package com.hiveworkshop.rms.ui.application.edit.mesh.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSelectionManager<T> implements SelectionManager<T> {
	protected final Set<T> selection = new HashSet<>();
	private final Set<SelectionListener> listeners = new HashSet<>();

	@Override
	public Set<T> getSelection() {
		return selection;
	}

	@Override
	public void setSelection(final Collection<? extends T> selectionItem) {
		selection.clear();
		for (final T item : selectionItem) {
			selection.add(item);
		}
		fireChangeListeners();
	}

	@Override
	public void addSelection(final Collection<? extends T> selectionItem) {
		for (final T item : selectionItem) {
			selection.add(item);
		}
		fireChangeListeners();
	}

	@Override
	public void removeSelection(final Collection<? extends T> selectionItem) {
		for (final T item : selectionItem) {
			selection.remove(item);
		}
		fireChangeListeners();
	}

	@Override
	public void addSelectionListener(final SelectionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSelectionListener(final SelectionListener listener) {
		listeners.remove(listener);
	}

	private void fireChangeListeners() {
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(this);
		}
	}

	@Override
	public boolean isEmpty() {
		return selection.isEmpty();
	}
}
