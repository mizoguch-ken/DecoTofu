package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderValueStringConverter}.
 */
class LadderValueStringConverterTest {

    @Test
    void constructorStartsZero() {
        assertEquals("0.0", new LadderValueStringConverter().toString(null));
    }

    @Test
    void decimalStringsAreParsedAndFormatted() {
        LadderValueStringConverter converter = new LadderValueStringConverter();

        assertEquals(Double.valueOf(12.5), converter.fromString("12.5"));
        assertEquals(Double.valueOf(-1000.0), converter.fromString("-1e3"));
        assertEquals(Double.valueOf(250.0), converter.fromString("2.5E+2"));
        assertEquals("250.0", converter.toString(null));
        assertEquals("4.75", converter.toString(Double.valueOf(4.75)));
    }

    @Test
    void nullOrInvalidStringsKeepCurrentValue() {
        LadderValueStringConverter converter = new LadderValueStringConverter();

        converter.fromString("7.0");
        assertEquals(Double.valueOf(7.0), converter.fromString(null));
        assertEquals(Double.valueOf(7.0), converter.fromString("abc"));
        assertEquals(Double.valueOf(7.0), converter.fromString("0x"));
        assertEquals(Double.valueOf(7.0), converter.fromString("12.3.4"));
    }

    @Test
    void nonFiniteToStringArgumentsKeepCurrentValue() {
        LadderValueStringConverter converter = new LadderValueStringConverter();

        assertEquals("5.0", converter.toString(Double.valueOf(5.0)));
        assertEquals("5.0", converter.toString(Double.NaN));
        assertEquals("5.0", converter.toString(Double.POSITIVE_INFINITY));
        assertEquals("5.0", converter.toString(Double.NEGATIVE_INFINITY));
    }

    /**
     * Current implementation passes prefixed literals such as "0x10" to
     * Long.parseLong with an explicit radix, which rejects the prefix.
     * This test documents that the literal forms throw today (suspected bug).
     */
    @Test
    void hexAndBinaryLiteralsCurrentlyThrowNumberFormatException() {
        LadderValueStringConverter converter = new LadderValueStringConverter();

        assertEquals(Double.valueOf(3.0), converter.fromString("3.0"));
        assertThrows(NumberFormatException.class, () -> converter.fromString("0x10"));
        assertThrows(NumberFormatException.class, () -> converter.fromString("0X1F"));
        assertThrows(NumberFormatException.class, () -> converter.fromString("0b101"));
        // value stays unchanged because parsing fails before assignment
        assertEquals("3.0", converter.toString(null));
    }
}
