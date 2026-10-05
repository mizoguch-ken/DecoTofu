/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * the keys of the json of the slaveinfo : gson serializes the fields of the
 * data of SoemSlaveInfo with their name, so every name is a part of the answer
 * the web page reads.
 *
 * @author mizoguch-ken
 */
class SoemSlaveInfoTest {

    /**
     * the names gson answers as keys : the synthetic field of the inner classes
     * (this$0) is never serialized, the methods of the data are neither
     *
     * @param type the class of the data of the slaveinfo
     * @return the keys of that data
     */
    private Set<String> keys(Class<?> type) {
        Set<String> names = new TreeSet<>();

        for (Field field : type.getDeclaredFields()) {
            if (!field.isSynthetic()) {
                names.add(field.getName());
            }
        }
        return names;
    }

    /**
     *
     * @param names
     * @return
     */
    private Set<String> keys(String... names) {
        return new TreeSet<>(Arrays.asList(names));
    }

    @Test
    @DisplayName("the keys of the group of the slaveinfo")
    void keysOfTheGroup() {
        assertEquals(keys("EcatError", "ExpectedWKC", "InterfaceName", "Slavelist", "SlaveCount"),
                keys(SoemSlaveInfo.SlaveInfo.class));
    }

    @Test
    @DisplayName("the keys of a slave of the slaveinfo")
    void keysOfASlave() {
        assertEquals(keys("ActivePorts", "ALStatusCode", "ALStatusCodeString", "BlockLRW", "CoEDetails",
                "ConfigAddress", "EbusCurrent", "EcatError", "EEP_ID", "EEP_MAN", "EEP_REV", "EoEDetails",
                "FMMU0Func", "FMMU1Func", "FMMU2Func", "FMMU3Func", "FMMUlist", "FoEDetails", "HasDC", "Index",
                "InputBits", "MailboxProtocols", "MailboxReadLength", "MailboxWriteLength", "Name", "OutputBits",
                "ParentPort", "PropagationDelay", "RxPDOlist", "SDO", "SMlist", "SoEDetails", "State", "TxPDOlist"),
                keys(SoemSlaveInfo.SlaveInfo.Slave.class));
    }

    @Test
    @DisplayName("the keys of the sync managers and the fmmu of a slave")
    void keysOfTheSmAndFmmu() {
        assertEquals(keys("Index", "SMFlags", "SMLength", "SMType", "StartAddress"),
                keys(SoemSlaveInfo.SlaveInfo.SM.class));
        assertEquals(keys("FMMUActive", "FMMUType", "Index", "LogEndbit", "LogLength", "LogStart", "LogStartbit",
                "PhysStart", "PhysStartBit"),
                keys(SoemSlaveInfo.SlaveInfo.FMMU.class));
    }

    @Test
    @DisplayName("the keys of the pdo of a slave")
    void keysOfThePdo() {
        assertEquals(keys("Index", "Name", "SIIlist", "SyncM"),
                keys(SoemSlaveInfo.SlaveInfo.PDO.class));
        assertEquals(keys("AbsBit", "AbsOffset", "BitLength", "Name", "ObjectDataType", "ObjectIndex",
                "ObjectSubIndex"),
                keys(SoemSlaveInfo.SlaveInfo.PDO.SII.class));
    }

    @Test
    @DisplayName("the keys of the sdo dictionary of a slave")
    void keysOfTheSdo() {
        assertEquals(keys("EcatError", "ODlist", "ODlistEntries"),
                keys(SoemSlaveInfo.SlaveInfo.SDO.class));
        assertEquals(keys("DataType", "DataTypeName", "EcatError", "Entries", "Index", "MaxSub", "Name", "OElist",
                "ObjectCode"),
                keys(SoemSlaveInfo.SlaveInfo.SDO.OD.class));
        assertEquals(keys("AccessType", "BitLength", "DataType", "DataTypeName", "Index", "Name", "ObjectAccess",
                "SDO"),
                keys(SoemSlaveInfo.SlaveInfo.SDO.OD.OE.class));
    }
}
