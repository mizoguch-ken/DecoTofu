package ken.mizoguch.soem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import ken.mizoguch.decotofu.JavaLibrary;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Soem#find_adapters()} and the SOEM 2.0 native context
 * ABI, with the shared library of DecoTofu-SOEM when it is built next to the
 * DecoTofu project.
 */
class SoemAdaptersTest {

    /**
     * the shared library DecoTofu-SOEM builds next to this project, in the order
     * of the platforms. The library is not a resource of this project : a test
     * which needs it is skipped when it is not built.
     */
    private static final String[] LIBRARY_CANDIDATES = {
        "../DecoTofu-SOEM/build/decotofusoem.dll",
        "../DecoTofu-SOEM/build/libdecotofusoem.so",
        "../DecoTofu-SOEM/build/libdecotofusoem.dylib",};

    /**
     * @return the built shared library of DecoTofu-SOEM, null when there is none
     */
    private static Path library() {
        String override = System.getProperty("decotofusoem.library");
        if ((override != null) && !override.isEmpty()) {
            Path path = Paths.get(override).toAbsolutePath().normalize();
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        for (String candidate : LIBRARY_CANDIDATES) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }

    @Test
    void findAdaptersIsRefusedWithoutLibrary() {
        Soem soem = new Soem();

        // no shared library loaded : the answer is null, no exception
        assertNull(soem.find_adapters());
    }

    @Test
    void findAdaptersAnswersTheAdaptersOfTheMachine() {
        Path library = library();
        assumeTrue(library != null, "the shared library of DecoTofu-SOEM is not built");

        Soem soem = new Soem();
        assumeTrue(soem.load(library), "the shared library can not be loaded : " + library);

        String adapters = soem.find_adapters();

        // a null answer means that the library is from a build which does not
        // export ec_adapters() : an other build than the one of this repository
        assertNotNull(adapters, "ec_adapters() is not answered by " + library);
        assertTrue(adapters.startsWith("{") && adapters.endsWith("}"), adapters);

        // the machine of a developer has at least one adapter, and the name of
        // an adapter is what SOEM opens : it is never an empty string
        if (!"{}".equals(adapters)) {
            assertFalse(adapters.contains("\":\"\""), adapters);
        }
    }

    @Test
    void contextAndParcelMatchNativeSoemLayout() {
        Path library = library();
        assumeTrue(library != null, "the shared library of DecoTofu-SOEM is not built");

        String libraryName = JavaLibrary.removeFileExtension(library.getFileName());
        if (JavaLibrary.isLinux() || JavaLibrary.isMac()) {
            libraryName = JavaLibrary.stripLibraryPrefix(libraryName);
        }
        JavaLibrary.addLibraryPath(library.getParent(), libraryName, false);

        SoemLibrary soemLib = LibraryLoader.create(SoemLibrary.class).load(libraryName);
        jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getRuntime(soemLib);

        assertTrue(Soem.verifyNativeAbi(soemLib, runtime));
        try {
            assertNotNull(soemLib.ec_version());
            assertEquals(SoemEtherCATMain.EC_MAXSLAVE, soemLib.ec_max_slave());
            assertEquals(SoemEtherCATMain.EC_MAXGROUP, soemLib.ec_max_group());
            assertEquals(SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCAT.ecx_parcelt(runtime)),
                    soemLib.ec_sizeof_parcel());
            assertEquals(SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCATMain.ec_slavet(runtime)),
                    soemLib.ec_sizeof_slave());
            assertEquals(SoemEtherCATMain.ecx_contextt.alignedSize(new SoemEtherCATMain.ec_groupt(runtime)),
                    soemLib.ec_sizeof_group());
            assertEquals(SoemEtherCATMain.ecx_contextt.expectedByteSize(runtime),
                    soemLib.ec_sizeof_context());
        } catch (UnsatisfiedLinkError ex) {
            // Older DecoTofu-SOEM DLL builds predate introspection exports
        }

