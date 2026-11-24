package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import ken.mizoguch.decotofu.I18n;

/**
 * Single source of truth for all user-enterable ladder block commands, categories,
 * placement constraints, menu labels, and syntax hints.
 */
final class LadderCommandCatalog {

    enum Category {
        CONTACT,
        COMPARISON,
        OUTPUT,
        TIMER_COUNTER,
        ARITHMETIC,
        BITWISE,
        OTHER;

        String title() {
            return I18n.get("command.cat." + name());
        }
    }

    enum Placement {
        CONTACT_SIDE,
        OUTPUT_SIDE
    }

    record CommandSpec(
            LadderBlock block,
            Category category,
            Placement placement,
            String mnemonic,
            String inputPrefix,
            String labelKey,
            String hintKey,
            boolean separatorBefore) {

        String menuLabel() {
            return I18n.get(labelKey);
        }

        String syntaxHint() {
            return I18n.get(hintKey);
        }
    }

    private static final String DEFAULT_HINT_KEY = "command.defaultHint";

    private static final List<CommandSpec> ENTRIES = List.of(
            // Contact (CONTACT_SIDE)
            spec(LadderBlock.LOAD, Category.CONTACT, Placement.CONTACT_SIDE, "LD", "LD ", false),
            spec(LadderBlock.LOAD_NOT, Category.CONTACT, Placement.CONTACT_SIDE, "/LD", "/LD ", false),
            spec(LadderBlock.LOAD_RISING, Category.CONTACT, Placement.CONTACT_SIDE, "@LD", "@LD ", false),
            spec(LadderBlock.LOAD_RISING_NOT, Category.CONTACT, Placement.CONTACT_SIDE, "/@LD", "/@LD ", false),
            spec(LadderBlock.LOAD_FALLING, Category.CONTACT, Placement.CONTACT_SIDE, "%LD", "%LD ", false),
            spec(LadderBlock.LOAD_FALLING_NOT, Category.CONTACT, Placement.CONTACT_SIDE, "/%LD", "/%LD ", false),

            // Comparison (CONTACT_SIDE)
            spec(LadderBlock.COMPARISON_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE, "=", "= ", false),
            spec(LadderBlock.COMPARISON_NOT_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE, "<>", "<> ", false),
            spec(LadderBlock.COMPARISON_LESS, Category.COMPARISON, Placement.CONTACT_SIDE, "<", "< ", false),
            spec(LadderBlock.COMPARISON_LESS_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE, "<=", "<= ", false),
            spec(LadderBlock.COMPARISON_GREATER, Category.COMPARISON, Placement.CONTACT_SIDE, ">", "> ", false),
            spec(LadderBlock.COMPARISON_GREATER_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE, ">=", ">= ", false),
            spec(LadderBlock.COMPARISON_AND_BITS, Category.COMPARISON, Placement.CONTACT_SIDE, "&", "& ", true),
            spec(LadderBlock.COMPARISON_OR_BITS, Category.COMPARISON, Placement.CONTACT_SIDE, "|", "| ", false),
            spec(LadderBlock.COMPARISON_XOR_BITS, Category.COMPARISON, Placement.CONTACT_SIDE, "^", "^ ", false),

            // Output (OUTPUT_SIDE)
            spec(LadderBlock.OUT, Category.OUTPUT, Placement.OUTPUT_SIDE, "OUT", "OUT ", false),
            spec(LadderBlock.OUT_NOT, Category.OUTPUT, Placement.OUTPUT_SIDE, "/OUT", "/OUT ", false),
            spec(LadderBlock.OUT_RISING, Category.OUTPUT, Placement.OUTPUT_SIDE, "@OUT", "@OUT ", false),
            spec(LadderBlock.OUT_RISING_NOT, Category.OUTPUT, Placement.OUTPUT_SIDE, "/@OUT", "/@OUT ", false),
            spec(LadderBlock.OUT_FALLING, Category.OUTPUT, Placement.OUTPUT_SIDE, "%OUT", "%OUT ", false),
            spec(LadderBlock.OUT_FALLING_NOT, Category.OUTPUT, Placement.OUTPUT_SIDE, "/%OUT", "/%OUT ", false),
            spec(LadderBlock.SET, Category.OUTPUT, Placement.OUTPUT_SIDE, "SET", "SET ", true),
            spec(LadderBlock.RESET, Category.OUTPUT, Placement.OUTPUT_SIDE, "RES", "RES ", false),

            // Timer / Counter (OUTPUT_SIDE)
            spec(LadderBlock.TIMER, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE, "TIM", "TIM ", false),
            spec(LadderBlock.TIMER_NOT, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE, "/TIM", "/TIM ", false),
            spec(LadderBlock.COUNTER, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE, "CNT", "CNT ", false),
            spec(LadderBlock.COUNTER_NOT, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE, "/CNT", "/CNT ", false),

            // Arithmetic / Data (OUTPUT_SIDE)
            spec(LadderBlock.MOVE, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "MOV", "MOV ", false),
            spec(LadderBlock.ADDITION, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "ADD", "ADD ", true),
            spec(LadderBlock.SUBTRACTION, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "SUB", "SUB ", false),
            spec(LadderBlock.MULTIPLICATION, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "MUL", "MUL ", false),
            spec(LadderBlock.DIVISION, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "DIV", "DIV ", false),
            spec(LadderBlock.AVERAGE, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "AVE", "AVE ", false),
            spec(LadderBlock.SIGMOID, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "SIG", "SIG ", false),
            spec(LadderBlock.RANDOM, Category.ARITHMETIC, Placement.OUTPUT_SIDE, "RND", "RND ", false),

            // Bitwise (OUTPUT_SIDE)
            spec(LadderBlock.AND_BITS, Category.BITWISE, Placement.OUTPUT_SIDE, "AND", "AND ", false),
            spec(LadderBlock.OR_BITS, Category.BITWISE, Placement.OUTPUT_SIDE, "OR", "OR ", false),
            spec(LadderBlock.XOR_BITS, Category.BITWISE, Placement.OUTPUT_SIDE, "XOR", "XOR ", false),
            spec(LadderBlock.NOT_BITS, Category.BITWISE, Placement.OUTPUT_SIDE, "NOT", "NOT ", false),
            spec(LadderBlock.SHIFT_LEFT_BITS, Category.BITWISE, Placement.OUTPUT_SIDE, "SFL", "SFL ", true),
            spec(LadderBlock.SHIFT_RIGHT_BITS, Category.BITWISE, Placement.OUTPUT_SIDE, "SFR", "SFR ", false),

            // Other
            spec(LadderBlock.BLOCK_COMMENT, Category.OTHER, Placement.CONTACT_SIDE, ";", ";", false),
            spec(LadderBlock.SCRIPT, Category.OTHER, Placement.OUTPUT_SIDE, "SCR", "SCR ", false));

