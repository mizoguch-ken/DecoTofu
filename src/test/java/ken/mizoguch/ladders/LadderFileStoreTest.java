package ken.mizoguch.ladders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderFileStore}. The store knows the file of the ladder which the editor
 * shows and the json representation of a ladder file, so the json of a ladder can be written and
 * read again without the javafx controls of the editor.
 */
class LadderFileStoreTest {

    /**
     *
     * @param name name of a ladder without any block
     * @return the json representation of the ladder
     */
    private static LadderJson ladderJson(String name) {
        LadderJson ladderJson = new LadderJson();

        ladderJson.addLadder(new LadderJsonLadder(1, name, 10, 10));
        return ladderJson;
    }

    @Test
    void aWorkFilePathOfADirectoryIsKept(@TempDir Path directory) {
        LadderFileStore store = new LadderFileStore();

        store.setWorkFilePath(directory.toString());

        assertEquals(directory, store.getFilePath());
        assertEquals(directory.toString(), store.getWorkFilePath());
    }

    @Test
    void aWorkFilePathOfAFileTakesItsDirectory(@TempDir Path directory) {
        LadderFileStore store = new LadderFileStore();

        store.setWorkFilePath(directory.resolve("ladder.json").toString());

        assertEquals(directory, store.getFilePath());
    }

    @Test
    void anEmptyWorkFilePathClearsTheFile() {
        LadderFileStore store = new LadderFileStore();

        store.setFilePath(Path.of("ladder.json"));
        store.setWorkFilePath("");

        assertNull(store.getFilePath());
        assertEquals("", store.getWorkFilePath());
        assertNull(store.getFileName());
    }

    @Test
    void noWorkFilePathKeepsTheFile() {
        LadderFileStore store = new LadderFileStore();
        Path file = Path.of("ladder.json");

        store.setFilePath(file);
        store.setWorkFilePath(null);

        assertEquals(file, store.getFilePath());
        assertEquals("ladder.json", store.getFileName());
        assertEquals("ladder", store.getFileBaseName());
    }

    @Test
    void aDirectoryHasNoFileName(@TempDir Path directory) {
        LadderFileStore store = new LadderFileStore();

        store.setFilePath(directory);

        assertNull(store.getFileName());
        assertNull(store.getFileBaseName());
    }

    @Test
    void theJsonOfALadderIsWrittenAndReadAgain(@TempDir Path directory) throws IOException {
        LadderFileStore store = new LadderFileStore();
        Path file = directory.resolve("ladder.json");

        assertTrue(store.writeJson(file, ladderJson("ladder")));
        assertTrue(Files.isRegularFile(file));

        LadderJson ladderJson = store.readJson(file);

        assertNotNull(ladderJson);
        assertEquals(1, ladderJson.getLadders().size());
        assertEquals("ladder", ladderJson.getLadders().get(0).getName());
    }

    @Test
    void theWrittenJsonUsesTheNewLineCharacterOfTheApplication(@TempDir Path directory) throws IOException {
        LadderFileStore store = new LadderFileStore();
        Path file = directory.resolve("ladder.json");

        assertTrue(store.writeJson(file, ladderJson("ladder")));

        String json = Files.readString(file);
        assertTrue(json.contains("\r\n"));
        assertTrue(json.contains("  \""), "the json of a ladder is not indented");
    }

    @Test
    void writeJsonRefusesAnUnknownFile(@TempDir Path directory) {
        LadderFileStore store = new LadderFileStore();

        assertFalse(store.writeJson(directory.resolve("unknown").resolve("ladder.json"), ladderJson("ladder")));
        assertFalse(store.writeJson(null, ladderJson("ladder")));
        assertFalse(store.writeJson(directory.resolve("ladder.json"), null));
    }

    @Test
    void readJsonRefusesAMissingFile(@TempDir Path directory) {
        LadderFileStore store = new LadderFileStore();

        assertNull(store.readJson(directory.resolve("unknown.json")));
        assertNull(store.readJson(null));
        assertNull(store.readJson(directory));
    }

    @Test
    void readJsonRefusesAnInvalidJsonFile(@TempDir Path directory) throws IOException {
        LadderFileStore store = new LadderFileStore();
        Path file = directory.resolve("ladder.json");

        Files.writeString(file, "{ not a ladder json ");

        assertNull(store.readJson(file));
    }

    @Test
    void scriptsAreCountedAndRemovedFromAJsonFile(@TempDir Path directory) throws IOException {
        LadderFileStore store = new LadderFileStore();
        LadderJson ladderJson = ladderJson("ladder");
        Path file = directory.resolve("ladder.json");

        ladderJson.getLadders().get(0).addBlock(new LadderJsonBlock(0, 0, "SCRIPT", false, false, "address", null,
                null, "value(1);"));
        ladderJson.getLadders().get(0).addBlock(new LadderJsonBlock(1, 0, "LOAD", false, false, "address", null,
                null, null));

        assertTrue(store.writeJson(file, ladderJson));

        LadderJson read = store.readJson(file);
        assertEquals(1, LadderFileStore.countScripts(read));
        assertEquals(1, LadderFileStore.removeScripts(read));
        assertEquals(0, LadderFileStore.countScripts(read));
    }

    @Test
    void aFileChooserTakesTheDirectoryOfTheLadder(@TempDir Path directory) {
        LadderFileStore store = new LadderFileStore();

        store.setFilePath(directory.resolve("ladder.json"));

        assertEquals("ladder", store.getFileBaseName());
        assertEquals(1, store.getFileChooser("JSON", "*.json", null).getExtensionFilters().size());
        assertEquals(directory.toFile(), store.getFileChooser("PDF", "*.pdf", "ladder").getInitialDirectory());
    }
}
