/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import jnr.ffi.NativeType;
import jnr.ffi.Platform;
import jnr.ffi.Struct;

/**
 *
 * @author mizoguch-ken
 */
public interface SoemEtherCATMain {

    /**
     * max. entries in EtherCAT error list
     */
    public static final int EC_MAXELIST = 64;
    /**
     * max. length of readable name in slavelist and Object Description List
     */
    public static final int EC_MAXNAME = 40;
    /**
     * max. number of slaves in array
     */
    public static final int EC_MAXSLAVE = 200;
    /**
     * max. number of groups
     */
    public static final int EC_MAXGROUP = 2;
    /**
     * max. number of IO segments per group
     */
    public static final int EC_MAXIOSEGMENTS = 64;
    /**
     * max. mailbox size
     */
    public static final int EC_MAXMBX = 1486;
    /**
     * max. number of entries in the mailbox pool and mailbox queue
     */
    public static final int EC_MBXPOOLSIZE = 32;
    /**
     * max. eeprom PDO entries
     */
    public static final int EC_MAXEEPDO = 0x200;
    /**
     * max. SM used
     */
    public static final int EC_MAXSM = 8;
    /**
     * max. FMMU used
     */
    public static final int EC_MAXFMMU = 4;
    /**
     * max. Adapter
     */
    public static final int EC_MAXLEN_ADAPTERNAME = 128;
    /**
     * define maximum number of concurrent threads in mapping
     */
    public static final int EC_MAX_MAPT = 1;

    public static final int EC_MBXQUEUESTATE_NONE = 0;
    public static final int EC_MBXQUEUESTATE_REQ = 1;
    public static final int EC_MBXQUEUESTATE_FAIL = 2;
    public static final int EC_MBXQUEUESTATE_DONE = 3;

    public static final int ECT_MBXH_NONE = 0;
    public static final int ECT_MBXH_CYCLIC = 1;
    public static final int ECT_MBXH_LOST = 2;

    public static final int ECT_ESMTRANS_IP = 0x0001;
    public static final int ECT_ESMTRANS_PS = 0x0002;
    public static final int ECT_ESMTRANS_PI = 0x0004;
    public static final int ECT_ESMTRANS_SP = 0x0008;
    public static final int ECT_ESMTRANS_SO = 0x0010;
    public static final int ECT_ESMTRANS_SI = 0x0020;
    public static final int ECT_ESMTRANS_OS = 0x0040;
    public static final int ECT_ESMTRANS_OP = 0x0080;
    public static final int ECT_ESMTRANS_OI = 0x0100;
    public static final int ECT_ESMTRANS_IB = 0x0200;
    public static final int ECT_ESMTRANS_BI = 0x0400;
    public static final int ECT_ESMTRANS_II = 0x0800;
    public static final int ECT_ESMTRANS_PP = 0x1000;
    public static final int ECT_ESMTRANS_SS = 0x2000;

    public class ec_adaptert extends Struct {

        public final String name;
        public final String desc;
        private final Pointer _next;

        public ec_adaptert(jnr.ffi.Runtime runtime) {
            super(runtime);

            name = new UTF8String(EC_MAXLEN_ADAPTERNAME);
            desc = new UTF8String(EC_MAXLEN_ADAPTERNAME);
            _next = new Pointer();
        }

        public ec_adaptert getNext(jnr.ffi.Runtime runtime) {
            jnr.ffi.Pointer pNext = _next.get();
            if (pNext == null) {
                return null;
            }
            ec_adaptert adapter = new ec_adaptert(runtime);
            adapter.useMemory(pNext);
            return adapter;
        }
    }

    /**
     * pointer structure to Tx and Rx stacks
     */
    public final class ec_stackT extends Struct {

        public final Pointer sock;
        public final Pointer txbuf;
        public final Pointer txbuflength;
        public final Pointer tempbuf;
        public final Pointer rxbuf;
        public final Pointer rxbufstat;
        public final Pointer rxsa;
        public final Unsigned64 rxcnt;

        public ec_stackT(jnr.ffi.Runtime runtime) {
            super(runtime);

            sock = new Pointer();
            txbuf = new Pointer();
            txbuflength = new Pointer();
            tempbuf = new Pointer();
            rxbuf = new Pointer();
            rxbufstat = new Pointer();
            rxsa = new Pointer();
            rxcnt = new Unsigned64();
        }
    }

    /**
     * platform mutex inside ecx_portt (CRITICAL_SECTION on Windows,
     * pthread_mutex_t on POSIX)
     */
    public final class ec_port_mutext extends Struct {

        @SuppressWarnings("unused")
        private final Pointer[] ptrs_;
        @SuppressWarnings("unused")
        private final Signed32[] ints_;

        public ec_port_mutext(jnr.ffi.Runtime runtime) {
            super(runtime);

            Platform platform = Platform.getNativePlatform();
            if (platform.getOS() == Platform.OS.WINDOWS) {
                // RTL_CRITICAL_SECTION: DebugInfo, LockCount, RecursionCount,
                // OwningThread, LockSemaphore, SpinCount (40 bytes on x64, 24 on x86)
                ptrs_ = super.array(new Pointer[4]);
                ints_ = super.array(new Signed32[2]);
            } else if (platform.getCPU() == Platform.CPU.AARCH64) {
                // glibc aarch64 pthread_mutex_t: 48 bytes, 8-byte aligned
                ptrs_ = super.array(new Pointer[6]);
                ints_ = null;
            } else if (runtime.addressSize() == 8) {
                // glibc x86_64 pthread_mutex_t: 40 bytes, 8-byte aligned
                ptrs_ = super.array(new Pointer[5]);
                ints_ = null;
            } else {
                // 32-bit pthread_mutex_t: 24 bytes, 4-byte aligned
                ptrs_ = super.array(new Pointer[6]);
                ints_ = null;
            }
        }
    }

