/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ken.mizoguch.soem;

import jnr.ffi.util.EnumMapper;

/**
 * the names the objects of the dictionary of a slave answer in the slaveinfo of
 * the web page : the numbers of the dictionaries of SOEM are named as the CiA
 * 301 dictionary does.
 *
 * @author mizoguch-ken
 */
final class SoemNames {

    /**
     * the object types of the CiA 301 dictionary of a slave.
     */
    public enum ec_objecttype implements EnumMapper.IntegerEnum {
        OTYPE_VAR(0x0007),
        OTYPE_ARRAY(0x0008),
        OTYPE_RECORD(0x0009);

        private final int value_;

        private ec_objecttype(int value) {
            value_ = value;
        }

        @Override
        public int intValue() {
            return value_;
        }
    }

    /**
     * the access of an entry of the dictionary of a slave : every state of the
     * slave has its own read and write access.
     */
    private enum ec_accesstype implements EnumMapper.IntegerEnum {
        ATYPE_Rpre(0x01),
        ATYPE_Rsafe(0x02),
        ATYPE_Rop(0x04),
        ATYPE_Wpre(0x08),
        ATYPE_Wsafe(0x10),
        ATYPE_Wop(0x20);

        private final int value_;

        private ec_accesstype(int value) {
            value_ = value;
        }

        @Override
        public int intValue() {
            return value_;
        }
    }

    /**
     * a utility class is never instantiated
     */
    private SoemNames() {
    }

    /**
     * the name of the data type of an entry of the dictionary of a slave
     *
     * @param dtype number of the data type of SOEM
     * @param bitlen number of bits of the entry, named by the string types
     * @return the name of the type
     */
    static String dtype2string(int dtype, int bitlen) {
        try {
            SoemEtherCATType.ec_datatype edtype = (SoemEtherCATType.ec_datatype) EnumMapper
                    .getInstance(SoemEtherCATType.ec_datatype.class).valueOf(dtype);
            return switch (edtype) {
                case ECT_BOOLEAN -> "BOOLEAN";
                case ECT_INTEGER8 -> "INTEGER8";
                case ECT_INTEGER16 -> "INTEGER16";
                case ECT_INTEGER32 -> "INTEGER32";
                case ECT_INTEGER24 -> "INTEGER24";
                case ECT_INTEGER64 -> "INTEGER64";
                case ECT_UNSIGNED8 -> "UNSIGNED8";
                case ECT_UNSIGNED16 -> "UNSIGNED16";
                case ECT_UNSIGNED32 -> "UNSIGNED32";
                case ECT_UNSIGNED24 -> "UNSIGNED24";
                case ECT_UNSIGNED64 -> "UNSIGNED64";
                case ECT_REAL32 -> "REAL32";
                case ECT_REAL64 -> "REAL64";
                case ECT_BIT1 -> "BIT1";
                case ECT_BIT2 -> "BIT2";
                case ECT_BIT3 -> "BIT3";
                case ECT_BIT4 -> "BIT4";
                case ECT_BIT5 -> "BIT5";
                case ECT_BIT6 -> "BIT6";
                case ECT_BIT7 -> "BIT7";
                case ECT_BIT8 -> "BIT8";
                case ECT_VISIBLE_STRING -> String.format("VISIBLE_STR(%d)", bitlen);
                case ECT_OCTET_STRING -> String.format("OCTET_STR(%d)", bitlen);
                default -> String.format("dt:0x%04X (%d)", edtype, bitlen);
            };
        } catch (IllegalArgumentException ex) {
            return String.format("dt:0x%04X (%d)", dtype, bitlen);
        }
    }

    /**
     * the name of the type of an object of the dictionary of a slave
     *
     * @param otype number of the object type of SOEM
     * @return the name of the type
     */
    static String otype2string(int otype) {
        try {
            ec_objecttype eotype = (ec_objecttype) EnumMapper.getInstance(ec_objecttype.class).valueOf(otype);
            return switch (eotype) {
                case OTYPE_VAR -> "VAR";
                case OTYPE_ARRAY -> "ARRAY";
                case OTYPE_RECORD -> "RECORD";
                default -> String.format("ot:0x%04X", eotype);
            };
        } catch (IllegalArgumentException ex) {
            return String.format("ot:0x%04X", otype);
        }
    }

    /**
     * the access of an entry of the dictionary of a slave : one letter for every
     * state of the slave, R or W when the entry is readable or writable in that
     * state, _ when it is not
     *
     * @param access bit mask of the access of SOEM
     * @return the access of the entry
     */
    static String access2string(int access) {
        return String.format("%s%s%s%s%s%s",
                (access & ec_accesstype.ATYPE_Rpre.intValue()) != 0 ? "R" : "_",
                (access & ec_accesstype.ATYPE_Wpre.intValue()) != 0 ? "W" : "_",
                (access & ec_accesstype.ATYPE_Rsafe.intValue()) != 0 ? "R" : "_",
                (access & ec_accesstype.ATYPE_Wsafe.intValue()) != 0 ? "W" : "_",
                (access & ec_accesstype.ATYPE_Rop.intValue()) != 0 ? "R" : "_",
                (access & ec_accesstype.ATYPE_Wop.intValue()) != 0 ? "W" : "_");
    }
}
