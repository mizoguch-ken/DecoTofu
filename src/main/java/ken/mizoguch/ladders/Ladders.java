/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ken.mizoguch.console.Console;
import ken.mizoguch.soem.Soem;

/**
 *
 * @author mizoguch-ken
 */
public class Ladders extends Service<Void> {

    /**
     * margin added to the ideal cycle time when the end of a cycle is awaited
     */
    private static final long LADDER_CYCLE_STOP_WAIT_MILLIS = 1000;

    private final DesignLaddersController ladderController_;
    private final TabPane tabLadder_;
    private final TreeTableView<LadderTreeTableIo> treeTableIo_;
    private final TableView<LadderTableIo> tableIo_;

    private Stage stage_;
    private List<Image> icons_;
    private LadderCommand ladderCommand_;
    private int historyGeneration_;
    /**
     * the registrations of the ladders : the web page which runs the javascript of the scripts and
     * the addresses of the slaves of a soem bus
     */
    private final LadderRegister register_;
    /**
     * the tabs of the editor : the ladder of a tab, the name and the selection of the tabs
     */
    private final LadderTabs tabs_;
    /**
     * the keys of the editor : the keys which create, remove, rename a ladder or run a file of the
     * ladders
     */
    private final LadderKeyHandler keyHandler_;
    /**
     * the file of the ladders : the path of the ladder shown by the editor and the json of the
     * ladder read from and written to a file
     */
    private final LadderFileStore fileStore_;
    /**
     * the files of the editor : a new ladder, a ladder loaded from a file, the ladder saved to a
     * file and the ladders exported to a pdf file
     */
    private final LadderFileActions fileActions_;

    private final Object lock_ = new Object();
    private volatile Ladder[] ladders_;
    private int laddersSize_;
    /**
     * the io of the ladders : the store owns the maps which the editor and the cycle of the ladder
     * use as their live io
     */
    private final LadderIoStore ioStore_;
    private long idealCycleTime_;
    private volatile boolean isCycling_, isChanged_;
    /**
     * run the javascript of the script blocks of a ladder loaded from a file
     */
    private volatile boolean isScriptEnable_;
    /**
     * finish flag of the last created cycle task : the flag is replaced by every task,
     * so the end of a task which is still stopping can not answer the wait of a cycle
     * which has just been started
     */
    private volatile AtomicBoolean cycleFinished_;

    /**
     *
     * @param ladderController
     */
    // the constructor calls members which a subclass could override : the class is never subclassed
    @SuppressWarnings("this-escape")
    public Ladders(DesignLaddersController ladderController) {
        ladderController_ = ladderController;
        tabLadder_ = ladderController_.getTabLadder();
        treeTableIo_ = ladderController_.getTreeTableIo();
        treeTableIo_.setRoot(new TreeItem<>());
        tableIo_ = ladderController.getTableIo();

        stage_ = null;
        icons_ = null;
        ladderCommand_ = null;
        historyGeneration_ = 0;
        fileStore_ = new LadderFileStore();

        ladders_ = null;
        laddersSize_ = 0;
        ioStore_ = new LadderIoStore(lock_);
        register_ = new LadderRegister(ioStore_, (address) -> {
            treeTableIo_.getRoot().getChildren().get(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX).getChildren()
                    .add(new TreeItem<>(new LadderTreeTableIo(address)));
        });
        tabs_ = new LadderTabs(this);
        keyHandler_ = new LadderKeyHandler(this);
        fileActions_ = new LadderFileActions(this);
        idealCycleTime_ = 0;
        isChanged_ = false;
        isCycling_ = false;
        isScriptEnable_ = true;
        cycleFinished_ = new AtomicBoolean(true);
    }

    /**
     *
     * @return
     */
    public DesignLaddersController getDesignController() {
        return ladderController_;
    }

    /**
     * the stage of the editor : the owner of the dialogs which ask a name or a file to the user
     *
     * @return the stage of the editor, null before the start up of the editor
     */
    Stage getStage() {
        return stage_;
    }

