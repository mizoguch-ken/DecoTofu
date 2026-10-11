package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderIo}.
 */
class LadderIoTest {

    @Test
    void addressAndCommentAccessors() {
        LadderIo io = new LadderIo("address");

        assertEquals("address", io.getAddress());
        assertEquals("", io.getComment());
        io.setComment("comment");
        assertEquals("comment", io.getComment());
        io.setAddress("other");
        assertEquals("other", io.getAddress());
        assertEquals(0.0, io.getValue());
    }

    @Test
    void setValueMakesARisingEdge() {
        LadderIo io = new LadderIo("rising");

        assertFalse(io.isLd());
        assertTrue(io.setValue(1.0));
        assertTrue(io.isLd());
        assertTrue(io.isLdRising());
        assertFalse(io.isLdFalling());
        // the edge is only answered by the cycle which makes it
        io.checkEdge();
        assertFalse(io.isLdRising());
        assertFalse(io.isLdFalling());
    }

    @Test
    void setValueMakesAFallingEdge() {
        LadderIo io = new LadderIo("falling");

        io.setValue(1.0);
        assertFalse(io.setValue(0.0));
        assertFalse(io.isLd());
        assertFalse(io.isLdRising());
        assertTrue(io.isLdFalling());
        io.checkEdge();
        assertFalse(io.isLdFalling());
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedCheckEdgeUsesTheSameEdge() {
        LadderIo io = new LadderIo("edge");

        io.setValue(1.0);
        assertTrue(io.isLdRising());
        io.chehckEdge();
        assertFalse(io.isLdRising());

        io.setValue(0.0);
        assertTrue(io.isLdFalling());
        io.checkEdge();
        assertFalse(io.isLdFalling());
    }

    @Test
    void setValueNotUsesTheDefaultValueOfLd() {
        LadderIo io = new LadderIo("not");

        io.setValueNot(1.0);
        assertTrue(io.isLd());
        io.resetValue();
        assertTrue(io.isLd());
        assertEquals(0.0, io.getValue());
        // a value made by setValue uses false as the default value of ld
        io.setValue(2.0);
        io.resetValue();
        assertFalse(io.isLd());
    }

    @Test
    void counterIsResetByAZeroValue() {
        LadderIo io = new LadderIo("counter");

        assertFalse(io.setValueCounter(1.0, 3.0));
        assertEquals(1.0, io.getCumulativeValue());
        assertFalse(io.setValueCounter(1.0, 3.0));
        assertTrue(io.setValueCounter(1.0, 3.0));
        assertEquals(3.0, io.getCumulativeValue());
        assertFalse(io.setValueCounter(0.0, 3.0));
        assertEquals(0.0, io.getCumulativeValue());
        // the count starts again
        assertFalse(io.setValueCounter(1.0, 3.0));
        assertEquals(1.0, io.getCumulativeValue());
    }

    @Test
    void timerIsResetByAZeroValue() {
        LadderIo io = new LadderIo("timer");

        assertFalse(io.setValueTimer(1.0, 10.0, 5000000L));
        assertTrue(io.setValueTimer(1.0, 10.0, 5000000L));
        assertFalse(io.setValueTimer(0.0, 10.0, 5000000L));
        assertEquals(0.0, io.getCumulativeValue());
    }

    @Test
    void setScriptWithoutWebEngineIsRefused() {
        LadderIo io = new LadderIo("script");

        assertFalse(io.setScript("value();"));
        assertFalse(io.setScript(null));
        io.setWebEngineState(null, null);
        assertFalse(io.setScript("value();"));
        assertEquals(0.0, io.getValue());
    }

    @Test
    void scriptLengthGuard() {
        assertTrue(LadderIo.isScriptLengthAcceptable(0, 0));
        assertTrue(LadderIo.isScriptLengthAcceptable(0, LadderIo.LADDER_IO_SCRIPT_MAX_LENGTH - 1));
        assertFalse(LadderIo.isScriptLengthAcceptable(0, LadderIo.LADDER_IO_SCRIPT_MAX_LENGTH));
        assertFalse(LadderIo.isScriptLengthAcceptable(LadderIo.LADDER_IO_SCRIPT_MAX_LENGTH, 1));
    }

    @Test
    void cycledFlag() {
        LadderIo io = new LadderIo("cycled");

        assertFalse(io.isCycled());
        io.setCycled(true);
        assertTrue(io.isCycled());
    }
}
