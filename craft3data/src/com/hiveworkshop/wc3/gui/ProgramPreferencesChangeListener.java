package com.hiveworkshop.wc3.gui;

import com.etheller.util.SubscriberSetNotifier;

public interface ProgramPreferencesChangeListener {
	void preferencesChanged();

	public final class ProgramPreferencesChangeNotifier extends SubscriberSetNotifier<ProgramPreferencesChangeListener>
			implements ProgramPreferencesChangeListener {

		@Override
		public void preferencesChanged() {
			for (final ProgramPreferencesChangeListener listener : set) {
				listener.preferencesChanged();
			}
		}

	}
}