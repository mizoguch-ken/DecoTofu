package ken.mizoguch.ladders;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Headless tests for DesignLadders menu structure, FXML/controller wiring,
 * key routing, and utility validation helpers.
 */
class DesignLaddersMenuTest {

    private static final boolean IS_MAC = System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("mac");

    private static Document loadDesignLaddersFxml() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (InputStream stream = DesignLaddersMenuTest.class.getResourceAsStream("/fxml/DesignLadders.fxml")) {
            assertNotNull(stream, "DesignLadders.fxml resource must exist");
            Document doc = builder.parse(stream);
            doc.getDocumentElement().normalize();
            return doc;
        }
    }

    private static KeyEvent keyEvent(KeyCode code, boolean shift, boolean shortcut, boolean alt) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift,
                shortcut && !IS_MAC, alt, shortcut && IS_MAC);
    }

    private static void collectElements(Element root, Set<String> fxmlIds,
            Map<KeyCombination, String> accelerators, Set<String> forbiddenNormalized) {
        String fxId = root.getAttribute("fx:id");
        if (fxId != null && !fxId.isEmpty()) {
            assertTrue(fxmlIds.add(fxId), "Duplicate fx:id in DesignLadders.fxml: " + fxId);
        }

        String text = root.getAttribute("text");
        assertNotEquals("Tab Move Lft", text, "Typo 'Tab Move Lft' should be removed");
        assertNotEquals("Save As..", text, "Typo 'Save As..' should be fixed to 'Save As...'");

        String accelerator = root.getAttribute("accelerator");
        if (accelerator != null && !accelerator.isEmpty()) {
            String normalized = accelerator.trim().toUpperCase(Locale.ROOT);
            assertFalse(forbiddenNormalized.contains(normalized),
                    "Grid editing key must not be bound as a MenuBar accelerator: " + accelerator);
            KeyCombination combo = KeyCombination.keyCombination(accelerator);
            String previous = accelerators.put(combo, fxId);
            assertNull(previous,
                    "Duplicate MenuBar accelerator " + accelerator + " on " + fxId + " and " + previous);
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                collectElements((Element) child, fxmlIds, accelerators, forbiddenNormalized);
            }
        }
    }

    @Test
    void fxmlMenuBarStructureAndControllerParity() throws Exception {
        Document doc = loadDesignLaddersFxml();

        NodeList menuBars = doc.getElementsByTagName("MenuBar");
        assertEquals(1, menuBars.getLength(), "Expected exactly one MenuBar in DesignLadders.fxml");
        Element menuBar = (Element) menuBars.item(0);

        List<String> topMenus = new java.util.ArrayList<>();
        NodeList barChildren = menuBar.getChildNodes();
        for (int i = 0; i < barChildren.getLength(); i++) {
            Node node = barChildren.item(i);
            if (node instanceof Element) {
                Element el = (Element) node;
                if ("menus".equals(el.getTagName())) {
                    NodeList menus = el.getChildNodes();
                    for (int j = 0; j < menus.getLength(); j++) {
                        if (menus.item(j) instanceof Element) {
                            Element menu = (Element) menus.item(j);
                            if ("Menu".equals(menu.getTagName())) {
                                topMenus.add(menu.getAttribute("text"));
                            }
                        }
                    }
                } else if ("Menu".equals(el.getTagName())) {
                    topMenus.add(el.getAttribute("text"));
                }
            }
        }
        assertEquals(List.of("File", "Edit", "Command", "Tab", "View", "Tools"), topMenus);

        Set<String> fxmlIds = new HashSet<>();
        Map<KeyCombination, String> accelerators = new HashMap<>();
        Set<String> forbiddenAccelerators = Set.of(
                "ENTER", "SLASH", "LEFT", "UP", "RIGHT", "DOWN",
                "SHIFT+LEFT", "SHIFT+UP", "SHIFT+RIGHT", "SHIFT+DOWN");
        collectElements(doc.getDocumentElement(), fxmlIds, accelerators, forbiddenAccelerators);

        assertFalse(fxmlIds.contains("menuEditSelectLeft"));
        assertFalse(fxmlIds.contains("menuEditSelectUp"));
        assertFalse(fxmlIds.contains("menuEditSelectRight"));
        assertFalse(fxmlIds.contains("menuEditSelectDown"));
        assertTrue(fxmlIds.contains("menuViewLanguage"));
        assertTrue(fxmlIds.contains("menuViewLanguageAuto"));
        assertTrue(fxmlIds.contains("menuViewLanguageEnglish"));
        assertTrue(fxmlIds.contains("menuViewLanguageJapanese"));

        Set<String> controllerFxmlFields = new HashSet<>();
        for (Field field : DesignLaddersController.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(FXML.class)) {
                controllerFxmlFields.add(field.getName());
            }
        }

        Set<String> missingInController = new HashSet<>(fxmlIds);
        missingInController.removeAll(controllerFxmlFields);
        assertTrue(missingInController.isEmpty(),
                "fx:id values missing @FXML fields in DesignLaddersController: " + missingInController);

        Set<String> missingInFxml = new HashSet<>(controllerFxmlFields);
        missingInFxml.removeAll(fxmlIds);
        assertTrue(missingInFxml.isEmpty(),
                "@FXML fields missing fx:id in DesignLadders.fxml: " + missingInFxml);
    }

    @Test
    void isEditorKeyRoutesHandledKeysAndAllowsMenuAccelerators() {
        assertFalse(LadderKeyHandler.isEditorKey(null));

        // Handled grid and LadderKeyHandler keys
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.ENTER, false, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.DELETE, false, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.DELETE, true, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.INSERT, true, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.SLASH, false, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.SLASH, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.LEFT, false, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.UP, false, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.RIGHT, false, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.DOWN, false, false, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.LEFT, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.UP, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.RIGHT, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.DOWN, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.N, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.O, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.S, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.Z, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.Y, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.X, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.C, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.V, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.T, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F4, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F2, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.TAB, false, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.TAB, true, true, false)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.LEFT, false, false, true)));
        assertTrue(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.RIGHT, false, false, true)));

        // Menu-only accelerators must NOT be consumed by ScrollPane filter
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.P, false, true, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.S, true, true, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F, true, true, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.I, false, true, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F2, false, false, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F5, false, false, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F5, true, false, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F7, false, false, false)));
        assertFalse(LadderKeyHandler.isEditorKey(keyEvent(KeyCode.F8, false, false, false)));
    }

    @Test
    void blockCapabilityHelpersMatchSupportedInstructionTypes() {
        assertFalse(DesignLaddersController.isInvertibleBlock(null));
        assertFalse(DesignLaddersController.isInvertibleBlock(LadderBlock.EMPTY));
        assertTrue(DesignLaddersController.isInvertibleBlock(LadderBlock.LOAD));
        assertTrue(DesignLaddersController.isInvertibleBlock(LadderBlock.LOAD_NOT));
        assertTrue(DesignLaddersController.isInvertibleBlock(LadderBlock.OUT));
        assertTrue(DesignLaddersController.isInvertibleBlock(LadderBlock.OUT_NOT));

        assertFalse(DesignLaddersController.isEdgeCyclableBlock(null));
        assertFalse(DesignLaddersController.isEdgeCyclableBlock(LadderBlock.EMPTY));
        assertTrue(DesignLaddersController.isEdgeCyclableBlock(LadderBlock.LOAD));
        assertTrue(DesignLaddersController.isEdgeCyclableBlock(LadderBlock.LOAD_NOT));
        assertTrue(DesignLaddersController.isEdgeCyclableBlock(LadderBlock.OUT));
        assertTrue(DesignLaddersController.isEdgeCyclableBlock(LadderBlock.OUT_NOT));

        int invertibleCount = 0;
        int edgeCyclableCount = 0;
        for (LadderBlock block : LadderBlock.values()) {
            if (DesignLaddersController.isInvertibleBlock(block)) {
                invertibleCount++;
            }
            if (DesignLaddersController.isEdgeCyclableBlock(block)) {
                edgeCyclableCount++;
                assertTrue(DesignLaddersController.isInvertibleBlock(block),
                        "Every edge-cyclable block should also be invertible: " + block);
            }
        }
        assertEquals(24, invertibleCount);
        assertEquals(12, edgeCyclableCount);
    }

    @Test
    void numericSettingParsersValidateBoundsAndRejectInvalidInput() {
        assertEquals(0L, DesignLaddersController.parseIdealCycleTime("0"));
        assertEquals(25L, DesignLaddersController.parseIdealCycleTime(" 25 "));
        assertNull(DesignLaddersController.parseIdealCycleTime("-1"));
        assertNull(DesignLaddersController.parseIdealCycleTime("abc"));
        assertNull(DesignLaddersController.parseIdealCycleTime(""));
        assertNull(DesignLaddersController.parseIdealCycleTime(null));

        assertEquals(-1, DesignLaddersController.parseHistoryLimit("-1"));
        assertEquals(0, DesignLaddersController.parseHistoryLimit("0"));
        assertEquals(100, DesignLaddersController.parseHistoryLimit(" 100 "));
        assertNull(DesignLaddersController.parseHistoryLimit("-2"));
        assertNull(DesignLaddersController.parseHistoryLimit("xyz"));
        assertNull(DesignLaddersController.parseHistoryLimit(" "));
        assertNull(DesignLaddersController.parseHistoryLimit(null));
    }

    @Test
    void matchesSearchQueryMatchesAddressCommentAndFunctionAddresses() {
        LadderGrid grid = new LadderGrid(0, 0);
        grid.setAddress("X000");
        grid.setComment("Start Motor");
        grid.getBlockFunctions()[0].setAddress("D100");
        grid.getBlockFunctions()[1].setAddress("D200");

        assertFalse(DesignLaddersController.matchesSearchQuery(grid, "x0"));

        grid.setBlock(LadderBlock.LOAD);
        assertTrue(DesignLaddersController.matchesSearchQuery(grid, "x0"));
        assertTrue(DesignLaddersController.matchesSearchQuery(grid, "motor"));
        assertTrue(DesignLaddersController.matchesSearchQuery(grid, "d100"));
        assertTrue(DesignLaddersController.matchesSearchQuery(grid, "d200"));

        grid.getBlockFunctions()[1].setNumber(true);
        assertFalse(DesignLaddersController.matchesSearchQuery(grid, "d200"));

        assertFalse(DesignLaddersController.matchesSearchQuery(grid, "Y999"));
        assertFalse(DesignLaddersController.matchesSearchQuery(grid, ""));
        assertFalse(DesignLaddersController.matchesSearchQuery(grid, null));
        assertFalse(DesignLaddersController.matchesSearchQuery(null, "x0"));
    }

    @Test
    void commandCatalogCoversEveryCommandBlockWithoutDuplicates() {
        Set<LadderBlock> expectedBlocks = new HashSet<>(List.of(LadderBlock.values()));
        expectedBlocks.remove(LadderBlock.CONTENTS);
        expectedBlocks.remove(LadderBlock.EMPTY);
        expectedBlocks.remove(LadderBlock.CONNECT_LINE);

        List<LadderCommandCatalog.CommandSpec> entries = LadderCommandCatalog.entries();
        assertEquals(expectedBlocks.size(), entries.size());

        Set<LadderBlock> seen = new HashSet<>();
        for (LadderCommandCatalog.CommandSpec spec : entries) {
            assertTrue(seen.add(spec.block()), "Duplicate block in catalog: " + spec.block());
            assertNotNull(spec.category());
            assertNotNull(spec.placement());
            assertFalse(spec.mnemonic().isBlank());
            assertFalse(spec.inputPrefix().isEmpty());
            assertFalse(spec.menuLabel().isBlank());
            assertFalse(spec.syntaxHint().isBlank());
            assertEquals(spec.block(), LadderBlock.get(spec.mnemonic()));
            assertSame(spec, LadderCommandCatalog.findByBlock(spec.block()));
        }
        assertEquals(expectedBlocks, seen);

        assertEquals(LadderBlock.LOAD, LadderCommandCatalog.findByInput("ld x0").block());
        assertEquals(LadderBlock.TIMER_NOT, LadderCommandCatalog.findByInput("/TIM T0 1000").block());
        assertEquals(LadderBlock.BLOCK_COMMENT, LadderCommandCatalog.findByInput(";hello").block());
        assertEquals(LadderBlock.COMPARISON_EQUAL, LadderCommandCatalog.findByInput("= D0 10").block());
        assertEquals(LadderBlock.SCRIPT, LadderCommandCatalog.findByInput("SCR X0; fn()").block());
        assertNull(LadderCommandCatalog.findByInput("/"));
        assertNull(LadderCommandCatalog.findByInput("   "));
        assertNull(LadderCommandCatalog.findByInput(null));

        assertEquals("LD <address> [; comment]", LadderCommandCatalog.hintForInput("ld X0"));
        assertTrue(LadderCommandCatalog.hintForInput("unknown").contains("<CMD>"));
    }

    @Test
    void isCommandTriggerRecognizesDirectCommandEntryKeys() {
        List<String> validChars = List.of("l", "O", ";", "/", "@", "%", "=", "<", ">", "&", "|", "^");
        for (String ch : validChars) {
            assertTrue(LadderKeyHandler.isCommandTriggerText(ch), "Expected trigger char: " + ch);
            KeyEvent plainTyped = new KeyEvent(KeyEvent.KEY_TYPED, ch, ch, KeyCode.UNDEFINED,
                    false, false, false, false);
            assertTrue(LadderKeyHandler.isCommandTriggerChar(plainTyped), "Expected KeyEvent trigger: " + ch);

            KeyEvent ctrlTyped = new KeyEvent(KeyEvent.KEY_TYPED, ch, ch, KeyCode.UNDEFINED,
                    false, true, false, false);
            KeyEvent altTyped = new KeyEvent(KeyEvent.KEY_TYPED, ch, ch, KeyCode.UNDEFINED,
                    false, false, true, false);
            KeyEvent metaTyped = new KeyEvent(KeyEvent.KEY_TYPED, ch, ch, KeyCode.UNDEFINED,
                    false, false, false, true);
            assertFalse(LadderKeyHandler.isCommandTriggerChar(ctrlTyped));
            assertFalse(LadderKeyHandler.isCommandTriggerChar(altTyped));
            assertFalse(LadderKeyHandler.isCommandTriggerChar(metaTyped));
        }

        for (String nonTrigger : List.of("\r", "\n", "\t", "\u001B", "\u007F", " ", "0", "", "LD")) {
            assertFalse(LadderKeyHandler.isCommandTriggerText(nonTrigger), "Unexpected trigger: " + nonTrigger);
        }
        assertFalse(LadderKeyHandler.isCommandTriggerText(null));
        assertFalse(LadderKeyHandler.isCommandTriggerChar(null));
    }

    @Test
    void formatGridCommandFormatsBlocksForInlineEditing() {
        assertEquals("", LadderPane.formatGridCommand(null));

        LadderGrid empty = new LadderGrid(1, 1);
        assertEquals("", LadderPane.formatGridCommand(empty));

        LadderGrid comment = new LadderGrid(1, 1);
        comment.setBlock(LadderBlock.BLOCK_COMMENT);
        comment.setComment("hello");
        assertEquals(";hello", LadderPane.formatGridCommand(comment));

        LadderGrid load = new LadderGrid(1, 1);
        load.setBlock(LadderBlock.LOAD);
        load.setAddress("X0");
        assertEquals("LD X0", LadderPane.formatGridCommand(load));

        LadderGrid timer = new LadderGrid(9, 1);
        timer.setBlock(LadderBlock.TIMER);
        timer.setAddress("T0");
        timer.getBlockFunctions()[0].setNumber(true);
        timer.getBlockFunctions()[0].setValue(100);
        timer.getBlockFunctions()[0].setRadix(LadderNumber.RADIX_DECIMAL);
        assertEquals("TIM T0 100.0", LadderPane.formatGridCommand(timer));

        LadderGrid add = new LadderGrid(9, 1);
        add.setBlock(LadderBlock.ADDITION);
        add.setAddress("D0");
        add.getBlockFunctions()[0].setAddress("D1");
        add.getBlockFunctions()[1].setAddress("D2");
        assertEquals("ADD D0 D1 D2", LadderPane.formatGridCommand(add));

        LadderGrid script = new LadderGrid(9, 1);
        script.setBlock(LadderBlock.SCRIPT);
        script.setAddress("X0");
        script.setBlockScript("run()");
        assertEquals("SCR X0; run()", LadderPane.formatGridCommand(script));
    }
}
