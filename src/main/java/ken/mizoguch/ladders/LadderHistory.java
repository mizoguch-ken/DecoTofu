/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

/**
 *
 * @author mizoguch-ken
 */
public class LadderHistory {

    private final LadderCommandType command;
    private LadderJsonLadder original;
    private LadderJsonLadder revised;

    /**
     *
     * @param command
     */
    public LadderHistory(LadderCommandType command) {
        this.command = command;
        this.original = null;
        this.revised = null;
    }

    /**
     *
     * @return
     */
    public LadderCommandType getCommand() {
        return command;
    }

    /**
     *
     * @return
     */
    public LadderJsonLadder getOriginal() {
        return original;
    }

    /**
     *
     * @param jsonLadder
     */
    public void setOriginal(LadderJsonLadder jsonLadder) {
        original = jsonLadder;
    }

    /**
     *
     * @return
     */
    public LadderJsonLadder getRevised() {
        return revised;
    }

    /**
     *
     * @param jsonLadder
     */
    public void setRevised(LadderJsonLadder jsonLadder) {
        revised = jsonLadder;
    }
}
