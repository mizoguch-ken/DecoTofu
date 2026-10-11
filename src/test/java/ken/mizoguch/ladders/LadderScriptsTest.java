package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the scripts of a ladder which comes from a json file
 * ({@link LadderFileStore#countScripts} and {@link LadderFileStore#removeScripts}).
 */
class LadderScriptsTest {

    private static LadderJson ladderJson(String... scripts) {
        LadderJson ladderJson = new LadderJson();
        LadderJsonLadder ladder = new LadderJsonLadder(1, "ladder", 10, 10);
        int index = 0;

        for (String script : scripts) {
            ladder.addBlock(new LadderJsonBlock(index++, 0, "SCRIPT", false, false, "address" + index, null, null,
                    script));
        }
        ladderJson.addLadder(ladder);
        return ladderJson;
    }

    @Test
    void nullAndEmptyLadderJsonHaveNoScript() {
        assertEquals(0, LadderFileStore.countScripts(null));
        assertEquals(0, LadderFileStore.countScripts(new LadderJson()));
        assertEquals(0, LadderFileStore.removeScripts(null));
        assertEquals(0, LadderFileStore.removeScripts(new LadderJson()));
    }

    @Test
    void blocksWithoutScriptAreNotCounted() {
        assertEquals(0, LadderFileStore.countScripts(ladderJson(new String[]{null})));
    }

    @Test
    void scriptBlocksAreCountedAndRemoved() {
        LadderJson ladderJson = ladderJson("value(1);", null, "value(2);");

        assertEquals(2, LadderFileStore.countScripts(ladderJson));
        assertEquals(2, LadderFileStore.removeScripts(ladderJson));
        assertEquals(0, LadderFileStore.countScripts(ladderJson));
        assertEquals(0, LadderFileStore.removeScripts(ladderJson));

        for (LadderJsonBlock block : ladderJson.getLadders().get(0).getBlocks()) {
            assertFalse(block.hasScript());
            assertNull(block.getBlockScript());
        }
    }
}
