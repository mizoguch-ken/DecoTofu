package ken.mizoguch.ladders;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class LadderCyclePacerTest {

    @Test
    void subMillisecondIdealCycleTimeSpinsWithoutOsSleep() {
        AtomicLong clock = new AtomicLong(1_000_000L);
        AtomicInteger sleepCalls = new AtomicInteger();
        AtomicInteger spinCalls = new AtomicInteger();

        LadderCyclePacer pacer = new LadderCyclePacer(
                clock::get,
                nanos -> sleepCalls.incrementAndGet(),
                () -> {
                    spinCalls.incrementAndGet();
                    clock.addAndGet(250L);
                });

        boolean completed = pacer.waitUntil(1_000_000L, () -> 1_000L, () -> true);

        assertTrue(completed);
        assertEquals(0, sleepCalls.get());
        assertEquals(4, spinCalls.get());
        assertEquals(1_001_000L, clock.get());
    }

    @Test
    void zeroIdealCycleTimeReturnsImmediately() {
        AtomicLong clock = new AtomicLong(500L);
        AtomicInteger sleepCalls = new AtomicInteger();
        AtomicInteger spinCalls = new AtomicInteger();

        LadderCyclePacer pacer = new LadderCyclePacer(
                clock::get,
                nanos -> sleepCalls.incrementAndGet(),
                spinCalls::incrementAndGet);

        assertTrue(pacer.waitUntil(500L, () -> 0L, () -> true));
        assertEquals(0, sleepCalls.get());
        assertEquals(0, spinCalls.get());
    }

    @Test
    void multiMillisecondWaitUsesCoarseSleepThenSpinsRemainingMargin() {
        AtomicLong clock = new AtomicLong(0L);
        AtomicLong sleptRequested = new AtomicLong();
        AtomicInteger sleepCalls = new AtomicInteger();
        AtomicInteger spinCalls = new AtomicInteger();

        LadderCyclePacer pacer = new LadderCyclePacer(
                clock::get,
                nanos -> {
                    sleepCalls.incrementAndGet();
                    sleptRequested.addAndGet(nanos);
                    clock.addAndGet(nanos);
                },
                () -> {
                    spinCalls.incrementAndGet();
                    clock.addAndGet(500_000L);
                });

        long idealNanos = TimeUnit.MILLISECONDS.toNanos(10);
        assertTrue(pacer.waitUntil(0L, () -> idealNanos, () -> true));

        assertEquals(1, sleepCalls.get());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(8), sleptRequested.get());
        assertEquals(4, spinCalls.get());
        assertEquals(idealNanos, clock.get());
    }

    @Test
    void adaptiveMarginGrowsAfterOvershootAndDecaysWhenAccurate() {
        AtomicLong clock = new AtomicLong(0L);
        AtomicInteger sleepCalls = new AtomicInteger();
        AtomicLong extraOvershootNanos = new AtomicLong(TimeUnit.MILLISECONDS.toNanos(12));

        LadderCyclePacer pacer = new LadderCyclePacer(
                clock::get,
                nanos -> {
                    sleepCalls.incrementAndGet();
                    clock.addAndGet(nanos + extraOvershootNanos.get());
                },
                () -> clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1)));

        long start0 = clock.get();
        assertTrue(pacer.waitUntil(start0, () -> TimeUnit.MILLISECONDS.toNanos(10), () -> true));
        long grownMargin = pacer.sleepMarginNanos();
        assertEquals(TimeUnit.MILLISECONDS.toNanos(13), grownMargin);

        // Next 5 ms wait is below grownMargin (13 ms), so it must not enter OS sleep
        int sleepsBeforeShortWait = sleepCalls.get();
        long start1 = clock.get();
        assertTrue(pacer.waitUntil(start1, () -> TimeUnit.MILLISECONDS.toNanos(5), () -> true));
        assertEquals(sleepsBeforeShortWait, sleepCalls.get());

        // Accurate 25 ms sleep with 0 overshoot decays margin down by one decay step
        extraOvershootNanos.set(0L);
        long start2 = clock.get();
        assertTrue(pacer.waitUntil(start2, () -> TimeUnit.MILLISECONDS.toNanos(25), () -> true));
        assertEquals(grownMargin - LadderCyclePacer.MARGIN_DECAY_STEP_NANOS, pacer.sleepMarginNanos());
    }

    @Test
    void longWaitSlicesSleepsAndRespectsKeepWaitingOrLiveIdealChange() {
        AtomicLong clock = new AtomicLong(0L);
        AtomicInteger sleepCalls = new AtomicInteger();
        AtomicBoolean keepWaiting = new AtomicBoolean(true);
        AtomicLong liveIdeal = new AtomicLong(TimeUnit.MILLISECONDS.toNanos(120));

        LadderCyclePacer pacer = new LadderCyclePacer(
                clock::get,
                nanos -> {
                    assertTrue(nanos <= LadderCyclePacer.MAX_SLEEP_SLICE_NANOS,
                            "Sleep slice exceeded max slice: " + nanos);
                    sleepCalls.incrementAndGet();
                    clock.addAndGet(nanos);
                    if (sleepCalls.get() == 2) {
                        liveIdeal.set(0L);
                    }
                },
                () -> clock.addAndGet(100_000L));

        assertTrue(pacer.waitUntil(0L, liveIdeal::get, keepWaiting::get));
        assertEquals(2, sleepCalls.get());

        keepWaiting.set(false);
        assertTrue(pacer.waitUntil(clock.get(), () -> TimeUnit.MILLISECONDS.toNanos(30), keepWaiting::get));
        assertEquals(2, sleepCalls.get());
    }

    @Test
    void interruptedSleepPreservesInterruptFlagAndReturnsFalse() {
        AtomicLong clock = new AtomicLong(0L);
        LadderCyclePacer pacer = new LadderCyclePacer(
                clock::get,
                nanos -> {
                    throw new InterruptedException("stop");
                },
                () -> {
                });

        try {
            assertFalse(pacer.waitUntil(0L, () -> TimeUnit.MILLISECONDS.toNanos(10), () -> true));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void realClockOneMicrosecondSmokeTestAvoidsMillisecondSleepStall() {
        LadderCyclePacer pacer = new LadderCyclePacer();
        // Warm up JIT/nanoTime once
        pacer.waitUntil(System.nanoTime(), () -> 1_000L, () -> true);

        int iterations = 20;
        long totalNanos = 0L;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            assertTrue(pacer.waitUntil(start, () -> 1_000L, () -> true));
            long elapsed = System.nanoTime() - start;
            assertTrue(elapsed >= 1_000L, "Elapsed cycle time must be >= target: " + elapsed);
            totalNanos += elapsed;
        }
        long averageNanos = totalNanos / iterations;
        assertTrue(averageNanos < 50_000L,
                "Expected spin wait average well below millisecond OS sleep stalls, got " + averageNanos + " ns");
    }
}
