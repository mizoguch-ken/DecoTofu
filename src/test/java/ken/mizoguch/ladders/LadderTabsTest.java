package ken.mizoguch.ladders;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderTabs}. The name of a ladder is the local address of its io, so a new
 * tab gets a name which no other ladder holds : the names are compared as the addresses of the io,
 * because a name is written with a space in the tab and with an underscore in the io.
 */
class LadderTabsTest {

    @Test
    void newNameAnswersLaddersWhenNoLadderHoldsTheName() {
        assertEquals("Ladders", LadderTabs.newName(Collections.emptyList()));
        assertEquals("Ladders", LadderTabs.newName(Arrays.asList("Ladders 2")));
    }

    @Test
    void newNameNumbersTheLaddersFromTwo() {
        assertEquals("Ladders 2", LadderTabs.newName(Arrays.asList("Ladders")));
        assertEquals("Ladders 3", LadderTabs.newName(Arrays.asList("Ladders", "Ladders 2")));
        assertEquals("Ladders 4", LadderTabs.newName(Arrays.asList("Ladders", "Ladders 2", "Ladders 3")));
    }

    @Test
    void newNameAnswersTheFirstFreeNumber() {
        assertEquals("Ladders 4", LadderTabs.newName(Arrays.asList("Ladders", "Ladders 2", "Ladders 3", "Ladders 5")));
        // a ladder of a file which the older versions of the editor wrote holds the number one
        assertEquals("Ladders 2", LadderTabs.newName(Arrays.asList("Ladders", "Ladders 1")));
    }

    @Test
    void newNameComparesTheNamesAsTheAddressesOfTheIo() {
        // the name "Ladders 2" and the address "Ladders_2" are the io of the same ladder
        assertEquals("Ladders 3", LadderTabs.newName(Arrays.asList("Ladders", "Ladders_2")));
        assertEquals("Ladders 3", LadderTabs.newName(Arrays.asList("Ladders", "Ladders_1", "Ladders 2")));
        // a name which holds an underscore is free when no ladder answers its address
        assertEquals("Ladders", LadderTabs.newName(Arrays.asList("Ladders_1")));
    }

    @Test
    void newNameDoesNotWaitTheNamesInOrder() {
        assertEquals("Ladders 3", LadderTabs.newName(Arrays.asList("Ladders 2", "Ladders")));
        assertEquals("Ladders 2", LadderTabs.newName(Arrays.asList("Ladders 3", "Ladders 4", "Ladders")));
    }
}
