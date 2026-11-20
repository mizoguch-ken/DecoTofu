/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.console;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mizoguch-ken
 */
public class Console {

    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyy'/'MM'/'dd' 'HH':'mm':'ss");
    private static final Object STREAM_LOCK = new Object();
    private static volatile PrintStream printStream_ = null;

    /**
     *
     * @param file
     * @return
     */
    public static boolean setPrintStream(Path file) {
        synchronized (STREAM_LOCK) {
            if (file == null) {
                if (printStream_ != null) {
                    printStream_.close();
                    printStream_ = null;
                }
            } else {
                Path parent = file.getParent();
                if ((parent != null) && Files.exists(parent)) {
                    try {
                        PrintStream printStream = new PrintStream(Files.newOutputStream(file), true,
                                StandardCharsets.UTF_8);
                        if (printStream_ != null) {
                            printStream_.close();
                        }
                        printStream_ = printStream;
                        return true;
                    } catch (IOException ex) {
                        Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return false;
        }
    }

    /**
     *
     */
    public static void close() {
        setPrintStream(null);
    }

    /**
     *
     * @param name
     * @param throwable
     */
    public static void writeStackTrace(final String name, final Throwable throwable) {
        if (throwable == null) {
            return;
        }
        String loggerName = (name != null) ? name : Console.class.getName();

        synchronized (STREAM_LOCK) {
            PrintStream printStream = printStream_;
            if (printStream != null) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter, true);
                for (StackTraceElement ste : throwable.getStackTrace()) {
                    if (ste.getClassName().equals(loggerName)) {
                        stringWriter.append(
                                LocalDateTime.now().format(DATE_FORMAT) + " " + loggerName + " " + ste.getMethodName()
                                        + "\n");
                        break;
                    }
                }
                throwable.printStackTrace(printWriter);

                printStream.println(stringWriter.getBuffer().toString());
                return;
            }
        }
        Logger.getLogger(loggerName).log(Level.SEVERE, null, throwable);
    }

    /**
     *
     * @param name
     * @param msg
     * @param err
     */
    public static void write(final String name, String msg, final boolean err) {
        String loggerName = (name != null) ? name : Console.class.getName();

        synchronized (STREAM_LOCK) {
            PrintStream printStream = printStream_;
            if (printStream != null) {
                String timestamp = LocalDateTime.now().format(DATE_FORMAT);
                printStream.println(err ? timestamp + " :: " + loggerName + " Error :: " + msg
                        : timestamp + " :: " + loggerName + " :: " + msg);
                return;
            }
        }
        if (err) {
            Logger.getLogger(loggerName).log(Level.WARNING, msg);
        } else {
            Logger.getLogger(loggerName).log(Level.INFO, msg);
        }
    }
}
