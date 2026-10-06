package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the scripts of a ladder which comes from a json file
 * ({@link Ladders#countLadderScripts} and {@link Ladders#removeLadderScripts}).
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
        assertEquals(0, Ladders.countLadderScripts(null));
        assertEquals(0, Ladders.countLadderScripts(new LadderJson()));
        assertEquals(0, Ladders.removeLadderScripts(null));
        assertEquals(0, Ladders.removeLadderScripts(new LadderJson()));
    }

    @Test
    void blocksWithoutScriptAreNotCounted() {
        assertEquals(0, Ladders.countLadderScripts(ladderJson(new String[]{null})));
    }

    @Test
    void scriptBlocksAreCountedAndRemoved() {
        LadderJson ladderJson = ladderJson("value(1);", null, "value(2);");

        assertEquals(2, Ladders.countLadderScripts(ladderJson));
        assertEquals(2, Ladders.removeLadderScripts(ladderJson));
        assertEquals(0, Ladders.countLadderScripts(ladderJson));
        assertEquals(0, Ladders.removeLadderScripts(ladderJson));

        for (LadderJsonBlock block : ladderJson.getLadders().get(0).getBlocks()) {
            assertFalse(block.hasScript());
            assertNull(block.getBlockScript());
        }
    }
}
