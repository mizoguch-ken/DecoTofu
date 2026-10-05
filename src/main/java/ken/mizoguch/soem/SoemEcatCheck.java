/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javax.swing.event.EventListenerList;
import ken.mizoguch.console.Console;

/**
 *
 * @author mizoguch-ken
 */
public class SoemEcatCheck extends Service<Void> {

    private final EventListenerList eventListenerList_;

    private final SoemLibrary soem_;

    private final Object ecatLock_;
    private final SoemEtherCAT.ecx_parcelt parcel_;
    private final SoemEtherCATMain.ecx_contextt context_;
    private volatile int expectedWKC_;
    private volatile boolean isNotifyCheck_;
    private volatile boolean exit_;
    private final AtomicInteger runningTasks_;

    /**
     * the period of the monitor of the state of the slaves
     */
    private static final long MONITOR_PERIOD_MILLIS = 10;

    /**
     *
     * @param eventListenerList
     * @param soem
     * @param parcel
     * @param context
     * @param ecatLock the lock of the native calls of the context of SOEM
     */
    public SoemEcatCheck(EventListenerList eventListenerList, SoemLibrary soem, SoemEtherCAT.ecx_parcelt parcel,
            SoemEtherCATMain.ecx_contextt context, Object ecatLock) {
        eventListenerList_ = eventListenerList;
        soem_ = soem;
        parcel_ = parcel;
        context_ = context;
        ecatLock_ = ecatLock;
        expectedWKC_ = 0;
        isNotifyCheck_ = false;
        exit_ = true;
        runningTasks_ = new AtomicInteger();
    }

    /**
     *
     */
    public void init() {
        exit_ = false;
    }

    /**
     *
     */
    public void exit() {
        exit_ = true;
    }

