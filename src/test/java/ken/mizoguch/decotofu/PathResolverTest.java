package ken.mizoguch.decotofu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PathResolver}.
 */
class PathResolverTest {

    @TempDir
    Path tempDir;

    @Test
    void userPathIsTheCurrentDirectory() {
        assertEquals(Paths.get(System.getProperty("user.dir")), PathResolver.userPath());
    }

    @Test
    void hasMarkerAcceptsAnyMarkerAndNullPath() throws Exception {
        Files.writeString(tempDir.resolve("marker_a.txt"), "a");

        assertTrue(PathResolver.hasMarker(tempDir, "marker_a.txt"));
        assertTrue(PathResolver.hasMarker(tempDir, "marker_b.txt", "marker_a.txt"));
        assertFalse(PathResolver.hasMarker(tempDir, "marker_b.txt"));
        assertFalse(PathResolver.hasMarker(null, "marker_a.txt"));
    }

    @Test
    void findMarkerPathSearchesTheParents() throws Exception {
        Path markerDirectory = Files.createDirectory(tempDir.resolve("app"));

        Files.writeString(markerDirectory.resolve("marker.txt"), "marker");
        Path deep = Files.createDirectories(markerDirectory.resolve("a/b/c"));

        assertEquals(markerDirectory, PathResolver.findMarkerPath(deep, PathResolver.MAX_PARENT_LOOKUP, "marker.txt"));
        // the marker is two parents above : it is not found with a shorter lookup
        assertNull(PathResolver.findMarkerPath(deep, 1, "marker.txt"));
        // a negative lookup searches every parent
        assertEquals(markerDirectory, PathResolver.findMarkerPath(deep, -1, "marker.txt"));
        assertNull(PathResolver.findMarkerPath(deep, -1, "no-such-marker.txt"));
        assertNull(PathResolver.findMarkerPath(null, -1, "marker.txt"));
    }

    @Test
    void applicationPathOfALoadedClassIsADirectory() {
        Path path = PathResolver.applicationPath(PathResolverTest.class);

        assertNotNull(path);
        assertTrue(Files.isDirectory(path));
    }

    @Test
    void applicationPathWithoutClassIsNull() {
        assertNull(PathResolver.applicationPath(null));
    }

    @Test
    void resolveUsesTheCurrentDirectoryWhenItHasTheMarker() throws Exception {
        Files.writeString(tempDir.resolve("resolve_here_marker.txt"), "marker");

        String userDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            assertEquals(tempDir.toAbsolutePath(), PathResolver.resolve(PathResolverTest.class, "resolve_here_marker.txt"));
        } finally {
            System.setProperty("user.dir", userDir);
        }
    }

    @Test
    void resolveFindsTheDirectoryOfTheRunningClasses() {
        String userDir = System.getProperty("user.dir");

        try {
            System.setProperty("user.dir", tempDir.toString());
            Path resolved = PathResolver.resolve(PathResolverTest.class, "pom.xml");

            // the current directory has no marker : the directory of the tests classes
            // is used, so the marker has to be found in the directory of the project
            assertEquals(PathResolver.findMarkerPath(PathResolver.applicationPath(PathResolverTest.class),
                    PathResolver.MAX_PARENT_LOOKUP, "pom.xml"), resolved);
            assertTrue(Files.exists(resolved.resolve("pom.xml")));
        } finally {
            System.setProperty("user.dir", userDir);
        }
    }

    @Test
    void resolveReturnsTheCurrentDirectoryWhenNothingIsFound() {
        String userDir = System.getProperty("user.dir");

        try {
            System.setProperty("user.dir", tempDir.toString());
            assertEquals(tempDir.toAbsolutePath(),
                    PathResolver.resolve(PathResolverTest.class, "decoTofuNoSuchMarkerXyz"));
            assertNotNull(PathResolver.applicationDirectory(PathResolverTest.class));
        } finally {
            System.setProperty("user.dir", userDir);
        }
    }
}
