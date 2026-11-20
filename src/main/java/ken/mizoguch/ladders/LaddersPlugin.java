/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.nio.file.Path;
import java.util.List;
import javafx.concurrent.Worker;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ken.mizoguch.console.Console;
import ken.mizoguch.decotofu.PathResolver;
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
        return (stage_ != null) ? stage_.isShowing() : null;
    }

    /**
     *
     */
    public void show() {
        if (stage_ != null) {
            stage_.show();
        }
    }

    /**
     *
     */
    public void hide() {
        if (stage_ != null) {
            stage_.hide();
        }
    }

    /**
     *
     * @return
     */
    public Boolean isFocused() {
        return (stage_ != null) ? stage_.isFocused() : null;
    }

    /**
     *
     */
    public void requestFocus() {
        if (stage_ != null) {
            stage_.requestFocus();
        }
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return (stage_ != null) ? stage_.getTitle() : null;
    }

    public void setTitle(String title) {
        if (stage_ != null) {
            stage_.setTitle(title);
        }
    }

    /**
     *
     * @return
     */
    public Double getWidth() {
        return (stage_ != null) ? stage_.getWidth() : null;
    }

    /**
     *
     * @param value
     */
    public void setWidth(double value) {
        if (stage_ != null) {
            stage_.setWidth(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMinWidth() {
        return (stage_ != null) ? stage_.getMinWidth() : null;
    }

    /**
     *
     * @param value
     */
    public void setMinWidth(double value) {
        if (stage_ != null) {
            stage_.setMinWidth(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMaxWidth() {
        return (stage_ != null) ? stage_.getMaxWidth() : null;
    }

    /**
     *
     * @param value
     */
    public void setMaxWidth(double value) {
        if (stage_ != null) {
            stage_.setMaxWidth(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getHeight() {
        return (stage_ != null) ? stage_.getHeight() : null;
    }

    /**
     *
     * @param value
     */
    public void setHeight(double value) {
        if (stage_ != null) {
            stage_.setHeight(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMinHeight() {
        return (stage_ != null) ? stage_.getMinHeight() : null;
    }

    /**
     *
     * @param value
     */
    public void setMinHeight(double value) {
        if (stage_ != null) {
            stage_.setMinHeight(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMaxHeight() {
        return (stage_ != null) ? stage_.getMaxHeight() : null;
    }

    /**
     *
     * @param value
     */
    public void setMaxHeight(double value) {
        if (stage_ != null) {
            stage_.setMaxHeight(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getOpacity() {
        return (stage_ != null) ? stage_.getOpacity() : null;
    }

    /**
     *
     * @param value
     */
    public void setOpacity(double value) {
        if (stage_ != null) {
            stage_.setOpacity(value);
        }
    }

    /**
     *
     * @return
     */
    public Boolean isAlwaysOnTop() {
        return (stage_ != null) ? stage_.isAlwaysOnTop() : null;
    }

    /**
     *
     * @param state
     */
    public void setAlwaysOnTop(boolean state) {
        if (stage_ != null) {
            stage_.setAlwaysOnTop(state);
        }
    }

    /**
     *
     * @return
     */
    public Boolean isMaximized() {
        return (stage_ != null) ? stage_.isMaximized() : null;
    }

    /**
     *
     * @param state
     */
    public void setMaximized(boolean state) {
        if (stage_ != null) {
            stage_.setMaximized(state);
        }
    }

    /**
     *
     * @return
     */
    public Double getX() {
        return (stage_ != null) ? stage_.getX() : null;
    }

    /**
     *
     * @param value
     */
    public void setX(double value) {
        if (stage_ != null) {
            stage_.setX(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getY() {
        return (stage_ != null) ? stage_.getY() : null;
    }

    /**
     *
     * @param value
     */
    public void setY(double value) {
        if (stage_ != null) {
            stage_.setY(value);
        }
    }

    public void toBack() {
        if (stage_ != null) {
            stage_.toBack();
        }
    }

    public void toFront() {
        if (stage_ != null) {
            stage_.toFront();
        }
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

    public Long getCycleTime() {
        return ladders_.getCycleTime();
    }

    public Long getMinCycleTime() {
        return ladders_.getMinCycleTime();
    }

    public Long getMaxCycleTime() {
        return ladders_.getMaxCycleTime();
    }

    public Boolean isRunning() {
        return ladders_.isCycling();
    }

    public Boolean isChanged() {
        return ladders_.isChanged();
    }

    public Boolean isScriptEnable() {
        return ladders_.isScriptEnable();
    }

    public void setScriptEnable(boolean enable) {
        ladders_.setScriptEnable(enable);
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

    public String getComment(String address) {
        return ladders_.getComment(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, address);
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
     * open a ladder file : a relative path is used from the current directory when the
     * file is there, otherwise it is used from the directory of the application because
     * the launcher does not always set the current directory
     *
     * @param path
     * @return
     */
    public Boolean fileOpen(String path) {
        Path filePath = PathResolver.resolvePagePath(LaddersPlugin.class, path);

        if (filePath == null) {
            write(FUNCTION_NAME, "incorrect file path [" + path + "]", true);
            return false;
        }
        return ladders_.fileOpen(filePath);
    }

    /**
     *
     * @return
     */
    public Boolean fileSave() {
        return ladders_.fileSave();
    }

    public Boolean fileSaveAs() {
        return ladders_.fileSaveAs();
    }

    public Boolean exportPdf() {
        return ladders_.exportPdf();
    }

    /**
     *
     * @return
     */
    public String getFileName() {
        return ladders_.getFileName();
    }

    public String getFilePath() {
        Path filePath = ladders_.getFilePath();
        return (filePath != null) ? filePath.toString() : null;
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
            if (Boolean.TRUE.equals(ladders_.isRegistered(webEngine_))) {
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
        if (webViewer_ != null) {
            webViewer_.writeStackTrace(name, throwable);
        } else {
            // the plugin is not attached to a web page : the console of the application
            // answers the message
            Console.writeStackTrace(name, throwable);
        }
    }

    @Override
    public void write(String name, String msg, boolean err) {
        if (webViewer_ != null) {
            webViewer_.write(name, msg, err);
        } else {
            Console.write(name, msg, err);
        }
    }
}
