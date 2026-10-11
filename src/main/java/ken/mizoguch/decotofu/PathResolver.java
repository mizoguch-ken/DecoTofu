/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.decotofu;

import java.io.File;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * directory of the files used by the application (properties.xml, web, log,
 * plugins) : the launcher of the application does not always set the current
 * directory, so the location of the running jar is used as a fallback
 *
 * @author mizoguch-ken
 */
public class PathResolver {

    /**
     * number of the parent directories searched from the location of the application
     */
    public static final int MAX_PARENT_LOOKUP = 3;

    /**
     * files and directories which identify the directory of the application
     */
    private static final String[] APPLICATION_MARKERS = {"properties.xml", "web"};

    /**
     *
     * @return the directory the virtual machine was started in
     */
    public static Path userPath() {
        return Paths.get(System.getProperty("user.dir"));
    }

    /**
     * find one of the markers in the start directory and in its parents
     *
     * @param startPath
     * @param maxParentLookup number of the parents to search, negative for unlimited
     * @param markers
     * @return the found directory, null when there is no marker
     */
    public static Path findMarkerPath(Path startPath, int maxParentLookup, String... markers) {
        Path path = startPath;

        for (int index = 0; (path != null) && ((maxParentLookup < 0) || (index <= maxParentLookup)); index++) {
            if (hasMarker(path, markers)) {
                return path;
            }
            path = path.getParent();
        }
        return null;
    }

    /**
     * check if one of the markers exists in the directory
     *
     * @param path
     * @param markers
     * @return
     */
    public static boolean hasMarker(Path path, String... markers) {
        if (path == null) {
            return false;
        }
        for (String marker : markers) {
            if (Files.exists(path.resolve(marker))) {
                return true;
            }
        }
        return false;
    }

    /**
     * directory of the jar or of the classes of the application class
     *
     * @param applicationClass
     * @return the directory, null when the location of the class is unknown
     */
    public static Path applicationPath(Class<?> applicationClass) {
        if (applicationClass == null) {
            return null;
        }
        try {
            URL codeSource = applicationClass.getProtectionDomain().getCodeSource().getLocation();

            if (codeSource == null) {
                return null;
            }
            Path path = Paths.get(codeSource.toURI());

            if (Files.isDirectory(path)) {
                return path;
            }
            // a jar : the jar itself is not a directory, so its parent is used
            return path.getParent();
        } catch (Exception ex) {
            // a malformed URL or an other class loader : there is no application directory
            return null;
        }
    }

    /**
     * directory of the application : the current directory is used first to keep the
     * behaviour of the launcher, then the directory of the running jar or classes and
     * their parents, then the first entry of the java class path as the last fallback
     *
     * @param applicationClass
     * @param markers
     * @return the directory of the application, never null
     */
    public static Path resolve(Class<?> applicationClass, String... markers) {
        Path userPath = userPath();
        Path path;

        if (hasMarker(userPath, markers)) {
            return userPath;
        }
        path = findMarkerPath(applicationPath(applicationClass), MAX_PARENT_LOOKUP, markers);
        if (path != null) {
            return path;
        }
        path = findMarkerPath(classPath(), MAX_PARENT_LOOKUP, markers);
        if (path != null) {
            return path;
        }
        return userPath;
    }

    /**
     * directory of the DecoTofu application
     *
     * @param applicationClass
     * @return the directory of the application, never null
     */
    public static Path applicationDirectory(Class<?> applicationClass) {
        return resolve(applicationClass, APPLICATION_MARKERS);
    }

    /**
     * path of a file given by the javascript of a page or by a plugin : an absolute path
     * is used as it is, a relative path is used from the current directory when the file
     * exists there to keep the behaviour of the previous versions, otherwise it is used
     * from the directory of the application because the launcher of the application does
     * not always set the current directory
     *
     * @param applicationClass class used to locate the directory of the application
     * @param path path given by the page
     * @param markers files and directories which identify the directory of the application
     * @return the path to use, null when the given path can not be used
     */
    public static Path resolvePagePath(Class<?> applicationClass, String path, String... markers) {
        Path given;

        if ((path == null) || path.isEmpty()) {
            return null;
        }
        try {
            given = Paths.get(path);
        } catch (InvalidPathException ex) {
            return null;
        }
        if (given.isAbsolute()) {
            return given;
        }
        // the current directory is the one of the user.dir property, as in resolve() :
        // toAbsolutePath() would use the directory the virtual machine was started in,
        // which is not the directory kept in the property
        Path userPath = userPath().toAbsolutePath().resolve(given);

        if (Files.exists(userPath)) {
            return userPath;
        }
        Path applicationPath = resolve(applicationClass, markers).resolve(given);

        if (Files.exists(applicationPath)) {
            return applicationPath;
        }
        // the file exists in no directory : the current directory is kept so that a file
        // which is going to be created is created where the previous versions created it
        return userPath;
    }

    /**
     * path of a file given by the javascript of a page or by a plugin, from the directory
     * of the DecoTofu application
     *
     * @param applicationClass class used to locate the directory of the application
     * @param path path given by the page
     * @return the path to use, null when the given path can not be used
     */
    public static Path resolvePagePath(Class<?> applicationClass, String path) {
        return resolvePagePath(applicationClass, path, APPLICATION_MARKERS);
    }

    /**
     * first directory of the java class path, kept as a fallback for the old launchers
     *
     * @return the directory, null when the class path is unusable
     */
    private static Path classPath() {
        String[] entries = System.getProperty("java.class.path", "").split(File.pathSeparator);

        for (String entry : entries) {
            if (entry.isEmpty()) {
                continue;
            }
            try {
                Path path = Paths.get(entry).toAbsolutePath();
                Path parent = Files.isDirectory(path) ? path : path.getParent();

                if (parent != null) {
                    return parent;
                }
            } catch (InvalidPathException ex) {
                // an other entry can be usable
            }
        }
        return null;
    }
}
