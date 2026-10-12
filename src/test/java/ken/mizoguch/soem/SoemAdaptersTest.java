package ken.mizoguch.soem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Soem#find_adapters()}, with the shared library of
 * DecoTofu-SOEM when it is built next to the DecoTofu project.
 */
class SoemAdaptersTest {

    /**
     * the shared library DecoTofu-SOEM builds next to this project, in the order
     * of the platforms. The library is not a resource of this project : a test
     * which needs it is skipped when it is not built.
     */
    private static final String[] LIBRARY_CANDIDATES = {
        "../DecoTofu-SOEM/build/decotofusoem.dll",
        "../DecoTofu-SOEM/build/libdecotofusoem.so",
        "../DecoTofu-SOEM/build/libdecotofusoem.dylib",};

    /**
     * @return the built shared library of DecoTofu-SOEM, null when there is none
     */
    private static Path library() {
        for (String candidate : LIBRARY_CANDIDATES) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }

    @Test
    void findAdaptersIsRefusedWithoutLibrary() {
        Soem soem = new Soem();

        // no shared library loaded : the answer is null, no exception
        assertNull(soem.find_adapters());
    }

    @Test
    void findAdaptersAnswersTheAdaptersOfTheMachine() {
        Path library = library();
        assumeTrue(library != null, "the shared library of DecoTofu-SOEM is not built");

        Soem soem = new Soem();
        assumeTrue(soem.load(library), "the shared library can not be loaded : " + library);

        String adapters = soem.find_adapters();

        // a null answer means that the library is from a build which does not
        // export ec_adapters() : an other build than the one of this repository
        assertNotNull(adapters, "ec_adapters() is not answered by " + library);
        assertTrue(adapters.startsWith("{") && adapters.endsWith("}"), adapters);

        // the machine of a developer has at least one adapter, and the name of
        // an adapter is what SOEM opens : it is never an empty string
        if (!"{}".equals(adapters)) {
            assertFalse(adapters.contains("\":\"\""), adapters);
        }
    }
}
