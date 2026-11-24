package ken.mizoguch.ladders;

import ken.mizoguch.decotofu.I18n;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class LadderI18nTest {

    @AfterEach
    void tearDown() {
        I18n.resetToSystemDefault();
    }

    @Test
    void commandCatalogLocalizesCategoriesLabelsAndHints() {
        I18n.setLanguage(I18n.LANG_EN);
        assertEquals("Contact", LadderCommandCatalog.Category.CONTACT.title());
        assertEquals("LD <addr>  — Load Contact",
                LadderCommandCatalog.findByBlock(LadderBlock.LOAD).menuLabel());
        assertEquals("LD <address> [; comment]", LadderCommandCatalog.hintForInput("LD"));
        assertTrue(LadderCommandCatalog.hintForInput("").contains("Enter: Apply"));

        I18n.setLanguage(I18n.LANG_JA);
        assertEquals("接点", LadderCommandCatalog.Category.CONTACT.title());
        assertEquals("LD <addr>  — a接点ロード",
                LadderCommandCatalog.findByBlock(LadderBlock.LOAD).menuLabel());
        assertEquals("LD <アドレス> [; コメント]", LadderCommandCatalog.hintForInput("LD"));
        assertTrue(LadderCommandCatalog.hintForInput("").contains("Enter: 適用"));
    }

    @Test
    void telemetryValidationMessagesSwitchByLocale() {
        LadderTelemetry telemetry = new LadderTelemetry();

        I18n.setLanguage(I18n.LANG_EN);
        IllegalArgumentException enEx = assertThrows(IllegalArgumentException.class,
                () -> telemetry.startTrace("[]"));
        assertEquals("Trace options must be a JSON object", enEx.getMessage());

        I18n.setLanguage(I18n.LANG_JA);
        IllegalArgumentException jaEx = assertThrows(IllegalArgumentException.class,
                () -> telemetry.startTrace("[]"));
        assertEquals("トレースオプション は JSON オブジェクトである必要があります", jaEx.getMessage());
    }
}
