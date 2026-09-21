package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderHistoryManager} and {@link LadderHistory}.
 */
class LadderHistoryManagerTest {

    private static LadderHistory history(Ladders.LADDER_COMMAND command) {
        return new LadderHistory(command);
    }

    @Test
    void historyStoresCommandAndJsonLadders() {
        LadderHistory ladderHistory = history(Ladders.LADDER_COMMAND.BLOCK_CHANGE);

        assertSame(Ladders.LADDER_COMMAND.BLOCK_CHANGE, ladderHistory.getCommand());
        assertNull(ladderHistory.getOriginal());
        assertNull(ladderHistory.getRevised());

        LadderJsonLadder original = new LadderJsonLadder(Integer.valueOf(1));
        LadderJsonLadder revised = new LadderJsonLadder(Integer.valueOf(2));
        ladderHistory.setOriginal(original);
        ladderHistory.setRevised(revised);
        assertSame(original, ladderHistory.getOriginal());
        assertSame(revised, ladderHistory.getRevised());
    }

    @Test
    void constructorStartsWithoutDequeues() {
        LadderHistoryManager manager = new LadderHistoryManager();

        assertNull(manager.getPasts());
        assertNull(manager.getFutures());
    }

    @Test
    void pushAddsToPastsHeadAndClearsFutures() {
        LadderHistoryManager manager = new LadderHistoryManager();
        LadderHistory first = history(Ladders.LADDER_COMMAND.LADDER_CREATE);
        LadderHistory second = history(Ladders.LADDER_COMMAND.LADDER_REMOVE);

        manager.push(first, -1);
        manager.push(second, -1);

        assertNull(manager.getFutures());
        assertEquals(2, manager.getPasts().size());
        assertSame(second, manager.getPasts().peekFirst());
        assertSame(first, manager.getPasts().peekLast());
    }

    @Test
    void pushTrimsPastsToGenerationLimit() {
        LadderHistoryManager manager = new LadderHistoryManager();
        LadderHistory first = history(Ladders.LADDER_COMMAND.CHANGE_ADDRESS);
        LadderHistory second = history(Ladders.LADDER_COMMAND.CHANGE_COMMENT);
        LadderHistory third = history(Ladders.LADDER_COMMAND.BLOCK_CHANGE);

        manager.push(first, 2);
        manager.push(second, 2);
        manager.push(third, 2);

        assertEquals(2, manager.getPasts().size());
        assertSame(third, manager.getPasts().peekFirst());
        assertSame(second, manager.getPasts().peekLast());
    }

    @Test
    void pushWithGenerationZeroLeavesNoPasts() {
        LadderHistoryManager manager = new LadderHistoryManager();

        manager.push(history(Ladders.LADDER_COMMAND.LADDER_CREATE), 0);

        assertTrue(manager.getPasts().isEmpty());
        assertNull(manager.getFutures());
    }

    @Test
    void undoIsDisabledForGenerationZero() {
        LadderHistoryManager manager = new LadderHistoryManager();
        LadderHistory first = history(Ladders.LADDER_COMMAND.LADDER_CREATE);

        manager.push(first, -1);
        assertNull(manager.undo(0));
        assertEquals(1, manager.getPasts().size());
    }

    @Test
    void undoWithoutHistoryReturnsNull() {
        LadderHistoryManager manager = new LadderHistoryManager();

        assertNull(manager.undo(-1));
        assertNull(manager.redo(-1));
    }

    @Test
    void undoRedoRoundTrip() {
        LadderHistoryManager manager = new LadderHistoryManager();
        LadderHistory first = history(Ladders.LADDER_COMMAND.LADDER_CREATE);
        LadderHistory second = history(Ladders.LADDER_COMMAND.LADDER_REMOVE);

        manager.push(first, -1);
        manager.push(second, -1);

        assertSame(second, manager.undo(-1));
        assertSame(first, manager.getPasts().peekFirst());
        assertEquals(1, manager.getFutures().size());

        assertSame(first, manager.undo(-1));
        assertNull(manager.getPasts());
        assertSame(first, manager.redo(-1));
        assertSame(second, manager.redo(-1));
        assertNull(manager.getFutures());
        assertEquals(2, manager.getPasts().size());
        assertSame(second, manager.getPasts().peekFirst());
    }

