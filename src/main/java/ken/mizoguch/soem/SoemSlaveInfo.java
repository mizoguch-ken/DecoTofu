/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ken.mizoguch.soem;

import java.util.ArrayList;
import java.util.List;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.util.EnumMapper;
import ken.mizoguch.console.Console;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT1;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT2;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT3;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT4;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT5;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT6;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT7;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BIT8;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_BOOLEAN;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_INTEGER16;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_INTEGER24;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_INTEGER32;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_INTEGER64;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_INTEGER8;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_OCTET_STRING;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_REAL32;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_REAL64;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_UNSIGNED16;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_UNSIGNED24;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_UNSIGNED32;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_UNSIGNED64;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_UNSIGNED8;
import static ken.mizoguch.soem.SoemEtherCATType.ec_datatype.ECT_VISIBLE_STRING;

/**
 *
 * @author mizoguch-ken
 */
public class SoemSlaveInfo {

    private SoemLibrary soem_;
    private jnr.ffi.Runtime runtime_;

    private SoemEtherCATMain.ecx_contextt context_;

    private Pointer pIOmap_;

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

    public class SlaveInfo {

        public class SM {

            Integer Index;
            Integer StartAddress;
            Integer SMLength;
            Long SMFlags;
            Short SMType;

        }

        public class FMMU {

            Integer Index;
            Long LogStart;
            Integer LogLength;
            Short LogStartbit;
            Short LogEndbit;
            Integer PhysStart;
            Short PhysStartBit;
            Short FMMUType;
            Short FMMUActive;
        }

        public class PDO {

            public class SII {

                Integer AbsOffset;
                Integer AbsBit;
                Integer ObjectIndex;
                Integer ObjectSubIndex;
                Integer BitLength;
                String ObjectDataType;
                String Name;
            }

            Integer SyncM;
            Integer Index;
            String Name;
            List<SII> SIIlist;

            public SII newSII() {
                SII sii = new SII();
                if (SIIlist == null) {
                    SIIlist = new ArrayList<>();
                }
                SIIlist.add(sii);
                return sii;
            }
        }

        public class SDO {

            public class OD {

                public class OE {

                    Integer Index;
                    Integer DataType;
                    Integer BitLength;
                    String DataTypeName;
                    Integer ObjectAccess;
                    String AccessType;
                    String Name;
                    String SDO;
                }

                Integer Entries;
                Integer Index;
                Integer DataType;
                Short ObjectCode;
                String DataTypeName;
                Short MaxSub;
                String Name;
                List<OE> OElist;
                String EcatError;

                public OE newOE() {
                    OE oe = new OE();
                    if (OElist == null) {
                        OElist = new ArrayList<>();
                    }
                    OElist.add(oe);
                    return oe;
                }
            }

            Integer ODlistEntries;
            List<OD> ODlist;
            String EcatError;

            public OD newOD() {
                OD od = new OD();
                if (ODlist == null) {
                    ODlist = new ArrayList<>();
                }
                ODlist.add(od);
                return od;
            }
        }

        public class Slave {

            Integer Index;
            String Name;
            Integer OutputBits;
            Integer InputBits;
            Integer State;
            Integer ALStatusCode;
            String ALStatusCodeString;
            Integer PropagationDelay;
            Short HasDC;
            Short ParentPort;
            Short ActivePorts;
            Integer ConfigAddress;
            Long EEP_MAN;
            Long EEP_ID;
            Long EEP_REV;
            List<SM> SMlist;
            List<FMMU> FMMUlist;
            Short FMMU0Func;
            Short FMMU1Func;
            Short FMMU2Func;
            Short FMMU3Func;
            Integer MailboxWriteLength;
            Integer MailboxReadLength;
            Integer MailboxProtocols;
            Short CoEDetails;
            Short FoEDetails;
            Short EoEDetails;
            Short SoEDetails;
            Short EbusCurrent;
            Short BlockLRW;
            List<PDO> RxPDOlist;
            List<PDO> TxPDOlist;
            SDO SDO;
            String EcatError;

            public SM newSM() {
                SM sm = new SM();
                if (SMlist == null) {
                    SMlist = new ArrayList<>();
                }
                SMlist.add(sm);
                return sm;
            }

            public FMMU newFMMU() {
                FMMU fmmu = new FMMU();
                if (FMMUlist == null) {
                    FMMUlist = new ArrayList<>();
                }
                FMMUlist.add(fmmu);
                return fmmu;
            }

            public PDO newRxPDO() {
                PDO pdo = new PDO();
                if (RxPDOlist == null) {
                    RxPDOlist = new ArrayList<>();
                }
                RxPDOlist.add(pdo);
                return pdo;
            }

