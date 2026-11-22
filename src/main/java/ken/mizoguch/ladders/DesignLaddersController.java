/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import ken.mizoguch.console.Console;

/**
 *
 * @author mizoguch-ken
 */
public class DesignLaddersController implements Initializable {

    public DesignLaddersController() {
    }

    @FXML
    private VBox paneRoot;
    // menu
    @FXML
    private MenuBar menuBar;
    // menu file
    @FXML
    private Menu menuFile;
    @FXML
    private MenuItem menuFileNew;
    @FXML
    private MenuItem menuFileOpen;
    @FXML
    private Menu menuFileOpenRecent;
    @FXML
    private MenuItem menuFileRecentFile1;
    @FXML
    private MenuItem menuFileRecentFile2;
    @FXML
    private MenuItem menuFileRecentFile3;
    @FXML
    private MenuItem menuFileRecentFile4;
    @FXML
    private MenuItem menuFileRecentFile5;
    @FXML
    private MenuItem menuFileRecentFile6;
    @FXML
    private MenuItem menuFileRecentFile7;
    @FXML
    private MenuItem menuFileRecentFile8;
    @FXML
    private MenuItem menuFileRecentFile9;
    @FXML
    private MenuItem menuFileRecentFile10;
    @FXML
    private SeparatorMenuItem menuFileRecentSeparator;
    @FXML
    private MenuItem menuFileRecentClear;
    @FXML
    private MenuItem menuFileSave;
    @FXML
    private MenuItem menuFileSaveAs;
    @FXML
    private MenuItem menuFileExportPdf;
    @FXML
    private MenuItem menuFileDifference;
    @FXML
    private MenuItem menuFileClose;
    // menu edit
    @FXML
    private Menu menuEdit;
    @FXML
    private MenuItem menuEditUndo;
    @FXML
    private MenuItem menuEditRedo;
    @FXML
    private MenuItem menuEditCut;
    @FXML
    private MenuItem menuEditCopy;
    @FXML
    private MenuItem menuEditPaste;
    @FXML
    private MenuItem menuEditDelete;
    @FXML
    private MenuItem menuEditBlock;
    @FXML
    private MenuItem menuEditInvertBlock;
    @FXML
    private MenuItem menuEditCycleEdge;
    @FXML
    private Menu menuEditWire;
    @FXML
    private MenuItem menuEditWireLeft;
    @FXML
    private MenuItem menuEditWireRight;
    @FXML
    private MenuItem menuEditWireUp;
    @FXML
    private MenuItem menuEditWireDown;
    @FXML
    private MenuItem menuEditInsertRow;
    @FXML
    private MenuItem menuEditDeleteRow;
    @FXML
    private MenuItem menuEditFindUsages;
    @FXML
    private MenuItem menuEditFindAddress;
    // menu command
    @FXML
    private Menu menuCommand;
    // menu tab
    @FXML
    private Menu menuTab;
    @FXML
    private MenuItem menuTabNew;
    @FXML
    private MenuItem menuTabClose;
    @FXML
    private MenuItem menuTabRename;
    @FXML
    private MenuItem menuTabMoveLeft;
    @FXML
    private MenuItem menuTabMoveRight;
    @FXML
    private MenuItem menuTabPrevious;
    @FXML
    private MenuItem menuTabNext;
    // menu view
    @FXML
    private Menu menuView;
    @FXML
    private CheckMenuItem menuViewIoPanel;
    @FXML
    private CheckMenuItem menuViewStatusBar;
    @FXML
    private MenuItem menuViewExpandAllIo;
    @FXML
    private MenuItem menuViewCollapseAllIo;
    @FXML
    private MenuItem menuViewClearFindResults;
    // menu tools
    @FXML
    private Menu menuTools;
    @FXML
    private MenuItem menuToolsConnect;
    @FXML
    private MenuItem menuToolsRun;
    @FXML
    private MenuItem menuToolsStop;
    @FXML
    private MenuItem menuToolsToggleValue;
    @FXML
    private MenuItem menuToolsIdealCycleTime;
    @FXML
    private MenuItem menuToolsHistoryLimit;
    @FXML
    private CheckMenuItem menuToolsScriptEnable;
    // main
    @FXML
    private SplitPane splitIoLadder;
    @FXML
    private SplitPane splitIoIo;
    @FXML
    private TreeTableView<LadderTreeTableIo> treeTableIo;
    @FXML
    private TreeTableColumn<LadderTreeTableIo, String> treeTableIoAddress;
    @FXML
    private TreeTableColumn<LadderTreeTableIo, String> treeTableIoComment;
    @FXML
    private TreeTableColumn<LadderTreeTableIo, Double> treeTableIoValue;
    @FXML
    private TableView<LadderTableIo> tableIo;
    @FXML
    private TableColumn<LadderTableIo, String> tableIoName;
    @FXML
    private TableColumn<LadderTableIo, String> tableIoAddress;
    @FXML
    private TableColumn<LadderTableIo, String> tableIoBlock;
    @FXML
    private TableColumn<LadderTableIo, String> tableIoPosition;
    @FXML
    private StackPane paneLadderContainer;
    @FXML
    private TabPane tabLadder;
    @FXML
    private VBox boxCommandInput;
    @FXML
    private Label lblCommandInputHint;
    @FXML
    private TextField txtCommandInput;
    @FXML
    private Button btnCommandInputOk;
    @FXML
    private Button btnCommandInputCancel;
    // status
    @FXML
    private HBox statusBar;
    @FXML
    private Label lblCycleTime;
    @FXML
    private Label lblMinCycleTime;
    @FXML
    private Label lblMaxCycleTime;
    @FXML
    private Label lblIdealCycleTime;

    /**
     * stage settings
     */
    public class StageSettings {

        public StageSettings() {
        }

        private boolean stageMaximized;
        private double stageWidth, stageHeight, rootWidth, rootHeight, ioLadderDividerPositions, ioIoDividerPositions;

        /**
         *
         * @return
         */
        public boolean isStageMaximized() {
            return stageMaximized;
        }

        /**
         *
         * @param bln
         * @param update
         */
        public void setStageMaximized(boolean bln, boolean update) {
            if (update || isShowing_) {
                stageMaximized = bln;
                if (update) {
                    if (Platform.isFxApplicationThread()) {
                        stage_.setMaximized(stageMaximized);
                    } else {
                        Platform.runLater(() -> {
                            stage_.setMaximized(stageMaximized);
                        });
                    }
                }
            }
        }

        /**
         *
         * @return
         */
        public double getStageWidth() {
            return stageWidth;
        }

        /**
         *
         * @param value
         * @param update
         */
        public void setStageWidth(double value, boolean update) {
            if (update || isShowing_) {
                stageWidth = value;
                if (update) {
                    if (Platform.isFxApplicationThread()) {
                        if (!stageMaximized) {
                            stage_.setWidth(stageWidth);
                        }
                    } else {
                        Platform.runLater(() -> {
                            if (!stageMaximized) {
                                stage_.setWidth(stageWidth);
                            }
                        });
                    }
                }
            }
        }

        /**
         *
         * @return
         */
        public double getStageHeight() {
            return stageHeight;
        }

        /**
         *
         * @param value
         * @param update
         */
        public void setStageHeight(double value, boolean update) {
            if (update || isShowing_) {
                stageHeight = value;
                if (update) {
                    if (Platform.isFxApplicationThread()) {
                        if (!stageMaximized) {
                            stage_.setHeight(stageHeight);
                        }
                    } else {
                        Platform.runLater(() -> {
                            if (!stageMaximized) {
                                stage_.setHeight(stageHeight);
                            }
                        });
                    }
                }
            }
        }

        /**
         *
         * @return
         */
        public double getRootWidth() {
            return rootWidth;
        }

        /**
         *
         * @param value
         * @param update
         */
        public void setRootWidth(double value, boolean update) {
            if (update || isShowing_) {
                rootWidth = value;
                if (update) {
                    if (Platform.isFxApplicationThread()) {
                        if (!stageMaximized) {
                            paneRoot.setPrefWidth(rootWidth);
                        }
                    } else {
                        Platform.runLater(() -> {
                            if (!stageMaximized) {
                                paneRoot.setPrefWidth(rootWidth);
                            }
                        });
                    }
                }
            }
        }

        /**
         *
         * @return
         */
        public double getRootHeight() {
            return rootHeight;
        }

        /**
         *
         * @param value
         * @param update
         */
        public void setRootHeight(double value, boolean update) {
            if (update || isShowing_) {
                rootHeight = value;
                if (update) {
                    if (Platform.isFxApplicationThread()) {
                        if (!stageMaximized) {
                            paneRoot.setPrefHeight(rootHeight);
                        }
                    } else {
                        Platform.runLater(() -> {
                            if (!stageMaximized) {
                                paneRoot.setPrefHeight(rootHeight);
                            }
                        });
                    }
                }
            }
        }

        /**
         *
         * @return
         */
        public double getIoLadderDividerPositions() {
            return ioLadderDividerPositions;
        }

        /**
         *
         * @param value
         * @param update
         */
        public void setIoLadderDividerPositions(double value, boolean update) {
            if (update || isShown_) {
                ioLadderDividerPositions = value;
                if (update) {
                    if (Platform.isFxApplicationThread()) {
                        if (!splitIoLadder.getDividers().isEmpty()) {
                            splitIoLadder.getDividers().get(0).setPosition(ioLadderDividerPositions);
                        }
                    } else {
                        Platform.runLater(() -> {
                            if (!splitIoLadder.getDividers().isEmpty()) {
                                splitIoLadder.getDividers().get(0).setPosition(ioLadderDividerPositions);
                            }
                        });
                    }
                }
            }
        }

        /**
         *
         * @return
         */
        public double getIoIoDividerPositions() {
            return ioIoDividerPositions;
        }

