package com.matrixeater.localization;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class LocalizationManager {
    private static LocalizationManager instance = new LocalizationManager();

    private Locale locale;
    private final Properties props = new Properties();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private LocalizationManager() {
        setLocale(Locale.getDefault());
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

    public void setLocale(Locale newLocale) {
        Locale old = this.locale;
        this.locale = newLocale == null ? Locale.ENGLISH : newLocale;
        loadPropertiesForLocale(this.locale);
        pcs.firePropertyChange("locale", old, this.locale);
    }

    private void loadPropertiesForLocale(Locale locale) {
        props.clear();
        String[] candidates = new String[] { locale.toString(), locale.getLanguage(), "en" };
        for (String candidate : candidates) {
            // 修复资源路径：从 /lang/ 改为 /res/lang/，并测试包路径
            String[] possiblePaths = new String[] {
                "/res/lang/" + candidate + ".properties",
                "res/lang/" + candidate + ".properties",
            };
            for (String path : possiblePaths) {
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
    }

    public String get(String key) {
        String v = props.getProperty(key);
        if (v == null) {
            // 调试信息：显示找不到的键值，帮助诊断问题
            System.err.println("LocalizationManager: 找不到键值 '" + key + "'，当前语言: " + locale + ", 已加载键数: " + props.size());
            return "!" + key + "!";
        }
        return v;
    }
}
