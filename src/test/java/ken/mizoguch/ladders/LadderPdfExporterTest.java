package ken.mizoguch.ladders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderPdfExporter} : the pages of the pdf are drawn from the json
 * representation of the ladders, so the nodes of the editor of the ladders are not needed.
 */
class LadderPdfExporterTest {

    private static final int GRID_COLUMN = 10;
    private static final int GRID_ROW = 10;

    /**
     * a json ladder with the given names, without any block : a page is drawn for each
     * ladder, so the drawing of the blocks is not used
     *
     * @param names
     * @return
     */
    private static LadderJson ladderJson(String... names) {
        LadderJson ladderJson = new LadderJson();

        for (int index = 0; index < names.length; index++) {
            ladderJson.addLadder(new LadderJsonLadder(index, names[index], GRID_COLUMN, GRID_ROW));
        }
        return ladderJson;
    }

    @Test
    void anExportWithoutFileAnswersNull(@TempDir Path directory) {
        LadderPdfExporter exporter = new LadderPdfExporter(ladderJson("ladder"), "ladder.json",
                new CopyOnWriteArrayList<>());

        assertNull(exporter.export(null));
    }

    @Test
    void laddersWithoutBlocksAreExported(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("ladders.pdf");
        LadderPdfExporter exporter = new LadderPdfExporter(ladderJson("ladder_1", "ladder_2"), "ladders.json",
                new CopyOnWriteArrayList<>());

        // the given file is answered when the pdf is written
        assertSame(file, exporter.export(file));
        assertTrue(Files.exists(file));
        assertEquals("%PDF-", new String(Files.readAllBytes(file), StandardCharsets.US_ASCII).substring(0, 5));
        // the font of the pages is embedded : a page without block is not a empty file
        assertTrue(Files.size(file) > 1024, "the pdf of the two ladders is too small");
    }

    @Test
    void anExportOfANotWritableFileAnswersNull(@TempDir Path directory) {
        Path file = directory.resolve("directory.pdf").resolve("ladder.pdf");
        LadderPdfExporter exporter = new LadderPdfExporter(ladderJson("ladder"), null,
                new CopyOnWriteArrayList<>());

        // the parent directory does not exist : the error of the writing is not thrown
        assertNull(exporter.export(file));
    }
}