        SoemEtherCATMain.ecx_contextt context = soemLib.ec_malloc_context().register();
        assertNotNull(context);
        SoemEtherCAT.ecx_parcelt parcel = soemLib.ec_malloc_parcel(context).register();
        assertNotNull(parcel);

        try {
            // 1. Verify ec_redport(context) matches context.port.redport
            Pointer redport = soemLib.ec_redport(context);
            assertNotNull(redport);
            assertEquals(redport.address(), context.port.redport.get().address());

            // 2. Verify parcel.context points to the same native memory as context
            long base = Struct.getMemory(context).address();
            assertEquals(base, Struct.getMemory(parcel.context).address());
            assertEquals(Struct.getMemory(context.slavecount).address(),
                    Struct.getMemory(parcel.context.slavecount).address());

            String offsets = "port=" + (Struct.getMemory(context.port).address() - base)
                    + " (size=" + Struct.size(context.port) + ", align=" + Struct.alignment(context.port) + ")"
                    + ", slavelist=" + (Struct.getMemory(context.slavelist[0]).address() - base)
                    + " (size=" + Struct.size(context.slavelist[0]) + ", align=" + Struct.alignment(context.slavelist[0]) + ")"
                    + ", slavecount=" + (Struct.getMemory(context.slavecount).address() - base)
                    + ", grouplist=" + (Struct.getMemory(context.grouplist[0]).address() - base)
                    + " (size=" + Struct.size(context.grouplist[0]) + ", align=" + Struct.alignment(context.grouplist[0]) + ")"
                    + ", ecaterror=" + (Struct.getMemory(context.ecaterror).address() - base)
                    + ", DCtime=" + (Struct.getMemory(context.DCtime).address() - base)
                    + ", esibuf=" + (Struct.getMemory(context.esibuf[0]).address() - base)
                    + ", esimap=" + (Struct.getMemory(context.esimap[0]).address() - base)
                    + ", esislave=" + (Struct.getMemory(context.esislave).address() - base)
                    + ", elist=" + (Struct.getMemory(context.elist).address() - base)
                    + " (size=" + Struct.size(context.elist) + ", align=" + Struct.alignment(context.elist) + ")"
                    + ", idxstack=" + (Struct.getMemory(context.idxstack).address() - base);

            // 3. Verify ecx_packeterror / ecx_iserror / ecx_poperror across the JNR-FFI boundary
            assertEquals(SoemOsal.FALSE, soemLib.ecx_iserror(context), offsets);
            assertEquals(SoemOsal.FALSE, context.ecaterror.get(), offsets);

            soemLib.ecx_packeterror(context, 3, 0x1018, 2, 0x0001);
            assertEquals(SoemOsal.TRUE, context.ecaterror.get(), offsets);
            assertEquals(SoemOsal.TRUE, soemLib.ecx_iserror(context), offsets);
            assertEquals(1, context.elist.head.get(), offsets);
            assertEquals(0, context.elist.tail.get());

            SoemEtherCATType.ec_errort popped = new SoemEtherCATType.ec_errort(runtime);
            assertEquals(SoemOsal.TRUE, soemLib.ecx_poperror(context, popped));
            assertEquals(1, context.elist.tail.get());
            assertEquals(3, popped.Slave.get());
            assertEquals(0x1018, popped.Index.get());
            assertEquals(2, popped.SubIdx.get());
            assertEquals(SoemEtherCATType.ec_err_type.EC_ERR_TYPE_PACKET_ERROR.intValue(), popped.Etype.get());
            assertEquals(1, popped.union.struct.ErrorCode.get());
            assertEquals(SoemOsal.FALSE, soemLib.ecx_poperror(context, popped));
            assertEquals(SoemOsal.FALSE, context.ecaterror.get());
        } finally {
            soemLib.ec_free_parcel(parcel);
            soemLib.ec_free_context(context);
        }
    }
}
