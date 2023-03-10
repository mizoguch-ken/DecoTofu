/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.concurrent.Worker;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ken.mizoguch.soem.Soem;
import ken.mizoguch.webviewer.plugin.WebViewerPlugin;

/**
 *
 * @author mizoguch-ken
 */
public class LaddersPlugin implements WebViewerPlugin {

    private WebViewerPlugin webViewer_;
    private static final String FUNCTION_NAME = "ladders";

    private WebEngine webEngine_;
    private Worker.State state_;

    private final Soem soem_;
    private final Ladders ladders_;
    private final javafx.stage.Stage stage_;

    /**
     *
     * @param soem
     * @param ladders
     */
    public LaddersPlugin(Soem soem, Ladders ladders) {
        webEngine_ = null;
        state_ = Worker.State.READY;
        soem_ = soem;
        ladders_ = ladders;
        stage_ = ladders.getDesignController().getStage();
    }

    /**
     *
     * @return
     */
    public Boolean isShowing() {
        return stage_.isShowing();
    }

    /**
     *
     */
    public void show() {
        stage_.show();
    }

    /**
     *
     */
    public void hide() {
        stage_.hide();
    }

    /**
     *
     * @return
     */
    public Boolean isFocused() {
        return stage_.isFocused();
    }

    /**
     *
     */
    public void requestFocus() {
        stage_.requestFocus();
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return stage_.getTitle();
    }

    /**
     *
     * @return
     */
    public Double getWidth() {
        return stage_.getWidth();
    }

    /**
     *
     * @param value
     */
    public void setWidth(double value) {
        stage_.setWidth(value);
    }

    /**
     *
     * @return
     */
    public Double getMinWidth() {
        return stage_.getMinWidth();
    }

    /**
     *
     * @param value
     */
    public void setMinWidth(double value) {
        stage_.setMinWidth(value);
    }

    /**
     *
     * @return
     */
    public Double getMaxWidth() {
        return stage_.getMaxWidth();
    }

    /**
     *
     * @param value
     */
    public void setMaxWidth(double value) {
        stage_.setMaxWidth(value);
    }

    /**
     *
     * @return
     */
    public Double getHeight() {
        return stage_.getHeight();
    }

    /**
     *
     * @param value
     */
    public void setHeight(double value) {
        stage_.setHeight(value);
    }

    /**
     *
     * @return
     */
    public Double getMinHeight() {
        return stage_.getMinHeight();
    }

    /**
     *
     * @param value
     */
    public void setMinHeight(double value) {
        stage_.setMinHeight(value);
    }

    /**
     *
     * @return
     */
    public Double getMaxHeight() {
        return stage_.getMaxHeight();
    }

    /**
     *
     * @param value
     */
    public void setMaxHeight(double value) {
        stage_.setMaxHeight(value);
    }

    /**
     *
     * @return
     */
    public Double getOpacity() {
        return stage_.getOpacity();
    }

    /**
     *
     * @param value
     */
    public void setOpacity(double value) {
        stage_.setOpacity(value);
    }

    /**
     *
     * @return
     */
    public Boolean isAlwaysOnTop() {
        return stage_.isAlwaysOnTop();
    }

    /**
     *
     * @param state
     */
    public void setAlwaysOnTop(boolean state) {
        stage_.setAlwaysOnTop(state);
    }

    /**
     *
     * @return
     */
    public Boolean isMaximized() {
        return stage_.isMaximized();
    }

    /**
     *
     * @param state
     */
    public void setMaximized(boolean state) {
        stage_.setMaximized(state);
    }

    /**
     *
     * @return
     */
    public Double getX() {
        return stage_.getX();
    }

    /**
     *
     * @param value
     */
    public void setX(double value) {
        stage_.setX(value);
    }

    /**
     *
     * @return
     */
    public Double getY() {
        return stage_.getY();
    }

    /**
     *
     * @param value
     */
    public void setY(double value) {
        stage_.setY(value);
    }

    /**
     *
     * @return
     */
    public Integer getHistoryGeneration() {
        return ladders_.getHistoryGeneration();
    }

    /**
     *
     * @param historyGeneration
     */
    public void setHistoryGeneration(int historyGeneration) {
        ladders_.setHistoryGeneration(historyGeneration);
    }

    /**
     *
     * @return
     */
    public Long getIdealCycleTime() {
        return ladders_.getIdealCycleTime();
    }

    /**
     *
     * @param idealCycleTime
     */
    public void setIdealCycleTime(long idealCycleTime) {
        ladders_.setIdealCycleTime(idealCycleTime);
    }

    /**
     *
     */
    public void registerWeb() {
        ladders_.register(webEngine_, state_);
    }

    /**
     *
     */
    public void unregisterWeb() {
        ladders_.unregister(webEngine_);
    }

    /**
     *
     * @param address
     * @param slave
     * @param bitsOffset
     * @param bitsMask
     * @return
     */
    public Boolean registerSoemIn(String address, int slave, long bitsOffset, long bitsMask) {
        return ladders_.registerIn(soem_, address, slave, bitsOffset, bitsMask);
    }

    /**
     *
     * @param address
     * @param slave
     * @param bitsOffset
     * @param bitsMask
     * @return
     */
    public Boolean registerSoemOut(String address, int slave, long bitsOffset, long bitsMask) {
        return ladders_.registerOut(soem_, address, slave, bitsOffset, bitsMask);
    }

    /**
     *
     */
    public void unregisterSoem() {
        ladders_.unregister(soem_);
    }

    /**
     *
     * @param address
     * @return
     */
    public Double getValue(String address) {
        return ladders_.getValue(address);
    }

    /**
     *
     * @param address
     * @param value
     */
    public void setValue(String address, double value) {
        ladders_.setValue(address, value);
    }

    /**
     *
     * @return
     */
    public Boolean connect() {
        return ladders_.connectLadder();
    }

    /**
     *
     * @return
     */
    public Boolean run() {
        return ladders_.runStartLadder();
    }

    /**
     *
     */
    public void stop() {
        ladders_.stopLadder();
    }

    /**
     *
     * @return
     */
    public Boolean fileNew() {
        return ladders_.fileNew();
    }

    /**
     *
     * @param path
     * @return
     */
    public Boolean fileOpen(String path) {
        return ladders_.fileOpen(Paths.get(path));
    }

    /**
     *
     * @return
     */
    public Boolean fileSave() {
        return ladders_.fileSave();
    }

    /**
     *
     * @return
     */
    public String getFileName() {
        return ladders_.getFileName();
    }

    @Override
    public void initialize(WebViewerPlugin webViewer) {
        webViewer_ = webViewer;
        webEngine_ = webViewer_.webEngine();
    }

    @Override
    public String functionName() {
        return FUNCTION_NAME;
    }

    @Override
    public void state(Worker.State state) {
        state_ = state;
        if (ladders_ != null) {
            if (ladders_.isRegistered(webEngine_)) {
                ladders_.register(webEngine_, state_);
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public Stage stage() {
        return webViewer_.stage();
    }

    @Override
    public List<Image> icons() {
        return webViewer_.icons();
    }

    @Override
    public WebEngine webEngine() {
        return webViewer_.webEngine();
    }

    @Override
    public Path webPath() {
        return webViewer_.webPath();
    }

    @Override
    public void writeStackTrace(String name, Throwable throwable) {
        webViewer_.writeStackTrace(name, throwable);
    }

    @Override
    public void write(String name, String msg, boolean err) {
        webViewer_.write(name, msg, err);
    }
}
