package ken.mizoguch.decotofu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JavaLibrary}.
 */
class JavaLibraryTest {

    @TempDir
    Path tempDir;

    @Test
    void classNameAccessors() {
        JavaLibrary.setClassName("DecoTofuTest");
        assertEquals("DecoTofuTest", JavaLibrary.getClassName());
        JavaLibrary.setClassName("");
        assertEquals("", JavaLibrary.getClassName());
    }

    @Test
    void findFileMatchesGlobPattern() throws Exception {
        Files.writeString(tempDir.resolve("a.txt"), "a");
        Files.writeString(tempDir.resolve("b.md"), "b");
        Path sub = Files.createDirectory(tempDir.resolve("sub"));
        Files.writeString(sub.resolve("c.txt"), "c");

        List<Path> matches = JavaLibrary.findFile(tempDir, "*.txt");
        assertEquals(2, matches.size());
        assertTrue(matches.stream().anyMatch((p) -> p.endsWith("a.txt")));
        assertTrue(matches.stream().anyMatch((p) -> p.endsWith("c.txt")));
    }

    @Test
    void findFileMissingRootReturnsEmptyList() {
        List<Path> matches = JavaLibrary.findFile(tempDir.resolve("no-such-root"), "*");
        assertTrue(matches.isEmpty());
    }

    @Test
    void loadLibraryInvalidArgumentsReturnFalse() {
        assertFalse(JavaLibrary.loadLibrary(null));
        assertFalse(JavaLibrary.loadLibrary(""));
        assertFalse(JavaLibrary.loadLibrary("decoTofuNoSuchLibraryXyz"));
        assertFalse(JavaLibrary.isLibrary("decoTofuNoSuchLibraryXyz"));
    }

    @Test
    void loadLibraryWithoutSystemLoadOnlyRegistersName() {
        assertFalse(JavaLibrary.isLibrary("registerOnlyLibXyz"));
        assertTrue(JavaLibrary.loadLibrary("registerOnlyLibXyz", false));
        assertTrue(JavaLibrary.isLibrary("registerOnlyLibXyz"));
        assertFalse(JavaLibrary.loadLibrary("registerOnlyLibXyz", false));
    }

    @Test
    void isLibraryPathFalseForUnregisteredPaths() {
        assertFalse(JavaLibrary.isLibraryPath(tempDir));
        assertFalse(JavaLibrary.isLibraryPath(Paths.get("no.such.dir")));
    }

    @Test
    void extractResourceInvalidArgumentsReturnFalse() {
        ClassLoader classLoader = JavaLibrary.class.getClassLoader();

        assertFalse(JavaLibrary.extractResourceLibrary(classLoader, null, false));
        assertFalse(JavaLibrary.extractResourceLibrary(classLoader, "", false));
        assertFalse(JavaLibrary.extractResourceZip(classLoader, null, false));
        assertFalse(JavaLibrary.extractResourceZip(classLoader, "", false));
    }

    @Test
    void extractResourceMissingResourceReturnsFalse() {
        ClassLoader classLoader = JavaLibrary.class.getClassLoader();

        assertFalse(JavaLibrary.extractResourceLibrary(classLoader, "missing.dll", false));
        assertFalse(JavaLibrary.extractResourceZip(classLoader, "missing.zip", false));
    }

    @Test
    void platformChecksMatchWindowsHost() {
        assertTrue(JavaLibrary.isWindows());
        assertTrue(JavaLibrary.is64Bit());
        assertFalse(JavaLibrary.isLinux());
        assertFalse(JavaLibrary.isMac());
    }
}
