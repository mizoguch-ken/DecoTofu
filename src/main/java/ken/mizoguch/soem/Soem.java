/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import ken.mizoguch.console.Console;
import ken.mizoguch.decotofu.JavaLibrary;

/**
 *
 * @author mizoguch-ken
 */
public class Soem {

    private volatile SoemLibrary soem_;
    private volatile jnr.ffi.Runtime runtime_;

    private volatile SoemEtherCAT.ecx_parcelt parcel_;
    private volatile SoemEtherCATMain.ecx_contextt context_;
    private volatile SoemEcatThread ecatThread_;

    private volatile Pointer pIOmap_;

    private final Gson gson_ = new Gson();

    /**
     * time to wait the end of the java tasks of the ecat thread at the close
     */
    private static final long ECAT_TERMINATION_WAIT_MILLIS = 3000;

    /**
     * time to wait the end of the native ecat thread at the close
     */
    private static final int ECAT_THREAD_JOIN_WAIT_MILLIS = 2000;

    /**
     * margin added to one cycle when the platform can not join the native ecat
     * thread (old shared library without ec_wait_thread)
     */
    private static final long ECAT_CYCLE_WAIT_MARGIN_MILLIS = 50;

    /**
     * nanoseconds in one millisecond
     */
    private static final long EC_NSEC_PER_MILLIS = 1000000;

    /**
     * upper limit of the adapters walked by find_adapters() : a linked list
     * broken in a circle never comes back
     */
    private static final int ADAPTERS_MAX = 1024;

    /**
     * lock of the native calls of the context of SOEM. The monitor task, the
     * leave of the ecat thread and the calls of the java side (sdo read, sdo
     * write and slaveinfo) never use the context at the same time. The cyclic
     * thread of the process data is a native thread that never takes this lock,
     * so the lock only serializes the java calls.
     */
    private final Object ecatLock_ = new Object();

    /**
     *
     */
    public Soem() {
        soem_ = null;
        runtime_ = null;
        parcel_ = null;
        context_ = null;
        ecatThread_ = null;
        pIOmap_ = null;
    }

    Soem(SoemLibrary soem, jnr.ffi.Runtime runtime) {
        soem_ = soem;
        runtime_ = runtime;
        parcel_ = null;
        context_ = null;
        ecatThread_ = null;
        pIOmap_ = null;
    }

    Soem(SoemLibrary soem, jnr.ffi.Runtime runtime, SoemEtherCATMain.ecx_contextt context,
            SoemEtherCAT.ecx_parcelt parcel, SoemEcatThread ecatThread) {
        soem_ = soem;
        runtime_ = runtime;
        context_ = context;
        parcel_ = parcel;
        ecatThread_ = ecatThread;
        pIOmap_ = null;
    }

    public Boolean load(Path libraryPath) {
        if ((soem_ == null) && (runtime_ == null)) {
            try {
                String libraryName = JavaLibrary.removeFileExtension(libraryPath.getFileName());

                if (JavaLibrary.isLinux() || JavaLibrary.isMac()) {
                    // the loader of jnr-ffi adds the "lib" prefix of the file by
                    // himself : only the name of the library is needed
                    libraryName = JavaLibrary.stripLibraryPrefix(libraryName);
                }

                Path parent = libraryPath.getParent();
                if (parent != null) {
                    // only the search path is registered here : jnr-ffi loads the
                    // library. JavaLibrary answers false when the name of the
                    // library is already registered, that is not a failure
                    JavaLibrary.addLibraryPath(parent, libraryName, false);
                }
                SoemLibrary soem = LibraryLoader.create(SoemLibrary.class).load(libraryName);
                jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getRuntime(soem);
                if (!verifyNativeAbi(soem, runtime)) {
                    return null;
                }
                soem_ = soem;
                runtime_ = runtime;
                return true;
            } catch (UnsatisfiedLinkError ex) {
                Console.write(Soem.class.getName(), "the library can not be loaded : " + libraryPath, true);
                soem_ = null;
                runtime_ = null;
                return null;
            } catch (Exception ex) {
                Console.writeStackTrace(Soem.class.getName(), ex);
                soem_ = null;
                runtime_ = null;
                return null;
            }
        }
        return false;
    }

