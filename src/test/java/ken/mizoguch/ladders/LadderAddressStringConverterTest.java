package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderAddressStringConverter}.
 */
class LadderAddressStringConverterTest {

    @Test
    void constructorStartsEmpty() {
        assertEquals("", new LadderAddressStringConverter().toString(null));
    }

    @Test
    void toStringTrimsAndRejectsSpacesAndBlank() {
        LadderAddressStringConverter converter = new LadderAddressStringConverter();

        assertEquals("ABC", converter.toString(" ABC "));
        assertEquals("ABC", converter.toString(""));
        assertEquals("ABC", converter.toString(" "));
        assertEquals("ABC", converter.toString("A B C"));
        assertEquals("ABC", converter.toString(null));
    }

    @Test
    void fromStringTrimsAndRejectsSpacesAndBlank() {
        LadderAddressStringConverter converter = new LadderAddressStringConverter();

        assertEquals("123", converter.fromString(" 123 "));
        assertEquals("123", converter.fromString(null));
        assertEquals("123", converter.fromString(""));
        assertEquals("123", converter.fromString("1 2 3"));
    }

    @Test
    void localAddressPrefixMustMatchCurrentState() {
        LadderAddressStringConverter converter = new LadderAddressStringConverter();

        assertEquals("ABC", converter.fromString("ABC"));
        assertEquals("ABC", converter.fromString(".ABC"));

        assertEquals(".ABC", converter.toString(".ABC"));
        assertEquals(".DEF", converter.fromString(".DEF"));
        assertEquals(".DEF", converter.fromString("GHI"));
    }
}
