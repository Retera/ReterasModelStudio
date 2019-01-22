package com.hiveworkshop.wc3.user;

import com.etheller.util.SubscriberSetNotifier;

public interface WarcraftDirectoryChangeListener {
	void directoryChanged();

	public final class WarcraftDirectoryChangeNotifier extends SubscriberSetNotifier<WarcraftDirectoryChangeListener>
			implements WarcraftDirectoryChangeListener {

		@Override
		public void directoryChanged() {
			for (final WarcraftDirectoryChangeListener listener : set) {
				listener.directoryChanged();
			}
		}

	}
}
