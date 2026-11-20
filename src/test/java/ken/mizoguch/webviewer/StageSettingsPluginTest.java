package ken.mizoguch.webviewer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StageSettingsPlugin} stage helpers.
 */
class StageSettingsPluginTest {

    @Test
    void getVersionAnswersNonEmptyString() {
        StageSettingsPlugin plugin = new StageSettingsPlugin();
        String version = plugin.getVersion();

        assertNotNull(version);
        assertFalse(version.isEmpty());
    }

    @Test
    void nullStageMethodsAnswerSafely() {
        StageSettingsPlugin plugin = new StageSettingsPlugin();

        assertNull(plugin.isShowing());
        assertNull(plugin.isFocused());
        assertNull(plugin.getTitle());
        assertNull(plugin.isAlwaysOnTop());
        assertNull(plugin.isMaximized());
        assertNull(plugin.isFullScreen());
        assertNull(plugin.isIconified());

        assertDoesNotThrow(() -> {
            plugin.setTitle("Test");
            plugin.setFullScreen(true);
            plugin.setIconified(true);
            plugin.setAlwaysOnTop(true);
            plugin.setMaximized(true);
            plugin.toFront();
            plugin.toBack();
        });
    }
}
