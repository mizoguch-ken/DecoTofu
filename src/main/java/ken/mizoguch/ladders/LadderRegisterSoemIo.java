/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

/**
 * the registration of an address of the ladders on a variable of a slave of a soem bus, which the
 * cycle of the ladders reads and writes
 *
 * @param address global address of the ladders
 * @param slave slave of the soem bus
 * @param bitsOffset offset of the bits of the variable of the slave
 * @param bitsMask mask of the bits of the variable of the slave
 * @author mizoguch-ken
 */
public record LadderRegisterSoemIo(String address, int slave, long bitsOffset, long bitsMask) {
}