    /**
     * the icons of the editor : the icons given to the dialogs which ask a name or a file to the
     * user
     *
     * @return the icons of the editor, null before the start up of the editor
     */
    List<Image> getIcons() {
        return icons_;
    }

    /**
     * mark the ladders changed or not since the last save of the ladder file
     *
     * @param changed true when the ladders are changed
     */
    void setChanged(boolean changed) {
        isChanged_ = changed;
    }

    /**
     * the tabs of the editor : the ladder of a tab, the name and the selection of the tabs
     *
     * @return the tabs of the editor
     */
    LadderTabs getTabs() {
        return tabs_;
    }

    /**
     *
     * @return
     */
    public TabPane getTabLadder() {
        return tabLadder_;
    }

    /**
     *
     * @return
     */
    public TreeTableView<LadderTreeTableIo> getTreeTableIo() {
        return treeTableIo_;
    }

    /**
     *
     * @return
     */
    public TableView<LadderTableIo> getTableIo() {
        return tableIo_;
    }

    /**
     *
     * @return
     */
    public Object getLock() {
        return lock_;
    }

    /**
     *
     * @return
     */
    public Ladder[] getLadders() {
        return ladders_;
    }

    /**
     *
     * @return
     */
    public LadderCommand getLadderCommand() {
        return ladderCommand_;
    }

    /**
     *
     * @return the io of the ladders : the live map of the store
     */
    public CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> getIoMap() {
        return ioStore_.getIoMap();
    }

    /**
     *
     * @return the comments of the addresses of the ladders : the live map of the store
     */
    public CopyOnWriteArrayList<ConcurrentHashMap<String, String>> getCommentMap() {
        return ioStore_.getCommentMap();
    }

    /**
     *
     * @return the io written by the javascript of the script blocks : the live map of the store
     */
    public CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> getScriptIoMap() {
        return ioStore_.getScriptIoMap();
    }

    /**
     * the store of the io : the cycle of the ladder and the editor share the same io
     *
     * @return the store of the io
     */
    LadderIoStore getIoStore() {
        return ioStore_;
    }

    /**
     *
     * @return the store of the file of the ladders
     */
    LadderFileStore getFileStore() {
        return fileStore_;
    }

    /**
     *
     * @return the soem used by the cycle, null when no slave is registered
     */
    Soem getSoem() {
        return register_.getSoem();
    }

    /**
     *
     * @return the inputs registered on the soem slaves
     */
    CopyOnWriteArrayList<LadderRegisterSoemIo> getRegisterSoemIn() {
        return register_.getRegisterSoemIn();
    }

    /**
     *
     * @return the outputs registered on the soem slaves
     */
    CopyOnWriteArrayList<LadderRegisterSoemIo> getRegisterSoemOut() {
        return register_.getRegisterSoemOut();
    }

    /**
     *
     * @return the number of the ladders run by the cycle
     */
    int getLaddersSize() {
        return laddersSize_;
    }

    /**
     * the cycling flag is cleared by the task of the cycle when it ends
     *
     * @param isCycling
     */
    void setCycling(boolean isCycling) {
        isCycling_ = isCycling;
    }

    /**
     *
     * @param oldIdx
     * @param oldAddress
     * @param newAddress
     * @param newIdx
     */
    public void changeAddress(int oldIdx, String oldAddress, int newIdx, String newAddress) {
        ladderCommand_.changeAddress(oldIdx, oldAddress, newIdx, newAddress);
        isChanged_ = true;
        setTitle();
    }

    /**
     *
     * @param idx
     * @param address
     * @return
     */
    public boolean isComment(int idx, String address) {
        return ioStore_.hasComment(idx, address);
    }

    /**
     *
     * @param idx
     * @param address
     * @return
     */
    public String getComment(int idx, String address) {
        return ioStore_.comment(idx, address);
    }