    /**
     * pointer structure to buffers, vars and mutexes for port instantiation
     */
    public final class ecx_portt extends Struct {

        public final ec_stackT stack;
        public final Pointer sockhandle_win;
        public final Signed32 sockhandle_posix;
        public final Unsigned8[] rxbuf;
        public final Signed32[] rxbufstat;
        public final Signed32[] rxsa;
        public final Unsigned8[] tempinbuf;
        public final Signed32 tempinbufs;
        public final Unsigned8[] txbuf;
        public final Signed32[] txbuflength;
        public final Unsigned8[] txbuf2;
        public final Signed32 txbuflength2;
        public final Unsigned8 lastidx;
        public final Signed32 redstate;
        public final Pointer redport;
        public final ec_port_mutext getindex_mutex;
        public final ec_port_mutext tx_mutex;
        public final ec_port_mutext rx_mutex;

        public ecx_portt(jnr.ffi.Runtime runtime) {
            super(runtime);

            stack = inner(new ec_stackT(runtime));
            if (Platform.getNativePlatform().getOS() == Platform.OS.WINDOWS) {
                sockhandle_win = new Pointer();
                sockhandle_posix = null;
            } else {
                sockhandle_win = null;
                sockhandle_posix = new Signed32();
            }
            rxbuf = super.array(new Unsigned8[SoemEtherCATType.EC_MAXBUF * SoemEtherCATType.EC_BUFSIZE]);
            rxbufstat = super.array(new Signed32[SoemEtherCATType.EC_MAXBUF]);
            rxsa = super.array(new Signed32[SoemEtherCATType.EC_MAXBUF]);
            tempinbuf = super.array(new Unsigned8[SoemEtherCATType.EC_BUFSIZE]);
            tempinbufs = new Signed32();
            txbuf = super.array(new Unsigned8[SoemEtherCATType.EC_MAXBUF * SoemEtherCATType.EC_BUFSIZE]);
            txbuflength = super.array(new Signed32[SoemEtherCATType.EC_MAXBUF]);
            txbuf2 = super.array(new Unsigned8[SoemEtherCATType.EC_BUFSIZE]);
            txbuflength2 = new Signed32();
            lastidx = new Unsigned8();
            redstate = new Signed32();
            redport = new Pointer();
            getindex_mutex = inner(new ec_port_mutext(runtime));
            tx_mutex = inner(new ec_port_mutext(runtime));
            rx_mutex = inner(new ec_port_mutext(runtime));
        }
    }

    /**
     * record for FMMU
     */
    public class ec_fmmut extends Struct {

        public final Unsigned32 LogStart;
        public final Unsigned16 LogLength;
        public final Unsigned8 LogStartbit;
        public final Unsigned8 LogEndbit;
        public final Unsigned16 PhysStart;
        public final Unsigned8 PhysStartBit;
        public final Unsigned8 FMMUtype;
        public final Unsigned8 FMMUactive;
        public final Unsigned8 unused1;
        public final Unsigned16 unused2;

        public ec_fmmut(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            LogStart = new Unsigned32();
            LogLength = new Unsigned16();
            LogStartbit = new Unsigned8();
            LogEndbit = new Unsigned8();
            PhysStart = new Unsigned16();
            PhysStartBit = new Unsigned8();
            FMMUtype = new Unsigned8();
            FMMUactive = new Unsigned8();
            unused1 = new Unsigned8();
            unused2 = new Unsigned16();
        }
    }

    /**
     * record for sync manager
     */
    public class ec_smt extends Struct {

        public final Unsigned16 StartAddr;
        public final Unsigned16 SMlength;
        public final Unsigned32 SMflags;

        public ec_smt(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            StartAddr = new Unsigned16();
            SMlength = new Unsigned16();
            SMflags = new Unsigned32();
        }
    }

    public class ec_state_status extends Struct {

        public final Unsigned16 State;
        public final Unsigned16 Unused;
        public final Unsigned16 ALstatuscode;

        public ec_state_status(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            State = new Unsigned16();
            Unused = new Unsigned16();
            ALstatuscode = new Unsigned16();
        }
    }

    public static final int ECT_MBXPROT_AOE = 0x0001;
    public static final int ECT_MBXPROT_EOE = 0x0002;
    public static final int ECT_MBXPROT_COE = 0x0004;
    public static final int ECT_MBXPROT_FOE = 0x0008;
    public static final int ECT_MBXPROT_SOE = 0x0010;
    public static final int ECT_MBXPROT_VOE = 0x0020;

    public static final int ECT_COEDET_SDO = 0x01;
    public static final int ECT_COEDET_SDOINFO = 0x02;
    public static final int ECT_COEDET_PDOASSIGN = 0x04;
    public static final int ECT_COEDET_PDOCONFIG = 0x08;
    public static final int ECT_COEDET_UPLOAD = 0x10;
    public static final int ECT_COEDET_SDOCA = 0x20;

    public static final int EC_SMENABLEMASK = 0xfffeffff;

    /**
     * for list of ethercat slaves detected
     */
    public final class ec_slavet extends Struct {

