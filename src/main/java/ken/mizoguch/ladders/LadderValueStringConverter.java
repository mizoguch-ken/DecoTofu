/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import javafx.util.StringConverter;

/**
 *
 * @author mizoguch-ken
 */
public class LadderValueStringConverter extends StringConverter<Double> {

    private Double value;
    private int radix;

    /**
     *
     */
    public LadderValueStringConverter() {
        value = 0.0;
        radix = LadderNumber.RADIX_DECIMAL;
    }

    @Override
    public String toString(Double object) {
        if (object != null) {
            if (!object.isInfinite() && !object.isNaN()) {
                value = object;
            }
        }
        return LadderNumber.format(value, radix);
    }

    @Override
    public Double fromString(String string) {
        if (string != null) {
            LadderNumber.Value number = LadderNumber.parse(string.trim());

            if (number != null) {
                value = number.getValue();
                radix = number.getRadix();
            }
        }
        return value;
    }
}