    /**
     * verify that the native library constants and struct byte sizes match the
     * JNR-FFI definitions before any context or parcel is allocated. Older
     * DecoTofu-SOEM builds that do not export introspection symbols are tolerated.
     */
    static boolean verifyNativeAbi(SoemLibrary soem, jnr.ffi.Runtime runtime) {
        if ((soem == null) || (runtime == null)) {
            return false;
        }
        try {
            int maxSlave = soem.ec_max_slave();
            if (maxSlave != SoemEtherCATMain.EC_MAXSLAVE) {
                Console.write(Soem.class.getName(), "SOEM ABI mismatch : EC_MAXSLAVE expected ["
                        + SoemEtherCATMain.EC_MAXSLAVE + "] native [" + maxSlave + "]", true);
                return false;
            }
            int maxGroup = soem.ec_max_group();
            if (maxGroup != SoemEtherCATMain.EC_MAXGROUP) {
                Console.write(Soem.class.getName(), "SOEM ABI mismatch : EC_MAXGROUP expected ["
                        + SoemEtherCATMain.EC_MAXGROUP + "] native [" + maxGroup + "]", true);
                return false;
            }
            long expectedParcel = SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCAT.ecx_parcelt(runtime));
            long actualParcel = soem.ec_sizeof_parcel();
            if (actualParcel != expectedParcel) {
                Console.write(Soem.class.getName(), "SOEM ABI mismatch : sizeof(ecx_parcelt) expected ["
                        + expectedParcel + "] native [" + actualParcel + "]", true);
                return false;
            }
            long expectedSlave = SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCATMain.ec_slavet(runtime));
            long actualSlave = soem.ec_sizeof_slave();
            if (actualSlave != expectedSlave) {
                Console.write(Soem.class.getName(), "SOEM ABI mismatch : sizeof(ec_slavet) expected ["
                        + expectedSlave + "] native [" + actualSlave + "]", true);
                return false;
            }
            long expectedGroup = SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCATMain.ec_groupt(runtime));
            long actualGroup = soem.ec_sizeof_group();
            if (actualGroup != expectedGroup) {
                Console.write(Soem.class.getName(), "SOEM ABI mismatch : sizeof(ec_groupt) expected ["
                        + expectedGroup + "] native [" + actualGroup + "]", true);
                return false;
            }
            long expectedContext = SoemEtherCATMain.ecx_contextt.expectedByteSize(runtime);
            long actualContext = soem.ec_sizeof_context();
            if (actualContext != expectedContext) {
                Console.write(Soem.class.getName(), "SOEM ABI mismatch : sizeof(ecx_contextt) expected ["
                        + expectedContext + "] native [" + actualContext + "]", true);
                return false;
            }
        } catch (UnsatisfiedLinkError ex) {
            return true;
        }
        return true;
    }

    public boolean isLoaded() {
        return (soem_ != null) && (runtime_ != null);
    }

    public boolean isRunning() {
        SoemEcatThread ecatThread = ecatThread_;
        return (ecatThread != null) && ecatThread.isInitialized();
    }

    public String version() {
        SoemLibrary soem = soem_;
        if (soem != null) {
            try {
                return soem.ec_version();
            } catch (UnsatisfiedLinkError ex) {
                return null;
            }
        }
        return null;
    }

    public Boolean init(int ioSize) {
        if (soem_ != null) {
            if (ioSize <= 0) {
                return false;
            }
            if ((parcel_ == null) && (context_ == null) && (pIOmap_ == null) && (ecatThread_ == null)) {
                SoemEtherCATMain.ecx_contextt ctx = soem_.ec_malloc_context();
                if (ctx == null) {
                    return false;
                }
                ctx = ctx.register();
                SoemEtherCAT.ecx_parcelt parcel = soem_.ec_malloc_parcel(ctx);
                if (parcel == null) {
                    soem_.ec_free_context(ctx);
                    return false;
                }
                parcel = parcel.register();
                pIOmap_ = Memory.allocateDirect(runtime_, ioSize, true);
                context_ = ctx;
                parcel_ = parcel;
                ecatThread_ = new SoemEcatThread(soem_, parcel_, context_, ecatLock_);
                return true;
            }
            return false;
        }
        return null;
    }

    public Boolean po2so(int slave, long eep_man, long eep_id, Callable<Integer> func) {
        if (soem_ != null) {
            SoemEcatThread ecatThread = ecatThread_;
            if (ecatThread != null) {
                ecatThread.po2so(slave, eep_man, eep_id, func);
                return true;
            }
            return false;
        }
        return null;
    }

    public Boolean start(String ifname, String ifname2, long cycletime, SoemPluginListener listener) {
        if (soem_ != null) {
            SoemEcatThread ecatThread = ecatThread_;
            SoemEtherCAT.ecx_parcelt parcel = parcel_;
            Pointer pIOmap = pIOmap_;
            if ((ecatThread != null) && (parcel != null) && (pIOmap != null)) {
                ecatThread.addSoemEcatListener(listener);
                if (ecatThread.init(ifname, ifname2, pIOmap)) {
                    parcel.wkc.set(0);
                    parcel.cycletime.set(cycletime);
                    parcel.dorun.set(SoemOsal.TRUE);
                    if (Platform.isFxApplicationThread()) {
                        runEcatThread();
                    } else {
                        Platform.runLater(() -> {
                            runEcatThread();
                        });
                    }
                    return true;
                }
            }
            return false;
        }
        return null;
    }

    private void runEcatThread() {
        SoemEcatThread ecatThread = ecatThread_;
        if (ecatThread == null) {
            return;
        }
        if (ecatThread.getState() == Worker.State.READY) {
            ecatThread.start();
        } else {
            ecatThread.restart();
        }
    }

    public Boolean setNotifyCheck(boolean state) {
        SoemEcatThread ecatThread = ecatThread_;
        if (ecatThread != null) {
            return ecatThread.setNotifyCheck(state);
        }
        return null;
    }

    public Boolean close(SoemPluginListener listener) {
        if (soem_ != null) {
            SoemEcatThread ecatThread = ecatThread_;
            SoemEtherCAT.ecx_parcelt parcel = parcel_;
            SoemEtherCATMain.ecx_contextt context = context_;
            // the port of SOEM is closed only when ecx_init() opened it
            boolean closePort = (ecatThread == null) || ecatThread.isPortOpen();

            if ((ecatThread != null) && (parcel != null)) {
                // stop the cyclic process before to wait the end of the native calls
                parcel.dorun.set(SoemOsal.FALSE);
            }
            if (ecatThread != null) {
                ecatThread.exit();
                if (Platform.isFxApplicationThread()) {
                    ecatThread.cancel();
                } else {
                    try {
                        Platform.runLater(ecatThread::cancel);
                    } catch (IllegalStateException ex) {
                    }
                }
                ecatThread.removeSoemEcatListener(listener);
                if (!ecatThread.awaitTermination(ECAT_TERMINATION_WAIT_MILLIS)) {
                    // the native calls of the java tasks are still running : keep the native
                    // memory to avoid a crash
                    Console.write(Soem.class.getName(),
                            "the ecat tasks are still running : the native memory is not freed", true);
                    return false;
                }
                // the thread of the process data is a thread of the shared library and not a
                // java task : it has to be out of ecatthread() before the parcel is freed
                if (!waitEcatThread(soem_, parcel)) {
                    Console.write(Soem.class.getName(),
                            "the ecat thread is still running : the native memory is not freed", true);
                    return false;
                }
                // every native call is over : the slaves can be left in INIT state
                ecatThread.shutdown();
                ecatThread_ = null;
            }
            if (parcel != null) {
                parcel.dorun.set(SoemOsal.FALSE);
                soem_.ec_free_parcel(parcel);
                parcel_ = null;
            }
            if (context != null) {
                if (closePort) {
                    soem_.ecx_close(context);
                }
                soem_.ec_free_context(context);
                context_ = null;
            }
            pIOmap_ = null;
            return true;
        }
        return null;
    }

    /**
     * wait the end of the native thread of the process data started by ec_run().
     * dorun = FALSE only asks that thread to leave its loop : the parcel of the
     * thread and the context it uses have to live until the thread is really out
     * of ecatthread().
     *
     * @return true when the thread is known to be out of ecatthread() or when
     * one cycle was waited on a platform that can not join it
     */
    private boolean waitEcatThread(SoemLibrary soem, SoemEtherCAT.ecx_parcelt parcel) {
        if ((soem == null) || (parcel == null)) {
            return true;
        }
        try {
            int ret;
            try {
                ret = soem.ec_stop(parcel, ECAT_THREAD_JOIN_WAIT_MILLIS);
            } catch (UnsatisfiedLinkError ex) {
                ret = soem.ec_wait_thread(parcel, ECAT_THREAD_JOIN_WAIT_MILLIS);
            }

            if (ret > 0) {
                return true;
            }
            if (ret == 0) {
                return false;
            }
            // the shared library can not wait the native thread on this platform
            Console.write(Soem.class.getName(), "the ecat thread is not joinable : one cycle is waited", true);
        } catch (UnsatisfiedLinkError ex) {
            // the shared library has no ec_wait_thread() : the worst time of one
            // cycle is waited instead of the end of the thread
            Console.write(Soem.class.getName(), "ec_wait_thread() is not exported : one cycle is waited", true);
        }
        try {
            TimeUnit.MILLISECONDS.sleep((parcel.cycletime.get() / EC_NSEC_PER_MILLIS)
                    + (SoemEtherCATType.EC_TIMEOUTRET / 1000) + ECAT_CYCLE_WAIT_MARGIN_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    public Integer slavecount() {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (context != null) {
            return context.slavecount.get();
        }
        return null;
    }

    /**
     * check the slave index against the slave list of the context
     *
     * @param context
     * @param slave
     * @return
     */
    private static boolean checkSlave(SoemEtherCATMain.ecx_contextt context, int slave) {
        return (context != null) && (slave >= 0) && (slave <= context.slavecount.get())
                && (slave < context.slavelist.length);
    }

    public Integer state(int slave) {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (checkSlave(context, slave)) {
            return context.slavelist[slave].state.get();
        }
        return null;
    }

    public Boolean islost(int slave) {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (checkSlave(context, slave)) {
            return (context.slavelist[slave].islost.get() == SoemOsal.TRUE);
        }
        return null;
    }

    public Boolean docheckstate() {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (context != null) {
            return (context.grouplist[0].docheckstate.get() == SoemOsal.TRUE);
        }
        return null;
    }

    public Boolean setOverlappedMode(boolean enable) {
        if (soem_ == null) {
            return null;
        }
        SoemEtherCATMain.ecx_contextt context = context_;
        SoemEcatThread ecatThread = ecatThread_;
        if ((context != null) && ((ecatThread == null) || (!ecatThread.isPortOpen() && !ecatThread.isInitialized()))) {
            context.overlappedMode.set((short) (enable ? SoemOsal.TRUE : SoemOsal.FALSE));
            return true;
        }
        return false;
    }

    public Boolean isOverlappedMode() {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (context != null) {
            return (context.overlappedMode.get() == SoemOsal.TRUE);
        }
        return null;
    }

    public Boolean setPackedMode(boolean enable) {
        if (soem_ == null) {
            return null;
        }
        SoemEtherCATMain.ecx_contextt context = context_;
        SoemEcatThread ecatThread = ecatThread_;
        if ((context != null) && ((ecatThread == null) || (!ecatThread.isPortOpen() && !ecatThread.isInitialized()))) {
            context.packedMode.set((short) (enable ? SoemOsal.TRUE : SoemOsal.FALSE));
            return true;
        }
        return false;
    }

    public Boolean isPackedMode() {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (context != null) {
            return (context.packedMode.get() == SoemOsal.TRUE);
        }
        return null;
    }

    public Boolean enableCyclicMailbox(int slave) {
        SoemLibrary soem = soem_;
        if (soem == null) {
            return null;
        }
        SoemEtherCATMain.ecx_contextt context = context_;
        if ((slave >= 1) && checkSlave(context, slave)) {
            synchronized (ecatLock_) {
                return soem.ecx_slavembxcyclic(context, slave) > 0;
            }
        }
        return false;
    }

    public Boolean dcsync0(int slave, boolean act, long cyclTime, int cyclShift) {
        SoemLibrary soem = soem_;
        if (soem == null) {
            return null;
        }
        SoemEtherCATMain.ecx_contextt context = context_;
        if ((slave >= 1) && checkSlave(context, slave) && (cyclTime >= 0) && (cyclTime <= 0xFFFFFFFFL)) {
            synchronized (ecatLock_) {
                soem.ecx_dcsync0(context, slave, act ? SoemOsal.TRUE : SoemOsal.FALSE, cyclTime, cyclShift);
            }
            return true;
        }
        return false;
    }

    public Boolean dcsync01(int slave, boolean act, long cyclTime0, long cyclTime1, int cyclShift) {
        SoemLibrary soem = soem_;
        if (soem == null) {
            return null;
        }
        SoemEtherCATMain.ecx_contextt context = context_;
        if ((slave >= 1) && checkSlave(context, slave) && (cyclTime0 >= 0) && (cyclTime0 <= 0xFFFFFFFFL)
                && (cyclTime1 >= 0) && (cyclTime1 <= 0xFFFFFFFFL)) {
            synchronized (ecatLock_) {
                soem.ecx_dcsync01(context, slave, act ? SoemOsal.TRUE : SoemOsal.FALSE, cyclTime0, cyclTime1,
                        cyclShift);
            }
            return true;
        }
        return false;
    }

    public Integer wkc() {
        SoemEtherCAT.ecx_parcelt parcel = parcel_;
        if (parcel != null) {
            return parcel.wkc.get();
        }
        return null;
    }

    public Integer expectedWkc() {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (context != null) {
            return (context.grouplist[0].outputsWKC.get() * 2) + context.grouplist[0].inputsWKC.get();
        }
        return null;
    }

    public Long dcTime() {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (context != null) {
            return context.DCtime.get();
        }
        return null;
    }

    public Boolean isError() {
        SoemLibrary soem = soem_;
        SoemEtherCATMain.ecx_contextt context = context_;
        if ((soem != null) && (context != null)) {
            synchronized (ecatLock_) {
                return soem.ecx_iserror(context) == SoemOsal.TRUE;
            }
        }
        return null;
    }

    public String popError() {
        SoemLibrary soem = soem_;
        SoemEtherCATMain.ecx_contextt context = context_;
        if ((soem != null) && (context != null)) {
            synchronized (ecatLock_) {
                if (soem.ecx_iserror(context) == SoemOsal.TRUE) {
                    return soem.ecx_elist2string(context);
                }
            }
        }
        return null;
    }

    public Byte[] sdoRead(int slave, int index, int subIndex, int byteSize) {
        return sdoRead(slave, index, subIndex, false, byteSize);
    }

    public Byte[] sdoRead(int slave, int index, int subIndex, boolean completeAccess, int byteSize) {
        SoemLibrary soem = soem_;
        jnr.ffi.Runtime runtime = runtime_;
        SoemEtherCATMain.ecx_contextt context = context_;
        if ((soem != null) && (runtime != null) && checkSlave(context, slave) && (byteSize > 0)) {
            Pointer psize = Memory.allocate(runtime, Integer.BYTES);
            Pointer p = Memory.allocate(runtime, byteSize);
            int wkc;

            psize.putInt(0, byteSize);
            // the mailbox of the slave is shared with the monitor task
            synchronized (ecatLock_) {
                wkc = soem.ecx_SDOread(context, slave, index, subIndex,
                        completeAccess ? SoemOsal.TRUE : SoemOsal.FALSE, psize, p, SoemEtherCATType.EC_TIMEOUTRXM);
            }
            if (wkc > 0) {
                int size = psize.getInt(0);
                if ((size < 0) || (size > byteSize)) {
                    Console.write(Soem.class.getName(), "SDOread : slave[" + slave + "] index[0x"
                            + Integer.toHexString(index) + "] size[" + size + "] byteSize[" + byteSize + "]", true);
                    return null;
                }
                Byte[] result = new Byte[size];
                for (int i = 0; i < result.length; i++) {
                    result[i] = p.getByte(i);
                }
                return result;
            }
        }
        return null;
    }

    public Integer sdoWrite(int slave, int index, int subIndex, byte[] value) {
        return sdoWrite(slave, index, subIndex, false, value);
    }

    public Integer sdoWrite(int slave, int index, int subIndex, boolean completeAccess, byte[] value) {
        SoemLibrary soem = soem_;
        jnr.ffi.Runtime runtime = runtime_;
        SoemEtherCATMain.ecx_contextt context = context_;
        if ((soem != null) && (runtime != null) && checkSlave(context, slave) && (value != null)
                && (value.length > 0)) {
            Pointer p = Memory.allocate(runtime, value.length);

            p.put(0, value, 0, value.length);
            // the mailbox of the slave is shared with the monitor task
            synchronized (ecatLock_) {
                return soem.ecx_SDOwrite(context, slave, index, subIndex,
                        completeAccess ? SoemOsal.TRUE : SoemOsal.FALSE, value.length, p,
                        SoemEtherCATType.EC_TIMEOUTRXM);
            }
        }
        return null;
    }

    public Long in(int slave, long bitsOffset, long bitsMask) {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (checkSlave(context, slave)) {
            SoemEtherCATMain.ec_slavet sl = context.slavelist[slave];

            return readBits(bitsOffset, bitsMask, sl.inputs.get(), sl.Istartbit.get(), sl.Ibits.get());
        }
        return null;
    }

    public Long out(int slave, long bitsOffset, long bitsMask) {
        SoemEtherCATMain.ecx_contextt context = context_;
        if (checkSlave(context, slave)) {
            SoemEtherCATMain.ec_slavet sl = context.slavelist[slave];

            return readBits(bitsOffset, bitsMask, sl.outputs.get(), sl.Ostartbit.get(), sl.Obits.get());
        }
        return null;
    }

    /**
     * read the variable of a slave in the map of the inputs or of the outputs :
     * the bits of the neighbours of the slave are never read
     *
     * @param bitsOffset offset of the variable in the bits of the slave
     * @param bitsMask bit mask of the variable (non-zero, all 64 bits supported)
     * @param map map of the slave, null when the slave has none
     * @param mapStartbit bit offset of the map of the slave in the first byte of
     * the map of the group
     * @param mapBits number of the bits of the map of the slave
     * @return the value of the variable, null when it is not in the map
     */
    static Long readBits(long bitsOffset, long bitsMask, Pointer map, int mapStartbit, long mapBits) {
        if ((map == null) || (bitsMask == 0) || (bitsOffset < 0) || (bitsOffset >= mapBits)) {
            return null;
        }
        long bits = mapBits - bitsOffset;

        if (bits < SoemBits.MAX_BITS) {
            bitsMask &= (1L << bits) - 1;
        }
        if (bitsMask == 0) {
            return 0L;
        }
        return SoemBits.read(map, bitsOffset / 8, mapStartbit + ((int) (bitsOffset % 8)), bitsMask,
                SoemBits.mapBytes(mapStartbit, mapBits));
    }

    public Long out(int slave, long bitsOffset, long bitsMask, long value) {
        SoemEtherCATMain.ecx_contextt context = context_;
        SoemEcatThread ecatThread = ecatThread_;
        if (checkSlave(context, slave) && (ecatThread != null)) {
            if ((bitsOffset >= 0) && (bitsOffset < context.slavelist[slave].Obits.get())) {
                return ecatThread.out(slave, bitsOffset, bitsMask, value);
            }
        }
        return null;
    }

    /**
     * the network adapters of the machine that SOEM can open
     *
     * @return a JSON object {"description" : "adapter name", ...} in the order
     * the machine reports them, "{}" when the machine has no adapter for SOEM,
     * null when the shared library of SOEM is not loaded or the call failed
     */
    public String find_adapters() {
        SoemLibrary soem = soem_;
        jnr.ffi.Runtime runtime = runtime_;
        if ((soem != null) && (runtime != null)) {
            // the order of the machine is kept : the description is only a label
            // for the user, the name behind it is what SOEM opens
            Map<String, String> adapters = new LinkedHashMap<>();
            Pointer list = null;

            try {
                list = soem.ec_adapters();
                if (list != null) {
                    SoemEtherCATMain.ec_adaptert adapter = new SoemEtherCATMain.ec_adaptert(runtime);

                    adapter.useMemory(list);
                    for (int i = 0; (adapter != null) && (i < ADAPTERS_MAX); i++) {
                        String name = adapter.name.get();

                        if ((name != null) && !name.isEmpty()) {
                            String desc = adapter.desc.get();

                            adapters.put((desc == null) ? name : desc, name);
                        }
                        adapter = adapter.getNext(runtime);
                    }
                }
                if (adapters.isEmpty()) {
                    // an empty answer is a machine without adapter or a shared
                    // library of an other build : the user has to know which one
                    Console.write(Soem.class.getName(), "find_adapters : no adapter found", true);
                }
                return gson_.toJson(adapters);
            } catch (IllegalArgumentException | ClassCastException | StackOverflowError | UnsatisfiedLinkError ex) {
                // UnsatisfiedLinkError : the shared library is from a build that
                // does not export ec_adapters() yet
                Console.writeStackTrace(Soem.class.getName(), ex);
            } finally {
                if (list != null) {
                    soem.ec_free_adapters_list(list);
                }
            }
        }
        return null;
    }

    public String slaveinfo(boolean printSDO, boolean printMAP) {
        SoemLibrary soem = soem_;
        jnr.ffi.Runtime runtime = runtime_;
        SoemEtherCATMain.ecx_contextt context = context_;
        Pointer pIOmap = pIOmap_;
        if ((soem != null) && (runtime != null)) {
            // the mailboxes and the state of the slaves are read : the monitor
            // task and the sdo calls of the java side use them at the same time
            synchronized (ecatLock_) {
                SoemSlaveInfo soemSlaveInfo = new SoemSlaveInfo(soem, runtime, context, pIOmap);
                return gson_.toJson(soemSlaveInfo.info(printSDO, printMAP));
            }
        }
        return null;
    }
}
