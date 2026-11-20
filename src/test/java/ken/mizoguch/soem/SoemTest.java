package ken.mizoguch.soem;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Soem} ABI introspection, mode toggles, DC sync, error/WKC queries,
 * Complete Access SDO overloads, and {@code ec_stop} / {@code ec_wait_thread} shutdown fallback.
 */
class SoemTest {

    private static jnr.ffi.Runtime systemRuntime() {
        return jnr.ffi.Runtime.getSystemRuntime();
    }

    private static SoemEtherCATMain.ecx_contextt allocateContext(jnr.ffi.Runtime runtime) {
        SoemEtherCATMain.ecx_contextt context = new SoemEtherCATMain.ecx_contextt(runtime);
        Pointer mem = Memory.allocateDirect(runtime, (int) context.expectedByteSize(), true);
        context.useMemory(mem);
        return context.register();
    }

    private static SoemEtherCAT.ecx_parcelt allocateParcel(jnr.ffi.Runtime runtime,
            SoemEtherCATMain.ecx_contextt context) {
        SoemEtherCAT.ecx_parcelt parcel = new SoemEtherCAT.ecx_parcelt(runtime);
        int ptrSize = runtime.addressSize();
        Pointer mem = Memory.allocateDirect(runtime, Struct.size(parcel), true);
        mem.putPointer(0L, Struct.getMemory(context));
        mem.putPointer(2L * ptrSize, Memory.allocateDirect(runtime, Integer.BYTES, true));
        mem.putPointer(3L * ptrSize, Memory.allocateDirect(runtime, Long.BYTES, true));
        mem.putPointer(4L * ptrSize, Memory.allocateDirect(runtime, Byte.BYTES, true));
        mem.putPointer(5L * ptrSize, Memory.allocateDirect(runtime, Byte.BYTES, true));
        parcel.useMemory(mem);
        return parcel.register();
    }

    private static Object defaultPrimitiveReturn(Class<?> returnType) {
        if (returnType == boolean.class) {
            return Boolean.FALSE;
        }
        if ((returnType == byte.class) || (returnType == short.class) || (returnType == int.class)) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        return null;
    }

