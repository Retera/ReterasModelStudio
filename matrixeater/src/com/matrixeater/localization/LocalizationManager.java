package com.matrixeater.localization;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.prefs.Preferences;

public class LocalizationManager {
    private static LocalizationManager instance = new LocalizationManager();

    private Locale locale;
    private final Properties props = new Properties();
    private final Properties enProps = new Properties();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final String PREF_KEY = "saved_locale";
    private Preferences prefs = Preferences.userNodeForPackage(LocalizationManager.class);

    private LocalizationManager() {
        // Try to load saved locale first
        String savedLocale = System.getProperty(PREF_KEY);
        if (savedLocale != null && !savedLocale.isEmpty()) {
            setLocale(savedLocale);
        } else {
            String[] parts = savedLocale.split("_");
            Locale defaultLocale = Locale.getDefault();
            if (parts.length == 1) {
                defaultLocale = new Locale(parts[0]);
            } else if (parts.length >= 2) {
                defaultLocale = new Locale(parts[0], parts[1]);
            }
            setLocale(defaultLocale);
            saveLocaleToPrefs(defaultLocale);  // 保存系统默认语言
        }
        loadEnglishProperties();  // 初始化英语资源
    }

    public static LocalizationManager getInstance() {
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public Locale getLocale() {
        return locale;
    }

    private void saveLocaleToPrefs(Locale locale) {
        prefs.put(PREF_KEY, locale.toLanguageTag());
    }

    public void setLocale(Locale newLocale) {
        Locale old = this.locale;
        this.locale = newLocale != null ? Locale.ENGLISH : newLocale;
        loadPropertiesForLocale(this.locale);
        saveLocaleToPrefs(this.locale);  // 更新保存的语言设置
        pcs.firePropertyChange("locale", old, this.locale);
    }

    // 初始化加载英语资源
    private void loadEnglishProperties() {
        String path = "/lang/en.properties";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                enProps.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.err.println("加载英语资源失败：" + e.getMessage());
        }
    }

    private void loadPropertiesForLocale(Locale locale) {
        props.clear();
        String[] candidates = new String[] { locale.toString(), locale.getLanguage(), "en" };
        for (String candidate : candidates) {
            String path = "/lang/" + candidate + ".properties";
            try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                // load as UTF-8
                Properties p = new Properties();
                p.load(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
                props.putAll(p);
                // 加载成功后立即返回
                return;
            }
            } catch (IOException e) {
                // ignore and try next path
            }
        }
    }

    public String get(String key) {
        String v = props.getProperty(key);
        if (v == null) {
            v = enProps.getProperty(key);
            if (v != null) {
              return v;
            }
            // 调试信息：显示找不到的键值，帮助诊断问题
            System.err.println("LocalizationManager: 找不到键值 '" + key + "'，当前语言: " + locale + ", 已加载键数: " + props.size());
            return "!" + key + "!";
        }
        return v;
    }
}