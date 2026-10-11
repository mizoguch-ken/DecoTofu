/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * the io of the ladders : the addresses of the global and of the local io, the comments of the
 * addresses and the io written by the javascript of the script blocks.
 *
 * the maps are the ones used by the cycle of the ladder, so they are never replaced : the editor
 * and the cycle share the live maps, and the monitor of the editor is used to write a value
 *
 * @author mizoguch-ken
 */
class LadderIoStore {

    private final CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap_;
    private final CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap_;
    private final CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap_;
    /**
     * monitor of the io : the same object as the one used by the editor of the ladders and by
     * the cycle of a ladder
     */
    private final Object lock_;

    /**
     *
     * @param lock monitor shared with the editor and the cycle of the ladder
     */
    LadderIoStore(Object lock) {
        ioMap_ = new CopyOnWriteArrayList<>();
        commentMap_ = new CopyOnWriteArrayList<>();
        scriptIoMap_ = new CopyOnWriteArrayList<>();
        lock_ = lock;
    }

    /**
     *
     * @return the io of the ladders, the first row holds the global addresses
     */
    CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> getIoMap() {
        return ioMap_;
    }

    /**
     *
     * @return the comment of the addresses of the ladders
     */
    CopyOnWriteArrayList<ConcurrentHashMap<String, String>> getCommentMap() {
        return commentMap_;
    }

    /**
     *
     * @return the io written by the javascript of the script blocks of the ladders
     */
    CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> getScriptIoMap() {
        return scriptIoMap_;
    }

    /**
     *
     * @return true when no address is registered
     */
    boolean isEmpty() {
        return ioMap_.isEmpty();
    }

    /**
     * add the row of the global addresses at the begin of every map
     */
    void addGlobalRow() {
        ioMap_.add(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());
        commentMap_.add(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());
        scriptIoMap_.add(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());
    }

    /**
     * remove every address of every map : the cycle of the ladder has to be stopped before
     */
    void clear() {
        ioMap_.clear();
        commentMap_.clear();
        scriptIoMap_.clear();
    }

    /**
     * register an address of a ladder when it is not registered yet
     *
     * @param idx
     * @param address
     * @return true when the address has just been registered
     */
    boolean createIo(int idx, String address) {
        if (!ioMap_.get(idx).containsKey(address)) {
            ioMap_.get(idx).put(address, new LadderIo(address));
            return true;
        }
        return false;
    }

    /**
     *
     * @param idx
     * @param address
     * @return the io of the address, null when the address is not registered
     */
    LadderIo io(int idx, String address) {
        return ioMap_.get(idx).get(address);
    }