        /**
         * state of slave
         */
        public final Unsigned16 state;
        /**
         * AL status code
         */
        public final Unsigned16 ALstatuscode;
        /**
         * Configured address
         */
        public final Unsigned16 configadr;
        /**
         * Alias address
         */
        public final Unsigned16 aliasadr;
        /**
         * Manufacturer from EEprom
         */
        public final Unsigned32 eep_man;
        /**
         * ID from EEprom
         */
        public final Unsigned32 eep_id;
        /**
         * revision from EEprom
         */
        public final Unsigned32 eep_rev;
        /**
         * serial number from EEprom
         */
        public final Unsigned32 eep_ser;
        /**
         * Interface type
         */
        public final Unsigned16 Itype;
        /**
         * Device type
         */
        public final Unsigned16 Dtype;
        /**
         * output bits
         */
        public final Unsigned16 Obits;
        /**
         * output bytes, if Obits < 8 then Obytes = 0
         */
        public final Unsigned32 Obytes;
        /**
         * output pointer in IOmap buffer
         */
        public final Pointer outputs;
        /**
         * output offset in IOmap buffer
         */
        public final Unsigned32 Ooffset;
        /**
         * startbit in first output byte
         */
        public final Unsigned8 Ostartbit;
        /**
         * input bits
         */
        public final Unsigned16 Ibits;
        /**
         * input bytes, if Ibits < 8 then Ibytes = 0
         */
        public final Unsigned32 Ibytes;
        /**
         * input pointer in IOmap buffer
         */
        public final Pointer inputs;
        /**
         * input offset in IOmap buffer
         */
        public final Unsigned32 Ioffset;
        /**
         * startbit in first input byte
         */
        public final Unsigned8 Istartbit;
        /**
         * SM structure
         */
        public final ec_smt[] SM;
        /**
         * SM type 0=unused 1=MbxWr 2=MbxRd 3=Outputs 4=Inputs
         */
        public final Unsigned8[] SMtype;
        /**
         * FMMU structure
         */
        public final ec_fmmut[] FMMU;
        /**
         * FMMU0 function
         */
        public final Unsigned8 FMMU0func;
        /**
         * FMMU1 function
         */
        public final Unsigned8 FMMU1func;
        /**
         * FMMU2 function
         */
        public final Unsigned8 FMMU2func;
        /**
         * FMMU3 function
         */
        public final Unsigned8 FMMU3func;
        /**
         * length of write mailbox in bytes, if no mailbox then 0
         */
        public final Unsigned16 mbx_l;
        /**
         * mailbox write offset
         */
        public final Unsigned16 mbx_wo;
        /**
         * length of read mailbox in bytes
         */
        public final Unsigned16 mbx_rl;
        /**
         * mailbox read offset
         */
        public final Unsigned16 mbx_ro;
        /**
         * mailbox supported protocols
         */
        public final Unsigned16 mbx_proto;
        /**
         * Counter value of mailbox link layer protocol 1..7
         */
        public final Unsigned8 mbx_cnt;
        /**
         * has DC capability
         */
        public final Unsigned8 hasdc;
        /**
         * Physical type; Ebus, EtherNet combinations
         */
        public final Unsigned8 ptype;
        /**
         * topology: 1 to 3 links
         */
        public final Unsigned8 topology;
        /**
         * active ports bitmap : ....3210 , set if respective port is active *
         */
        public final Unsigned8 activeports;
        /**
         * consumed ports bitmap : ....3210, used for internal delay measurement
         * *
         */
        public final Unsigned8 consumedports;
        /**
         * slave number for parent, 0=master
         */
        public final Unsigned16 parent;
        /**
         * port number on parent this slave is connected to *
         */
        public final Unsigned8 parentport;
        /**
         * port number on this slave the parent is connected to *
         */
        public final Unsigned8 entryport;
        /**
         * DC receivetimes on port A
         */
        public final Signed32 DCrtA;
        /**
         * DC receivetimes on port B
         */
        public final Signed32 DCrtB;
        /**
         * DC receivetimes on port C
         */
        public final Signed32 DCrtC;
        /**
         * DC receivetimes on port D
         */
        public final Signed32 DCrtD;
        /**
         * propagation delay
         */
        public final Signed32 pdelay;
        /**
         * next DC slave
         */
        public final Unsigned16 DCnext;
        /**
         * previous DC slave
         */
        public final Unsigned16 DCprevious;
        /**
         * DC cycle time in ns
         */
        public final Signed32 DCcycle;
        /**
         * DC shift from clock modulus boundary
         */
        public final Signed32 DCshift;
        /**
         * DC sync activation, 0=off, 1=on
         */
        public final Unsigned8 DCactive;
        /**
         * link to SII config
         */
        public final Unsigned16 SIIindex;
        /**
         * 1 = 8 bytes per read, 0 = 4 bytes per read
         */
        public final Unsigned8 eep_8byte;
        /**
         * 0 = eeprom to master , 1 = eeprom to PDI
         */
        public final Unsigned8 eep_pdi;
        /**
         * CoE details
         */
        public final Unsigned8 CoEdetails;
        /**
         * FoE details
         */
        public final Unsigned8 FoEdetails;
        /**
         * EoE details
         */
        public final Unsigned8 EoEdetails;
        /**
         * SoE details
         */
        public final Unsigned8 SoEdetails;
        /**
         * E-bus current
         */
        public final Signed16 Ebuscurrent;
        /**
         * if >0 block use of LRW in processdata
         */
        public final Unsigned8 blockLRW;
        /**
         * group
         */
        public final Unsigned8 group;
        /**
         * first unused FMMU
         */
        public final Unsigned8 FMMUunused;
        /**
         * Boolean for tracking whether the slave is (not) responding, not
         * used/set by the SOEM library
         */
        public final Unsigned8 islost;
        /**
         * registered configuration function PO->SO
         */
        public final SoemLibrary.CallBackPO2SOconfig PO2SOconfig;
        /**
         * mailbox handler state, 0 = no handler, 1 = cyclic task mbx handler,
         * 2 = slave lost
         */
        public final Signed32 mbxhandlerstate;
        /**
         * mailbox handler robust mailbox protocol state
         */
        public final Signed32 mbxrmpstate;
        /**
         * mailbox handler RMP extended mbx in state
         */
        public final Unsigned16 mbxinstateex;
        /**
         * pointer to CoE mailbox in buffer
         */
        public final Pointer coembxin;
        /**
         * CoE mailbox in flag, true = mailbox full
         */
        public final Unsigned8 coembxinfull;
        /**
         * CoE mailbox in overrun counter
         */
        public final Signed32 coembxoverrun;
        /**
         * pointer to SoE mailbox in buffer
         */
        public final Pointer soembxin;
        /**
         * SoE mailbox in flag, true = mailbox full
         */
        public final Unsigned8 soembxinfull;
        /**
         * SoE mailbox in overrun counter
         */
        public final Signed32 soembxoverrun;
        /**
         * pointer to FoE mailbox in buffer
         */
        public final Pointer foembxin;
        /**
         * FoE mailbox in flag, true = mailbox full
         */
        public final Unsigned8 foembxinfull;
        /**
         * FoE mailbox in overrun counter
         */
        public final Signed32 foembxoverrun;
        /**
         * pointer to EoE mailbox in buffer
         */
        public final Pointer eoembxin;
        /**
         * EoE mailbox in flag, true = mailbox full
         */
        public final Unsigned8 eoembxinfull;
        /**
         * EoE mailbox in overrun counter
         */
        public final Signed32 eoembxoverrun;
        /**
         * pointer to VoE mailbox in buffer
         */
        public final Pointer voembxin;
        /**
         * VoE mailbox in flag, true = mailbox full
         */
        public final Unsigned8 voembxinfull;
        /**
         * VoE mailbox in overrun counter
         */
        public final Signed32 voembxoverrun;
        /**
         * pointer to AoE mailbox in buffer
         */
        public final Pointer aoembxin;
        /**
         * AoE mailbox in flag, true = mailbox full
         */
        public final Unsigned8 aoembxinfull;
        /**
         * AoE mailbox in overrun counter
         */
        public final Signed32 aoembxoverrun;
        /**
         * pointer to out mailbox status register buffer
         */
        public final Pointer mbxstatus;
        /**
         * readable name
         */
        public String name;

