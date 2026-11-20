package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderTableIo}.
 */
class LadderTableIoTest {

    @Test
    void setPositionParsesTrimmedValuesAndIgnoresMalformedInput() {
        LadderTableIo item = new LadderTableIo("Ladders", "X0", "LOAD", 1, 2);

        assertEquals("1 : 2", item.getPosition());

        item.setPosition(" 3 : 4 ");
        assertEquals(3, item.getColumn());
        assertEquals(4, item.getRow());
        assertEquals("3 : 4", item.getPosition());

        item.setPosition("5:6");
        assertEquals(5, item.getColumn());
        assertEquals(6, item.getRow());

        assertDoesNotThrow(() -> {
            item.setPosition(null);
            item.setPosition("invalid");
            item.setPosition("10:");
            item.setPosition("a:b");
        });
        assertEquals(5, item.getColumn());
        assertEquals(6, item.getRow());
        assertEquals("5 : 6", item.getPosition());
    }
}
