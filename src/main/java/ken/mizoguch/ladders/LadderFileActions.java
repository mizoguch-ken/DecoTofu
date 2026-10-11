/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ken.mizoguch.console.Console;

/**
 * the files of the ladders of the editor : a new ladder, a ladder loaded from a json file, the
 * ladder saved to a json file and the ladders exported to a pdf file.
 *
 * the editor of the ladders keeps the answers of its menus and of its keys as an action on a file, so
 * this class asks the store of the file for the json representation of the ladders and the chooser of
 * the file, and it keeps the state of the editor which follows a file : the path of the ladder, the
 * changed flag and the title of the window.
 *
 * a ladder which is loaded from a file is not trusted : its script blocks can run their javascript in
 * the web page with the apis of the application, so the user is asked before the scripts are kept, and
 * the ladder which the editor was showing is kept as a backup to be restored when the loaded file can
 * not be displayed
 *
 * @author mizoguch-ken
 */
class LadderFileActions {

    private final Ladders ladders_;

    /**
     *
     * @param ladders the editor which shows the ladders
     */
    LadderFileActions(Ladders ladders) {
        ladders_ = ladders;
    }

    /**
     * a new ladder of the editor : the io of the editor and the tabs of the ladders are cleared, and
     * the ladder of a new editor holds the name of the editor
     */
    void ladderNew() {
        ladders_.allClear();

        addGlobalRow();

        ladders_.getLadderCommand().ladderCreate(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX + 1, "Ladders",
                LadderDefaults.LADDER_DEFAULT_GRID_COLUMN, LadderDefaults.LADDER_DEFAULT_GRID_ROW,
                LadderDefaults.LADDER_DEFAULT_GRID_MIN_SIZE, LadderDefaults.LADDER_DEFAULT_GRID_MAX_SIZE,
                LadderDefaults.LADDER_DEFAULT_GRID_CONTENTS_WIDTH, LadderDefaults.LADDER_DEFAULT_GRID_CONTENTS_HIGHT);
    }

    /**
     * add the global address row of the ladder list
     */
    private void addGlobalRow() {
        ladders_.getTreeTableIo().getRoot().getChildren().add(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX,
                new TreeItem<>(new LadderTreeTableIo("Global".replace(" ", "_"), LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX)));
        ladders_.getIoStore().addGlobalRow();
    }

    /**
     *
     * @param file the file of a ladder, null when the user chooses the file
     * @return the loaded file, null when no ladder is loaded
     */
    Path ladderOpen(Path file) {
        return ladderOpen(ladders_.getTabLadder(), ladders_.getTreeTableIo(),
                ladders_.getIoStore().getIoMap(), ladders_.getIoStore().getCommentMap(), file);
    }

    /**
     *
     * @param tabPane tabs of the ladders
     * @param treeTableView list of the io of the ladders
     * @param ioMap io of the ladders
     * @param commentMap comments of the addresses of the ladders
     * @param file the file of a ladder, null when the user chooses the file
     * @return the loaded file, null when no ladder is loaded
     */
    private Path ladderOpen(TabPane tabPane, TreeTableView<LadderTreeTableIo> treeTableView,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap, Path file) {
        LadderFileStore fileStore = ladders_.getFileStore();
        LadderJson ladderJson;
        int scriptCount;
        LadderJson backup;

        if (file == null) {
            File fcfile = fileStore.getFileChooser("JSON", "*.json", null).showOpenDialog(ladders_.getStage());
            if (fcfile == null) {
                return null;
            }
            file = fcfile.toPath();
        }

        ladderJson = fileStore.readJson(file);
        if (ladderJson == null) {
            return null;
        }

        // the script blocks of a file run their javascript in the web page with the apis
        // of the application : the user decides if they are kept
        scriptCount = LadderFileStore.countScripts(ladderJson);

        if (scriptCount > 0) {
            if (!confirmLadderScripts(scriptCount, file)) {
                LadderFileStore.removeScripts(ladderJson);
                ladders_.writeLog("ladder scripts removed [" + scriptCount + "] [" + file.getFileName() + "]", true);
            }
        }

        // allClear() destroys the ladder of the editor : it is kept as a backup to be
        // restored when the loaded file can not be displayed
        backup = ladderJsonSave(tabPane, ioMap, commentMap);
        // the history of the backup is not restored : the undo history is left empty
        backup.setHistoryManager(new LadderHistoryManager(), -1);

        ladders_.allClear();
        addGlobalRow();

        if (ladderJsonLoad(ladders_, tabPane, treeTableView, ioMap, commentMap,
                ladders_.getIoStore().getScriptIoMap(), ladderJson)) {
            return file;
        }

        // the file was readable but the ladder could not be restored : use the backup
        ladders_.writeLog("load failed [" + file.getFileName() + "]", true);
        if (!ladderRestoreBackup(backup, tabPane, treeTableView, ioMap, commentMap)) {
            ladders_.writeLog("the ladder backup could not be restored", true);
        }
        return null;
    }

