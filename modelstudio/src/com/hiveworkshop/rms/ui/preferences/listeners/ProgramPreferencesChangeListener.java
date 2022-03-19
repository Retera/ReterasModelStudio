package com.hiveworkshop.rms.ui.preferences.listeners;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public interface ProgramPreferencesChangeListener {
    void preferencesChanged();

    final class ProgramPreferencesChangeNotifier extends SubscriberSetNotifier<ProgramPreferencesChangeListener>
            implements ProgramPreferencesChangeListener {

        @Override
        public void preferencesChanged() {
            for (ProgramPreferencesChangeListener listener : listenerSet) {
                listener.preferencesChanged();
            }
        }

    }
}