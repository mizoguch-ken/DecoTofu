/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * the tabs of the editor of the ladders : a tab shows the grid of a ladder, and the name of the tab
 * is the local address of the io of the ladder.
 *
 * the tabs are created, removed and moved by the commands of the history of the editor, so this
 * class only asks the commands to change a tab and it controls the name and the selection of the
 * tabs
 *
 * @author mizoguch-ken
 */
class LadderTabs {

    /**
     * the name which a new ladder gets when no other ladder holds it
     */
    private static final String NEW_LADDER_NAME = "Ladders";

    private final Ladders ladders_;

    LadderTabs(Ladders ladders) {
        ladders_ = ladders;
    }

    /**
     *
     * @return true when the tab is created
     */
    boolean create() {
        TabPane tabPane = ladders_.getTabLadder();
        int index;
        LadderPane pane;

        index = tabPane.getTabs().size() + 1;
        pane = ladders_.getLadderCommand().ladderCreate(index, newName(usedNames(tabPane)), LadderDefaults.LADDER_DEFAULT_GRID_COLUMN,
                LadderDefaults.LADDER_DEFAULT_GRID_ROW, LadderDefaults.LADDER_DEFAULT_GRID_MIN_SIZE, LadderDefaults.LADDER_DEFAULT_GRID_MAX_SIZE,
                LadderDefaults.LADDER_DEFAULT_GRID_CONTENTS_WIDTH, LadderDefaults.LADDER_DEFAULT_GRID_CONTENTS_HIGHT);
        tabPane.getSelectionModel().select(pane.getLadder().getIdx() - 1);
        ladders_.setChanged(true);

        ladders_.setTitle();
        return true;
    }

    /**
     *
     * @param index index of the tab given by the selection model of the tab pane
     * @return true when the tab is removed
     */
    boolean remove(int index) {
        ladders_.getLadderCommand().ladderRemove(index);
        ladders_.setChanged(true);

        ladders_.setTitle();
        return true;
    }

    /**
     * ask the name of the ladder of the pane to the user, and change the name of the ladder when no
     * other ladder holds the name
     *
     * @param pane pane of the ladder to rename
     * @return true when the name is changed
     */
    boolean rename(LadderPane pane) {
        Stage stage = ladders_.getStage();
        List<Image> icons = ladders_.getIcons();
        TextInputDialog alert = new TextInputDialog(pane.getLadder().getName());

        alert.initOwner(stage);
        alert.initStyle(StageStyle.UTILITY);
        if (!icons.isEmpty()) {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons);
        }

        alert.setTitle("Rename");
        alert.setHeaderText(null);
        alert.setContentText(null);
        alert.setGraphic(null);
        Optional<String> result = alert.showAndWait();
        stage.requestFocus();

