package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderNumber}.
 */
class LadderNumberTest {

    @Test
    void decimalNumbersAreNumbers() {
        assertTrue(LadderNumber.isNumber("0"));
        assertTrue(LadderNumber.isNumber("12"));
        assertTrue(LadderNumber.isNumber("12.5"));
        assertTrue(LadderNumber.isNumber(".5"));
        assertTrue(LadderNumber.isNumber("12."));
        assertTrue(LadderNumber.isNumber("+3"));
        assertTrue(LadderNumber.isNumber("-3"));
        assertTrue(LadderNumber.isNumber("-1e3"));
        assertTrue(LadderNumber.isNumber("2.5E+2"));
    }

    @Test
    void prefixedNumbersAreNumbers() {
        assertTrue(LadderNumber.isNumber("0x10"));
        assertTrue(LadderNumber.isNumber("0X1f"));
        assertTrue(LadderNumber.isNumber("0b101"));
        assertTrue(LadderNumber.isNumber("-0x10"));
    }

    @Test
    void addressesAreNotNumbers() {
        assertFalse(LadderNumber.isNumber("OUT"));
        assertFalse(LadderNumber.isNumber(".local"));
        assertFalse(LadderNumber.isNumber("1O"));
        assertFalse(LadderNumber.isNumber("12.3.4"));
        assertFalse(LadderNumber.isNumber("0x"));
        assertFalse(LadderNumber.isNumber("0b2"));
        assertFalse(LadderNumber.isNumber(" 1"));
        assertFalse(LadderNumber.isNumber("1 "));
    }

    /**
     * an empty address is answered by false without an exception : the blocks of a ladder
     * hold an empty address when they are created
     */
    @Test
    void nullAndEmptyStringsAreNotNumbers() {
        assertFalse(LadderNumber.isNumber(null));
        assertFalse(LadderNumber.isNumber(""));
    }

    @Test
    void parseAnswersTheValueAndTheRadix() {
        LadderNumber.Value decimal = LadderNumber.parse("12.5");

        assertNotNull(decimal);
        assertEquals(12.5, decimal.getValue());
        assertEquals(LadderNumber.RADIX_DECIMAL, decimal.getRadix());

        LadderNumber.Value hexadecimal = LadderNumber.parse("0x1F");

        assertNotNull(hexadecimal);
        assertEquals(31.0, hexadecimal.getValue());
        assertEquals(LadderNumber.RADIX_HEXADECIMAL, hexadecimal.getRadix());

        LadderNumber.Value binary = LadderNumber.parse("0b101");

        assertNotNull(binary);
        assertEquals(5.0, binary.getValue());
        assertEquals(LadderNumber.RADIX_BINARY, binary.getRadix());
    }

    @Test
    void parseAnswersNullWhenTheStringIsNotANumber() {
        assertNull(LadderNumber.parse(null));
        assertNull(LadderNumber.parse(""));
        assertNull(LadderNumber.parse("OUT"));
        assertNull(LadderNumber.parse("12.3.4"));
    }

    @Test
    void formatWritesThePrefixOfTheRadix() {
        assertEquals("31.0", LadderNumber.format(31.0, LadderNumber.RADIX_DECIMAL));
        assertEquals("0x1f", LadderNumber.format(31.0, LadderNumber.RADIX_HEXADECIMAL));
        assertEquals("0b11111", LadderNumber.format(31.0, LadderNumber.RADIX_BINARY));
        // an unknown radix is shown as a decimal value, as the converter of a value does
        assertEquals("31.0", LadderNumber.format(31.0, LadderGrid.LADDER_GRID_INITIAL_BLOCK_FUNCTION_RADIX));
    }

    /**
     * a fractional value is cut to its integer part by the hexadecimal and the binary
     * notation, as the editor of the ladders shows it
     */
    @Test
    void formatCutsAFractionalValueForThePrefixedRadixes() {
        assertEquals("0x1", LadderNumber.format(1.75, LadderNumber.RADIX_HEXADECIMAL));
        assertEquals("0b1", LadderNumber.format(1.75, LadderNumber.RADIX_BINARY));
        assertEquals("-0x1", LadderNumber.format(-1.75, LadderNumber.RADIX_HEXADECIMAL));
        assertEquals("1.75", LadderNumber.format(1.75, LadderNumber.RADIX_DECIMAL));
    }

    /**
     * the sign of a number is out of its notation : it is kept by the hexadecimal and the
     * binary notation too, as the decimal notation already kept it
     */
    @Test
    void parseKeepsTheSignOfTheNumber() {
        assertEquals(-1000.0, LadderNumber.parse("-1e3").getValue());
        assertEquals(-16.0, LadderNumber.parse("-0x10").getValue());
        assertEquals(LadderNumber.RADIX_HEXADECIMAL, LadderNumber.parse("-0x10").getRadix());
        assertEquals(-5.0, LadderNumber.parse("-0b101").getValue());
        assertEquals(LadderNumber.RADIX_BINARY, LadderNumber.parse("-0b101").getRadix());
        assertEquals(16.0, LadderNumber.parse("+0x10").getValue());
    }

    /**
     * the text written by format() is read again by parse() : the editor of the ladders
     * writes the value of a block and reads it back when the ladder is loaded
     */
    @Test
    void formatAndParseAnswerEachOther() {
        int[] radixes = {LadderNumber.RADIX_DECIMAL, LadderNumber.RADIX_HEXADECIMAL, LadderNumber.RADIX_BINARY};

        for (int radix : radixes) {
            double[] values = {0.0, 1.0, 31.0, -31.0};

            for (double value : values) {
                String text = LadderNumber.format(value, radix);
                LadderNumber.Value parsed = LadderNumber.parse(text);

                assertNotNull(parsed, text);
                assertEquals(value, parsed.getValue());
            }
        }
    }
}
