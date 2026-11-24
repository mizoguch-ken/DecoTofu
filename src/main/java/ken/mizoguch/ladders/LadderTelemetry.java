package ken.mizoguch.ladders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import ken.mizoguch.decotofu.I18n;

/**
 * Low-overhead telemetry, structural/state inspection, bounded trace buffering,
 * and safety-gated actuation support for DecoTofu ladders.
 *
 * <p>This class intentionally has no JavaFX dependencies so it can be exercised
 * in headless unit tests as well as live PLC cycle loops.
 *
 * @author mizoguch-ken
 */
class LadderTelemetry {

    private static final int DEFAULT_MAX_ENTRIES = 512;
    private static final int MIN_MAX_ENTRIES = 16;
    private static final int MAX_MAX_ENTRIES = 10000;
    private static final long MAX_MIN_INTERVAL_MS = 60000L;
    private static final Pattern PATTERN_CALLBACK = Pattern.compile(
            "^[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*\\z");
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private record Bounds(double min, double max) {
    }

    private record BufferedEvent(long seq, String json, String observation) {
    }

    private final Object lock_ = new Object();
    private final AtomicLong cycleCount_ = new AtomicLong(0L);
    private final Map<String, Double> lastValues_ = new HashMap<>();
    private final Map<String, Bounds> writableBounds_ = new TreeMap<>();
    private final Deque<BufferedEvent> traceBuffer_ = new ArrayDeque<>();

    private volatile boolean tracing_;
    private volatile Consumer<String> eventListener_;

    private int maxEntries_ = DEFAULT_MAX_ENTRIES;
    private long minIntervalNs_;
    private double epsilon_;
    private List<String> watchAddresses_;
    private long lastTraceNs_ = Long.MIN_VALUE;
    private long nextSeq_;
    private long drainCursorSeq_;
    private long lastActionSeq_;
    private long lastActionCycle_ = Long.MIN_VALUE;
    private String lastActionInferenceId_;

    static boolean isValidCallbackName(String name) {
        return (name != null) && PATTERN_CALLBACK.matcher(name).matches();
    }

    void setEventListener(Consumer<String> listener) {
        eventListener_ = listener;
    }

    void notifyListener(String eventJson) {
        if ((eventJson == null) || eventJson.isEmpty()) {
            return;
        }
        Consumer<String> listener = eventListener_;
        if (listener != null) {
            try {
                listener.accept(eventJson);
            } catch (RuntimeException ignored) {
            }
        }
    }

    boolean isTracing() {
        return tracing_;
    }

    long getCycleCount() {
        return cycleCount_.get();
    }