    /**
     * wait the end of the check task : the native context and parcel are freed only when this task
     * is out of the native calls
     *
     * @param timeoutMillis
     * @return true when the task is no longer in the native calls
     */
    public boolean awaitTermination(long timeoutMillis) {
        long deadline = System.currentTimeMillis() + Math.max(0, timeoutMillis);

        while (runningTasks_.get() > 0) {
            if (System.currentTimeMillis() >= deadline) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return runningTasks_.get() == 0;
    }

    /**
     *
     * @param expectedWKC
     */
    public void setExpectedWKC(int expectedWKC) {
        expectedWKC_ = expectedWKC;
    }

    /**
     *
     * @param bln
     * @return
     */
    public boolean setNotifyCheck(boolean bln) {
        isNotifyCheck_ = bln;
        return isNotifyCheck_;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() {
                runningTasks_.incrementAndGet();
                try {
                    int slave, docheckstate;

                    while (!exit_) {
                        docheckstate = context_.grouplist[0].docheckstate.get();
                        if ((parcel_.wkc.get() < expectedWKC_) || (docheckstate == SoemOsal.TRUE)) {
                            context_.grouplist[0].docheckstate.set(SoemOsal.FALSE);
                            // the state of the slaves is read : the sdo calls of the java
                            // side and the leave of the ecat thread use the same context
                            synchronized (ecatLock_) {
                                soem_.ecx_readstate(context_);
                            }
                            for (slave = 1; (slave < context_.slavelist.length)
                                    && (slave <= context_.slavecount.get()); slave++) {
                                if ((context_.slavelist[slave].group.get() == 0) && (context_.slavelist[slave].state
                                        .get() != SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue())) {
                                    context_.grouplist[0].docheckstate.set(SoemOsal.TRUE);
                                    if (context_.slavelist[slave].state
                                            .get() == (SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue()
                                                    + SoemEtherCATType.ec_state.EC_STATE_ERROR.intValue())) {
                                        if (isNotifyCheck_) {
                                            for (SoemPluginListener listener : eventListenerList_
                                                    .getListeners(SoemPluginListener.class)) {
                                                listener.errorSafeOpErrorSoemEcatCheck(slave);
                                            }
                                        }
                                        context_.slavelist[slave].state
                                                .set(SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue()
                                                        + SoemEtherCATType.ec_state.EC_STATE_ACK.intValue());
                                        synchronized (ecatLock_) {
                                            soem_.ecx_writestate(context_, slave);
                                        }
                                    } else if (context_.slavelist[slave].state
                                            .get() == SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue()) {
                                        if (isNotifyCheck_) {
                                            for (SoemPluginListener listener : eventListenerList_
                                                    .getListeners(SoemPluginListener.class)) {
                                                listener.warningSafeOpSoemEcatCheck(slave);
                                            }
                                        }
                                        context_.slavelist[slave].state
                                                .set(SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue());
                                        synchronized (ecatLock_) {
                                            soem_.ecx_writestate(context_, slave);
                                        }
                                    } else if (context_.slavelist[slave].state
                                            .get() > SoemEtherCATType.ec_state.EC_STATE_NONE.intValue()) {
                                        boolean reconfigured;

                                        synchronized (ecatLock_) {
                                            reconfigured = (soem_.ecx_reconfig_slave(context_, slave,
                                                    SoemEtherCAT.EC_TIMEOUTMON) == SoemOsal.TRUE);
                                        }
                                        if (reconfigured) {
                                            context_.slavelist[slave].islost.set(SoemOsal.FALSE);
                                            if (isNotifyCheck_) {
                                                for (SoemPluginListener listener : eventListenerList_
                                                        .getListeners(SoemPluginListener.class)) {
                                                    listener.messageReconfiguredSoemEcatCheck(slave);
                                                }
                                            }
                                        }
                                    } else if (context_.slavelist[slave].islost.get() == SoemOsal.FALSE) {
                                        synchronized (ecatLock_) {
                                            soem_.ecx_statecheck(context_, slave,
                                                    SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue(),
                                                    SoemEtherCATType.EC_TIMEOUTRET);
                                        }
                                        if (context_.slavelist[slave].state
                                                .get() == SoemEtherCATType.ec_state.EC_STATE_NONE.intValue()) {
                                            context_.slavelist[slave].islost.set(SoemOsal.TRUE);
                                            if (isNotifyCheck_) {
                                                for (SoemPluginListener listener : eventListenerList_
                                                        .getListeners(SoemPluginListener.class)) {
                                                    listener.errorLostSoemEcatCheck(slave);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (context_.slavelist[slave].islost.get() == SoemOsal.TRUE) {
                                    if (context_.slavelist[slave].state.get() == SoemEtherCATType.ec_state.EC_STATE_NONE
                                            .intValue()) {
                                        boolean recovered;

                                        synchronized (ecatLock_) {
                                            recovered = (soem_.ecx_recover_slave(context_, slave,
                                                    SoemEtherCAT.EC_TIMEOUTMON) == SoemOsal.TRUE);
                                        }
                                        if (recovered) {
                                            context_.slavelist[slave].islost.set(SoemOsal.FALSE);
                                            if (isNotifyCheck_) {
                                                for (SoemPluginListener listener : eventListenerList_
                                                        .getListeners(SoemPluginListener.class)) {
                                                    listener.messageRecoveredSoemEcatCheck(slave);
                                                }
                                            }
                                        }
                                    } else {
                                        context_.slavelist[slave].islost.set(SoemOsal.FALSE);
                                        if (isNotifyCheck_) {
                                            for (SoemPluginListener listener : eventListenerList_
                                                    .getListeners(SoemPluginListener.class)) {
                                                listener.messageFoundSoemEcatCheck(slave);
                                            }
                                        }
                                    }
                                }
                            }
                            if (isNotifyCheck_) {
                                if ((docheckstate == SoemOsal.TRUE)
                                        && (context_.grouplist[0].docheckstate.get() == SoemOsal.FALSE)) {
                                    for (SoemPluginListener listener : eventListenerList_
                                            .getListeners(SoemPluginListener.class)) {
                                        listener.messageAllSlavesResumedOperationalSoemEcatCheck();
                                    }
                                }
                            }
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(MONITOR_PERIOD_MILLIS);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            exit_ = true;
                        }
                    }
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    Console.writeStackTrace(SoemEcatCheck.class.getName(), ex);
                } finally {
                    runningTasks_.decrementAndGet();
                }
                return null;
            }
        };
    }
}
