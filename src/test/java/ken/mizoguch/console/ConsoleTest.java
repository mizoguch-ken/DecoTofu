package ken.mizoguch.console;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Console}.
 */
class ConsoleTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        Console.close();
    }

    @Test
    void setPrintStreamCreatesFileAndWritesLines() throws Exception {
        Path file = tempDir.resolve("console.log");

        assertTrue(Console.setPrintStream(file));
        Console.write("ConsoleTest", "hello", false);
        Console.write("ConsoleTest", "bad", true);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).matches(
                "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} :: ConsoleTest :: hello"));
        assertTrue(lines.get(1).matches(
                "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} :: ConsoleTest Error :: bad"));
    }

    @Test
    void setPrintStreamRequiresExistingParent() {
        assertFalse(Console.setPrintStream(tempDir.resolve("missing").resolve("console.log")));
        assertFalse(Console.setPrintStream(Paths.get("console.log")));
    }

    @Test
    void setPrintStreamNullClosesStreamAndReturnsFalse() throws Exception {
        Path file = tempDir.resolve("console.log");

        assertTrue(Console.setPrintStream(file));
        Console.write("ConsoleTest", "open", false);
        assertFalse(Console.setPrintStream(null));
        assertFalse(Console.setPrintStream(null));

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(1, lines.size());
    }

    @Test
    void writeStackTraceWritesToActiveStream() throws Exception {
        Path file = tempDir.resolve("trace.log");
        assertTrue(Console.setPrintStream(file));

        RuntimeException throwable = new RuntimeException("trace-message");
        Console.writeStackTrace(ConsoleTest.class.getName(), throwable);

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("java.lang.RuntimeException: trace-message"));
        assertTrue(content.contains(ConsoleTest.class.getName() + " writeStackTraceWritesToActiveStream"));
    }

    @Test
    void writeFallsBackToLoggerWhenStreamClosed() {
        assertDoesNotThrow(() -> {
            Console.write("ConsoleTest", "logged", true);
            Console.write("ConsoleTest", "logged", false);
            Console.writeStackTrace(ConsoleTest.class.getName(), new RuntimeException("logged"));
        });
    }
}
