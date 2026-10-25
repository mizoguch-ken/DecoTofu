/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

/**
 * the comment of an address of the io of the ladders, with the value of the address : the json
 * representation which the file of the ladders holds.
 *
 * the value of the address is normalized, because a json number holds neither a nan nor an infinity
 *
 * @param idx index of the ladder which holds the address of the comment
 * @param address address of the io of the comment
 * @param comment comment of the address
 * @param value value of the address, which is null when the json does not hold any value
 * @author mizoguch-ken
 */
public record LadderJsonComment(Integer idx, String address, String comment, Double value) {

    /**
     * the compact constructor of the record : the value of the address it keeps is a json number,
     * which is kept as it is when the json does not hold any value
     *
     * @param value value of the address of the comment
     */
    public LadderJsonComment {
        if (value != null) {
            if (value.isNaN()) {
                value = 0.0;
            } else if (value == Double.NEGATIVE_INFINITY) {
                value = Double.MIN_VALUE;
            } else if (value == Double.POSITIVE_INFINITY) {
                value = Double.MAX_VALUE;
            }
        }
    }
}
