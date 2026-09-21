package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderGrid}.
 */
class LadderGridTest {

    @Test
    void constructorInitializesDefaults() {
        LadderGrid grid = new LadderGrid(1, 2);

        assertEquals(1, grid.getColumnIndex());
        assertEquals(2, grid.getRowIndex());
        assertEquals(1, grid.getColSpan());
        assertEquals(1, grid.getRowSpan());
        assertEquals(Ladders.LADDER_BLOCK.EMPTY, grid.getBlock());
        assertEquals(0.0, grid.getBlockValue());
        assertNull(grid.getBlockScript());
        assertFalse(grid.isBlockLd());
        assertFalse(grid.isVertical());
        assertFalse(grid.isVerticalOr());
        assertEquals("", grid.getAddress());
        assertEquals("", grid.getComment());
        assertEquals(0.0, grid.getCumulativeValue());
        assertEquals(0, grid.getInConnectNumber());
        assertEquals(0, grid.getOutConnectNumber());
        assertFalse(grid.isSelect());
        assertFalse(grid.isSelectRange());
        assertNull(grid.getLeftLadderGrid());
        assertNull(grid.getUpLadderGrid());
        assertNull(grid.getRightLadderGrid());
        assertNull(grid.getDownLadderGrid());

        assertEquals(LadderGrid.LADDER_BLOCK_FUNCTIONS, grid.getBlockFunctions().length);
        for (LadderGrid.BlockFunction function : grid.getBlockFunctions()) {
            assertNull(function.getAddress());
            assertEquals(0.0, function.getValue());
            assertEquals(-1, function.getRadix());
            assertFalse(function.isNumber());
        }
    }

    @Test
    void setBlockResetsScriptOnlyWhenChanged() {
        LadderGrid grid = new LadderGrid(0, 0);

        grid.setBlockScript("script");
        assertTrue(grid.setBlock(Ladders.LADDER_BLOCK.LOAD));
        assertNull(grid.getBlockScript());

        assertFalse(grid.setBlock(Ladders.LADDER_BLOCK.LOAD));
        grid.setBlockScript("keep");
        assertFalse(grid.setBlock(Ladders.LADDER_BLOCK.LOAD));
        assertEquals("keep", grid.getBlockScript());
    }

    @Test
    void statefulSettersReturnChangedFlag() {
        LadderGrid grid = new LadderGrid(0, 0);

        assertTrue(grid.setVertical(true));
        assertFalse(grid.setVertical(true));
        assertTrue(grid.setVerticalOr(true));
        assertFalse(grid.setVerticalOr(true));
        assertTrue(grid.setSelect(true));
        assertFalse(grid.setSelect(true));
        assertTrue(grid.setSelectRange(true));
        assertFalse(grid.setSelectRange(true));
    }

    @Test
    void clearResetsMutableState() {
        LadderGrid grid = new LadderGrid(3, 4, 5, 6);

        grid.setBlock(Ladders.LADDER_BLOCK.OUT);
        grid.setBlockValue(12.0);
        grid.getBlockFunctions()[0].setAddress("X1");
        grid.getBlockFunctions()[0].setValue(1.0);
        grid.getBlockFunctions()[0].setRadix(10);
        grid.getBlockFunctions()[0].setNumber(true);
        grid.setBlockScript("script");
        grid.setAddress("A1");
        grid.setComment("comment");
        grid.setCumulativeValue(2.0);
        grid.setInConnectNumber(3);
        grid.setOutConnectNumber(4);
        grid.setVertical(true);

        grid.clear();

        assertEquals(3, grid.getColumnIndex());
        assertEquals(6, grid.getRowSpan());
        assertEquals(LadderGrid.LADDER_GRID_INITIAL_BLOCK, grid.getBlock());
        assertEquals(0.0, grid.getBlockValue());
        assertNull(grid.getBlockScript());
        assertEquals("", grid.getAddress());
        assertEquals("", grid.getComment());
        assertEquals(0.0, grid.getCumulativeValue());
        assertEquals(0, grid.getInConnectNumber());
        assertEquals(0, grid.getOutConnectNumber());
        // current behavior: clear() does not reset the vertical flags
        assertTrue(grid.isVertical());
        for (LadderGrid.BlockFunction function : grid.getBlockFunctions()) {
            assertNull(function.getAddress());
            assertEquals(0.0, function.getValue());
            assertEquals(-1, function.getRadix());
            assertFalse(function.isNumber());
        }
    }

    @Test
    void copyCreatesDeepCopyOfBlockFunctions() {
        LadderGrid grid = new LadderGrid(1, 1);
        grid.setBlock(Ladders.LADDER_BLOCK.LOAD);
        grid.setBlockValue(7.0);
        grid.setAddress("X1");
        grid.setComment("comment");
        grid.setCumulativeValue(5.0);
        grid.setVertical(true);
        LadderGrid.BlockFunction source = grid.getBlockFunctions()[0];
        source.setAddress("F1");
        source.setValue(3.0);
        source.setRadix(16);
        source.setNumber(true);

        LadderGrid copy = grid.copy();

        assertEquals(grid.getColumnIndex(), copy.getColumnIndex());
        assertEquals(grid.getBlock(), copy.getBlock());
        assertEquals(grid.getBlockValue(), copy.getBlockValue());
        assertEquals(grid.getAddress(), copy.getAddress());
        assertEquals(grid.getComment(), copy.getComment());
        assertEquals(grid.getCumulativeValue(), copy.getCumulativeValue());
        assertTrue(copy.isVertical());
        assertNotSame(grid, copy);
        assertNotSame(grid.getBlockFunctions(), copy.getBlockFunctions());

        copy.getBlockFunctions()[0].setAddress("changed");
        assertEquals("F1", source.getAddress());
    }

    @Test
    void setBlockFunctionsCopiesValuesIntoExistingArray() {
        LadderGrid grid = new LadderGrid(0, 0);
        LadderGrid source = new LadderGrid(0, 0);

        source.getBlockFunctions()[0].setAddress("Z9");
        source.getBlockFunctions()[0].setValue(8.0);
        source.getBlockFunctions()[0].setRadix(2);
        source.getBlockFunctions()[0].setNumber(true);

        grid.setBlockFunctions(source.getBlockFunctions());

        assertEquals("Z9", grid.getBlockFunctions()[0].getAddress());
        assertEquals(8.0, grid.getBlockFunctions()[0].getValue());
        assertEquals(2, grid.getBlockFunctions()[0].getRadix());
        assertTrue(grid.getBlockFunctions()[0].isNumber());
        assertNotSame(grid.getBlockFunctions()[0], source.getBlockFunctions()[0]);

        grid.getBlockFunctions()[0].setAddress("own");
        assertEquals("Z9", source.getBlockFunctions()[0].getAddress());
    }
}
