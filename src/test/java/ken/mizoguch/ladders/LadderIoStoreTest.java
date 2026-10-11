package ken.mizoguch.ladders;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderIoStore}. The store holds the io of the ladders without any javafx
 * control : the editor of the ladders adds the rows of the maps, and the javascript of the script
 * blocks writes the values of the io of the script.
 */
class LadderIoStoreTest {

    /**
     * the index of the first ladder : the row of the global addresses is the first one
     */
    private static final int LADDER_IDX = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX + 1;

    /**
     * store with the row of the global addresses and the row of one ladder, as the editor builds it
     *
     * @param addresses addresses registered in the global io map
     * @return the store of the io
     */
    private static LadderIoStore store(String... addresses) {
        LadderIoStore store = new LadderIoStore(new Object());

        store.addGlobalRow();
        store.getIoMap().add(new ConcurrentHashMap<>());
        store.getCommentMap().add(new ConcurrentHashMap<>());
        store.getScriptIoMap().add(new ConcurrentHashMap<>());
        for (String address : addresses) {
            store.createIo(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, address);
        }
        return store;
    }

    @Test
    void addGlobalRowAddsARowToEveryMap() {
        LadderIoStore store = new LadderIoStore(new Object());

        assertTrue(store.isEmpty());

        store.addGlobalRow();

        assertEquals(1, store.getIoMap().size());
        assertEquals(1, store.getCommentMap().size());
        assertEquals(1, store.getScriptIoMap().size());
        assertFalse(store.isEmpty());
    }