        /**
         *
         * @param value
         * @param update
         */
        public void setIoIoDividerPositions(double value, boolean update) {
            if (update || isShown_) {
                ioIoDividerPositions = value;
                if (update) {
                    if (Platform.isFxApplicationThread()) {
                        splitIoIo.getDividers().get(0).setPosition(ioIoDividerPositions);
                    } else {
                        Platform.runLater(() -> {
                            splitIoIo.getDividers().get(0).setPosition(ioIoDividerPositions);
                        });
                    }
                }
            }
        }
    }

    private Stage stage_;
    private List<Image> icons_;
    private StageSettings stageSettings_;
    private List<MenuItem> recentFileMenus_;
    private ChangeListener<Number> ioLadderDividerListener_;

    private Ladders ladders_;
    private int historyGeneration_;
    private long idealCycleTime_;
    private Path ladderPath_;

    private CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap_;

    /**
     * viewRefresh_ and isShown_ are answered to the cycle of the ladder : the cycle task
     * reads them while the javafx application thread changes them with the events of the
     * window, so they have to be volatile
     */
    private volatile boolean viewRefresh_, isShown_;
    private boolean isShowing_;
    private Tab commandInputTab_;
    private LadderPane commandInputPane_;
    private ScrollPane commandInputScrollPane_;
    private LadderGrid commandInputGrid_;
    private LadderGridPane commandInputGridPane_;
    private List<MenuItem> contactCommandItems_;
    private List<MenuItem> outputCommandItems_;
    private Map<LadderCommandCatalog.Category, Menu> commandCategoryMenus_;

    static boolean isInvertibleBlock(LadderBlock block) {
        if (block == null) {
            return false;
        }
        return switch (block) {
            case LOAD, LOAD_NOT, LOAD_RISING, LOAD_RISING_NOT, LOAD_FALLING, LOAD_FALLING_NOT,
                    OUT, OUT_NOT, OUT_RISING, OUT_RISING_NOT, OUT_FALLING, OUT_FALLING_NOT,
                    COMPARISON_EQUAL, COMPARISON_NOT_EQUAL, COMPARISON_LESS, COMPARISON_LESS_EQUAL,
                    COMPARISON_GREATER, COMPARISON_GREATER_EQUAL,
                    SET, RESET, TIMER, TIMER_NOT, COUNTER, COUNTER_NOT -> true;
            default -> false;
        };
    }

    static boolean isEdgeCyclableBlock(LadderBlock block) {
        if (block == null) {
            return false;
        }
        return switch (block) {
            case LOAD, LOAD_NOT, LOAD_RISING, LOAD_RISING_NOT, LOAD_FALLING, LOAD_FALLING_NOT,
                    OUT, OUT_NOT, OUT_RISING, OUT_RISING_NOT, OUT_FALLING, OUT_FALLING_NOT -> true;
            default -> false;
        };
    }

