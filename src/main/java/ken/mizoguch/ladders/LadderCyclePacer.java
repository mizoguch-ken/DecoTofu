package ken.mizoguch.ladders;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

/**
 * Precision hybrid pacer for the ladder scan loop.
 *
 * <p>Short waits (smaller than the adaptive OS sleep overshoot margin) spin with
 * {@link Thread#onSpinWait()} instead of entering an OS sleep syscall whose
 * wake-up granularity on desktop OSes can overshoot sub-millisecond targets by
 * tens of microseconds to milliseconds. Longer waits sleep for a bounded coarse
 * slice minus the adaptive margin and spin-wait the final tail to the exact
 * deadline.</p>
 */
final class LadderCyclePacer {

    static final long MIN_SLEEP_MARGIN_NANOS = TimeUnit.MILLISECONDS.toNanos(2);
    static final long MAX_SLEEP_MARGIN_NANOS = TimeUnit.MILLISECONDS.toNanos(20);
    static final long MARGIN_HEADROOM_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    static final long MARGIN_DECAY_STEP_NANOS = TimeUnit.MICROSECONDS.toNanos(250);
    static final long MAX_SLEEP_SLICE_NANOS = TimeUnit.MILLISECONDS.toNanos(50);

    @FunctionalInterface
    interface SleepAction {
        void sleep(long nanos) throws InterruptedException;
    }

    private final LongSupplier nanoClock_;
    private final SleepAction sleeper_;
    private final Runnable spinAction_;
    private long sleepMarginNanos_;

    LadderCyclePacer() {
        this(System::nanoTime, TimeUnit.NANOSECONDS::sleep, Thread::onSpinWait);
    }

    LadderCyclePacer(LongSupplier nanoClock, SleepAction sleeper, Runnable spinAction) {
        nanoClock_ = Objects.requireNonNull(nanoClock, "nanoClock");
        sleeper_ = Objects.requireNonNull(sleeper, "sleeper");
        spinAction_ = Objects.requireNonNull(spinAction, "spinAction");
        sleepMarginNanos_ = MIN_SLEEP_MARGIN_NANOS;
    }

    long sleepMarginNanos() {
        return sleepMarginNanos_;
    }

    boolean waitUntil(long cycleStartTime, LongSupplier idealCycleTimeSupplier, BooleanSupplier keepWaiting) {
        boolean didSleep = false;
        long maxNeededMarginNanos = 0L;

        while (keepWaiting.getAsBoolean()) {
            long idealCycleTime = idealCycleTimeSupplier.getAsLong();
            if (idealCycleTime <= 0L) {
                maybeDecaySleepMargin(didSleep, maxNeededMarginNanos);
                return true;
            }
            long deadline = cycleStartTime + idealCycleTime;
            long now = nanoClock_.getAsLong();
            long remaining = deadline - now;
            if (remaining <= 0L) {
                maybeDecaySleepMargin(didSleep, maxNeededMarginNanos);
                return true;
            }
            if (remaining > sleepMarginNanos_) {
                long sleepNanos = Math.min(remaining - sleepMarginNanos_, MAX_SLEEP_SLICE_NANOS);
                if (sleepNanos > 0L) {
                    long beforeSleep = now;
                    try {
                        sleeper_.sleep(sleepNanos);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                    long afterSleep = nanoClock_.getAsLong();
                    long needed = computeNeededMargin(sleepNanos, afterSleep - beforeSleep);
                    didSleep = true;
                    if (needed > maxNeededMarginNanos) {
                        maxNeededMarginNanos = needed;
                    }
                    if (needed > sleepMarginNanos_) {
                        sleepMarginNanos_ = needed;
                    }
                    continue;
                }
            }
            boolean completed = spinUntilDeadline(cycleStartTime, idealCycleTimeSupplier, keepWaiting);
            if (completed) {
                maybeDecaySleepMargin(didSleep, maxNeededMarginNanos);
            }
            return completed;
        }
        return true;
    }

    private static long computeNeededMargin(long requestedSleepNanos, long actualSleepNanos) {
        long overshoot = Math.max(0L, actualSleepNanos - requestedSleepNanos);
        return Math.min(MAX_SLEEP_MARGIN_NANOS,
                Math.max(MIN_SLEEP_MARGIN_NANOS, overshoot + MARGIN_HEADROOM_NANOS));
    }

    private void maybeDecaySleepMargin(boolean didSleep, long maxNeededMarginNanos) {
        if (didSleep && (sleepMarginNanos_ > MIN_SLEEP_MARGIN_NANOS)
                && (maxNeededMarginNanos <= (sleepMarginNanos_ - MARGIN_DECAY_STEP_NANOS))) {
            sleepMarginNanos_ = Math.max(MIN_SLEEP_MARGIN_NANOS, sleepMarginNanos_ - MARGIN_DECAY_STEP_NANOS);
        }
    }

    private boolean spinUntilDeadline(long cycleStartTime, LongSupplier idealCycleTimeSupplier,
            BooleanSupplier keepWaiting) {
        while (keepWaiting.getAsBoolean()) {
            if (Thread.currentThread().isInterrupted()) {
                return false;
            }
            long currentIdeal = idealCycleTimeSupplier.getAsLong();
            if (currentIdeal <= 0L) {
                return true;
            }
            if (((cycleStartTime + currentIdeal) - nanoClock_.getAsLong()) <= 0L) {
                return true;
            }
            spinAction_.run();
        }
        return true;
    }
}
