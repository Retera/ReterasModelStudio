package com.hiveworkshop.wc3.resources;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Localization {
    private static final Localization INSTANCE = new Localization();

    private ResourceBundle bundle;
    private Locale locale = Locale.ENGLISH;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Localization() {
        load(Locale.ENGLISH);
    }

    public static Localization getInstance() {
        return INSTANCE;
    }

    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public void setLocale(final Locale l) {
        final Locale newLocale = (l == null) ? Locale.ENGLISH : l;
        final Locale old = this.locale;
        if (!old.equals(newLocale)) {
            this.locale = newLocale;
            load(newLocale);
            pcs.firePropertyChange("locale", old, newLocale);
        }
    }

    private void load(final Locale l) {
        final String[] candidates = new String[] { l.toString(), l.getLanguage(), "en" };
        for (final String cand : candidates) {
            final String path = "/lang/craft3data_" + cand + ".properties";
            try (final InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    try {
                        bundle = new PropertyResourceBundle(new InputStreamReader(is, "utf-8"));
                        return;
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (final IOException e) {
            }
        }
        bundle = null;
    }

    public String get(final String key) {
        if (bundle == null) {
            return "!" + key + "!";
        }
        try {
            return bundle.getString(key);
        } catch (final MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    public static String t(final String key) {
        return getInstance().get(key);
    }
}
