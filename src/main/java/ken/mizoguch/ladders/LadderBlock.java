/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.util.Locale;

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
        switch (this) {
            case LOAD:
            case LOAD_NOT:
            case LOAD_RISING:
            case LOAD_RISING_NOT:
            case LOAD_FALLING:
            case LOAD_FALLING_NOT:
                return true;
            default:
                return false;
        }
    }

    /**
     * the block compares or calculates the value of its address : =, <>, <, &, ...
     *
     * @return true when the block is a comparison
     */
    public boolean isComparison() {
        switch (this) {
            case COMPARISON_EQUAL:
            case COMPARISON_NOT_EQUAL:
            case COMPARISON_LESS:
            case COMPARISON_LESS_EQUAL:
            case COMPARISON_GREATER:
            case COMPARISON_GREATER_EQUAL:
            case COMPARISON_AND_BITS:
            case COMPARISON_OR_BITS:
            case COMPARISON_XOR_BITS:
                return true;
            default:
                return false;
        }
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
        command = command.toUpperCase(Locale.getDefault());
        for (LadderBlock value : LadderBlock.values()) {
            if (value.command_.equals(command)) {
                return value;
            }
        }
        return EMPTY;
    }
}
