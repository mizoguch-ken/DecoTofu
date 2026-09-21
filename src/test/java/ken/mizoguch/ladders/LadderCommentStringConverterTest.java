package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderCommentStringConverter}.
 */
class LadderCommentStringConverterTest {

    @Test
    void constructorStartsEmpty() {
        assertEquals("", new LadderCommentStringConverter().toString(null));
    }

    @Test
    void toStringTrimsAndKeepsValueOnSemicolon() {
        LadderCommentStringConverter converter = new LadderCommentStringConverter();

        assertEquals("abc", converter.toString("abc"));
        assertEquals("x", converter.toString(" x "));
        assertEquals("x", converter.toString("a;b"));
        assertEquals("x", converter.toString(";comment"));
        assertEquals("x", converter.toString(null));
    }

    @Test
    void fromStringTrimsAndKeepsValueOnSemicolon() {
        LadderCommentStringConverter converter = new LadderCommentStringConverter();

        assertEquals("comment text", converter.fromString("comment text"));
        assertEquals("trimmed", converter.fromString(" trimmed "));
        assertEquals("trimmed", converter.fromString("a ; b"));
        assertEquals("trimmed", converter.fromString(null));
        // current behavior: empty string holds no semicolon, so it is accepted and resets the value
        assertEquals("", converter.fromString(""));
    }
}
