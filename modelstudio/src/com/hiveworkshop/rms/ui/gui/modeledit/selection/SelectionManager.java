package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class SelectionManager<T> implements SelectionView {
//	protected final Set<T> selection = new HashSet<>();
	protected ModelView modelView;
	private final Set<SelectionListener> listeners = new HashSet<>();

	public SelectionManager(ModelView modelView) {
		this.modelView = modelView;
	}

	protected void fireChangeListeners() {
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(this);
		}
	}

	public abstract Set<T> getSelection();

	public abstract void setSelection(final Collection<? extends T> selectionItem);

	public abstract void addSelection(final Collection<? extends T> selectionItem);

	public abstract void removeSelection(final Collection<? extends T> selectionItem);

	public void addSelectionListener(final SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(final SelectionListener listener) {
		listeners.remove(listener);
	}

	@Override
	public abstract boolean isEmpty();
}
