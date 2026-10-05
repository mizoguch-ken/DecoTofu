/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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
     * the states of a slave of the SOEM enums : the structs of the context hold
     * them as int, so the enums are answered once here instead of at every
     * comparison of the monitor
     */
    private static final int STATE_NONE = SoemEtherCATType.ec_state.EC_STATE_NONE.intValue();
    private static final int STATE_ACK = SoemEtherCATType.ec_state.EC_STATE_ACK.intValue();
    private static final int STATE_SAFE_OP = SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue();
    private static final int STATE_OPERATIONAL = SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue();
    private static final int STATE_ERROR = SoemEtherCATType.ec_state.EC_STATE_ERROR.intValue();

    /**
     * the state of a slave in safe op that answers an error : SOEM adds the
     * error bit to the state of the slave
     */
    private static final int STATE_SAFE_OP_ERROR = STATE_SAFE_OP + STATE_ERROR;

    /**
     * the state to answer to a slave in safe op with an error to make it
     * answer again
     */
    private static final int STATE_SAFE_OP_ACK = STATE_SAFE_OP + STATE_ACK;

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
        return SoemAwait.awaitZero(runningTasks_, timeoutMillis);
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
                    while (!exit_) {
                        checkSlaves();
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

    /**
     * one period of the monitor : the state of the slaves of the group is read
     * when the work counter of the cycle is not the expected one or when a
     * slave was not operational at the last period, then every slave that is
     * not operational is put back in the operational state
     */
    private void checkSlaves() {
        int docheckstate = context_.grouplist[0].docheckstate.get();

        if ((parcel_.wkc.get() < expectedWKC_) || (docheckstate == SoemOsal.TRUE)) {
            context_.grouplist[0].docheckstate.set(SoemOsal.FALSE);
            // the state of the slaves is read : the sdo calls of the java
            // side and the leave of the ecat thread use the same context
            synchronized (ecatLock_) {
                soem_.ecx_readstate(context_);
            }
            for (int slave = 1; (slave < context_.slavelist.length) && (slave <= context_.slavecount.get()); slave++) {
                SoemEtherCATMain.ec_slavet sl = context_.slavelist[slave];

                if ((sl.group.get() == 0) && (sl.state.get() != STATE_OPERATIONAL)) {
                    context_.grouplist[0].docheckstate.set(SoemOsal.TRUE);
                    checkSlaveState(slave, sl);
                    checkLostSlave(slave, sl);
                }
            }
            // no slave of the group is missing any more : the group resumed
            if ((docheckstate == SoemOsal.TRUE) && (context_.grouplist[0].docheckstate.get() == SoemOsal.FALSE)) {
                notifyListeners(listener -> listener.messageAllSlavesResumedOperationalSoemEcatCheck());
            }
        }
    }

    /**
     * a slave of the group that does not answer in the operational state : ask
     * it the operational state, reconfigure it when it is in a state that can
     * not answer, and flag it lost when it disappeared from the network
     *
     * @param slave number of the slave in the context of SOEM
     * @param sl that slave of the context of SOEM
     */
    private void checkSlaveState(int slave, SoemEtherCATMain.ec_slavet sl) {
        if (sl.state.get() == STATE_SAFE_OP_ERROR) {
            notifyListeners(listener -> listener.errorSafeOpErrorSoemEcatCheck(slave));
            sl.state.set(STATE_SAFE_OP_ACK);
            synchronized (ecatLock_) {
                soem_.ecx_writestate(context_, slave);
            }
        } else if (sl.state.get() == STATE_SAFE_OP) {
            notifyListeners(listener -> listener.warningSafeOpSoemEcatCheck(slave));
            sl.state.set(STATE_OPERATIONAL);
            synchronized (ecatLock_) {
                soem_.ecx_writestate(context_, slave);
            }
        } else if (sl.state.get() > STATE_NONE) {
            boolean reconfigured;

            synchronized (ecatLock_) {
                reconfigured = (soem_.ecx_reconfig_slave(context_, slave, SoemEtherCAT.EC_TIMEOUTMON) == SoemOsal.TRUE);
            }
            if (reconfigured) {
                sl.islost.set(SoemOsal.FALSE);
                notifyListeners(listener -> listener.messageReconfiguredSoemEcatCheck(slave));
            }
        } else if (sl.islost.get() == SoemOsal.FALSE) {
            synchronized (ecatLock_) {
                soem_.ecx_statecheck(context_, slave, STATE_OPERATIONAL, SoemEtherCATType.EC_TIMEOUTRET);
            }
            if (sl.state.get() == STATE_NONE) {
                sl.islost.set(SoemOsal.TRUE);
                notifyListeners(listener -> listener.errorLostSoemEcatCheck(slave));
            }
        }
    }

    /**
     * a slave flagged lost is found again when it answers on the network
     *
     * @param slave number of the slave in the context of SOEM
     * @param sl that slave of the context of SOEM
     */
    private void checkLostSlave(int slave, SoemEtherCATMain.ec_slavet sl) {
        if (sl.islost.get() != SoemOsal.TRUE) {
            return;
        }
        if (sl.state.get() != STATE_NONE) {
            sl.islost.set(SoemOsal.FALSE);
            notifyListeners(listener -> listener.messageFoundSoemEcatCheck(slave));
        } else {
            boolean recovered;

            synchronized (ecatLock_) {
                recovered = (soem_.ecx_recover_slave(context_, slave, SoemEtherCAT.EC_TIMEOUTMON) == SoemOsal.TRUE);
            }
            if (recovered) {
                sl.islost.set(SoemOsal.FALSE);
                notifyListeners(listener -> listener.messageRecoveredSoemEcatCheck(slave));
            }
        }
    }

    /**
     * answer one event of the monitor to the listeners of the plugin : the
     * plugin asks to be notified or not with setNotifyCheck()
     *
     * @param event the call to do on every listener
     */
    private void notifyListeners(Consumer<SoemPluginListener> event) {
        if (isNotifyCheck_) {
            for (SoemPluginListener listener : eventListenerList_.getListeners(SoemPluginListener.class)) {
                event.accept(listener);
            }
        }
    }
}

