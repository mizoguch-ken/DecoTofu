package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Ladder}. The blocks which write an io are put in the list of the
 * blocks run by the cycle with {@link Ladder#setOutGrids}, so that the cycle of the ladder
 * is answered without the javafx controls used to connect it.
 */
class LadderTest {

    /**
     * one millisecond of the ladder cycle : the cycle time is used in nanoseconds by the
     * timer blocks of a ladder
     */
    private static final long CYCLE_TIME = 1000000L;

    /**
     * io map as the application builds it : the first index holds the global addresses,
     * an other index holds the addresses local to a ladder
     *
     * @param addresses addresses registered in the global io map
     * @return the io map
     */
    private static CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap(String... addresses) {
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap = new CopyOnWriteArrayList<>();

        ioMap.add(new ConcurrentHashMap<>());
        ioMap.add(new ConcurrentHashMap<>());
        for (String address : addresses) {
            ioMap.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).put(address, new LadderIo(address));
        }
        return ioMap;
    }

    /**
     * block which writes an io : the connect number one makes it the begin of a route, so
     * the cycle runs it without an other block
     *
     * @param column
     * @param row
     * @param block
     * @param address
     * @return the block of the ladder
     */
    private static LadderGrid outGrid(int column, int row, LadderBlock block, String address) {
        LadderGrid grid = new LadderGrid(column, row);

        grid.setBlock(block);
        grid.setAddress(address);
        grid.setInConnectNumber(1);
        return grid;
    }

    /**
     * @param grids
     * @return the list of the blocks run by the cycle
     */
    private static List<LadderGrid> grids(LadderGrid... grids) {
        List<LadderGrid> list = new ArrayList<>();

        for (LadderGrid grid : grids) {
            list.add(grid);
        }
        return list;
    }

    @Test
    void outBlockSetsTheValueOfItsIo() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid out = outGrid(1, 1, LadderBlock.OUT, "OUT");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("OUT");

        ladder.addLadderGrid(out);
        ladder.setOutGrids(grids(out));
        ladder.run(map, CYCLE_TIME);

        assertEquals(1.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT").getValue());
        assertTrue(map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT").isLd());
    }

    @Test
    void blockWithoutItsIoIsIgnored() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid out = outGrid(1, 1, LadderBlock.OUT, "OUT");
        LadderGrid missing = outGrid(2, 1, LadderBlock.OUT, "MISSING");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("OUT");

        ladder.addLadderGrid(out);
        ladder.addLadderGrid(missing);
        ladder.setOutGrids(grids(missing, out));
        // the io of the second block is not registered : the cycle answers the other
        // blocks of the ladder and no io is added by the cycle
        ladder.run(map, CYCLE_TIME);
        ladder.run(map, CYCLE_TIME);

        assertEquals(1.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("OUT").getValue());
        assertFalse(map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).containsKey("MISSING"));
    }

    @Test
    void blockWithANumberAsItsAddressIsIgnored() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid number = outGrid(1, 1, LadderBlock.OUT, "1.5");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("OUT");

        ladder.addLadderGrid(number);
        ladder.setOutGrids(grids(number));
        ladder.run(map, CYCLE_TIME);

        // a number is not an address of the io map : the block is reported and ignored
        assertFalse(map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).containsKey("1.5"));
    }

    @Test
    void blockWithoutTheIoOfItsFunctionIsIgnored() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid addition = outGrid(1, 1, LadderBlock.ADDITION, "SUM");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("SUM");

        addition.getBlockFunctions()[0].setAddress("MISSING");
        addition.getBlockFunctions()[1].setNumber(true);
        addition.getBlockFunctions()[1].setValue(2.0);
        ladder.addLadderGrid(addition);
        ladder.setOutGrids(grids(addition));
        ladder.run(map, CYCLE_TIME);

        // the function of the block has no io : the addition is not run
        assertEquals(0.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("SUM").getValue());
    }

    @Test
    void additionWithRegisteredFunctionsSetsItsIo() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid addition = outGrid(1, 1, LadderBlock.ADDITION, "SUM");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("SUM", "A");

        map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("A").setValue(3.0);
        addition.getBlockFunctions()[0].setAddress("A");
        addition.getBlockFunctions()[1].setNumber(true);
        addition.getBlockFunctions()[1].setValue(2.0);
        ladder.addLadderGrid(addition);
        ladder.setOutGrids(grids(addition));
        ladder.run(map, CYCLE_TIME);

        assertEquals(5.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("SUM").getValue());
    }

    /**
     * the two functions of the block are written as numbers : no io is read by the cycle
     */
    @Test
    void additionWithTwoNumberFunctionsSetsItsIo() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid addition = outGrid(1, 1, LadderBlock.ADDITION, "SUM");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("SUM");

        addition.getBlockFunctions()[0].setNumber(true);
        addition.getBlockFunctions()[0].setValue(3.0);
        addition.getBlockFunctions()[1].setNumber(true);
        addition.getBlockFunctions()[1].setValue(2.0);
        ladder.addLadderGrid(addition);
        ladder.setOutGrids(grids(addition));
        ladder.run(map, CYCLE_TIME);

        assertEquals(5.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("SUM").getValue());
    }

    /**
     * the two functions of the block are addresses : the cycle reads the two io of the
     * global addresses of the application
     */
    @Test
    void additionWithTwoIoFunctionsSetsItsIo() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid addition = outGrid(1, 1, LadderBlock.ADDITION, "SUM");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("SUM", "A", "B");

        map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("A").setValue(3.0);
        map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("B").setValue(2.0);
        addition.getBlockFunctions()[0].setAddress("A");
        addition.getBlockFunctions()[1].setAddress("B");
        ladder.addLadderGrid(addition);
        ladder.setOutGrids(grids(addition));
        ladder.run(map, CYCLE_TIME);

        assertEquals(5.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("SUM").getValue());
    }

    /**
     * the io of the second function is local to the ladder : the cycle reads the io map of
     * the ladder for that function and the global io map for the address of the block
     */
    @Test
    void additionWithALocalIoFunctionSetsItsIo() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid addition = outGrid(1, 1, LadderBlock.ADDITION, "SUM");
        String local = LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX + "A";
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("SUM");

        map.get(1).put(local, new LadderIo(local));
        map.get(1).get(local).setValue(3.0);
        addition.getBlockFunctions()[0].setNumber(true);
        addition.getBlockFunctions()[0].setValue(2.0);
        addition.getBlockFunctions()[1].setAddress(local);
        ladder.addLadderGrid(addition);
        ladder.setOutGrids(grids(addition));
        ladder.run(map, CYCLE_TIME);

        assertEquals(5.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("SUM").getValue());
    }

    /**
     * a bit block cuts the values of its two functions to their integer part and combines
     * them, whatever the notation of the functions
     */
    @Test
    void bitBlockCombinesItsFunctions() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid and = outGrid(1, 1, LadderBlock.AND_BITS, "AND");
        LadderGrid xor = outGrid(2, 1, LadderBlock.XOR_BITS, "XOR");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("AND", "XOR", "MASK");

        map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("MASK").setValue(3.0);
        and.getBlockFunctions()[0].setNumber(true);
        and.getBlockFunctions()[0].setValue(15.0);
        and.getBlockFunctions()[1].setAddress("MASK");
        xor.getBlockFunctions()[0].setNumber(true);
        xor.getBlockFunctions()[0].setValue(15.0);
        xor.getBlockFunctions()[1].setNumber(true);
        xor.getBlockFunctions()[1].setValue(3.0);
        ladder.addLadderGrid(and);
        ladder.addLadderGrid(xor);
        ladder.setOutGrids(grids(and, xor));
        ladder.run(map, CYCLE_TIME);

        assertEquals(3.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("AND").getValue());
        assertEquals(12.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("XOR").getValue());
    }

    /**
     * the shift blocks shift the integer value of their first function by the second one,
     * and the not block inverts the integer value of its function
     */
    @Test
    void shiftAndNotBlocksCalculateTheirIo() {
        Ladder ladder = new Ladder(1, "ladder", 4, 3);
        LadderGrid shift = outGrid(1, 1, LadderBlock.SHIFT_LEFT_BITS, "SFL");
        LadderGrid not = outGrid(2, 1, LadderBlock.NOT_BITS, "NOT");
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> map = ioMap("SFL", "NOT");

        shift.getBlockFunctions()[0].setNumber(true);
        shift.getBlockFunctions()[0].setValue(1.0);
        shift.getBlockFunctions()[1].setNumber(true);
        shift.getBlockFunctions()[1].setValue(4.0);
        not.getBlockFunctions()[0].setNumber(true);
        not.getBlockFunctions()[0].setValue(0.0);
        ladder.addLadderGrid(shift);
        ladder.addLadderGrid(not);
        ladder.setOutGrids(grids(shift, not));
        ladder.run(map, CYCLE_TIME);

        assertEquals(16.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("SFL").getValue());
        assertEquals(-1.0, map.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).get("NOT").getValue());
    }
}
