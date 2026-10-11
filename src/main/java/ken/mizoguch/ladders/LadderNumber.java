/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * notation of the numbers of the ladders : a decimal number, a hexadecimal number with a
 * 0x prefix and a binary number with a 0b prefix. The pattern is defined here only, the
 * classes of the ladders share it.
 *
 * @author mizoguch-ken
 */
public class LadderNumber {

    /**
     * radix of a number written without a prefix
     */
    public static final int RADIX_DECIMAL = 10;
    /**
     * radix of a number written with the 0x prefix
     */
    public static final int RADIX_HEXADECIMAL = 16;
    /**
     * radix of a number written with the 0b prefix
     */
    public static final int RADIX_BINARY = 2;

    private static final Pattern PATTERN_REAL_NUMBER = Pattern.compile(
            "^[\\+\\-]?(?:((?:\\d*\\.\\d+|\\d+\\.?\\d*)(?:[eE][\\+\\-]?\\d+|))|0[xX]([0-9a-fA-F]+)|0[bB]([0-1]+))$");

    /**
     * value of a parsed number and radix it was written with
     */
    public static class Value {

        private final double value;
        private final int radix;

        private Value(double value, int radix) {
            this.value = value;
            this.radix = radix;
        }

        /**
         *
         * @return the value of the number
         */
        public double getValue() {
            return value;
        }

        /**
         *
         * @return the radix the number was written with
         */
        public int getRadix() {
            return radix;
        }
    }

    /**
     * the numbers of the ladders are not created by this class
     */
    private LadderNumber() {
    }

    /**
     * check if a string is a number : a real number is never used as an address of the io
     * map. The first character is checked before the regular expression, so that the cycle
     * of the ladder is not slowed by a regular expression for every block of a ladder. A
     * number never starts with an other character than a sign, a digit or a dot, the dot
     * starts the numbers written without their integer part as ".5"
     *
     * @param string
     * @return true when the string is a number
     */
    public static boolean isNumber(String string) {
        if ((string == null) || string.isEmpty()) {
            return false;
        }
        char ch = string.charAt(0);

        if (!((ch == '+') || (ch == '-') || (ch == '.') || ((ch >= '0') && (ch <= '9')))) {
            return false;
        }
        return PATTERN_REAL_NUMBER.matcher(string).find();
    }

    /**
     * read a number written with the notation of the ladders : the decimal value is read as
     * it is, the hexadecimal and the binary values are read without their prefix. The sign
     * of the number is out of the notation, so it is taken from the given string and kept
     * for every radix
     *
     * @param string
     * @return the value and the radix of the number, null when the string is not a number
     */
    public static Value parse(String string) {
        if (string == null) {
            return null;
        }
        Matcher matcher = PATTERN_REAL_NUMBER.matcher(string);

        if (!matcher.find()) {
            return null;
        }
        if (matcher.group(1) != null) {
            return new Value(Double.parseDouble(string), RADIX_DECIMAL);
        }
        boolean negative = (string.charAt(0) == '-');

        if (matcher.group(2) != null) {
            return new Value(sign(negative, Long.parseLong(matcher.group(2), RADIX_HEXADECIMAL)),
                    RADIX_HEXADECIMAL);
        }
        return new Value(sign(negative, Long.parseLong(matcher.group(3), RADIX_BINARY)), RADIX_BINARY);
    }

    /**
     * @param negative true when the number carries a minus sign
     * @param value value read without its sign
     * @return the value of the number
     */
    private static double sign(boolean negative, long value) {
        return negative ? -value : value;
    }

    /**
     * text of a value with the radix it is shown with : the hexadecimal and the binary
     * values carry their prefix and their sign before the prefix, so that the text is read
     * again by parse(). An other radix is answered as a decimal value
     *
     * @param value
     * @param radix
     * @return the text of the value
     */
    public static String format(double value, int radix) {
        boolean negative = (value < 0.0);
        String prefix = negative ? "-" : "";
        long integer = (long) value;

        switch (radix) {
            case RADIX_HEXADECIMAL:
                return prefix + "0x" + Long.toString(negative ? -integer : integer, RADIX_HEXADECIMAL);
            case RADIX_BINARY:
                return prefix + "0b" + Long.toString(negative ? -integer : integer, RADIX_BINARY);
            default:
                return Double.toString(value);
        }
    }
}