    private static final Map<LadderBlock, CommandSpec> BY_BLOCK = buildByBlock();
    private static final Map<Category, List<CommandSpec>> BY_CATEGORY = buildByCategory();

    private LadderCommandCatalog() {
    }

    private static CommandSpec spec(LadderBlock block, Category category, Placement placement,
            String mnemonic, String inputPrefix, boolean separatorBefore) {
        return new CommandSpec(block, category, placement, mnemonic, inputPrefix,
                "command.label." + block.name(), "command.hint." + block.name(), separatorBefore);
    }

    private static Map<LadderBlock, CommandSpec> buildByBlock() {
        Map<LadderBlock, CommandSpec> map = new EnumMap<>(LadderBlock.class);
        for (CommandSpec entry : ENTRIES) {
            map.put(entry.block(), entry);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Map<Category, List<CommandSpec>> buildByCategory() {
        Map<Category, List<CommandSpec>> grouped = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            grouped.put(category, new ArrayList<>());
        }
        for (CommandSpec entry : ENTRIES) {
            grouped.get(entry.category()).add(entry);
        }
        Map<Category, List<CommandSpec>> immutable = new EnumMap<>(Category.class);
        for (Map.Entry<Category, List<CommandSpec>> entry : grouped.entrySet()) {
            immutable.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutable);
    }

    static List<CommandSpec> entries() {
        return ENTRIES;
    }

    static List<CommandSpec> entries(Category category) {
        List<CommandSpec> list = (category != null) ? BY_CATEGORY.get(category) : null;
        return (list != null) ? list : List.of();
    }

    static CommandSpec findByBlock(LadderBlock block) {
        return (block != null) ? BY_BLOCK.get(block) : null;
    }

    static CommandSpec findByInput(String rawInput) {
        if (rawInput == null) {
            return null;
        }
        String trimmed = rawInput.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith(";")) {
            return BY_BLOCK.get(LadderBlock.BLOCK_COMMENT);
        }
        int tokenEnd = 0;
        while (tokenEnd < trimmed.length()) {
            char ch = trimmed.charAt(tokenEnd);
            if (Character.isWhitespace(ch) || (ch == ';')) {
                break;
            }
            tokenEnd++;
        }
        if (tokenEnd == 0) {
            return null;
        }
        LadderBlock block = LadderBlock.get(trimmed.substring(0, tokenEnd));
        return (block != LadderBlock.EMPTY) ? BY_BLOCK.get(block) : null;
    }

    static String hintForInput(String rawInput) {
        CommandSpec spec = findByInput(rawInput);
        return (spec != null) ? spec.syntaxHint() : I18n.get(DEFAULT_HINT_KEY);
    }
}