            public PDO newTxPDO() {
                PDO pdo = new PDO();
                if (TxPDOlist == null) {
                    TxPDOlist = new ArrayList<>();
                }
                TxPDOlist.add(pdo);
                return pdo;
            }

            public SDO newSDO() {
                SDO sdo = new SDO();
                SDO = sdo;
                return sdo;
            }
        }

        String InterfaceName;
        int SlaveCount;
        int ExpectedWKC;
        List<Slave> Slavelist;
        String EcatError;

        public Slave newSlave() {
            Slave slave = new Slave();
            if (Slavelist == null) {
                Slavelist = new ArrayList<>();
            }
            Slavelist.add(slave);
            return slave;
        }
    }

    public SoemSlaveInfo(SoemLibrary soem, jnr.ffi.Runtime runtime, SoemEtherCATMain.ecx_contextt context, Pointer pIOmap) {
        soem_ = soem;
        runtime_ = runtime;
        context_ = context;
        pIOmap_ = pIOmap;
    }

    public SlaveInfo info(boolean printSDO, boolean printMAP) {
        if (context_ != null) {
            try {
                int ssigen, nSM, cnt, cnt2;

                SlaveInfo info = new SlaveInfo();
                SlaveInfo.Slave slave;
                SlaveInfo.SM sm;
                SlaveInfo.FMMU fmmu;

                if (context_.ecaterror.get() > 0) {
                    info.EcatError = soem_.ecx_elist2string(context_);
                }
                info.SlaveCount = context_.slavecount.get();
                info.ExpectedWKC = (context_.grouplist[0].outputsWKC.get() * 2) + context_.grouplist[0].inputsWKC.get();

                soem_.ecx_readstate(context_);
                for (cnt = 1; cnt <= context_.slavecount.get(); cnt++) {
                    slave = info.newSlave();
                    slave.Index = cnt;
                    slave.Name = context_.slavelist[cnt].name.get();
                    slave.OutputBits = context_.slavelist[cnt].Obits.get();
                    slave.InputBits = context_.slavelist[cnt].Ibits.get();
                    slave.State = context_.slavelist[cnt].state.get();
                    slave.ALStatusCode = context_.slavelist[cnt].ALstatuscode.get();
                    slave.ALStatusCodeString = soem_.ec_ALstatuscode2string(context_.slavelist[cnt].ALstatuscode.get());
                    slave.PropagationDelay = context_.slavelist[cnt].pdelay.get();
                    slave.HasDC = context_.slavelist[cnt].hasdc.get();
                    if (context_.slavelist[cnt].hasdc.get() > 0) {
                        slave.ParentPort = context_.slavelist[cnt].parentport.get();
                    }
                    slave.ActivePorts = context_.slavelist[cnt].activeports.get();
                    slave.ConfigAddress = context_.slavelist[cnt].configadr.get();
                    slave.EEP_MAN = context_.slavelist[cnt].eep_man.get();
                    slave.EEP_ID = context_.slavelist[cnt].eep_id.get();
                    slave.EEP_REV = context_.slavelist[cnt].eep_rev.get();
                    for (nSM = 0; nSM < SoemEtherCATMain.EC_MAXSM; nSM++) {
                        if (context_.slavelist[cnt].SM[nSM].StartAddr.get() > 0) {
                            sm = slave.newSM();
                            sm.Index = nSM;
                            sm.StartAddress = context_.slavelist[cnt].SM[nSM].StartAddr.get();
                            sm.SMLength = context_.slavelist[cnt].SM[nSM].SMlength.get();
                            sm.SMFlags = context_.slavelist[cnt].SM[nSM].SMflags.get();
                            sm.SMType = context_.slavelist[cnt].SMtype[nSM].get();
                        }
                    }
                    for (cnt2 = 0; cnt2 < context_.slavelist[cnt].FMMUunused.get(); cnt2++) {
                        fmmu = slave.newFMMU();
                        fmmu.Index = cnt2;
                        fmmu.LogStart = context_.slavelist[cnt].FMMU[cnt2].LogStart.get();
                        fmmu.LogLength = context_.slavelist[cnt].FMMU[cnt2].LogLength.get();
                        fmmu.LogStartbit = context_.slavelist[cnt].FMMU[cnt2].LogStartbit.get();
                        fmmu.LogEndbit = context_.slavelist[cnt].FMMU[cnt2].LogEndbit.get();
                        fmmu.PhysStart = context_.slavelist[cnt].FMMU[cnt2].PhysStart.get();
                        fmmu.PhysStartBit = context_.slavelist[cnt].FMMU[cnt2].PhysStartBit.get();
                        fmmu.FMMUType = context_.slavelist[cnt].FMMU[cnt2].FMMUtype.get();
                        fmmu.FMMUActive = context_.slavelist[cnt].FMMU[cnt2].FMMUactive.get();
                    }
                    slave.FMMU0Func = context_.slavelist[cnt].FMMU0func.get();
                    slave.FMMU1Func = context_.slavelist[cnt].FMMU1func.get();
                    slave.FMMU2Func = context_.slavelist[cnt].FMMU2func.get();
                    slave.FMMU3Func = context_.slavelist[cnt].FMMU3func.get();
                    slave.MailboxWriteLength = context_.slavelist[cnt].mbx_l.get();
                    slave.MailboxReadLength = context_.slavelist[cnt].mbx_rl.get();
                    slave.MailboxProtocols = context_.slavelist[cnt].mbx_proto.get();
                    ssigen = soem_.ecx_siifind(context_, cnt, SoemEtherCATType.ECT_SII_GENERAL);
                    if (ssigen > 0) {
                        context_.slavelist[cnt].CoEdetails.set(soem_.ecx_siigetbyte(context_, cnt, ssigen + 0x07));
                        context_.slavelist[cnt].FoEdetails.set(soem_.ecx_siigetbyte(context_, cnt, ssigen + 0x08));
                        context_.slavelist[cnt].EoEdetails.set(soem_.ecx_siigetbyte(context_, cnt, ssigen + 0x09));
                        context_.slavelist[cnt].SoEdetails.set(soem_.ecx_siigetbyte(context_, cnt, ssigen + 0x0a));
                        if ((soem_.ecx_siigetbyte(context_, cnt, ssigen + 0x0d) & 0x02) > 0) {
                            context_.slavelist[cnt].blockLRW.set(1);
                            context_.slavelist[0].blockLRW.set(context_.slavelist[0].blockLRW.get() + 1);
                        }
                        context_.slavelist[cnt].Ebuscurrent.set(soem_.ecx_siigetbyte(context_, cnt, ssigen + 0x0e));
                        context_.slavelist[cnt].Ebuscurrent.set(context_.slavelist[cnt].Ebuscurrent.get() + (soem_.ecx_siigetbyte(context_, cnt, ssigen + 0x0f) << 8));
                        context_.slavelist[0].Ebuscurrent.set(context_.slavelist[0].Ebuscurrent.get() + context_.slavelist[cnt].Ebuscurrent.get());
                        slave.CoEDetails = context_.slavelist[cnt].CoEdetails.get();
                        slave.FoEDetails = context_.slavelist[cnt].FoEdetails.get();
                        slave.EoEDetails = context_.slavelist[cnt].EoEdetails.get();
                        slave.SoEDetails = context_.slavelist[cnt].SoEdetails.get();
                        slave.EbusCurrent = context_.slavelist[cnt].Ebuscurrent.get();
                        slave.BlockLRW = context_.slavelist[cnt].blockLRW.get();
                        if (((context_.slavelist[cnt].mbx_proto.get() & SoemEtherCATMain.ECT_MBXPROT_COE) > 0) && printSDO) {
                            si_sdo(context_, cnt, slave.newSDO());
                        }
                        if (printMAP) {
                            if ((context_.slavelist[cnt].mbx_proto.get() & SoemEtherCATMain.ECT_MBXPROT_COE) > 0) {
                                si_map_sdo(context_, pIOmap_, cnt, slave);
                            } else {
                                si_map_sii(context_, pIOmap_, cnt, slave);
                            }
                        }
                    }
                }
                return info;
            } catch (ClassCastException | IllegalArgumentException | IndexOutOfBoundsException | NullPointerException | StackOverflowError | UnsatisfiedLinkError ex) {
                Console.writeStackTrace(SoemSlaveInfo.class.getName(), ex);
            }
        }
        return null;
    }