        public ec_slavet(jnr.ffi.Runtime runtime) {
            super(runtime);

            state = new Unsigned16();
            ALstatuscode = new Unsigned16();
            configadr = new Unsigned16();
            aliasadr = new Unsigned16();
            eep_man = new Unsigned32();
            eep_id = new Unsigned32();
            eep_rev = new Unsigned32();
            eep_ser = new Unsigned32();
            Itype = new Unsigned16();
            Dtype = new Unsigned16();
            Obits = new Unsigned16();
            Obytes = new Unsigned32();
            outputs = new Pointer();
            Ooffset = new Unsigned32();
            Ostartbit = new Unsigned8();
            Ibits = new Unsigned16();
            Ibytes = new Unsigned32();
            inputs = new Pointer();
            Ioffset = new Unsigned32();
            Istartbit = new Unsigned8();
            SM = super.array(new ec_smt[EC_MAXSM]);
            SMtype = super.array(new Unsigned8[EC_MAXSM]);
            FMMU = super.array(new ec_fmmut[EC_MAXFMMU]);
            FMMU0func = new Unsigned8();
            FMMU1func = new Unsigned8();
            FMMU2func = new Unsigned8();
            FMMU3func = new Unsigned8();
            mbx_l = new Unsigned16();
            mbx_wo = new Unsigned16();
            mbx_rl = new Unsigned16();
            mbx_ro = new Unsigned16();
            mbx_proto = new Unsigned16();
            mbx_cnt = new Unsigned8();
            hasdc = new Unsigned8();
            ptype = new Unsigned8();
            topology = new Unsigned8();
            activeports = new Unsigned8();
            consumedports = new Unsigned8();
            parent = new Unsigned16();
            parentport = new Unsigned8();
            entryport = new Unsigned8();
            DCrtA = new Signed32();
            DCrtB = new Signed32();
            DCrtC = new Signed32();
            DCrtD = new Signed32();
            pdelay = new Signed32();
            DCnext = new Unsigned16();
            DCprevious = new Unsigned16();
            DCcycle = new Signed32();
            DCshift = new Signed32();
            DCactive = new Unsigned8();
            SIIindex = new Unsigned16();
            eep_8byte = new Unsigned8();
            eep_pdi = new Unsigned8();
            CoEdetails = new Unsigned8();
            FoEdetails = new Unsigned8();
            EoEdetails = new Unsigned8();
            SoEdetails = new Unsigned8();
            Ebuscurrent = new Signed16();
            blockLRW = new Unsigned8();
            group = new Unsigned8();
            FMMUunused = new Unsigned8();
            islost = new Unsigned8();
            PO2SOconfig = inner(new SoemLibrary.CallBackPO2SOconfig(runtime));
            mbxhandlerstate = new Signed32();
            mbxrmpstate = new Signed32();
            mbxinstateex = new Unsigned16();
            coembxin = new Pointer();
            coembxinfull = new Unsigned8();
            coembxoverrun = new Signed32();
            soembxin = new Pointer();
            soembxinfull = new Unsigned8();
            soembxoverrun = new Signed32();
            foembxin = new Pointer();
            foembxinfull = new Unsigned8();
            foembxoverrun = new Signed32();
            eoembxin = new Pointer();
            eoembxinfull = new Unsigned8();
            eoembxoverrun = new Signed32();
            voembxin = new Pointer();
            voembxinfull = new Unsigned8();
            voembxoverrun = new Signed32();
            aoembxin = new Pointer();
            aoembxinfull = new Unsigned8();
            aoembxoverrun = new Signed32();
            mbxstatus = new Pointer();
            name = new UTF8String(EC_MAXNAME + 1);
        }
    }

    /**
     * for list of ethercat slave groups
     */
    public final class ec_groupt extends Struct {

