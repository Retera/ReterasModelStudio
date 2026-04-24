package hiveworkshop.localizationmanager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final String LANG_KEY = "savedlocale";
    private Preferences prefs = Preferences.userNodeForPackage(LocalizationManager.class);

    private LocalizationManager() {
        String savedLocaleStr = prefs.get(LANG_KEY, null);
        Locale savedLang;
        if (savedLocaleStr != null && !savedLocaleStr.isEmpty()) {
            savedLang = Locale.forLanguageTag(savedLocaleStr);
        } else {
            savedLang = Locale.getDefault();
        }
        if (savedLang == null) {
            savedLang = Locale.ENGLISH;
        }
        setLocale(savedLang); // 这里会自动加载资源并保存 prefs
        loadEnglishProperties();
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
        prefs.put(LANG_KEY, locale.toLanguageTag());
    }

    public void setLocale(Locale newLocale) {
        Locale old = this.locale;
        this.locale = newLocale != null ? newLocale : Locale.ENGLISH;
        loadPropertiesForLocale(this.locale);
        saveLocaleToPrefs(this.locale); // 更新保存的语言设置
        pcs.firePropertyChange("locale", old, this.locale);
    }

    // 初始化加载英语资源
    private void loadEnglishProperties() {
        String path = "/hiveworkshop/wc3/lang/en.properties";
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
            String path = "/hiveworkshop/wc3/lang/" + candidate + ".properties";
            try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                // load as UTF-8
                Properties p = new Properties();
                p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                props.putAll(p);
                // 加载成功后立即返回
                return;
            }
            } catch (IOException e) {
                System.err.println("❌ Failed to load " + path + ": " + e.getMessage());
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
            System.err.println("LocalizationManager: Missing key '" + key + "' for locale " + locale);
            return "!" + key + "!";
        }
        return v;
    }
}