    /**
     * io of a global address : an address can be removed from the io map while the cycle is
     * running, so a null answer only skips the io of the cycle
     *
     * @param address
     * @return the io of the address, null when the address is not registered
     */
    LadderIo globalIo(String address) {
        ConcurrentHashMap<String, LadderIo> ioMap;

        if ((address == null) || ioMap_.isEmpty()) {
            return null;
        }
        ioMap = ioMap_.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX);
        if (ioMap == null) {
            return null;
        }
        return ioMap.get(address);
    }

    /**
     * value of a global address : a local address is never answered without the ladder which
     * holds it
     *
     * @param address
     * @return the value, null when the address is local or not registered
     */
    Double value(String address) {
        if (!address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
            return value(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, address);
        }
        return null;
    }

    /**
     *
     * @param idx
     * @param address
     * @return the value, null when the address is not registered
     */
    Double value(int idx, String address) {
        if (ioMap_.get(idx).containsKey(address)) {
            return ioMap_.get(idx).get(address).getValue();
        }
        return null;
    }

    /**
     *
     * @param idx
     * @param address
     * @return true when the address has a comment
     */
    boolean hasComment(int idx, String address) {
        return commentMap_.get(idx).containsKey(address);
    }

    /**
     *
     * @param idx
     * @param address
     * @return the comment of the address, null when the address has no comment
     */
    String comment(int idx, String address) {
        return commentMap_.get(idx).get(address);
    }

    /**
     * value written during a cycle of the ladder by the javascript of a script block : the io of
     * the ladder gets the value, and the io of the script is consumed by the next cycle
     *
     * @param idx
     * @param address
     * @param value
     */
    void setDirect(int idx, String address, double value) {
        synchronized (lock_) {
            ioMap_.get(idx).get(address).setValue(value);
            setScriptIo(idx, address, value, true);
        }
    }

    /**
     * value written outside a cycle of the ladder by the javascript of a script block : the value
     * is kept for the next cycle of the ladder
     *
     * @param idx
     * @param address
     * @param value
     */
    void setScript(int idx, String address, double value) {
        synchronized (lock_) {
            setScriptIo(idx, address, value, false);
        }
    }

    /**
     * the row of a ladder is added to the maps : a ladder is created at the index of its tab, and
     * the row of the global addresses keeps its place at the begin of the maps
     *
     * @param idx index of the ladder, from one
     */
    void addLadderRow(int idx) {
        addRows(ioMap_, commentMap_, scriptIoMap_, idx);
    }

    /**
     *
     * @param idx index of the ladder, from one
     */
    void removeLadderRow(int idx) {
        removeRows(ioMap_, commentMap_, scriptIoMap_, idx);
    }

    /**
     * the rows of two ladders are exchanged : the io of a ladder follows its ladder, so the maps
     * hold the row of a ladder at the index of the ladder
     *
     * @param idx index of a ladder, from one
     * @param otherIdx index of the other ladder
     */
    void swapLadderRows(int idx, int otherIdx) {
        swapRows(ioMap_, commentMap_, scriptIoMap_, idx, otherIdx);
    }

    /**
     * the maps of the editor are the maps of the cycle of a ladder, but the difference of two
     * ladders shows its own maps : the rows are changed in the maps which the caller gives, and the
     * io of the scripts is left out when the caller has no map of it
     *
     * @param ioMap io of the ladders, the first row holds the global addresses
     * @param commentMap comments of the addresses of the ladders
     * @param scriptIoMap io written by the javascript of the script blocks, null when the maps do
     *                    not belong to a cycle
     * @param idx index of the ladder, from one
     */
    static void addRows(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap, int idx) {
        ioMap.add(idx, new ConcurrentHashMap<>());
        commentMap.add(idx, new ConcurrentHashMap<>());
        if (scriptIoMap != null) {
            scriptIoMap.add(idx, new ConcurrentHashMap<>());
        }
    }

    /**
     *
     * @param ioMap
     * @param commentMap
     * @param scriptIoMap null when the maps do not belong to a cycle
     * @param idx index of the ladder, from one
     */
    static void removeRows(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap, int idx) {
        ioMap.remove(idx);
        commentMap.remove(idx);
        if (scriptIoMap != null) {
            scriptIoMap.remove(idx);
        }
    }

    /**
     *
     * @param ioMap
     * @param commentMap
     * @param scriptIoMap null when the maps do not belong to a cycle
     * @param idx index of a ladder, from one
     * @param otherIdx index of the other ladder
     */
    static void swapRows(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap, int idx, int otherIdx) {
        swapRow(ioMap, idx, otherIdx);
        swapRow(commentMap, idx, otherIdx);
        if (scriptIoMap != null) {
            swapRow(scriptIoMap, idx, otherIdx);
        }
    }

    /**
     * a map is never replaced : the editor and the cycle of a ladder hold the map they got from the
     * store, so a row is exchanged inside the map
     *
     * @param map a row of the maps of the ladders
     * @param idx
     * @param otherIdx
     */
    private static <T> void swapRow(CopyOnWriteArrayList<ConcurrentHashMap<String, T>> map, int idx, int otherIdx) {
        ConcurrentHashMap<String, T> row;

        row = map.get(idx);
        map.set(idx, map.get(otherIdx));
        map.set(otherIdx, row);
    }

    /**
     * the values which the javascript of the script blocks of the ladders has written : the io of a
     * ladder gets the value waited by a script of the ladder, the io of a script which is cycled is
     * consumed by the cycle, and the io of a script which is no longer registered is removed
     *
     * the cycle of the ladders calls it under the monitor of the editor, so the values of the io of
     * a ladder are changed with the state of the ladder
     */
    void applyScriptIo() {
        Iterator<Map.Entry<String, LadderIo>> iterator;
        Map.Entry<String, LadderIo> entry;
        ConcurrentHashMap<String, LadderIo> scriptIoMap;
        LadderIo io;
        int index;

        for (index = 0; index < scriptIoMap_.size(); index++) {
            scriptIoMap = scriptIoMap_.get(index);
            if (!scriptIoMap.isEmpty()) {
                for (iterator = scriptIoMap.entrySet().iterator(); iterator.hasNext();) {
                    entry = iterator.next();
                    io = ioMap_.get(index).get(entry.getKey());
                    if (io == null) {
                        // the io of the script does not exist any more
                        iterator.remove();
                    } else if (entry.getValue().isCycled()) {
                        io.checkEdge();
                        iterator.remove();
                    } else {
                        io.setValue(entry.getValue().getValue());
                        entry.getValue().setCycled(true);
                    }
                }
            }
        }
    }

    /**
     *
     * @param idx
     * @param address
     * @param value
     * @param isCycled true when the io of the script is consumed by the next cycle
     */
    private void setScriptIo(int idx, String address, double value, boolean isCycled) {
        scriptIoMap_.get(idx).putIfAbsent(address, new LadderIo(address));
        scriptIoMap_.get(idx).get(address).setValue(value);
        scriptIoMap_.get(idx).get(address).setCycled(isCycled);
    }
}
