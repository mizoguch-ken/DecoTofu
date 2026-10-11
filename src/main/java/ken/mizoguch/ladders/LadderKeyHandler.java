/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;

/**
 * the keys of the editor of the ladders : the keys with the shortcut of the system create, remove or
 * rename a ladder, open and save the file of the ladders, and take back or run again a change of the
 * ladders.
 *
 * the keys without the shortcut are used by the grid of the ladder shown by a tab, and the grid
 * answers if it has run the key
 *
 * @author mizoguch-ken
 */
class LadderKeyHandler {

    private final Ladders ladders_;

    LadderKeyHandler(Ladders ladders) {
        ladders_ = ladders;
    }

    /**
     *
     * @param pane pane of the ladder which has the focus
     * @param event key pressed on the pane
     * @param scrollPane scroll pane which shows the pane of the ladder
     * @return true when the key is run by the editor or by the ladder
     */
    boolean handle(LadderPane pane, KeyEvent event, ScrollPane scrollPane) {
        Tab tab = ladders_.getTabs().selected();

        if (tab != null) {
            switch (event.getCode()) {
                case TAB:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladders_.getTabs().selectNext();
                        return true;
                    } else if (event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladders_.getTabs().selectPrevious();
                        return true;
                    }
                    break;
                case F2:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladders_.getTabs().rename(pane);
                        return true;
                    }
                    break;
                case F4:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        if (!ladders_.isCycling()) {
                            return ladders_.getTabs().remove(ladders_.getTabs().selectedIndex());
                        }
                        return false;
                    }
                    break;
                case LEFT:
                    if (!event.isShiftDown() && !event.isShortcutDown() && event.isAltDown()) {
                        if (!ladders_.isCycling()) {
                            return ladders_.ladderMoveLeft(pane.getLadder());
                        }
                        return false;
                    }
                    break;
                case RIGHT:
                    if (!event.isShiftDown() && !event.isShortcutDown() && event.isAltDown()) {
                        if (!ladders_.isCycling()) {
                            return ladders_.ladderMoveRight(pane.getLadder());
                        }
                        return false;
                    }
                    break;
                case N:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladders_.fileNew();
                        return true;
                    }
                    break;
                case O:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        if (ladders_.fileOpen(null)) {
                            ladders_.getDesignController().addRecentFile(ladders_.getFilePath().toString());
                        }
                        return true;
                    }
                    break;
                case S:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        if (ladders_.fileSave()) {
                            ladders_.getDesignController().addRecentFile(ladders_.getFilePath().toString());
                        }
                        return true;
                    } else if (!event.isShiftDown() && event.isShortcutDown() && event.isAltDown()) {
                        if (ladders_.fileSaveAs()) {
                            ladders_.getDesignController().addRecentFile(ladders_.getFilePath().toString());
                        }
                        return true;
                    }
                    break;
                case T:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladders_.ladderNewTab();
                        return true;
                    }
                    break;
                case Y:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladders_.redo(tab, pane);
                        return true;
                    }
                    break;
                case Z:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladders_.undo(tab, pane);
                        return true;
                    }
                    break;
                default:
                    break;
            }

            if (pane.onKeyPressed(event, scrollPane)) {
                if (pane.isChanged()) {
                    ladders_.setChanged(true);

                    tab.setText(pane.getLadder().getName() + " *");
                    ladders_.setTitle();
                    return true;
                }
            }
        }
        return false;
    }
}
