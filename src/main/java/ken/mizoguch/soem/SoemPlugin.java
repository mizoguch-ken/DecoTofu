/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ken.mizoguch.console.Console;
import ken.mizoguch.webviewer.plugin.WebViewerPlugin;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author mizoguch-ken
 */
public class SoemPlugin implements WebViewerPlugin, SoemPluginListener {

    private WebViewerPlugin webViewer_;
    private static final String FUNCTION_NAME = "soem";

    private WebEngine webEngine_;
    private Worker.State state_;

    private final Soem soem_;

    private String funcErrorSafeOpError_, funcErrorLost_, funcWarningSafeOp_,
            funcMessageReconfigured_, funcMessageRecovered_, funcMessageFound_, funcMessageAllSlavesResumedOperational_;

    /**
     *
     * @param soem
     */
    public SoemPlugin(Soem soem) {
        webEngine_ = null;
        state_ = Worker.State.READY;
        soem_ = soem;
        funcErrorSafeOpError_ = null;
        funcErrorLost_ = null;
        funcWarningSafeOp_ = null;
        funcMessageReconfigured_ = null;
        funcMessageRecovered_ = null;
        funcMessageFound_ = null;
        funcMessageAllSlavesResumedOperational_ = null;
    }

    /**
     *
     * @param state
     */
    public void setState(Worker.State state) {
        state_ = state;
    }

    /**
     *
     * @return
     */
    public Soem getSoem() {
        return soem_;
    }

    /**
     *
     * @param libraryPath
     * @return
     */
    public Boolean load(String libraryPath) {
        return soem_.load(Paths.get(libraryPath));
    }

    /**
     *
     * @param size
     * @return
     */
    public Boolean init(int size) {
        Boolean ret = soem_.init(size);
        if (ret != null) {
            return ret;
        }
        return null;
    }

    /**
     *
     * @param slave
     * @param eep_man
     * @param eep_id
     * @param func
     * @return
     */
    public Boolean po2so(int slave, long eep_man, long eep_id, String func) {
        Callable<Integer> fnc = () -> {
            if (func != null) {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        return (Integer) webEngine_.executeScript(func + "(" + slave + ");");
                    } catch (JSException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            }
            return 0;
        };
        return soem_.po2so(slave, eep_man, eep_id, fnc);
    }

    /**
     *
     * @param ifname
     * @param cycletime
     * @return
     */
    public Boolean start(String ifname, long cycletime) {
        return soem_.start(ifname, null, cycletime, this);
    }

    /**
     *
     * @param ifname
     * @param ifname2
     * @param cycletime
     * @return
     */
    public Boolean start_redundant(String ifname, String ifname2, long cycletime) {
        return soem_.start(ifname, ifname2, cycletime, this);
    }

    /**
     *
     * @param func
     */
    public void setNotifyErrorSafeOpError(String func) {
        funcErrorSafeOpError_ = func;
        if ((funcErrorSafeOpError_ == null) && (funcErrorLost_ == null) && (funcWarningSafeOp_ == null)
                && (funcMessageReconfigured_ == null) && (funcMessageRecovered_ == null) && (funcMessageFound_ == null)
                && (funcMessageAllSlavesResumedOperational_ == null)) {
            soem_.setNotifyCheck(false);
        } else {
            soem_.setNotifyCheck(true);
        }
    }

    /**
     *
     * @param func
     */
    public void setNotifyErrorLost(String func) {
        funcErrorLost_ = func;
        if ((funcErrorSafeOpError_ == null) && (funcErrorLost_ == null) && (funcWarningSafeOp_ == null)
                && (funcMessageReconfigured_ == null) && (funcMessageRecovered_ == null) && (funcMessageFound_ == null)
                && (funcMessageAllSlavesResumedOperational_ == null)) {
            soem_.setNotifyCheck(false);
        } else {
            soem_.setNotifyCheck(true);
        }
    }

