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
                if (JavaLibrary.isWindows()) {
                } else if (JavaLibrary.isLinux()) {
                    libraryName = libraryName.substring(libraryName.indexOf("lib") + 3);
                } else if (JavaLibrary.isMac()) {
                    libraryName = libraryName.substring(libraryName.indexOf("lib") + 3);
                } else {
                }

                if (JavaLibrary.addLibraryPath(libraryPath.getParent(), libraryName, false)) {
                    soem_ = LibraryLoader.create(SoemLibrary.class).load(libraryName);
                    runtime_ = jnr.ffi.Runtime.getRuntime(soem_);
                    return true;
                }
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
                ecatThread_ = new SoemEcatThread(soem_, parcel_, context_);
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
            if (ecatThread_ != null) {
                ecatThread_.exit();
                ecatThread_.cancel();
                ecatThread_.removeSoemEcatListener(listener);
                ecatThread_ = null;
            }
            if (parcel_ != null) {
                parcel_.dorun.set(SoemOsal.FALSE);
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException ex) {
                }
                soem_.ec_free_parcel(parcel_);
                parcel_ = null;
            }
            if (context_ != null) {
                soem_.ecx_close(context_);
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

    public Integer slavecount() {
        if (context_ != null) {
            return context_.slavecount.get();
        }
        return null;
    }

    public Integer state(int slave) {
        if (context_ != null) {
            return context_.slavelist[slave].state.get();
        }
        return null;
    }

    public Boolean islost(int slave) {
        if (context_ != null) {
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
        if (context_ != null) {
            Pointer psize = Memory.allocate(runtime_, Integer.BYTES);
            Pointer p = Memory.allocate(runtime_, byteSize);

            psize.putInt(0, byteSize);
            if (soem_.ecx_SDOread(context_, slave, index, subIndex, SoemOsal.FALSE, psize, p,
                    SoemEtherCATType.EC_TIMEOUTRXM) > 0) {
                Byte[] result = new Byte[psize.getInt(0)];
                for (int i = 0; i < result.length; i++) {
                    result[i] = p.getByte(i);
                }
                return result;
            }
        }
        return null;
    }

    public Integer sdoWrite(int slave, int index, int subIndex, byte[] value) {
        if (context_ != null) {
            Pointer p = Memory.allocate(runtime_, value.length);

            p.put(0, value, 0, value.length);
            return soem_.ecx_SDOwrite(context_, slave, index, subIndex, SoemOsal.FALSE, value.length, p,
                    SoemEtherCATType.EC_TIMEOUTRXM);
        }
        return null;
    }

    public Long in(int slave, long bitsOffset, long bitsMask) {
        if (context_ != null) {
            if ((bitsOffset >= 0) && (bitsOffset < context_.slavelist[slave].Ibits.get())) {
                if (context_.slavelist[slave].inputs.get() != null) {
                    long bits = (context_.slavelist[slave].Ibits.get() - bitsOffset);
                    if ((bitsMask > 0) && (bits > 0)) {
                        if (bits < 64) {
                            bitsMask &= (1 << bits) - 1;
                        }

                        if (bitsMask < 0xff) {
                            return ((context_.slavelist[slave].inputs.get().getByte(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffff) {
                            return ((context_.slavelist[slave].inputs.get().getShort(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffff) {
                            return ((context_.slavelist[slave].inputs.get().getInt(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffff) {
                            return ((context_.slavelist[slave].inputs.get().getInt(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffffffL) {
                            return ((context_.slavelist[slave].inputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffffffffL) {
                            return ((context_.slavelist[slave].inputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffffffffffL) {
                            return ((context_.slavelist[slave].inputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else {
                            return ((context_.slavelist[slave].inputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Istartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        }
                    }
                }
            }
        }
        return null;
    }

    public Long out(int slave, long bitsOffset, long bitsMask) {
        if (context_ != null) {
            if ((bitsOffset >= 0) && (bitsOffset < context_.slavelist[slave].Obits.get())) {
                if (context_.slavelist[slave].outputs.get() != null) {
                    long bits = (context_.slavelist[slave].Obits.get() - bitsOffset);
                    if ((bitsMask > 0) && (bits > 0)) {
                        if (bits < 64) {
                            bitsMask &= (1 << bits) - 1;
                        }

                        if (bitsMask < 0xff) {
                            return ((context_.slavelist[slave].outputs.get().getByte(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffff) {
                            return ((context_.slavelist[slave].outputs.get().getShort(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffff) {
                            return ((context_.slavelist[slave].outputs.get().getInt(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffff) {
                            return ((context_.slavelist[slave].outputs.get().getInt(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffffffL) {
                            return ((context_.slavelist[slave].outputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffffffffL) {
                            return ((context_.slavelist[slave].outputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else if (bitsMask < 0xffffffffffffffL) {
                            return ((context_.slavelist[slave].outputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        } else {
                            return ((context_.slavelist[slave].outputs.get().getLong(
                                    bitsOffset / 8) >> (context_.slavelist[slave].Ostartbit.get() + (bitsOffset % 8)))
                                    & bitsMask);
                        }
                    }
                }
            }
        }
        return null;
    }

    public Long out(int slave, long bitsOffset, long bitsMask, long value) {
        if ((context_ != null) && (ecatThread_ != null)) {
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
            SoemSlaveInfo soemSlaveInfo = new SoemSlaveInfo(soem_, runtime_, context_, pIOmap_);
            return gson_.toJson(soemSlaveInfo.info(printSDO, printMAP));
        }
        return null;
    }
}
