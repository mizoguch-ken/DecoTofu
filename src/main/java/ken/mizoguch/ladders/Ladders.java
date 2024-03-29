/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ken.mizoguch.console.Console;
import ken.mizoguch.decotofu.JavaLibrary;
import ken.mizoguch.soem.Soem;
import netscape.javascript.JSException;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 *
 * @author mizoguch-ken
 */
public class Ladders extends Service<Void> {

    public static final long LADDER_VIEW_REFRESH_CYCLE_TIME = TimeUnit.MILLISECONDS.toNanos(100);
    public static final int LADDER_DEFAULT_GRID_COLUMN = 10;
    public static final int LADDER_DEFAULT_GRID_ROW = 10;
    public static final double LADDER_DEFAULT_GRID_MIN_SIZE = 78.912;
    public static final double LADDER_DEFAULT_GRID_MAX_SIZE = LADDER_DEFAULT_GRID_MIN_SIZE * 122.966;
    public static final double LADDER_DEFAULT_GRID_CONTENTS_WIDTH = LADDER_DEFAULT_GRID_MIN_SIZE * 0.382;
    public static final double LADDER_DEFAULT_GRID_CONTENTS_HIGHT = LADDER_DEFAULT_GRID_CONTENTS_WIDTH * 0.618;

    public static final int LADDER_GLOBAL_ADDRESS_INDEX = 0;
    public static final String LADDER_LOCAL_ADDRESS_PREFIX = ".";

    /**
     *
     */
    public static enum LADDER_BLOCK {
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

        private LADDER_BLOCK(final String command) {
            command_ = command;
        }

        public String toCommand() {
            return command_;
        }

        public static LADDER_BLOCK get(String command) {
            command = command.toUpperCase(Locale.getDefault());
            for (LADDER_BLOCK value : LADDER_BLOCK.values()) {
                if (value.command_.equals(command)) {
                    return value;
                }
            }
            return EMPTY;
        }
    }

    /**
     *
     */
    public static enum LADDER_COMMAND {
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

    private final DesignLaddersController ladderController_;
    private final TabPane tabLadder_;
    private final TreeTableView<LadderTreeTableIo> treeTableIo_;
    private final TableView<LadderTableIo> tableIo_;

    private Stage stage_;
    private List<Image> icons_;
    private WebEngine webEngine_;
    private Worker.State state_;
    private Soem soem_;
    private LadderCommand ladderCommand_;
    private int historyGeneration_;
    private final CopyOnWriteArrayList<LadderRegisterSoemIo> registerSoemIn_, registerSoemOut_;
    private Path filePath_;
    private final String newLineCharacter_ = "\r\n";

    private final Object lock_ = new Object();
    private Ladder[] ladders_;
    private ObservableList<TreeItem<LadderTreeTableIo>> ovScript_;
    private int laddersSize_, scriptIndex_, scriptSize_;
    private final CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap_;
    private final CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap_;
    private final CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap_;
    private long idealCycleTime_;
    private boolean isCycling_, isChanged_;

    private final Gson gson_ = new GsonBuilder().setPrettyPrinting().create();

    /**
     *
     * @param ladderController
     */
    public Ladders(DesignLaddersController ladderController) {
        ladderController_ = ladderController;
        tabLadder_ = ladderController_.getTabLadder();
        treeTableIo_ = ladderController_.getTreeTableIo();
        treeTableIo_.setRoot(new TreeItem<>());
        tableIo_ = ladderController.getTableIo();

        stage_ = null;
        icons_ = null;
        webEngine_ = null;
        state_ = Worker.State.READY;
        soem_ = null;
        ladderCommand_ = null;
        historyGeneration_ = 0;
        registerSoemIn_ = new CopyOnWriteArrayList<>();
        registerSoemOut_ = new CopyOnWriteArrayList<>();
        filePath_ = null;

        ladders_ = null;
        laddersSize_ = 0;
        scriptIndex_ = 0;
        scriptSize_ = 0;
        ioMap_ = new CopyOnWriteArrayList<>();
        commentMap_ = new CopyOnWriteArrayList<>();
        scriptIoMap_ = new CopyOnWriteArrayList<>();
        idealCycleTime_ = 0;
        isChanged_ = false;
        isCycling_ = false;
    }

