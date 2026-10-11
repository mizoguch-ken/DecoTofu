/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

/**
 * the commands which the undo history of the ladders can take back : the name of the enum is the
 * name used by the json representation of the history of the editor.
 *
 * @author mizoguch-ken
 */
public enum LadderCommandType {
    // root
    CHANGE_ADDRESS,
    CHANGE_COMMENT,
    // ladder
    LADDER_CREATE,
    LADDER_REMOVE,
    LADDER_REMOVE_ROW,
    LADDER_INSERT_ROW,
    LADDER_MOVE_LEFT,
    LADDER_MOVE_RIGHT,
    LADDER_CHANGE_NAME,
    // block
    BLOCK_CHANGE;
}
