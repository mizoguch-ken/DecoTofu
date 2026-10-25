/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.int16_t;
import jnr.ffi.types.int32_t;
import jnr.ffi.types.u_int16_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

/**
 *
 * @author mizoguch-ken
 *
 */
public interface SoemLibrary {

    public interface PO2SOconfig {

        @Delegate
        int call(int slave);
    }

    public interface PO2SOconfigx {

        @Delegate
        int call(SoemEtherCATMain.ecx_contextt context, int slave);
    }

    public interface FOEhook {

        @Delegate
        public int call(int slave, int packetnumber, int datasize);
    }

    public interface EOEhook {

        @Delegate
        public int call(SoemEtherCATMain.ecx_contextt context, int slave, Pointer eoembx);
    }

    public final class CallBackPO2SOconfig extends Struct {

        public Struct.Function<PO2SOconfig> PO2SOconfig = function(PO2SOconfig.class);

        public CallBackPO2SOconfig(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }

    public final class CallBackPO2SOconfigx extends Struct {

        public Struct.Function<PO2SOconfigx> PO2SOconfig = function(PO2SOconfigx.class);

        public CallBackPO2SOconfigx(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }

    public final class CallBackFOEhook extends Struct {

        public Struct.Function<FOEhook> FOEhook = function(FOEhook.class);

        public CallBackFOEhook(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }

    public final class CallBackEOEhook extends Struct {

        public Struct.Function<EOEhook> FOEhook = function(EOEhook.class);

        public CallBackEOEhook(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }

    public class Bool extends Struct {

        private final Struct.Unsigned8 value_;

        public Bool(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Unsigned8();
        }

        public short get() {
            return value_.shortValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(short value) {
            value_.set(value);
        }
    }

    public class Int8 extends Struct {

        private final Struct.Signed8 value_;

        public Int8(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Signed8();
        }

        public short get() {
            return value_.byteValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(short value) {
            value_.set(value);
        }
    }

    public class Int16 extends Struct {

        private final Struct.Signed16 value_;

        public Int16(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Signed16();
        }

        public short get() {
            return value_.shortValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(short value) {
            value_.set(value);
        }
    }

    public class Int32 extends Struct {

        private final Struct.Signed32 value_;

        public Int32(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Signed32();
        }

        public int get() {
            return value_.intValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(int value) {
            value_.set(value);
        }
    }

    public class Int64 extends Struct {

        private final Struct.Signed64 value_;

        public Int64(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Signed64();
        }

        public long get() {
            return value_.longValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(long value) {
            value_.set(value);
        }
    }

    public class Uint8 extends Struct {

        private final Struct.Unsigned8 value_;

        public Uint8(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Unsigned8();
        }

        public short get() {
            return value_.shortValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(short value) {
            value_.set(value);
        }
    }

    public class Uint16 extends Struct {

        private final Struct.Unsigned16 value_;

        public Uint16(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Unsigned16();
        }

        public int get() {
            return value_.intValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(int value) {
            value_.set(value);
        }
    }

    public class Uint32 extends Struct {

        private final Struct.Unsigned32 value_;

        public Uint32(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Unsigned32();
        }

        public long get() {
            return value_.longValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(long value) {
            value_.set(value);
        }
    }

    public class Uint64 extends Struct {

        private final Struct.Unsigned64 value_;

        public Uint64(jnr.ffi.Runtime runtime) {
            super(runtime);

            value_ = new Struct.Unsigned64();
        }

        public long get() {
            return value_.longValue();
        }

        public void set(Number value) {
            value_.set(value);
        }

        public void set(long value) {
            value_.set(value);
        }
    }

    public class UTF8String extends Struct {

        private final Struct.UTF8String value_;

        public UTF8String(jnr.ffi.Runtime runtime, int size) {
            super(runtime);

            value_ = new Struct.UTF8String(size);
        }

        public java.lang.String get() {
            return value_.get();
        }

        public void set(java.lang.String value) {
            value_.set(value);
        }
    }

    // SoemEtherCAT
    public SoemEtherCATMain.ecx_contextt ec_malloc_context();

    public void ec_free_context(SoemEtherCATMain.ecx_contextt context);

