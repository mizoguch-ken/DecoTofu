/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javax.swing.event.EventListenerList;
import jnr.ffi.Pointer;
import ken.mizoguch.console.Console;

/**
 *
 * @author mizoguch-ken
 */
public class SoemEcatThread extends Service<Void> {

    private final EventListenerList eventListenerList_;

    /** the callback of the java side asked for one slave with po2so(). */
    private static final class EcatPO2SO {

        private long eep_man;
        private long eep_id;
        private Callable<Integer> func;

        public EcatPO2SO(long eep_man, long eep_id, Callable<Integer> func) {
            this.eep_man = eep_man;
            this.eep_id = eep_id;
            this.func = func;
        }
    }

    /** one byte of the map of the group queued for the next cycle. */
    private static final class EcatData {

        private int keepMask;
        private int value;

        public EcatData(int keepMask, int value) {
            this.keepMask = keepMask;
            this.value = value;
        }
    }

    private final SoemLibrary soem_;
    private final Object lockOut_ = new Object();
    private final Object ecatLock_;
    private final SoemEtherCAT.ecx_parcelt parcel_;
    private final SoemEtherCATMain.ecx_contextt context_;
    private final ConcurrentMap<Integer, EcatPO2SO> po2so_;
    private final ConcurrentMap<Long, EcatData> out_;
    private final AtomicInteger runningTasks_;
    private SoemEcatCheck ecatCheck_;
    private volatile boolean init_, exit_, portOpen_;

    /**
     * the time to wait the answer of the java fx side at the PO2SO callback
     */
    private static final long PO2SO_WAIT_MILLIS = 500;

    /**
     * the time the cyclic task waits the end of the current cycle
     */
    private static final long CYCLE_WAIT_NANOS = 200000;

    /**
     *
     * @param soem
     * @param parcel
     * @param context
     * @param ecatLock the lock of the native calls of the context of SOEM
     */
    public SoemEcatThread(SoemLibrary soem, SoemEtherCAT.ecx_parcelt parcel, SoemEtherCATMain.ecx_contextt context,
            Object ecatLock) {
        eventListenerList_ = new EventListenerList();
        soem_ = soem;
        parcel_ = parcel;
        context_ = context;
        ecatLock_ = ecatLock;
        po2so_ = new ConcurrentHashMap<>();
        out_ = new ConcurrentHashMap<>();
        ecatCheck_ = null;
        init_ = false;
        exit_ = true;
        portOpen_ = false;
        runningTasks_ = new AtomicInteger();
    }

    /**
     * the port of SOEM is open when ecx_init() succeeded and ecx_close() is not
     * called yet : the context of SOEM has to be closed only in that case
     *
     * @return
     */
    public boolean isPortOpen() {
        return portOpen_;
    }

    /**
     *
     * @param listener
     */
    public void addSoemEcatListener(SoemPluginListener listener) {
        boolean isListener = false;
        for (SoemPluginListener sel : eventListenerList_.getListeners(SoemPluginListener.class)) {
            if (sel == listener) {
                isListener = true;
                break;
            }
        }
        if (!isListener) {
            eventListenerList_.add(SoemPluginListener.class, listener);
        }
    }

    /**
     *
     * @param listener
     */
    public void removeSoemEcatListener(SoemPluginListener listener) {
        eventListenerList_.remove(SoemPluginListener.class, listener);
    }

    /**
     *
     * @param slave
     * @param eep_man
     * @param eep_id
     * @param func
     */
    public void po2so(int slave, long eep_man, long eep_id, Callable<Integer> func) {
        EcatPO2SO po2so = po2so_.get(slave);

        if (po2so != null) {
            po2so.eep_man = eep_man;
            po2so.eep_id = eep_id;
            po2so.func = func;
        } else {
            po2so_.put(slave, new EcatPO2SO(eep_man, eep_id, func));
        }
    }