    /**
     *
     * @param idx
     * @param address
     * @param comment
     * @return
     */
    public boolean changeComment(int idx, String address, String comment) {
        if (ladderCommand_.changeComment(idx, address, comment)) {
            isChanged_ = true;
            setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @param address
     * @param value
     */
    public void setValueDirect(String address, double value) {
        setValueDirect(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, address, value);
    }

    /**
     *
     * @param idx
     * @param address
     * @param value
     */
    public void setValueDirect(int idx, String address, double value) {
        if (ioStore_.createIo(idx, address)) {
            treeTableIo_.getRoot().getChildren().get(idx).getChildren()
                    .add(new TreeItem<>(new LadderTreeTableIo(address, value)));
        }

        changeTreeTableIoValue(idx, address, value);
        ioStore_.setDirect(idx, address, value);
    }

    /**
     * value of the view of the io of an address of a ladder
     *
     * @param idx
     * @param address
     * @param value
     */
    private void changeTreeTableIoValue(int idx, String address, double value) {
        ObservableList<TreeItem<LadderTreeTableIo>> rows;
        int index, size;

        rows = treeTableIo_.getRoot().getChildren().get(idx).getChildren();
        for (index = 0, size = rows.size(); index < size; index++) {
            if (address.equals(rows.get(index).getValue().getAddress())) {
                rows.get(index).getValue().setValue(value);
                break;
            }
        }
    }

    /**
     * clear the io, the tabs and the history of the editor : the cycle of the ladder is stopped
     * before the maps which the cycle reads are cleared
     */
    void allClear() {
        stopLadder();

        // wait the end of the current cycle and clear the io of the cycle
        synchronized (lock_) {
            register_.clearSoem();
            ioStore_.clear();
            ladders_ = null;
        }

        tabLadder_.getTabs().clear();
        treeTableIo_.getRoot().getChildren().clear();
        ladderCommand_.clearHistoryManager();
    }

    /**
     *
     * @param workFilePath
     */
    public void setWorkFilePath(String workFilePath) {
        fileStore_.setWorkFilePath(workFilePath);
    }

    /**
     *
     * @return
     */
    public String getWorkFilePath() {
        return fileStore_.getWorkFilePath();
    }

    /**
     * a new ladder of the editor : the io of the editor and the tabs of the ladders are cleared
     */
    public void ladderNew() {
        fileActions_.ladderNew();
    }

    /**
     *
     * @return
     */
    public boolean ladderNewTab() {
        return tabs_.create();
    }

    /**
     *
     * @param index
     * @return
     */
    public boolean ladderRemoveTab(int index) {
        return tabs_.remove(index);
    }

    /**
     *
     * @param pane
     * @return
     */
    public boolean ladderChangeTabName(LadderPane pane) {
        return tabs_.rename(pane);
    }

    /**
     *
     * @param idx
     * @param name
     * @return
     */
    public boolean ladderChangeTabName(int idx, String name) {
        return tabs_.rename(idx, name);
    }

    /**
     * the json representation of a file of the ladders, read without changing the editor
     *
     * @param file
     * @return the json representation, null when the file can not be read
     */
    public LadderJson ladderJsonOpen(Path file) {
        return fileActions_.ladderJsonOpen(file);
    }

    /**
     * the ladders of a json representation : the ladders are restored without the history of the
     * user, so that a file which the difference of two ladders shows is never mixed with the undo
     * history of the editor
     *
     * @param ladders the editor which shows the ladders, null when the ladders are only shown for the
     *                difference of two files
     * @param tabPane
     * @param treeTableView
     * @param ioMap
     * @param commentMap
     * @param scriptIoMap null when the maps do not belong to a cycle
     * @param ladderJson
     * @return true when the ladders of the json representation are shown
     */
    public boolean ladderJsonLoad(Ladders ladders, TabPane tabPane, TreeTableView<LadderTreeTableIo> treeTableView,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap, LadderJson ladderJson) {
        return fileActions_.ladderJsonLoad(ladders, tabPane, treeTableView, ioMap, commentMap, scriptIoMap, ladderJson);
    }

    /**
     * the json representation of the ladders which the editor shows, with the history of the user
     *
     * @param tabPane
     * @return the json representation
     */
    public LadderJson ladderJsonSave(TabPane tabPane) {
        return fileActions_.ladderJsonSave(tabPane);
    }

    /**
     *
     * @param ladder
     * @return
     */
    public boolean ladderMoveLeft(Ladder ladder) {
        int idx = ladder.getIdx();

        if (idx > 1) {
            TreeItem<LadderTreeTableIo> treeItem = treeTableIo_.getRoot().getChildren().get(idx);
            treeTableIo_.getRoot().getChildren().set(idx, treeTableIo_.getRoot().getChildren().get(idx - 1));
            treeTableIo_.getRoot().getChildren().set(idx - 1, treeItem);

            ioStore_.swapLadderRows(idx, idx - 1);

            ladderCommand_.ladderMoveLeft(ladder);
            isChanged_ = true;

            setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @param ladder
     * @return
     */
    public boolean ladderMoveRight(Ladder ladder) {
        int idx = ladder.getIdx();

        if (idx < getTabLadder().getTabs().size()) {
            TreeItem<LadderTreeTableIo> treeItem = treeTableIo_.getRoot().getChildren().get(idx);
            treeTableIo_.getRoot().getChildren().set(idx, treeTableIo_.getRoot().getChildren().get(idx + 1));
            treeTableIo_.getRoot().getChildren().set(idx + 1, treeItem);

            ioStore_.swapLadderRows(idx, idx + 1);

            ladderCommand_.ladderMoveRight(ladder);
            isChanged_ = true;

            setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @param index
     */
    public void ladderChangeSelect(int index) {
        ladderCommand_.ladderChangeSelect(index);
    }

    /**
     *
     * @param tabPane
     */
    public void ladderChangeSelectNext(TabPane tabPane) {
        tabs_.selectNext();
    }

    /**
     *
     * @param tabPane
     */
    public void ladderChangeSelectPrevious(TabPane tabPane) {
        tabs_.selectPrevious();
    }

    /**
     *
     * @param tab
     * @param pane
     * @return
     */
    public boolean undo(Tab tab, LadderPane pane) {
        if (ladderCommand_.undo()) {
            isChanged_ = true;

            if (tab == null) {
                tab = tabLadder_.getSelectionModel().getSelectedItem();
                if (tab != null) {
                    pane = (LadderPane) ((ScrollPane) tab.getContent()).getContent();
                }
            }

            if (tab != null) {
                pane.setChanged(true);
                tab.setText(pane.getLadder().getName() + " *");
            }

            setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @param tab
     * @param pane
     * @return
     */
    public boolean redo(Tab tab, LadderPane pane) {
        if (ladderCommand_.redo()) {
            isChanged_ = true;

            if (tab == null) {
                tab = tabLadder_.getSelectionModel().getSelectedItem();
                if (tab != null) {
                    pane = (LadderPane) ((ScrollPane) tab.getContent()).getContent();
                }
            }

            if (tab != null) {
                pane.setChanged(true);
                tab.setText(pane.getLadder().getName() + " *");
            }

            setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public boolean isChanged() {
        return isChanged_;
    }

    /**
     *
     * @return
     */
    public boolean isCycling() {
        return isCycling_;
    }

    /**
     *
     * @param pane
     * @param event
     * @param scrollPane
     * @return
     */
    public boolean onKeyPressed(LadderPane pane, KeyEvent event, ScrollPane scrollPane) {
        return keyHandler_.handle(pane, event, scrollPane);
    }

    void setTitle() {
        String fileName = getFileName();

        if (fileName == null) {
            fileName = "";
        }

        if (isChanged()) {
            stage_.setTitle(fileName + " *");
        } else {
            stage_.setTitle(fileName);
        }
    }

    /**
     *
     * @param stage
     * @param icons
     * @param ladderPath
     * @return
     */
    public boolean startUp(Stage stage, List<Image> icons, Path ladderPath) {
        stage_ = stage;
        icons_ = icons;

        // command
        ladderCommand_ = new LadderCommand(stage_, icons_, this);
        ladderCommand_.setHistoryGeneration(historyGeneration_);

        fileStore_.setFilePath(ladderPath);
        ladderNew();
        return false;
    }

    /**
     *
     */
    public void cleanUp() {
        // clear
        allClear();
        cancel();
    }

    @Override
    protected Task<Void> createTask() {
        LadderCycleTask task;

        task = new LadderCycleTask(this);
        cycleFinished_ = task.getFinished();
        return task;
    }

    public int getHistoryGeneration() {
        return historyGeneration_;
    }

    public void setHistoryGeneration(int historyGeneration) {
        historyGeneration_ = historyGeneration;
        ladderController_.setHistoryGeneration(historyGeneration_);
        if (ladderCommand_ != null) {
            ladderCommand_.setHistoryGeneration(historyGeneration_);
        }
    }

    public long getIdealCycleTime() {
        return idealCycleTime_;
    }

    public void setIdealCycleTime(long idealCycleTime) {
        idealCycleTime_ = idealCycleTime;
        ladderController_.setIdealCycleTime(idealCycleTime_);
    }

    /**
     *
     * @return true when the scripts of a ladder loaded from a file are run
     */
    public boolean isScriptEnable() {
        return isScriptEnable_;
    }

    /**
     *
     * @param isScriptEnable run the scripts of a ladder loaded from a file
     */
    public void setScriptEnable(boolean isScriptEnable) {
        isScriptEnable_ = isScriptEnable;
    }

    public Boolean isRegistered(Object object) {
        return register_.isRegistered(object);
    }

    public void register(WebEngine webEngine, Worker.State state) {
        register_.register(webEngine, state);
    }

    public Boolean registerIn(Object... objects) {
        return register_.registerIn(objects);
    }

    public Boolean registerOut(Object... objects) {
        return register_.registerOut(objects);
    }

    public void unregister(Object object) {
        register_.unregister(object);
    }

    public Double getValue(String address) {
        return ioStore_.value(address);
    }

    /**
     *
     * @param idx
     * @param address
     * @return
     */
    public Double getValue(int idx, String address) {
        return ioStore_.value(idx, address);
    }

    public void setValue(String address, double value) {
        if (!address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
            setValue(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, address, value);
        }
    }

    /**
     *
     * @param idx
     * @param address
     * @param value
     */
    public void setValue(int idx, String address, double value) {
        if (ioStore_.createIo(idx, address)) {
            treeTableIo_.getRoot().getChildren().get(idx).getChildren()
                    .add(new TreeItem<>(new LadderTreeTableIo(address, value)));
        }

        changeTreeTableIoValue(idx, address, value);
        ioStore_.setScript(idx, address, value);
    }

    public boolean connectLadder() {
        LadderPane pane;
        int index;

        // check connect
        if (checkConnectLadder(tabLadder_, treeTableIo_, ioStore_.getIoMap())) {
            synchronized (lock_) {
                // connect
                laddersSize_ = tabLadder_.getTabs().size();
                ladders_ = new Ladder[laddersSize_];
                for (index = 0; index < laddersSize_; index++) {
                    pane = (LadderPane) ((ScrollPane) tabLadder_.getTabs().get(index).getContent()).getContent();
                    ladders_[index] = pane.getLadder().copy();
                    ladders_[index].connectLadder(ioStore_.getIoMap(), treeTableIo_);
                }
            }
            register_.registerWeb();
            return true;
        }
        return false;
    }

    /**
     *
     * @param tabPane
     * @param ioTreeTable
     * @param ioMap
     * @return
     */
    public boolean checkConnectLadder(TabPane tabPane, TreeTableView<LadderTreeTableIo> ioTreeTable,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap) {
        LadderPane pane;
        int index, size;

        for (index = 0, size = tabPane.getTabs().size(); index < size; index++) {
            pane = (LadderPane) ((ScrollPane) tabPane.getTabs().get(index).getContent()).getContent();
            if (pane.getLadder().connectLadder(ioMap, ioTreeTable)) {
                pane.clearEditing();
            } else {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean runStartLadder() {
        if (!isCycling_ && (ladders_ != null) && !ioStore_.isEmpty()) {
            // the synchronized start refuses a second start asked while the first one is
            // not done yet : the cycle of the ladder would be started twice
            isCycling_ = true;
            if (Platform.isFxApplicationThread()) {
                if (!startLadder()) {
                    isCycling_ = false;
                    return false;
                }
            } else {
                Platform.runLater(() -> {
                    if (!startLadder()) {
                        isCycling_ = false;
                    }
                });
            }
            return true;
        }
        return false;
    }

    /**
     * start the task of the cycle of the ladder : the service refuses a start which is not
     * allowed by its state, so the caller has to clear the cycling flag when nothing started
     *
     * @return true when the cycle task was started
     */
    private boolean startLadder() {
        try {
            if (getState() == State.READY) {
                start();
            } else {
                restart();
            }
            return true;
        } catch (IllegalStateException ex) {
            Console.writeStackTrace(Ladders.class.getName(), ex);
        }
        return false;
    }

    public void stopLadder() {
        if (isCycling_) {
            isCycling_ = false;
            // wait the end of the current cycle : never block the javafx application thread
            if (!Platform.isFxApplicationThread()) {
                if (!awaitCycleStop(cycleStopTimeoutMillis())) {
                    writeLog("the ladder cycle is still running", true);
                }
            }
        }
    }

    /**
     * time to wait for the end of the current cycle, in milliseconds
     *
     * @return
     */
    private long cycleStopTimeoutMillis() {
        return TimeUnit.NANOSECONDS.toMillis(idealCycleTime_ + LadderDefaults.LADDER_VIEW_REFRESH_CYCLE_TIME)
                + LADDER_CYCLE_STOP_WAIT_MILLIS;
    }

    /**
     * wait the end of the ladder cycle task : the io and the native memory used by the cycle are
     * released only when this method returns true
     *
     * @param timeoutMillis
     * @return true when the cycle task is no longer running
     */
    public boolean awaitCycleStop(long timeoutMillis) {
        long deadline = System.currentTimeMillis() + Math.max(0, timeoutMillis);
        final AtomicBoolean finished = cycleFinished_;

        while (!finished.get() || isCycling_) {
            if (System.currentTimeMillis() >= deadline) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return finished.get() && !isCycling_;
    }

    /**
     * ask the user what has to be done with the changes of the ladders which the editor shows
     *
     * @return the answer of the user, null when the ladders hold no change
     */
    public ButtonType fileSavedCheck() {
        return fileActions_.fileSavedCheck();
    }

    /**
     * a new ladder of the editor : the changes of the current ladder are saved or discarded before
     * the editor is cleared
     *
     * @return true when the new ladder is created
     */
    public boolean fileNew() {
        return fileActions_.fileNew();
    }

    /**
     * an other ladder loaded from a file : the changes of the current ladder are saved or discarded
     * before the editor is cleared
     *
     * @param filePath the file of a ladder, null when the user chooses the file
     * @return true when an other ladder is loaded
     */
    public boolean fileOpen(Path filePath) {
        return fileActions_.fileOpen(filePath);
    }

    /**
     *
     * @return true when the ladders are saved
     */
    public boolean fileSave() {
        return fileActions_.fileSave();
    }

    /**
     *
     * @return true when the ladders are saved to a file chosen by the user
     */
    public boolean fileSaveAs() {
        return fileActions_.fileSaveAs();
    }

    /**
     *
     * @return true when the ladders are exported to a pdf file
     */
    public boolean exportPdf() {
        return fileActions_.exportPdf();
    }

    public String getFileName() {
        return fileStore_.getFileName();
    }

    /**
     *
     * @return
     */
    public Path getFilePath() {
        return fileStore_.getFilePath();
    }

    void writeLog(final String msg, final boolean err) {
        Console.write(Ladders.class.getName(), msg, err);
    }
}
