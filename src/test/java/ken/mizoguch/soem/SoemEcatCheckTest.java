package ken.mizoguch.soem;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for slave lost/recovery mailbox and input handling in {@link SoemEcatCheck}.
 */
class SoemEcatCheckTest {

    @Test
    void handleSlaveLostMarksLostZerosByteAlignedInputsAndParksCyclicMailbox() {
        jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getSystemRuntime();
        SoemEtherCATMain.ec_slavet slave = new SoemEtherCATMain.ec_slavet(runtime);
        slave.useMemory(Memory.allocateDirect(runtime, jnr.ffi.Struct.size(slave), true));

        Pointer io = Memory.allocateDirect(runtime, 8, true);
        io.putByte(0, (byte) 0xAA);
        io.putByte(1, (byte) 0x55);
        io.putByte(2, (byte) 0xFF);
        io.putByte(3, (byte) 0x77);
        io.putByte(4, (byte) 0x99);

        slave.inputs.set(io.slice(1, 3));
        slave.Ibytes.set(3L);
        slave.Istartbit.set((short) 0);
        slave.mbxhandlerstate.set(SoemEtherCATMain.ECT_MBXH_CYCLIC);
        slave.islost.set((short) SoemOsal.FALSE);

        SoemEcatCheck.handleSlaveLost(slave);

        assertEquals(SoemOsal.TRUE, slave.islost.get());
        assertEquals(SoemEtherCATMain.ECT_MBXH_LOST, slave.mbxhandlerstate.get());
        // Slave's 3 bytes are zeroed, neighbouring bytes stay untouched
        assertEquals((byte) 0xAA, io.getByte(0));
        assertEquals((byte) 0x00, io.getByte(1));
        assertEquals((byte) 0x00, io.getByte(2));
        assertEquals((byte) 0x00, io.getByte(3));
        assertEquals((byte) 0x99, io.getByte(4));
    }

    @Test
    void handleSlaveLostLeavesBitPackedInputsAndNonCyclicMailboxUntouched() {
        jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getSystemRuntime();
        SoemEtherCATMain.ec_slavet slave = new SoemEtherCATMain.ec_slavet(runtime);
        slave.useMemory(Memory.allocateDirect(runtime, jnr.ffi.Struct.size(slave), true));

        Pointer io = Memory.allocateDirect(runtime, 4, true);
        io.putByte(0, (byte) 0xF5);
        slave.inputs.set(io);
        slave.Ibytes.set(1L);
        slave.Istartbit.set((short) 2);
        slave.mbxhandlerstate.set(SoemEtherCATMain.ECT_MBXH_NONE);

        SoemEcatCheck.handleSlaveLost(slave);

        assertEquals(SoemOsal.TRUE, slave.islost.get());
        assertEquals(SoemEtherCATMain.ECT_MBXH_NONE, slave.mbxhandlerstate.get());
        assertEquals((byte) 0xF5, io.getByte(0));
    }

    @Test
    void handleSlaveSafeOpRecoveryRestoresOperationalAndCyclicMailbox() {
        jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getSystemRuntime();
        SoemEtherCATMain.ec_slavet slave = new SoemEtherCATMain.ec_slavet(runtime);
        slave.useMemory(Memory.allocateDirect(runtime, jnr.ffi.Struct.size(slave), true));

        slave.state.set(SoemEtherCATType.ec_state.EC_STATE_SAFE_OP.intValue());
        slave.mbxhandlerstate.set(SoemEtherCATMain.ECT_MBXH_LOST);

        SoemEcatCheck.handleSlaveSafeOpRecovery(slave);

        assertEquals(SoemEtherCATType.ec_state.EC_STATE_OPERATIONAL.intValue(), slave.state.get());
        assertEquals(SoemEtherCATMain.ECT_MBXH_CYCLIC, slave.mbxhandlerstate.get());
    }
}
