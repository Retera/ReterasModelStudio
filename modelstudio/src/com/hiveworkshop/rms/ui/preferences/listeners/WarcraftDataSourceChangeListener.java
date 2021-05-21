package com.hiveworkshop.rms.ui.preferences.listeners;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public interface WarcraftDataSourceChangeListener {
	void dataSourcesChanged();

	final class WarcraftDataSourceChangeNotifier extends SubscriberSetNotifier<WarcraftDataSourceChangeListener>
			implements WarcraftDataSourceChangeListener {

		@Override
		public void dataSourcesChanged() {
			for (WarcraftDataSourceChangeListener listener : listenerSet) {
				listener.dataSourcesChanged();
			}
		}

	}
}
