/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

/**
 * the times of the cycles of the ladders : the shortest and the longest time of the cycle are kept
 * while the cycle is running, and the times of the cycles since the last refresh of the view are
 * cumulated to answer the average time of a cycle to the view.
 *
 * a cycle which answers no time (negative or zero) is not kept for the times of the cycles : the
 * time of a cycle is read with {@link System#nanoTime()} which is not a wall clock, so a cycle which
 * is stopped and started again has to be counted from the time of the last cycle which has been kept
 *
 * @author mizoguch-ken
 */
final class LadderCycleStatistics {

    private long cycleTime_, minCycleTime_, maxCycleTime_, cumulativeCycleTime_, cumulativeCycleTimeCount_, nanoTimeOld_;
    private boolean isMinCycleTimeChanged_, isMaxCycleTimeChanged_;

    /**
     *
     * @param nanoTime time of the last cycle of the cycle which is started, the time of the start
     *                 when the cycle has never run
     */
    LadderCycleStatistics(long nanoTime) {
        cycleTime_ = 0;
        minCycleTime_ = Long.MAX_VALUE;
        maxCycleTime_ = 0;
        cumulativeCycleTime_ = 0;
        cumulativeCycleTimeCount_ = 0;
        nanoTimeOld_ = nanoTime;
        isMinCycleTimeChanged_ = false;
        isMaxCycleTimeChanged_ = false;
    }

    /**
     * add the time of a cycle : the shortest and the longest times and the cumulated times are
     * changed only when the cycle has run a positive time, but the time of the cycle is answered
     * even when it answers no time : the cycle of the ladder waits the ideal time of a cycle with
     * it, as it does when a cycle is interrupted
     *
     * @param nanoTime time at the end of the cycle
     * @return the time of the cycle, 0 or a negative time when the cycle answered no time
     */
    long add(long nanoTime) {
        long cycleTime = nanoTime - nanoTimeOld_;

        isMinCycleTimeChanged_ = false;
        isMaxCycleTimeChanged_ = false;
        cycleTime_ = cycleTime;
        if (cycleTime > 0) {
            if ((cycleTime - minCycleTime_) < 0) {
                minCycleTime_ = cycleTime;
                isMinCycleTimeChanged_ = true;
            } else if ((cycleTime - maxCycleTime_) > 0) {
                maxCycleTime_ = cycleTime;
                isMaxCycleTimeChanged_ = true;
            }
            cumulativeCycleTime_ += cycleTime;
            cumulativeCycleTimeCount_++;
            nanoTimeOld_ = nanoTime;
        }
        return cycleTime_;
    }

    /**
     *
     * @return the time of the last cycle which has been kept
     */
    long cycleTime() {
        return cycleTime_;
    }

    /**
     *
     * @return the shortest time of a cycle, Long.MAX_VALUE when no cycle has been kept
     */
    long minCycleTime() {
        return minCycleTime_;
    }

    /**
     *
     * @return the longest time of a cycle, 0 when no cycle has been kept
     */
    long maxCycleTime() {
        return maxCycleTime_;
    }

    /**
     *
     * @return true when the shortest time of the cycles has been changed by the last cycle
     */
    boolean isMinCycleTimeChanged() {
        return isMinCycleTimeChanged_;
    }

    /**
     *
     * @return true when the longest time of the cycles has been changed by the last cycle
     */
    boolean isMaxCycleTimeChanged() {
        return isMaxCycleTimeChanged_;
    }

    /**
     *
     * @return the sum of the times of the cycles since the last refresh of the view
     */
    long cumulativeCycleTime() {
        return cumulativeCycleTime_;
    }

    /**
     *
     * @return the number of the cycles since the last refresh of the view
     */
    long cumulativeCycleTimeCount() {
        return cumulativeCycleTimeCount_;
    }

    /**
     * the view of the ladders is drawn again when the cycles cumulate the time of a refresh of the
     * view, even when the cycles run faster than the ideal time of a cycle
     *
     * @param refreshCycleTime time of the cycles which are drawn again together
     * @return true when the view has to be drawn again
     */
    boolean isViewRefreshDue(long refreshCycleTime) {
        return (refreshCycleTime - cumulativeCycleTime_) < 0;
    }

    /**
     * the cumulated times are answered to the view of the ladders, so they are counted again from
     * the next cycle
     */
    void takeCumulative() {
        cumulativeCycleTime_ = 0;
        cumulativeCycleTimeCount_ = 0;
    }
}