    /**
     *
     * @param func
     */
    public void setNotifyWarningSafeOp(String func) {
        funcWarningSafeOp_ = func;
        if ((funcErrorSafeOpError_ == null) && (funcErrorLost_ == null) && (funcWarningSafeOp_ == null)
                && (funcMessageReconfigured_ == null) && (funcMessageRecovered_ == null) && (funcMessageFound_ == null)
                && (funcMessageAllSlavesResumedOperational_ == null)) {
            soem_.setNotifyCheck(false);
        } else {
            soem_.setNotifyCheck(true);
        }
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageReconfigured(String func) {
        funcMessageReconfigured_ = func;
        if ((funcErrorSafeOpError_ == null) && (funcErrorLost_ == null) && (funcWarningSafeOp_ == null)
                && (funcMessageReconfigured_ == null) && (funcMessageRecovered_ == null) && (funcMessageFound_ == null)
                && (funcMessageAllSlavesResumedOperational_ == null)) {
            soem_.setNotifyCheck(false);
        } else {
            soem_.setNotifyCheck(true);
        }
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageRecovered(String func) {
        funcMessageRecovered_ = func;
        if ((funcErrorSafeOpError_ == null) && (funcErrorLost_ == null) && (funcWarningSafeOp_ == null)
                && (funcMessageReconfigured_ == null) && (funcMessageRecovered_ == null) && (funcMessageFound_ == null)
                && (funcMessageAllSlavesResumedOperational_ == null)) {
            soem_.setNotifyCheck(false);
        } else {
            soem_.setNotifyCheck(true);
        }
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageFound(String func) {
        funcMessageFound_ = func;
        if ((funcErrorSafeOpError_ == null) && (funcErrorLost_ == null) && (funcWarningSafeOp_ == null)
                && (funcMessageReconfigured_ == null) && (funcMessageRecovered_ == null) && (funcMessageFound_ == null)
                && (funcMessageAllSlavesResumedOperational_ == null)) {
            soem_.setNotifyCheck(false);
        } else {
            soem_.setNotifyCheck(true);
        }
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageAllSlavesResumedOperational(String func) {
        funcMessageAllSlavesResumedOperational_ = func;
        if ((funcErrorSafeOpError_ == null) && (funcErrorLost_ == null) && (funcWarningSafeOp_ == null)
                && (funcMessageReconfigured_ == null) && (funcMessageRecovered_ == null) && (funcMessageFound_ == null)
                && (funcMessageAllSlavesResumedOperational_ == null)) {
            soem_.setNotifyCheck(false);
        } else {
            soem_.setNotifyCheck(true);
        }
    }

    /**
     *
     * @return
     */
    public Integer slavecount() {
        return soem_.slavecount();
    }

    /**
     *
     * @param slave
     * @return
     */
    public Integer state(int slave) {
        return soem_.state(slave);
    }

    /**
     *
     * @param slave
     * @return
     */
    public Boolean islost(int slave) {
        return soem_.islost(slave);
    }

    /**
     *
     * @return
     */
    public Boolean docheckstate() {
        return soem_.docheckstate();
    }

    /**
     *
     * @param slave
     * @param index
     * @param subIndex
     * @param byteSize
     * @return
     */
    public Byte[] sdoRead(int slave, int index, int subIndex, int byteSize) {
        return soem_.sdoRead(slave, index, subIndex, byteSize);
    }

    /**
     *
     * @param slave
     * @param index
     * @param subIndex
     * @param byteSize
     * @param value
     * @return
     */
    public Integer sdoWrite(int slave, int index, int subIndex, int byteSize, Object value) {
        byte[] bytes = null;

        if (value instanceof Number) {
            switch (byteSize) {
                case 1:
                    bytes = ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                            .put(((Number) value).byteValue()).array();
                    break;
                case 2:
                    bytes = ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                            .putShort(((Number) value).shortValue()).array();
                    break;
                case 4:
                    bytes = ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(((Number) value).intValue()).array();
                    break;
                case 8:
                    bytes = ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                            .putLong(((Number) value).longValue()).array();
                    break;
            }
        } else if (value instanceof JSObject) {
            Object length = ((JSObject) value).getMember("length");

            if (length instanceof Number) {
                Object buf;

                bytes = new byte[((Number) length).intValue()];
                for (int i = 0; i < bytes.length; i++) {
                    buf = ((JSObject) value).getSlot(i);
                    if (buf instanceof Number) {
                        bytes[i] = ((Number) buf).byteValue();
                    }
                }
            }
        }

        if (bytes != null) {
            return soem_.sdoWrite(slave, index, subIndex, bytes);
        }

        return null;
    }

    /**
     *
     * @param slave
     * @param bitsOffset
     * @param bitsMask
     * @return
     */
    public Long in(int slave, long bitsOffset, long bitsMask) {
        return soem_.in(slave, bitsOffset, bitsMask);
    }

    /**
     *
     * @param slave
     * @param bitsOffset
     * @param bitsMask
     * @return
     */
    public Long outs(int slave, long bitsOffset, long bitsMask) {
        return soem_.out(slave, bitsOffset, bitsMask);
    }

    /**
     *
     * @param slave
     * @param bitsOffset
     * @param bitsMask
     * @param value
     * @return
     */
    public Long out(int slave, long bitsOffset, long bitsMask, long value) {
        return soem_.out(slave, bitsOffset, bitsMask, value);
    }

    /**
     *
     * @return
     */
    public String find_adapters() {
        return soem_.find_adapters();
    }

    /**
     *
     * @param printSDO
     * @param printMAP
     * @return
     */
    public String slaveinfo(boolean printSDO, boolean printMAP) {
        return soem_.slaveinfo(printSDO, printMAP);
    }

    @Override
    public void errorSafeOpErrorSoemEcatCheck(final int slave) {
        if (funcErrorSafeOpError_ != null) {
            Platform.runLater(() -> {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        webEngine_.executeScript(funcErrorSafeOpError_ + "(" + slave + ")");
                    } catch (JSException | NullPointerException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            });
        }
    }

    @Override
    public void errorLostSoemEcatCheck(final int slave) {
        if (funcErrorLost_ != null) {
            Platform.runLater(() -> {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        webEngine_.executeScript(funcErrorLost_ + "(" + slave + ")");
                    } catch (JSException | NullPointerException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            });
        }
    }

    @Override
    public void warningSafeOpSoemEcatCheck(final int slave) {
        if (funcWarningSafeOp_ != null) {
            Platform.runLater(() -> {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        webEngine_.executeScript(funcWarningSafeOp_ + "(" + slave + ")");
                    } catch (JSException | NullPointerException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            });
        }
    }

    @Override
    public void messageReconfiguredSoemEcatCheck(final int slave) {
        if (funcMessageReconfigured_ != null) {
            Platform.runLater(() -> {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        webEngine_.executeScript(funcMessageReconfigured_ + "(" + slave + ")");
                    } catch (JSException | NullPointerException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            });
        }
    }

    @Override
    public void messageRecoveredSoemEcatCheck(final int slave) {
        if (funcMessageRecovered_ != null) {
            Platform.runLater(() -> {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        webEngine_.executeScript(funcMessageRecovered_ + "(" + slave + ")");
                    } catch (JSException | NullPointerException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            });
        }
    }

    @Override
    public void messageFoundSoemEcatCheck(final int slave) {
        if (funcMessageFound_ != null) {
            Platform.runLater(() -> {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        webEngine_.executeScript(funcMessageFound_ + "(" + slave + ")");
                    } catch (JSException | NullPointerException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            });
        }
    }

    @Override
    public void messageAllSlavesResumedOperationalSoemEcatCheck() {
        if (funcMessageAllSlavesResumedOperational_ != null) {
            Platform.runLater(() -> {
                if (state_ == Worker.State.SUCCEEDED) {
                    try {
                        webEngine_.executeScript(funcMessageAllSlavesResumedOperational_ + "()");
                    } catch (JSException | NullPointerException ex) {
                        Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                    }
                }
            });
        }
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
    }

    @Override
    public void close() {
        soem_.close(this);
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
