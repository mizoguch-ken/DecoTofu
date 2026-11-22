package ken.mizoguch.ladders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LadderTelemetryTest {

    private static LadderIoStore newStoreWithGlobals() {
        Object lock = new Object();
        LadderIoStore store = new LadderIoStore(lock);
        store.addGlobalRow();
        store.addLadderRow(1);

        store.createIo(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "X0");
        store.createIo(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "Y0");
        store.createIo(1, ".T0");

        store.getCommentMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).put("X0", "Start");
        store.getCommentMap().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).put("Y0", "Motor");
        store.getCommentMap().get(1).put(".T0", "LocalTimer");

        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "X0").setValue(0.0d);
        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "Y0").setValue(0.0d);
        store.io(1, ".T0").setValue(42.0d);
        return store;
    }

    @Test
    void tracesDeltaTransitionsOnlyAndDrainsWithoutDuplicates() {
        LadderTelemetry telemetry = new LadderTelemetry();
        LadderIoStore store = newStoreWithGlobals();

        // Zero-overhead no-op while tracing is off
        assertNull(telemetry.onCycleComplete(store, 100_000L));
        assertNull(telemetry.onCycleComplete(store, 120_000L));
        assertEquals(2L, telemetry.getCycleCount());
        assertEquals("[]", telemetry.drainTraceJson(0));

        JsonObject startStatus = JsonParser.parseString(
                telemetry.startTrace("{\"maxEntries\":32,\"minIntervalMs\":0}"))
                .getAsJsonObject();
        assertTrue(startStatus.get("tracing").getAsBoolean());

        // Initial cycle establishes baseline & emits initial values once
        String ev1 = telemetry.onCycleComplete(store, 150_000L);
        assertNotNull(ev1);
        JsonObject obj1 = JsonParser.parseString(ev1).getAsJsonObject();
        assertEquals("transition", obj1.get("kind").getAsString());

        // Unchanged cycle emits nothing
        assertNull(telemetry.onCycleComplete(store, 150_000L));

        // Change X0 and Y0 -> transition includes previous, value, and comment
        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "X0").setValue(1.0d);
        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "Y0").setValue(2.5d);
        String ev2 = telemetry.onCycleComplete(store, 200_000L);
        assertNotNull(ev2);
        JsonObject obj2 = JsonParser.parseString(ev2).getAsJsonObject();
        JsonObject changes = obj2.getAsJsonObject("changes");
        assertEquals(0.0d, changes.getAsJsonObject("X0").get("previous").getAsDouble(), 1e-9);
        assertEquals(1.0d, changes.getAsJsonObject("X0").get("value").getAsDouble(), 1e-9);
        assertEquals("Start", changes.getAsJsonObject("X0").get("comment").getAsString());
        assertEquals(2.5d, changes.getAsJsonObject("Y0").get("value").getAsDouble(), 1e-9);

        // Unchanged again -> null
        assertNull(telemetry.onCycleComplete(store, 200_000L));

        // Non-destructive trace() keeps cursor unchanged; drainTrace() advances cursor
        assertEquals(2, JsonParser.parseString(telemetry.traceJson(0)).getAsJsonArray().size());
        JsonArray firstDrain = JsonParser.parseString(telemetry.drainTraceJson(1)).getAsJsonArray();
        assertEquals(1, firstDrain.size());
        assertEquals(1L, firstDrain.get(0).getAsJsonObject().get("seq").getAsLong());

        JsonArray secondDrainObs = JsonParser.parseString(telemetry.drainObservationsJson(0))
                .getAsJsonArray();
        assertEquals(1, secondDrainObs.size());
        String obsText = secondDrainObs.get(0).getAsString();
        assertTrue(obsText.contains("[LADDER cycle="));
        assertTrue(obsText.contains("X0(Start)=0->1"));
        assertEquals("[]", telemetry.drainTraceJson(0));

        telemetry.stopTrace();
        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "X0").setValue(0.0d);
        assertNull(telemetry.onCycleComplete(store, 100_000L));
    }

    @Test
    void inspectsSnapshotAddressesAndStructure() {
        LadderTelemetry telemetry = new LadderTelemetry();
        LadderIoStore store = newStoreWithGlobals();
        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "X0").setValue(1.0d);
        telemetry.onCycleComplete(store, 111L);

        JsonObject snap = JsonParser.parseString(
                telemetry.snapshotJson(store, true, 111L, 90L, 140L, "[\"X0\"]"))
                .getAsJsonObject();
        assertTrue(snap.get("running").getAsBoolean());
        assertEquals(1L, snap.get("cycleCount").getAsLong());
        JsonArray io = snap.getAsJsonArray("io");
        assertEquals(1, io.size());
        assertEquals("X0", io.get(0).getAsJsonObject().get("address").getAsString());
        assertEquals(1.0d, io.get(0).getAsJsonObject().get("value").getAsDouble(), 1e-9);

        JsonArray globalOnly = JsonParser.parseString(telemetry.addressesJson(store, false))
                .getAsJsonArray();
        assertEquals(2, globalOnly.size());
        JsonArray withLocal = JsonParser.parseString(telemetry.addressesJson(store, true))
                .getAsJsonArray();
        assertEquals(3, withLocal.size());
        assertEquals("local", withLocal.get(2).getAsJsonObject().get("scope").getAsString());

        Ladder ladder = new Ladder(1, "Main", 4, 3);
        LadderGrid empty = new LadderGrid(1, 1);
        LadderGrid ld = new LadderGrid(1, 2);
        ld.setBlock(LadderBlock.LOAD);
        ld.setAddress("X0");
        ld.setComment("Start");
        LadderGrid scr = new LadderGrid(2, 2);
        scr.setBlock(LadderBlock.SCRIPT);
        scr.setAddress("Y0");
        scr.setBlockScript("ladders.setValue('Y0', 1);");
        ladder.addLadderGrid(empty);
        ladder.addLadderGrid(scr);
        ladder.addLadderGrid(ld);

        JsonObject noScripts = JsonParser.parseString(
                telemetry.structureJson(new Ladder[]{ladder}, false))
                .getAsJsonObject();
        assertEquals(1, noScripts.get("tabs").getAsInt());
        JsonArray blocksNoScr = noScripts.getAsJsonArray("ladders").get(0).getAsJsonObject()
                .getAsJsonArray("blocks");
        assertEquals(2, blocksNoScr.size());
        assertEquals("LD", blocksNoScr.get(0).getAsJsonObject().get("command").getAsString());
        assertFalse(blocksNoScr.get(1).getAsJsonObject().has("script"));

        JsonObject withScripts = JsonParser.parseString(
                telemetry.structureJson(new Ladder[]{ladder}, true))
                .getAsJsonObject();
        JsonArray blocksWithScr = withScripts.getAsJsonArray("ladders").get(0).getAsJsonObject()
                .getAsJsonArray("blocks");
        assertEquals("ladders.setValue('Y0', 1);",
                blocksWithScr.get(1).getAsJsonObject().get("script").getAsString());
    }

    @Test
    void enforcesAtomicSafetyBoundsAndLogsRejectionsAndFeedback() {
        LadderTelemetry telemetry = new LadderTelemetry();
        List<String> emitted = new ArrayList<>();
        telemetry.setEventListener(emitted::add);

        String boundsOut = telemetry.setWritableBounds(
                "{\"Y0\":{\"min\":0.0,\"max\":10.0},\"D0\":{\"min\":-5.0,\"max\":5.0}}");
        JsonObject parsedBounds = JsonParser.parseString(boundsOut).getAsJsonObject();
        assertEquals(2, parsedBounds.size());

        Map<String, Double> written = new LinkedHashMap<>();
        String ok = telemetry.applySafeAction(
                "{\"inferenceId\":\"inf-1\",\"prompt\":\"p\",\"note\":\"safe\",\"writes\":{\"Y0\":4.5,\"D0\":-2.0}}",
                written::put);
        JsonObject okObj = JsonParser.parseString(ok).getAsJsonObject();
        assertTrue(okObj.get("applied").getAsBoolean());
        assertEquals(4.5d, written.get("Y0"), 1e-9);
        assertEquals(-2.0d, written.get("D0"), 1e-9);

        // Atomic safety rejection: first entry valid, second out-of-bounds -> zero writes
        written.clear();
        String badRange = telemetry.applySafeAction(
                "{\"inferenceId\":\"inf-2\",\"writes\":{\"Y0\":2.0,\"D0\":9.0}}",
                written::put);
        JsonObject badRangeObj = JsonParser.parseString(badRange).getAsJsonObject();
        assertFalse(badRangeObj.get("applied").getAsBoolean());
        assertTrue(badRangeObj.get("rejected").getAsBoolean());
        assertEquals("inf-2", badRangeObj.get("inferenceId").getAsString());
        assertTrue(written.isEmpty());

        // Unlisted, local, and non-numeric rejections also write nothing
        assertTrue(JsonParser.parseString(
                telemetry.applySafeAction("{\"Z9\":1.0}", written::put))
                .getAsJsonObject().get("rejected").getAsBoolean());
        assertTrue(JsonParser.parseString(
                telemetry.applySafeAction("{\".T0\":1.0}", written::put))
                .getAsJsonObject().get("rejected").getAsBoolean());
        assertTrue(JsonParser.parseString(
                telemetry.applySafeAction("{\"Y0\":\"NaN\"}", written::put))
                .getAsJsonObject().get("rejected").getAsBoolean());
        assertTrue(written.isEmpty());

        String errJson = telemetry.onCycleError("Arithmetic overflow", 50_000L);
        assertEquals("error", JsonParser.parseString(errJson).getAsJsonObject().get("kind").getAsString());
        assertEquals(-1.0d, JsonParser.parseString(errJson).getAsJsonObject().get("reward").getAsDouble(), 1e-9);

        String fbJson = telemetry.recordFeedback(
                "{\"inferenceId\":\"inf-1\",\"reward\":1.0,\"note\":\"verified\"}");
        JsonObject fbObj = JsonParser.parseString(fbJson).getAsJsonObject();
        assertEquals("feedback", fbObj.get("kind").getAsString());
        assertEquals(1.0d, fbObj.get("reward").getAsDouble(), 1e-9);

        JsonArray drained = JsonParser.parseString(telemetry.drainTraceJson(0)).getAsJsonArray();
        assertEquals(7, drained.size());
        assertEquals("safety_reject", drained.get(1).getAsJsonObject().get("kind").getAsString());
        assertEquals(-1.0d, drained.get(1).getAsJsonObject().get("reward").getAsDouble(), 1e-9);
        assertEquals("inf-2", drained.get(1).getAsJsonObject().get("inferenceId").getAsString());

        assertTrue(LadderTelemetry.isValidCallbackName("onLadderTrace"));
        assertTrue(LadderTelemetry.isValidCallbackName("app.handlers.onTrace_1"));
        assertFalse(LadderTelemetry.isValidCallbackName(null));
        assertFalse(LadderTelemetry.isValidCallbackName(""));
        assertFalse(LadderTelemetry.isValidCallbackName("1bad"));
        assertFalse(LadderTelemetry.isValidCallbackName("foo();"));
        assertFalse(LadderTelemetry.isValidCallbackName("a+b"));
    }

    @Test
    void validatesSetValuesAtomically() {
        LadderTelemetry telemetry = new LadderTelemetry();
        Map<String, Double> target = new LinkedHashMap<>();

        String ok = telemetry.setValues("{\"X0\":1.0,\"Y0\":0.0}", target::put);
        assertEquals(2, JsonParser.parseString(ok).getAsJsonObject().get("written").getAsInt());
        assertEquals(1.0d, target.get("X0"), 1e-9);

        target.clear();
        boolean threw = false;
        try {
            telemetry.setValues("{\"X0\":1.0,\".LOCAL\":2.0}", target::put);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        assertTrue(threw);
        assertTrue(target.isEmpty());
    }

    @Test
    void linksAcceptedActionsToSubsequentTransitionsAndErrorsCausally() {
        LadderTelemetry telemetry = new LadderTelemetry();
        LadderIoStore store = newStoreWithGlobals();
        telemetry.startTrace("{\"maxEntries\":32,\"minIntervalMs\":0}");
        telemetry.onCycleComplete(store, 100_000L);
        telemetry.drainTraceJson(0); // discard initial baseline transition

        telemetry.setWritableBounds("{\"Y0\":{\"min\":0.0,\"max\":10.0}}");
        Map<String, Double> written = new LinkedHashMap<>();
        String act = telemetry.applySafeAction(
                "{\"inferenceId\":\"inf-99\",\"writes\":{\"Y0\":5.0}}",
                written::put);
        assertTrue(JsonParser.parseString(act).getAsJsonObject().get("applied").getAsBoolean());

        store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "Y0").setValue(5.0d);
        String transJson = telemetry.onCycleComplete(store, 100_000L);
        assertNotNull(transJson);
        JsonObject transObj = JsonParser.parseString(transJson).getAsJsonObject();
        assertTrue(transObj.has("causedBySeq"));
        assertEquals("inf-99", transObj.get("causedByInferenceId").getAsString());

        String errJson = telemetry.onCycleError("Motor stall", 100_000L);
        JsonObject errObj = JsonParser.parseString(errJson).getAsJsonObject();
        assertTrue(errObj.has("causedBySeq"));
        assertEquals("inf-99", errObj.get("causedByInferenceId").getAsString());
        assertEquals("inf-99", errObj.get("inferenceId").getAsString());
    }
}
