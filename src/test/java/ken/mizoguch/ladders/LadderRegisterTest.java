package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.List;
import ken.mizoguch.soem.Soem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LadderRegister}. The registration of an address on a slave of a soem bus
 * is asked by the javascript of a page with a variable number of arguments : the arguments are
 * checked, the address is added to the io and to the view of the io when it is new.
 */
class LadderRegisterTest {

    /**
     * the addresses which the view of the io has been asked to show
     */
    private final List<String> rows_ = new ArrayList<>();

    /**
     *
     * @return the io of a ladder with the row of the global addresses
     */
    private LadderIoStore store() {
        LadderIoStore store = new LadderIoStore(new Object());

        store.addGlobalRow();
        return store;
    }

    /**
     *
     * @param store the io of the ladders
     * @return the registrations of the ladders
     */
    private LadderRegister register(LadderIoStore store) {
        return new LadderRegister(store, (address) -> rows_.add(address));
    }

    @Test
    void registerInRegistersAnAddressOnASlave() {
        LadderIoStore store = store();
        LadderRegister register = register(store);
        LadderRegisterSoemIo in;

        assertTrue(register.registerIn(new Soem(), "IN", 1, 8L, 3L));

        assertEquals(1, register.getRegisterSoemIn().size());
        in = register.getRegisterSoemIn().get(0);
        assertEquals("IN", in.address());
        assertEquals(1, in.slave());
        assertEquals(8L, in.bitsOffset());
        assertEquals(3L, in.bitsMask());
        assertEquals(1, rows_.size());
        assertEquals("IN", rows_.get(0));
        assertNotNull(store.io(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, "IN"));
    }

    @Test
    void registerInRefusesALocalAddress() {
        LadderRegister register = register(store());

        assertFalse(register.registerIn(new Soem(), ".IN", 1, 0L, 1L));

        assertTrue(register.getRegisterSoemIn().isEmpty());
        assertTrue(rows_.isEmpty());
    }

    @Test
    void registerInRefusesAnAddressWithASpace() {
        LadderRegister register = register(store());

        assertFalse(register.registerIn(new Soem(), "IN 1", 1, 0L, 1L));
        assertTrue(register.registerIn(new Soem(), " IN ", 1, 0L, 1L));
        assertEquals("IN", register.getRegisterSoemIn().get(0).address());
        assertEquals(1, register.getRegisterSoemIn().size());
    }

    @Test
    void registerInRefusesIncorrectArguments() {
        LadderRegister register = register(store());

        assertNull(register.registerIn((Object[]) null));
        assertFalse(register.registerIn(new Soem(), "IN"));
        assertFalse(register.registerIn("not a bus", "IN", 1, 0L, 1L));

        assertNull(register.getSoem());
        assertTrue(register.getRegisterSoemIn().isEmpty());
    }

    @Test
    void registerInTakesTheLastRegistrationOfAnAddress() {
        LadderRegister register = register(store());

        assertTrue(register.registerIn(new Soem(), "IN", 1, 0L, 1L));
        assertTrue(register.registerIn(new Soem(), "IN", 1, 0L, 1L));
        assertTrue(register.registerIn(new Soem(), "IN", 2, 4L, 7L));

        assertEquals(1, register.getRegisterSoemIn().size());
        assertEquals("IN", register.getRegisterSoemIn().get(0).address());
        assertEquals(2, register.getRegisterSoemIn().get(0).slave());
        assertEquals(7L, register.getRegisterSoemIn().get(0).bitsMask());
        assertEquals(1, rows_.size());
    }

    @Test
    void registerOutRegistersOnTheOutputsOnly() {
        LadderRegister register = register(store());

        assertTrue(register.registerIn(new Soem(), "IN", 1, 0L, 1L));
        assertTrue(register.registerOut(new Soem(), "OUT", 1, 0L, 1L));

        assertEquals(1, register.getRegisterSoemIn().size());
        assertEquals(1, register.getRegisterSoemOut().size());
        assertEquals("IN", register.getRegisterSoemIn().get(0).address());
        assertEquals("OUT", register.getRegisterSoemOut().get(0).address());
    }

    @Test
    void aSecondBusDoesNotClearTheRegisteredAddresses() {
        LadderRegister register = register(store());

        register.registerIn(new Soem(), "IN", 1, 0L, 1L);
        register.registerOut(new Soem(), "OUT", 1, 0L, 1L);

        assertNotNull(register.getSoem());
        assertEquals(1, register.getRegisterSoemIn().size());
        assertEquals(1, register.getRegisterSoemOut().size());
    }

    @Test
    void clearSoemRemovesTheAddressesAndKeepsTheBus() {
        LadderRegister register = register(store());

        register.registerIn(new Soem(), "IN", 1, 0L, 1L);
        register.registerOut(new Soem(), "OUT", 1, 0L, 1L);
        register.clearSoem();

        assertTrue(register.getRegisterSoemIn().isEmpty());
        assertTrue(register.getRegisterSoemOut().isEmpty());
        assertNotNull(register.getSoem());
    }

    @Test
    void unregisterSoemRemovesTheBus() {
        LadderRegister register = register(store());
        Soem soem = new Soem();

        register.registerIn(new Soem(), "IN", 1, 0L, 1L);
        register.unregister(new Object());
        assertEquals(1, register.getRegisterSoemIn().size());
        assertNotNull(register.getSoem());

        register.unregister(soem);
        assertTrue(register.getRegisterSoemIn().isEmpty());
        assertNull(register.getSoem());
    }

    @Test
    void isRegisteredAnswersNullWithoutAnObject() {
        LadderRegister register = register(store());

        assertNull(register.isRegistered(null));
        assertFalse(register.isRegistered(new Object()));
    }
}
