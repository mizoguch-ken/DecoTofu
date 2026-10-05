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
        String str;

        try {
            SoemEtherCATType.ec_datatype edtype = (SoemEtherCATType.ec_datatype) EnumMapper
                    .getInstance(SoemEtherCATType.ec_datatype.class).valueOf(dtype);
            switch (edtype) {
                case ECT_BOOLEAN:
                    str = "BOOLEAN";
                    break;
                case ECT_INTEGER8:
                    str = "INTEGER8";
                    break;
                case ECT_INTEGER16:
                    str = "INTEGER16";
                    break;
                case ECT_INTEGER32:
                    str = "INTEGER32";
                    break;
                case ECT_INTEGER24:
                    str = "INTEGER24";
                    break;
                case ECT_INTEGER64:
                    str = "INTEGER64";
                    break;
                case ECT_UNSIGNED8:
                    str = "UNSIGNED8";
                    break;
                case ECT_UNSIGNED16:
                    str = "UNSIGNED16";
                    break;
                case ECT_UNSIGNED32:
                    str = "UNSIGNED32";
                    break;
                case ECT_UNSIGNED24:
                    str = "UNSIGNED24";
                    break;
                case ECT_UNSIGNED64:
                    str = "UNSIGNED64";
                    break;
                case ECT_REAL32:
                    str = "REAL32";
                    break;
                case ECT_REAL64:
                    str = "REAL64";
                    break;
                case ECT_BIT1:
                    str = "BIT1";
                    break;
                case ECT_BIT2:
                    str = "BIT2";
                    break;
                case ECT_BIT3:
                    str = "BIT3";
                    break;
                case ECT_BIT4:
                    str = "BIT4";
                    break;
                case ECT_BIT5:
                    str = "BIT5";
                    break;
                case ECT_BIT6:
                    str = "BIT6";
                    break;
                case ECT_BIT7:
                    str = "BIT7";
                    break;
                case ECT_BIT8:
                    str = "BIT8";
                    break;
                case ECT_VISIBLE_STRING:
                    str = String.format("VISIBLE_STR(%d)", bitlen);
                    break;
                case ECT_OCTET_STRING:
                    str = String.format("OCTET_STR(%d)", bitlen);
                    break;
                default:
                    str = String.format("dt:0x%04X (%d)", edtype, bitlen);
            }
        } catch (IllegalArgumentException ex) {
            str = String.format("dt:0x%04X (%d)", dtype, bitlen);
        }
        return str;
    }

    /**
     * the name of the type of an object of the dictionary of a slave
     *
     * @param otype number of the object type of SOEM
     * @return the name of the type
     */
    static String otype2string(int otype) {
        String str;

        try {
            ec_objecttype eotype = (ec_objecttype) EnumMapper.getInstance(ec_objecttype.class).valueOf(otype);
            switch (eotype) {
                case OTYPE_VAR:
                    str = "VAR";
                    break;
                case OTYPE_ARRAY:
                    str = "ARRAY";
                    break;
                case OTYPE_RECORD:
                    str = "RECORD";
                    break;
                default:
                    str = String.format("ot:0x%04X", eotype);
            }
        } catch (IllegalArgumentException ex) {
            str = String.format("ot:0x%04X", otype);
        }
        return str;
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
