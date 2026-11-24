package ken.mizoguch.decotofu;

import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ken.mizoguch.webviewer.StageSettingsPlugin;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DecoTofuI18nTest {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)}");

    @AfterEach
    void tearDown() {
        I18n.resetToSystemDefault();
    }

    private static Set<String> placeholders(String text) {
        Set<String> tokens = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            tokens.add(matcher.group(0));
        }
        return tokens;
    }

    @Test
    void englishAndJapaneseBundlesHaveIdenticalKeysAndPlaceholders() {
        Properties en = I18n.englishBundle();
        Properties ja = I18n.japaneseBundle();

        assertFalse(en.isEmpty(), "English bundle must not be empty");
        assertEquals(en.stringPropertyNames(), ja.stringPropertyNames(),
                "English and Japanese bundles must define identical keys");

        for (String key : en.stringPropertyNames()) {
            String enVal = en.getProperty(key);
            String jaVal = ja.getProperty(key);
            assertNotNull(enVal, "Missing English value for key: " + key);
            assertNotNull(jaVal, "Missing Japanese value for key: " + key);
            assertFalse(enVal.isEmpty(), "Empty English value for key: " + key);
            assertFalse(jaVal.isEmpty(), "Empty Japanese value for key: " + key);
            assertEquals(placeholders(enVal), placeholders(jaVal),
                    "Placeholder mismatch between English and Japanese for key: " + key);
        }
    }

    @Test
    void languageSwitchingSynchronizesSystemPropertyDisplayLocaleAndListeners() {
        AtomicReference<Locale> lastLocale = new AtomicReference<>();
        AtomicInteger callCount = new AtomicInteger();
        Consumer<Locale> listener = locale -> {
            lastLocale.set(locale);
            callCount.incrementAndGet();
        };
        I18n.addListener(listener);
        try {
            I18n.setLanguage("ja");
            assertEquals(I18n.LANG_JA, I18n.getLanguageSetting());
            assertEquals(I18n.LANG_JA, I18n.getResolvedLanguage());
            assertTrue(I18n.isJapanese());
            assertEquals(I18n.LANG_JA, System.getProperty(I18n.PROPERTY_LANGUAGE));
            assertEquals(Locale.JAPANESE.getLanguage(), Locale.getDefault(Locale.Category.DISPLAY).getLanguage());
            assertEquals(Locale.JAPANESE, lastLocale.get());
            assertEquals("ファイル", I18n.get("menu.file"));

            I18n.setLanguage("en");
            assertEquals(I18n.LANG_EN, I18n.getLanguageSetting());
            assertEquals(I18n.LANG_EN, I18n.getResolvedLanguage());
            assertFalse(I18n.isJapanese());
            assertEquals(I18n.LANG_EN, System.getProperty(I18n.PROPERTY_LANGUAGE));
            assertEquals(Locale.ENGLISH.getLanguage(), Locale.getDefault(Locale.Category.DISPLAY).getLanguage());
            assertEquals(Locale.ENGLISH, lastLocale.get());
            assertEquals("File", I18n.get("menu.file"));

            I18n.setLanguage("fr");
            assertEquals(I18n.LANG_EN, I18n.getLanguageSetting());
            assertEquals(I18n.LANG_EN, I18n.getResolvedLanguage());

            I18n.setLanguage("auto");
            assertEquals(I18n.LANG_AUTO, I18n.getLanguageSetting());
            assertEquals(I18n.getResolvedLanguage(), System.getProperty(I18n.PROPERTY_LANGUAGE));

            int countBeforeRemove = callCount.get();
            I18n.removeListener(listener);
            I18n.setLanguage("ja");
            assertEquals(countBeforeRemove, callCount.get());
        } finally {
            I18n.removeListener(listener);
        }
    }

    @Test
    void stageSettingsPluginExposesAndSwitchesLanguage() {
        StageSettingsPlugin stagePlugin = new StageSettingsPlugin();
        assertTrue(stagePlugin.setLanguage("ja"));
        assertEquals("ja", stagePlugin.getLanguage());
        assertEquals("ja", stagePlugin.getResolvedLanguage());
        assertEquals(Locale.JAPANESE, stagePlugin.locale());

        assertTrue(stagePlugin.setLanguage("en"));
        assertEquals("en", stagePlugin.getLanguage());
        assertEquals("en", stagePlugin.getResolvedLanguage());
        assertEquals(Locale.ENGLISH, stagePlugin.locale());

        assertTrue(stagePlugin.setLanguage("auto"));
        assertEquals("auto", stagePlugin.getLanguage());
    }

    @Test
    void placeholderReplacementPreservesLiteralQuotesAndRawNumbers() {
        assertEquals("Value \"bad\" -> 12000ns",
                I18n.replacePlaceholders("Value \"{0}\" -> {1}ns", "bad", 12000));
        assertEquals("It's 10000 and {1}",
                I18n.replacePlaceholders("It's {0} and {1}", 10000));
    }
}
