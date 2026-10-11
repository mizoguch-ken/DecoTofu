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
     * a hexadecimal or a binary literal is read without its prefix : the prefixes are kept
     * only to show the value, so that "0x10" answers 16 instead of failing on the prefix
     */
    @Test
    void hexAndBinaryLiteralsAreParsedWithTheirRadix() {
        LadderValueStringConverter converter = new LadderValueStringConverter();

        assertEquals(Double.valueOf(16.0), converter.fromString("0x10"));
        assertEquals("0x10", converter.toString(null));
        assertEquals(Double.valueOf(31.0), converter.fromString("0X1F"));
        assertEquals("0x1f", converter.toString(null));
        assertEquals(Double.valueOf(5.0), converter.fromString("0b101"));
        assertEquals("0b101", converter.toString(null));
        // the decimal notation is back when a decimal value is given
        assertEquals(Double.valueOf(3.5), converter.fromString("3.5"));
        assertEquals("3.5", converter.toString(null));
    }
}
