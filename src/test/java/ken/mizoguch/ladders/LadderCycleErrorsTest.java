package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderCycleErrors}. An error of a cycle of the ladders does not stop the
 * cycle, but an error which is repeated at every cycle stops it : the cycle would run the ladder in
 * an unknown state.
 */
class LadderCycleErrorsTest {

    /** number of the errors of the cycles which are accepted by the tests */
    private static final int LIMIT = 3;

    @Test
    void noErrorIsMadeByTheCyclesWhichRun() {
        LadderCycleErrors errors = new LadderCycleErrors(LIMIT);

        assertEquals(0, errors.count());
        assertTrue(errors.isFirst());
        assertFalse(errors.isOverLimit());
    }

    @Test
    void increaseAnswersTheNumberOfTheErrorsOfTheSeries() {
        LadderCycleErrors errors = new LadderCycleErrors(LIMIT);

        assertEquals(1, errors.increase());
        assertEquals(2, errors.increase());
        assertEquals(3, errors.increase());
        assertEquals(3, errors.count());
    }

    @Test
    void onlyTheFirstErrorOfASeriesAnswersItsTrace() {
        LadderCycleErrors errors = new LadderCycleErrors(LIMIT);

        assertTrue(errors.isFirst());
        errors.increase();
        assertFalse(errors.isFirst());
        errors.increase();
        assertFalse(errors.isFirst());
    }

    @Test
    void theCycleIsStoppedWhenTheErrorsAreMoreThanTheLimit() {
        LadderCycleErrors errors = new LadderCycleErrors(LIMIT);

        for (int count = 0; count < LIMIT; count++) {
            errors.increase();
            // the limit of the errors is not reached by the cycles of the limit
            assertFalse(errors.isOverLimit());
        }

        errors.increase();
        assertTrue(errors.isOverLimit());
        assertEquals(LIMIT + 1, errors.count());
    }

    @Test
    void aCycleWhichRunsClearsTheErrorsBeforeIt() {
        LadderCycleErrors errors = new LadderCycleErrors(LIMIT);

        errors.increase();
        errors.increase();
        assertEquals(2, errors.count());

        errors.clear();

        assertEquals(0, errors.count());
        assertTrue(errors.isFirst());
        assertFalse(errors.isOverLimit());
    }

    @Test
    void noErrorIsAcceptedWhenTheLimitIsZero() {
        LadderCycleErrors errors = new LadderCycleErrors(0);

        assertFalse(errors.isOverLimit());

        errors.increase();
        assertTrue(errors.isOverLimit());
    }
}
