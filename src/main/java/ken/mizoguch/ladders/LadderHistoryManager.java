/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author mizoguch-ken
 */
public class LadderHistoryManager {

    private Deque<LadderHistory> pasts;
    private Deque<LadderHistory> futures;

    /**
     *
     */
    public LadderHistoryManager() {
        pasts = null;
        futures = null;
    }

    /**
     *
     */
    public void clear() {
        clearPasts();
        clearFutures();
    }

    /**
     *
     * @param historyManager
     * @param generation
     */
    public void set(LadderHistoryManager historyManager, int generation) {
        clear();
        pasts = historyManager.getPasts();
        futures = historyManager.getFutures();

        if (pasts != null) {
            if ((generation == 0) || (pasts.isEmpty())) {
                clearPasts();
            } else {
                trimToGeneration(pasts, generation);
            }
        }
        if (futures != null) {
            if ((generation == 0) || (futures.isEmpty())) {
                clearFutures();
            } else {
                trimToGeneration(futures, generation);
            }
        }
    }

    /**
     *
     * @return
     */
    public Deque<LadderHistory> getPasts() {
        return pasts;
    }

    /**
     *
     * @return
     */
    public Deque<LadderHistory> getFutures() {
        return futures;
    }

    /**
     *
     * @param history
     * @param generation
     */
    public void push(LadderHistory history, int generation) {
        pushPastNonClearFutures(history, generation);
        clearFutures();
    }

    /**
     *
     * @param generation
     * @return
     */
    public LadderHistory undo(int generation) {
        LadderHistory history = null;

        if (generation != 0) {
            if (pasts != null) {
                if (!pasts.isEmpty()) {
                    history = pasts.pop();
                    if (futures == null) {
                        futures = new ArrayDeque<>();
                    }
                    futures.push(history);
                    trimToGeneration(futures, generation);

                    if (pasts.isEmpty()) {
                        pasts = null;
                    }
                }
            }
        }
        return history;
    }

    /**
     *
     * @param generation
     * @return
     */
    public LadderHistory redo(int generation) {
        LadderHistory history = null;

        if (generation != 0) {
            if (futures != null) {
                if (!futures.isEmpty()) {
                    history = futures.pop();
                    pushPastNonClearFutures(history, generation);

                    if (futures.isEmpty()) {
                        futures = null;
                    }
                }
            }
        }
        return history;
    }

    private void clearPasts() {
        if (pasts != null) {
            pasts.clear();
            pasts = null;
        }
    }

    private void clearFutures() {
        if (futures != null) {
            futures.clear();
            futures = null;
        }
    }

    private void pushPastNonClearFutures(LadderHistory history, int generation) {
        if (pasts == null) {
            pasts = new ArrayDeque<>();
        }
        pasts.push(history);
        trimToGeneration(pasts, generation);
    }

    /**
     * the histories older than the generation are dropped : the oldest ones are the last of the
     * deque, which is a sequenced collection
     *
     * @param histories the pasts or the futures of the editor
     * @param generation number of the histories kept
     */
    private static void trimToGeneration(Deque<LadderHistory> histories, int generation) {
        if ((generation > -1) && (histories.size() > generation)) {
            for (int index = histories.size() - generation; index > 0; index--) {
                histories.removeLast();
            }
        }
    }
}