        if (result.isPresent()) {
            String name = result.get().trim();
            if (checkName(name)) {
                addressOf(pane.getLadder().getIdx()).setAddress(name.replace(" ", "_"));
                pane.setChanged(true);
                ladders_.getLadderCommand().ladderChangeName(pane, name);
                ladders_.setChanged(true);

                ladders_.setTitle();
                return true;
            } else {
                ladders_.writeLog("Duplicate names", true);
            }
        }
        return false;
    }

    /**
     *
     * @param idx index of the ladder, from one
     * @param name name of the ladder
     * @return true when the name is changed
     */
    boolean rename(int idx, String name) {
        name = name.trim();
        if (checkName(name)) {
            LadderPane pane = paneOf(idx - 1);
            addressOf(idx).setAddress(name.replace(" ", "_"));
            pane.setChanged(true);
            ladders_.getLadderCommand().ladderChangeName(pane, name);
            ladders_.setChanged(true);

            ladders_.setTitle();
            return true;
        } else {
            ladders_.writeLog("Duplicate names", true);
        }
        return false;
    }

    /**
     * the name of a ladder is the local address of its io : no other ladder can hold the name
     *
     * @param name name of a ladder
     * @return true when no ladder holds the name
     */
    boolean checkName(String name) {
        if (name == null) {
            return false;
        }
        return !isNameUsed(name, usedNames(ladders_.getTabLadder()));
    }

    /**
     * the name which a new tab shows : Ladders when no ladder holds that name, and the name with
     * the first number from two which no ladder holds when Ladders is used
     *
     * @param usedNames names of the ladders which the editor shows
     * @return a name which no ladder holds
     */
    static String newName(Collection<String> usedNames) {
        String name = NEW_LADDER_NAME;
        int number = 1;

        while (isNameUsed(name, usedNames)) {
            name = NEW_LADDER_NAME + " " + (++number);
        }
        return name;
    }

    /**
     * the name of a ladder is written with a space in the tab which shows the ladder, and it is
     * written with an underscore in the io of the ladder : the names are answered as addresses, so a
     * name which holds a space is not taken for a free name
     *
     * @param name name of a ladder
     * @param usedNames names of the ladders which the editor shows
     * @return true when one of the names answers the same address as the name
     */
    private static boolean isNameUsed(String name, Collection<String> usedNames) {
        String address, otherAddress;

        address = address(name);
        for (String usedName : usedNames) {
            otherAddress = address(usedName);
            if (otherAddress.equals(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param name name of a ladder
     * @return the local address of the io of the ladder
     */
    private static String address(String name) {
        return name.replace(" ", "_");
    }

    /**
     *
     * @param tabPane tabs of the editor
     * @return the names of the ladders which the tabs show
     */
    private static List<String> usedNames(TabPane tabPane) {
        List<String> names = new ArrayList<>();
        int index, size;

        for (index = 0, size = tabPane.getTabs().size(); index < size; index++) {
            names.add(paneOf(tabPane, index).getLadder().getName());
        }
        return names;
    }

    /**
     *
     * @return the tab which is selected, null when the editor shows no ladder
     */
    Tab selected() {
        return ladders_.getTabLadder().getSelectionModel().getSelectedItem();
    }

    /**
     *
     * @return index of the selected tab, from zero, -1 when the editor shows no ladder
     */
    int selectedIndex() {
        return ladders_.getTabLadder().getSelectionModel().getSelectedIndex();
    }

    /**
     * select the next tab, the first tab when the last tab is selected
     */
    void selectNext() {
        int index = ladders_.getTabLadder().getSelectionModel().getSelectedIndex();
        int size = ladders_.getTabLadder().getTabs().size();

        if (index < (size - 1)) {
            ladders_.ladderChangeSelect(index + 1);
        } else {
            ladders_.ladderChangeSelect(0);
        }
    }

    /**
     * select the previous tab, the last tab when the first tab is selected
     */
    void selectPrevious() {
        int index = ladders_.getTabLadder().getSelectionModel().getSelectedIndex();
        int size = ladders_.getTabLadder().getTabs().size();

        if (index > 0) {
            ladders_.ladderChangeSelect(index - 1);
        } else {
            ladders_.ladderChangeSelect(size - 1);
        }
    }

    /**
     * a file of the ladders holds every ladder of the editor : the tab of a ladder shows the name of
     * its ladder without the mark of a change
     */
    void markSaved() {
        Tab tab;
        LadderPane pane;
        int index, size;

        for (index = 0, size = ladders_.getTabLadder().getTabs().size(); index < size; index++) {
            tab = ladders_.getTabLadder().getTabs().get(index);
            pane = paneOf(index);
            pane.setChanged(false);
            tab.setText(pane.getLadder().getName());
        }
    }

    /**
     *
     * @param index index of the tab, from zero
     * @return the pane of the ladder shown by the tab
     */
    LadderPane paneOf(int index) {
        return paneOf(ladders_.getTabLadder(), index);
    }

    /**
     * a tab shows the grid of a ladder in a scroll pane
     *
     * @param tabPane tabs of the editor
     * @param index index of the tab, from zero
     * @return the pane of the ladder shown by the tab
     */
    private static LadderPane paneOf(TabPane tabPane, int index) {
        return (LadderPane) ((ScrollPane) tabPane.getTabs().get(index).getContent()).getContent();
    }

    /**
     *
     * @param idx index of the ladder, from one
     * @return the row of the io of the ladder which holds its address
     */
    private LadderTreeTableIo addressOf(int idx) {
        TreeTableView<LadderTreeTableIo> treeTableIo = ladders_.getTreeTableIo();

        return treeTableIo.getRoot().getChildren().get(idx).getValue();
    }
}
