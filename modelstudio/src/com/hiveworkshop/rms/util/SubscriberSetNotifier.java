package com.hiveworkshop.rms.util;

import java.util.HashSet;
import java.util.Set;

public abstract class SubscriberSetNotifier<LISTENER_TYPE> {
	protected final Set<LISTENER_TYPE> listenerSet;
	// bad for iteration but there should never be a dude subscribed 2x

	public SubscriberSetNotifier() {
		listenerSet = new HashSet<>();
	}

	public final void subscribe(final LISTENER_TYPE listener) {
		listenerSet.add(listener);
	}

	public final void unsubscribe(final LISTENER_TYPE listener) {
		listenerSet.remove(listener);
	}
}
