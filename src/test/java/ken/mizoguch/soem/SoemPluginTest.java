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
        // the end of the pattern is the end of the name, not the end of a line
        assertFalse(SoemPlugin.isScriptFunction("onError\n"));
        assertFalse(SoemPlugin.isScriptFunction("onError\r\n"));
    }

    @Test
    void anUnusableLibraryPathIsRefused() {
        SoemPlugin plugin = new SoemPlugin(new Soem());

        // the plugin is not attached to a web page : the messages are answered by the
        // console of the application and no library is loaded
        assertFalse(plugin.load(null));
        assertFalse(plugin.load(""));
    }

    @Test
    void aLibraryWhichIsNotAFileIsRefused() {
        SoemPlugin plugin = new SoemPlugin(new Soem());

        // the given file exists in no directory : the loader of jnr-ffi is not left search
        // an other library of the java library path
        assertFalse(plugin.load("decoTofuNoSuchLibraryXyz"));
        assertFalse(plugin.load("decoTofuNoSuchLibraryXyz/decoTofuNoSuchLibraryXyz.dll"));
    }
}