        /**
         * logical start address for this group
         */
        public final Unsigned32 logstartaddr;
        /**
         * output bytes, if Obits < 8 then Obytes = 0
         */
        public final Unsigned32 Obytes;
        /**
         * output pointer in IOmap buffer
         */
        public final Pointer outputs;
        /**
         * input bytes, if Ibits < 8 then Ibytes = 0
         */
        public final Unsigned32 Ibytes;
        /**
         * input pointer in IOmap buffer
         */
        public final Pointer inputs;
        /**
         * has DC capabillity
         */
        public final Unsigned8 hasdc;
        /**
         * next DC slave
         */
        public final Unsigned16 DCnext;
        /**
         * E-bus current
         */
        public final Signed16 Ebuscurrent;
        /**
         * if >0 block use of LRW in processdata
         */
        public final Unsigned8 blockLRW;
        /**
         * IO segegments used
         */
        public final Unsigned16 nsegments;
        /**
         * 1st input segment
         */
        public final Unsigned16 Isegment;
        /**
         * Offset in input segment
         */
        public final Unsigned16 Ioffset;
        /**
         * Expected workcounter outputs
         */
        public final Unsigned16 outputsWKC;
        /**
         * Expected workcounter inputs
         */
        public final Unsigned16 inputsWKC;
        /**
         * check slave states
         */
        public final Unsigned8 docheckstate;
        /**
         * IO segmentation list. Datagrams must not break SM in two.
         */
        public final Unsigned32[] IOsegment;
        /**
         * pointer to the group's mailbox status data
         */
        public final Pointer mbxstatus;
        /**
         * length of the group's mailbox status data
         */
        public final Signed32 mbxstatuslength;
        /**
         * lookup table of mailbox status per slave
         */
        public final Unsigned16[] mbxstatuslookup;
        /**
         * position of the last mailbox status
         */
        public final Unsigned16 lastmbxpos;
        /**
         * mailbox transmit queue (embedded single entry, mirrors C
         * "ec_mbxqueuet mbxtxqueue")
         */
        public final ec_mbxqueuet[] mbxtxqueue;

        public ec_groupt(jnr.ffi.Runtime runtime) {
            super(runtime);

            logstartaddr = new Unsigned32();
            Obytes = new Unsigned32();
            outputs = new Pointer();
            Ibytes = new Unsigned32();
            inputs = new Pointer();
            hasdc = new Unsigned8();
            DCnext = new Unsigned16();
            Ebuscurrent = new Signed16();
            blockLRW = new Unsigned8();
            nsegments = new Unsigned16();
            Isegment = new Unsigned16();
            Ioffset = new Unsigned16();
            outputsWKC = new Unsigned16();
            inputsWKC = new Unsigned16();
            docheckstate = new Unsigned8();
            IOsegment = super.array(new Unsigned32[EC_MAXIOSEGMENTS]);
            mbxstatus = new Pointer();
            mbxstatuslength = new Signed32();
            mbxstatuslookup = super.array(new Unsigned16[EC_MAXSLAVE]);
            lastmbxpos = new Unsigned16();
            mbxtxqueue = super.array(new ec_mbxqueuet[1]);
        }
    }

    /**
     * mailbox pool used by context
     */
    public final class ec_mbxpoolt extends Struct {

        public final Signed32 listhead;
        public final Signed32 listtail;
        public final Signed32 listcount;
        public final Signed32[] mbxemptylist;
        public final Pointer mbxmutex;
        public final Unsigned8[] mbx;

        public ec_mbxpoolt(jnr.ffi.Runtime runtime) {
            super(runtime);

            listhead = new Signed32();
            listtail = new Signed32();
            listcount = new Signed32();
            mbxemptylist = super.array(new Signed32[EC_MBXPOOLSIZE]);
            mbxmutex = new Pointer();
            mbx = super.array(new Unsigned8[EC_MBXPOOLSIZE * (EC_MAXMBX + 1)]);
        }
    }

    /**
     * mailbox queue (transmit queue) used by groups
     */
    public final class ec_mbxqueuet extends Struct {

        public final Signed32 listhead;
        public final Signed32 listtail;
        public final Signed32 listcount;
        public final Pointer[] mbx;
        public final Signed32[] mbxstate;
        public final Signed32[] mbxremove;
        public final Signed32[] mbxticket;
        public final Unsigned16[] mbxslave;
        public final Pointer mbxmutex;

        public ec_mbxqueuet(jnr.ffi.Runtime runtime) {
            super(runtime);

            listhead = new Signed32();
            listtail = new Signed32();
            listcount = new Signed32();
            mbx = super.array(new Pointer[EC_MBXPOOLSIZE]);
            mbxstate = super.array(new Signed32[EC_MBXPOOLSIZE]);
            mbxremove = super.array(new Signed32[EC_MBXPOOLSIZE]);
            mbxticket = super.array(new Signed32[EC_MBXPOOLSIZE]);
            mbxslave = super.array(new Unsigned16[EC_MBXPOOLSIZE]);
            mbxmutex = new Pointer();
        }
    }

    /**
     * ENI CoE command structure
     */
    public final class ec_enicoecmdt extends Struct {

        public final Unsigned16 Transition;
        public final Unsigned8 CA;
        public final Unsigned8 Ccs;
        public final Unsigned16 Index;
        public final Unsigned8 SubIdx;
        public final Signed32 Timeout;
        public final Signed32 DataSize;
        public final Pointer Data;

        public ec_enicoecmdt(jnr.ffi.Runtime runtime) {
            super(runtime);

            Transition = new Unsigned16();
            CA = new Unsigned8();
            Ccs = new Unsigned8();
            Index = new Unsigned16();
            SubIdx = new Unsigned8();
            Timeout = new Signed32();
            DataSize = new Signed32();
            Data = new Pointer();
        }
    }

    /**
     * ENI slave structure
     */
    public final class ec_enislavet extends Struct {

