package com.hiveworkshop.wc3.user;

import com.etheller.util.SubscriberSetNotifier;

public interface WarcraftDataSourceChangeListener {
	void dataSourcesChanged();

	public final class WarcraftDataSourceChangeNotifier extends SubscriberSetNotifier<WarcraftDataSourceChangeListener>
			implements WarcraftDataSourceChangeListener {

		@Override
		public void dataSourcesChanged() {
			for (final WarcraftDataSourceChangeListener listener : set) {
				listener.dataSourcesChanged();
			}
		}

	}
}