    @Test
    void createIoRegistersAnAddressOnlyOnce() {
        LadderIoStore store = store();

        assertTrue(store.createIo(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
        assertFalse(store.createIo(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
        assertNotNull(store.getIoMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT"));
        assertFalse(store.isEmpty());
    }

    @Test
    void ioAnswersNullForAnUnknownAddress() {
        LadderIoStore store = store("OUT");

        assertNotNull(store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
        assertEquals("OUT", store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT").getAddress());
        assertNull(store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "UNKNOWN"));
    }

    @Test
    void globalIoAnswersTheIoOfAGlobalAddress() {
        LadderIoStore store = store("OUT");

        assertNotNull(store.globalIo("OUT"));
        assertNull(store.globalIo("UNKNOWN"));
        assertNull(store.globalIo(null));
    }

    @Test
    void globalIoAnswersNullWhenNoAddressIsRegistered() {
        LadderIoStore store = new LadderIoStore(new Object());

        store.addGlobalRow();
        store.getIoMap().clear();

        assertNull(store.globalIo("OUT"));
    }

    @Test
    void valueAnswersTheValueOfAGlobalAddress() {
        LadderIoStore store = store("OUT");

        assertNull(store.value("UNKNOWN"));
        store.setDirect(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT", 12.0);
        assertEquals(12.0, store.value("OUT"));
        assertEquals(12.0, store.value(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
    }

    @Test
    void valueRefusesALocalAddress() {
        LadderIoStore store = store();
        String local = LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX + "IN";

        assertTrue(store.createIo(LADDER_IDX, local));
        store.setDirect(LADDER_IDX, local, 7.0);

        assertNull(store.value(local));
        assertEquals(7.0, store.value(LADDER_IDX, local));
    }

    @Test
    void setDirectChangesTheIoAndCyclesTheScriptIo() {
        LadderIoStore store = store("OUT");

        store.setDirect(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT", 5.0);

        assertEquals(5.0, store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT").getValue());
        assertTrue(store.getScriptIoMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT").isCycled());
        assertEquals(5.0, store.getScriptIoMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT").getValue());
    }

    @Test
    void setScriptKeepsTheIoValueAndWaitsForTheNextCycle() {
        LadderIoStore store = store("OUT");

        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT").setValue(3.0);
        store.setScript(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT", 9.0);

        assertEquals(3.0, store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT").getValue());
        assertFalse(store.getScriptIoMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT").isCycled());
        assertEquals(9.0, store.getScriptIoMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT").getValue());
    }

    @Test
    void commentAnswersTheCommentOfAnAddress() {
        LadderIoStore store = store("OUT");

        assertFalse(store.hasComment(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
        assertNull(store.comment(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));

        store.getCommentMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).put("OUT", "a comment");

        assertTrue(store.hasComment(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
        assertEquals("a comment", store.comment(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
    }

    @Test
    void clearRemovesEveryRow() {
        LadderIoStore store = store("OUT");

        store.clear();

        assertTrue(store.getIoMap().isEmpty());
        assertTrue(store.getCommentMap().isEmpty());
        assertTrue(store.getScriptIoMap().isEmpty());
        assertTrue(store.isEmpty());
    }

    @Test
    void mapsAreTheLiveOnesOfTheStore() {
        LadderIoStore store = store();
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap = store.getIoMap();

        // the editor and the cycle of the ladder change the maps they get from the store
        ioMap.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).put("OUT", new LadderIo("OUT"));

        assertSame(ioMap, store.getIoMap());
        assertNotNull(store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT"));
    }

    @Test
    void applyScriptIoGivesTheValueOfAScriptToTheIoOfTheLadder() {
        LadderIoStore store = store();

        store.createIo(LADDER_IDX, "LOCAL");
        store.setScript(LADDER_IDX, "LOCAL", 7.0);

        store.applyScriptIo();

        assertEquals(7.0, store.io(LADDER_IDX, "LOCAL").getValue());
        assertTrue(store.getScriptIoMap().get(LADDER_IDX).get("LOCAL").isCycled());

        // the io of a script which is waited by a cycle is consumed by the next cycle
        store.applyScriptIo();

        assertFalse(store.getScriptIoMap().get(LADDER_IDX).containsKey("LOCAL"));
        assertEquals(7.0, store.io(LADDER_IDX, "LOCAL").getValue());
    }

    @Test
    void applyScriptIoConsumesTheIoOfADirectValueAtOnce() {
        LadderIoStore store = store();

        store.createIo(LADDER_IDX, "LOCAL");
        // a direct value is written in the io of the ladder by the script and its io of
        // script is already cycled : the cycle only has to remove it
        store.setDirect(LADDER_IDX, "LOCAL", 3.0);

        assertEquals(3.0, store.io(LADDER_IDX, "LOCAL").getValue());

        store.applyScriptIo();

        assertFalse(store.getScriptIoMap().get(LADDER_IDX).containsKey("LOCAL"));
        assertEquals(3.0, store.io(LADDER_IDX, "LOCAL").getValue());
    }

    @Test
    void applyScriptIoRemovesTheIoOfAScriptWhoseAddressIsGone() {
        LadderIoStore store = store();

        // an address can be removed from the io of a ladder while the cycle is running
        store.getScriptIoMap().get(LADDER_IDX).put("GONE", new LadderIo("GONE"));

        store.applyScriptIo();

        assertFalse(store.getScriptIoMap().get(LADDER_IDX).containsKey("GONE"));
    }

    @Test
    void applyScriptIoRunsOnEveryRowOfTheMaps() {
        LadderIoStore store = store("OUT");

        store.createIo(LADDER_IDX, "LOCAL");
        store.setScript(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT", 5.0);
        store.setScript(LADDER_IDX, "LOCAL", 6.0);

        store.applyScriptIo();

        assertEquals(5.0, store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "OUT").getValue());
        assertEquals(6.0, store.io(LADDER_IDX, "LOCAL").getValue());
        assertTrue(store.getScriptIoMap().get(LADDER_IDX).get("LOCAL").isCycled());
    }

    @Test
    void addLadderRowAddsARowToEveryMap() {
        LadderIoStore store = new LadderIoStore(new Object());

        store.addGlobalRow();
        store.addLadderRow(LADDER_IDX);

        assertEquals(2, store.getIoMap().size());
        assertEquals(2, store.getCommentMap().size());
        assertEquals(2, store.getScriptIoMap().size());
        assertNotNull(store.getIoMap().get(LADDER_IDX));
        // the row of the global addresses keeps its place at the begin of the maps
        assertTrue(store.getIoMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).isEmpty());
    }

    @Test
    void removeLadderRowRemovesTheRowOfEveryMap() {
        LadderIoStore store = store("OUT");

        store.createIo(LADDER_IDX, "LOCAL");
        store.getCommentMap().get(LADDER_IDX).put("LOCAL", "a comment");
        store.setScript(LADDER_IDX, "LOCAL", 7.0);

        store.removeLadderRow(LADDER_IDX);

        assertEquals(1, store.getIoMap().size());
        assertEquals(1, store.getCommentMap().size());
        assertEquals(1, store.getScriptIoMap().size());
        // the row of the ladder is gone : only the row of the global addresses is left
        assertNull(store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "LOCAL"));
        assertTrue(store.getScriptIoMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).isEmpty());
    }

    @Test
    void swapLadderRowsExchangesTheRowOfEveryMap() {
        LadderIoStore store = store("OUT");

        store.createIo(LADDER_IDX, "LOCAL");
        store.io(LADDER_IDX, "LOCAL").setValue(3.0);
        store.getCommentMap().get(LADDER_IDX).put("LOCAL", "a comment");
        store.getCommentMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).put("OUT", "the global io");

        store.swapLadderRows(LADDER_IDX, LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX);

        // the io of a ladder follows its ladder : the rows are exchanged inside the maps
        // which the cycle of the ladder reads, the maps are never replaced
        assertEquals(3.0, store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "LOCAL").getValue());
        assertNotNull(store.io(LADDER_IDX, "OUT"));
        assertNull(store.io(LADDER_IDX, "LOCAL"));
        assertEquals("a comment", store.comment(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "LOCAL"));
        assertEquals("the global io", store.comment(LADDER_IDX, "OUT"));
    }

    @Test
    void rowsAreChangedInTheMapsWhichTheCallerGives() {
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap = new CopyOnWriteArrayList<>();
        ConcurrentHashMap<String, LadderIo> globalRow = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, LadderIo> ladderRow = new ConcurrentHashMap<>();

        // the difference of two ladders shows its own maps without the io of the scripts
        ladderRow.put("LOCAL", new LadderIo("LOCAL"));
        ioMap.add(globalRow);
        ioMap.add(ladderRow);
        commentMap.add(new ConcurrentHashMap<>());
        commentMap.add(new ConcurrentHashMap<>());

        LadderIoStore.addRows(ioMap, commentMap, null, LADDER_IDX + 1);
        assertEquals(3, ioMap.size());
        assertEquals(3, commentMap.size());

        LadderIoStore.swapRows(ioMap, commentMap, null, LADDER_IDX, LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX);
        assertSame(ladderRow, ioMap.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX));
        assertSame(globalRow, ioMap.get(LADDER_IDX));

        LadderIoStore.removeRows(ioMap, commentMap, null, LADDER_IDX + 1);
        assertEquals(2, ioMap.size());
        assertEquals(2, commentMap.size());
    }
}