        public final Unsigned16 Slave;
        public final Unsigned32 VendorId;
        public final Unsigned32 ProductCode;
        public final Unsigned32 RevisionNo;
        public final Pointer CoECmds;
        public final Signed32 CoECmdCount;

        public ec_enislavet(jnr.ffi.Runtime runtime) {
            super(runtime);

            Slave = new Unsigned16();
            VendorId = new Unsigned32();
            ProductCode = new Unsigned32();
            RevisionNo = new Unsigned32();
            CoECmds = new Pointer();
            CoECmdCount = new Signed32();
        }
    }

    /**
     * ENI structure
     */
    public final class ec_enit extends Struct {

        public final Pointer slave;
        public final Signed32 slavecount;

        public ec_enit(jnr.ffi.Runtime runtime) {
            super(runtime);

            slave = new Pointer();
            slavecount = new Signed32();
        }
    }

    /**
     * SII FMMU structure
     */
    public class ec_eepromFMMUt extends Struct {

        public final Unsigned16 Startpos;
        public final Unsigned8 nFMMU;
        public final Unsigned8 FMMU0;
        public final Unsigned8 FMMU1;
        public final Unsigned8 FMMU2;
        public final Unsigned8 FMMU3;

        public ec_eepromFMMUt(jnr.ffi.Runtime runtime) {
            super(runtime);

            Startpos = new Unsigned16();
            nFMMU = new Unsigned8();
            FMMU0 = new Unsigned8();
            FMMU1 = new Unsigned8();
            FMMU2 = new Unsigned8();
            FMMU3 = new Unsigned8();
        }
    }

    /**
     * SII SM structure
     */
    public class ec_eepromSMt extends Struct {

        public final Unsigned16 Startpos;
        public final Unsigned8 nSM;
        public final Unsigned16 PhStart;
        public final Unsigned16 Plength;
        public final Unsigned8 Creg;
        public final Unsigned8 Sreg;
        /* dont care */
        public final Unsigned8 Activate;
        public final Unsigned8 PDIctrl;

        /* dont care */
        public ec_eepromSMt(jnr.ffi.Runtime runtime) {
            super(runtime);

            Startpos = new Unsigned16();
            nSM = new Unsigned8();
            PhStart = new Unsigned16();
            Plength = new Unsigned16();
            Creg = new Unsigned8();
            Sreg = new Unsigned8();
            Activate = new Unsigned8();
            PDIctrl = new Unsigned8();
        }
    }

    /**
     * record to store rxPDO and txPDO table from eeprom
     */
    public final class ec_eepromPDOt extends Struct {

        public final Unsigned16 Startpos;
        public final Unsigned16 Length;
        public final Unsigned16 nPDO;
        public final Unsigned16[] Index;
        public final Unsigned16[] SyncM;
        public final Unsigned16[] BitSize;
        public final Unsigned16[] SMbitsize;

        public ec_eepromPDOt(jnr.ffi.Runtime runtime) {
            super(runtime);

            Startpos = new Unsigned16();
            Length = new Unsigned16();
            nPDO = new Unsigned16();
            Index = super.array(new Unsigned16[EC_MAXEEPDO]);
            SyncM = super.array(new Unsigned16[EC_MAXEEPDO]);
            BitSize = super.array(new Unsigned16[EC_MAXEEPDO]);
            SMbitsize = super.array(new Unsigned16[EC_MAXSM]);
        }
    }

    /**
     * mailbox buffer array
     */
    // public final SoemLibrary.Uint8[] ec_mbxbuft = new SoemLibrary.Uint8[EC_MAXMBX
    // + 1];
    /**
     * standard ethercat mailbox header
     */
    public class ec_mbxheadert extends Struct {

        public final Unsigned16 length;
        public final Unsigned16 address;
        public final Unsigned8 priority;
        public final Unsigned8 mbxtype;

        public ec_mbxheadert(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            length = new Unsigned16();
            address = new Unsigned16();
            priority = new Unsigned8();
            mbxtype = new Unsigned8();
        }
    }

    /**
     * ALstatus and ALstatus code
     */
    public class ec_alstatust extends Struct {

        public final Unsigned16 alstatus;
        public final Unsigned16 unused;
        public final Unsigned16 alstatuscode;

        public ec_alstatust(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            alstatus = new Unsigned16();
            unused = new Unsigned16();
            alstatuscode = new Unsigned16();
        }
    }

    /**
     * stack structure to store segmented LRD/LWR/LRW constructs
     */
    public final class ec_idxstackT extends Struct {

        public final Unsigned8 pushed;
        public final Unsigned8 pulled;
        public final Unsigned8[] idx;
        public final Pointer[] data;
        public final Unsigned16[] length;
        public final Unsigned16[] dcoffset;
        /**
         * construct type (LRD/LWR/LRW)
         */
        public final Unsigned8[] type;

        public ec_idxstackT(jnr.ffi.Runtime runtime) {
            super(runtime);

            pushed = new Unsigned8();
            pulled = new Unsigned8();
            idx = super.array(new Unsigned8[SoemEtherCATType.EC_MAXBUF]);
            data = super.array(new Pointer[SoemEtherCATType.EC_MAXBUF]);
            length = super.array(new Unsigned16[SoemEtherCATType.EC_MAXBUF]);
            dcoffset = super.array(new Unsigned16[SoemEtherCATType.EC_MAXBUF]);
            type = super.array(new Unsigned8[SoemEtherCATType.EC_MAXBUF]);
        }
    }

    /**
     * ringbuf for error storage
     */
    public final class ec_eringt extends Struct {

        public final Signed16 head;
        public final Signed16 tail;
        @SuppressWarnings("unused")
        private final Padding align_;
        public final SoemEtherCATType.ec_errort[] Error;