    @Test
    void verifyNativeAbiValidatesAllConstantsAndSizes() {
        jnr.ffi.Runtime runtime = systemRuntime();
        long expectedParcel = SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCAT.ecx_parcelt(runtime));
        long expectedSlave = SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCATMain.ec_slavet(runtime));
        long expectedGroup = SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCATMain.ec_groupt(runtime));
        long expectedContext = SoemEtherCATMain.ecx_contextt.expectedByteSize(runtime);

        SoemLibrary matching = (SoemLibrary) Proxy.newProxyInstance(
                SoemLibrary.class.getClassLoader(),
                new Class<?>[] { SoemLibrary.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "ec_max_slave" -> SoemEtherCATMain.EC_MAXSLAVE;
                    case "ec_max_group" -> SoemEtherCATMain.EC_MAXGROUP;
                    case "ec_sizeof_parcel" -> expectedParcel;
                    case "ec_sizeof_slave" -> expectedSlave;
                    case "ec_sizeof_group" -> expectedGroup;
                    case "ec_sizeof_context" -> expectedContext;
                    default -> defaultPrimitiveReturn(method.getReturnType());
                });
        assertTrue(Soem.verifyNativeAbi(matching, runtime));

        SoemLibrary legacy = (SoemLibrary) Proxy.newProxyInstance(
                SoemLibrary.class.getClassLoader(),
                new Class<?>[] { SoemLibrary.class },
                (proxy, method, args) -> {
                    if ("ec_max_slave".equals(method.getName())) {
                        throw new UnsatisfiedLinkError("ec_max_slave");
                    }
                    return defaultPrimitiveReturn(method.getReturnType());
                });
        assertTrue(Soem.verifyNativeAbi(legacy, runtime));

        for (String field : new String[] {
                "ec_max_slave", "ec_max_group", "ec_sizeof_parcel",
                "ec_sizeof_slave", "ec_sizeof_group", "ec_sizeof_context"
        }) {
            SoemLibrary mismatch = (SoemLibrary) Proxy.newProxyInstance(
                    SoemLibrary.class.getClassLoader(),
                    new Class<?>[] { SoemLibrary.class },
                    (proxy, method, args) -> {
                        if (field.equals(method.getName())) {
                            return switch (method.getName()) {
                                case "ec_max_slave" -> SoemEtherCATMain.EC_MAXSLAVE + 1;
                                case "ec_max_group" -> SoemEtherCATMain.EC_MAXGROUP + 1;
                                case "ec_sizeof_parcel" -> expectedParcel + 8L;
                                case "ec_sizeof_slave" -> expectedSlave + 8L;
                                case "ec_sizeof_group" -> expectedGroup + 8L;
                                default -> expectedContext + 8L;
                            };
                        }
                        return switch (method.getName()) {
                            case "ec_max_slave" -> SoemEtherCATMain.EC_MAXSLAVE;
                            case "ec_max_group" -> SoemEtherCATMain.EC_MAXGROUP;
                            case "ec_sizeof_parcel" -> expectedParcel;
                            case "ec_sizeof_slave" -> expectedSlave;
                            case "ec_sizeof_group" -> expectedGroup;
                            case "ec_sizeof_context" -> expectedContext;
                            default -> defaultPrimitiveReturn(method.getReturnType());
                        };
                    });
            assertFalse(Soem.verifyNativeAbi(mismatch, runtime), "expected mismatch for " + field);
        }

        assertFalse(Soem.verifyNativeAbi(null, runtime));
        assertFalse(Soem.verifyNativeAbi(matching, null));
    }

    @Test
    void unloadedSoemAnswersNullOrFalseSafely() {
        Soem soem = new Soem();

        assertFalse(soem.isLoaded());
        assertFalse(soem.isRunning());
        assertNull(soem.version());
        assertNull(soem.setOverlappedMode(true));
        assertNull(soem.isOverlappedMode());
        assertNull(soem.setPackedMode(true));
        assertNull(soem.isPackedMode());
        assertNull(soem.enableCyclicMailbox(1));
        assertNull(soem.dcsync0(1, true, 1000L, 0));
        assertNull(soem.dcsync01(1, true, 1000L, 2000L, 0));
        assertNull(soem.wkc());
        assertNull(soem.expectedWkc());
        assertNull(soem.dcTime());
        assertNull(soem.isError());
        assertNull(soem.popError());
        assertNull(soem.sdoRead(1, 0x1018, 1, true, 4));
        assertNull(soem.sdoWrite(1, 0x1018, 1, true, new byte[] { 1 }));
    }

    @Test
    void contextConfigurationDcSyncDiagnosticsAndCompleteAccessSdoWork() {
        jnr.ffi.Runtime runtime = systemRuntime();
        SoemEtherCATMain.ecx_contextt context = allocateContext(runtime);
        SoemEtherCAT.ecx_parcelt parcel = allocateParcel(runtime, context);
        context.slavecount.set(2);
        context.grouplist[0].outputsWKC.set(1);
        context.grouplist[0].inputsWKC.set(2);
        context.DCtime.set(987654321L);
        parcel.wkc.set(4);

        List<String> calls = new ArrayList<>();
        int[] lastSdoReadCA = { -1 };
        int[] lastSdoWriteCA = { -1 };

        SoemLibrary lib = (SoemLibrary) Proxy.newProxyInstance(
                SoemLibrary.class.getClassLoader(),
                new Class<?>[] { SoemLibrary.class },
                (proxy, method, args) -> {
                    calls.add(method.getName());
                    return switch (method.getName()) {
                        case "ec_version" -> "2.0.0";
                        case "ecx_slavembxcyclic" -> 1;
                        case "ecx_dcsync0", "ecx_dcsync01" -> null;
                        case "ecx_iserror" -> SoemOsal.TRUE;
                        case "ecx_elist2string" -> "E_TEST";
                        case "ecx_SDOread" -> {
                            lastSdoReadCA[0] = ((Number) args[4]).intValue();
                            Pointer psize = (Pointer) args[5];
                            Pointer p = (Pointer) args[6];
                            psize.putInt(0, 2);
                            p.putByte(0, (byte) 0x12);
                            p.putByte(1, (byte) 0x34);
                            yield 1;
                        }
                        case "ecx_SDOwrite" -> {
                            lastSdoWriteCA[0] = ((Number) args[4]).intValue();
                            yield 1;
                        }
                        default -> defaultPrimitiveReturn(method.getReturnType());
                    };
                });

        SoemEcatThread ecatThread = new SoemEcatThread(lib, parcel, context, new Object());
        Soem soem = new Soem(lib, runtime, context, parcel, ecatThread);

        assertTrue(soem.isLoaded());
        assertFalse(soem.isRunning());
        assertEquals("2.0.0", soem.version());
        assertEquals(4, soem.wkc());
        assertEquals(4, soem.expectedWkc());
        assertEquals(987654321L, soem.dcTime());

        assertFalse(soem.isOverlappedMode());
        assertTrue(soem.setOverlappedMode(true));
        assertTrue(soem.isOverlappedMode());

        assertFalse(soem.isPackedMode());
        assertTrue(soem.setPackedMode(true));
        assertTrue(soem.isPackedMode());

        assertFalse(soem.enableCyclicMailbox(0));
        assertFalse(soem.enableCyclicMailbox(3));
        assertTrue(soem.enableCyclicMailbox(1));

        assertFalse(soem.dcsync0(0, true, 1000L, 0));
        assertFalse(soem.dcsync0(1, true, -1L, 0));
        assertTrue(soem.dcsync0(1, true, 1000000L, 250));
        assertFalse(soem.dcsync01(1, true, 1000L, -1L, 0));
        assertTrue(soem.dcsync01(2, true, 1000000L, 2000000L, -100));

        assertTrue(soem.isError());
        assertEquals("E_TEST", soem.popError());

        Byte[] readData = soem.sdoRead(1, 0x1600, 0, true, 8);
        assertArrayEquals(new Byte[] { (byte) 0x12, (byte) 0x34 }, readData);
        assertEquals(SoemOsal.TRUE, lastSdoReadCA[0]);

        assertEquals(1, soem.sdoWrite(1, 0x1600, 0, true, new byte[] { 1, 2, 3 }));
        assertEquals(SoemOsal.TRUE, lastSdoWriteCA[0]);

        assertTrue(calls.contains("ecx_dcsync0"));
        assertTrue(calls.contains("ecx_dcsync01"));
    }

    @Test
    void closePrefersEcStopAndFallsBackToEcWaitThread() {
        jnr.ffi.Runtime runtime = systemRuntime();

        SoemEtherCATMain.ecx_contextt ctx1 = allocateContext(runtime);
        SoemEtherCAT.ecx_parcelt parcel1 = allocateParcel(runtime, ctx1);
        List<String> calls1 = new ArrayList<>();
        SoemLibrary libWithStop = (SoemLibrary) Proxy.newProxyInstance(
                SoemLibrary.class.getClassLoader(),
                new Class<?>[] { SoemLibrary.class },
                (proxy, method, args) -> {
                    calls1.add(method.getName());
                    if ("ec_stop".equals(method.getName())) {
                        return 1;
                    }
                    return defaultPrimitiveReturn(method.getReturnType());
                });
        Soem soem1 = new Soem(libWithStop, runtime, ctx1, parcel1,
                new SoemEcatThread(libWithStop, parcel1, ctx1, new Object()));
        assertTrue(soem1.close(null));
        assertTrue(calls1.contains("ec_stop"));
        assertFalse(calls1.contains("ec_wait_thread"));

        SoemEtherCATMain.ecx_contextt ctx2 = allocateContext(runtime);
        SoemEtherCAT.ecx_parcelt parcel2 = allocateParcel(runtime, ctx2);
        List<String> calls2 = new ArrayList<>();
        SoemLibrary libLegacy = (SoemLibrary) Proxy.newProxyInstance(
                SoemLibrary.class.getClassLoader(),
                new Class<?>[] { SoemLibrary.class },
                (proxy, method, args) -> {
                    calls2.add(method.getName());
                    if ("ec_stop".equals(method.getName())) {
                        throw new UnsatisfiedLinkError("ec_stop");
                    }
                    if ("ec_wait_thread".equals(method.getName())) {
                        return 1;
                    }
                    return defaultPrimitiveReturn(method.getReturnType());
                });
        Soem soem2 = new Soem(libLegacy, runtime, ctx2, parcel2,
                new SoemEcatThread(libLegacy, parcel2, ctx2, new Object()));
        assertTrue(soem2.close(null));
        assertTrue(calls2.contains("ec_stop"));
        assertTrue(calls2.contains("ec_wait_thread"));
    }
}
