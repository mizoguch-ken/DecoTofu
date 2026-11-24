package ken.mizoguch.decotofu;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Application-wide bilingual (English / Japanese) localization helper for DecoTofu.
 *
 * <p>Stores the resolved language code in the {@code decotofu.language} system property and updates
 * {@link Locale.Category#DISPLAY} so dynamically loaded WebViewer plug-ins and JavaFX dialogs follow
 * runtime language changes.</p>
 */
public final class I18n {

    public static final String PROPERTY_LANGUAGE = "decotofu.language";
    public static final String LANG_AUTO = "auto";
    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";

    private static final String BUNDLE_EN = "/ken/mizoguch/decotofu/i18n/messages.properties";
    private static final String BUNDLE_JA = "/ken/mizoguch/decotofu/i18n/messages_ja.properties";

    private static final Locale SYSTEM_DISPLAY_LOCALE = Locale.getDefault(Locale.Category.DISPLAY);
    private static final Properties EN_BUNDLE = loadBundle(BUNDLE_EN);
    private static final Properties JA_BUNDLE = loadBundle(BUNDLE_JA);
    private static final CopyOnWriteArrayList<Consumer<Locale>> LISTENERS = new CopyOnWriteArrayList<>();

    private static volatile String languageSetting_ = LANG_AUTO;
    private static volatile Locale locale_ = resolveLocale(LANG_AUTO);

    private I18n() {
    }

    public static String normalizeLanguageSetting(String raw) {
        if (raw == null) {
            return LANG_AUTO;
        }
        String norm = raw.trim().toLowerCase(Locale.ROOT);
        if (norm.isEmpty() || LANG_AUTO.equals(norm) || "system".equals(norm)) {
            return LANG_AUTO;
        }
        if (LANG_JA.equals(norm) || "japanese".equals(norm)
                || norm.startsWith("ja-") || norm.startsWith("ja_")) {
            return LANG_JA;
        }
        if (LANG_EN.equals(norm) || "english".equals(norm)
                || norm.startsWith("en-") || norm.startsWith("en_")) {
            return LANG_EN;
        }
        return null;
    }

    public static Locale resolveLocale(String setting) {
        String normalized = normalizeLanguageSetting(setting);
        if (LANG_JA.equals(normalized)) {
            return Locale.JAPANESE;
        }
        if (LANG_EN.equals(normalized)) {
            return Locale.ENGLISH;
        }
        if ((SYSTEM_DISPLAY_LOCALE != null) && LANG_JA.equalsIgnoreCase(SYSTEM_DISPLAY_LOCALE.getLanguage())) {
            return Locale.JAPANESE;
        }
        return Locale.ENGLISH;
    }

    public static boolean setLanguage(String setting) {
        String normalized = normalizeLanguageSetting(setting);
        if (normalized == null) {
            return false;
        }
        Locale resolved = resolveLocale(normalized);
        languageSetting_ = normalized;
        locale_ = resolved;
        System.setProperty(PROPERTY_LANGUAGE, resolved.getLanguage());
        Locale.setDefault(Locale.Category.DISPLAY, resolved);
        for (Consumer<Locale> listener : LISTENERS) {
            try {
                listener.accept(resolved);
            } catch (RuntimeException ex) {
                // Keep notifying remaining listeners
            }
        }
        return true;
    }

    public static String getLanguageSetting() {
        return languageSetting_;
    }

    public static String getResolvedLanguage() {
        return locale_.getLanguage();
    }

    public static Locale getLocale() {
        return locale_;
    }

    public static boolean isJapanese() {
        return LANG_JA.equalsIgnoreCase(locale_.getLanguage());
    }

    public static void addListener(Consumer<Locale> listener) {
        if (listener != null) {
            LISTENERS.addIfAbsent(listener);
        }
    }

    public static void removeListener(Consumer<Locale> listener) {
        if (listener != null) {
            LISTENERS.remove(listener);
        }
    }

    public static String get(String key, Object... args) {
        return get(locale_, key, args);
    }

    public static String get(Locale locale, String key, Object... args) {
        if (key == null) {
            return "";
        }
        String pattern = null;
        if ((locale != null) && LANG_JA.equalsIgnoreCase(locale.getLanguage())) {
            pattern = JA_BUNDLE.getProperty(key);
        }
        if (pattern == null) {
            pattern = EN_BUNDLE.getProperty(key);
        }
        if (pattern == null) {
            return key;
        }
        return replacePlaceholders(pattern, args);
    }

    public static String replacePlaceholders(String pattern, Object... args) {
        if ((pattern == null) || (args == null) || (args.length == 0) || (pattern.indexOf('{') < 0)) {
            return pattern;
        }
        StringBuilder sb = new StringBuilder(pattern.length() + 16);
        int len = pattern.length();
        int i = 0;
        while (i < len) {
            char ch = pattern.charAt(i);
            if (ch == '{') {
                int close = pattern.indexOf('}', i + 1);
                if (close > (i + 1)) {
                    int idx = parseNonNegativeInt(pattern, i + 1, close);
                    if ((idx >= 0) && (idx < args.length)) {
                        sb.append(args[idx]);
                        i = close + 1;
                        continue;
                    }
                }
            }
            sb.append(ch);
            i++;
        }
        return sb.toString();
    }

    static Properties englishBundle() {
        return EN_BUNDLE;
    }

    static Properties japaneseBundle() {
        return JA_BUNDLE;
    }

    public static void resetToSystemDefault() {
        languageSetting_ = LANG_AUTO;
        locale_ = resolveLocale(LANG_AUTO);
        System.clearProperty(PROPERTY_LANGUAGE);
        Locale.setDefault(Locale.Category.DISPLAY, SYSTEM_DISPLAY_LOCALE);
    }

    private static int parseNonNegativeInt(String text, int start, int end) {
        int value = 0;
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);
            if ((c < '0') || (c > '9')) {
                return -1;
            }
            value = (value * 10) + (c - '0');
        }
        return value;
    }

    private static Properties loadBundle(String resourcePath) {
        Properties props = new Properties();
        try (InputStream in = I18n.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    props.load(reader);
                }
            }
        } catch (IOException ex) {
            // Keep empty bundle on read error
        }
        return props;
    }
}