        public ec_eringt(jnr.ffi.Runtime runtime) {
            super(runtime);

            head = new Signed16();
            tail = new Signed16();
            align_ = new Padding(NativeType.SLONGLONG, 0);
            Error = super.array(new SoemEtherCATType.ec_errort[EC_MAXELIST + 1]);
        }
    }

    /**
     * SyncManager Communication Type structure for CA
     */
    public final class ec_SMcommtypet extends Struct {

        public final Unsigned8 n;
        public final Unsigned8 nu1;
        public final Unsigned8[] SMtype;

        public ec_SMcommtypet(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            n = new Unsigned8();
            nu1 = new Unsigned8();
            SMtype = super.array(new Unsigned8[EC_MAXSM]);
        }
    }

    /**
     * SDO assign structure for CA
     */
    public final class ec_PDOassignt extends Struct {

        public final Unsigned8 n;
        public final Unsigned8 nu1;
        public final Unsigned16[] index;

        public ec_PDOassignt(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            n = new Unsigned8();
            nu1 = new Unsigned8();
            index = super.array(new Unsigned16[256]);
        }
    }

    /**
     * SDO description structure for CA
     */
    public final class ec_PDOdesct extends Struct {

        public final Unsigned8 n;
        public final Unsigned8 nu1;
        public final Unsigned32[] PDO;

        public ec_PDOdesct(jnr.ffi.Runtime runtime) {
            super(runtime, new Struct.Alignment(1));

            n = new Unsigned8();
            nu1 = new Unsigned8();
            PDO = super.array(new Unsigned32[256]);
        }
    }

    /**
     * Context structure , referenced by all ecx functions
     */
    public final class ecx_contextt extends Struct {

        @SuppressWarnings("unused")
        private final Pointer _base;
        /**
         * port, may include red_port
         */
        public final ecx_portt port;
        /**
         * list of detected slaves
         */
        public final ec_slavet[] slavelist;
        /**
         * number of slaves found in configuration
         */
        public final SoemLibrary.Int32 slavecount;
        /**
         * list of groups
         */
        public final ec_groupt[] grouplist;
        /**
         * ecaterror state
         */
        public final SoemLibrary.Bool ecaterror;
        /**
         * last DC time from slaves
         */
        public final SoemLibrary.Int64 DCtime;
        /**
         * internal, eeprom cache buffer
         */
        public final SoemLibrary.Uint8[] esibuf;
        /**
         * internal, eeprom cache map
         */
        public final SoemLibrary.Uint32[] esimap;
        /**
         * internal, current slave for eeprom cache
         */
        public final SoemLibrary.Uint16 esislave;
        /**
         * internal, error list
         */
        public final ec_eringt elist;
        /**
         * internal, processdata stack buffer info
         */
        public final ec_idxstackT idxstack;
        /**
         * internal, SM buffer
         */
        public final ec_SMcommtypet[] SMcommtype;
        /**
         * internal, PDO assign list
         */
        public final ec_PDOassignt[] PDOassign;
        /**
         * internal, PDO description list
         */
        public final ec_PDOdesct[] PDOdesc;
        /**
         * internal, SM list from eeprom
         */
        public final ec_eepromSMt eepSM;
        /**
         * internal, FMMU list from eeprom
         */
        public final ec_eepromFMMUt eepFMMU;
        /**
         * internal, mailbox pool
         */
        public final ec_mbxpoolt mbxpool;
        /**
         * pointer to ENI structure
         */
        public final SoemLibrary.Ptr ENI;
        /**
         * registered FoE hook
         */
        public final SoemLibrary.CallBackFOEhook FOEhook;
        /**
         * registered EoE hook
         */
        public final SoemLibrary.CallBackEOEhook EOEhook;
        /**
         * flag to control legacy automatic state change or manual state change
         */
        public final SoemLibrary.Int32 manualstatechange;
        /**
         * userdata, promotes application configuration esp. with multiple
         * ec_context instances. Note: userdata memory is managed by
         * application, not SOEM
         */
        public final SoemLibrary.Ptr userdata;
        /**
         * if true, overlapping IOmap will be created and overlap behavior will
         * be used during process data transfer
         */
        public final SoemLibrary.Bool overlappedMode;
        /**
         * if true, slaves with inputs or outputs of less than 8 bits will not
         * be byte aligned in the IOmap, instead packed together with minimal
         * gaps
         */
        public final SoemLibrary.Bool packedMode;

        public ecx_contextt(jnr.ffi.Runtime runtime) {
            super(runtime);

            int i;
            _base = new Pointer();
            port = new ecx_portt(runtime);
            slavelist = new ec_slavet[EC_MAXSLAVE];
            for (i = 0; i < slavelist.length; i++) {
                slavelist[i] = new ec_slavet(runtime);
            }
            slavecount = new SoemLibrary.Int32(runtime);
            grouplist = new ec_groupt[EC_MAXGROUP];
            for (i = 0; i < grouplist.length; i++) {
                grouplist[i] = new ec_groupt(runtime);
            }
            ecaterror = new SoemLibrary.Bool(runtime);
            DCtime = new SoemLibrary.Int64(runtime);
            esibuf = new SoemLibrary.Uint8[SoemEtherCATType.EC_MAXEEPBUF];
            for (i = 0; i < esibuf.length; i++) {
                esibuf[i] = new SoemLibrary.Uint8(runtime);
            }
            esimap = new SoemLibrary.Uint32[SoemEtherCATType.EC_MAXEEPBITMAP];
            for (i = 0; i < esimap.length; i++) {
                esimap[i] = new SoemLibrary.Uint32(runtime);
            }
            esislave = new SoemLibrary.Uint16(runtime);
            elist = new ec_eringt(runtime);
            idxstack = new ec_idxstackT(runtime);
            SMcommtype = new ec_SMcommtypet[EC_MAX_MAPT];
            for (i = 0; i < SMcommtype.length; i++) {
                SMcommtype[i] = new ec_SMcommtypet(runtime);
            }
            PDOassign = new ec_PDOassignt[EC_MAX_MAPT];
            for (i = 0; i < PDOassign.length; i++) {
                PDOassign[i] = new ec_PDOassignt(runtime);
            }
            PDOdesc = new ec_PDOdesct[EC_MAX_MAPT];
            for (i = 0; i < PDOdesc.length; i++) {
                PDOdesc[i] = new ec_PDOdesct(runtime);
            }
            eepSM = new ec_eepromSMt(runtime);
            eepFMMU = new ec_eepromFMMUt(runtime);
            mbxpool = new ec_mbxpoolt(runtime);
            ENI = new SoemLibrary.Ptr(runtime);
            FOEhook = new SoemLibrary.CallBackFOEhook(runtime);
            EOEhook = new SoemLibrary.CallBackEOEhook(runtime);
            manualstatechange = new SoemLibrary.Int32(runtime);
            userdata = new SoemLibrary.Ptr(runtime);
            overlappedMode = new SoemLibrary.Bool(runtime);
            packedMode = new SoemLibrary.Bool(runtime);
        }