    public Pointer ec_redport(SoemEtherCATMain.ecx_contextt context);

    public SoemEtherCAT.ecx_parcelt ec_malloc_parcel(SoemEtherCATMain.ecx_contextt context);

    public void ec_free_parcel(SoemEtherCAT.ecx_parcelt parcel);

    public int ec_run(SoemEtherCAT.ecx_parcelt parcel);

    /**
     * wait the end of the native thread of the process data started by ec_run()
     *
     * @param parcel
     * @param timeoutMillis -1 to wait for ever
     * @return 1 when the thread left the cyclic function, 0 when it is still
     * running, -1 when the platform of the shared library can not wait it
     */
    public int ec_wait_thread(SoemEtherCAT.ecx_parcelt parcel, int timeoutMillis);

    // SoemEtherCATBase
    public int ecx_setupdatagram(Pointer port, Pointer frame, @u_int8_t int com, @u_int8_t int idx, @u_int16_t int ADP,
            @u_int16_t int ADO, @u_int16_t int length, Pointer data);

    public int ecx_adddatagram(Pointer port, Pointer frame, @u_int8_t int com, @u_int8_t int idx, @u_int8_t int more,
            @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data);

    public int ecx_BWR(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public int ecx_BRD(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public int ecx_APRD(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public int ecx_ARMW(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public int ecx_FRMW(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public @u_int16_t int ecx_APRDw(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, int timeout);

    public int ecx_FPRD(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public @u_int16_t int ecx_FPRDw(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, int timeout);

    public int ecx_APWRw(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int data, int timeout);

    public int ecx_APWR(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public int ecx_FPWRw(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int data, int timeout);

    public int ecx_FPWR(Pointer port, @u_int16_t int ADP, @u_int16_t int ADO, @u_int16_t int length, Pointer data,
            int timeout);

    public int ecx_LRW(Pointer port, @u_int32_t long LogAdr, @u_int16_t int length, Pointer data, int timeout);

    public int ecx_LRD(Pointer port, @u_int32_t long LogAdr, @u_int16_t int length, Pointer data, int timeout);

    public int ecx_LWR(Pointer port, @u_int32_t long LogAdr, @u_int16_t int length, Pointer data, int timeout);

    public int ecx_LRWDC(Pointer port, @u_int32_t long LogAdr, @u_int16_t int length, Pointer data, @u_int16_t int DCrs,
            Pointer DCtime, int timeout);

    // SoemEtherCATMain
    /**
     * the adapters of the machine as the linked list of ec_adaptert. The head of
     * the list is answered as a pointer and not as a struct : jnr-ffi answers a
     * struct by allocating java memory for it, so the null of a machine that has
     * no adapter to offer would be released by the native free() as if it came
     * from the native heap. ec_free_adapters_list() releases the list from the
     * head it was answered with, releasing an element in the middle of the list
     * leaks the elements in front of it.
     */
    public Pointer ec_adapters();

    public void ec_free_adapters_list(Pointer list);

    public @u_int8_t int ec_nextmbxcnt(@u_int8_t int cnt);

    public void ec_clearmbx(@u_int8_t int[] Mbx);

    public void ecx_pusherror(SoemEtherCATMain.ecx_contextt context, SoemEtherCATType.ec_errort Ec);

    public @u_int8_t int ecx_poperror(SoemEtherCATMain.ecx_contextt context, SoemEtherCATType.ec_errort Ec);

    public @u_int8_t int ecx_iserror(SoemEtherCATMain.ecx_contextt context);

    public void ecx_packeterror(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Slave, @u_int16_t int Index,
            @u_int8_t int SubIdx, @u_int16_t int ErrorCode);

    public int ecx_init(SoemEtherCATMain.ecx_contextt context, String ifname);

    public int ecx_init_redundant(SoemEtherCATMain.ecx_contextt context, Pointer redport, String ifname,
            String if2name);

    public void ecx_close(SoemEtherCATMain.ecx_contextt context);

    public @u_int8_t int ecx_siigetbyte(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave,
            @u_int16_t int address);

    public @int16_t int ecx_siifind(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int16_t int cat);

    public void ecx_siistring(SoemEtherCATMain.ecx_contextt context, Pointer str, @u_int16_t int slave,
            @u_int16_t int Sn);

    public @u_int16_t int ecx_siiFMMU(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave,
            SoemEtherCATMain.ec_eepromFMMUt FMMU);

    public @u_int16_t int ecx_siiSM(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave,
            SoemEtherCATMain.ec_eepromSMt SM);

    public @u_int16_t int ecx_siiSMnext(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave,
            SoemEtherCATMain.ec_eepromSMt SM, @u_int16_t int n);

    public int ecx_siiPDO(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave,
            SoemEtherCATMain.ec_eepromPDOt PDO, @u_int8_t int t);

    public int ecx_readstate(SoemEtherCATMain.ecx_contextt context);

    public int ecx_writestate(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave);

    public @u_int16_t int ecx_statecheck(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave,
            @u_int16_t int reqstate, int timeout);

    public int ecx_mbxhandler(SoemEtherCATMain.ecx_contextt context, @u_int8_t int group, int limit);

    public int ecx_mbxempty(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, int timeout);

    public int ecx_mbxsend(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int[] mbx,
            int timeout);

    public int ecx_mbxreceive(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int[] mbx,
            int timeout);

    public void ecx_esidump(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int[] esibuf);

    public @u_int32_t long ecx_readeeprom(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave,
            @u_int16_t int eeproma, int timeout);

    public int ecx_writeeeprom(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int16_t int eeproma,
            @u_int16_t int data, int timeout);

    public int ecx_eeprom2master(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave);

    public int ecx_eeprom2pdi(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave);

    public @u_int64_t long ecx_readeepromAP(SoemEtherCATMain.ecx_contextt context, @u_int16_t int aiadr,
            @u_int16_t int eeproma, int timeout);

    public int ecx_writeeepromAP(SoemEtherCATMain.ecx_contextt context, @u_int16_t int aiadr, @u_int16_t int eeproma,
            @u_int16_t int data, int timeout);

    public @u_int64_t long ecx_readeepromFP(SoemEtherCATMain.ecx_contextt context, @u_int16_t int configadr,
            @u_int16_t int eeproma, int timeout);

    public int ecx_writeeepromFP(SoemEtherCATMain.ecx_contextt context, @u_int16_t int configadr,
            @u_int16_t int eeproma, @u_int16_t int data, int timeout);

    public void ecx_readeeprom1(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int16_t int eeproma);

    public @u_int32_t long ecx_readeeprom2(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, int timeout);

    public int ecx_send_overlap_processdata_group(SoemEtherCATMain.ecx_contextt context, @u_int8_t int group);

    public int ecx_receive_processdata_group(SoemEtherCATMain.ecx_contextt context, @u_int8_t int group, int timeout);

    public int ecx_send_processdata(SoemEtherCATMain.ecx_contextt context);

    public int ecx_send_overlap_processdata(SoemEtherCATMain.ecx_contextt context);

    public int ecx_receive_processdata(SoemEtherCATMain.ecx_contextt context, int timeout);

    public int ecx_send_processdata_group(SoemEtherCATMain.ecx_contextt context, @u_int8_t int group);

    // SoemEtherCATDC
    public @u_int8_t int ecx_configdc(SoemEtherCATMain.ecx_contextt context);

    public void ecx_dcsync0(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int act,
            @u_int32_t long CyclTime, @int32_t int CyclShift);

    public void ecx_dcsync01(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int act,
            @u_int32_t long CyclTime0, @u_int32_t long CyclTime1, @int32_t int CyclShift);

    // SoemEtherCATCoE
    public void ecx_SDOerror(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Slave, @u_int16_t int Index,
            @u_int8_t int SubIdx, @int32_t int AbortCode);

    public int ecx_SDOread(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int16_t int index,
            @u_int8_t int subindex, @u_int8_t int CA, Pointer psize, Pointer p, int timeout);

    public int ecx_SDOwrite(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Slave, @u_int16_t int Index,
            @u_int8_t int SubIndex, @u_int8_t int CA, int psize, final Pointer p, int Timeout);

    public int ecx_RxPDO(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Slave, @u_int16_t int RxPDOnumber,
            int psize, final Pointer p);

    public int ecx_TxPDO(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int16_t int TxPDOnumber,
            Pointer psize, Pointer p, int timeout);

    public int ecx_readPDOmap(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Slave, Pointer Osize,
            Pointer Isize);

    public int ecx_readPDOmapCA(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Slave, int Thread_n,
            Pointer Osize, Pointer Isize);

    public int ecx_readODlist(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Slave,
            SoemEtherCATCoE.ec_ODlistt pODlist);

    public int ecx_readODdescription(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Item,
            SoemEtherCATCoE.ec_ODlistt pODlist);

    public int ecx_readOEsingle(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Item, @u_int8_t int SubI,
            SoemEtherCATCoE.ec_ODlistt pODlist, SoemEtherCATCoE.ec_OElistt pOElist);

    public int ecx_readOE(SoemEtherCATMain.ecx_contextt context, @u_int16_t int Item,
            SoemEtherCATCoE.ec_ODlistt pODlist, SoemEtherCATCoE.ec_OElistt pOElist);

    // SoemEtherCATEoE
    public int ecx_EOEdefinehook(SoemEtherCATMain.ecx_contextt context, CallBackEOEhook hook);

    // public int ecx_EOEsetIp(SoemEtherCATMain.ecx_contextt context, @u_int16_t int
    // slave, @u_int8_t int port, SoemEtherCATEoE.eoe_param_t ipparam, int timeout);
    // public int ecx_EOEgetIp(SoemEtherCATMain.ecx_contextt context, @u_int16_t int
    // slave, @u_int8_t int port, SoemEtherCATEoE.eoe_param_t ipparam, int timeout);
    public int ecx_EOEsend(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int port, int psize,
            Pointer p, int timeout);

    public int ecx_EOErecv(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int port,
            Pointer psize, Pointer p, int timeout);

    // public int ecx_EOEreadfragment(SoemEtherCATMain.ec_mbxbuft MbxIn, Pointer
    // rxfragmentno, Pointer rxframesize, Pointer rxframeoffset, Pointer rxframeno,
    // Pointer psize, Pointer p);
    // SoemEtherCATFoE
    public int ecx_FOEdefinehook(SoemEtherCATMain.ecx_contextt context, CallBackFOEhook hook);

    public int ecx_FOEread(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, Pointer filename,
            @u_int32_t long password, Pointer psize, Pointer p, int timeout);

    public int ecx_FOEwrite(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, Pointer filename,
            @u_int32_t long password, int psize, Pointer p, int timeout);

    // SoemEtherCATSoE
    public int ecx_SoEread(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int driveNo,
            @u_int8_t int elementflags, @u_int16_t int idn, Pointer psize, Pointer p, int timeout);

    public int ecx_SoEwrite(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, @u_int8_t int driveNo,
            @u_int8_t int elementflags, @u_int16_t int idn, int psize, Pointer p, int timeout);

    public int ecx_readIDNmap(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, Pointer Osize,
            Pointer Isize);

    // SoemEtherCATConfig
    public int ecx_config_init(SoemEtherCATMain.ecx_contextt context, @u_int8_t int usetable);

    public int ecx_config_map_group(SoemEtherCATMain.ecx_contextt context, Pointer pIOmap, @u_int8_t int group);

    public int ecx_config_overlap_map_group(SoemEtherCATMain.ecx_contextt context, Pointer pIOmap, @u_int8_t int group);

    public int ecx_config_map_group_aligned(SoemEtherCATMain.ecx_contextt context, Pointer pIOmap, @u_int8_t int group);

    public int ecx_recover_slave(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, int timeout);

    public int ecx_reconfig_slave(SoemEtherCATMain.ecx_contextt context, @u_int16_t int slave, int timeout);

    // SoemEtherCATPrint
    String ec_sdoerror2string(@u_int32_t long sdoerrorcode);

    String ec_ALstatuscode2string(@u_int16_t int ALstatuscode);

    String ec_soeerror2string(@u_int16_t int errorcode);

    String ec_mbxerror2string(@u_int16_t int errorcode);

    String ecx_err2string(SoemEtherCATType.ec_errort Ec);

    String ecx_elist2string(SoemEtherCATMain.ecx_contextt context);
}
