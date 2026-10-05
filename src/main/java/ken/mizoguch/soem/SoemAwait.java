/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * wait the end of the tasks of the ecat : the native memory of SOEM is freed
 * only when no task is inside a native call any more.
 *
 * @author mizoguch-ken
 */
final class SoemAwait {

    /** the period of the poll of the counter of the running tasks. */
    private static final long POLL_MILLIS = 5;

    /**
     * a utility class is never instantiated
     */
    private SoemAwait() {
    }

    /**
     *
     * @param runningTasks number of the tasks of the ecat inside a native call
     * @param timeoutMillis time left to wait the end of those tasks
     * @return true when the counter answers zero before the timeout
     */
    static boolean awaitZero(AtomicInteger runningTasks, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + Math.max(0, timeoutMillis);

        while (runningTasks.get() > 0) {
            if (System.currentTimeMillis() >= deadline) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(POLL_MILLIS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return runningTasks.get() == 0;
    }
}