        private static long alignOffset(long offset, int alignment) {
            return ((offset + alignment - 1) / alignment) * alignment;
        }

        static long alignedSize(Struct s) {
            return alignOffset(Struct.size(s), Struct.alignment(s));
        }

        private static long advanceStruct(long offset, Struct s, int[] maxAlign) {
            int alignment = Struct.alignment(s);
            if (alignment > maxAlign[0]) {
                maxAlign[0] = alignment;
            }
            return alignOffset(offset, alignment) + alignedSize(s);
        }

        private static long advanceStructArray(long offset, Struct[] array, int[] maxAlign) {
            for (Struct s : array) {
                offset = advanceStruct(offset, s, maxAlign);
            }
            return offset;
        }

        public long expectedByteSize() {
            int[] maxAlign = { 1 };
            long offset = 0;
            offset = advanceStruct(offset, port, maxAlign);
            offset = advanceStructArray(offset, slavelist, maxAlign);
            offset = advanceStruct(offset, slavecount, maxAlign);
            offset = advanceStructArray(offset, grouplist, maxAlign);
            offset = advanceStruct(offset, ecaterror, maxAlign);
            offset = advanceStruct(offset, DCtime, maxAlign);
            offset = advanceStructArray(offset, esibuf, maxAlign);
            offset = advanceStructArray(offset, esimap, maxAlign);
            offset = advanceStruct(offset, esislave, maxAlign);
            offset = advanceStruct(offset, elist, maxAlign);
            offset = advanceStruct(offset, idxstack, maxAlign);
            offset = advanceStructArray(offset, SMcommtype, maxAlign);
            offset = advanceStructArray(offset, PDOassign, maxAlign);
            offset = advanceStructArray(offset, PDOdesc, maxAlign);
            offset = advanceStruct(offset, eepSM, maxAlign);
            offset = advanceStruct(offset, eepFMMU, maxAlign);
            offset = advanceStruct(offset, mbxpool, maxAlign);
            offset = advanceStruct(offset, ENI, maxAlign);
            offset = advanceStruct(offset, FOEhook, maxAlign);
            offset = advanceStruct(offset, EOEhook, maxAlign);
            offset = advanceStruct(offset, manualstatechange, maxAlign);
            offset = advanceStruct(offset, userdata, maxAlign);
            offset = advanceStruct(offset, overlappedMode, maxAlign);
            offset = advanceStruct(offset, packedMode, maxAlign);
            return alignOffset(offset, maxAlign[0]);
        }

        public static long expectedByteSize(jnr.ffi.Runtime runtime) {
            return new ecx_contextt(runtime).expectedByteSize();
        }

        private static long sliceStruct(jnr.ffi.Pointer base, long offset, Struct s) {
            int alignment = Struct.alignment(s);
            long size = alignOffset(Struct.size(s), alignment);
            offset = alignOffset(offset, alignment);
            s.useMemory(base.slice(offset, size));
            return offset + size;
        }

        private static long sliceStructArray(jnr.ffi.Pointer base, long offset, Struct[] array) {
            for (Struct s : array) {
                offset = sliceStruct(base, offset, s);
            }
            return offset;
        }

        public ecx_contextt register() {
            jnr.ffi.Pointer base = Struct.getMemory(this);
            if (base == null) {
                return this;
            }
            long offset = 0;
            offset = sliceStruct(base, offset, port);
            offset = sliceStructArray(base, offset, slavelist);
            offset = sliceStruct(base, offset, slavecount);
            offset = sliceStructArray(base, offset, grouplist);
            offset = sliceStruct(base, offset, ecaterror);
            offset = sliceStruct(base, offset, DCtime);
            offset = sliceStructArray(base, offset, esibuf);
            offset = sliceStructArray(base, offset, esimap);
            offset = sliceStruct(base, offset, esislave);
            offset = sliceStruct(base, offset, elist);
            offset = sliceStruct(base, offset, idxstack);
            offset = sliceStructArray(base, offset, SMcommtype);
            offset = sliceStructArray(base, offset, PDOassign);
            offset = sliceStructArray(base, offset, PDOdesc);
            offset = sliceStruct(base, offset, eepSM);
            offset = sliceStruct(base, offset, eepFMMU);
            offset = sliceStruct(base, offset, mbxpool);
            offset = sliceStruct(base, offset, ENI);
            offset = sliceStruct(base, offset, FOEhook);
            offset = sliceStruct(base, offset, EOEhook);
            offset = sliceStruct(base, offset, manualstatechange);
            offset = sliceStruct(base, offset, userdata);
            offset = sliceStruct(base, offset, overlappedMode);
            sliceStruct(base, offset, packedMode);
            return this;
        }
    }
}