    @Test
    void undoTrimsFuturesToGenerationLimit() {
        LadderHistoryManager manager = new LadderHistoryManager();
        LadderHistory first = history(Ladders.LADDER_COMMAND.LADDER_CREATE);
        LadderHistory second = history(Ladders.LADDER_COMMAND.LADDER_REMOVE);
        LadderHistory third = history(Ladders.LADDER_COMMAND.LADDER_INSERT_ROW);

        manager.push(first, -1);
        manager.push(second, -1);
        manager.push(third, -1);

        manager.undo(1);
        manager.undo(1);
        // generation trims the oldest futures from the tail, keeping the newest head entry
        assertEquals(1, manager.getFutures().size());
        assertSame(second, manager.getFutures().peekFirst());
    }

    @Test
    void pushAfterUndoClearsFutures() {
        LadderHistoryManager manager = new LadderHistoryManager();
        LadderHistory first = history(Ladders.LADDER_COMMAND.LADDER_CREATE);
        LadderHistory second = history(Ladders.LADDER_COMMAND.LADDER_REMOVE);

        manager.push(first, -1);
        manager.push(second, -1);
        manager.undo(-1);
        assertNotNull(manager.getFutures());

        manager.push(history(Ladders.LADDER_COMMAND.BLOCK_CHANGE), -1);
        assertNull(manager.getFutures());
    }

    @Test
    void clearRemovesAllHistory() {
        LadderHistoryManager manager = new LadderHistoryManager();

        manager.push(history(Ladders.LADDER_COMMAND.LADDER_CREATE), -1);
        manager.push(history(Ladders.LADDER_COMMAND.LADDER_REMOVE), -1);
        manager.undo(-1);
        manager.clear();

        assertNull(manager.getPasts());
        assertNull(manager.getFutures());
    }

    @Test
    void setSharesDequeReferencesForUnlimitedGeneration() {
        LadderHistoryManager source = new LadderHistoryManager();
        LadderHistoryManager copy = new LadderHistoryManager();
        LadderHistory first = history(Ladders.LADDER_COMMAND.LADDER_CREATE);

        source.push(first, -1);
        copy.set(source, -1);

        assertSame(source.getPasts(), copy.getPasts());
        assertNull(copy.getFutures());
        assertEquals(1, copy.getPasts().size());
    }

    @Test
    void setWithGenerationZeroClearsSharedPasts() {
        LadderHistoryManager source = new LadderHistoryManager();
        LadderHistoryManager copy = new LadderHistoryManager();

        source.push(history(Ladders.LADDER_COMMAND.LADDER_CREATE), -1);
        source.push(history(Ladders.LADDER_COMMAND.LADDER_REMOVE), -1);
        copy.set(source, 0);

        assertNull(copy.getPasts());
        // the shared deque is emptied but source still references the empty deque
        assertTrue(source.getPasts().isEmpty());
    }

    @Test
    void setTrimsSharedFuturesToGenerationLimit() {
        LadderHistoryManager source = new LadderHistoryManager();
        LadderHistoryManager copy = new LadderHistoryManager();

        source.push(history(Ladders.LADDER_COMMAND.LADDER_CREATE), -1);
        source.push(history(Ladders.LADDER_COMMAND.LADDER_REMOVE), -1);
        source.push(history(Ladders.LADDER_COMMAND.LADDER_INSERT_ROW), -1);
        source.undo(-1);
        source.undo(-1);
        source.undo(-1);
        assertNull(source.getPasts());
        assertEquals(3, source.getFutures().size());

        copy.set(source, 2);
        assertSame(source.getFutures(), copy.getFutures());
        assertEquals(2, copy.getFutures().size());
        assertNull(copy.getPasts());
    }
}
