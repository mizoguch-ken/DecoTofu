/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import ken.mizoguch.console.Console;
import netscape.javascript.JSException;

/**
 *
 * @author mizoguch-ken
 */
public class LadderIo {

    private volatile WebEngine webEngine_;
    private volatile Worker.State state_;

    private String address_, comment_;
    private volatile double value_;

    /**
     * the count of a counter block : it is changed by the cycle of the ladder and answered
     * to the view of the io by the javafx application thread, so it is volatile
     */
    private volatile double cumulativeValue_;
    private volatile boolean isDefaultLd_, isLd_, isLdRising_, isLdFalling_, isOldLd_;

    private Object scriptObject_;
    private final StringBuilder script_;
    private final Object lockScript_;
    private volatile boolean isCycled_;

    /**
     * maximum length of the javascript of the script blocks of a ladder : the script of
     * a block is written by the ladder and executed in the web page, so an abnormal
     * length is refused instead of filling the memory
     */
    public static final int LADDER_IO_SCRIPT_MAX_LENGTH = 4096;

    private final String undefined_ = "undefined";

    /**
     *
     * @param address
     */
    public LadderIo(String address) {
        webEngine_ = null;
        state_ = null;
        address_ = address;
        comment_ = "";
        value_ = 0.0;
        cumulativeValue_ = 0.0;
        isDefaultLd_ = false;
        isLd_ = false;
        isLdRising_ = false;
        isLdFalling_ = false;
        isOldLd_ = false;
        script_ = new StringBuilder();
        lockScript_ = new Object();
        isCycled_ = false;
    }

    /**
     *
     * @param webEngine
     * @param state
     */
    public void setWebEngineState(WebEngine webEngine, Worker.State state) {
        webEngine_ = webEngine;
        state_ = state;
    }

    /**
     *
     * @return
     */
    public String getAddress() {
        return address_;
    }

    /**
     *
     * @param address
     */
    public void setAddress(String address) {
        address_ = address;
    }

    /**
     *
     * @return
     */
    public String getComment() {
        return comment_;
    }

    /**
     *
     * @param comment
     */
    public void setComment(String comment) {
        comment_ = comment;
    }

    /**
     * check the rising and the falling edge of the io : it is done by the cycle of the
     * ladder which reads the io, so an edge is answered only to the cycle which makes it
     */
    public void checkEdge() {
        isLdRising_ = (isLd_ && !isOldLd_);
        isLdFalling_ = (!isLd_ && isOldLd_);
    }

    /**
     * @deprecated misspelling of {@link #checkEdge()} : kept for the compatibility of the
     * web pages and of the plugins which call it
     */
    @Deprecated
    public void chehckEdge() {
        checkEdge();
    }

