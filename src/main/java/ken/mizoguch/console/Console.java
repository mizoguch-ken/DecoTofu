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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mizoguch-ken
 */
public class Console {

    private static PrintStream printStream_ = null;

    /**
     *
     * @param file
     * @return
     */
    public static boolean setPrintStream(Path file) {
        if (file == null) {
            if (printStream_ != null) {
                printStream_.close();
                printStream_ = null;
            }
        } else if (Files.exists(file.getParent())) {
            try {
                printStream_ = new PrintStream(Files.newOutputStream(file), true, "UTF-8");
                return true;
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
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
        if (printStream_ == null) {
            Logger.getLogger(name).log(Level.SEVERE, null, throwable);
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss");
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter, true);
            for (StackTraceElement ste : throwable.getStackTrace()) {
                if (ste.getClassName().equals(name)) {
                    stringWriter.append(simpleDateFormat.format(new Date()) + " " + name + " " + ste.getMethodName() + "\n");
                    break;
                }
            }
            throwable.printStackTrace(printWriter);

            if (printStream_ != null) {
                printStream_.println(stringWriter.getBuffer().toString());
            }
        }
    }

    /**
     *
     * @param name
     * @param msg
     * @param err
     */
    public static void write(final String name, String msg, final boolean err) {
        if (printStream_ == null) {
            if (err) {
                Logger.getLogger(name).log(Level.WARNING, msg);
            } else {
                Logger.getLogger(name).log(Level.INFO, msg);
            }
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss");
            if (err) {
                printStream_.println(simpleDateFormat.format(new Date()) + " :: " + name + " Error :: " + msg);
            } else {
                printStream_.println(simpleDateFormat.format(new Date()) + " :: " + name + " :: " + msg);
            }
        }
    }
}