    private String dtype2string(int dtype, int bitlen) {
        String str;

        try {
            SoemEtherCATType.ec_datatype edtype = (SoemEtherCATType.ec_datatype) EnumMapper.getInstance(SoemEtherCATType.ec_datatype.class).valueOf(dtype);
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

    private String otype2string(int otype) {
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

    private String access2string(int access) {
        String str;

        str = String.format("%s%s%s%s%s%s",
                (access & ec_accesstype.ATYPE_Rpre.intValue()) != 0 ? "R" : "_",
                (access & ec_accesstype.ATYPE_Wpre.intValue()) != 0 ? "W" : "_",
                (access & ec_accesstype.ATYPE_Rsafe.intValue()) != 0 ? "R" : "_",
                (access & ec_accesstype.ATYPE_Wsafe.intValue()) != 0 ? "W" : "_",
                (access & ec_accesstype.ATYPE_Rop.intValue()) != 0 ? "R" : "_",
                (access & ec_accesstype.ATYPE_Wop.intValue()) != 0 ? "W" : "_");
        return str;
    }

    private String SDO2string(SoemEtherCATMain.ecx_contextt context, int index, int idx, int subidx, int dtype) {
        StringBuilder str;
        SoemLibrary.UTF8String usdo = new SoemLibrary.UTF8String(runtime_, 128);
        SoemLibrary.Int32 l = new SoemLibrary.Int32(runtime_);
        Pointer pUsdo = Memory.allocate(runtime_, Struct.size(usdo));
        Pointer pL = Memory.allocate(runtime_, Struct.size(l));
        usdo.useMemory(pUsdo);
        l.useMemory(pL);
        l.set(Struct.size(usdo));

        soem_.ecx_SDOread(context, index, idx, subidx, SoemOsal.FALSE, pL, pUsdo, SoemEtherCATType.EC_TIMEOUTRXM);
        if (context.ecaterror.get() > 0) {
            return soem_.ecx_elist2string(context);
        } else {
            try {
                SoemEtherCATType.ec_datatype edtype = (SoemEtherCATType.ec_datatype) EnumMapper.getInstance(SoemEtherCATType.ec_datatype.class).valueOf(dtype);
                switch (edtype) {
                    case ECT_BOOLEAN:
                        if (pUsdo.getByte(0) == SoemOsal.TRUE) {
                            str = new StringBuilder("TRUE");
                        } else {
                            str = new StringBuilder("FALSE");
                        }
                        break;
                    case ECT_INTEGER8:
                        str = new StringBuilder(String.format("0x%02X / %d", pUsdo.getByte(0), pUsdo.getByte(0)));
                        break;
                    case ECT_INTEGER16:
                        str = new StringBuilder(String.format("0x%04X / %d", pUsdo.getShort(0), pUsdo.getShort(0)));
                        break;
                    case ECT_INTEGER32:
                    case ECT_INTEGER24:
                        str = new StringBuilder(String.format("0x%08X / %d", pUsdo.getInt(0), pUsdo.getInt(0)));
                        break;
                    case ECT_INTEGER64:
                        str = new StringBuilder(String.format("0x%016X / %d", pUsdo.getLong(0), pUsdo.getLong(0)));
                        break;
                    case ECT_UNSIGNED8:
                        str = new StringBuilder(String.format("0x%02X / %d", pUsdo.getShort(0), pUsdo.getShort(0)));
                        break;
                    case ECT_UNSIGNED16:
                        str = new StringBuilder(String.format("0x%04X / %d", pUsdo.getInt(0), pUsdo.getInt(0)));
                        break;
                    case ECT_UNSIGNED32:
                    case ECT_UNSIGNED24:
                        str = new StringBuilder(String.format("0x%08X / %d", pUsdo.getLong(0), pUsdo.getLong(0)));
                        break;
                    case ECT_UNSIGNED64:
                        str = new StringBuilder(String.format("0x%016X / %d", pUsdo.getLongLong(0), pUsdo.getLongLong(0)));
                        break;
                    case ECT_REAL32:
                        str = new StringBuilder(String.format("%f", pUsdo.getFloat(0)));
                        break;
                    case ECT_REAL64:
                        str = new StringBuilder(String.format("%f", pUsdo.getDouble(0)));
                        break;
                    case ECT_BIT1:
                    case ECT_BIT2:
                    case ECT_BIT3:
                    case ECT_BIT4:
                    case ECT_BIT5:
                    case ECT_BIT6:
                    case ECT_BIT7:
                    case ECT_BIT8:
                        str = new StringBuilder(String.format("0x%X", pUsdo.getShort(0)));
                        break;
                    case ECT_VISIBLE_STRING:
                        str = new StringBuilder(usdo.get());
                        break;
                    case ECT_OCTET_STRING:
                        str = new StringBuilder();
                        for (int i = 0; i < l.get(); i++) {
                            str.append(String.format("0x%02X ", pUsdo.getByte(i)));
                        }
                        break;
                    default:
                        str = new StringBuilder("Unknown type");
                }
            } catch (IllegalArgumentException ex) {
                str = new StringBuilder("Unknown type");
            }
            return str.toString();
        }
    }

    private int si_PDOassign(SoemEtherCATMain.ecx_contextt context, int index, int PDOassign, int mapoffset, int bitoffset, SlaveInfo.PDO pdo) {
        SlaveInfo.PDO.SII sii;
        int idxloop, nidx, subidxloop, idx, subidx;
        SoemLibrary.Uint16 rdat;
        SoemLibrary.Uint8 subcnt;
        int wkc, bsize = 0;
        SoemLibrary.Int32 rdl, rdat2;
        int bitlen, obj_subidx;
        int obj_idx;
        int abs_offset, abs_bit;
        SoemEtherCATCoE.ec_ODlistt ODlist = new SoemEtherCATCoE.ec_ODlistt(runtime_);
        SoemEtherCATCoE.ec_OElistt OElist = new SoemEtherCATCoE.ec_OElistt(runtime_);

        rdat = new SoemLibrary.Uint16(runtime_);
        subcnt = new SoemLibrary.Uint8(runtime_);
        rdl = new SoemLibrary.Int32(runtime_);
        rdat2 = new SoemLibrary.Int32(runtime_);

        Pointer pRdat = Memory.allocate(runtime_, Struct.size(rdat));
        Pointer pSubcnt = Memory.allocate(runtime_, Struct.size(subcnt));
        Pointer pRdl = Memory.allocate(runtime_, Struct.size(rdl));
        Pointer pRdat2 = Memory.allocate(runtime_, Struct.size(rdat2));
        Pointer pODlist = Memory.allocate(runtime_, Struct.size(ODlist));
        Pointer pOElist = Memory.allocate(runtime_, Struct.size(OElist));

        rdat.useMemory(pRdat);
        subcnt.useMemory(pSubcnt);
        rdl.useMemory(pRdl);
        rdat2.useMemory(pRdat2);
        ODlist.useMemory(pODlist);
        OElist.useMemory(pOElist);

        rdl.set(Struct.size(rdat));
        rdat.set(0);
        /* read PDO assign subindex 0 ( = number of PDO's) */
        wkc = soem_.ecx_SDOread(context, index, PDOassign, 0x00, SoemOsal.FALSE, pRdl, pRdat, SoemEtherCATType.EC_TIMEOUTRXM);
        /* positive result from slave ? */
        if ((wkc > 0) && (rdat.get() > 0)) {
            /* number of available sub indexes */
            nidx = rdat.get();
            bsize = 0;
            /* read all PDO's */
            for (idxloop = 1; idxloop <= nidx; idxloop++) {
                rdl.set(Struct.size(rdat));
                rdat.set(0);
                /* read PDO assign */
                soem_.ecx_SDOread(context, index, PDOassign, idxloop, SoemOsal.FALSE, pRdl, pRdat, SoemEtherCATType.EC_TIMEOUTRXM);
                /* result is index of PDO */
                idx = rdat.get();
                if (idx > 0) {
                    rdl.set(Struct.size(subcnt));
                    subcnt.set(0);
                    /* read number of subindexes of PDO */
                    soem_.ecx_SDOread(context, index, idx, 0x00, SoemOsal.FALSE, pRdl, pSubcnt, SoemEtherCATType.EC_TIMEOUTRXM);
                    subidx = subcnt.get();
                    /* for each subindex */
                    for (subidxloop = 1; subidxloop <= subidx; subidxloop++) {
                        rdl.set(Struct.size(rdat2));
                        rdat2.set(0);
                        /* read SDO that is mapped in PDO */
                        soem_.ecx_SDOread(context, index, idx, subidxloop, SoemOsal.FALSE, pRdl, pRdat2, SoemEtherCATType.EC_TIMEOUTRXM);
                        /* extract bitlength of SDO */
                        bitlen = (rdat2.get() & 0xff);
                        bsize += bitlen;
                        obj_idx = (rdat2.get() >> 16);
                        obj_subidx = ((rdat2.get() >> 8) & 0x000000ff);
                        abs_offset = mapoffset + (bitoffset / 8);
                        abs_bit = bitoffset % 8;
                        ODlist.Slave.set(index);
                        ODlist.Index[0].set(obj_idx);
                        OElist.Entries.set(0);
                        wkc = 0;
                        /* read object entry from dictionary if not a filler (0x0000:0x00) */
                        if ((obj_idx > 0) || (obj_subidx > 0)) {
                            wkc = soem_.ecx_readOEsingle(context, 0, obj_subidx, ODlist, OElist);
                        }
                        sii = pdo.newSII();
                        sii.AbsOffset = abs_offset;
                        sii.AbsBit = abs_bit;
                        sii.ObjectIndex = obj_idx;
                        sii.ObjectSubIndex = obj_subidx;
                        sii.BitLength = bitlen;
                        if ((wkc > 0) && (OElist.Entries.get() > 0)) {
                            sii.ObjectDataType = dtype2string(OElist.DataType[obj_subidx].get(), bitlen);
                            sii.Name = OElist.Name[obj_subidx].get();
                        }
                        bitoffset += bitlen;
                    }
                }
            }
        }
        /* return total found bitlength (PDO) */
        return bsize;
    }

    private int si_map_sdo(SoemEtherCATMain.ecx_contextt context, Pointer pIOmap, int index, SlaveInfo.Slave slave) {
        SlaveInfo.PDO pdo;
        int wkc;
        SoemLibrary.Int32 rdl;
        int retVal = 0;
        SoemLibrary.Uint8 nSM, tSM;
        int iSM;
        int Tsize, outputs_bo, inputs_bo;
        int SMt_bug_add;

        rdl = new SoemLibrary.Int32(runtime_);
        nSM = new SoemLibrary.Uint8(runtime_);
        tSM = new SoemLibrary.Uint8(runtime_);

        Pointer pRdl = Memory.allocate(runtime_, Struct.size(rdl));
        Pointer pNSM = Memory.allocate(runtime_, Struct.size(nSM));
        Pointer pTSM = Memory.allocate(runtime_, Struct.size(tSM));

        rdl.useMemory(pRdl);
        nSM.useMemory(pNSM);
        tSM.useMemory(pTSM);

        SMt_bug_add = 0;
        outputs_bo = 0;
        inputs_bo = 0;
        rdl.set(Struct.size(nSM));
        nSM.set(0);
        /* read SyncManager Communication Type object count */
        wkc = soem_.ecx_SDOread(context, index, SoemEtherCATType.ECT_SDO_SMCOMMTYPE, 0x00, SoemOsal.FALSE, pRdl, pNSM, SoemEtherCATType.EC_TIMEOUTRXM);
        /* positive result from slave ? */
        if ((wkc > 0) && (nSM.get() > 2)) {
            /* make nSM equal to number of defined SM */
            nSM.set(nSM.get() - 1);
            /* limit to maximum number of SM defined, if true the slave can't be configured */
            if (nSM.get() > SoemEtherCATMain.EC_MAXSM) {
                nSM.set(SoemEtherCATMain.EC_MAXSM);
            }
            /* iterate for every SM type defined */
            for (iSM = 2; iSM <= nSM.get(); iSM++) {
                rdl.set(Struct.size(tSM));
                tSM.set(0);
                /* read SyncManager Communication Type */
                wkc = soem_.ecx_SDOread(context, index, SoemEtherCATType.ECT_SDO_SMCOMMTYPE, iSM + 1, SoemOsal.FALSE, pRdl, pTSM, SoemEtherCATType.EC_TIMEOUTRXM);
                if (wkc > 0) {
                    if ((iSM == 2) && (tSM.get() == 2)) // SM2 has type 2 == mailbox out, this is a bug in the slave!
                    {
                        SMt_bug_add = 1; // try to correct, this works if the types are 0 1 2 3 and should be 1 2 3 4
                    }
                    if (tSM.get() > 0) {
                        tSM.set(tSM.get() + SMt_bug_add); // only add if SMt > 0
                    }
                    if (tSM.get() == 3) // outputs
                    {
                        /* read the assign RXPDO */
                        pdo = slave.newRxPDO();
                        pdo.Index = iSM;
                        if (context.slavelist[index].outputs.get() != null) {
                            Tsize = si_PDOassign(context, index, SoemEtherCATType.ECT_SDO_PDOASSIGN + iSM, (int) (context.slavelist[index].outputs.get().address() - pIOmap.address()), outputs_bo, pdo);
                            outputs_bo += Tsize;
                        }
                    }
                    if (tSM.get() == 4) // inputs
                    {
                        /* read the assign TXPDO */
                        pdo = slave.newTxPDO();
                        pdo.Index = iSM;
                        if (context.slavelist[index].inputs.get() != null) {
                            Tsize = si_PDOassign(context, index, SoemEtherCATType.ECT_SDO_PDOASSIGN + iSM, (int) (context.slavelist[index].inputs.get().address() - pIOmap.address()), inputs_bo, pdo);
                            inputs_bo += Tsize;
                        }
                    }
                }
            }
        }

        /* found some I/O bits ? */
        if ((outputs_bo > 0) || (inputs_bo > 0)) {
            retVal = 1;
        }
        return retVal;
    }

    private int si_siiPDO(SoemEtherCATMain.ecx_contextt context, int index, int t, int mapoffset, int bitoffset, SlaveInfo.Slave slave) {
        SlaveInfo.PDO pdo;
        SlaveInfo.PDO.SII sii;
        int a, w, c, e, er;
        int eectl;
        int obj_idx;
        int obj_subidx;
        int obj_name;
        int obj_datatype;
        int bitlen;
        int totalsize;
        SoemEtherCATMain.ec_eepromPDOt PDO = new SoemEtherCATMain.ec_eepromPDOt(runtime_);
        Pointer pPDO = Memory.allocate(runtime_, Struct.size(PDO));
        PDO.useMemory(pPDO);
        int abs_offset, abs_bit;
        SoemLibrary.UTF8String str_name = new SoemLibrary.UTF8String(runtime_, SoemEtherCATMain.EC_MAXNAME + 1);
        Pointer pStr_name = Memory.allocate(runtime_, Struct.size(str_name));
        str_name.useMemory(pStr_name);

        eectl = context.slavelist[index].eep_pdi.get();
        totalsize = 0;
        PDO.nPDO.set(0);
        PDO.Length.set(0);
        PDO.Index[1].set(0);
        for (c = 0; c < SoemEtherCATMain.EC_MAXSM; c++) {
            PDO.SMbitsize[c].set(0);
        }
        if (t > 1) {
            t = 1;
        }
        PDO.Startpos.set(soem_.ecx_siifind(context, index, SoemEtherCATType.ECT_SII_PDO + t));
        if (PDO.Startpos.get() > 0) {
            a = PDO.Startpos.get();
            w = soem_.ecx_siigetbyte(context, index, a++);
            w += (soem_.ecx_siigetbyte(context, index, a++) << 8);
            PDO.Length.set(w);
            c = 1;
            /* traverse through all PDOs */
            do {
                PDO.nPDO.set(PDO.nPDO.get() + 1);
                PDO.Index[PDO.nPDO.get()].set(soem_.ecx_siigetbyte(context, index, a++));
                PDO.Index[PDO.nPDO.get()].set(PDO.Index[PDO.nPDO.get()].get() + (soem_.ecx_siigetbyte(context, index, a++) << 8));
                PDO.BitSize[PDO.nPDO.get()].set(0);
                c++;
                /* number of entries in PDO */
                e = soem_.ecx_siigetbyte(context, index, a++);
                PDO.SyncM[PDO.nPDO.get()].set(soem_.ecx_siigetbyte(context, index, a++));
                a++;
                obj_name = soem_.ecx_siigetbyte(context, index, a++);
                a += 2;
                c += 2;
                if (PDO.SyncM[PDO.nPDO.get()].get() < SoemEtherCATMain.EC_MAXSM) /* active and in range SM? */ {
                    str_name.set("");
                    if (obj_name > 0) {
                        soem_.ecx_siistring(context, pStr_name, index, obj_name);
                    }
                    if (t > 0) {
                        pdo = slave.newRxPDO();
                        pdo.SyncM = PDO.SyncM[PDO.nPDO.get()].get();
                        pdo.Index = PDO.Index[PDO.nPDO.get()].get();
                        pdo.Name = str_name.get();
                    } else {
                        pdo = slave.newTxPDO();
                        pdo.SyncM = PDO.SyncM[PDO.nPDO.get()].get();
                        pdo.Index = PDO.Index[PDO.nPDO.get()].get();
                        pdo.Name = str_name.get();
                    }
                    /* read all entries defined in PDO */
                    for (er = 1; er <= e; er++) {
                        sii = pdo.newSII();
                        c += 4;
                        obj_idx = soem_.ecx_siigetbyte(context, index, a++);
                        obj_idx += (soem_.ecx_siigetbyte(context, index, a++) << 8);
                        obj_subidx = soem_.ecx_siigetbyte(context, index, a++);
                        obj_name = soem_.ecx_siigetbyte(context, index, a++);
                        obj_datatype = soem_.ecx_siigetbyte(context, index, a++);
                        bitlen = soem_.ecx_siigetbyte(context, index, a++);
                        abs_offset = mapoffset + (bitoffset / 8);
                        abs_bit = bitoffset % 8;
                        PDO.BitSize[PDO.nPDO.get()].set(PDO.BitSize[PDO.nPDO.get()].get() + bitlen);
                        a += 2;

                        str_name.set("");
                        if (obj_name > 0) {
                            soem_.ecx_siistring(context, pStr_name, index, obj_name);
                        }
                        sii.AbsOffset = abs_offset;
                        sii.AbsBit = abs_bit;
                        sii.ObjectIndex = obj_idx;
                        sii.ObjectSubIndex = obj_subidx;
                        sii.BitLength = bitlen;
                        sii.ObjectDataType = dtype2string(obj_datatype, bitlen);
                        sii.Name = str_name.get();
                        bitoffset += bitlen;
                        totalsize += bitlen;
                    }
                    PDO.SMbitsize[PDO.SyncM[PDO.nPDO.get()].get()].set(PDO.SMbitsize[PDO.SyncM[PDO.nPDO.get()].get()].get() + PDO.BitSize[PDO.nPDO.get()].get());
                    c++;
                } else /* PDO deactivated because SM is 0xff or > EC_MAXSM */ {
                    c += 4 * e;
                    a += 8 * e;
                    c++;
                }
                if (PDO.nPDO.get() >= (SoemEtherCATMain.EC_MAXEEPDO - 1)) {
                    c = PDO.Length.get();
                    /* limit number of PDO entries in buffer */
                }
            } while (c < PDO.Length.get());
        }
        if (eectl > 0) {
            soem_.ecx_eeprom2pdi(context, index);
            /* if eeprom control was previously pdi then restore */
        }
        return totalsize;
    }

    private int si_map_sii(SoemEtherCATMain.ecx_contextt context, Pointer pIOmap, int index, SlaveInfo.Slave slave) {
        int retVal = 0;
        int Tsize, outputs_bo, inputs_bo;
        long address;

        outputs_bo = 0;
        inputs_bo = 0;
        /* read the assign RXPDOs */
        if (context.slavelist[index].outputs.get() == null) {
            address = 0;
        } else {
            address = context.slavelist[index].outputs.get().address();
        }
        Tsize = si_siiPDO(context, index, 1, (int) (address - pIOmap.address()), outputs_bo, slave);
        outputs_bo += Tsize;
        /* read the assign TXPDOs */
        if (context.slavelist[index].inputs.get() == null) {
            address = 0;
        } else {
            address = context.slavelist[index].inputs.get().address();
        }
        Tsize = si_siiPDO(context, index, 0, (int) (address - pIOmap.address()), inputs_bo, slave);
        inputs_bo += Tsize;
        /* found some I/O bits ? */
        if ((outputs_bo > 0) || (inputs_bo > 0)) {
            retVal = 1;
        }
        return retVal;
    }

    private void si_sdo(SoemEtherCATMain.ecx_contextt context, int cnt, SlaveInfo.SDO sdo) {
        SlaveInfo.SDO.OD od;
        SlaveInfo.SDO.OD.OE oe;
        SoemEtherCATCoE.ec_ODlistt ODlist = new SoemEtherCATCoE.ec_ODlistt(runtime_);
        SoemEtherCATCoE.ec_OElistt OElist = new SoemEtherCATCoE.ec_OElistt(runtime_);
        SoemLibrary.Uint8 maxSub = new SoemLibrary.Uint8(runtime_);
        SoemLibrary.Int32 l = new SoemLibrary.Int32(runtime_);
        Pointer pMaxSub = Memory.allocate(runtime_, Struct.size(maxSub));
        Pointer pL = Memory.allocate(runtime_, Struct.size(l));
        maxSub.useMemory(pMaxSub);
        l.useMemory(pL);
        l.set(Struct.size(maxSub));
        int i, j;

        Pointer pODlist = Memory.allocate(runtime_, Struct.size(ODlist));
        ODlist.useMemory(pODlist);
        ODlist.Entries.set(0);
        if (soem_.ecx_readODlist(context, cnt, ODlist) > 0) {
            sdo.ODlistEntries = ODlist.Entries.get();
            for (i = 0; i < ODlist.Entries.get(); i++) {
                od = sdo.newOD();
                soem_.ecx_readODdescription(context, i, ODlist);
                if (context.ecaterror.get() > 0) {
                    od.EcatError = soem_.ecx_elist2string(context);
                }
                od.Index = ODlist.Index[i].get();
                od.DataType = ODlist.DataType[i].get();
                od.ObjectCode = ODlist.ObjectCode[i].get();
                od.DataTypeName = otype2string(od.ObjectCode);
                od.Name = ODlist.Name[i].toString();
                Pointer pOElist = Memory.allocate(runtime_, Struct.size(OElist));
                OElist.useMemory(pOElist);
                soem_.ecx_readOE(context, i, ODlist, OElist);
                if (context.ecaterror.get() > 0) {
                    od.EcatError = soem_.ecx_elist2string(context);
                }
                if (od.ObjectCode != ec_objecttype.OTYPE_VAR.intValue()) {
                    soem_.ecx_SDOread(context, cnt, od.Index, 0, SoemOsal.FALSE, pL, pMaxSub, SoemEtherCATType.EC_TIMEOUTRXM);
                    od.MaxSub = maxSub.get();
                } else {
                    od.MaxSub = ODlist.MaxSub[i].get();
                }
                for (j = 0; j < od.MaxSub + 1; j++) {
                    if ((OElist.DataType[j].get() > 0) && (OElist.BitLength[j].get() > 0)) {
                        oe = od.newOE();
                        oe.Index = j;
                        oe.DataType = OElist.DataType[j].get();
                        oe.BitLength = OElist.BitLength[j].get();
                        oe.DataTypeName = dtype2string(oe.DataType, oe.BitLength);
                        oe.ObjectAccess = OElist.ObjAccess[j].get();
                        oe.AccessType = access2string(oe.ObjectAccess);
                        oe.Name = OElist.Name[j].toString();
                        if ((OElist.ObjAccess[j].get() & 0x0007) > 0) {
                            oe.SDO = SDO2string(context, cnt, ODlist.Index[i].get(), j, OElist.DataType[j].get());
                        }
                    }
                }
            }
        } else {
            if (context.ecaterror.get() > 0) {
                sdo.EcatError = soem_.ecx_elist2string(context);
            }
        }
    }
}
