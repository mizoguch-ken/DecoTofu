package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for all user-enterable ladder block commands, categories,
 * placement constraints, menu labels, and syntax hints.
 */
final class LadderCommandCatalog {

    enum Category {
        CONTACT("Contact"),
        COMPARISON("Comparison"),
        OUTPUT("Output"),
        TIMER_COUNTER("Timer / Counter"),
        ARITHMETIC("Arithmetic / Data"),
        BITWISE("Bitwise"),
        OTHER("Other");

        private final String title_;

        Category(String title) {
            title_ = title;
        }

        String title() {
            return title_;
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
            String menuLabel,
            String syntaxHint,
            boolean separatorBefore) {
    }

    private static final String DEFAULT_HINT = "<CMD> <args...> [; comment]  (Enter: Apply, Esc: Cancel)";

    private static final List<CommandSpec> ENTRIES = List.of(
            // Contact (CONTACT_SIDE)
            spec(LadderBlock.LOAD, Category.CONTACT, Placement.CONTACT_SIDE,
                    "LD", "LD ", "LD <addr>  — Load Contact", "LD <address> [; comment]", false),
            spec(LadderBlock.LOAD_NOT, Category.CONTACT, Placement.CONTACT_SIDE,
                    "/LD", "/LD ", "/LD <addr>  — Load Inverted", "/LD <address> [; comment]", false),
            spec(LadderBlock.LOAD_RISING, Category.CONTACT, Placement.CONTACT_SIDE,
                    "@LD", "@LD ", "@LD <addr>  — Load Rising Edge", "@LD <address> [; comment]", false),
            spec(LadderBlock.LOAD_RISING_NOT, Category.CONTACT, Placement.CONTACT_SIDE,
                    "/@LD", "/@LD ", "/@LD <addr>  — Load Rising Inverted", "/@LD <address> [; comment]", false),
            spec(LadderBlock.LOAD_FALLING, Category.CONTACT, Placement.CONTACT_SIDE,
                    "%LD", "%LD ", "%LD <addr>  — Load Falling Edge", "%LD <address> [; comment]", false),
            spec(LadderBlock.LOAD_FALLING_NOT, Category.CONTACT, Placement.CONTACT_SIDE,
                    "/%LD", "/%LD ", "/%LD <addr>  — Load Falling Inverted", "/%LD <address> [; comment]", false),

            // Comparison (CONTACT_SIDE)
            spec(LadderBlock.COMPARISON_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE,
                    "=", "= ", "= <addr> <val>  — Equal", "= <address> <value> [; comment]", false),
            spec(LadderBlock.COMPARISON_NOT_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE,
                    "<>", "<> ", "<> <addr> <val>  — Not Equal", "<> <address> <value> [; comment]", false),
            spec(LadderBlock.COMPARISON_LESS, Category.COMPARISON, Placement.CONTACT_SIDE,
                    "<", "< ", "< <addr> <val>  — Less Than", "< <address> <value> [; comment]", false),
            spec(LadderBlock.COMPARISON_LESS_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE,
                    "<=", "<= ", "<= <addr> <val>  — Less or Equal", "<= <address> <value> [; comment]", false),
            spec(LadderBlock.COMPARISON_GREATER, Category.COMPARISON, Placement.CONTACT_SIDE,
                    ">", "> ", "> <addr> <val>  — Greater Than", "> <address> <value> [; comment]", false),
            spec(LadderBlock.COMPARISON_GREATER_EQUAL, Category.COMPARISON, Placement.CONTACT_SIDE,
                    ">=", ">= ", ">= <addr> <val>  — Greater or Equal", ">= <address> <value> [; comment]", false),
            spec(LadderBlock.COMPARISON_AND_BITS, Category.COMPARISON, Placement.CONTACT_SIDE,
                    "&", "& ", "& <addr> <mask>  — Bitwise AND Test", "& <address> <mask> [; comment]", true),
            spec(LadderBlock.COMPARISON_OR_BITS, Category.COMPARISON, Placement.CONTACT_SIDE,
                    "|", "| ", "| <addr> <mask>  — Bitwise OR Test", "| <address> <mask> [; comment]", false),
            spec(LadderBlock.COMPARISON_XOR_BITS, Category.COMPARISON, Placement.CONTACT_SIDE,
                    "^", "^ ", "^ <addr> <mask>  — Bitwise XOR Test", "^ <address> <mask> [; comment]", false),

            // Output (OUTPUT_SIDE)
            spec(LadderBlock.OUT, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "OUT", "OUT ", "OUT <addr>  — Output Coil", "OUT <address> [; comment]", false),
            spec(LadderBlock.OUT_NOT, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "/OUT", "/OUT ", "/OUT <addr>  — Output Inverted", "/OUT <address> [; comment]", false),
            spec(LadderBlock.OUT_RISING, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "@OUT", "@OUT ", "@OUT <addr>  — Output Rising Pulse", "@OUT <address> [; comment]", false),
            spec(LadderBlock.OUT_RISING_NOT, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "/@OUT", "/@OUT ", "/@OUT <addr>  — Output Rising Inverted", "/@OUT <address> [; comment]", false),
            spec(LadderBlock.OUT_FALLING, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "%OUT", "%OUT ", "%OUT <addr>  — Output Falling Pulse", "%OUT <address> [; comment]", false),
            spec(LadderBlock.OUT_FALLING_NOT, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "/%OUT", "/%OUT ", "/%OUT <addr>  — Output Falling Inverted", "/%OUT <address> [; comment]", false),
            spec(LadderBlock.SET, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "SET", "SET ", "SET <addr>  — Set (Latch)", "SET <address> [; comment]", true),
            spec(LadderBlock.RESET, Category.OUTPUT, Placement.OUTPUT_SIDE,
                    "RES", "RES ", "RES <addr>  — Reset (Unlatch)", "RES <address> [; comment]", false),

            // Timer / Counter (OUTPUT_SIDE)
            spec(LadderBlock.TIMER, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE,
                    "TIM", "TIM ", "TIM <addr> <ms>  — Timer", "TIM <address> <ms> [; comment]", false),
            spec(LadderBlock.TIMER_NOT, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE,
                    "/TIM", "/TIM ", "/TIM <addr> <ms>  — Timer Inverted", "/TIM <address> <ms> [; comment]", false),
            spec(LadderBlock.COUNTER, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE,
                    "CNT", "CNT ", "CNT <addr> <count>  — Counter", "CNT <address> <count> [; comment]", false),
            spec(LadderBlock.COUNTER_NOT, Category.TIMER_COUNTER, Placement.OUTPUT_SIDE,
                    "/CNT", "/CNT ", "/CNT <addr> <count>  — Counter Inverted", "/CNT <address> <count> [; comment]", false),

            // Arithmetic / Data (OUTPUT_SIDE)
            spec(LadderBlock.MOVE, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "MOV", "MOV ", "MOV <dest> <src>  — Move", "MOV <dest> <src> [; comment]", false),
            spec(LadderBlock.ADDITION, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "ADD", "ADD ", "ADD <dest> <src1> <src2>  — Add", "ADD <dest> <src1> <src2> [; comment]", true),
            spec(LadderBlock.SUBTRACTION, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "SUB", "SUB ", "SUB <dest> <src1> <src2>  — Subtract", "SUB <dest> <src1> <src2> [; comment]", false),
            spec(LadderBlock.MULTIPLICATION, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "MUL", "MUL ", "MUL <dest> <src1> <src2>  — Multiply", "MUL <dest> <src1> <src2> [; comment]", false),
            spec(LadderBlock.DIVISION, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "DIV", "DIV ", "DIV <dest> <src1> <src2>  — Divide", "DIV <dest> <src1> <src2> [; comment]", false),
            spec(LadderBlock.AVERAGE, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "AVE", "AVE ", "AVE <dest> <src1> <src2>  — Average", "AVE <dest> <src1> <src2> [; comment]", false),
            spec(LadderBlock.SIGMOID, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "SIG", "SIG ", "SIG <dest> <src> <gain>  — Sigmoid", "SIG <dest> <src> <gain> [; comment]", false),
            spec(LadderBlock.RANDOM, Category.ARITHMETIC, Placement.OUTPUT_SIDE,
                    "RND", "RND ", "RND <dest>  — Random (0..1)", "RND <dest> [; comment]", false),

            // Bitwise (OUTPUT_SIDE)
            spec(LadderBlock.AND_BITS, Category.BITWISE, Placement.OUTPUT_SIDE,
                    "AND", "AND ", "AND <dest> <src1> <src2>  — Bitwise AND", "AND <dest> <src1> <src2> [; comment]", false),
            spec(LadderBlock.OR_BITS, Category.BITWISE, Placement.OUTPUT_SIDE,
                    "OR", "OR ", "OR <dest> <src1> <src2>  — Bitwise OR", "OR <dest> <src1> <src2> [; comment]", false),
            spec(LadderBlock.XOR_BITS, Category.BITWISE, Placement.OUTPUT_SIDE,
                    "XOR", "XOR ", "XOR <dest> <src1> <src2>  — Bitwise XOR", "XOR <dest> <src1> <src2> [; comment]", false),
            spec(LadderBlock.NOT_BITS, Category.BITWISE, Placement.OUTPUT_SIDE,
                    "NOT", "NOT ", "NOT <dest> <src>  — Bitwise NOT", "NOT <dest> <src> [; comment]", false),
            spec(LadderBlock.SHIFT_LEFT_BITS, Category.BITWISE, Placement.OUTPUT_SIDE,
                    "SFL", "SFL ", "SFL <dest> <src> <bits>  — Shift Left", "SFL <dest> <src> <bits> [; comment]", true),
            spec(LadderBlock.SHIFT_RIGHT_BITS, Category.BITWISE, Placement.OUTPUT_SIDE,
                    "SFR", "SFR ", "SFR <dest> <src> <bits>  — Shift Right", "SFR <dest> <src> <bits> [; comment]", false),

            // Other
            spec(LadderBlock.BLOCK_COMMENT, Category.OTHER, Placement.CONTACT_SIDE,
                    ";", ";", "; <comment>  — Block Comment", "; <comment>", false),
            spec(LadderBlock.SCRIPT, Category.OTHER, Placement.OUTPUT_SIDE,
                    "SCR", "SCR ", "SCR <addr>; <script>  — Script", "SCR <address>; <script> [; comment]", false));

    private static final Map<LadderBlock, CommandSpec> BY_BLOCK = buildByBlock();
    private static final Map<Category, List<CommandSpec>> BY_CATEGORY = buildByCategory();

    private LadderCommandCatalog() {
    }

    private static CommandSpec spec(LadderBlock block, Category category, Placement placement,
            String mnemonic, String inputPrefix, String menuLabel, String syntaxHint, boolean separatorBefore) {
        return new CommandSpec(block, category, placement, mnemonic, inputPrefix, menuLabel, syntaxHint,
                separatorBefore);
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
        return (spec != null) ? spec.syntaxHint() : DEFAULT_HINT;
    }
}
