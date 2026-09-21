package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderJson} and the LadderJson* model classes.
 */
class LadderJsonTest {

    @Test
    void addLadderIgnoresNullAndSortsByIdx() {
        LadderJson json = new LadderJson();

        json.addLadder(null);
        assertNull(json.getLadders());
        json.sortLadders();

        LadderJsonLadder third = new LadderJsonLadder(Integer.valueOf(3), "three", Integer.valueOf(10), Integer.valueOf(8));
        LadderJsonLadder first = new LadderJsonLadder(Integer.valueOf(1), "one", Integer.valueOf(10), Integer.valueOf(8));

        json.addLadder(third);
        json.addLadder(first);
        json.sortLadders();

        assertEquals(2, json.getLadders().size());
        assertSame(first, json.getLadders().get(0));
        assertSame(third, json.getLadders().get(1));
        assertEquals("one", first.getName());
        assertEquals(Integer.valueOf(10), first.getColumn());
        assertEquals(Integer.valueOf(8), first.getRow());
    }

    @Test
    void commentsAreAddedAndSortedByAddress() {
        LadderJson json = new LadderJson();
        LadderJsonComment second = new LadderJsonComment(Integer.valueOf(2), "b", "second", Double.valueOf(2.0));
        LadderJsonComment firstComment = new LadderJsonComment(Integer.valueOf(1), "a", "first", Double.valueOf(1.0));

        json.addComment(second);
        json.addComment(firstComment);
        json.sortComments();

        assertEquals("a", json.getComments().get(0).getAddress());
        assertSame(firstComment, json.getComments().get(0));
        assertEquals(Integer.valueOf(1), json.getComments().get(0).getIdx());
        assertEquals("first", json.getComments().get(0).getComment());
        assertEquals(Double.valueOf(1.0), json.getComments().get(0).getValue());
        assertSame(second, json.getComments().get(1));

        // current behavior: addComment has no null check, null entries are stored
        json.addComment(null);
        assertEquals(3, json.getComments().size());
        assertNull(json.getComments().get(2));
    }

    @Test
    void historyManagerIntegrationSharesState() {
        LadderJson json = new LadderJson();
        assertNull(json.getHistoryManager());

        LadderHistoryManager manager = new LadderHistoryManager();
        manager.push(new LadderHistory(Ladders.LADDER_COMMAND.LADDER_CREATE), -1);

        json.setHistoryManager(manager, -1);
        assertNotNull(json.getHistoryManager());
        assertSame(manager.getPasts(), json.getHistoryManager().getPasts());

        json.setHistoryManager(manager, 0);
        assertNull(json.getHistoryManager().getPasts());
        // the shared deque is emptied but the caller manager still references the empty deque
        assertTrue(manager.getPasts().isEmpty());
    }

    @Test
    void commentValueNormalization() {
        assertEquals(Double.valueOf(0.0), new LadderJsonComment(Integer.valueOf(1), "a", "c", Double.NaN).getValue());
        assertEquals(Double.valueOf(Double.MAX_VALUE),
                new LadderJsonComment(Integer.valueOf(1), "a", "c", Double.POSITIVE_INFINITY).getValue());
        assertEquals(Double.valueOf(Double.MIN_VALUE),
                new LadderJsonComment(Integer.valueOf(1), "a", "c", Double.NEGATIVE_INFINITY).getValue());
        assertEquals(Double.valueOf(5.0), new LadderJsonComment(Integer.valueOf(1), "a", "c", Double.valueOf(5.0)).getValue());
    }

    @Test
    void commentWithNullValueThrows() {
        assertThrows(NullPointerException.class,
                () -> new LadderJsonComment(Integer.valueOf(1), "a", "c", null));
    }

    @Test
    void blockDefaultsFromLadderGridInitialValues() {
        LadderJsonBlock block = new LadderJsonBlock(Integer.valueOf(2), Integer.valueOf(3));

        assertEquals(Integer.valueOf(2), block.getColumnIndex());
        assertEquals(Integer.valueOf(3), block.getRowIndex());
        assertEquals("EMPTY", block.getBlock());
        assertFalse(block.isVertical());
        assertFalse(block.isVerticalOr());
        assertEquals("", block.getAddress());
        assertNull(block.getComment());
        assertNull(block.getBlockFunctions());
        assertNull(block.getBlockScript());
    }