    /**
     *
     * @param ifname
     * @param ifname2
     * @param pIOmap
     * @return
     */
    public boolean init(String ifname, String ifname2, Pointer pIOmap) {
        if (ecatCheck_ == null) {
            try {
                int index, size, state;

                exit_ = false;
                if (!openPort(ifname, ifname2)) {
                    return false;
                }

                if (soem_.ecx_config_init(context_, SoemOsal.FALSE) > 0) {
                    context_.slavelist[0].state.set(SoemEtherCATType.ec_state.EC_STATE_PRE_OP.intValue());
                    soem_.ecx_writestate(context_, 0);
                    soem_.ecx_statecheck(context_, 0, SoemEtherCATType.ec_state.EC_STATE_PRE_OP.intValue(),
                            SoemEtherCATType.EC_TIMEOUTSTATE);
                    if (context_.slavecount.get() > 0) {
                        for (index = 1, size = context_.slavecount.get(); (index < context_.slavelist.length)
                                && (index <= size); index++) {
                            EcatPO2SO po2so = po2so_.get(index);

                            if (po2so != null) {
                                SoemEtherCATMain.ec_slavet sl = context_.slavelist[index];

                                if ((sl.eep_man.get() == po2so.eep_man) && (sl.eep_id.get() == po2so.eep_id)) {
                                    sl.PO2SOconfig.PO2SOconfig.set((int slave) -> answerPo2so(slave));
                                } else {
                                    writeLog("PO2SO: EEP_MAN[" + sl.eep_man.get() + ":" + po2so.eep_man + "] & EEP_ID["
                                            + sl.eep_id.get() + ":" + po2so.eep_id
                                            + "] of Slave[" + index + "] are different ", true);
                                    initFail();
                                    return false;
                                }
                            }
                        }
                    }
                    // the map of the group is written in pIOmap : the answer of the
                    // native call is the size of the map, 0 means that the process
                    // data of the slaves was not mapped
                    int map = soem_.ecx_config_map_group(context_, pIOmap, 0);
                    if (map <= 0) {
                        writeLog("ecx_config_map_group() failed : [" + map + "]", true);
                        initFail();
                        return false;
                    }
                    soem_.ecx_configdc(context_);
                    context_.slavelist[0].state.set(SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue());
                    soem_.ecx_writestate(context_, 0);
                    state = soem_.ecx_statecheck(context_, 0, SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue(),
                            SoemEtherCATType.EC_TIMEOUTSTATE * 4);
                    if (state != SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue()) {
                        writeLog("the slaves are not all in SAFE_OP : [" + state + "]", true);
                    }
                    ecatCheck_ = new SoemEcatCheck(eventListenerList_, soem_, parcel_, context_, ecatLock_);
                    ecatCheck_.setExpectedWKC(
                            (context_.grouplist[0].outputsWKC.get() * 2) + context_.grouplist[0].inputsWKC.get());
                    context_.slavelist[0].state.set(SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue());
                    soem_.ecx_send_processdata(context_);
                    soem_.ecx_receive_processdata(context_, SoemEtherCATType.EC_TIMEOUTRET);
                    soem_.ecx_writestate(context_, 0);
                    state = soem_.ecx_statecheck(context_, 0, SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue(),
                            SoemEtherCATType.EC_TIMEOUTSTATE);
                    if (state == SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue()) {
                        ecatCheck_.init();
                        if (Platform.isFxApplicationThread()) {
                            runEcatCheck();
                        } else {
                            Platform.runLater(() -> {
                                runEcatCheck();
                            });
                        }
                        init_ = true;
                        return true;
                    }
                    // the slaves did not reach OPERATIONAL : leave the native
                    // context as before init(), so start() can be tried again
                    writeLog("the slaves are not in OPERATIONAL : [" + state + "]", true);
                    initFail();
                } else {
                    writeLog("ecx_config_init() failed : no slave configured", true);
                    initFail();
                }
            } catch (Exception ex) {
                Console.writeStackTrace(SoemEcatThread.class.getName(), ex);
                initFail();
            }
        }
        return false;
    }

    /**
     * open the port of SOEM on the network interface of the master
     *
     * @param ifname name of the network interface of the master
     * @param ifname2 name of the second interface of a redundant port, null
     * when the port of SOEM is not redundant
     * @return true when the port of SOEM is open
     */
    private boolean openPort(String ifname, String ifname2) {
        if (ifname2 == null) {
            if (soem_.ecx_init(context_, ifname) <= 0) {
                writeLog("ecx_init() failed on [" + ifname + "]", true);
                return false;
            }
        } else {
            if (soem_.ecx_init_redundant(context_, soem_.ec_redport(context_), ifname, ifname2) <= 0) {
                writeLog("ecx_init_redundant() failed on [" + ifname + "][" + ifname2 + "]", true);
                return false;
            }
        }
        // from now, the port of SOEM is open and it has to be closed
        portOpen_ = true;
        return true;
    }