    /**
     *
     * @return
     */
    public double getValue() {
        return value_;
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean setValue(double value) {
        isDefaultLd_ = false;
        value_ = value;
        isLd_ = (value != 0.0);
        checkEdge();
        isOldLd_ = isLd_;
        return isLd_;
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean setValueNot(double value) {
        isDefaultLd_ = true;
        value_ = value;
        isLd_ = (value != 0.0);
        checkEdge();
        isOldLd_ = isLd_;
        return isLd_;
    }

    /**
     *
     * @return
     */
    public boolean resetValue() {
        isLd_ = isDefaultLd_;
        value_ = 0.0;
        cumulativeValue_ = 0.0;
        checkEdge();
        isOldLd_ = isLd_;
        return isLd_;
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean setValueRising(double value) {
        isDefaultLd_ = false;
        isLd_ = (value != 0.0) && (value_ == 0.0);
        value_ = value;
        checkEdge();
        isOldLd_ = isLd_;
        return isLdRising_;
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean setValueRisingNot(double value) {
        isDefaultLd_ = true;
        isLd_ = !((value == 0.0) && (value_ != 0.0));
        value_ = value;
        checkEdge();
        isOldLd_ = isLd_;
        return isLdRising_;
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean setValueFalling(double value) {
        isDefaultLd_ = false;
        isLd_ = (value == 0.0) && (value_ != 0.0);
        value_ = value;
        checkEdge();
        isOldLd_ = isLd_;
        return isLdFalling_;
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean setValueFallingNot(double value) {
        isDefaultLd_ = true;
        isLd_ = !((value != 0.0) && (value_ == 0.0));
        value_ = value;
        checkEdge();
        isOldLd_ = isLd_;
        return isLdFalling_;
    }

    /**
     *
     * @param value
     * @param time
     * @param cycleTime
     * @return
     */
    public boolean setValueTimer(double value, double time, long cycleTime) {
        isDefaultLd_ = false;
        if (value != 0.0) {
            cumulativeValue_ += (cycleTime / 1000000.0);
            if ((time <= cumulativeValue_)) {
                value_ = value;
                isLd_ = true;
            } else {
                isLd_ = false;
                value_ = 0.0;
            }
        } else {
            cumulativeValue_ = 0.0;
            value_ = 0.0;
            isLd_ = false;
        }
        checkEdge();
        isOldLd_ = isLd_;
        return isLd_;
    }

    /**
     *
     * @param value
     * @param time
     * @param cycleTime
     * @return
     */
    public boolean setValueTimerNot(double value, double time, long cycleTime) {
        isDefaultLd_ = true;
        if (value != 0.0) {
            cumulativeValue_ += (cycleTime / 1000000.0);
            if (time > cumulativeValue_) {
                value_ = value;
                isLd_ = true;
            } else {
                value_ = 0.0;
                isLd_ = false;
            }
        } else {
            cumulativeValue_ = 0.0;
            value_ = 0.0;
            isLd_ = false;
        }
        checkEdge();
        isOldLd_ = isLd_;
        return isLd_;
    }

    /**
     *
     * @param value
     * @param count
     * @return
     */
    public boolean setValueCounter(double value, double count) {
        isDefaultLd_ = false;
        if (value != 0.0) {
            cumulativeValue_++;
            if (count <= cumulativeValue_) {
                value_ = value;
                isLd_ = true;
            } else {
                value_ = 0.0;
                isLd_ = false;
            }
        } else {
            cumulativeValue_ = 0.0;
            value_ = 0.0;
            isLd_ = false;
        }
        checkEdge();
        isOldLd_ = isLd_;
        return isLd_;
    }

    /**
     *
     * @param value
     * @param count
     * @return
     */
    public boolean setValueCounterNot(double value, double count) {
        isDefaultLd_ = true;
        if (value != 0.0) {
            cumulativeValue_++;
            if (count > cumulativeValue_) {
                value_ = value;
                isLd_ = true;
            } else {
                value_ = 0.0;
                isLd_ = false;
            }
        } else {
            cumulativeValue_ = 0.0;
            value_ = 0.0;
            isLd_ = false;
        }
        checkEdge();
        isOldLd_ = isLd_;
        return isLd_;
    }

    /**
     *
     * @param script
     * @return
     */
    public boolean setScript(String script) {
        if ((webEngine_ == null) || (script == null)) {
            return false;
        }
        isDefaultLd_ = false;
        synchronized (lockScript_) {
            if (!isScriptLengthAcceptable(script_.length(), script.length())) {
                // an abnormal length of script is refused : the javascript of a script
                // block is executed in the web page with the apis of the application
                Console.write(LadderIo.class.getName(), "script too long [" + address_ + "]", true);
                script_.setLength(0);
                return false;
            }
            script_.append(script).append(";");
        }
        Platform.runLater(() -> {
            String executeScript;

            synchronized (lockScript_) {
                executeScript = script_.toString();
                script_.delete(0, script_.length());
            }
            if (executeScript.length() > 0) {
                if (webEngine_ != null) {
                    if (state_ == Worker.State.SUCCEEDED) {
                        try {
                            scriptObject_ = webEngine_.executeScript(executeScript);
                        } catch (JSException | ClassCastException ex) {
                            Console.writeStackTrace(LadderIo.class.getName(), ex);
                        }
                        setScriptValue(scriptObject_);
                    }
                }
            }
        });
        return true;
    }

    /**
     * use the result of the javascript of a script block : the result comes from the
     * javafx application thread while the value of the io is read by the cycle of the
     * ladder, so the value and the edge flags are changed together
     *
     * @param scriptResult
     */
    private synchronized void setScriptValue(Object scriptResult) {
        if (!undefined_.equals(scriptResult)) {
            if (scriptResult instanceof Integer) {
                value_ = (Integer) scriptResult;
            } else if (scriptResult instanceof Double) {
                value_ = (Double) scriptResult;
            }
        }
        isLd_ = (value_ != 0.0);
        checkEdge();
        isOldLd_ = isLd_;
    }

    /**
     *
     * @return
     */
    public boolean isLd() {
        return isLd_;
    }

    /**
     *
     * @return
     */
    public boolean isLdRising() {
        return isLdRising_;
    }

    /**
     *
     * @return
     */
    public boolean isLdFalling() {
        return isLdFalling_;
    }

    /**
     *
     * @return
     */
    public double getCumulativeValue() {
        return cumulativeValue_;
    }

    /**
     *
     * @return
     */
    public boolean isCycled() {
        return isCycled_;
    }

    /**
     * check the length of the javascript which is waited by an io : the script of the
     * script blocks of a ladder is executed in the web page, so an abnormal length is
     * refused
     *
     * @param scriptLength length of the scripts already waited
     * @param newScriptLength length of the new script
     * @return true when the new script can be added
     */
    static boolean isScriptLengthAcceptable(int scriptLength, int newScriptLength) {
        return (scriptLength + newScriptLength + 1) <= LADDER_IO_SCRIPT_MAX_LENGTH;
    }

    /**
     *
     * @param isCycled
     */
    public void setCycled(boolean isCycled) {
        isCycled_ = isCycled;
    }
}