    @Test
    void blockFunctionConversionStopsAtFirstUnusableEntry() {
        LadderGrid.BlockFunction number = new LadderGrid.BlockFunction();
        number.setNumber(true);
        number.setRadix(16);
        number.setValue(256.0);

        LadderGrid.BlockFunction address = new LadderGrid.BlockFunction();
        address.setAddress("X1");

        LadderGrid.BlockFunction empty = new LadderGrid.BlockFunction();
        LadderGrid.BlockFunction afterBreak = new LadderGrid.BlockFunction();
        afterBreak.setAddress("ignored");

        List<LadderGrid.BlockFunction> functions = new ArrayList<>();
        functions.add(number);
        functions.add(address);
        functions.add(empty);
        functions.add(afterBreak);

        LadderJsonBlock block = new LadderJsonBlock(Integer.valueOf(0), Integer.valueOf(0), "LD",
                Boolean.TRUE, Boolean.FALSE, "A", "comment", functions, "script");

        assertEquals("LD", block.getBlock());
        assertTrue(block.isVertical());
        assertFalse(block.isVerticalOr());
        assertEquals("A", block.getAddress());
        assertEquals("comment", block.getComment());
        assertEquals("script", block.getBlockScript());

        List<LadderJsonBlock.JsonBlockFunction> converted = block.getBlockFunctions();
        assertEquals(2, converted.size());
        assertNull(converted.get(0).address);
        assertEquals(Double.valueOf(256.0), converted.get(0).value);
        assertEquals(Integer.valueOf(16), converted.get(0).radix);
        assertEquals("X1", converted.get(1).address);
        assertNull(converted.get(1).value);
        assertNull(converted.get(1).radix);
    }

    @Test
    void blockFunctionNumberWithoutRadixStopsConversion() {
        LadderGrid.BlockFunction numberDefaultRadix = new LadderGrid.BlockFunction();
        numberDefaultRadix.setNumber(true);
        numberDefaultRadix.setValue(9.0);

        LadderGrid.BlockFunction withAddress = new LadderGrid.BlockFunction();
        withAddress.setAddress("Y2");

        List<LadderGrid.BlockFunction> functions = new ArrayList<>();
        functions.add(withAddress);

        LadderJsonBlock block = new LadderJsonBlock(Integer.valueOf(0), Integer.valueOf(0), "OUT",
                Boolean.FALSE, Boolean.FALSE, null, null, functions, null);

        List<LadderJsonBlock.JsonBlockFunction> converted = block.getBlockFunctions();
        assertEquals(1, converted.size());
        assertEquals("Y2", converted.get(0).address);

        functions = new ArrayList<>();
        functions.add(numberDefaultRadix);
        block = new LadderJsonBlock(Integer.valueOf(0), Integer.valueOf(0), "OUT",
                Boolean.FALSE, Boolean.FALSE, null, null, functions, null);
        assertTrue(block.getBlockFunctions().isEmpty());
    }

    @Test
    void ladderBlocksIgnoreNullAndSortByColumnRowKey() {
        LadderJsonLadder ladder = new LadderJsonLadder(Integer.valueOf(1), "main", Integer.valueOf(3), Integer.valueOf(2));

        ladder.addBlock(null);
        assertNull(ladder.getBlocks());

        LadderJsonBlock late = new LadderJsonBlock(Integer.valueOf(1), Integer.valueOf(1));
        LadderJsonBlock early = new LadderJsonBlock(Integer.valueOf(2), Integer.valueOf(0));
        LadderJsonBlock middle = new LadderJsonBlock(Integer.valueOf(0), Integer.valueOf(1));

        ladder.addBlock(late);
        ladder.addBlock(early);
        ladder.addBlock(middle);
        ladder.sortBlocks(3, 0);

        assertEquals(3, ladder.getBlocks().size());
        assertSame(early, ladder.getBlocks().get(0));
        assertSame(middle, ladder.getBlocks().get(1));
        assertSame(late, ladder.getBlocks().get(2));
    }

    @Test
    void ladderConstructorVariants() {
        LadderJsonLadder single = new LadderJsonLadder(Integer.valueOf(7));
        assertEquals(Integer.valueOf(7), single.getIdx());
        assertNull(single.getName());

        LadderJsonLadder named = new LadderJsonLadder(Integer.valueOf(7), "name");
        assertEquals("name", named.getName());

        LadderJsonLadder indexes = new LadderJsonLadder(Integer.valueOf(7), Integer.valueOf(1), Integer.valueOf(2));
        assertEquals(Integer.valueOf(1), indexes.getColumnIndex());
        assertEquals(Integer.valueOf(2), indexes.getRowIndex());

        LadderJsonLadder addressed = new LadderJsonLadder(Integer.valueOf(7), "%IX1.0", "comment");
        assertEquals("%IX1.0", addressed.getAddress());
        assertEquals("comment", addressed.getComment());
    }
}
