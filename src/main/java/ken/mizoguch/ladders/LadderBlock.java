/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * the blocks of a ladder : the name of the enum is the name used by the json representation of a
 * ladder, and the command is the text shown in the ladder and written by the import and the export
 * of the ladder.
 *
 * @author mizoguch-ken
 */
public enum LadderBlock {
    // Contents
    CONTENTS(""),
    // Empty
    EMPTY(""),
    // Comment
    BLOCK_COMMENT(";"),
    // Connect
    CONNECT_LINE(""),
    // Load
    LOAD("LD"),
    LOAD_NOT("/LD"),
    LOAD_RISING("@LD"),
    LOAD_RISING_NOT("/@LD"),
    LOAD_FALLING("%LD"),
    LOAD_FALLING_NOT("/%LD"),
    // Out
    OUT("OUT"),
    OUT_NOT("/OUT"),
    OUT_RISING("@OUT"),
    OUT_RISING_NOT("/@OUT"),
    OUT_FALLING("%OUT"),
    OUT_FALLING_NOT("/%OUT"),
    // Load Function
    COMPARISON_EQUAL("="),
    COMPARISON_NOT_EQUAL("<>"),
    COMPARISON_LESS("<"),
    COMPARISON_LESS_EQUAL("<="),
    COMPARISON_GREATER(">"),
    COMPARISON_GREATER_EQUAL(">="),
    COMPARISON_AND_BITS("&"),
    COMPARISON_OR_BITS("|"),
    COMPARISON_XOR_BITS("^"),
    // Out Function
    SET("SET"),
    RESET("RES"),
    AND_BITS("AND"),
    OR_BITS("OR"),
    XOR_BITS("XOR"),
    NOT_BITS("NOT"),
    ADDITION("ADD"),
    SUBTRACTION("SUB"),
    MULTIPLICATION("MUL"),
    DIVISION("DIV"),
    AVERAGE("AVE"),
    SHIFT_LEFT_BITS("SFL"),
    SHIFT_RIGHT_BITS("SFR"),
    SIGMOID("SIG"),
    RANDOM("RND"),
    TIMER("TIM"),
    TIMER_NOT("/TIM"),
    COUNTER("CNT"),
    COUNTER_NOT("/CNT"),
    MOVE("MOV"),
    // Collaboration
    SCRIPT("SCR");

    private final String command_;

    /**
     * the block of every command : the first block of the enum keeps the command when several
     * blocks hold it, as the search of the blocks used to answer
     */
    private static final Map<String, LadderBlock> COMMANDS_ = new HashMap<>();

    static {
        for (LadderBlock block : LadderBlock.values()) {
            COMMANDS_.putIfAbsent(block.command_, block);
        }
    }

    private LadderBlock(final String command) {
        command_ = command;
    }

    public String toCommand() {
        return command_;
    }

    /**
     * the block reads the value of its address as a contact : -| |-, -|/|- ...
     *
     * @return true when the block is a contact
     */
    public boolean isLoad() {
        return switch (this) {
            case LOAD, LOAD_NOT, LOAD_RISING, LOAD_RISING_NOT, LOAD_FALLING, LOAD_FALLING_NOT ->
                true;
            default ->
                false;
        };
    }

    /**
     * the block compares or calculates the value of its address : =, <>, <, &, ...
     *
     * @return true when the block is a comparison
     */
    public boolean isComparison() {
        return switch (this) {
            case COMPARISON_EQUAL, COMPARISON_NOT_EQUAL, COMPARISON_LESS, COMPARISON_LESS_EQUAL,
                    COMPARISON_GREATER, COMPARISON_GREATER_EQUAL, COMPARISON_AND_BITS,
                    COMPARISON_OR_BITS, COMPARISON_XOR_BITS ->
                true;
            default ->
                false;
        };
    }

    /**
     * the block runs on the value of its address and stops the route when its value is
     * not run : a contact or a comparison
     *
     * @return true when the block is a contact or a comparison
     */
    public boolean isContact() {
        return isLoad() || isComparison();
    }

    /**
     *
     * @param command command of a block, without case
     * @return the block of the command, EMPTY when no block holds the command
     */
    public static LadderBlock get(String command) {
        LadderBlock block = COMMANDS_.get(command.toUpperCase(Locale.ROOT));
        return (block != null) ? block : EMPTY;
    }
}