    /**
     * Cycle completion hook invoked from the ladder cycle under the editor monitor.
     * Returns immediately with zero allocations when tracing is disabled.
     */
    String onCycleComplete(LadderIoStore ioStore, long cycleTimeNs) {
        long cycle = cycleCount_.incrementAndGet();
        if (!tracing_ || (ioStore == null)) {
            return null;
        }
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap = ioStore.getIoMap();
        if (ioMap.isEmpty()) {
            return null;
        }
        ConcurrentHashMap<String, LadderIo> globalIo = ioMap.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX);
        if ((globalIo == null) || globalIo.isEmpty()) {
            return null;
        }
        CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap = ioStore.getCommentMap();
        ConcurrentHashMap<String, String> globalComments = (commentMap.size() > LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX)
                ? commentMap.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX)
                : null;

        synchronized (lock_) {
            if (!tracing_) {
                return null;
            }
            long nowNs = System.nanoTime();
            if ((minIntervalNs_ > 0L) && (lastTraceNs_ != Long.MIN_VALUE)
                    && ((nowNs - lastTraceNs_) < minIntervalNs_)) {
                return null;
            }

            TreeMap<String, JsonObject> changes = null;
            if ((watchAddresses_ != null) && !watchAddresses_.isEmpty()) {
                for (String addr : watchAddresses_) {
                    LadderIo io = globalIo.get(addr);
                    if (io != null) {
                        changes = checkAddressTransition(addr, io, globalComments, changes);
                    }
                }
            } else {
                for (Map.Entry<String, LadderIo> entry : globalIo.entrySet()) {
                    changes = checkAddressTransition(entry.getKey(), entry.getValue(),
                            globalComments, changes);
                }
            }

            if ((changes == null) || changes.isEmpty()) {
                return null;
            }
            lastTraceNs_ = nowNs;

            JsonObject changesObj = new JsonObject();
            for (Map.Entry<String, JsonObject> ch : changes.entrySet()) {
                changesObj.add(ch.getKey(), ch.getValue());
            }

            JsonObject ev = new JsonObject();
            long seq = ++nextSeq_;
            long nowMs = System.currentTimeMillis();
            ev.addProperty("seq", seq);
            ev.addProperty("cycle", cycle);
            ev.addProperty("timestampMs", nowMs);
            ev.addProperty("cycleTimeNs", Math.max(0L, cycleTimeNs));
            ev.addProperty("kind", "transition");
            if ((lastActionCycle_ != Long.MIN_VALUE) && ((cycle - lastActionCycle_) <= 3L)) {
                ev.addProperty("causedBySeq", lastActionSeq_);
                if (lastActionInferenceId_ != null) {
                    ev.addProperty("causedByInferenceId", lastActionInferenceId_);
                }
            }
            ev.add("changes", changesObj);
            return appendBufferedEventLocked(seq, ev);
        }
    }

    String onCycleError(String message, long cycleTimeNs) {
        long cycle = cycleCount_.get();
        String note = ((message == null) || message.isEmpty()) ? I18n.get("log.telemetry.cycleError") : message;
        synchronized (lock_) {
            long seq = ++nextSeq_;
            JsonObject ev = new JsonObject();
            ev.addProperty("seq", seq);
            ev.addProperty("cycle", cycle);
            ev.addProperty("timestampMs", System.currentTimeMillis());
            ev.addProperty("cycleTimeNs", Math.max(0L, cycleTimeNs));
            ev.addProperty("kind", "error");
            ev.addProperty("note", note);
            ev.addProperty("reward", -1.0d);
            if ((lastActionCycle_ != Long.MIN_VALUE) && ((cycle - lastActionCycle_) <= 3L)) {
                ev.addProperty("causedBySeq", lastActionSeq_);
                if (lastActionInferenceId_ != null) {
                    ev.addProperty("causedByInferenceId", lastActionInferenceId_);
                    ev.addProperty("inferenceId", lastActionInferenceId_);
                }
                lastActionSeq_ = 0L;
                lastActionCycle_ = Long.MIN_VALUE;
                lastActionInferenceId_ = null;
            }
            return appendBufferedEventLocked(seq, ev);
        }
    }

    String startTrace(String optionsJson) {
        int maxEntries = DEFAULT_MAX_ENTRIES;
        long minIntervalMs = 0L;
        double epsilon = 0.0d;
        List<String> addresses = null;
        boolean reset = false;

        if ((optionsJson != null) && !optionsJson.trim().isEmpty()) {
            JsonObject opts = parseObject(optionsJson, I18n.get("log.telemetry.label.traceOptions"));
            if (opts.has("maxEntries") && !opts.get("maxEntries").isJsonNull()) {
                maxEntries = readInt(opts.get("maxEntries"), "maxEntries",
                        MIN_MAX_ENTRIES, MAX_MAX_ENTRIES);
            }
            if (opts.has("minIntervalMs") && !opts.get("minIntervalMs").isJsonNull()) {
                minIntervalMs = readLong(opts.get("minIntervalMs"), "minIntervalMs",
                        0L, MAX_MIN_INTERVAL_MS);
            }
            if (opts.has("epsilon") && !opts.get("epsilon").isJsonNull()) {
                epsilon = readFiniteDouble(opts.get("epsilon"), "epsilon");
                if (epsilon < 0.0d) {
                    throw new IllegalArgumentException(I18n.get("log.telemetry.epsilonNegative"));
                }
            }
            if (opts.has("addresses") && !opts.get("addresses").isJsonNull()) {
                addresses = readAddressList(opts.get("addresses"), "addresses");
            }
            if (opts.has("reset") && !opts.get("reset").isJsonNull()) {
                reset = readBoolean(opts.get("reset"), "reset");
            }
        }

        synchronized (lock_) {
            if (reset) {
                traceBuffer_.clear();
                drainCursorSeq_ = nextSeq_;
            }
            lastValues_.clear();
            lastTraceNs_ = Long.MIN_VALUE;
            maxEntries_ = maxEntries;
            minIntervalNs_ = minIntervalMs * 1_000_000L;
            epsilon_ = epsilon;
            watchAddresses_ = (addresses == null) ? null : List.copyOf(addresses);
            while (traceBuffer_.size() > maxEntries_) {
                traceBuffer_.removeFirst();
            }
            tracing_ = true;

            JsonObject status = new JsonObject();
            status.addProperty("tracing", true);
            status.addProperty("maxEntries", maxEntries_);
            status.addProperty("minIntervalMs", minIntervalMs);
            status.addProperty("epsilon", epsilon_);
            status.addProperty("buffered", traceBuffer_.size());
            if (watchAddresses_ != null) {
                JsonArray arr = new JsonArray();
                for (String a : watchAddresses_) {
                    arr.add(a);
                }
                status.add("addresses", arr);
            }
            return GSON.toJson(status);
        }
    }

    void stopTrace() {
        tracing_ = false;
    }

    void clearTrace() {
        synchronized (lock_) {
            traceBuffer_.clear();
            lastValues_.clear();
            lastTraceNs_ = Long.MIN_VALUE;
            drainCursorSeq_ = nextSeq_;
            lastActionSeq_ = 0L;
            lastActionCycle_ = Long.MIN_VALUE;
            lastActionInferenceId_ = null;
        }
    }

    void resetCycleStats() {
        cycleCount_.set(0L);
        clearTrace();
    }

    String traceJson(int limit) {
        synchronized (lock_) {
            if (traceBuffer_.isEmpty()) {
                return "[]";
            }
            int cap = (limit <= 0) ? traceBuffer_.size() : limit;
            List<BufferedEvent> all = new ArrayList<>(traceBuffer_);
            int start = Math.max(0, all.size() - cap);
            JsonArray arr = new JsonArray();
            for (int i = start; i < all.size(); i++) {
                arr.add(JsonParser.parseString(all.get(i).json()));
            }
            return GSON.toJson(arr);
        }
    }

    String drainTraceJson(int limit) {
        List<BufferedEvent> drained = drainEvents(limit);
        if (drained.isEmpty()) {
            return "[]";
        }
        JsonArray arr = new JsonArray();
        for (BufferedEvent ev : drained) {
            arr.add(JsonParser.parseString(ev.json()));
        }
        return GSON.toJson(arr);
    }

    String drainObservationsJson(int limit) {
        List<BufferedEvent> drained = drainEvents(limit);
        if (drained.isEmpty()) {
            return "[]";
        }
        JsonArray arr = new JsonArray();
        for (BufferedEvent ev : drained) {
            arr.add(ev.observation());
        }
        return GSON.toJson(arr);
    }

    String snapshotJson(LadderIoStore ioStore, boolean running, long cycleTimeNs,
            long minCycleTimeNs, long maxCycleTimeNs, String filterJson) {
        Set<String> filter = parseFilterSet(filterJson);

        JsonObject root = new JsonObject();
        root.addProperty("running", running);
        root.addProperty("cycleCount", cycleCount_.get());
        root.addProperty("cycleTimeNs", Math.max(0L, cycleTimeNs));
        root.addProperty("minCycleTimeNs", Math.max(0L, minCycleTimeNs));
        root.addProperty("maxCycleTimeNs", Math.max(0L, maxCycleTimeNs));
        root.addProperty("timestampMs", System.currentTimeMillis());

        JsonArray ioArr = new JsonArray();
        if ((ioStore != null) && !ioStore.getIoMap().isEmpty()) {
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap = ioStore.getIoMap();
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap = ioStore.getCommentMap();
            ConcurrentHashMap<String, LadderIo> globalIo = ioMap.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX);
            ConcurrentHashMap<String, String> globalComments = (commentMap.size() > LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX)
                    ? commentMap.get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX)
                    : null;
            if (globalIo != null) {
                List<String> keys = new ArrayList<>(globalIo.keySet());
                keys.sort(Comparator.naturalOrder());
                for (String addr : keys) {
                    if ((filter != null) && !filter.contains(addr)) {
                        continue;
                    }
                    LadderIo io = globalIo.get(addr);
                    if (io == null) {
                        continue;
                    }
                    String comment = ((globalComments != null) && (globalComments.get(addr) != null))
                            ? globalComments.get(addr)
                            : io.getComment();
                    JsonObject item = new JsonObject();
                    item.addProperty("address", addr);
                    item.addProperty("comment", (comment != null) ? comment : "");
                    addFiniteNumber(item, "value", io.getValue());
                    item.addProperty("ld", io.isLd());
                    item.addProperty("rising", io.isLdRising());
                    item.addProperty("falling", io.isLdFalling());
                    ioArr.add(item);
                }
            }
        }
        root.add("io", ioArr);
        return GSON.toJson(root);
    }

    String addressesJson(LadderIoStore ioStore, boolean includeLocal) {
        JsonArray arr = new JsonArray();
        if (ioStore == null) {
            return "[]";
        }
        CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap = ioStore.getIoMap();
        CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap = ioStore.getCommentMap();
        int maxIdx = includeLocal ? ioMap.size() : Math.min(1, ioMap.size());
        for (int idx = 0; idx < maxIdx; idx++) {
            ConcurrentHashMap<String, LadderIo> row = ioMap.get(idx);
            if ((row == null) || row.isEmpty()) {
                continue;
            }
            ConcurrentHashMap<String, String> comments = (idx < commentMap.size()) ? commentMap.get(idx) : null;
            List<String> keys = new ArrayList<>(row.keySet());
            keys.sort(Comparator.naturalOrder());
            String scope = (idx == LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX) ? "global" : "local";
            for (String addr : keys) {
                LadderIo io = row.get(addr);
                if (io == null) {
                    continue;
                }
                String comment = ((comments != null) && (comments.get(addr) != null))
                        ? comments.get(addr)
                        : io.getComment();
                JsonObject item = new JsonObject();
                item.addProperty("scope", scope);
                item.addProperty("tabIndex", idx);
                item.addProperty("address", addr);
                item.addProperty("comment", (comment != null) ? comment : "");
                addFiniteNumber(item, "value", io.getValue());
                arr.add(item);
            }
        }
        return GSON.toJson(arr);
    }

    String structureJson(Ladder[] ladders, boolean includeScripts) {
        JsonObject root = new JsonObject();
        JsonArray tabsArr = new JsonArray();
        int count = 0;
        if (ladders != null) {
            for (Ladder ladder : ladders) {
                if (ladder == null) {
                    continue;
                }
                count++;
                JsonObject tabObj = new JsonObject();
                tabObj.addProperty("index", ladder.getIdx());
                tabObj.addProperty("name", (ladder.getName() != null) ? ladder.getName() : "");
                tabObj.addProperty("columns", ladder.getColumn());
                tabObj.addProperty("rows", ladder.getRow());

                List<LadderGrid> sortedGrids = new ArrayList<>();
                for (LadderGrid g : ladder.getLadderGrids()) {
                    if (g == null) {
                        continue;
                    }
                    LadderBlock blk = g.getBlock();
                    if ((blk == null)
                            || (blk == LadderBlock.EMPTY)
                            || (blk == LadderBlock.CONTENTS)
                            || (blk == LadderBlock.CONNECT_LINE)) {
                        continue;
                    }
                    sortedGrids.add(g);
                }
                sortedGrids.sort(Comparator.comparingInt(LadderGrid::getRowIndex)
                        .thenComparingInt(LadderGrid::getColumnIndex));

                JsonArray blocksArr = new JsonArray();
                for (LadderGrid g : sortedGrids) {
                    LadderBlock blk = g.getBlock();
                    JsonObject blkObj = new JsonObject();
                    blkObj.addProperty("col", g.getColumnIndex());
                    blkObj.addProperty("row", g.getRowIndex());
                    blkObj.addProperty("block", blk.name());
                    blkObj.addProperty("command", blk.toCommand());
                    String addr = g.getAddress();
                    if ((addr != null) && !addr.isEmpty()) {
                        blkObj.addProperty("address", addr);
                    }
                    String comment = g.getComment();
                    if ((comment != null) && !comment.isEmpty()) {
                        blkObj.addProperty("comment", comment);
                    }
                    addFiniteNumber(blkObj, "blockValue", g.getBlockValue());

                    LadderGrid.BlockFunction[] funcs = g.getBlockFunctions();
                    if (funcs != null) {
                        JsonArray funcsArr = new JsonArray();
                        for (LadderGrid.BlockFunction fn : funcs) {
                            if (fn == null) {
                                continue;
                            }
                            String fAddr = fn.getAddress();
                            if (((fAddr != null) && !fAddr.isEmpty()) || fn.isNumber()) {
                                JsonObject fnObj = new JsonObject();
                                if (fAddr != null) {
                                    fnObj.addProperty("address", fAddr);
                                }
                                addFiniteNumber(fnObj, "value", fn.getValue());
                                fnObj.addProperty("number", fn.isNumber());
                                funcsArr.add(fnObj);
                            }
                        }
                        if (!funcsArr.isEmpty()) {
                            blkObj.add("functions", funcsArr);
                        }
                    }

                    if (includeScripts) {
                        String script = g.getBlockScript();
                        if ((script != null) && !script.isEmpty()) {
                            blkObj.addProperty("script", script);
                        }
                    }
                    blocksArr.add(blkObj);
                }
                tabObj.add("blocks", blocksArr);
                tabsArr.add(tabObj);
            }
        }
        root.addProperty("tabs", count);
        root.add("ladders", tabsArr);
        return GSON.toJson(root);
    }

    String setWritableBounds(String boundsJson) {
        JsonObject obj = parseObject(boundsJson, I18n.get("log.telemetry.label.writableBounds"));
        Map<String, Bounds> parsed = new TreeMap<>();
        List<String> removals = new ArrayList<>();
        boolean clearAll = obj.isEmpty();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String addr = requireGlobalAddress(entry.getKey());
            JsonElement val = entry.getValue();
            if ((val == null) || val.isJsonNull()) {
                removals.add(addr);
                continue;
            }
            if (!val.isJsonObject()) {
                throw new IllegalArgumentException(I18n.get("log.telemetry.boundsNotObject", addr));
            }
            JsonObject bo = val.getAsJsonObject();
            if (bo.has("allow") && !bo.get("allow").isJsonNull() && !readBoolean(bo.get("allow"), "allow")) {
                removals.add(addr);
                continue;
            }
            if (!bo.has("min") || !bo.has("max")) {
                throw new IllegalArgumentException(I18n.get("log.telemetry.boundsRequireMinMax", addr));
            }
            double min = readFiniteDouble(bo.get("min"), "min");
            double max = readFiniteDouble(bo.get("max"), "max");
            if (min > max) {
                throw new IllegalArgumentException(I18n.get("log.telemetry.boundsMinGtMax", addr));
            }
            parsed.put(addr, new Bounds(min, max));
        }

        synchronized (lock_) {
            if (clearAll) {
                writableBounds_.clear();
            } else {
                for (String rem : removals) {
                    writableBounds_.remove(rem);
                }
                writableBounds_.putAll(parsed);
            }
            return writableBoundsJsonLocked();
        }
    }

    String getWritableBoundsJson() {
        synchronized (lock_) {
            return writableBoundsJsonLocked();
        }
    }

    String applySafeAction(String actionJson, BiConsumer<String, Double> writer) {
        if (writer == null) {
            throw new IllegalStateException(I18n.get("log.telemetry.ioWriterUnavailable"));
        }
        JsonObject obj = parseObject(actionJson, I18n.get("log.telemetry.label.actionPayload"));
        String inferenceId = optionalString(obj, "inferenceId");
        String prompt = optionalString(obj, "prompt");
        String note = optionalString(obj, "note");

        Map<String, JsonElement> rawWrites = new LinkedHashMap<>();
        String preValidationError = null;
        if (obj.has("writes")) {
            JsonElement writesEl = obj.get("writes");
            if ((writesEl != null) && writesEl.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : writesEl.getAsJsonObject().entrySet()) {
                    rawWrites.put(entry.getKey(), entry.getValue());
                }
            } else {
                preValidationError = I18n.get("log.telemetry.writesNotObject");
            }
        } else {
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String k = entry.getKey();
                if (!"inferenceId".equals(k) && !"prompt".equals(k)
                        && !"note".equals(k) && !"writes".equals(k)) {
                    rawWrites.put(k, entry.getValue());
                }
            }
        }

        Map<String, Double> validated = new TreeMap<>();
        String rejectReason = preValidationError;
        if (rejectReason == null) {
            if (rawWrites.isEmpty()) {
                rejectReason = I18n.get("log.telemetry.actionNoTargets");
            } else {
                synchronized (lock_) {
                    for (Map.Entry<String, JsonElement> entry : rawWrites.entrySet()) {
                        String addr = entry.getKey();
                        if ((addr == null) || addr.trim().isEmpty() || !addr.equals(addr.trim())) {
                            rejectReason = I18n.get("log.telemetry.invalidAddress", addr);
                            break;
                        }
                        if (addr.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                            rejectReason = I18n.get("log.telemetry.localAddressForbidden", addr);
                            break;
                        }
                        Bounds bounds = writableBounds_.get(addr);
                        if (bounds == null) {
                            rejectReason = I18n.get("log.telemetry.addressNotAllowlisted", addr);
                            break;
                        }
                        Double num = parseFiniteNumberOrNull(entry.getValue());
                        if (num == null) {
                            rejectReason = I18n.get("log.telemetry.valueNotFinite", addr);
                            break;
                        }
                        double v = num.doubleValue();
                        if ((v < bounds.min()) || (v > bounds.max())) {
                            rejectReason = I18n.get("log.telemetry.valueOutOfBounds",
                                    v, addr, bounds.min(), bounds.max());
                            break;
                        }
                        validated.put(addr, num);
                    }
                }
            }
        }

        if (rejectReason != null) {
            JsonObject rejectEv = new JsonObject();
            String rejectEventJson;
            synchronized (lock_) {
                long seq = ++nextSeq_;
                rejectEv.addProperty("seq", seq);
                rejectEv.addProperty("cycle", cycleCount_.get());
                rejectEv.addProperty("timestampMs", System.currentTimeMillis());
                rejectEv.addProperty("kind", "safety_reject");
                rejectEv.addProperty("reward", -1.0d);
                if (inferenceId != null) {
                    rejectEv.addProperty("inferenceId", inferenceId);
                }
                if (prompt != null) {
                    rejectEv.addProperty("prompt", prompt);
                }
                String fullNote = ((note != null) && !note.isEmpty())
                        ? (rejectReason + " (" + note + ")")
                        : rejectReason;
                rejectEv.addProperty("note", fullNote);
                rejectEventJson = appendBufferedEventLocked(seq, rejectEv);
            }
            notifyListener(rejectEventJson);

            JsonObject out = new JsonObject();
            out.addProperty("applied", false);
            out.addProperty("rejected", true);
            out.addProperty("reason", rejectReason);
            if (inferenceId != null) {
                out.addProperty("inferenceId", inferenceId);
            }
            return GSON.toJson(out);
        }

        for (Map.Entry<String, Double> write : validated.entrySet()) {
            writer.accept(write.getKey(), write.getValue());
        }

        JsonObject writesOut = new JsonObject();
        for (Map.Entry<String, Double> write : validated.entrySet()) {
            writesOut.addProperty(write.getKey(), write.getValue());
        }

        String actionEventJson;
        synchronized (lock_) {
            long seq = ++nextSeq_;
            long curCycle = cycleCount_.get();
            JsonObject ev = new JsonObject();
            ev.addProperty("seq", seq);
            ev.addProperty("cycle", curCycle);
            ev.addProperty("timestampMs", System.currentTimeMillis());
            ev.addProperty("kind", "action");
            ev.add("writes", writesOut);
            if (inferenceId != null) {
                ev.addProperty("inferenceId", inferenceId);
            }
            if (prompt != null) {
                ev.addProperty("prompt", prompt);
            }
            if (note != null) {
                ev.addProperty("note", note);
            }
            lastActionSeq_ = seq;
            lastActionCycle_ = curCycle;
            lastActionInferenceId_ = inferenceId;
            actionEventJson = appendBufferedEventLocked(seq, ev);
        }
        notifyListener(actionEventJson);

        JsonObject out = new JsonObject();
        out.addProperty("applied", true);
        out.addProperty("rejected", false);
        out.addProperty("written", validated.size());
        out.add("writes", writesOut);
        if (inferenceId != null) {
            out.addProperty("inferenceId", inferenceId);
        }
        return GSON.toJson(out);
    }

    String setValues(String valuesJson, BiConsumer<String, Double> writer) {
        if (writer == null) {
            throw new IllegalStateException(I18n.get("log.telemetry.ioWriterUnavailable"));
        }
        JsonObject obj = parseObject(valuesJson, I18n.get("log.telemetry.label.valuesPayload"));
        if (obj.isEmpty()) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.valuesEmpty"));
        }
        Map<String, Double> parsed = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String addr = requireGlobalAddress(entry.getKey());
            double val = readFiniteDouble(entry.getValue(), addr);
            parsed.put(addr, Double.valueOf(val));
        }
        for (Map.Entry<String, Double> entry : parsed.entrySet()) {
            writer.accept(entry.getKey(), entry.getValue());
        }
        JsonObject out = new JsonObject();
        out.addProperty("written", parsed.size());
        return GSON.toJson(out);
    }

    String recordFeedback(String feedbackJson) {
        JsonObject obj = parseObject(feedbackJson, I18n.get("log.telemetry.label.feedbackPayload"));
        String inferenceId = optionalString(obj, "inferenceId");
        String prompt = optionalString(obj, "prompt");
        String note = optionalString(obj, "note");
        String correction = optionalString(obj, "correction");
        Double reward = null;
        if (obj.has("reward") && !obj.get("reward").isJsonNull()) {
            reward = Double.valueOf(readFiniteDouble(obj.get("reward"), "reward"));
        }

        String eventJson;
        synchronized (lock_) {
            long seq = ++nextSeq_;
            JsonObject ev = new JsonObject();
            ev.addProperty("seq", seq);
            ev.addProperty("cycle", cycleCount_.get());
            ev.addProperty("timestampMs", System.currentTimeMillis());
            ev.addProperty("kind", "feedback");
            if (inferenceId != null) {
                ev.addProperty("inferenceId", inferenceId);
            }
            if (prompt != null) {
                ev.addProperty("prompt", prompt);
            }
            if (reward != null) {
                ev.addProperty("reward", reward);
            }
            if (note != null) {
                ev.addProperty("note", note);
            }
            if (correction != null) {
                ev.addProperty("correction", correction);
            }
            eventJson = appendBufferedEventLocked(seq, ev);
        }
        notifyListener(eventJson);
        return eventJson;
    }

    private TreeMap<String, JsonObject> checkAddressTransition(String addr, LadderIo io,
            ConcurrentHashMap<String, String> globalComments, TreeMap<String, JsonObject> changes) {
        if ((addr == null) || (io == null)) {
            return changes;
        }
        double current = io.getValue();
        Double prev = lastValues_.get(addr);
        if (!hasChanged(prev, current)) {
            return changes;
        }
        lastValues_.put(addr, Double.valueOf(current));
        if (!Double.isFinite(current)) {
            return changes;
        }
        if (changes == null) {
            changes = new TreeMap<>();
        }
        JsonObject ch = new JsonObject();
        if ((prev != null) && Double.isFinite(prev.doubleValue())) {
            ch.addProperty("previous", prev.doubleValue());
        }
        ch.addProperty("value", current);
        String comment = ((globalComments != null) && (globalComments.get(addr) != null))
                ? globalComments.get(addr)
                : io.getComment();
        if ((comment != null) && !comment.isEmpty()) {
            ch.addProperty("comment", comment);
        }
        changes.put(addr, ch);
        return changes;
    }

    private boolean hasChanged(Double prev, double current) {
        if (prev == null) {
            return true;
        }
        double p = prev.doubleValue();
        if (Double.isNaN(p) || Double.isNaN(current)) {
            return Double.compare(p, current) != 0;
        }
        if (epsilon_ <= 0.0d) {
            return p != current;
        }
        return Math.abs(p - current) > epsilon_;
    }

    private String appendBufferedEventLocked(long seq, JsonObject ev) {
        String json = GSON.toJson(ev);
        String obs = formatEventObservation(ev);
        traceBuffer_.addLast(new BufferedEvent(seq, json, obs));
        while (traceBuffer_.size() > maxEntries_) {
            traceBuffer_.removeFirst();
        }
        return json;
    }

    private List<BufferedEvent> drainEvents(int limit) {
        synchronized (lock_) {
            if (traceBuffer_.isEmpty()) {
                return List.of();
            }
            int cap = (limit <= 0) ? Integer.MAX_VALUE : limit;
            List<BufferedEvent> out = new ArrayList<>();
            long lastSeq = drainCursorSeq_;
            for (BufferedEvent ev : traceBuffer_) {
                if (ev.seq() > drainCursorSeq_) {
                    out.add(ev);
                    lastSeq = ev.seq();
                    if (out.size() >= cap) {
                        break;
                    }
                }
            }
            drainCursorSeq_ = lastSeq;
            return out;
        }
    }

    private String writableBoundsJsonLocked() {
        JsonObject out = new JsonObject();
        for (Map.Entry<String, Bounds> entry : writableBounds_.entrySet()) {
            JsonObject b = new JsonObject();
            b.addProperty("min", entry.getValue().min());
            b.addProperty("max", entry.getValue().max());
            out.add(entry.getKey(), b);
        }
        return GSON.toJson(out);
    }

    private static String formatEventObservation(JsonObject ev) {
        StringBuilder sb = new StringBuilder("[LADDER");
        if (ev.has("cycle") && ev.get("cycle").isJsonPrimitive()) {
            sb.append(" cycle=").append(ev.get("cycle").getAsString());
        }
        String kind = optionalString(ev, "kind");
        if ((kind != null) && !kind.isEmpty()) {
            sb.append(' ').append(kind);
        }
        sb.append(']');
        if (ev.has("changes") && ev.get("changes").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : ev.getAsJsonObject("changes").entrySet()) {
                sb.append(' ').append(entry.getKey());
                if (entry.getValue().isJsonObject()) {
                    JsonObject ch = entry.getValue().getAsJsonObject();
                    String comment = optionalString(ch, "comment");
                    if ((comment != null) && !comment.isEmpty()) {
                        sb.append('(').append(comment).append(')');
                    }
                    sb.append('=');
                    if (ch.has("previous")) {
                        sb.append(formatNum(ch.get("previous"))).append("->");
                    }
                    if (ch.has("value")) {
                        sb.append(formatNum(ch.get("value")));
                    }
                }
            }
        }
        if (ev.has("writes") && ev.get("writes").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : ev.getAsJsonObject("writes").entrySet()) {
                sb.append(' ').append(entry.getKey()).append('=').append(formatNum(entry.getValue()));
            }
        }
        String note = optionalString(ev, "note");
        if ((note != null) && !note.isEmpty()) {
            sb.append(" # ").append(note);
        }
        return sb.toString();
    }

    private static String formatNum(JsonElement el) {
        if ((el == null) || !el.isJsonPrimitive()) {
            return "";
        }
        if (el.getAsJsonPrimitive().isNumber()) {
            double d = el.getAsDouble();
            if (Double.isFinite(d) && (d == Math.rint(d)) && (Math.abs(d) < 1e15d)) {
                return Long.toString((long) d);
            }
        }
        return el.getAsString();
    }

    private static JsonObject parseObject(String json, String label) {
        if ((json == null) || json.trim().isEmpty()) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.jsonMustBeObject", label));
        }
        try {
            JsonElement el = JsonParser.parseString(json.trim());
            if ((el == null) || !el.isJsonObject()) {
                throw new IllegalArgumentException(I18n.get("log.telemetry.jsonMustBeObject", label));
            }
            return el.getAsJsonObject();
        } catch (JsonSyntaxException ex) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.invalidJsonIn", label, ex.getMessage()), ex);
        }
    }

    private static Set<String> parseFilterSet(String filterJson) {
        if ((filterJson == null) || filterJson.trim().isEmpty()) {
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(filterJson.trim());
            if ((root == null) || root.isJsonNull()) {
                return null;
            }
            if (root.isJsonArray()) {
                return new HashSet<>(readAddressList(root, "filter"));
            }
            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("addresses") && !obj.get("addresses").isJsonNull()) {
                    return new HashSet<>(readAddressList(obj.get("addresses"), "addresses"));
                }
                return null;
            }
            throw new IllegalArgumentException(I18n.get("log.telemetry.snapshotFilterInvalidType"));
        } catch (JsonSyntaxException ex) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.snapshotFilterInvalidJson", ex.getMessage()),
                    ex);
        }
    }

    private static List<String> readAddressList(JsonElement el, String key) {
        if ((el == null) || !el.isJsonArray()) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.fieldNotArray", key));
        }
        JsonArray arr = el.getAsJsonArray();
        List<String> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            JsonElement item = arr.get(i);
            if ((item == null) || !item.isJsonPrimitive() || !item.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException(I18n.get("log.telemetry.fieldEntriesNotStrings", key));
            }
            out.add(requireGlobalAddress(item.getAsString()));
        }
        return out;
    }

    private static String requireGlobalAddress(String addr) {
        if ((addr == null) || addr.trim().isEmpty() || !addr.equals(addr.trim())) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.invalidGlobalAddress", addr));
        }
        if (addr.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.localAddressDisallowed", addr));
        }
        return addr;
    }

    private static String optionalString(JsonObject obj, String key) {
        if ((obj == null) || !obj.has(key)) {
            return null;
        }
        JsonElement el = obj.get(key);
        if ((el == null) || el.isJsonNull()) {
            return null;
        }
        if (!el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString()) {
            return null;
        }
        return el.getAsString();
    }

    private static int readInt(JsonElement el, String key, int min, int max) {
        double d = readFiniteDouble(el, key);
        if ((d != Math.rint(d)) || (d < min) || (d > max)) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.fieldIntRange", key, min, max));
        }
        return (int) d;
    }

    private static long readLong(JsonElement el, String key, long min, long max) {
        if ((el == null) || !el.isJsonPrimitive() || !el.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.fieldNotNumber", key));
        }
        double d = el.getAsDouble();
        if (!Double.isFinite(d) || (d != Math.rint(d)) || (d < min) || (d > max)) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.fieldIntRange", key, min, max));
        }
        return el.getAsLong();
    }

    private static double readFiniteDouble(JsonElement el, String key) {
        Double parsed = parseFiniteNumberOrNull(el);
        if (parsed == null) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.fieldNotFinite", key));
        }
        return parsed.doubleValue();
    }

    private static Double parseFiniteNumberOrNull(JsonElement el) {
        if ((el == null) || !el.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive prim = el.getAsJsonPrimitive();
        if (!prim.isNumber()) {
            return null;
        }
        double d = prim.getAsDouble();
        return Double.isFinite(d) ? Double.valueOf(d) : null;
    }

    private static boolean readBoolean(JsonElement el, String key) {
        if ((el == null) || !el.isJsonPrimitive() || !el.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(I18n.get("log.telemetry.fieldNotBoolean", key));
        }
        return el.getAsBoolean();
    }

    private static void addFiniteNumber(JsonObject obj, String key, double value) {
        if (Double.isFinite(value)) {
            obj.addProperty(key, value);
        } else {
            obj.add(key, com.google.gson.JsonNull.INSTANCE);
        }
    }
}
