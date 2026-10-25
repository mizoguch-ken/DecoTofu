/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ken.mizoguch.console.Console;
import ken.mizoguch.decotofu.PathResolver;
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

    /**
     * name of a js function : a name and the properties of an object only, so the name
     * of a function can not carry an other javascript
     */
    private static final Pattern PATTERN_SCRIPT_FUNCTION = Pattern
            .compile("^[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*\\z");

    private WebEngine webEngine_;
    private Worker.State state_;

    private final Soem soem_;

    /**
     * the notification of the monitor task of the ecat that the web page may be
     * notified of : every event is answered by the js function set with the
     * matching setter
     */
    private enum CheckEvent {
        ERROR_SAFE_OP_ERROR,
        ERROR_LOST,
        WARNING_SAFE_OP,
        MESSAGE_RECONFIGURED,
        MESSAGE_RECOVERED,
        MESSAGE_FOUND,
        MESSAGE_ALL_SLAVES_RESUMED_OPERATIONAL
    }

    private final EnumMap<CheckEvent, String> func_;

    /**
     *
     * @param soem
     */
    public SoemPlugin(Soem soem) {
        webEngine_ = null;
        state_ = Worker.State.READY;
        soem_ = soem;
        func_ = new EnumMap<>(CheckEvent.class);
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
     * load the shared library of SOEM : a relative path is used from the current
     * directory when the library is there, otherwise it is used from the directory of the
     * application because the launcher does not always set the current directory. The file
     * has to exist : a name which is not a file would let the loader of jnr-ffi take an
     * other library of the java library path
     *
     * @param libraryPath
     * @return true when the library of the given path is loaded
     */
    public Boolean load(String libraryPath) {
        Path path = PathResolver.resolvePagePath(SoemPlugin.class, libraryPath);

        if (path == null) {
            write(FUNCTION_NAME, "incorrect library path [" + libraryPath + "]", true);
            return false;
        }
        if (!Files.exists(path)) {
            write(FUNCTION_NAME, "library not found [" + libraryPath + "] in [" + PathResolver.userPath()
                    + "] or in [" + PathResolver.applicationDirectory(SoemPlugin.class) + "]", true);
            return false;
        }
        // Soem.load() answers null when the library can not be loaded : the web page gets
        // only true or false
        return Boolean.TRUE.equals(soem_.load(path));
    }

    /**
     *
     * @param size
     * @return
     */
    public Boolean init(int size) {
        return soem_.init(size);
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
        setNotify(CheckEvent.ERROR_SAFE_OP_ERROR, func);
    }

    /**
     *
     * @param func
     */
    public void setNotifyErrorLost(String func) {
        setNotify(CheckEvent.ERROR_LOST, func);
    }

    /**
     *
     * @param func
     */
    public void setNotifyWarningSafeOp(String func) {
        setNotify(CheckEvent.WARNING_SAFE_OP, func);
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageReconfigured(String func) {
        setNotify(CheckEvent.MESSAGE_RECONFIGURED, func);
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageRecovered(String func) {
        setNotify(CheckEvent.MESSAGE_RECOVERED, func);
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageFound(String func) {
        setNotify(CheckEvent.MESSAGE_FOUND, func);
    }

    /**
     *
     * @param func
     */
    public void setNotifyMessageAllSlavesResumedOperational(String func) {
        setNotify(CheckEvent.MESSAGE_ALL_SLAVES_RESUMED_OPERATIONAL, func);
    }

    /**
     * set the js function of one notification of the monitor task of the ecat :
     * the task answers the plugin only when the web page has at least one
     * function to call
     *
     * @param event notification of the monitor task
     * @param func js function name, null when the web page does not want it
     */
    private void setNotify(CheckEvent event, String func) {
        if (func != null) {
            func_.put(event, func);
        } else {
            func_.remove(event);
        }
        soem_.setNotifyCheck(!func_.isEmpty());
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

        if (value instanceof Number number) {
            bytes = switch (byteSize) {
                case 1 -> ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                        .put(number.byteValue()).array();
                case 2 -> ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                        .putShort(number.shortValue()).array();
                case 4 -> ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(number.intValue()).array();
                case 8 -> ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(number.longValue()).array();
                default -> null;
            };
        } else if (value instanceof JSObject object) {
            Object length = object.getMember("length");

            if (length instanceof Number lengthNumber) {
                bytes = new byte[lengthNumber.intValue()];
                for (int i = 0; i < bytes.length; i++) {
                    if (object.getSlot(i) instanceof Number slot) {
                        bytes[i] = slot.byteValue();
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
        runScript(func_.get(CheckEvent.ERROR_SAFE_OP_ERROR), "(" + slave + ")");
    }

    @Override
    public void errorLostSoemEcatCheck(final int slave) {
        runScript(func_.get(CheckEvent.ERROR_LOST), "(" + slave + ")");
    }

    @Override
    public void warningSafeOpSoemEcatCheck(final int slave) {
        runScript(func_.get(CheckEvent.WARNING_SAFE_OP), "(" + slave + ")");
    }

    @Override
    public void messageReconfiguredSoemEcatCheck(final int slave) {
        runScript(func_.get(CheckEvent.MESSAGE_RECONFIGURED), "(" + slave + ")");
    }

    @Override
    public void messageRecoveredSoemEcatCheck(final int slave) {
        runScript(func_.get(CheckEvent.MESSAGE_RECOVERED), "(" + slave + ")");
    }

    @Override
    public void messageFoundSoemEcatCheck(final int slave) {
        runScript(func_.get(CheckEvent.MESSAGE_FOUND), "(" + slave + ")");
    }

    @Override
    public void messageAllSlavesResumedOperationalSoemEcatCheck() {
        runScript(func_.get(CheckEvent.MESSAGE_ALL_SLAVES_RESUMED_OPERATIONAL), "()");
    }

    /**
     * call the js function of the web page on the fx thread : the notification
     * of the monitor task of the ecat comes from an other thread
     *
     * @param func js function name, nothing is done when the web page does not
     * set it
     * @param args call of the js function
     */
    private void runScript(String func, String args) {
        if (!isScriptFunction(func)) {
            if (func != null) {
                Console.write(SoemPlugin.class.getName(), "invalid script function [" + func + "]", true);
            }
            return;
        }
        Platform.runLater(() -> {
            if (state_ == Worker.State.SUCCEEDED) {
                try {
                    webEngine_.executeScript(func + args);
                } catch (JSException | NullPointerException ex) {
                    Console.writeStackTrace(SoemPlugin.class.getName(), ex);
                }
            }
        });
    }

    /**
     * check the name of a js function : the name and the arguments build the script of a
     * notification, so a name which comes from a web page can not carry an other
     * javascript
     *
     * @param func js function name
     * @return true when the name can be used to build a script
     */
    static boolean isScriptFunction(String func) {
        return (func != null) && PATTERN_SCRIPT_FUNCTION.matcher(func).matches();
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
        if (Boolean.FALSE.equals(soem_.close(this))) {
            // a native call of SOEM is still running : the native memory of the
            // ecat was not freed, Soem.close() writes the reason in the console
            Console.write(SoemPlugin.class.getName(), "the ecat is not closed", true);
        }
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
