/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.util.HashMap;
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

    private SoemLibrary soem_;
    private jnr.ffi.Runtime runtime_;

    private SoemEtherCAT.ecx_parcelt parcel_;
    private SoemEtherCATMain.ecx_contextt context_;
    private SoemEcatThread ecatThread_;

    private Pointer pIOmap_;

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
                soem_ = LibraryLoader.create(SoemLibrary.class).load(libraryName);
                runtime_ = jnr.ffi.Runtime.getRuntime(soem_);
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

    public Boolean init(int ioSize) {
        if (soem_ != null) {
            if ((parcel_ == null) && (context_ == null) && (pIOmap_ == null) && (ecatThread_ == null)) {
                pIOmap_ = Memory.allocateDirect(runtime_, ioSize, true);
                context_ = soem_.ec_malloc_context().register();
                parcel_ = soem_.ec_malloc_parcel(context_).register();
                ecatThread_ = new SoemEcatThread(soem_, parcel_, context_, ecatLock_);
                return true;
            }
            return false;
        }
        return null;
    }

    public Boolean po2so(int slave, long eep_man, long eep_id, Callable<Integer> func) {
        if (soem_ != null) {
            if (ecatThread_ != null) {
                ecatThread_.po2so(slave, eep_man, eep_id, func);
                return true;
            }
            return false;
        }
        return null;
    }

    public Boolean start(String ifname, String ifname2, long cycletime, SoemPluginListener listener) {
        if (soem_ != null) {
            if (ecatThread_ != null) {
                ecatThread_.addSoemEcatListener(listener);
                if (ecatThread_.init(ifname, ifname2, pIOmap_)) {
                    parcel_.wkc.set(0);
                    parcel_.cycletime.set(cycletime);
                    parcel_.dorun.set(SoemOsal.TRUE);
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
        if (ecatThread_.getState() == Worker.State.READY) {
            ecatThread_.start();
        } else {
            ecatThread_.restart();
        }
    }

    public Boolean setNotifyCheck(boolean state) {
        if (ecatThread_ != null) {
            return ecatThread_.setNotifyCheck(state);
        }
        return null;
    }

    public Boolean close(SoemPluginListener listener) {
        if (soem_ != null) {
            // the port of SOEM is closed only when ecx_init() opened it
            boolean closePort = (ecatThread_ == null) || ecatThread_.isPortOpen();

            if ((ecatThread_ != null) && (parcel_ != null)) {
                // stop the cyclic process before to wait the end of the native calls
                parcel_.dorun.set(SoemOsal.FALSE);
            }
            if (ecatThread_ != null) {
                ecatThread_.exit();
                ecatThread_.cancel();
                ecatThread_.removeSoemEcatListener(listener);
                if (!ecatThread_.awaitTermination(ECAT_TERMINATION_WAIT_MILLIS)) {
                    // the native calls of the java tasks are still running : keep the native
                    // memory to avoid a crash
                    Console.write(Soem.class.getName(),
                            "the ecat tasks are still running : the native memory is not freed", true);
                    return false;
                }
                // the thread of the process data is a thread of the shared library and not a
                // java task : it has to be out of ecatthread() before the parcel is freed
                if (!waitEcatThread()) {
                    Console.write(Soem.class.getName(),
                            "the ecat thread is still running : the native memory is not freed", true);
                    return false;
                }
                // every native call is over : the slaves can be left in INIT state
                ecatThread_.shutdown();
                ecatThread_ = null;
            }
            if (parcel_ != null) {
                parcel_.dorun.set(SoemOsal.FALSE);
                soem_.ec_free_parcel(parcel_);
                parcel_ = null;
            }
            if (context_ != null) {
                if (closePort) {
                    soem_.ecx_close(context_);
                }
                soem_.ec_free_context(context_);
                context_ = null;
            }
            if (pIOmap_ != null) {
                pIOmap_ = null;
            }
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
    private boolean waitEcatThread() {
        try {
            int ret = soem_.ec_wait_thread(parcel_, ECAT_THREAD_JOIN_WAIT_MILLIS);

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
            TimeUnit.MILLISECONDS.sleep((parcel_.cycletime.get() / EC_NSEC_PER_MILLIS)
                    + (SoemEtherCATType.EC_TIMEOUTRET / 1000) + ECAT_CYCLE_WAIT_MARGIN_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    public Integer slavecount() {
        if (context_ != null) {
            return context_.slavecount.get();
        }
        return null;
    }

    /**
     * check the slave index against the slave list of the context
     *
     * @param slave
     * @return
     */
    private boolean checkSlave(int slave) {
        return (context_ != null) && (slave >= 0) && (slave <= context_.slavecount.get())
                && (slave < context_.slavelist.length);
    }

    public Integer state(int slave) {
        if (checkSlave(slave)) {
            return context_.slavelist[slave].state.get();
        }
        return null;
    }

    public Boolean islost(int slave) {
        if (checkSlave(slave)) {
            return (context_.slavelist[slave].islost.get() == SoemOsal.TRUE);
        }
        return null;
    }

    public Boolean docheckstate() {
        if (context_ != null) {
            return (context_.grouplist[0].docheckstate.get() == SoemOsal.TRUE);
        }
        return null;
    }

    public Byte[] sdoRead(int slave, int index, int subIndex, int byteSize) {
        if (checkSlave(slave) && (byteSize > 0)) {
            Pointer psize = Memory.allocate(runtime_, Integer.BYTES);
            Pointer p = Memory.allocate(runtime_, byteSize);
            int wkc;

            psize.putInt(0, byteSize);
            // the mailbox of the slave is shared with the monitor task
            synchronized (ecatLock_) {
                wkc = soem_.ecx_SDOread(context_, slave, index, subIndex, SoemOsal.FALSE, psize, p,
                        SoemEtherCATType.EC_TIMEOUTRXM);
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
        if (checkSlave(slave) && (value != null)) {
            Pointer p = Memory.allocate(runtime_, value.length);

            p.put(0, value, 0, value.length);
            // the mailbox of the slave is shared with the monitor task
            synchronized (ecatLock_) {
                return soem_.ecx_SDOwrite(context_, slave, index, subIndex, SoemOsal.FALSE, value.length, p,
                        SoemEtherCATType.EC_TIMEOUTRXM);
            }
        }
        return null;
    }

    public Long in(int slave, long bitsOffset, long bitsMask) {
        if (checkSlave(slave)) {
            Pointer inputs = context_.slavelist[slave].inputs.get();
            long Ibits = context_.slavelist[slave].Ibits.get();

            if ((inputs != null) && (bitsOffset >= 0) && (bitsOffset < Ibits)) {
                long bits = Ibits - bitsOffset;

                if ((bitsMask > 0) && (bits > 0)) {
                    if (bits < SoemBits.MAX_BITS) {
                        bitsMask &= (1L << bits) - 1;
                    }
                    return SoemBits.read(inputs, bitsOffset / 8,
                            context_.slavelist[slave].Istartbit.get() + ((int) (bitsOffset % 8)), bitsMask,
                            SoemBits.mapBytes(context_.slavelist[slave].Istartbit.get(), Ibits));
                }
            }
        }
        return null;
    }

    public Long out(int slave, long bitsOffset, long bitsMask) {
        if (checkSlave(slave)) {
            Pointer outputs = context_.slavelist[slave].outputs.get();
            long Obits = context_.slavelist[slave].Obits.get();

            if ((outputs != null) && (bitsOffset >= 0) && (bitsOffset < Obits)) {
                long bits = Obits - bitsOffset;

                if ((bitsMask > 0) && (bits > 0)) {
                    if (bits < SoemBits.MAX_BITS) {
                        bitsMask &= (1L << bits) - 1;
                    }
                    return SoemBits.read(outputs, bitsOffset / 8,
                            context_.slavelist[slave].Ostartbit.get() + ((int) (bitsOffset % 8)), bitsMask,
                            SoemBits.mapBytes(context_.slavelist[slave].Ostartbit.get(), Obits));
                }
            }
        }
        return null;
    }

    public Long out(int slave, long bitsOffset, long bitsMask, long value) {
        if (checkSlave(slave) && (ecatThread_ != null)) {
            if ((bitsOffset >= 0) && (bitsOffset < context_.slavelist[slave].Obits.get())) {
                return ecatThread_.out(slave, bitsOffset, bitsMask, value);
            }
        }
        return null;
    }

    public String find_adapters() {
        if (soem_ != null) {
            Map<String, String> adapters = new HashMap<>();

            try {
                SoemEtherCATMain.ec_adaptert findAdapters = soem_.ec_find_adapters();
                SoemEtherCATMain.ec_adaptert findAdapter = findAdapters;
                while (findAdapter != null) {
                    adapters.put(findAdapter.desc.get(), findAdapter.name.get());
                    findAdapter = findAdapter.getNext(runtime_);
                }
                soem_.ec_free_adapters(findAdapters);
                return gson_.toJson(adapters);
            } catch (IllegalArgumentException | StackOverflowError | ClassCastException ex) {
                Console.writeStackTrace(Soem.class.getName(), ex);
            }
        }
        return null;
    }

    public String slaveinfo(boolean printSDO, boolean printMAP) {
        if (soem_ != null) {
            // the mailboxes and the state of the slaves are read : the monitor
            // task and the sdo calls of the java side use them at the same time
            synchronized (ecatLock_) {
                SoemSlaveInfo soemSlaveInfo = new SoemSlaveInfo(soem_, runtime_, context_, pIOmap_);
                return gson_.toJson(soemSlaveInfo.info(printSDO, printMAP));
            }
        }
        return null;
    }
}
