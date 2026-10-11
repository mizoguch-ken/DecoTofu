/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

/**
 * the errors of the cycles of the ladders : an error of a cycle does not stop the cycle of the
 * ladders, because an io which is not registered or a block which answers an error only makes a
 * wrong value for a time.
 *
 * but an error which is repeated at every cycle means that the ladder is run in an unknown state,
 * so the cycle is stopped when the errors are more than the limit
 *
 * @author mizoguch-ken
 */
final class LadderCycleErrors {

    private int count_;
    private final int limit_;

    /**
     *
     * @param limit number of the errors of the cycles which are accepted before the cycle is stopped
     */
    LadderCycleErrors(int limit) {
        count_ = 0;
        limit_ = limit;
    }

    /**
     * the first error of a series of errors answers the stack trace of the error, the next errors
     * only answer their message : a repeated error would fill the console with the same trace
     *
     * @return true when no cycle has made an error since the last cycle which has run
     */
    boolean isFirst() {
        return count_ == 0;
    }

    /**
     *
     * @return the number of the errors of the cycles since the last cycle which has run
     */
    int increase() {
        return ++count_;
    }

    /**
     *
     * @return the number of the errors of the cycles since the last cycle which has run
     */
    int count() {
        return count_;
    }

    /**
     *
     * @return true when the errors of the cycles are more than the limit of the cycle
     */
    boolean isOverLimit() {
        return count_ > limit_;
    }

    /**
     * a cycle which runs clears the errors of the cycles before it
     */
    void clear() {
        count_ = 0;
    }
}
