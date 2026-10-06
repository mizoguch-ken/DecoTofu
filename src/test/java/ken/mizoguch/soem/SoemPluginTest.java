package ken.mizoguch.soem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SoemPlugin#isScriptFunction}.
 */
class SoemPluginTest {

    @Test
    void jsFunctionNamesAreAccepted() {
        assertTrue(SoemPlugin.isScriptFunction("onError"));
        assertTrue(SoemPlugin.isScriptFunction("$onLost1"));
        assertTrue(SoemPlugin.isScriptFunction("_warning"));
        assertTrue(SoemPlugin.isScriptFunction("decoTofon.onError"));
    }

    @Test
    void namesWhichCarryAnOtherScriptAreRefused() {
        assertFalse(SoemPlugin.isScriptFunction(null));
        assertFalse(SoemPlugin.isScriptFunction(""));
        assertFalse(SoemPlugin.isScriptFunction("onError();exit();"));
        assertFalse(SoemPlugin.isScriptFunction("onError("));
        assertFalse(SoemPlugin.isScriptFunction("onError)();"));
        assertFalse(SoemPlugin.isScriptFunction("onError\nfoo"));
        assertFalse(SoemPlugin.isScriptFunction("1onError"));
        assertFalse(SoemPlugin.isScriptFunction("on Error"));
        assertFalse(SoemPlugin.isScriptFunction(".onError"));
        assertFalse(SoemPlugin.isScriptFunction("onError."));
    }
}