    static Long parseIdealCycleTime(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            long value = Long.parseLong(trimmed);
            return (value >= 0L) ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static Integer parseHistoryLimit(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(trimmed);
            return (value >= -1) ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static boolean matchesSearchQuery(LadderGrid grid, String normalizedQuery) {
        if ((grid == null) || (normalizedQuery == null) || normalizedQuery.isEmpty()) {
            return false;
        }
        LadderBlock block = grid.getBlock();
        if ((block == null) || (block == LadderBlock.CONTENTS)
                || (block == LadderBlock.EMPTY) || (block == LadderBlock.CONNECT_LINE)) {
            return false;
        }
        String address = grid.getAddress();
        if ((address != null) && address.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
            return true;
        }
        String comment = grid.getComment();
        if ((comment != null) && comment.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
            return true;
        }
        LadderGrid.BlockFunction[] functions = grid.getBlockFunctions();
        if (functions != null) {
            for (LadderGrid.BlockFunction fn : functions) {
                if ((fn != null) && !fn.isNumber()) {
                    String fnAddr = fn.getAddress();
                    if ((fnAddr != null) && fnAddr.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Tab selectedTab() {
        return tabLadder.getSelectionModel().getSelectedItem();
    }

    private LadderPane selectedLadderPane() {
        Tab tab = selectedTab();
        if ((tab != null) && (tab.getContent() instanceof ScrollPane scrollPane)
                && (scrollPane.getContent() instanceof LadderPane pane)) {
            return pane;
        }
        return null;
    }

    private void dispatchPaneKey(KeyCode code, boolean shift, boolean shortcut, boolean alt) {
        if (isCommandInputActive()) {
            return;
        }
        Tab tab = selectedTab();
        if ((tab != null) && (tab.getContent() instanceof ScrollPane scrollPane)
                && (scrollPane.getContent() instanceof LadderPane pane)) {
            boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
            boolean ctrlDown = shortcut && !isMac;
            boolean metaDown = shortcut && isMac;
            ladders_.onKeyPressed(pane,
                    new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, ctrlDown, alt, metaDown),
                    scrollPane);
        }
    }

    private void populateCommandMenu() {
        if (menuCommand == null) {
            return;
        }
        if (contactCommandItems_ == null) {
            contactCommandItems_ = new ArrayList<>();
        }
        if (outputCommandItems_ == null) {
            outputCommandItems_ = new ArrayList<>();
        }
        if (commandCategoryMenus_ == null) {
            commandCategoryMenus_ = new EnumMap<>(LadderCommandCatalog.Category.class);
        }
        menuCommand.getItems().clear();
        contactCommandItems_.clear();
        outputCommandItems_.clear();
        commandCategoryMenus_.clear();

        LadderCommandCatalog.Category[] submenuCategories = {
                LadderCommandCatalog.Category.CONTACT,
                LadderCommandCatalog.Category.COMPARISON,
                LadderCommandCatalog.Category.OUTPUT,
                LadderCommandCatalog.Category.TIMER_COUNTER,
                LadderCommandCatalog.Category.ARITHMETIC,
                LadderCommandCatalog.Category.BITWISE
        };
        for (LadderCommandCatalog.Category category : submenuCategories) {
            Menu subMenu = new Menu(category.title());
            subMenu.setMnemonicParsing(false);
            commandCategoryMenus_.put(category, subMenu);
            menuCommand.getItems().add(subMenu);
        }
        menuCommand.getItems().add(new SeparatorMenuItem());

        for (LadderCommandCatalog.CommandSpec spec : LadderCommandCatalog.entries()) {
            MenuItem item = new MenuItem(spec.menuLabel());
            item.setMnemonicParsing(false);
            item.setOnAction((ActionEvent event) -> openCommandFromMenu(spec));
            if (spec.placement() == LadderCommandCatalog.Placement.CONTACT_SIDE) {
                contactCommandItems_.add(item);
            } else {
                outputCommandItems_.add(item);
            }
            if (spec.category() == LadderCommandCatalog.Category.OTHER) {
                menuCommand.getItems().add(item);
            } else {
                Menu subMenu = commandCategoryMenus_.get(spec.category());
                if (spec.separatorBefore()) {
                    subMenu.getItems().add(new SeparatorMenuItem());
                }
                subMenu.getItems().add(item);
            }
        }
    }

    private void setCommandCategoryDisabled(LadderCommandCatalog.Category category, boolean disabled) {
        if (commandCategoryMenus_ != null) {
            Menu subMenu = commandCategoryMenus_.get(category);
            if (subMenu != null) {
                subMenu.setDisable(disabled);
            }
        }
    }

    private void updateCommandMenuEnablement() {
        LadderPane pane = selectedLadderPane();
        boolean allowContact = false;
        boolean allowOutput = false;
        if (pane != null) {
            LadderGrid target = null;
            if (pane.getPreviousGrids().isEmpty()) {
                if ((pane.getLadder().getColumn() > 1) && (pane.getLadder().getRow() > 1)) {
                    target = pane.getLadder().findGrid(1, 1);
                }
            } else if (pane.getPreviousGrids().size() == 1) {
                target = pane.getPreviousGrids().get(0);
            }
            boolean editable = (target != null) && (target.getColumnIndex() > 0) && (target.getRowIndex() > 0);
            allowContact = editable && (target.getColumnIndex() < (pane.getLadder().getColumn() - 1));
            allowOutput = editable && pane.getLadder().checkGridOut(target);
        }
        if (contactCommandItems_ != null) {
            for (MenuItem item : contactCommandItems_) {
                item.setDisable(!allowContact);
            }
        }
        if (outputCommandItems_ != null) {
            for (MenuItem item : outputCommandItems_) {
                item.setDisable(!allowOutput);
            }
        }
        setCommandCategoryDisabled(LadderCommandCatalog.Category.CONTACT, !allowContact);
        setCommandCategoryDisabled(LadderCommandCatalog.Category.COMPARISON, !allowContact);
        setCommandCategoryDisabled(LadderCommandCatalog.Category.OUTPUT, !allowOutput);
        setCommandCategoryDisabled(LadderCommandCatalog.Category.TIMER_COUNTER, !allowOutput);
        setCommandCategoryDisabled(LadderCommandCatalog.Category.ARITHMETIC, !allowOutput);
        setCommandCategoryDisabled(LadderCommandCatalog.Category.BITWISE, !allowOutput);
    }

    private void openCommandFromMenu(LadderCommandCatalog.CommandSpec spec) {
        if (spec == null) {
            return;
        }
        if (isCommandInputActive()) {
            txtCommandInput.setText(spec.inputPrefix());
            lblCommandInputHint.setText(spec.syntaxHint());
            lblCommandInputHint.setStyle("-fx-font-size: 11px; -fx-opacity: 0.85;");
            txtCommandInput.requestFocus();
            txtCommandInput.deselect();
            txtCommandInput.positionCaret(txtCommandInput.getText().length());
            return;
        }
        Tab tab = selectedTab();
        ScrollPane scrollPane = ((tab != null) && (tab.getContent() instanceof ScrollPane sp)) ? sp : null;
        LadderPane pane = selectedLadderPane();
        showCommandInput(pane, scrollPane, spec.inputPrefix(), false, spec.syntaxHint());
    }

    boolean isCommandInputActive() {
        return (boxCommandInput != null) && boxCommandInput.isVisible() && (commandInputPane_ != null);
    }

    boolean showCommandInput(LadderPane pane, ScrollPane scrollPane, String initialText,
            boolean selectAll, String customHint) {
        if ((pane == null) || (boxCommandInput == null) || (txtCommandInput == null)) {
            return false;
        }
        if (pane.getPreviousGrids().isEmpty()
                && (pane.getLadder().getColumn() > 1) && (pane.getLadder().getRow() > 1)) {
            LadderGrid firstEditable = pane.getLadder().findGrid(1, 1);
            if (firstEditable != null) {
                ladders_.getLadderCommand().blockChangeSelect(scrollPane, pane, firstEditable, false);
            }
        }
        if (pane.getPreviousGrids().size() != 1) {
            return false;
        }
        LadderGrid targetGrid = pane.getPreviousGrids().get(0);
        if ((targetGrid.getColumnIndex() <= 0) || (targetGrid.getRowIndex() <= 0)) {
            return false;
        }
        LadderGridPane targetGridPane = pane.findGridPane(targetGrid);
        if (targetGridPane == null) {
            return false;
        }

        commandInputTab_ = selectedTab();
        commandInputPane_ = pane;
        commandInputScrollPane_ = scrollPane;
        commandInputGrid_ = targetGrid;
        commandInputGridPane_ = targetGridPane;

        String text = (initialText != null) ? initialText : "";
        txtCommandInput.setText(text);
        lblCommandInputHint.setText((customHint != null) ? customHint : LadderCommandCatalog.hintForInput(text));
        lblCommandInputHint.setStyle("-fx-font-size: 11px; -fx-opacity: 0.85;");

        boxCommandInput.setVisible(true);
        boxCommandInput.toFront();
        positionCommandInput(targetGridPane);
        Platform.runLater(() -> {
            if (isCommandInputActive() && (commandInputGridPane_ == targetGridPane)) {
                positionCommandInput(targetGridPane);
            }
        });

        txtCommandInput.requestFocus();
        if (selectAll && !text.isEmpty()) {
            txtCommandInput.selectAll();
        } else {
            txtCommandInput.deselect();
            txtCommandInput.positionCaret(text.length());
        }
        return true;
    }

    private void positionCommandInput(LadderGridPane gridPane) {
        if ((gridPane == null) || (paneLadderContainer == null) || (boxCommandInput == null)) {
            return;
        }
        Bounds gridSceneBounds = gridPane.localToScene(gridPane.getBoundsInLocal());
        if (gridSceneBounds == null) {
            return;
        }
        Bounds gridLocal = paneLadderContainer.sceneToLocal(gridSceneBounds);
        if (gridLocal == null) {
            return;
        }
        double containerWidth = paneLadderContainer.getWidth();
        double containerHeight = paneLadderContainer.getHeight();
        if ((containerWidth <= 0.0) || (containerHeight <= 0.0)) {
            return;
        }
        boxCommandInput.applyCss();
        boxCommandInput.autosize();
        double boxWidth = boxCommandInput.getWidth();
        if (boxWidth <= 0.0) {
            boxWidth = boxCommandInput.prefWidth(-1);
        }
        double boxHeight = boxCommandInput.getHeight();
        if (boxHeight <= 0.0) {
            boxHeight = boxCommandInput.prefHeight(-1);
        }
        double padding = 6.0;
        double maxX = Math.max(padding, containerWidth - boxWidth - padding);
        double x = Math.max(padding, Math.min(gridLocal.getMinX(), maxX));

        double yBelow = gridLocal.getMaxY() + 4.0;
        double yAbove = gridLocal.getMinY() - boxHeight - 4.0;
        double maxY = Math.max(padding, containerHeight - boxHeight - padding);
        double y = ((yBelow + boxHeight) <= (containerHeight - padding))
                ? yBelow
                : ((yAbove >= padding) ? yAbove : Math.max(padding, Math.min(yBelow, maxY)));

        boxCommandInput.setTranslateX(x);
        boxCommandInput.setTranslateY(y);
    }

    private void commitCommandInput() {
        if (!isCommandInputActive()) {
            return;
        }
        String text = txtCommandInput.getText();
        if ((text == null) || text.trim().isEmpty()) {
            closeCommandInput(true);
            return;
        }
        LadderPane pane = commandInputPane_;
        Tab tab = commandInputTab_;
        if (pane.applyBlockInput(text, commandInputGrid_, commandInputGridPane_, commandInputScrollPane_)) {
            ladders_.setChanged(true);
            if (tab != null) {
                tab.setText(pane.getLadder().getName() + " *");
            }
            ladders_.setTitle();
            closeCommandInput(true);
        } else {
            lblCommandInputHint.setText("Invalid syntax — " + LadderCommandCatalog.hintForInput(text));
            lblCommandInputHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #c62828;");
            txtCommandInput.requestFocus();
            txtCommandInput.deselect();
            txtCommandInput.positionCaret(txtCommandInput.getText().length());
        }
    }

    private void closeCommandInput(boolean restoreFocus) {
        ScrollPane scrollPane = commandInputScrollPane_;
        commandInputTab_ = null;
        commandInputPane_ = null;
        commandInputScrollPane_ = null;
        commandInputGrid_ = null;
        commandInputGridPane_ = null;
        if (boxCommandInput != null) {
            boxCommandInput.setVisible(false);
        }
        if (txtCommandInput != null) {
            txtCommandInput.clear();
        }
        if (restoreFocus) {
            if (scrollPane != null) {
                scrollPane.requestFocus();
            } else if (tabLadder != null) {
                tabLadder.requestFocus();
            }
        }
    }

    private void attachIoLadderDividerListener() {
        if (!splitIoLadder.getDividers().isEmpty()) {
            if (ioLadderDividerListener_ == null) {
                ioLadderDividerListener_ = (ObservableValue<? extends Number> observable, Number oldValue,
                        Number newValue) -> {
                    if (newValue != null) {
                        stageSettings_.setIoLadderDividerPositions(newValue.doubleValue(), false);
                    }
                };
            }
            var posProp = splitIoLadder.getDividers().get(0).positionProperty();
            posProp.removeListener(ioLadderDividerListener_);
            posProp.addListener(ioLadderDividerListener_);
        }
    }

    private void setIoPanelVisible(boolean visible) {
        boolean currentlyVisible = splitIoLadder.getItems().contains(splitIoIo);
        if (visible && !currentlyVisible) {
            splitIoLadder.getItems().add(0, splitIoIo);
            attachIoLadderDividerListener();
            stageSettings_.setIoLadderDividerPositions(stageSettings_.getIoLadderDividerPositions(), true);
        } else if (!visible && currentlyVisible) {
            if (!splitIoLadder.getDividers().isEmpty()) {
                stageSettings_.setIoLadderDividerPositions(
                        splitIoLadder.getDividers().get(0).getPosition(), false);
            }
            splitIoLadder.getItems().remove(splitIoIo);
        }
        if (menuViewIoPanel != null) {
            menuViewIoPanel.setSelected(visible);
        }
    }

    private void setStatusBarVisible(boolean visible) {
        statusBar.setVisible(visible);
        statusBar.setManaged(visible);
        if (menuViewStatusBar != null) {
            menuViewStatusBar.setSelected(visible);
        }
    }

    private boolean canToggleSelectedBlockValue() {
        LadderPane pane = selectedLadderPane();
        if ((pane == null) || (pane.getPreviousGrids().size() != 1)) {
            return false;
        }
        LadderGrid grid = pane.getPreviousGrids().get(0);
        if (grid == null) {
            return false;
        }
        String address = grid.getAddress();
        if ((address == null) || address.isEmpty() || LadderNumber.isNumber(address)) {
            return false;
        }
        int idx = address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)
                ? pane.getLadder().getIdx()
                : LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
        return ladders_.getValue(idx, address) != null;
    }

    private void toggleSelectedBlockValue() {
        LadderPane pane = selectedLadderPane();
        if ((pane == null) || (pane.getPreviousGrids().size() != 1)) {
            return;
        }
        LadderGrid grid = pane.getPreviousGrids().get(0);
        if (grid == null) {
            return;
        }
        String address = grid.getAddress();
        if ((address == null) || address.isEmpty() || LadderNumber.isNumber(address)) {
            return;
        }
        int idx = address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)
                ? pane.getLadder().getIdx()
                : LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
        Double gridValue = ladders_.getValue(idx, address);
        if (gridValue != null) {
            ladders_.setValue(idx, address, (gridValue == 0.0) ? 1.0 : 0.0);
        }
    }

    private void addMatchingAddressRows(LadderPane pane, String address) {
        for (int index = 0, size = pane.getChildren().size(); index < size; index++) {
            LadderGrid grid = ((LadderGridPane) pane.getChildren().get(index)).getLadderGrid();
            if (grid.getBlock() == LadderBlock.BLOCK_COMMENT) {
                continue;
            }
            boolean matched = address.equals(grid.getAddress());
            if (!matched) {
                LadderGrid.BlockFunction[] functions = grid.getBlockFunctions();
                if (functions != null) {
                    for (int i = 0; i < LadderGrid.LADDER_BLOCK_FUNCTIONS; i++) {
                        if (!functions[i].isNumber() && address.equals(functions[i].getAddress())) {
                            matched = true;
                            break;
                        }
                    }
                }
            }
            if (matched) {
                tableIo.getItems().add(new LadderTableIo(pane.getLadder().getName(), address,
                        grid.getBlock().toString(), grid.getColumnIndex(), grid.getRowIndex()));
            }
        }
    }

    private boolean findSelectedTreeTableIo() {
        TreeItem<LadderTreeTableIo> selectedItem = treeTableIo.getSelectionModel().getSelectedItem();
        if ((selectedItem == null) || (selectedItem.getParent() == null)) {
            return false;
        }
        LadderTreeTableIo treTableIo = selectedItem.getValue();
        LadderTreeTableIo treTableIoParent = selectedItem.getParent().getValue();
        if ((treTableIo == null) || (treTableIoParent == null)
                || (treTableIoParent == treeTableIo.getRoot().getValue())) {
            return false;
        }

        String address = treTableIo.getAddress();
        if ((address == null) || address.isEmpty()) {
            return false;
        }
        tableIo.getItems().clear();
        if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
            for (int index = 0, size = tabLadder.getTabs().size(); index < size; index++) {
                LadderPane pane = (LadderPane) ((ScrollPane) tabLadder.getTabs().get(index).getContent()).getContent();
                if (pane.getLadder().getName().replace(" ", "_").equals(treTableIoParent.getAddress())) {
                    addMatchingAddressRows(pane, address);
                    break;
                }
            }
        } else {
            for (int idx = 0, siz = tabLadder.getTabs().size(); idx < siz; idx++) {
                LadderPane pane = (LadderPane) ((ScrollPane) tabLadder.getTabs().get(idx).getContent()).getContent();
                addMatchingAddressRows(pane, address);
            }
        }
        return true;
    }

    private void findBlocksByQuery(String query) {
        if ((query == null) || query.trim().isEmpty()) {
            return;
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        setIoPanelVisible(true);
        tableIo.getItems().clear();
        for (int idx = 0, siz = tabLadder.getTabs().size(); idx < siz; idx++) {
            LadderPane pane = (LadderPane) ((ScrollPane) tabLadder.getTabs().get(idx).getContent()).getContent();
            for (int i = 0, size = pane.getChildren().size(); i < size; i++) {
                LadderGrid grid = ((LadderGridPane) pane.getChildren().get(i)).getLadderGrid();
                if (matchesSearchQuery(grid, normalized)) {
                    String addr = grid.getAddress();
                    String displayAddress = ((addr != null) && !addr.isEmpty()) ? addr : grid.getComment();
                    tableIo.getItems().add(new LadderTableIo(pane.getLadder().getName(), displayAddress,
                            grid.getBlock().toString(), grid.getColumnIndex(), grid.getRowIndex()));
                }
            }
        }
    }

    private TextInputDialog createUtilityInputDialog(String title, String contentText, String initialValue) {
        TextInputDialog dialog = new TextInputDialog(initialValue);
        dialog.initOwner(stage_);
        dialog.initStyle(StageStyle.UTILITY);
        if ((icons_ != null) && !icons_.isEmpty()) {
            ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().addAll(icons_);
        }
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(contentText);
        dialog.setGraphic(null);
        return dialog;
    }

    private void promptFindAddress() {
        Optional<String> result = createUtilityInputDialog("Find Address", "Address or comment:", "").showAndWait();
        if (stage_ != null) {
            stage_.requestFocus();
        }
        result.ifPresent(this::findBlocksByQuery);
    }

    private void promptIdealCycleTime() {
        Optional<String> result = createUtilityInputDialog("Ideal Cycle Time",
                "Ideal cycle time in nanoseconds (0 = fastest):",
                Long.toString(ladders_.getIdealCycleTime())).showAndWait();
        if (stage_ != null) {
            stage_.requestFocus();
        }
        result.ifPresent(text -> {
            Long parsed = parseIdealCycleTime(text);
            if (parsed != null) {
                ladders_.setIdealCycleTime(parsed);
            } else {
                Console.write(DesignLaddersController.class.getName(),
                        "Ideal cycle time must be an integer >= 0 [" + text + "]", true);
            }
        });
    }

    private void promptHistoryLimit() {
        Optional<String> result = createUtilityInputDialog("Undo History Limit",
                "Max undo steps (-1 = unlimited, 0 = disabled):",
                Integer.toString(ladders_.getHistoryGeneration())).showAndWait();
        if (stage_ != null) {
            stage_.requestFocus();
        }
        result.ifPresent(text -> {
            Integer parsed = parseHistoryLimit(text);
            if (parsed != null) {
                ladders_.setHistoryGeneration(parsed);
            } else {
                Console.write(DesignLaddersController.class.getName(),
                        "Undo history limit must be an integer >= -1 [" + text + "]", true);
            }
        });
    }

    private void addEventDesign() {
        // root
        stage_.setOnShowing((WindowEvent event) -> {
            stageSettings_.setStageMaximized(stageSettings_.isStageMaximized(), true);
            stageSettings_.setStageWidth(stageSettings_.getStageWidth(), true);
            stageSettings_.setStageHeight(stageSettings_.getStageHeight(), true);
            stageSettings_.setRootWidth(stageSettings_.getRootWidth(), true);
            stageSettings_.setRootHeight(stageSettings_.getRootHeight(), true);
            isShowing_ = true;
        });
        stage_.setOnShown((WindowEvent event) -> {
            stageSettings_.setIoLadderDividerPositions(stageSettings_.getIoLadderDividerPositions(), true);
            stageSettings_.setIoIoDividerPositions(stageSettings_.getIoIoDividerPositions(), true);
            isShown_ = true;
        });
        stage_.setOnHiding((WindowEvent event) -> {
            isShowing_ = false;
        });
        stage_.setOnHidden((WindowEvent event) -> {
            isShown_ = false;
        });
        stage_.maximizedProperty()
                .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (newValue != null) {
                        stageSettings_.setStageMaximized(newValue, false);
                        if (!newValue) {
                            stageSettings_.setStageWidth(stageSettings_.getStageWidth(), false);
                            stageSettings_.setStageHeight(stageSettings_.getStageHeight(), false);
                        }
                    }
                });
        stage_.widthProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!stageSettings_.isStageMaximized()) {
                        stageSettings_.setStageWidth(stage_.getWidth(), false);
                        stageSettings_.setRootWidth(paneRoot.getWidth(), false);
                    }
                });
        stage_.heightProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!stageSettings_.isStageMaximized()) {
                        stageSettings_.setStageHeight(stage_.getHeight(), false);
                        stageSettings_.setRootHeight(paneRoot.getHeight(), false);
                    }
                });
        attachIoLadderDividerListener();
        splitIoIo.getDividers().get(0).positionProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (newValue != null) {
                        stageSettings_.setIoIoDividerPositions(newValue.doubleValue(), false);
                    }
                });

        // menu file
        menuFile.setOnShowing((event) -> {
            boolean hasRecent = recentFileMenus_.stream()
                    .anyMatch(m -> (m.getText() != null) && !m.getText().isEmpty());
            menuFileOpenRecent.setDisable(!hasRecent);
            menuFileRecentClear.setDisable(!hasRecent);
        });
        menuFileNew.setOnAction((ActionEvent event) -> {
            ladders_.fileNew();
        });
        menuFileOpen.setOnAction((ActionEvent event) -> {
            if (ladders_.fileOpen(null)) {
                addRecentFile(ladders_.getFilePath().toString());
            }
        });
        menuFileSave.setOnAction((ActionEvent event) -> {
            if (ladders_.fileSave()) {
                addRecentFile(ladders_.getFilePath().toString());
            }
        });
        menuFileSaveAs.setOnAction((ActionEvent event) -> {
            if (ladders_.fileSaveAs()) {
                addRecentFile(ladders_.getFilePath().toString());
            }
        });
        menuFileExportPdf.setOnAction((ActionEvent event) -> {
            ladders_.exportPdf();
        });
        menuFileDifference.setOnAction((ActionEvent event) -> {
            // ladder
            try {
                File fcfile = ladders_.getFileStore().getFileChooser("JSON", "*.json", null).showOpenDialog(stage_);
                if (fcfile != null) {
                    Path file = fcfile.toPath();
                    LadderJson originalJson = ladders_.ladderJsonOpen(file);
                    if (originalJson != null) {
                        Stage stage = new Stage(StageStyle.DECORATED);
                        FXMLLoader loader = new FXMLLoader(
                                this.getClass().getClassLoader().getResource("fxml/DesignLaddersDifference.fxml"));
                        Parent root = (Parent) loader.load();
                        DesignLaddersDifferenceController ladderDifferenceController = (DesignLaddersDifferenceController) loader
                                .getController();
                        stage.setScene(new Scene(root));
                        if ((icons_ != null) && !icons_.isEmpty()) {
                            stage.getIcons().addAll(icons_);
                        }
                        ladderDifferenceController.startUp(stage, ladders_);
                        ladderDifferenceController.diffarence(originalJson, file,
                                ladders_.ladderJsonSave(tabLadder), ladders_.getFilePath());
                        stage.setTitle("Difference");
                        stage.showAndWait();
                    }
                }
            } catch (IOException ex) {
                Console.writeStackTrace(DesignLaddersController.class.getName(), ex);
            }
        });
        for (MenuItem recentFileMenu : recentFileMenus_) {
            recentFileMenu.setOnAction((ActionEvent event) -> {
                String pathText = recentFileMenu.getText();
                if ((pathText != null) && !pathText.isEmpty()) {
                    try {
                        if (ladders_.fileOpen(Paths.get(pathText))) {
                            addRecentFile(ladders_.getFilePath().toString());
                        }
                    } catch (InvalidPathException ex) {
                        Console.writeStackTrace(DesignLaddersController.class.getName(), ex);
                    }
                }
            });
        }
        menuFileRecentClear.setOnAction((ActionEvent event) -> {
            for (MenuItem recentFileMenu : recentFileMenus_) {
                recentFileMenu.setText("");
            }
            runViewRecentFile();
        });
        menuFileClose.setOnAction((ActionEvent event) -> {
            if (stage_ != null) {
                stage_.hide();
            }
        });

        // menu edit
        menuEdit.setOnShowing((event) -> {
            LadderCommand cmd = ladders_.getLadderCommand();
            menuEditUndo.setDisable((cmd == null) || !cmd.canUndo());
            menuEditRedo.setDisable((cmd == null) || !cmd.canRedo());

            LadderPane pane = selectedLadderPane();
            List<LadderGrid> selected = (pane != null) ? pane.getPreviousGrids() : List.of();
            boolean hasSelection = !selected.isEmpty();
            boolean singleCell = (selected.size() == 1)
                    && (selected.get(0).getColumnIndex() > 0)
                    && (selected.get(0).getRowIndex() > 0);
            boolean singleEditableRow = !selected.isEmpty()
                    && (selected.get(selected.size() - 1).getRowIndex() > 0);
            boolean hasGridCell = hasSelection && selected.stream()
                    .anyMatch(g -> (g.getColumnIndex() > 0) && (g.getRowIndex() > 0));
            LadderBlock singleBlock = singleCell ? selected.get(0).getBlock() : null;

            menuEditCut.setDisable(!hasSelection);
            menuEditCopy.setDisable(!hasSelection);
            menuEditPaste.setDisable(!singleCell || (cmd == null) || !cmd.hasCopyBuffer());
            menuEditDelete.setDisable(!hasSelection);
            menuEditBlock.setDisable(!singleCell);
            menuEditInvertBlock.setDisable(!singleCell || !isInvertibleBlock(singleBlock));
            menuEditCycleEdge.setDisable(!singleCell || !isEdgeCyclableBlock(singleBlock));

            boolean canWireLeft = singleCell && (selected.get(0).getColumnIndex() > 1);
            boolean canWireRight = singleCell && (selected.get(0).getColumnIndex() < (pane.getLadder().getColumn() - 1));
            boolean canWireUp = singleCell && (selected.get(0).getColumnIndex() > 1)
                    && (selected.get(0).getRowIndex() > 1);
            boolean canWireDown = singleCell && (selected.get(0).getColumnIndex() > 1)
                    && (selected.get(0).getRowIndex() < (pane.getLadder().getRow() - 1));
            menuEditWireLeft.setDisable(!canWireLeft);
            menuEditWireRight.setDisable(!canWireRight);
            menuEditWireUp.setDisable(!canWireUp);
            menuEditWireDown.setDisable(!canWireDown);
            menuEditWire.setDisable(!canWireLeft && !canWireRight && !canWireUp && !canWireDown);

            menuEditInsertRow.setDisable(!singleEditableRow);
            menuEditDeleteRow.setDisable(!singleEditableRow || (pane.getLadder().getRow() <= 2));

            TreeItem<LadderTreeTableIo> treeSel = treeTableIo.getSelectionModel().getSelectedItem();
            boolean canFindTreeIo = treeTableIo.isFocused() && (treeSel != null)
                    && (treeSel.getParent() != null) && (treeSel.getParent().getParent() != null);
            menuEditFindUsages.setDisable(!hasGridCell && !canFindTreeIo);
            menuEditFindAddress.setDisable(tabLadder.getTabs().isEmpty());
        });
        menuEdit.setOnHidden((event) -> {
            menuEditUndo.setDisable(false);
            menuEditRedo.setDisable(false);
            menuEditCut.setDisable(false);
            menuEditCopy.setDisable(false);
            menuEditPaste.setDisable(false);
            menuEditDelete.setDisable(false);
            menuEditBlock.setDisable(false);
            menuEditInvertBlock.setDisable(false);
            menuEditCycleEdge.setDisable(false);
            menuEditWire.setDisable(false);
            menuEditWireLeft.setDisable(false);
            menuEditWireRight.setDisable(false);
            menuEditWireUp.setDisable(false);
            menuEditWireDown.setDisable(false);
            menuEditInsertRow.setDisable(false);
            menuEditDeleteRow.setDisable(false);
            menuEditFindUsages.setDisable(false);
            menuEditFindAddress.setDisable(false);
        });
        menuEditUndo.setOnAction((ActionEvent event) -> {
            Tab tab = selectedTab();
            LadderPane pane = selectedLadderPane();
            ladders_.undo(tab, pane);
        });
        menuEditRedo.setOnAction((ActionEvent event) -> {
            Tab tab = selectedTab();
            LadderPane pane = selectedLadderPane();
            ladders_.redo(tab, pane);
        });
        menuEditCut.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.X, false, true, false);
        });
        menuEditCopy.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.C, false, true, false);
        });
        menuEditPaste.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.V, false, true, false);
        });
        menuEditDelete.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.DELETE, false, false, false);
        });
        menuEditBlock.setOnAction((ActionEvent event) -> {
            Tab tab = selectedTab();
            LadderPane pane = selectedLadderPane();
            if ((tab != null) && (tab.getContent() instanceof ScrollPane scrollPane) && (pane != null)
                    && (pane.getPreviousGrids().size() == 1)) {
                LadderGrid grid = pane.getPreviousGrids().get(0);
                showCommandInput(pane, scrollPane, LadderPane.formatGridCommand(grid), true, null);
            }
        });
        menuEditInvertBlock.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.SLASH, false, false, false);
        });
        menuEditCycleEdge.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.SLASH, false, true, false);
        });
        menuEditWireLeft.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.LEFT, false, true, false);
        });
        menuEditWireRight.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.RIGHT, false, true, false);
        });
        menuEditWireUp.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.UP, false, true, false);
        });
        menuEditWireDown.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.DOWN, false, true, false);
        });
        menuEditInsertRow.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.INSERT, true, false, false);
        });
        menuEditDeleteRow.setOnAction((ActionEvent event) -> {
            dispatchPaneKey(KeyCode.DELETE, true, false, false);
        });
        menuEditFindUsages.setOnAction((ActionEvent event) -> {
            if (!treeTableIo.isFocused() || !findSelectedTreeTableIo()) {
                dispatchPaneKey(KeyCode.F, false, true, false);
            }
        });
        menuEditFindAddress.setOnAction((ActionEvent event) -> {
            promptFindAddress();
        });

        // menu command
        populateCommandMenu();
        menuCommand.setOnShowing((event) -> {
            updateCommandMenuEnablement();
        });

        // menu tab
        menuTab.setOnShowing((event) -> {
            int count = tabLadder.getTabs().size();
            int selIdx = tabLadder.getSelectionModel().getSelectedIndex();
            boolean cycling = ladders_.isCycling();

            menuTabClose.setDisable(cycling || (count <= 0) || (selIdx < 0));
            menuTabRename.setDisable(selIdx < 0);
            menuTabMoveLeft.setDisable(cycling || (selIdx <= 0));
            menuTabMoveRight.setDisable(cycling || (selIdx < 0) || (selIdx >= (count - 1)));
            menuTabPrevious.setDisable(count <= 1);
            menuTabNext.setDisable(count <= 1);
        });
        menuTab.setOnHidden((event) -> {
            boolean cycling = ladders_.isCycling();
            menuTabClose.setDisable(cycling);
            menuTabRename.setDisable(false);
            menuTabMoveLeft.setDisable(cycling);
            menuTabMoveRight.setDisable(cycling);
            menuTabPrevious.setDisable(false);
            menuTabNext.setDisable(false);
        });
        menuTabNew.setOnAction((ActionEvent event) -> {
            ladders_.ladderNewTab();
        });
        menuTabClose.setOnAction((ActionEvent event) -> {
            if (!ladders_.isCycling() && (selectedTab() != null)) {
                ladders_.ladderRemoveTab(tabLadder.getSelectionModel().getSelectedIndex());
            }
        });
        menuTabRename.setOnAction((ActionEvent event) -> {
            LadderPane pane = selectedLadderPane();
            if (pane != null) {
                ladders_.ladderChangeTabName(pane);
            }
        });
        menuTabMoveLeft.setOnAction((ActionEvent event) -> {
            LadderPane pane = selectedLadderPane();
            if (!ladders_.isCycling() && (pane != null)) {
                ladders_.ladderMoveLeft(pane.getLadder());
            }
        });
        menuTabMoveRight.setOnAction((ActionEvent event) -> {
            LadderPane pane = selectedLadderPane();
            if (!ladders_.isCycling() && (pane != null)) {
                ladders_.ladderMoveRight(pane.getLadder());
            }
        });
        menuTabPrevious.setOnAction((ActionEvent event) -> {
            if (tabLadder.getTabs().size() > 1) {
                ladders_.ladderChangeSelectPrevious(tabLadder);
            }
        });
        menuTabNext.setOnAction((ActionEvent event) -> {
            if (tabLadder.getTabs().size() > 1) {
                ladders_.ladderChangeSelectNext(tabLadder);
            }
        });

        // menu view
        menuView.setOnShowing((event) -> {
            boolean ioVisible = splitIoLadder.getItems().contains(splitIoIo);
            boolean hasIoRoots = (treeTableIo.getRoot() != null) && !treeTableIo.getRoot().getChildren().isEmpty();
            menuViewIoPanel.setSelected(ioVisible);
            menuViewStatusBar.setSelected(statusBar.isVisible());
            menuViewExpandAllIo.setDisable(!ioVisible || !hasIoRoots);
            menuViewCollapseAllIo.setDisable(!ioVisible || !hasIoRoots);
            menuViewClearFindResults.setDisable(tableIo.getItems().isEmpty());
        });
        menuViewIoPanel.setOnAction((ActionEvent event) -> {
            setIoPanelVisible(menuViewIoPanel.isSelected());
        });
        menuViewStatusBar.setOnAction((ActionEvent event) -> {
            setStatusBarVisible(menuViewStatusBar.isSelected());
        });
        menuViewExpandAllIo.setOnAction((ActionEvent event) -> {
            if (treeTableIo.getRoot() != null) {
                for (TreeItem<LadderTreeTableIo> item : treeTableIo.getRoot().getChildren()) {
                    item.setExpanded(true);
                }
            }
        });
        menuViewCollapseAllIo.setOnAction((ActionEvent event) -> {
            if (treeTableIo.getRoot() != null) {
                for (TreeItem<LadderTreeTableIo> item : treeTableIo.getRoot().getChildren()) {
                    item.setExpanded(false);
                }
            }
        });
        menuViewClearFindResults.setOnAction((ActionEvent event) -> {
            tableIo.getItems().clear();
        });

        // menu tools
        menuTools.setOnShowing((event) -> {
            boolean cycling = ladders_.isCycling();
            menuToolsRun.setDisable(cycling);
            menuToolsStop.setDisable(!cycling);
            menuToolsToggleValue.setDisable(!canToggleSelectedBlockValue());
            menuToolsScriptEnable.setSelected(ladders_.isScriptEnable());
        });
        menuTools.setOnHidden((event) -> {
            menuToolsToggleValue.setDisable(false);
        });
        menuToolsConnect.setOnAction((ActionEvent event) -> {
            ladders_.connectLadder();
        });
        menuToolsRun.setOnAction((ActionEvent event) -> {
            if ((ladders_.getLadders() == null) && !ladders_.connectLadder()) {
                return;
            }
            viewRefresh_ = true;
            ladders_.runStartLadder();
        });
        menuToolsStop.setOnAction((ActionEvent event) -> {
            viewRefresh_ = false;
            ladders_.stopLadder();
        });
        menuToolsToggleValue.setOnAction((ActionEvent event) -> {
            toggleSelectedBlockValue();
        });
        menuToolsIdealCycleTime.setOnAction((ActionEvent event) -> {
            promptIdealCycleTime();
        });
        menuToolsHistoryLimit.setOnAction((ActionEvent event) -> {
            promptHistoryLimit();
        });
        menuToolsScriptEnable.setOnAction((ActionEvent event) -> {
            ladders_.setScriptEnable(menuToolsScriptEnable.isSelected());
        });

        // io
        treeTableIo.setOnKeyPressed((KeyEvent event) -> {
            if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                if (event.getCode() == KeyCode.F) {
                    if (findSelectedTreeTableIo()) {
                        event.consume();
                    }
                }
            } else if (!event.isShiftDown() && !event.isShortcutDown() && !event.isAltDown()) {
                if (event.getCode() == KeyCode.DELETE) {
                    event.consume();
                }
            }
        });
        treeTableIoAddress.setCellFactory((param) -> {
            return new TextFieldTreeTableCell<>(new LadderAddressStringConverter());
        });
        treeTableIoAddress.setCellValueFactory((param) -> {
            return param.getValue().getValue().addressProperty();
        });
        treeTableIoAddress.setOnEditCommit((event) -> {
            if ((event.getOldValue() != null) && (event.getNewValue() != null)) {
                if (!event.getOldValue().equals(event.getNewValue())) {
                    TreeItem<LadderTreeTableIo> parent = event.getRowValue().getParent();

                    if (parent == event.getTreeTableView().getRoot()) {
                        // tab name
                        int idx;
                        ObservableList<TreeItem<LadderTreeTableIo>> ovTreeTable = event.getTreeTableView().getRoot()
                                .getChildren();
                        for (idx = 0; idx < ovTreeTable.size(); idx++) {
                            if (ovTreeTable.get(idx) == event.getRowValue()) {
                                break;
                            }
                        }
                        if ((LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX < idx) && (idx < ovTreeTable.size())) {
                            ladders_.ladderChangeTabName(idx, event.getNewValue());
                        }
                    } else {
                        // address
                        int idx;
                        ObservableList<TreeItem<LadderTreeTableIo>> ovTreeTable = event.getTreeTableView().getRoot()
                                .getChildren();
                        for (idx = 0; idx < ovTreeTable.size(); idx++) {
                            if (ovTreeTable.get(idx) == parent) {
                                break;
                            }
                        }
                        if (idx < ovTreeTable.size()) {
                            int oldIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                            if (event.getOldValue().startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                oldIdx = idx;
                            }

                            int newIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                            if (event.getNewValue().startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                newIdx = idx;
                            }

                            ladders_.changeAddress(oldIdx, event.getOldValue(), newIdx, event.getNewValue());
                        }
                    }
                }
            }
        });
        treeTableIoComment.setCellFactory((param) -> {
            return new TextFieldTreeTableCell<>(new LadderCommentStringConverter());
        });
        treeTableIoComment.setCellValueFactory((param) -> {
            return param.getValue().getValue().commentProperty();
        });
        treeTableIoComment.setOnEditCommit((event) -> {
            if ((event.getOldValue() != null) && (event.getNewValue() != null)) {
                if (!event.getOldValue().equals(event.getNewValue())) {
                    TreeItem<LadderTreeTableIo> parent = event.getRowValue().getParent();

                    if (parent != event.getTreeTableView().getRoot()) {
                        // comment
                        int idx;
                        ObservableList<TreeItem<LadderTreeTableIo>> ovTreeTable = event.getTreeTableView().getRoot()
                                .getChildren();
                        for (idx = 0; idx < ovTreeTable.size(); idx++) {
                            if (ovTreeTable.get(idx) == parent) {
                                break;
                            }
                        }
                        if (idx < ovTreeTable.size()) {
                            ladders_.changeComment(idx, event.getRowValue().getValue().getAddress(),
                                    event.getNewValue());
                        }
                    }
                }
            }
        });
        treeTableIoValue.setCellFactory((param) -> {
            return new TextFieldTreeTableCell<>(new LadderValueStringConverter());
        });
        treeTableIoValue.setCellValueFactory((param) -> {
            return param.getValue().getValue().valueProperty().asObject();
        });
        treeTableIoValue.setOnEditCommit((event) -> {
            if ((event.getOldValue() != null) && (event.getNewValue() != null)) {
                if (!event.getOldValue().equals(event.getNewValue())) {
                    TreeItem<LadderTreeTableIo> parent = event.getRowValue().getParent();

                    if ((parent != null) && (parent != event.getTreeTableView().getRoot())) {
                        ObservableList<TreeItem<LadderTreeTableIo>> ovTreeTable = event.getTreeTableView().getRoot()
                                .getChildren();
                        int idx;
                        for (idx = 0; idx < ovTreeTable.size(); idx++) {
                            if (ovTreeTable.get(idx) == parent) {
                                break;
                            }
                        }
                        if (idx < ovTreeTable.size()) {
                            String ioAddress = event.getRowValue().getValue().getAddress();
                            if ((ioAddress != null) && !ioAddress.isEmpty()) {
                                int targetIdx = ioAddress.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)
                                        ? idx
                                        : LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                                ladders_.setValue(targetIdx, ioAddress, event.getNewValue());
                            }
                        }
                    }
                }
            }
        });
        tableIo.setOnKeyReleased((KeyEvent event) -> {
            ScrollPane scrollPane;
            LadderPane pane;
            LadderGridPane gridPane;
            LadderTableIo tblIo;

            event.consume();
            switch (event.getCode()) {
                case UP:
                case DOWN:
                    tblIo = tableIo.getSelectionModel().getSelectedItem();
                    if (tblIo != null) {
                        for (int index = 0, size = tabLadder.getTabs().size(); index < size; index++) {
                            scrollPane = (ScrollPane) tabLadder.getTabs().get(index).getContent();
                            pane = (LadderPane) scrollPane.getContent();
                            if (pane.getLadder().getName().equals(tblIo.getName())) {
                                gridPane = pane.findGridPane(tblIo.getColumn(), tblIo.getRow());
                                if (gridPane != null) {
                                    tabLadder.getSelectionModel().select(index);
                                    ladders_.getLadderCommand().blockChangeSelect(scrollPane, pane,
                                            gridPane.getLadderGrid(), false);
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        });
        tableIo.setOnMouseClicked((MouseEvent event) -> {
            ScrollPane scrollPane;
            LadderPane pane;
            LadderGridPane gridPane;
            LadderTableIo tblIo;

            event.consume();
            switch (event.getClickCount()) {
                case 1:
                    tblIo = tableIo.getSelectionModel().getSelectedItem();
                    if (tblIo != null) {
                        for (int index = 0, size = tabLadder.getTabs().size(); index < size; index++) {
                            scrollPane = (ScrollPane) tabLadder.getTabs().get(index).getContent();
                            pane = (LadderPane) scrollPane.getContent();
                            if (pane.getLadder().getName().equals(tblIo.getName())) {
                                gridPane = pane.findGridPane(tblIo.getColumn(), tblIo.getRow());
                                if (gridPane != null) {
                                    tabLadder.getSelectionModel().select(index);
                                    ladders_.getLadderCommand().blockChangeSelect(scrollPane, pane,
                                            gridPane.getLadderGrid(), false);
                                }
                            }
                        }
                    }
                    break;
            }
        });
        tableIoName.setCellValueFactory((param) -> {
            return param.getValue().nameProperty();
        });
        tableIoAddress.setCellValueFactory((param) -> {
            return param.getValue().addressProperty();
        });
        tableIoBlock.setCellValueFactory((param) -> {
            return param.getValue().blockProperty();
        });
        tableIoPosition.setCellValueFactory((param) -> {
            return param.getValue().positionProperty();
        });

        // ladder
        tabLadder.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if ((tabLadder.getSelectionModel().getSelectedItem() != null) && ladders_.isEditorKey(event)) {
                event.consume();
                ScrollPane scrollPane = (ScrollPane) tabLadder.getSelectionModel().getSelectedItem().getContent();
                LadderPane pane = (LadderPane) scrollPane.getContent();
                ladders_.onKeyPressed(pane, event, scrollPane);
            }
        });
        tabLadder.addEventFilter(KeyEvent.KEY_TYPED, (KeyEvent event) -> {
            if (tabLadder.getSelectionModel().getSelectedItem() != null) {
                ScrollPane scrollPane = (ScrollPane) tabLadder.getSelectionModel().getSelectedItem().getContent();
                LadderPane pane = (LadderPane) scrollPane.getContent();
                if (ladders_.onKeyTyped(pane, event, scrollPane)) {
                    event.consume();
                }
            }
        });
        tabLadder.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if (isCommandInputActive()) {
                closeCommandInput(false);
            }
        });
        tabLadder.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (isCommandInputActive()) {
                closeCommandInput(false);
            }
        });

        // inline command input
        txtCommandInput.setOnAction((ActionEvent event) -> commitCommandInput());
        btnCommandInputOk.setOnAction((ActionEvent event) -> commitCommandInput());
        btnCommandInputCancel.setOnAction((ActionEvent event) -> closeCommandInput(true));
        txtCommandInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isCommandInputActive()) {
                lblCommandInputHint.setText(LadderCommandCatalog.hintForInput(newVal));
                lblCommandInputHint.setStyle("-fx-font-size: 11px; -fx-opacity: 0.85;");
            }
        });
        txtCommandInput.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                closeCommandInput(true);
            }
        });
        boxCommandInput.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (boxCommandInput.isVisible() && (event.isShortcutDown() || event.isAltDown()
                    || (event.getCode() == KeyCode.DELETE) || (event.getCode() == KeyCode.INSERT)
                    || event.getCode().isFunctionKey())) {
                event.consume();
            }
        });
    }

    private void runInitDesign() {
        if (Platform.isFxApplicationThread()) {
            initDesign();
        } else {
            Platform.runLater(() -> {
                initDesign();
            });
        }
    }

    private void initDesign() {
        // menu file
        menuFile.setText("File");
        menuFileNew.setText("New");
        menuFileOpen.setText("Open...");
        menuFileOpenRecent.setText("Open Recent");
        menuFileRecentClear.setText("Clear Recent");
        menuFileSave.setText("Save");
        menuFileSaveAs.setText("Save As...");
        menuFileExportPdf.setText("Export as PDF...");
        menuFileDifference.setText("Difference...");
        menuFileClose.setText("Close");

        // menu edit
        menuEdit.setText("Edit");
        menuEditUndo.setText("Undo");
        menuEditRedo.setText("Redo");
        menuEditCut.setText("Cut");
        menuEditCopy.setText("Copy");
        menuEditPaste.setText("Paste");
        menuEditDelete.setText("Delete");
        menuEditBlock.setText("Edit Block...");
        menuEditInvertBlock.setText("Invert Block");
        menuEditCycleEdge.setText("Cycle Edge");
        menuEditWire.setText("Wire");
        menuEditWireLeft.setText("Wire Left");
        menuEditWireRight.setText("Wire Right");
        menuEditWireUp.setText("Wire Up");
        menuEditWireDown.setText("Wire Down");
        menuEditInsertRow.setText("Insert Row");
        menuEditDeleteRow.setText("Delete Row");
        menuEditFindUsages.setText("Find Usages");
        menuEditFindAddress.setText("Find Address...");

        // menu command
        menuCommand.setText("Command");

        // menu tab
        menuTab.setText("Tab");
        menuTabNew.setText("New Tab");
        menuTabClose.setText("Close Tab");
        menuTabRename.setText("Rename Tab...");
        menuTabMoveLeft.setText("Move Tab Left");
        menuTabMoveRight.setText("Move Tab Right");
        menuTabPrevious.setText("Previous Tab");
        menuTabNext.setText("Next Tab");

        // menu view
        menuView.setText("View");
        menuViewIoPanel.setText("I/O Panel");
        menuViewIoPanel.setSelected(splitIoLadder.getItems().contains(splitIoIo));
        menuViewStatusBar.setText("Status Bar");
        menuViewStatusBar.setSelected(statusBar.isVisible());
        menuViewExpandAllIo.setText("Expand All I/O");
        menuViewCollapseAllIo.setText("Collapse All I/O");
        menuViewClearFindResults.setText("Clear Find Results");

        // menu tools
        menuTools.setText("Tools");
        menuToolsConnect.setText("Connect");
        menuToolsRun.setText("Run");
        menuToolsStop.setText("Stop");
        menuToolsStop.setDisable(true);
        menuToolsToggleValue.setText("Toggle Block Value");
        menuToolsIdealCycleTime.setText("Ideal Cycle Time...");
        menuToolsHistoryLimit.setText("Undo History Limit...");
        menuToolsScriptEnable.setText("Enable Scripts (SCR)");
        menuToolsScriptEnable.setSelected(ladders_.isScriptEnable());

        // inline command input
        btnCommandInputOk.setText("OK");
        btnCommandInputCancel.setText("Cancel");

        // io
        treeTableIo.setCache(true);
        treeTableIoAddress.setText("Address");
        treeTableIoComment.setText("Comment");
        treeTableIoValue.setText("Value");
        tableIo.setCache(true);
        tableIoName.setText("Name");
        tableIoAddress.setText("Address");
        tableIoBlock.setText("Block");
        tableIoPosition.setText("Position");
    }

    /**
     *
     * @return
     */
    public Ladders getLadders() {
        return ladders_;
    }

    /**
     *
     * @return
     */
    public TabPane getTabLadder() {
        return tabLadder;
    }

    /**
     *
     * @return
     */
    public TreeTableView<LadderTreeTableIo> getTreeTableIo() {
        return treeTableIo;
    }

    /**
     *
     * @return
     */
    public TableView<LadderTableIo> getTableIo() {
        return tableIo;
    }

    /**
     *
     * @param historyGeneration
     */
    public void setHistoryGeneration(int historyGeneration) {
        historyGeneration_ = historyGeneration;
    }

    /**
     *
     * @param idealCycleTime
     */
    public void setIdealCycleTime(long idealCycleTime) {
        idealCycleTime_ = idealCycleTime;
        final long value = idealCycleTime;
        Platform.runLater(() -> {
            lblIdealCycleTime.setText(Long.toString(value) + "ns(Ideal)");
        });
    }

    /**
     *
     * @param bln
     */
    public void runViewRunning(boolean bln) {
        if (Platform.isFxApplicationThread()) {
            viewRunning(bln);
        } else {
            Platform.runLater(() -> {
                viewRunning(bln);
            });
        }
    }

    private void viewRunning(boolean bln) {
        menuTabClose.setDisable(bln);
        menuTabMoveLeft.setDisable(bln);
        menuTabMoveRight.setDisable(bln);
        menuToolsRun.setDisable(bln);
        menuToolsStop.setDisable(!bln);
    }

    /**
     *
     * @param minCycleTime
     */
    public void setMinCycleTime(long minCycleTime) {
        Platform.runLater(() -> {
            lblMinCycleTime.setText(Long.toString(minCycleTime) + "ns(Min)");
        });
    }

    /**
     *
     * @param maxCycleTime
     */
    public void setMaxCycleTime(long maxCycleTime) {
        Platform.runLater(() -> {
            lblMaxCycleTime.setText(Long.toString(maxCycleTime) + "ns(Max)");
        });
    }

    /**
     *
     * @param cumulativeCycleTime
     * @param cumulativeCycleTimeCount
     */
    public void refreshLadder(long cumulativeCycleTime, long cumulativeCycleTimeCount) {
        if (isShown_ && viewRefresh_) {
            viewRefresh_ = false;
            Platform.runLater(() -> {
                try {
                    // cycletime
                    lblCycleTime.setText(Long.toString(cumulativeCycleTime
                            / Math.max(1, cumulativeCycleTimeCount)) + "ns");

                    // io map
                    ioMap_ = ladders_.getIoMap();

                    // ladder refresh : the variables of the refresh are local, because the
                    // refresh of the next cycle can be queued before this one is done
                    Tab selectedTab = tabLadder.getSelectionModel().getSelectedItem();

                    if (selectedTab != null) {
                        ScrollPane selectedScrollPane = (ScrollPane) selectedTab.getContent();

                        ((LadderPane) selectedScrollPane.getContent()).refreshView(ioMap_, selectedScrollPane);
                    }

                    // tree table refresh : the row of the tree answers the ladder of the io map
                    ObservableList<TreeItem<LadderTreeTableIo>> ladderRows = treeTableIo.getRoot().getChildren();

                    for (int ladderIndex = 0; ladderIndex < ladderRows.size(); ladderIndex++) {
                        TreeItem<LadderTreeTableIo> ladderRow = ladderRows.get(ladderIndex);

                        if (!ladderRow.isExpanded() || (ladderIndex >= ioMap_.size())) {
                            continue;
                        }
                        ConcurrentHashMap<String, LadderIo> ladderIoMap = ioMap_.get(ladderIndex);

                        for (TreeItem<LadderTreeTableIo> ioRow : ladderRow.getChildren()) {
                            LadderTreeTableIo tableIo = ioRow.getValue();
                            LadderIo ladderIo;

                            if (tableIo == null) {
                                continue;
                            }
                            ladderIo = ladderIoMap.get(tableIo.getAddress());
                            if (ladderIo == null) {
                                // the io of the row is not registered any more : the cycle does
                                // not answer it, so the value of the row is left as it is
                                continue;
                            }
                            double value = ladderIo.getValue();

                            if (value != tableIo.getValue()) {
                                tableIo.setValue(value);
                            }
                        }
                    }
                } finally {
                    viewRefresh_ = true;
                }
            });
        }
    }

    /**
     *
     * @param filename
     */
    public void addRecentFile(String filename) {
        if ((filename != null) && !filename.isEmpty()) {
            List<String> recent = new ArrayList<>();
            recent.add(filename);
            for (MenuItem recentFileMenu : recentFileMenus_) {
                String existing = recentFileMenu.getText();
                if ((existing != null) && !existing.isEmpty() && !existing.equals(filename)) {
                    recent.add(existing);
                    if (recent.size() >= recentFileMenus_.size()) {
                        break;
                    }
                }
            }
            for (int i = 0; i < recentFileMenus_.size(); i++) {
                recentFileMenus_.get(i).setText((i < recent.size()) ? recent.get(i) : "");
            }
            runViewRecentFile();
        }
    }

    private void runViewRecentFile() {
        if (Platform.isFxApplicationThread()) {
            viewRecentFile();
        } else {
            Platform.runLater(() -> {
                viewRecentFile();
            });
        }
    }

    private void viewRecentFile() {
        boolean hasRecent = false;
        for (MenuItem recentFileMenu : recentFileMenus_) {
            String text = recentFileMenu.getText();
            boolean visible = (text != null) && !text.isEmpty();
            recentFileMenu.setVisible(visible);
            if (visible) {
                hasRecent = true;
            }
        }
        if (menuFileOpenRecent != null) {
            menuFileOpenRecent.setDisable(!hasRecent);
        }
        if (menuFileRecentSeparator != null) {
            menuFileRecentSeparator.setVisible(hasRecent);
        }
        if (menuFileRecentClear != null) {
            menuFileRecentClear.setDisable(!hasRecent);
        }
    }

    private void initProperties() {
        // stage
        stageSettings_.setStageMaximized(false, true);
        stageSettings_.setStageWidth(640.0, true);
        stageSettings_.setStageHeight(480.0, true);
        stageSettings_.setRootWidth(640.0, true);
        stageSettings_.setRootHeight(480.0, true);
        stageSettings_.setIoLadderDividerPositions(0.382, true);
        stageSettings_.setIoIoDividerPositions(0.618, true);

        // history generation
        historyGeneration_ = -1;

        // ideal cycletime
        idealCycleTime_ = 0;

        // ladder scripts
        ladders_.setScriptEnable(true);
        if (menuToolsScriptEnable != null) {
            menuToolsScriptEnable.setSelected(true);
        }
        if (menuViewIoPanel != null) {
            menuViewIoPanel.setSelected(true);
        }
        if (menuViewStatusBar != null) {
            menuViewStatusBar.setSelected(true);
        }

        // menu
        for (MenuItem recentFileMenu : recentFileMenus_) {
            recentFileMenu.setText("");
        }
    }

    /**
     * read a numeric property : a damaged properties.xml has to keep the application
     * running, so an unreadable value is only reported and the default is used
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return
     */
    private double parseDoubleProperty(Properties properties, String key, double defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed) || (parsed <= 0.0)) {
                Console.write(DesignLaddersController.class.getName(),
                        "properties : " + key + " is out of range [" + value + "]", true);
                return defaultValue;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            Console.write(DesignLaddersController.class.getName(),
                    "properties : " + key + " is not a number [" + value + "]", true);
            return defaultValue;
        }
    }

    /**
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return
     */
    private int parseIntProperty(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Console.write(DesignLaddersController.class.getName(),
                    "properties : " + key + " is not an integer [" + value + "]", true);
            return defaultValue;
        }
    }

    /**
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return
     */
    private long parseLongProperty(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            Console.write(DesignLaddersController.class.getName(),
                    "properties : " + key + " is not a long number [" + value + "]", true);
            return defaultValue;
        }
    }

    private boolean loadProperties(Path propertyFile) {
        if (Files.exists(propertyFile) && Files.isRegularFile(propertyFile) && Files.isReadable(propertyFile)) {
            Properties properties = new Properties();
            try (InputStream inputStream = Files.newInputStream(propertyFile)) {
                properties.loadFromXML(inputStream);

                // stage
                stageSettings_.setStageMaximized(
                        (Boolean.parseBoolean(properties.getProperty("STAGE_MAXIMIZED", "false"))), true);
                stageSettings_.setStageWidth(parseDoubleProperty(properties, "STAGE_WIDTH", 640.0), true);
                stageSettings_.setStageHeight(parseDoubleProperty(properties, "STAGE_HEIGHT", 480.0), true);
                stageSettings_.setRootWidth(parseDoubleProperty(properties, "PANE_ROOT_WIDTH", 640.0), true);
                stageSettings_.setRootHeight(parseDoubleProperty(properties, "PANE_ROOT_HEIGHT", 480.0), true);
                stageSettings_.setIoLadderDividerPositions(
                        parseDoubleProperty(properties, "SPLIT_DIVIDER_IO_LADDER", 0.382), true);
                stageSettings_.setIoIoDividerPositions(
                        parseDoubleProperty(properties, "SPLIT_DIVIDER_IO_IO", 0.618), true);
                ladders_.setWorkFilePath(properties.getProperty("WORK_FILE_PATH", ""));

                // history generation
                ladders_.setHistoryGeneration(parseIntProperty(properties, "HISTORY_GENERATION", -1));

                // ideal cycletime
                ladders_.setIdealCycleTime(parseLongProperty(properties, "IDEAL_CYCLE_TIME", 0));

                // ladder scripts
                ladders_.setScriptEnable(Boolean.parseBoolean(properties.getProperty("LADDER_SCRIPT_ENABLE", "true")));

                // menu
                for (int i = 0; i < recentFileMenus_.size(); i++) {
                    recentFileMenus_.get(i).setText(properties.getProperty("MENU_RECENT_FILE_" + (i + 1), ""));
                }
                return true;
            } catch (IOException ex) {
                Console.writeStackTrace(DesignLaddersController.class.getName(), ex);
            }
        }
        // stage
        stageSettings_.setStageMaximized(stageSettings_.isStageMaximized(), true);
        stageSettings_.setStageWidth(stageSettings_.getStageWidth(), true);
        stageSettings_.setStageHeight(stageSettings_.getStageHeight(), true);
        stageSettings_.setRootWidth(stageSettings_.getRootWidth(), true);
        stageSettings_.setRootHeight(stageSettings_.getRootHeight(), true);
        stageSettings_.setIoLadderDividerPositions(stageSettings_.getIoLadderDividerPositions(), true);
        stageSettings_.setIoIoDividerPositions(stageSettings_.getIoIoDividerPositions(), true);
        return false;
    }

    private boolean saveProperties(Path propertyFile) {
        try {
            if (!Files.exists(propertyFile)) {
                Files.createFile(propertyFile);
            }
            if (Files.isWritable(propertyFile)) {
                Properties properties = new Properties();

                // stage
                properties.setProperty("STAGE_MAXIMIZED", Boolean.toString(stageSettings_.isStageMaximized()));
                properties.setProperty("STAGE_WIDTH", Double.toString(stageSettings_.getStageWidth()));
                properties.setProperty("STAGE_HEIGHT", Double.toString(stageSettings_.getStageHeight()));
                properties.setProperty("PANE_ROOT_WIDTH", Double.toString(stageSettings_.getRootWidth()));
                properties.setProperty("PANE_ROOT_HEIGHT", Double.toString(stageSettings_.getRootHeight()));
                properties.setProperty("SPLIT_DIVIDER_IO_LADDER",
                        Double.toString(stageSettings_.getIoLadderDividerPositions()));
                properties.setProperty("SPLIT_DIVIDER_IO_IO",
                        Double.toString(stageSettings_.getIoIoDividerPositions()));
                properties.setProperty("WORK_FILE_PATH", ladders_.getWorkFilePath());

                // history generation
                properties.setProperty("HISTORY_GENERATION", Integer.toString(ladders_.getHistoryGeneration()));

                // ideal cycletime
                properties.setProperty("IDEAL_CYCLE_TIME", Long.toString(ladders_.getIdealCycleTime()));

                // ladder scripts
                properties.setProperty("LADDER_SCRIPT_ENABLE", Boolean.toString(ladders_.isScriptEnable()));

                // menu
                for (int i = 0; i < recentFileMenus_.size(); i++) {
                    String text = recentFileMenus_.get(i).getText();
                    properties.setProperty("MENU_RECENT_FILE_" + (i + 1), (text != null) ? text : "");
                }

                try (OutputStream outputStream = Files.newOutputStream(propertyFile)) {
                    properties.storeToXML(outputStream, "Ladders");
                }
                return true;
            }
        } catch (IOException ex) {
            Console.writeStackTrace(DesignLaddersController.class.getName(), ex);
        }
        return false;
    }

    public Stage getStage() {
        return stage_;
    }

    /**
     *
     * @param stage
     * @param icons
     * @param currentPath
     */
    public void startUp(Stage stage, List<Image> icons, Path currentPath) {
        stage_ = stage;
        icons_ = icons;

        // ladder path
        ladderPath_ = currentPath.resolve("ladder");
        if (!Files.exists(ladderPath_)) {
            try {
                Files.createDirectories(ladderPath_);
            } catch (IOException ex) {
                Console.writeStackTrace(DesignLaddersController.class.getName(), ex);
            }
        }

        // load properties
        initProperties();
        loadProperties(ladderPath_.resolve("properties.xml"));

        // design
        addEventDesign();
        runInitDesign();
        runViewRecentFile();

        // ladders
        ladders_.startUp(stage_, icons_, ladderPath_);
        ladders_.setHistoryGeneration(historyGeneration_);
        ladders_.setIdealCycleTime(idealCycleTime_);
    }

    /**
     *
     * @return
     */
    public boolean cleanUp() {
        // save check
        ButtonType response = ladders_.fileSavedCheck();
        if ((response == ButtonType.YES) || ((ladders_.getFileName() != null) && (response == null))) {
            if (ladders_.fileSave()) {
                addRecentFile(ladders_.getFilePath().toString());
            } else {
                return false;
            }
        } else if ((response == ButtonType.CANCEL) || (response == ButtonType.CLOSE)) {
            return false;
        }

        // save properties
        if (ladderPath_ != null) {
            saveProperties(ladderPath_.resolve("properties.xml"));
        }

        // clean up
        ladders_.cleanUp();

        return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stage_ = null;
        icons_ = null;
        stageSettings_ = new StageSettings();
        ioLadderDividerListener_ = null;
        recentFileMenus_ = List.of(menuFileRecentFile1, menuFileRecentFile2, menuFileRecentFile3,
                menuFileRecentFile4, menuFileRecentFile5, menuFileRecentFile6, menuFileRecentFile7,
                menuFileRecentFile8, menuFileRecentFile9, menuFileRecentFile10);

        ladders_ = new Ladders(this);
        historyGeneration_ = -1;
        idealCycleTime_ = 0;
        ladderPath_ = null;

        ioMap_ = null;

        viewRefresh_ = true;
        isShowing_ = false;
        isShown_ = false;
        commandInputTab_ = null;
        commandInputPane_ = null;
        commandInputScrollPane_ = null;
        commandInputGrid_ = null;
        commandInputGridPane_ = null;
        contactCommandItems_ = new ArrayList<>();
        outputCommandItems_ = new ArrayList<>();
        commandCategoryMenus_ = new EnumMap<>(LadderCommandCatalog.Category.class);
    }
}
