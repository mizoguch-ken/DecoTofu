/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import jnr.ffi.Pointer;

/**
 * bit level access to the SOEM process data map.
 *
 * A map of a group is a packed little endian buffer. A variable of a slave
 * starts at the byte (bitsOffset / 8) of the slave mapping and at the bit
 * (Ostartbit or Istartbit) + (bitsOffset % 8) of that byte, so it is in general
 * not aligned on a byte and it may end in the middle of a byte.
 *
 * Only the bytes that really hold a bit of the variable may be read or written.
 * Reading a wider word than the variable is not safe : the map of the group ends
 * with the last slave and there is no byte allocated after it, and a mask built
 * with the int shift (1 &lt;&lt; bits) is wrong from 32 bits on.
 *
 * @author mizoguch-ken
 */
final class SoemBits {

    /** the widest variable that the interface of the plugin can handle. */
    static final int MAX_BITS = 64;

    /**
     * a utility class is never instantiated
     */
    private SoemBits() {
    }

    /**
     * @param bitsMask bit mask of the variable (&gt; 0)
     * @return the number of significant bits of the mask (1..64)
     */
    static int bitLength(long bitsMask) {
        return Long.SIZE - Long.numberOfLeadingZeros(bitsMask);
    }

    /**
     * number of bytes of the map that belong to a slave mapping : a mapping of
     * "bits" bits starting at the bit "startBit" ends in the middle of a byte.
     *
     * @param startBit first bit of the mapping in the first byte of the slave
     * @param bits number of bits of the mapping
     * @return the number of bytes of the map held by the slave
     */
    static long mapBytes(int startBit, long bits) {
        return (startBit + bits + Byte.SIZE - 1) / Byte.SIZE;
    }

    /**
     * number of bytes of the map to read or to write for the variable : from the
     * byte of the first bit of the mask to the byte of the last one.
     *
     * @param startBit bit offset of the variable in the accessed byte
     * @param bitLength number of significant bits of the mask
     * @param remainingBytes bytes left from the accessed byte in the map
     * @return the number of bytes to access, never more than remainingBytes
     */
    static int byteCount(int startBit, int bitLength, long remainingBytes) {
        long count = ((long) startBit + bitLength + Byte.SIZE - 1) / Byte.SIZE;
        return (int) Math.clamp(count, 0L, Math.max(0L, remainingBytes));
    }

    /**
     * the bits of "bits" that the byte "byteIndex" of the map holds once they
     * are shifted by "startBit". A byte of the map that holds no bit of the
     * variable answers 0.
     *
     * @param bits value or mask of the variable
     * @param startBit bit offset of the variable in the first byte of the map
     * @param byteIndex byte of the map
     * @return the 8 bits of that byte
     */
    static int byteAt(long bits, int startBit, int byteIndex) {
        long shift = ((long) byteIndex * Byte.SIZE) - startBit;
        long value;

        if (shift >= Long.SIZE) {
            value = 0;
        } else if (shift >= 0) {
            value = bits >>> shift;
        } else if (-shift >= Long.SIZE) {
            value = 0;
        } else {
            value = bits << -shift;
        }
        return (int) (value & 0xff);
    }

    /**
     * the bits of one byte of the map that do not belong to the variable : a
     * write of the variable has to keep them.
     *
     * @param maskByte the bits of the byte that belong to the variable
     * @return the other bits of that byte
     */
    static int keepMask(int maskByte) {
        return maskByte ^ 0xff;
    }

    /**
     * merge the value of a variable in one byte of the map : the bits of the
     * variable take their place in the byte, the bits of the neighbour
     * variables are kept.
     *
     * @param mapByte current byte of the map
     * @param keep bits of the byte that do not belong to the variable
     * @param bits value of the variable in that byte
     * @return the new byte of the map
     */
    static byte merge(byte mapByte, int keep, int bits) {
        return (byte) ((Byte.toUnsignedInt(mapByte) & keep) | bits);
    }

    /**
     * read the variable of a slave in the map, the bytes that only hold the
     * neighbour slaves are never read.
     *
     * @param map map of the group or map of the slave
     * @param byteOffset first byte of the variable in "map"
     * @param startBit bit offset of the variable in that byte
     * @param bitsMask bit mask of the variable (&gt; 0)
     * @param mapBytes bytes of "map" that belong to the slave
     * @return the value of the variable, masked with "bitsMask"
     */
    static long read(Pointer map, long byteOffset, int startBit, long bitsMask, long mapBytes) {
        int count = byteCount(startBit, bitLength(bitsMask), mapBytes - byteOffset);
        long value = 0;

        for (int cnt = 0; cnt < count; cnt++) {
            value |= byteShifted(map.getByte(byteOffset + cnt), startBit, cnt) & bitsMask;
        }
        return value;
    }

    /**
     * the bits of one byte of the map at their place in the value of the
     * variable, without the bits of the neighbour variables.
     *
     * @param mapByte one byte of the map
     * @param startBit bit offset of the variable in the first byte of the map
     * @param byteIndex index of that byte
     * @return the contribution of that byte to the value of the variable
     */
    private static long byteShifted(byte mapByte, int startBit, int byteIndex) {
        long shift = ((long) byteIndex * Byte.SIZE) - startBit;
        long bits = Byte.toUnsignedLong(mapByte);

        if (shift >= Long.SIZE) {
            return 0;
        }
        return (shift >= 0) ? (bits << shift) : (bits >>> -shift);
    }
}