    /**
     * the PO2SO callback SOEM calls while it configures a slave : the answer of
     * the java side is waited on the fx thread, the native call of SOEM must
     * not wait for ever
     *
     * @param slave the slave SOEM is configuring
     * @return the answer of the java side, 0 when it does not answer
     */
    private int answerPo2so(int slave) {
        EcatPO2SO po2so = po2so_.get(slave);

        if (po2so == null) {
            // the callback was removed while the slaves are configured
            return 0;
        }
        RunnableFuture<Integer> future = new FutureTask<>(po2so.func);

        if (Platform.isFxApplicationThread()) {
            try {
                future.run();
                return future.get(PO2SO_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Console.writeStackTrace(SoemEcatThread.class.getName(), ex);
            } catch (ExecutionException | TimeoutException ex) {
                Console.writeStackTrace(SoemEcatThread.class.getName(), ex);
            }
        } else {
            try {
                Platform.runLater(future);
                // the java side may never answer : the
                // configuration of the slaves must not wait
                // for ever inside the native call
                return future.get(PO2SO_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Console.writeStackTrace(SoemEcatThread.class.getName(), ex);
            } catch (ExecutionException ex) {
                Console.writeStackTrace(SoemEcatThread.class.getName(), ex);
            } catch (TimeoutException ex) {
                future.cancel(true);
                writeLog("PO2SO : no answer of the java side for the Slave[" + slave + "]", true);
            }
        }
        return 0;
    }


    /**
     * the configuration of the slaves failed : leave the native context as it was
     * before init(), so that start() can be tried again
     */
    private void initFail() {
        if (ecatCheck_ != null) {
            ecatCheck_.exit();
            ecatCheck_.cancel();
            ecatCheck_ = null;
        }
        init_ = false;
        closePort();
    }

    /**
     * close the port of SOEM only when ecx_init() opened it
     */
    private void closePort() {
        if (portOpen_ && (soem_ != null) && (context_ != null)) {
            portOpen_ = false;
            soem_.ecx_close(context_);
        }
    }

    private void runEcatCheck() {
        if (ecatCheck_.getState() == State.READY) {
            ecatCheck_.start();
        } else {
            ecatCheck_.restart();
        }
    }

    /**
     * stop the java tasks : no native call of SOEM is done here, the monitor task
     * and the native thread of the process data are still using the context of
     * SOEM. The slaves are left in INIT state by shutdown(), when every native
     * call is over
     */
    public void exit() {
        exit_ = true;
        if (ecatCheck_ != null) {
            ecatCheck_.exit();
            ecatCheck_.cancel();
        }
    }

    /**
     * leave the slaves in INIT state : to call after exit() and
     * awaitTermination(), and after the end of the native thread of the process
     * data, so that no native call uses the context at the same time
     */
    public void shutdown() {
        if (init_ && (soem_ != null) && (context_ != null)) {
            init_ = false;
            // the sdo calls of the java side use the context until it is freed
            synchronized (ecatLock_) {
                context_.slavelist[0].state.set(SoemEtherCATType.ec_state.EC_STATE_INIT.intValue());
                soem_.ecx_writestate(context_, 0);
                soem_.ecx_statecheck(context_, 0, SoemEtherCATType.ec_state.EC_STATE_INIT.intValue(),
                        SoemEtherCATType.EC_TIMEOUTSTATE);
            }
        }
    }

    /**
     * wait the end of the check task and of the ec task : the native context and the native parcel
     * are freed only when both tasks are out of the native calls
     *
     * @param timeoutMillis
     * @return true when both tasks are no longer in the native calls
     */
    public boolean awaitTermination(long timeoutMillis) {
        long deadline = System.currentTimeMillis() + Math.max(0, timeoutMillis);
        boolean terminated = true;

        if (ecatCheck_ != null) {
            terminated = ecatCheck_.awaitTermination(remainingMillis(deadline));
        }
        return SoemAwait.awaitZero(runningTasks_, remainingMillis(deadline)) && terminated;
    }

    private long remainingMillis(long deadline) {
        return Math.max(1, deadline - System.currentTimeMillis());
    }

    /**
     *
     * @param bln
     * @return
     */
    public Boolean setNotifyCheck(boolean bln) {
        if (ecatCheck_ != null) {
            return ecatCheck_.setNotifyCheck(bln);
        }
        return null;
    }

    /**
     *
     * @param slave
     * @param bitsOffset
     * @param bitsMask
     * @param value
     * @return
     */
    public Long out(int slave, long bitsOffset, long bitsMask, long value) {
        SoemEtherCATMain.ec_slavet sl = context_.slavelist[slave];
        Pointer group = context_.slavelist[0].outputs.get();
        Pointer outputs = sl.outputs.get();

        if ((group != null) && (outputs != null)) {
            int mapStartbit = sl.Ostartbit.get();
            long Obits = sl.Obits.get();
            long bits = Obits - bitsOffset;

            if ((bitsOffset >= 0) && (bitsOffset < Obits) && (bitsMask > 0) && (bits > 0)) {
                long address = outputs.address() - group.address() + (bitsOffset / 8);
                int startbit = mapStartbit + ((int) (bitsOffset % 8));

                if (bits < SoemBits.MAX_BITS) {
                    bitsMask &= (1L << bits) - 1;
                }
                // the bytes of the map of the group that hold the variable : no
                // byte of a neighbour slave is written
                int byteCount = SoemBits.byteCount(startbit, SoemBits.bitLength(bitsMask),
                        SoemBits.mapBytes(mapStartbit, Obits) - (bitsOffset / 8));
                // the queue is keyed with the address in the map of the group : the
                // cyclic task applies it at the end of the current cycle
                synchronized (lockOut_) {
                    for (int cnt = 0; cnt < byteCount; cnt++) {
                        int maskByte = SoemBits.byteAt(bitsMask, startbit, cnt);
                        EcatData data;

                        if (maskByte == 0) {
                            continue;
                        }
                        int keep = SoemBits.keepMask(maskByte);
                        int bitsByte = SoemBits.byteAt(value & bitsMask, startbit, cnt);

                        data = out_.get(address + cnt);
                        if (data != null) {
                            // merge with the value already queued for that byte
                            data.keepMask &= keep;
                            data.value |= bitsByte;
                        } else {
                            int current = Byte.toUnsignedInt(group.getByte(address + cnt));

                            if (current != Byte.toUnsignedInt(SoemBits.merge((byte) current, keep, bitsByte))) {
                                out_.put(address + cnt, new EcatData(keep, bitsByte));
                            }
                        }
                    }
                }
                return address;
            }
        }
        return null;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runningTasks_.incrementAndGet();
                try {
                    Pointer pointer;
                    Map.Entry<Long, EcatData> entry;
                    int value;

                    if (!exit_ && (soem_.ec_run(parcel_) != SoemOsal.TRUE)) {
                        // the native thread of the process data is not started : the
                        // cyclic process of the ecat can not run
                        throw new IllegalStateException(
                                "ec_run() failed : the thread of the process data is not started");
                    }
                    while (!exit_) {
                        if (parcel_.isprocess.get() == SoemOsal.FALSE) {
                            if (!out_.isEmpty()) {
                                pointer = context_.slavelist[0].outputs.get();
                                synchronized (lockOut_) {
                                    for (Iterator<Map.Entry<Long, EcatData>> iterator = out_.entrySet()
                                            .iterator(); iterator.hasNext();) {
                                        entry = iterator.next();
                                        value = pointer.getByte(entry.getKey());
                                        pointer.putByte(entry.getKey(), SoemBits.merge((byte) value,
                                                entry.getValue().keepMask, entry.getValue().value));
                                        iterator.remove();
                                    }
                                }
                            }
                            parcel_.isprocess.set(SoemOsal.TRUE);
                        } else {
                            // nothing to do : wait the end of the current cycle of
                            // the native thread without burning the cpu
                            LockSupport.parkNanos(CYCLE_WAIT_NANOS);
                        }
                    }
                } catch (Exception ex) {
                    Console.writeStackTrace(SoemEcatThread.class.getName(), ex);
                    // the state of the service answers FAILED : the ecat is not running
                    throw ex;
                } finally {
                    runningTasks_.decrementAndGet();
                }
                return null;
            }
        };
    }

    private void writeLog(final String msg, boolean err) {
        Console.write(SoemEcatThread.class.getName(), msg, err);
    }
}
