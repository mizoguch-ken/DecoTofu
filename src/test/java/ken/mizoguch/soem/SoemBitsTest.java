/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * the bit access of the process data map : ken.mizoguch.soem.SoemBits
 *
 * @author mizoguch-ken
 */
class SoemBitsTest {

    /** size of the map of the group used by the tests */
    private static final int MAP_SIZE = 16;

    /**
     * a map filled with "bytes", the rest of the map is cleared
     *
     * @param bytes
     * @return
     */
    private Pointer map(int... bytes) {
        Pointer map = Memory.allocate(Runtime.getSystemRuntime(), MAP_SIZE);

        for (int i = 0; i < MAP_SIZE; i++) {
            map.putByte(i, (byte) ((i < bytes.length) ? bytes[i] : 0));
        }
        return map;
    }

    /**
     * the write of the same bit access, the one used to queue the outputs of the
     * ecat thread : only the bits of the map that belong to the variable are
     * replaced, the neighbour bits of the byte are kept
     *
     * @param map
     * @param byteOffset
     * @param startBit
     * @param bitsMask
     * @param value
     */
    private void write(Pointer map, long byteOffset, int startBit, long bitsMask, long value) {
        long masked = value & bitsMask;
        int count = SoemBits.byteCount(startBit, SoemBits.bitLength(bitsMask),
                SoemBits.mapBytes(startBit, SoemBits.bitLength(bitsMask)));

        for (int cnt = 0; cnt < count; cnt++) {
            int mask = SoemBits.byteAt(bitsMask, startBit, cnt);
            int bits = SoemBits.byteAt(masked, startBit, cnt);
            int current = Byte.toUnsignedInt(map.getByte(byteOffset + cnt));

            map.putByte(byteOffset + cnt, (byte) ((current & (mask ^ 0xff)) | bits));
        }
    }

    @Test
    @DisplayName("the number of significant bits of a mask")
    void bitLengthOfAMask() {
        assertEquals(1, SoemBits.bitLength(0x1));
        assertEquals(8, SoemBits.bitLength(0xff));
        assertEquals(16, SoemBits.bitLength(0xffff));
        assertEquals(32, SoemBits.bitLength(0xffffffffL));
        // (1 << 48) - 1 is 0 with the int shift of the previous code : the mask
        // of a 48 bits variable has to keep its 48 bits
        assertEquals(48, SoemBits.bitLength(0xffffffffffffL));
        assertEquals(64, SoemBits.bitLength(-1L));
    }

    @Test
    @DisplayName("the number of bytes held by a slave mapping")
    void mapBytesOfASlave() {
        assertEquals(1, SoemBits.mapBytes(0, 1));
        assertEquals(1, SoemBits.mapBytes(0, 8));
        assertEquals(2, SoemBits.mapBytes(1, 8));
        assertEquals(2, SoemBits.mapBytes(4, 12));
        assertEquals(4, SoemBits.mapBytes(0, 32));
        assertEquals(9, SoemBits.mapBytes(4, 64));
    }

    @Test
    @DisplayName("the byte of the map that holds the bits of a variable")
    void byteAtOfAVariable() {
        // a 16 bits variable that starts at the bit 4 of the first byte
        assertEquals(0xf0, SoemBits.byteAt(0xffffL, 4, 0));
        assertEquals(0xff, SoemBits.byteAt(0xffffL, 4, 1));
        assertEquals(0x0f, SoemBits.byteAt(0xffffL, 4, 2));
        assertEquals(0x00, SoemBits.byteAt(0xffffL, 4, 3));

        assertEquals(0x34, SoemBits.byteAt(0x1234L, 0, 0));
        assertEquals(0x12, SoemBits.byteAt(0x1234L, 0, 1));
        assertEquals(0x00, SoemBits.byteAt(0x1234L, 0, 2));
    }

    @Test
    @DisplayName("an aligned variable is read little endian")
    void readAnAlignedVariable() {
        assertEquals(0x1234L, SoemBits.read(map(0x34, 0x12), 0, 0, 0xffffL, MAP_SIZE));
        assertEquals(0x12L, SoemBits.read(map(0x34, 0x12), 1, 0, 0xffL, MAP_SIZE));
    }

