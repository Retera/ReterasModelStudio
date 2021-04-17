package com.hiveworkshop.rms.ui.preferences.listeners;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public interface WarcraftDirectoryChangeListener {
	void directoryChanged();

	final class WarcraftDirectoryChangeNotifier extends SubscriberSetNotifier<WarcraftDirectoryChangeListener>
			implements WarcraftDirectoryChangeListener {

		@Override
		public void directoryChanged() {
			for (final WarcraftDirectoryChangeListener listener : set) {
				listener.directoryChanged();
			}
		}

	}
}
