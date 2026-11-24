/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import javafx.concurrent.Task;
import ken.mizoguch.console.Console;
import ken.mizoguch.decotofu.I18n;
import ken.mizoguch.soem.Soem;

/**
 * the task of the cycle of the ladders : the task is created by the service of the editor when a
 * cycle is started, and it runs until the cycling flag of the editor is cleared or until the task
 * is cancelled.
 *
 * the values of the editor are read at every cycle, so a registration or an ideal cycle time
 * changed while the cycle is running is taken into account. the io and the state of the ladders
 * are changed under the monitor of the editor
 *
 * @author mizoguch-ken
 */
class LadderCycleTask extends Task<Void> {

    /**
     * number of the cycle errors accepted before the cycle is stopped
     */
    private static final int LADDER_CYCLE_ERROR_LIMIT = 100;

    private final Ladders ladders_;
    private final AtomicBoolean finished_;

    /**
     *
     * @param ladders the editor which started the cycle
     */
    LadderCycleTask(Ladders ladders) {
        ladders_ = ladders;
        finished_ = new AtomicBoolean(false);
    }

    /**
     *
     * @return the flag set when the cycle is no longer running
     */
    AtomicBoolean getFinished() {
        return finished_;
    }

    @Override
    protected Void call() {
        DesignLaddersController controller;
        Object lock;
        LadderIoStore ioStore;
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap;
        CopyOnWriteArrayList<LadderRegisterSoemIo> registerSoemIn, registerSoemOut;
        LadderCycleStatistics statistics;
        LadderCycleErrors errors;

        try {
            LadderRegisterSoemIo registerSoemIo;
            LadderIo cycleIo;
            Soem soem;
            Long registerSoemValue;
            long cycleStartTime;
            int index;

            controller = ladders_.getDesignController();
            lock = ladders_.getLock();
            ioStore = ladders_.getIoStore();
            ioMap = ioStore.getIoMap();
            registerSoemIn = ladders_.getRegisterSoemIn();
            registerSoemOut = ladders_.getRegisterSoemOut();
            LadderTelemetry telemetry = ladders_.getTelemetry();

            controller.runViewRunning(true);

            cycleStartTime = System.nanoTime();
            statistics = new LadderCycleStatistics(cycleStartTime);
            ladders_.updateCycleTimes(0L, 0L, 0L);
            errors = new LadderCycleErrors(LADDER_CYCLE_ERROR_LIMIT);
            LadderCyclePacer pacer = new LadderCyclePacer();
            LongSupplier idealCycleTimeSupplier = ladders_::getIdealCycleTime;
            BooleanSupplier keepWaiting = () -> ladders_.isCycling() && !isCancelled();

            while (ladders_.isCycling() && !isCancelled()) {
                boolean refreshView = false;
                String traceEvent = null;
                try {
                    synchronized (lock) {
                        // soem
                        soem = ladders_.getSoem();
                        if (soem != null) {
                            for (index = 0; index < registerSoemOut.size(); index++) {
                                registerSoemIo = registerSoemOut.get(index);
                                cycleIo = ioStore.globalIo(registerSoemIo.address());
                                if (cycleIo != null) {
                                    soem.out(registerSoemIo.slave(), registerSoemIo.bitsOffset(),
                                            registerSoemIo.bitsMask(), (long) cycleIo.getValue());
                                }
                            }
                            for (index = 0; index < registerSoemIn.size(); index++) {
                                registerSoemIo = registerSoemIn.get(index);
                                registerSoemValue = soem.in(registerSoemIo.slave(),
                                        registerSoemIo.bitsOffset(), registerSoemIo.bitsMask());
                                cycleIo = ioStore.globalIo(registerSoemIo.address());
                                if ((registerSoemValue != null) && (cycleIo != null)) {
                                    cycleIo.setValue(registerSoemValue);
                                }
                            }
                        }
                        // script io
                        ioStore.applyScriptIo();

                        // ladder
                        Ladder[] laddersLocal = ladders_.getLadders();
                        if (laddersLocal != null) {
                            for (index = 0; index < laddersLocal.length; index++) {
                                laddersLocal[index].run(ioMap, statistics.cycleTime());
                            }
                        }

                        // refresh view
                        refreshView = statistics.isViewRefreshDue(LadderDefaults.LADDER_VIEW_REFRESH_CYCLE_TIME);
                        if (telemetry != null) {
                            traceEvent = telemetry.onCycleComplete(ioStore, statistics.cycleTime());
                        }
                    }
                    errors.clear();
                    if ((telemetry != null) && (traceEvent != null)) {
                        telemetry.notifyListener(traceEvent);
                    }
                } catch (Exception ex) {
                    // the error of a cycle does not stop the cycle thread : an
                    // error which is repeated stops the cycle, because the ladder
                    // would be run in an unknown state
                    if (errors.isFirst()) {
                        Console.writeStackTrace(Ladders.class.getName(), ex);
                    } else {
                        Console.write(Ladders.class.getName(), I18n.get("log.ladder.cycleError", ex), true);
                    }
                    errors.increase();
                    if (telemetry != null) {
                        String errEvent = telemetry.onCycleError(ex.toString(), statistics.cycleTime());
                        if (errEvent != null) {
                            telemetry.notifyListener(errEvent);
                        }
                    }
                    if (errors.isOverLimit()) {
                        Console.write(Ladders.class.getName(),
                                I18n.get("log.ladder.cycleStoppedAfterErrors", errors.count()), true);
                        ladders_.setCycling(false);
                    }
                }
                // refreshLadder only queues an asynchronous update : keep the lock for io and ladder state
                if (refreshView) {
                    controller.refreshLadder(statistics.cumulativeCycleTime(), statistics.cumulativeCycleTimeCount());
                    statistics.takeCumulative();
                }

                // ideal cycletime
                if (ladders_.getIdealCycleTime() > 0) {
                    if (!pacer.waitUntil(cycleStartTime, idealCycleTimeSupplier, keepWaiting)) {
                        ladders_.setCycling(false);
                    }
                }

                // cycletimme
                long cycleEndTime = System.nanoTime();
                long cycleTime = statistics.add(cycleEndTime);
                updateCycleMetrics(cycleTime, statistics);
                cycleStartTime = cycleEndTime;
                if (statistics.isMinCycleTimeChanged()) {
                    controller.setMinCycleTime(statistics.minCycleTime());
                } else if (statistics.isMaxCycleTimeChanged()) {
                    controller.setMaxCycleTime(statistics.maxCycleTime());
                }
            }
        } catch (Exception ex) {
            switch (ex) {
                case InterruptedException interrupted -> Thread.currentThread().interrupt();
                default -> {
                }
            }
            // an unexpected error of the cycle stops the cycle : the ladder is
            // started again by the user with a new task
            Console.writeStackTrace(Ladders.class.getName(), ex);
        } finally {
            try {
                ladders_.getDesignController().runViewRunning(false);
            } finally {
                // the cycling flag is cleared before the finish flag, so awaitCycleStop()
                // never answers a cycle which is still running as finished
                ladders_.setCycling(false);
                finished_.set(true);
            }
        }
        return null;
    }

    private void updateCycleMetrics(long cycleTime, LadderCycleStatistics statistics) {
        long min = statistics.minCycleTime();
        ladders_.updateCycleTimes(Math.max(0L, cycleTime), (min == Long.MAX_VALUE) ? 0L : min,
                statistics.maxCycleTime());
    }
}