    @Test
    @DisplayName("an unaligned variable is read from the bits it owns")
    void readAnUnalignedVariable() {
        // 00001111 00001111 : the 8 bits that start at the bit 4 of the first byte
        assertEquals(0xf0L, SoemBits.read(map(0x0f, 0x0f), 0, 4, 0xffL, MAP_SIZE));
        // the 12 bits that start at the bit 4 of the first byte
        assertEquals(0x0f0L, SoemBits.read(map(0x0f, 0x0f), 0, 4, 0xfffL, MAP_SIZE));
        // the neighbour bits of the first byte are not a part of the variable
        assertEquals(0x0fL, SoemBits.read(map(0xff, 0x0f), 0, 4, 0xfL, MAP_SIZE));
    }

    @Test
    @DisplayName("a variable wider than 32 bits keeps its bits")
    void readAWideVariable() {
        Pointer map = map(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08);

        assertEquals(0x030201L, SoemBits.read(map, 0, 0, 0xffffffL, MAP_SIZE));
        assertEquals(0x060504030201L, SoemBits.read(map, 0, 0, 0xffffffffffffL, MAP_SIZE));
        assertEquals(0x0807060504030201L, SoemBits.read(map, 0, 0, -1L, MAP_SIZE));
    }

    @Test
    @DisplayName("a 64 bits variable of an unaligned mapping")
    void readAnUnalignedQuad() {
        Pointer map = map();
        long value = 0x0102030405060708L;

        write(map, 0, 4, -1L, value);
        assertEquals(value, SoemBits.read(map, 0, 4, -1L, SoemBits.mapBytes(4, 64)));
        // the mapping of a 64 bits variable that starts at the bit 4 ends at the
        // byte 8 of the map : it holds the highest bits of the variable
        assertEquals(0x01, Byte.toUnsignedInt(map.getByte(8)));
    }

    @Test
    @DisplayName("no byte behind the mapping of the slave is read")
    void readStaysInsideTheSlave() {
        // the mapping of the slave is one byte wide, the next byte of the map
        // belongs to the next slave of the group
        Pointer map = map(0x11, 0xff);

        // the variable asks for two bytes : only the byte of the slave is read
        assertEquals(0x11L, SoemBits.read(map, 0, 0, 0xffffL, 1));
        assertEquals(0xffL, SoemBits.read(map, 1, 0, 0xffffL, 2));
        assertEquals(0x00L, SoemBits.read(map, 2, 0, 0xffffL, 2));
    }

    @Test
    @DisplayName("write then read is the identity")
    void writeThenRead() {
        long[][] cases = {
            {0, 0, 0xffL, 0xa5L},
            {0, 0, 0xffffL, 0x1234L},
            {0, 3, 0xfffL, 0x7ffL},
            {1, 7, 0xffffffffL, 0xdeadbeefL},
            {2, 0, 0xffffffffffffL, 0x5a5a5a5a5a5aL},
            {3, 4, -1L, 0xfedcba9876543210L},
            {7, 1, 0x1L, 0x1L},};

        for (long[] sel : cases) {
            Pointer map = map();
            long byteOffset = sel[0];
            int startBit = (int) sel[1];
            long bitsMask = sel[2];
            long value = sel[3] & bitsMask;

            write(map, byteOffset, startBit, bitsMask, value);
            assertEquals(value, SoemBits.read(map, byteOffset, startBit, bitsMask, MAP_SIZE),
                    "byteOffset[" + byteOffset + "] startBit[" + startBit + "] mask[0x"
                    + Long.toHexString(bitsMask) + "]");
        }
    }

    @Test
    @DisplayName("writing a variable leaves the neighbour variables alone")
    void writeDoesNotTouchTheNeighbours() {
        Pointer map = map(0b10000001, 0b01111110, 0b10000001);

        // the variable holds the bits 4..7 of the first byte and the bits 0..3 of
        // the second one
        write(map, 0, 4, 0xffL, 0x0aL);
        assertEquals(0b10100001, Byte.toUnsignedInt(map.getByte(0)));
        assertEquals(0b01110000, Byte.toUnsignedInt(map.getByte(1)));
        assertEquals(0b10000001, Byte.toUnsignedInt(map.getByte(2)));
        assertEquals(0x0aL, SoemBits.read(map, 0, 4, 0xffL, 2));
    }
}
