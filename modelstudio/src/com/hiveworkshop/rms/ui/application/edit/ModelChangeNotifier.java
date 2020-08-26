package com.hiveworkshop.rms.ui.application.edit;

import java.util.HashSet;
import java.util.Set;

public final class ModelChangeNotifier implements ModelChangeListener {
	private final Set<ModelChangeListener> listeners;

	public ModelChangeNotifier() {
		listeners = new HashSet<>();
	}

	public void subscribe(final ModelChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void modelChanged() {
		for (final ModelChangeListener listener : listeners) {
			listener.modelChanged();
		}
	}
}
