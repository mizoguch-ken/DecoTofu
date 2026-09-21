/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.console;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
    private static volatile PrintStream printStream_ = null;

    /**
     *
     * @param file
     * @return
     */
    public static synchronized boolean setPrintStream(Path file) {
        if (file == null) {
            if (printStream_ != null) {
                printStream_.close();
                printStream_ = null;
            }
        } else {
            Path parent = file.getParent();
            if ((parent != null) && Files.exists(parent)) {
                try {
                    PrintStream printStream = new PrintStream(Files.newOutputStream(file), true, "UTF-8");
                    if (printStream_ != null) {
                        printStream_.close();
                    }
                    printStream_ = printStream;
                    return true;
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
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
        PrintStream printStream = printStream_;

        if (printStream == null) {
            Logger.getLogger(name).log(Level.SEVERE, null, throwable);
        } else {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter, true);
            for (StackTraceElement ste : throwable.getStackTrace()) {
                if (ste.getClassName().equals(name)) {
                    stringWriter.append(
                            LocalDateTime.now().format(DATE_FORMAT) + " " + name + " " + ste.getMethodName() + "\n");
                    break;
                }
            }
            throwable.printStackTrace(printWriter);

            printStream.println(stringWriter.getBuffer().toString());
        }
    }

    /**
     *
     * @param name
     * @param msg
     * @param err
     */
    public static void write(final String name, String msg, final boolean err) {
        PrintStream printStream = printStream_;

        if (printStream == null) {
            if (err) {
                Logger.getLogger(name).log(Level.WARNING, msg);
            } else {
                Logger.getLogger(name).log(Level.INFO, msg);
            }
        } else {
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            if (err) {
                printStream.println(timestamp + " :: " + name + " Error :: " + msg);
            } else {
                printStream.println(timestamp + " :: " + name + " :: " + msg);
            }
        }
    }
}