    /**
     * restore a ladder backed up before the editor is cleared : the undo history is disabled, so that
     * the failure of a load is not added to the history of the user
     *
     * @param backup
     * @param tabPane
     * @param treeTableView
     * @param ioMap
     * @param commentMap
     * @return true when the ladder of the editor is the ladder of the backup
     */
    private boolean ladderRestoreBackup(LadderJson backup, TabPane tabPane,
            TreeTableView<LadderTreeTableIo> treeTableView,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap) {
        boolean restore;

        ladders_.getLadderCommand().setDisableHistory(true);
        try {
            ladders_.allClear();
            addGlobalRow();
            restore = ladderJsonLoad(ladders_, tabPane, treeTableView, ioMap, commentMap,
                    ladders_.getIoStore().getScriptIoMap(), backup);
        } finally {
            ladders_.getLadderCommand().setDisableHistory(false);
        }
        return restore;
    }

    /**
     * ask the user if the scripts of a ladder loaded from a file have to be kept
     *
     * @param count number of the script blocks
     * @param file
     * @return true when the scripts are kept
     */
    private boolean confirmLadderScripts(int count, Path file) {
        Alert alert;
        Optional<ButtonType> result;
        List<Image> icons = ladders_.getIcons();

        if (!ladders_.isScriptEnable()) {
            return false;
        }
        if (!Platform.isFxApplicationThread()) {
            // a dialog can not be shown from an other thread : the scripts are removed
            // because the user of the application can not confirm them, so a ladder which
            // is loaded without the user never runs javascript in the web page
            ladders_.writeLog("ladder scripts are not confirmed : no dialog from an other thread [" + count + "] "
                    + ((file != null) ? "[" + file.getFileName() + "]" : ""), true);
            return false;
        }

        alert = new Alert(Alert.AlertType.CONFIRMATION);
        if ((icons != null) && !icons.isEmpty()) {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons);
        }
        alert.setTitle("Ladder");
        alert.getDialogPane().setHeaderText(null);
        alert.getDialogPane().setContentText("Run Scripts Program? [" + count + "] "
                + ((file != null) ? file.getFileName().toString() : ""));
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        result = alert.showAndWait();
        // a closed dialog never runs the scripts of a file
        return result.orElse(ButtonType.NO) == ButtonType.YES;
    }

    /**
     * save the ladders of the editor to the file of the ladder : a ladder which has no file yet is
     * saved as a new file chosen by the user
     *
     * @return the saved file, null when the save failed or the user closed the chooser
     */
    private Path ladderSave() {
        Path file = ladders_.getFileStore().getFilePath();

        if ((file != null) && Files.exists(file) && Files.isRegularFile(file)) {
            return ladderSave(file);
        }
        return ladderSaveAs();
    }

    /**
     *
     * @param file
     * @return the saved file, null when the save failed
     */
    private Path ladderSave(Path file) {
        if (ladders_.getFileStore().writeJson(file, ladderJsonSave(ladders_.getTabLadder()))) {
            // every ladder of the editor is held by the file
            ladders_.getTabs().markSaved();
            return file;
        }
        return null;
    }

    /**
     *
     * @return the saved file, null when the user closed the chooser or the save failed
     */
    private Path ladderSaveAs() {
        File fcfile = ladders_.getFileStore().getFileChooser("JSON", "*.json", null).showSaveDialog(ladders_.getStage());

        if (fcfile != null) {
            return ladderSave(fcfile.toPath());
        }
        return null;
    }

    /**
     * export the ladders of the editor to a pdf file : the pages are drawn by LadderPdfExporter from
     * the json representation of the ladders, so the editor of the ladders is not occupied with
     * pdfbox
     *
     * @param file
     * @return the exported file
     */
    private Path ladderExportPdf(Path file) {
        LadderJson ladderJson = ladderJsonSave(ladders_.getTabLadder());

        if (ladderJson == null) {
            return null;
        }
        return new LadderPdfExporter(ladderJson, ladders_.getFileName(), ladders_.getIoStore().getCommentMap()).export(file);
    }

    /**
     *
     * @return the exported file, null when the user closed the chooser
     */
    private Path ladderExportPdf() {
        File fcfile = ladders_.getFileStore().getFileChooser("PDF", "*.pdf", ladders_.getFileStore().getFileBaseName())
                .showSaveDialog(ladders_.getStage());

        if (fcfile != null) {
            return ladderExportPdf(fcfile.toPath());
        }
        return null;
    }

    /**
     * the json representation of a file of the ladders, read without changing the editor
     *
     * @param file
     * @return the json representation, null when the file can not be read
     */
    LadderJson ladderJsonOpen(Path file) {
        return ladders_.getFileStore().readJson(file);
    }

    /**
     * the ladders of a json representation : the ladders are restored without the history of the
     * user, so that a file which the difference of two ladders shows is never mixed with the undo
     * history of the editor
     *
     * @param ladders the editor which shows the ladders, null when the ladders are only shown for
     *                the difference of two files
     * @param tabPane tabs of the ladders
     * @param treeTableView list of the io of the ladders
     * @param ioMap io of the ladders
     * @param commentMap comments of the addresses of the ladders
     * @param scriptIoMap io written by the javascript of the script blocks, null when the maps do
     *                    not belong to a cycle
     * @param ladderJson
     * @return true when the ladders of the json representation are shown
     */
    boolean ladderJsonLoad(Ladders ladders, TabPane tabPane, TreeTableView<LadderTreeTableIo> treeTableView,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap, LadderJson ladderJson) {
        LadderCommand ladderCommand = ladders_.getLadderCommand();

        ladderCommand.setDisableHistory(true);
        try {
            // ladder
            if (!ladderCommand.restoreLadders(ladders, tabPane, treeTableView, ioMap, commentMap, scriptIoMap,
                    ladderJson, false)) {
                return false;
            }

            // comments
            if (!ladderCommand.restoreComments(tabPane, treeTableView, ioMap, commentMap, ladderJson)) {
                return false;
            }

            // history
            if (ladders != null) {
                if (ladderJson.getHistoryManager() != null) {
                    ladderCommand.setHistoryManager(ladderJson.getHistoryManager());
                }
            }

            ladderCommand.setDisableHistory(false);
            return true;
        } catch (Exception ex) {
            Console.writeStackTrace(Ladders.class.getName(), ex);
        }
        ladderCommand.setDisableHistory(false);
        return false;
    }

    /**
     * the json representation of the ladders which the editor shows, with the history of the user
     *
     * @param tabPane tabs of the ladders
     * @return the json representation
     */
    LadderJson ladderJsonSave(TabPane tabPane) {
        LadderIoStore ioStore = ladders_.getIoStore();

        return ladderJsonSave(tabPane, ioStore.getIoMap(), ioStore.getCommentMap());
    }

    /**
     *
     * @param tabPane
     * @param ioMap
     * @param commentMap
     * @return the json representation of the ladders of the tabs and of the comments of their
     *         addresses
     */
    private LadderJson ladderJsonSave(TabPane tabPane,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap) {
        LadderJson ladderJson = new LadderJson();
        LadderCommand ladderCommand = ladders_.getLadderCommand();

        // ladder
        ladderCommand.backupLadders(ladderJson, tabPane);

        // comments
        ladderCommand.backupComments(ladderJson, ioMap, commentMap);

        // historys
        ladderJson.setHistoryManager(ladderCommand.getHistoryManager(), -1);

        return ladderJson;
    }

    /**
     *
     * @param file
     */
    private void fileNotFound(Path file) {
        if (file != null) {
            ladders_.writeLog("File Not Found" + ": " + file.toString(), true);
        } else {
            ladders_.writeLog("File Not Found", true);
        }
    }

    /**
     * ask the user what has to be done with the changes of the ladders which the editor shows
     *
     * @return the answer of the user, null when the ladders hold no change
     */
    ButtonType fileSavedCheck() {
        ButtonType response = null;
        Alert alert;
        Optional<ButtonType> result;
        List<Image> icons = ladders_.getIcons();

        if (ladders_.isChanged()) {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            if ((icons != null) && !icons.isEmpty()) {
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons);
            }
            alert.setTitle("Ladder");
            alert.getDialogPane().setHeaderText(null);
            alert.getDialogPane().setContentText("Save Changes Program?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            result = alert.showAndWait();
            // the dialog can be closed without an answer : it is a cancel, not an error
            response = result.orElse(ButtonType.CANCEL);
        }
        return response;
    }

    /**
     * apply the answer of fileSavedCheck() : the current ladders are kept when the save failed, so
     * that fileNew() and fileOpen() can not destroy edited data
     *
     * @param response answer of fileSavedCheck(), null when the ladders were saved
     * @return true when the current ladders can be replaced
     */
    private boolean saveChanges(ButtonType response) {
        if (response == null) {
            // nothing to save
            return true;
        }
        if (response == ButtonType.YES) {
            if (ladderSave() == null) {
                ladders_.writeLog("save failed : the current ladder is kept", true);
                return false;
            }
            return true;
        }
        // ButtonType.NO discards the changes on purpose, ButtonType.CANCEL and a closed
        // dialog keep the current ladders
        return response == ButtonType.NO;
    }

    /**
     *
     * @return true when the new ladder is created
     */
    boolean fileNew() {
        if (!saveChanges(fileSavedCheck())) {
            // the changes are kept : a new ladder is not created
            return false;
        }
        ladderNew();
        ladders_.getFileStore().setFilePath(null);
        ladders_.setChanged(false);
        ladders_.setTitle();
        return true;
    }

    /**
     *
     * @param filePath the file of a ladder, null when the user chooses the file
     * @return true when an other ladder is loaded
     */
    boolean fileOpen(Path filePath) {
        Path file;

        if (!saveChanges(fileSavedCheck())) {
            // the changes are kept : an other ladder is not loaded
            return false;
        }
        file = ladderOpen(filePath);
        if (file == null) {
            if (filePath != null) {
                fileNotFound(filePath);
            }
        } else {
            ladders_.getFileStore().setFilePath(file);
            ladders_.setChanged(false);
            ladders_.setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @return true when the ladders are saved
     */
    boolean fileSave() {
        Path file = ladderSave();

        if (file != null) {
            ladders_.getFileStore().setFilePath(file);
            ladders_.setChanged(false);
            ladders_.setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @return true when the ladders are saved to a file chosen by the user
     */
    boolean fileSaveAs() {
        Path file = ladderSaveAs();

        if (file != null) {
            ladders_.getFileStore().setFilePath(file);
            ladders_.setChanged(false);
            ladders_.setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @return true when the ladders are exported to a pdf file
     */
    boolean exportPdf() {
        Path file = ladderExportPdf();
        return file != null;
    }
}
