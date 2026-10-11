package ken.mizoguch.ladders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderCycleStatistics}. The times of the cycles of the ladders are read with
 * {@link System#nanoTime()} : the tests give the times of the end of the cycles so the shortest and
 * the longest times of a cycle and the cumulated times of the cycles answer without waiting a cycle.
 */
class LadderCycleStatisticsTest {

    /** time of the last cycle of a cycle which is started */
    private static final long BEGIN = 1_000L;

    @Test
    void aCycleWhichStartsAnswersNoTime() {
        LadderCycleStatistics statistics = new LadderCycleStatistics(BEGIN);

        assertEquals(0L, statistics.cycleTime());
        assertEquals(Long.MAX_VALUE, statistics.minCycleTime());
        assertEquals(0L, statistics.maxCycleTime());
        assertEquals(0L, statistics.cumulativeCycleTime());
        assertEquals(0L, statistics.cumulativeCycleTimeCount());
    }

    @Test
    void addKeepsTheTimeOfEveryCycle() {
        LadderCycleStatistics statistics = new LadderCycleStatistics(BEGIN);

        assertEquals(1000L, statistics.add(BEGIN + 1000L));
        assertEquals(1000L, statistics.cycleTime());

        assertEquals(500L, statistics.add(BEGIN + 1500L));
        assertEquals(500L, statistics.cycleTime());
        assertEquals(1500L, statistics.cumulativeCycleTime());
        assertEquals(2L, statistics.cumulativeCycleTimeCount());
    }

    @Test
    void addAnswersTheShortestAndTheLongestTimeOfTheCycles() {
        LadderCycleStatistics statistics = new LadderCycleStatistics(BEGIN);

        statistics.add(BEGIN + 1000L);
        assertTrue(statistics.isMinCycleTimeChanged());
        assertFalse(statistics.isMaxCycleTimeChanged());
        assertEquals(1000L, statistics.minCycleTime());
        // the longest time is not answered by the cycle which answers the shortest one
        assertEquals(0L, statistics.maxCycleTime());

        // the time of a cycle is measured from the end of the last cycle which has been kept
        statistics.add(BEGIN + 5000L);
        assertFalse(statistics.isMinCycleTimeChanged());
        assertTrue(statistics.isMaxCycleTimeChanged());
        assertEquals(4000L, statistics.maxCycleTime());

        statistics.add(BEGIN + 6500L);
        assertFalse(statistics.isMinCycleTimeChanged());
        assertFalse(statistics.isMaxCycleTimeChanged());
        assertEquals(1000L, statistics.minCycleTime());
        assertEquals(4000L, statistics.maxCycleTime());
    }

    @Test
    void addIgnoresACycleWithoutTime() {
        LadderCycleStatistics statistics = new LadderCycleStatistics(BEGIN);

        // a cycle which is interrupted answers no time : the time is answered to the cycle
        // of the ladder but it is not kept for the shortest and the longest times
        assertEquals(1000L, statistics.add(BEGIN + 1000L));
        assertEquals(0L, statistics.add(BEGIN + 1000L));
        assertEquals(0L, statistics.cycleTime());
        assertEquals(1000L, statistics.cumulativeCycleTime());
        assertEquals(1L, statistics.cumulativeCycleTimeCount());

        // a time which goes back is not kept either, and the next cycle is measured from
        // the last cycle which has been kept
        assertEquals(-500L, statistics.add(BEGIN + 500L));
        assertEquals(1000L, statistics.minCycleTime());
        assertEquals(400L, statistics.add(BEGIN + 1400L));
        assertEquals(1400L, statistics.cumulativeCycleTime());
        assertEquals(2L, statistics.cumulativeCycleTimeCount());
    }

    @Test
    void takeCumulativeCountsTheCyclesOfTheNextRefreshOfTheView() {
        LadderCycleStatistics statistics = new LadderCycleStatistics(BEGIN);

        statistics.add(BEGIN + 1000L);
        statistics.add(BEGIN + 2000L);
        assertEquals(2000L, statistics.cumulativeCycleTime());
        assertEquals(2L, statistics.cumulativeCycleTimeCount());

        statistics.takeCumulative();

        assertEquals(0L, statistics.cumulativeCycleTime());
        assertEquals(0L, statistics.cumulativeCycleTimeCount());
        // the shortest and the longest times of the cycles are kept over a refresh of the view
        assertEquals(1000L, statistics.minCycleTime());
        assertEquals(1000L, statistics.maxCycleTime());
    }

    @Test
    void viewRefreshIsDueWhenTheCyclesCumulateTheTimeOfARefresh() {
        LadderCycleStatistics statistics = new LadderCycleStatistics(BEGIN);

        assertFalse(statistics.isViewRefreshDue(1000L));

        statistics.add(BEGIN + 400L);
        assertFalse(statistics.isViewRefreshDue(1000L));

        statistics.add(BEGIN + 1100L);
        assertTrue(statistics.isViewRefreshDue(1000L));

        statistics.takeCumulative();
        assertFalse(statistics.isViewRefreshDue(1000L));

        // the view is drawn again even when the cycles run faster than the time of a refresh
        for (long nanoTime = BEGIN + 1200L; nanoTime <= (BEGIN + 2200L); nanoTime += 100L) {
            statistics.add(nanoTime);
        }
        assertTrue(statistics.isViewRefreshDue(1000L));
    }
}
