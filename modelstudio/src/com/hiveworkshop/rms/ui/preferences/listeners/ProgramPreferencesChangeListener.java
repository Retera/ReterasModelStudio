package com.hiveworkshop.rms.ui.preferences.listeners;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public class ProgramPreferencesChangeListener extends SubscriberSetNotifier<Runnable> {

	public void runListeners() {
		for (Runnable listener : listenerSet) {
			listener.run();
		}
	}
}