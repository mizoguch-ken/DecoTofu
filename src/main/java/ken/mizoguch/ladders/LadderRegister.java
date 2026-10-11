/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import ken.mizoguch.soem.Soem;

/**
 * the registrations of the ladders : the web page which runs the javascript of the script blocks
 * and the inputs and the outputs of the slaves of a soem bus.
 *
 * an address registered on a slave is a global address : it is added to the io when it is new, and
 * the view of the io is asked to show it
 *
 * @author mizoguch-ken
 */
class LadderRegister {

    /**
     * the view of the io : it shows an address which has just been registered
     */
    interface View {

        /**
         *
         * @param address the global address which is new in the io
         */
        void addIoRow(String address);
    }

    private final LadderIoStore ioStore_;
    private final View view_;
    private final CopyOnWriteArrayList<LadderRegisterSoemIo> registerSoemIn_, registerSoemOut_;

    private WebEngine webEngine_;
    private Worker.State state_;
    private Soem soem_;

    /**
     *
     * @param ioStore the io of the ladders
     * @param view the view of the io
     */
    LadderRegister(LadderIoStore ioStore, View view) {
        ioStore_ = ioStore;
        view_ = view;
        registerSoemIn_ = new CopyOnWriteArrayList<>();
        registerSoemOut_ = new CopyOnWriteArrayList<>();
        webEngine_ = null;
        state_ = Worker.State.READY;
        soem_ = null;
    }

    /**
     *
     * @return the web page which runs the javascript of the scripts, null when none is registered
     */
    WebEngine getWebEngine() {
        return webEngine_;
    }

    /**
     *
     * @return the state of the registered web page
     */
    Worker.State getState() {
        return state_;
    }

    /**
     *
     * @return the soem bus used by the cycle, null when none is registered
     */
    Soem getSoem() {
        return soem_;
    }

    /**
     *
     * @return the inputs of the ladders registered on the slaves
     */
    CopyOnWriteArrayList<LadderRegisterSoemIo> getRegisterSoemIn() {
        return registerSoemIn_;
    }

    /**
     *
     * @return the outputs of the ladders registered on the slaves
     */
    CopyOnWriteArrayList<LadderRegisterSoemIo> getRegisterSoemOut() {
        return registerSoemOut_;
    }

    /**
     *
     * @param object a web page or a soem bus
     * @return true when the object is the registered one, null when the object is not given
     */
    Boolean isRegistered(Object object) {
        if (object != null) {
            if (object instanceof WebEngine) {
                if (webEngine_ != null) {
                    return webEngine_.equals(object);
                }
            }
            return false;
        }
        return null;
    }

    /**
     * the web page which runs the javascript of the script blocks of the ladders : the io get the
     * state of the page to answer the value of an address to the view of the io
     *
     * @param webEngine
     * @param state
     */
    void register(WebEngine webEngine, Worker.State state) {
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap;
        int idx;

        if ((webEngine != null) && (state != null)) {
            webEngine_ = webEngine;
            state_ = state;
            ioMap = ioStore_.getIoMap();
            for (idx = 0; idx < ioMap.size(); idx++) {
                ioMap.get(idx).entrySet().forEach((entry) -> {
                    entry.getValue().setWebEngineState(webEngine_, state_);
                });
            }
        }
    }

    /**
     * attach the io of the ladders to the web page which is actually registered
     */
    void registerWeb() {
        register(webEngine_, state_);
    }

    /**
     * register the input of a ladder on a slave of the soem bus, as the javascript of a page asks
     * it with the variable number of the arguments
     *
     * @param objects soem bus, address, slave, offset of bits, mask of bits
     * @return true when the address is registered, null when no argument is given
     */
    Boolean registerIn(Object... objects) {
        return registerSoem(registerSoemIn_, objects);
    }

    /**
     * register the output of a ladder on a slave of the soem bus, as the javascript of a page asks
     * it with the variable number of the arguments
     *
     * @param objects soem bus, address, slave, offset of bits, mask of bits
     * @return true when the address is registered, null when no argument is given
     */
    Boolean registerOut(Object... objects) {
        return registerSoem(registerSoemOut_, objects);
    }

    /**
     * the registration of a web page or of a soem bus is removed
     *
     * @param object a web page or a soem bus
     */
    void unregister(Object object) {
        if (object != null) {
            if (object instanceof WebEngine) {
                webEngine_ = null;
                state_ = Worker.State.READY;
            } else if (object instanceof Soem) {
                clearSoem();
                soem_ = null;
            }
        }
    }

    /**
     * the addresses registered on the slaves are removed : the bus is kept, because the cycle of
     * the ladder reads its io with the addresses of the ladder
     */
    void clearSoem() {
        registerSoemIn_.clear();
        registerSoemOut_.clear();
    }

    /**
     * register an address of a ladder on a slave of the soem bus : the bus of the first
     * registration clears the addresses registered before, and an address registered twice takes
     * the last slave and bits of the registration
     *
     * @param registers the inputs or the outputs registered on the slaves
     * @param objects soem bus, address, slave, offset of bits, mask of bits
     * @return true when the address is registered, null when no argument is given
     */
    private Boolean registerSoem(CopyOnWriteArrayList<LadderRegisterSoemIo> registers, Object... objects) {
        if (objects != null) {
            switch (objects.length) {
                case 5:
                    if ((objects[0] instanceof Soem) && (objects[1] instanceof String)
                            && (objects[2] instanceof Integer) && (objects[3] instanceof Long)
                            && (objects[4] instanceof Long)) {
                        if (soem_ == null) {
                            soem_ = (Soem) objects[0];
                            clearSoem();
                        }

                        String address = ((String) objects[1]).trim();
                        if (!address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX) && !address.contains(" ")) {
                            if (ioStore_.createIo(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, address)) {
                                view_.addIoRow(address);
                            }

                            int slave = (int) objects[2];
                            long bitsOffset = (long) objects[3];
                            long bitsMask = (long) objects[4];
                            for (int index = 0, size = registers.size(); index < size; index++) {
                                if (registers.get(index).getAddress().equals(address)) {
                                    if ((registers.get(index).getSlave() != slave)
                                            || (registers.get(index).getBitsOffset() != bitsOffset)
                                            || (registers.get(index).getBitsMask() != bitsMask)) {
                                        registers.remove(index);
                                        registers.add(new LadderRegisterSoemIo(address, slave, bitsOffset, bitsMask));
                                    }
                                    return true;
                                }
                            }
                            registers.add(new LadderRegisterSoemIo(address, slave, bitsOffset, bitsMask));
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }
        return null;
    }
}