    /**
     *
     * @return
     */
    public DesignLaddersController getDesignController() {
        return ladderController_;
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
     * @return
     */
    public CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> getIoMap() {
        return ioMap_;
    }

    /**
     *
     * @return
     */
    public CopyOnWriteArrayList<ConcurrentHashMap<String, String>> getCommentMap() {
        return commentMap_;
    }

    /**
     *
     * @return
     */
    public CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> getScriptIoMap() {
        return scriptIoMap_;
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
        return commentMap_.get(idx).containsKey(address);
    }

    /**
     *
     * @param idx
     * @param address
     * @return
     */
    public String getComment(int idx, String address) {
        return commentMap_.get(idx).get(address);
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
        setValueDirect(LADDER_GLOBAL_ADDRESS_INDEX, address, value);
    }

    /**
     *
     * @param idx
     * @param address
     * @param value
     */
    public void setValueDirect(int idx, String address, double value) {
        if (!ioMap_.get(idx).containsKey(address)) {
            ioMap_.get(idx).put(address, new LadderIo(address));
            treeTableIo_.getRoot().getChildren().get(idx).getChildren()
                    .add(new TreeItem<>(new LadderTreeTableIo(address, value)));
        }

        ovScript_ = treeTableIo_.getRoot().getChildren().get(idx).getChildren();
        for (scriptIndex_ = 0, scriptSize_ = ovScript_.size(); scriptIndex_ < scriptSize_; scriptIndex_++) {
            if (address.equals(ovScript_.get(scriptIndex_).getValue().getAddress())) {
                ovScript_.get(scriptIndex_).getValue().setValue(value);
                break;
            }
        }

        synchronized (lock_) {
            ioMap_.get(idx).get(address).setValue(value);
            scriptIoMap_.get(idx).putIfAbsent(address, new LadderIo(address));
            scriptIoMap_.get(idx).get(address).setValue(value);
            scriptIoMap_.get(idx).get(address).setCycled(true);
        }
    }

    private void allClear() {
        stopLadder();

        if (registerSoemIn_ != null) {
            registerSoemIn_.clear();
        }
        if (registerSoemOut_ != null) {
            registerSoemOut_.clear();
        }

        tabLadder_.getTabs().clear();
        treeTableIo_.getRoot().getChildren().clear();
        ladderCommand_.clearHistoryManager();
        ladders_ = null;
        ioMap_.clear();
        commentMap_.clear();
        scriptIoMap_.clear();
    }

    /**
     *
     * @param workFilePath
     */
    public void setWorkFilePath(String workFilePath) {
        if (workFilePath != null) {
            if (workFilePath.isEmpty()) {
                filePath_ = null;
            } else {
                filePath_ = Paths.get(workFilePath);
                if (!Files.isDirectory(filePath_)) {
                    filePath_ = filePath_.getParent();
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public String getWorkFilePath() {
        if (filePath_ == null) {
            return "";
        } else {
            return filePath_.toString();
        }
    }

    /**
     *
     */
    public void ladderNew() {
        allClear();

        // global
        treeTableIo_.getRoot().getChildren().add(LADDER_GLOBAL_ADDRESS_INDEX,
                new TreeItem<>(new LadderTreeTableIo("Global".replace(" ", "_"), LADDER_GLOBAL_ADDRESS_INDEX)));
        ioMap_.add(LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());
        commentMap_.add(LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());
        scriptIoMap_.add(LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());

        ladderCommand_.ladderCreate(LADDER_GLOBAL_ADDRESS_INDEX + 1, "Ladders", LADDER_DEFAULT_GRID_COLUMN,
                LADDER_DEFAULT_GRID_ROW, LADDER_DEFAULT_GRID_MIN_SIZE, LADDER_DEFAULT_GRID_MAX_SIZE,
                LADDER_DEFAULT_GRID_CONTENTS_WIDTH, LADDER_DEFAULT_GRID_CONTENTS_HIGHT);
    }

    /**
     *
     * @return
     */
    public boolean ladderNewTab() {
        StringBuilder name = new StringBuilder("Ladders");
        int index, size;

        for (index = 0, size = tabLadder_.getTabs().size(); index < size; index++) {
            if (checkTabName(name.toString())) {
                break;
            }
            name.delete(0, name.length());
            name.append("Ladders").append(" ").append(index + 1);
        }
        index = tabLadder_.getTabs().size() + 1;
        LadderPane pane = ladderCommand_.ladderCreate(index, name.toString(), LADDER_DEFAULT_GRID_COLUMN,
                LADDER_DEFAULT_GRID_ROW, LADDER_DEFAULT_GRID_MIN_SIZE, LADDER_DEFAULT_GRID_MAX_SIZE,
                LADDER_DEFAULT_GRID_CONTENTS_WIDTH, LADDER_DEFAULT_GRID_CONTENTS_HIGHT);
        tabLadder_.getSelectionModel().select(pane.getLadder().getIdx() - 1);
        isChanged_ = true;

        setTitle();
        return true;
    }

    /**
     *
     * @param index
     * @return
     */
    public boolean ladderRemoveTab(int index) {
        ladderCommand_.ladderRemove(index);
        isChanged_ = true;

        setTitle();
        return true;
    }

    /**
     *
     * @param pane
     * @return
     */
    public boolean ladderChangeTabName(LadderPane pane) {
        TextInputDialog alert = new TextInputDialog(pane.getLadder().getName());

        alert.initOwner(stage_);
        alert.initStyle(StageStyle.UTILITY);
        if (!icons_.isEmpty()) {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons_);
        }

        alert.setTitle("Rename");
        alert.setHeaderText(null);
        alert.setContentText(null);
        alert.setGraphic(null);
        Optional<String> result = alert.showAndWait();
        stage_.requestFocus();

        if (result.isPresent()) {
            String name = result.get().trim();
            if (checkTabName(name)) {
                treeTableIo_.getRoot().getChildren().get(pane.getLadder().getIdx()).getValue()
                        .setAddress(name.replace(" ", "_"));
                pane.setChanged(true);
                ladderCommand_.ladderChangeName(pane, name);
                isChanged_ = true;

                setTitle();
                return true;
            } else {
                writeLog("Duplicate names", true);
            }
        }
        return false;
    }

    /**
     *
     * @param idx
     * @param name
     * @return
     */
    public boolean ladderChangeTabName(int idx, String name) {
        name = name.trim();
        if (checkTabName(name)) {
            LadderPane pane = (LadderPane) ((ScrollPane) tabLadder_.getTabs().get(idx - 1).getContent()).getContent();
            treeTableIo_.getRoot().getChildren().get(idx).getValue().setAddress(name.replace(" ", "_"));
            pane.setChanged(true);
            ladderCommand_.ladderChangeName(pane, name);
            isChanged_ = true;

            setTitle();
            return true;
        } else {
            writeLog("Duplicate names", true);
        }
        return false;
    }

    /**
     *
     * @param name
     * @return
     */
    private boolean checkTabName(String name) {
        if (name != null) {
            int index, size;

            name = name.replace(" ", "_");
            for (index = 0, size = tabLadder_.getTabs().size(); index < size; index++) {
                if (((LadderPane) ((ScrollPane) tabLadder_.getTabs().get(index).getContent()).getContent()).getLadder()
                        .getName().equals(name)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Path ladderOpen(TabPane tabPane, TreeTableView<LadderTreeTableIo> treeTableView, Path file) {
        return ladderOpen(tabPane, treeTableView, ioMap_, commentMap_, file);
    }

    private Path ladderOpen(TabPane tabPane, TreeTableView<LadderTreeTableIo> treeTableView,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap, Path file) {
        if (file == null) {
            FileChooser fileChooser = new FileChooser();

            if (filePath_ != null) {
                if (filePath_.getParent() != null) {
                    if (Files.exists(filePath_.getParent())) {
                        fileChooser.setInitialDirectory(filePath_.getParent().toFile());
                    }
                }
            }
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            File fcfile = fileChooser.showOpenDialog(stage_);
            if (fcfile == null) {
                return null;
            }
            file = fcfile.toPath();
        }

        LadderJson ladderJson = ladderJsonOpen(file);
        if (ladderJson != null) {
            allClear();

            // global
            treeTableIo_.getRoot().getChildren().add(LADDER_GLOBAL_ADDRESS_INDEX,
                    new TreeItem<>(new LadderTreeTableIo("Global".replace(" ", "_"), LADDER_GLOBAL_ADDRESS_INDEX)));
            ioMap_.add(LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());
            commentMap_.add(LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());
            scriptIoMap_.add(LADDER_GLOBAL_ADDRESS_INDEX, new ConcurrentHashMap<>());

            if (ladderJsonLoad(this, tabPane, treeTableView, ioMap, commentMap, scriptIoMap_, ladderJson)) {
                return file;
            }
        }
        return null;
    }

    private Path ladderSave() {
        if (filePath_ == null) {
            return ladderSaveAs();
        } else if (Files.exists(filePath_)) {
            if (Files.isRegularFile(filePath_)) {
                return ladderSave(filePath_);
            } else {
                return ladderSaveAs();
            }
        } else {
            return ladderSaveAs();
        }
    }

    private Path ladderSave(Path file) {
        if (file != null) {
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(file), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(
                            new StringReader(gson_.toJson(ladderJsonSave(tabLadder_, ioMap_, commentMap_))))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    outputStreamWriter.write(line);
                    if (bufferedReader.ready()) {
                        outputStreamWriter.write(newLineCharacter_);
                    }
                }

                Tab tab;
                LadderPane pane;
                int index, size;

                for (index = 0, size = tabLadder_.getTabs().size(); index < size; index++) {
                    tab = tabLadder_.getTabs().get(index);
                    pane = (LadderPane) ((ScrollPane) tab.getContent()).getContent();
                    pane.setChanged(false);
                    tab.setText(pane.getLadder().getName());
                }
                return file;
            } catch (IOException ex) {
                Console.writeStackTrace(Ladders.class.getName(), ex);
            }
        }
        return null;
    }

    private Path ladderSaveAs() {
        FileChooser fileChooser = new FileChooser();

        if (filePath_ != null) {
            if (filePath_.getParent() != null) {
                if (Files.exists(filePath_.getParent())) {
                    fileChooser.setInitialDirectory(filePath_.getParent().toFile());
                }
            }
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File fcfile = fileChooser.showSaveDialog(stage_);
        if (fcfile != null) {
            return ladderSave(fcfile.toPath());
        }
        return null;
    }

    private PDPage pdfNewPage(PDDocument doc, PDRectangle rectangle) {
        PDPage page = new PDPage(rectangle);
        doc.addPage(page);
        return page;
    }

    private PDPageContentStream pdfPageBeginGrid(PDDocument doc, PDPage page, PDFont font, float fontSize,
            float lineWidth, float marginTop, float marginLeft) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
                true, true);
        contentStream.setFont(font, fontSize);
        contentStream.setLeading(font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize);
        contentStream.setLineWidth(lineWidth);
        return contentStream;
    }

    private void pdfPageEndGrid(PDPageContentStream contentStream) throws IOException {
        contentStream.close();
    }

    private Path ladderExportPdf(Path file) {
        if (file != null) {
            try (PDDocument doc = new PDDocument();
                    TrueTypeCollection ttc = new TrueTypeCollection(
                        this.getClass().getClassLoader().getResourceAsStream("font/MyricaM.TTC"))) {
                PDRectangle rectangle = PDRectangle.A4;
                PDPage page;
                PDPageContentStream contentStream;
                PDFont font = PDType0Font.load(doc, ttc.getFontByName("MyricaMM"), true);
                float margin = 20f * 25.4f / 72f;
                float fontSize = 7.54f;
                float textWidth, textHeight;
                float gridWidth, gridHeight;
                float fontWidth = font.getFontDescriptor().getFontBoundingBox().getWidth() / 1000f * fontSize;
                float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize;
                float pageShowMinWidth = margin;
                float pageShowMaxWidth = rectangle.getWidth() - margin;
                float pageShowMinHeight = margin;
                float pageShowMaxHeight = rectangle.getHeight() - margin;
                float lineWidth = 1f;
                float contentsSize = font.getFontDescriptor().getFontBoundingBox().getWidth() / 1000f * fontSize * 4f;
                float gridSize = (rectangle.getWidth() - (pageShowMinWidth * 2f) - contentsSize) / 10f;
                float gridSize2 = gridSize / 2f;
                float gridSize3 = gridSize / 3f;
                float gridSize4 = gridSize / 4f;
                float gridSize6 = gridSize / 6f;
                float gridSize12 = gridSize / 12f;

                LadderJson ladderJson = ladderJsonSave(tabLadder_);
                List<LadderJsonLadder> jsonLadders = ladderJson.getLadders();

                String name, address, comment, line, content, c;
                int index, size, index2, size2, column, row, i;

                size = jsonLadders.size();
                List<LadderJsonBlock> jsonBlocks;
                LadderJsonBlock jsonBlock;
                for (index = 0; index < size; index++) {
                    column = 0;
                    row = 0;
                    name = jsonLadders.get(index).getName();
                    page = pdfNewPage(doc, rectangle);
                    gridWidth = pageShowMinWidth + contentsSize;
                    gridHeight = rectangle.getHeight() - pageShowMinHeight - contentsSize;
                    contentStream = pdfPageBeginGrid(doc, page, font, fontSize, lineWidth, pageShowMinWidth,
                            pageShowMinHeight);
                    if (getFileName() == null) {
                        // page header name & title
                        contentStream.beginText();
                        contentStream.newLineAtOffset(
                                ((pageShowMaxWidth - (font.getStringWidth(name) / 1000f * fontSize)) / 2f)
                                        + pageShowMinWidth + fontWidth,
                                pageShowMaxHeight - fontHeight);
                        contentStream.showText(name);
                        contentStream.endText();
                        gridHeight += fontHeight;
                    } else {
                        // page header name
                        contentStream.beginText();
                        contentStream.newLineAtOffset(((pageShowMaxWidth
                                - (font.getStringWidth(name + " [" + getFileName() + "]") / 1000f * fontSize)) / 2f)
                                + pageShowMinWidth + fontWidth, pageShowMaxHeight - fontHeight);
                        contentStream.showText(name + " [" + getFileName() + "]");
                        contentStream.endText();
                        gridHeight += fontHeight;
                    }

                    for (i = 1; i < LADDER_DEFAULT_GRID_COLUMN; i++) {
                        contentStream.beginText();
                        content = String.valueOf(i);
                        contentStream.newLineAtOffset(gridWidth + (gridSize * (i - 1)) + gridSize2
                                + ((font.getStringWidth(content) / 1000f * fontSize) / 2f), gridHeight);
                        contentStream.showText(content);
                        contentStream.endText();
                    }

                    jsonBlocks = jsonLadders.get(index).getBlocks();
                    size2 = jsonBlocks.size();
                    for (index2 = 0; index2 < size2; index2++) {
                        jsonBlock = jsonBlocks.get(index2);
                        if (row < jsonBlock.getRowIndex()) {
                            gridWidth = pageShowMinWidth + contentsSize;
                            column = 0;
                            for (; row < jsonBlock.getRowIndex(); row++) {
                                if (row > 0) {
                                    gridHeight -= gridSize;
                                }
                                if (gridHeight < (pageShowMinHeight + gridSize)) {
                                    pdfPageEndGrid(contentStream);
                                    page = pdfNewPage(doc, rectangle);
                                    contentStream = pdfPageBeginGrid(doc, page, font, fontSize, lineWidth,
                                            pageShowMinWidth, pageShowMinHeight);
                                    gridHeight = rectangle.getHeight() - pageShowMinHeight - contentsSize;

                                    for (i = 1; i < LADDER_DEFAULT_GRID_COLUMN; i++) {
                                        contentStream.beginText();
                                        content = String.valueOf(i);
                                        contentStream.newLineAtOffset(
                                                gridWidth + (gridSize * (i - 1)) + gridSize2
                                                        + ((font.getStringWidth(content) / 1000f * fontSize) / 2f),
                                                gridHeight);
                                        contentStream.showText(content);
                                        contentStream.endText();
                                    }
                                }

                                contentStream.beginText();
                                content = String.valueOf(row + 1);
                                contentStream.newLineAtOffset(gridWidth
                                        + ((-contentsSize + (font.getStringWidth(content) / 1000f * fontSize)) / 2f),
                                        gridHeight - gridSize2);
                                contentStream.showText(content);
                                contentStream.endText();
                            }
                        }
                        if (column < jsonBlock.getColumnIndex()) {
                            if (column == 0) {
                                column = 1;
                            }
                            gridWidth += (jsonBlock.getColumnIndex() - column) * gridSize;
                            column = jsonBlock.getColumnIndex();
                        }

                        address = jsonBlock.getAddress();
                        if (address == null) {
                            comment = jsonBlock.getComment();
                        } else {
                            if (address.startsWith(LADDER_LOCAL_ADDRESS_PREFIX)) {
                                comment = getComment(index + 1, jsonBlock.getAddress());
                            } else {
                                comment = getComment(LADDER_GLOBAL_ADDRESS_INDEX, jsonBlock.getAddress());
                            }
                        }
                        if (jsonBlock.isVertical()) {
                            contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                            contentStream.lineTo(gridWidth, gridHeight);
                        }
                        if (jsonBlock.isVerticalOr()) {
                            contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                            contentStream.lineTo(gridWidth, gridHeight - gridSize);
                        }
                        contentStream.stroke();

                        switch (Ladders.LADDER_BLOCK.valueOf(jsonBlock.getBlock())) {
                            case BLOCK_COMMENT:
                                textWidth = gridWidth;
                                textHeight = gridHeight;

                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                for (i = 0; i < comment.length(); i++) {
                                    c = comment.substring(i, i + 1);
                                    textWidth += font.getStringWidth(c) / 1000 * fontSize;
                                    if (textWidth > (gridWidth + gridSize - fontWidth)) {
                                        textHeight += fontHeight;
                                        if (textHeight > (gridHeight + gridSize - fontHeight)) {
                                            break;
                                        }
                                        contentStream.newLine();
                                        textWidth = gridWidth;
                                    }
                                    contentStream.showText(c);
                                }
                                contentStream.endText();
                                break;
                            case CONNECT_LINE:
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                contentStream.stroke();
                                break;
                            case LOAD:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                contentStream.stroke();
                                break;
                            case LOAD_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|/|-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                break;
                            case LOAD_RISING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|↑|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case LOAD_RISING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|/|-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -|/↑|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case LOAD_FALLING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|↓|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case LOAD_FALLING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|/|-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -|/↓|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                break;
                            case OUT_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/)
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize3, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                break;
                            case OUT_RISING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(↑)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT_RISING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/)
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/↑)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT_FALLING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(↓)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT_FALLING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/)
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/↓)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case COMPARISON_EQUAL:
                            case COMPARISON_NOT_EQUAL:
                            case COMPARISON_LESS:
                            case COMPARISON_LESS_EQUAL:
                            case COMPARISON_GREATER:
                            case COMPARISON_GREATER_EQUAL:
                            case COMPARISON_AND_BITS:
                            case COMPARISON_OR_BITS:
                            case COMPARISON_XOR_BITS:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address);
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LADDER_BLOCK.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case SET:
                            case RESET:
                            case RANDOM:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LADDER_BLOCK.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case NOT_BITS:
                            case TIMER:
                            case COUNTER:
                            case MOVE:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address);
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LADDER_BLOCK.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case TIMER_NOT:
                            case COUNTER_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address);
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -|/|
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LADDER_BLOCK.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case AND_BITS:
                            case OR_BITS:
                            case XOR_BITS:
                            case ADDITION:
                            case SUBTRACTION:
                            case MULTIPLICATION:
                            case DIVISION:
                            case AVERAGE:
                            case SHIFT_LEFT_BITS:
                            case SHIFT_RIGHT_BITS:
                            case SIGMOID:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address);
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value.longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 - gridSize6);
                                if (jsonBlock.getBlockFunctions().get(1).address != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(1).address);
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(1).radix) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(1).value));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(1).value.longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(1).value.longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LADDER_BLOCK.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case SCRIPT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // script
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                contentStream.showText(jsonBlock.getBlockScript());
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LADDER_BLOCK.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            default:
                                break;
                        }
                    }
                    pdfPageEndGrid(contentStream);
                }

                // page footer count
                for (index = 0; index < doc.getNumberOfPages(); index++) {
                    line = (index + 1) + " / " + doc.getNumberOfPages();
                    contentStream = pdfPageBeginGrid(doc, doc.getPage(index), font, fontSize, lineWidth,
                            pageShowMinWidth, pageShowMinHeight);
                    contentStream.beginText();
                    contentStream
                            .newLineAtOffset(((pageShowMaxWidth - (font.getStringWidth(line) / 1000f * fontSize)) / 2f)
                                    + pageShowMinWidth + fontWidth, pageShowMinHeight);
                    contentStream.showText(line);
                    contentStream.endText();
                    pdfPageEndGrid(contentStream);
                }

                doc.save(file.toFile());
                return file;
            } catch (JSException | IOException ex) {
                Console.writeStackTrace(Ladders.class.getName(), ex);
            }
        }
        return null;
    }

    private Path ladderExportPdf() {
        FileChooser fileChooser = new FileChooser();

        if (filePath_ != null) {
            if (filePath_.getParent() != null) {
                if (Files.exists(filePath_.getParent())) {
                    fileChooser.setInitialDirectory(filePath_.getParent().toFile());
                }
            }
            if (!Files.isDirectory(filePath_)) {
                fileChooser.setInitialFileName(JavaLibrary.removeFileExtension(filePath_.getFileName()));
            }
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File fcfile = fileChooser.showSaveDialog(stage_);
        if (fcfile != null) {
            return ladderExportPdf(fcfile.toPath());
        }
        return null;
    }

    /**
     *
     * @param file
     * @return
     */
    public LadderJson ladderJsonOpen(Path file) {
        if (file != null) {
            if (Files.exists(file)) {
                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(file), "UTF-8"))) {
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                    return gson_.fromJson(builder.toString(), LadderJson.class);
                } catch (FileNotFoundException ex) {
                    Console.writeStackTrace(Ladders.class.getName(), ex);
                } catch (JSException | IOException ex) {
                    Console.writeStackTrace(Ladders.class.getName(), ex);
                }
            }
        }
        return null;
    }

    /**
     *
     * @param ladders
     * @param tabPane
     * @param treeTableView
     * @param ioMap
     * @param commentMap
     * @param scriptIoMap
     * @param ladderJson
     * @return
     */
    public boolean ladderJsonLoad(Ladders ladders, TabPane tabPane, TreeTableView<LadderTreeTableIo> treeTableView,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> scriptIoMap, LadderJson ladderJson) {
        ladderCommand_.setDisableHistory(true);
        try {
            // ladder
            if (!ladderCommand_.restoreLadders(ladders, tabPane, treeTableView, ioMap, commentMap, scriptIoMap,
                    ladderJson, false)) {
                return false;
            }

            // comments
            if (!ladderCommand_.restoreComments(tabPane, treeTableView, ioMap, commentMap, ladderJson)) {
                return false;
            }

            // history
            if (ladders != null) {
                if (ladderJson.getHistoryManager() != null) {
                    ladderCommand_.setHistoryManager(ladderJson.getHistoryManager());
                }
            }

            ladderCommand_.setDisableHistory(false);
            return true;
        } catch (Exception ex) {
            Console.writeStackTrace(Ladders.class.getName(), ex);
        }
        ladderCommand_.setDisableHistory(false);
        return false;
    }

    /**
     *
     * @param tabPane
     * @return
     */
    public LadderJson ladderJsonSave(TabPane tabPane) {
        return ladderJsonSave(tabPane, ioMap_, commentMap_);
    }

    private LadderJson ladderJsonSave(TabPane tabPane, CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap) {
        LadderJson ladderJson = new LadderJson();

        // ladder
        ladderCommand_.backupLadders(ladderJson, tabPane);

        // comments
        ladderCommand_.backupComments(ladderJson, ioMap, commentMap);

        // historys
        ladderJson.setHistoryManager(ladderCommand_.getHistoryManager(), -1);

        return ladderJson;
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

            ConcurrentHashMap<String, LadderIo> ioMap = ioMap_.get(idx);
            ioMap_.set(idx, ioMap_.get(idx - 1));
            ioMap_.set(idx - 1, ioMap);

            ConcurrentHashMap<String, String> commentMap = commentMap_.get(idx);
            commentMap_.set(idx, commentMap_.get(idx - 1));
            commentMap_.set(idx - 1, commentMap);

            ConcurrentHashMap<String, LadderIo> scriptIoMap = scriptIoMap_.get(idx);
            scriptIoMap_.set(idx, scriptIoMap_.get(idx - 1));
            scriptIoMap_.set(idx - 1, scriptIoMap);

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

            ConcurrentHashMap<String, LadderIo> ioMap = ioMap_.get(idx);
            ioMap_.set(idx, ioMap_.get(idx + 1));
            ioMap_.set(idx + 1, ioMap);

            ConcurrentHashMap<String, String> commentMap = commentMap_.get(idx);
            commentMap_.set(idx, commentMap_.get(idx + 1));
            commentMap_.set(idx + 1, commentMap);

            ConcurrentHashMap<String, LadderIo> scriptIoMap = scriptIoMap_.get(idx);
            scriptIoMap_.set(idx, scriptIoMap_.get(idx + 1));
            scriptIoMap_.set(idx + 1, scriptIoMap);

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
        int index = tabPane.getSelectionModel().getSelectedIndex();
        int size = tabPane.getTabs().size();

        if (index < (size - 1)) {
            ladderCommand_.ladderChangeSelect(index + 1);
        } else {
            ladderCommand_.ladderChangeSelect(0);
        }
    }

    /**
     *
     * @param tabPane
     */
    public void ladderChangeSelectPrevious(TabPane tabPane) {
        int index = tabLadder_.getSelectionModel().getSelectedIndex();
        int size = tabLadder_.getTabs().size();

        if (index > 0) {
            ladderCommand_.ladderChangeSelect(index - 1);
        } else {
            ladderCommand_.ladderChangeSelect(size - 1);
        }
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
        Tab tab = tabLadder_.getSelectionModel().getSelectedItem();
        if (tab != null) {
            switch (event.getCode()) {
                case TAB:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladderChangeSelectNext(tabLadder_);
                        return true;
                    } else if (event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladderChangeSelectPrevious(tabLadder_);
                        return true;
                    }
                    break;
                case F2:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladderChangeTabName(pane);
                        return true;
                    }
                    break;
                case F4:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        if (!isCycling_) {
                            ladderRemoveTab(tabLadder_.getSelectionModel().getSelectedIndex());
                            return true;
                        }
                        return false;
                    }
                    break;
                case LEFT:
                    if (!event.isShiftDown() && !event.isShortcutDown() && event.isAltDown()) {
                        if (!isCycling_) {
                            ladderMoveLeft(pane.getLadder());
                            return true;
                        }
                        return false;
                    }
                    break;
                case RIGHT:
                    if (!event.isShiftDown() && !event.isShortcutDown() && event.isAltDown()) {
                        if (!isCycling_) {
                            ladderMoveRight(pane.getLadder());
                            return true;
                        }
                        return false;
                    }
                    break;
                case N:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        fileNew();
                        return true;
                    }
                    break;
                case O:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        if (fileOpen(null)) {
                            ladderController_.addRecentFile(getFilePath().toString());
                        }
                        return true;
                    }
                    break;
                case S:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        if (fileSave()) {
                            ladderController_.addRecentFile(getFilePath().toString());
                        }
                        return true;
                    } else if (!event.isShiftDown() && event.isShortcutDown() && event.isAltDown()) {
                        if (fileSaveAs()) {
                            ladderController_.addRecentFile(getFilePath().toString());
                        }
                        return true;
                    }
                    break;
                case T:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        ladderNewTab();
                        return true;
                    }
                    break;
                case Y:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        redo(tab, pane);
                        return true;
                    }
                    break;
                case Z:
                    if (!event.isShiftDown() && event.isShortcutDown() && !event.isAltDown()) {
                        undo(tab, pane);
                        return true;
                    }
                    break;
                default:
                    break;
            }

            if (pane.onKeyPressed(event, scrollPane)) {
                if (pane.isChanged()) {
                    isChanged_ = true;

                    tab.setText(pane.getLadder().getName() + " *");
                    setTitle();
                    return true;
                }
            }
        }
        return false;
    }

    private void setTitle() {
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

        filePath_ = ladderPath;
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
        return new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Map.Entry<String, LadderIo> entry;
                    LadderRegisterSoemIo registerSoemIo;
                    Long registerSoemValue;
                    long cycleTime, minCycleTime, maxCycleTime, cumulativeCycleTime, cumulativeCycleTimeCount, nanoTime,
                            nanoTimeOld, waitTime;
                    int index, size;

                    ladderController_.runViewRunning(true);

                    cycleTime = 0;
                    minCycleTime = Long.MAX_VALUE;
                    maxCycleTime = 0;
                    cumulativeCycleTime = 0;
                    cumulativeCycleTimeCount = 0;

                    nanoTimeOld = System.nanoTime();
                    while (isCycling_) {
                        synchronized (lock_) {
                            // soem
                            if (soem_ != null) {
                                for (index = 0, size = registerSoemOut_.size(); index < size; index++) {
                                    registerSoemIo = registerSoemOut_.get(index);
                                    soem_.out(registerSoemIo.getSlave(), registerSoemIo.getBitsOffset(),
                                            registerSoemIo.getBitsMask(), (long) ioMap_.get(LADDER_GLOBAL_ADDRESS_INDEX)
                                                    .get(registerSoemIo.getAddress()).getValue());
                                }
                                for (index = 0, size = registerSoemIn_.size(); index < size; index++) {
                                    registerSoemIo = registerSoemIn_.get(index);
                                    registerSoemValue = soem_.in(registerSoemIo.getSlave(),
                                            registerSoemIo.getBitsOffset(), registerSoemIo.getBitsMask());
                                    if (registerSoemValue != null) {
                                        ioMap_.get(LADDER_GLOBAL_ADDRESS_INDEX).get(registerSoemIo.getAddress())
                                                .setValue(registerSoemValue);
                                    }
                                }
                            }

                            // script io
                            for (index = 0; index < scriptIoMap_.size(); index++) {
                                if (!scriptIoMap_.get(index).isEmpty()) {
                                    for (Iterator<Map.Entry<String, LadderIo>> iterator = scriptIoMap_.get(index)
                                            .entrySet().iterator(); iterator.hasNext();) {
                                        entry = iterator.next();
                                        if (entry.getValue().isCycled()) {
                                            ioMap_.get(index).get(entry.getKey()).chehckEdge();
                                            iterator.remove();
                                        } else {
                                            ioMap_.get(index).get(entry.getKey()).setValue(entry.getValue().getValue());
                                            entry.getValue().setCycled(true);
                                        }
                                    }
                                }
                            }

                            // ladder
                            for (index = 0; index < laddersSize_; index++) {
                                ladders_[index].run(ioMap_, cycleTime);
                            }

                            // refresh view
                            if ((LADDER_VIEW_REFRESH_CYCLE_TIME - cumulativeCycleTime) < 0) {
                                ladderController_.refreshLadder(cumulativeCycleTime, cumulativeCycleTimeCount);
                                cumulativeCycleTime = 0;
                                cumulativeCycleTimeCount = 0;
                            }
                        }

                        // ideal cycletime
                        waitTime = idealCycleTime_ - cycleTime;
                        if (waitTime > 0) {
                            try {
                                TimeUnit.NANOSECONDS.sleep(waitTime);
                            } catch (InterruptedException ex) {
                            }
                        }

                        // cycletimme
                        nanoTime = System.nanoTime();
                        cycleTime = nanoTime - nanoTimeOld;
                        if (cycleTime > 0) {
                            if ((cycleTime - minCycleTime) < 0) {
                                minCycleTime = cycleTime;
                                ladderController_.setMinCycleTime(minCycleTime);
                            } else if ((cycleTime - maxCycleTime) > 0) {
                                maxCycleTime = cycleTime;
                                ladderController_.setMaxCycleTime(maxCycleTime);
                            }
                            cumulativeCycleTime += cycleTime;
                            cumulativeCycleTimeCount++;
                            nanoTimeOld = nanoTime;
                        }
                    }
                } catch (Exception ex) {
                    Console.writeStackTrace(Ladders.class.getName(), ex);
                }
                ladderController_.runViewRunning(false);
                isCycling_ = false;
                return null;
            }
        };
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

    public Boolean isRegistered(Object object) {
        if (object != null) {
            if (object instanceof WebEngine) {
                if (webEngine_ != null) {
                    return webEngine_.equals(object);
                }
            }
            return false;
        }
        return null;
    }

    public void register(WebEngine webEngine, Worker.State state) {
        if ((webEngine != null) && (state != null)) {
            webEngine_ = webEngine;
            state_ = state;
            for (int idx = 0; idx < ioMap_.size(); idx++) {
                ioMap_.get(idx).entrySet().forEach((entry) -> {
                    entry.getValue().setWebEngineState(webEngine_, state_);
                });
            }
        }
    }

    public Boolean registerIn(Object... objects) {
        if (objects != null) {
            switch (objects.length) {
                case 5:
                    if ((objects[0] instanceof Soem) && (objects[1] instanceof String)
                            && (objects[2] instanceof Integer) && (objects[3] instanceof Long)
                            && (objects[4] instanceof Long)) {
                        if (soem_ == null) {
                            soem_ = (Soem) objects[0];
                            if (registerSoemIn_ != null) {
                                registerSoemIn_.clear();
                            }
                            if (registerSoemOut_ != null) {
                                registerSoemOut_.clear();
                            }
                        }

                        String address = ((String) objects[1]).trim();
                        if (!address.startsWith(LADDER_LOCAL_ADDRESS_PREFIX) && !address.contains(" ")) {
                            if (!ioMap_.get(LADDER_GLOBAL_ADDRESS_INDEX).containsKey(address)) {
                                ioMap_.get(LADDER_GLOBAL_ADDRESS_INDEX).put(address, new LadderIo(address));
                                treeTableIo_.getRoot().getChildren().get(LADDER_GLOBAL_ADDRESS_INDEX).getChildren()
                                        .add(new TreeItem<>(new LadderTreeTableIo(address)));
                            }

                            int slave = (int) objects[2];
                            long bitsOffset = (long) objects[3];
                            long bitsMask = (long) objects[4];
                            for (int index = 0, size = registerSoemIn_.size(); index < size; index++) {
                                if (registerSoemIn_.get(index).getAddress().equals(address)) {
                                    if ((registerSoemIn_.get(index).getSlave() != slave)
                                            || (registerSoemIn_.get(index).getBitsOffset() != bitsOffset)
                                            || (registerSoemIn_.get(index).getBitsMask() != bitsMask)) {
                                        registerSoemIn_.remove(index);
                                        registerSoemIn_
                                                .add(new LadderRegisterSoemIo(address, slave, bitsOffset, bitsMask));
                                    }
                                    return true;
                                }
                            }
                            registerSoemIn_.add(new LadderRegisterSoemIo(address, slave, bitsOffset, bitsMask));
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }
        return null;
    }

    public Boolean registerOut(Object... objects) {
        if (objects != null) {
            switch (objects.length) {
                case 5:
                    if ((objects[0] instanceof Soem) && (objects[1] instanceof String)
                            && (objects[2] instanceof Integer) && (objects[3] instanceof Long)
                            && (objects[4] instanceof Long)) {
                        if (soem_ == null) {
                            soem_ = (Soem) objects[0];
                            if (registerSoemIn_ != null) {
                                registerSoemIn_.clear();
                            }
                            if (registerSoemOut_ != null) {
                                registerSoemOut_.clear();
                            }
                        }

                        String address = ((String) objects[1]).trim();
                        if (!address.startsWith(LADDER_LOCAL_ADDRESS_PREFIX) && !address.contains(" ")) {
                            if (!ioMap_.get(LADDER_GLOBAL_ADDRESS_INDEX).containsKey(address)) {
                                ioMap_.get(LADDER_GLOBAL_ADDRESS_INDEX).put(address, new LadderIo(address));
                                treeTableIo_.getRoot().getChildren().get(LADDER_GLOBAL_ADDRESS_INDEX).getChildren()
                                        .add(new TreeItem<>(new LadderTreeTableIo(address)));
                            }

                            int slave = (int) objects[2];
                            long bitsOffset = (long) objects[3];
                            long bitsMask = (long) objects[4];
                            for (int index = 0, size = registerSoemOut_.size(); index < size; index++) {
                                if (registerSoemOut_.get(index).getAddress().equals(address)) {
                                    if ((registerSoemOut_.get(index).getSlave() != slave)
                                            || (registerSoemOut_.get(index).getBitsOffset() != bitsOffset)
                                            || (registerSoemOut_.get(index).getBitsMask() != bitsMask)) {
                                        registerSoemOut_.remove(index);
                                        registerSoemOut_
                                                .add(new LadderRegisterSoemIo(address, slave, bitsOffset, bitsMask));
                                    }
                                    return true;
                                }
                            }
                            registerSoemOut_.add(new LadderRegisterSoemIo(address, (int) objects[2], (long) objects[3],
                                    (long) objects[4]));
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }
        return null;
    }

    public void unregister(Object object) {
        if (object != null) {
            if (object instanceof WebEngine) {
                webEngine_ = null;
                state_ = Worker.State.READY;
            } else if (object instanceof Soem) {
                if (registerSoemIn_ != null) {
                    registerSoemIn_.clear();
                }
                if (registerSoemOut_ != null) {
                    registerSoemOut_.clear();
                }
                soem_ = null;
            }
        }
    }

    public Double getValue(String address) {
        if (!address.startsWith(LADDER_LOCAL_ADDRESS_PREFIX)) {
            return getValue(LADDER_GLOBAL_ADDRESS_INDEX, address);
        }
        return null;
    }

    /**
     *
     * @param idx
     * @param address
     * @return
     */
    public Double getValue(int idx, String address) {
        if (ioMap_.get(idx).containsKey(address)) {
            return ioMap_.get(idx).get(address).getValue();
        }
        return null;
    }

    public void setValue(String address, double value) {
        if (!address.startsWith(LADDER_LOCAL_ADDRESS_PREFIX)) {
            setValue(LADDER_GLOBAL_ADDRESS_INDEX, address, value);
        }
    }

    /**
     *
     * @param idx
     * @param address
     * @param value
     */
    public void setValue(int idx, String address, double value) {
        if (!ioMap_.get(idx).containsKey(address)) {
            ioMap_.get(idx).put(address, new LadderIo(address));
            treeTableIo_.getRoot().getChildren().get(idx).getChildren()
                    .add(new TreeItem<>(new LadderTreeTableIo(address, value)));
        }

        ovScript_ = treeTableIo_.getRoot().getChildren().get(idx).getChildren();
        for (scriptIndex_ = 0, scriptSize_ = ovScript_.size(); scriptIndex_ < scriptSize_; scriptIndex_++) {
            if (address.equals(ovScript_.get(scriptIndex_).getValue().getAddress())) {
                ovScript_.get(scriptIndex_).getValue().setValue(value);
                break;
            }
        }

        synchronized (lock_) {
            scriptIoMap_.get(idx).putIfAbsent(address, new LadderIo(address));
            scriptIoMap_.get(idx).get(address).setValue(value);
            scriptIoMap_.get(idx).get(address).setCycled(false);
        }
    }

    public boolean connectLadder() {
        LadderPane pane;
        int index;

        // check connect
        if (checkConnectLadder(tabLadder_, treeTableIo_, ioMap_)) {
            synchronized (lock_) {
                // connect
                laddersSize_ = tabLadder_.getTabs().size();
                ladders_ = new Ladder[laddersSize_];
                for (index = 0; index < laddersSize_; index++) {
                    pane = (LadderPane) ((ScrollPane) tabLadder_.getTabs().get(index).getContent()).getContent();
                    ladders_[index] = pane.getLadder().copy();
                    ladders_[index].connectLadder(ioMap_, treeTableIo_);
                }
            }
            register(webEngine_, state_);
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

    public boolean runStartLadder() {
        if (!isCycling_ && (ladders_ != null) && (!ioMap_.isEmpty())) {
            isCycling_ = true;
            if (Platform.isFxApplicationThread()) {
                startLadder();
            } else {
                Platform.runLater(() -> {
                    startLadder();
                });
            }
            return true;
        }
        return false;
    }

    private void startLadder() {
        if (getState() == State.READY) {
            start();
        } else {
            restart();
        }
    }

    public void stopLadder() {
        if (isCycling_) {
            isCycling_ = false;
            try {
                TimeUnit.NANOSECONDS.sleep(idealCycleTime_ + LADDER_VIEW_REFRESH_CYCLE_TIME);
            } catch (InterruptedException ex) {
            }
        }
    }

    private void fileNotFound(Path file) {
        if (file != null) {
            writeLog("File Not Found" + ": " + file.toString(), true);
        } else {
            writeLog("File Not Found", true);
        }
    }

    /**
     *
     * @return
     */
    public ButtonType fileSavedCheck() {
        ButtonType response = null;

        if (isChanged_) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            if (!icons_.isEmpty()) {
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons_);
            }
            alert.setTitle("Ladder");
            alert.getDialogPane().setHeaderText(null);
            alert.getDialogPane().setContentText("Save Changes Program?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            response = result.get();
        }
        return response;
    }

    public boolean fileNew() {
        ButtonType response = fileSavedCheck();

        if (response == null) {
            ladderNew();
            filePath_ = null;
            isChanged_ = false;
            setTitle();
            return true;
        } else if (response == ButtonType.YES) {
            ladderSave();
            ladderNew();
            filePath_ = null;
            isChanged_ = false;
            setTitle();
            return true;
        } else if (response == ButtonType.NO) {
            ladderNew();
            filePath_ = null;
            isChanged_ = false;
            setTitle();
            return true;
        }
        return false;
    }

    public boolean fileOpen(Path filePath) {
        Path file;
        ButtonType response = fileSavedCheck();

        if (response == null) {
            file = ladderOpen(tabLadder_, treeTableIo_, filePath);
            if (file == null) {
                if (filePath != null) {
                    fileNotFound(filePath);
                }
            } else {
                filePath_ = file;
                isChanged_ = false;
                setTitle();
                return true;
            }
        } else if (response == ButtonType.YES) {
            ladderSave();
            file = ladderOpen(tabLadder_, treeTableIo_, filePath);
            if (file == null) {
                if (filePath != null) {
                    fileNotFound(filePath);
                }
            } else {
                filePath_ = file;
                isChanged_ = false;
                setTitle();
                return true;
            }
        } else if (response == ButtonType.NO) {
            file = ladderOpen(tabLadder_, treeTableIo_, filePath);
            if (file == null) {
                if (filePath != null) {
                    fileNotFound(filePath);
                }
            } else {
                filePath_ = file;
                isChanged_ = false;
                setTitle();
                return true;
            }
        }
        return false;
    }

    public boolean fileSave() {
        Path file = ladderSave();

        if (file != null) {
            filePath_ = file;
            isChanged_ = false;
            setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public boolean fileSaveAs() {
        Path file = ladderSaveAs();

        if (file != null) {
            filePath_ = file;
            isChanged_ = false;
            setTitle();
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public boolean exportPdf() {
        Path file = ladderExportPdf();
        return file != null;
    }

    public String getFileName() {
        if (filePath_ == null) {
            return null;
        }
        if (Files.isDirectory(filePath_)) {
            return null;
        }
        return filePath_.getFileName().toString();
    }

    /**
     *
     * @return
     */
    public Path getFilePath() {
        return filePath_;
    }

    private void writeLog(final String msg, final boolean err) {
        Console.write(Ladders.class.getName(), msg, err);
    }
}
