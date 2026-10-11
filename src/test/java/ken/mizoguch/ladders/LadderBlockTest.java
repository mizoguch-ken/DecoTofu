package ken.mizoguch.ladders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * the classification of the blocks of the ladders, used to run the route of a ladder
 */
class LadderBlockTest {

    @Test
    void contactsAreLoads() {
        assertTrue(LadderBlock.LOAD.isLoad());
        assertTrue(LadderBlock.LOAD_RISING_NOT.isLoad());
        assertTrue(LadderBlock.LOAD_FALLING_NOT.isLoad());
        assertFalse(LadderBlock.COMPARISON_EQUAL.isLoad());
        assertFalse(LadderBlock.OUT.isLoad());
    }

    @Test
    void comparisonsAreComparisons() {
        assertTrue(LadderBlock.COMPARISON_EQUAL.isComparison());
        assertTrue(LadderBlock.COMPARISON_XOR_BITS.isComparison());
        assertFalse(LadderBlock.LOAD.isComparison());
        assertFalse(LadderBlock.AND_BITS.isComparison());
    }

    /**
     * the route of a ladder stops on a contact or on a comparison, on nothing else
     */
    @Test
    void onlyContactsAndComparisonsStopTheRoute() {
        for (LadderBlock block : LadderBlock.values()) {
            assertEquals(block.isLoad() || block.isComparison(), block.isContact(), block.toCommand());
        }
        assertFalse(LadderBlock.CONNECT_LINE.isContact());
        assertFalse(LadderBlock.OUT.isContact());
        assertFalse(LadderBlock.MOVE.isContact());
        assertFalse(LadderBlock.SCRIPT.isContact());
    }
}
