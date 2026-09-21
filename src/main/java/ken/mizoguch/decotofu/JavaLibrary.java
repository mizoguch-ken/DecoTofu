/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.decotofu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import ken.mizoguch.console.Console;

/**
 *
 * @author mizoguch-ken
 */
public class JavaLibrary {

    private static class Finder extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private final List<Path> pathMatches;

        public Finder(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            pathMatches = new ArrayList<>();
        }

        public void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                pathMatches.add(file);
            }
        }

        public List<Path> done() {
            return pathMatches;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            find(file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            find(dir);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return CONTINUE;
        }
    }

    private static String className = "";
    private static final String DEFAULT_LIBRARY_PATH = System.getProperty("java.library.path", "");
    private static final List<Path> PATHS = new ArrayList<>();
    private static final List<String> LIBS = new ArrayList<>();

    /**
     *
     * @return
     */
    public static String getClassName() {
        return className;
    }

    /**
     *
     * @param name
     */
    public static void setClassName(String name) {
        className = name;
    }

    /**
     *
     * @param root
     * @param pattern
     */
    public static List<Path> findFile(Path root, String pattern) {
        List<Path> matches = new ArrayList<>();
        try {
            Finder finder = new Finder(pattern);
            Files.walkFileTree(root, finder);
            matches.addAll(finder.done());
        } catch (IOException ex) {
            Console.writeStackTrace(JavaLibrary.class.getName(), ex);
        }
        return matches;
    }

    /**
     *
     * @param path
     * @return
     */
    public static boolean isLibraryPath(Path path) {
        return PATHS.stream().anyMatch((t) -> (t.equals(path)));
    }

    /**
     *
     * @param classLoader
     * @param zipPath
     * @param systemLoad
     * @return
     */
    public static boolean extractResourceZip(ClassLoader classLoader, String zipPath, boolean systemLoad) {
        String zipName;

        if (zipPath == null) {
            return false;
        }
        if (zipPath.isEmpty()) {
            return false;
        }

        zipName = removeFileExtension(Paths.get(zipPath).getFileName());
        for (int i = 0; i < 10; i++) {
            Path local = Paths.get(System.getProperty("java.io.tmpdir"), getClassName() + "_" + getUserName() + "_" + i)
                    .resolve(zipName);
            // library paths check
            if (isLibraryPath(local)) {
                return true;
            }

            InputStream resourceStream = classLoader.getResourceAsStream(zipPath);
            if (resourceStream == null) {
                Console.write(JavaLibrary.class.getName(), "resource not found : " + zipPath, true);
                return false;
            }
            try (ZipInputStream zipInputStream = new ZipInputStream(resourceStream)) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null) {
                    Path file = resolveZipEntry(local, zipEntry);
                    if (file != null) {
                        if (zipEntry.isDirectory()) {
                            Files.createDirectories(file);
                        } else {
                            Files.createDirectories(file.getParent());
                            Files.copy(zipInputStream, file, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    zipInputStream.closeEntry();
                    zipEntry = zipInputStream.getNextEntry();
                }
            } catch (IOException ex) {
                Console.writeStackTrace(JavaLibrary.class.getName(), ex);
                continue;
            }

            // add PATHS
            PATHS.add(local);
            // set java.library.path
            refreshLibraryPath();
            return true;
        }
        return false;
    }

    /**
     * resolve an entry of a zip archive inside the destination directory
     *
     * @param targetDir
     * @param zipEntry
     * @return null when the entry would escape the destination directory
     */
    private static Path resolveZipEntry(Path targetDir, ZipEntry zipEntry) {
        Path destination = targetDir.normalize();
        Path file = destination.resolve(zipEntry.getName()).normalize();

        if (!file.startsWith(destination)) {
            Console.write(JavaLibrary.class.getName(),
                    "zip entry outside of destination directory : " + zipEntry.getName(), true);
            return null;
        }
        return file;
    }

    /**
     *
     * @param classLoader
     * @param resourcePath
     * @param systemLoad
     * @return
     */
    public static boolean extractResourceLibrary(ClassLoader classLoader, String resourcePath, boolean systemLoad) {
        Path resource;
        String libraryName;

        if (resourcePath == null) {
            return false;
        }
        if (resourcePath.isEmpty()) {
            return false;
        }

        resource = Paths.get(resourcePath);

        // library name
        libraryName = removeFileExtension(resource.getFileName());
        if (isLinux() || isMac()) {
            libraryName = stripLibraryPrefix(libraryName);
        }

        // library load check local
        if (isLibrary(libraryName)) {
            return false;
        }

        // library load check system
        if (systemLoad && loadLibrary(libraryName, systemLoad)) {
            return true;
        }

        for (int i = 0; i < 10; i++) {
            Path local = Paths.get(System.getProperty("java.io.tmpdir"), getClassName() + "_" + getUserName() + "_" + i);
            if (addResourceLibraryPath(classLoader, resourcePath, local, libraryName, systemLoad)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param libraryName
     * @return the library name without the "lib" prefix when it has one
     */
    private static String stripLibraryPrefix(String libraryName) {
        if (libraryName.startsWith("lib")) {
            return libraryName.substring("lib".length());
        }
        return libraryName;
    }

    /**
     *
     * @param classLoader
     * @param resourcePath
     * @param localPath
     * @param libraryName
     * @param systemLoad
     * @return
     */
    public static boolean addResourceLibraryPath(ClassLoader classLoader, String resourcePath, Path localPath,
            String libraryName, boolean systemLoad) {
        String prefix, suffix;

        // set prefix suffix
        if (isWindows()) {
            prefix = "";
            suffix = ".dll";
        } else if (isLinux()) {
            prefix = "lib";
            suffix = ".so";
        } else if (isMac()) {
            prefix = "lib";
            suffix = ".dylib";
        } else {
            prefix = "";
            suffix = "";
        }

        try (InputStream resourceStream = classLoader.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                Console.write(JavaLibrary.class.getName(), "resource not found : " + resourcePath, true);
                return false;
            }

            // mkdir
            if (!Files.exists(localPath)) {
                Files.createDirectories(localPath);
            }
            // copy file
            Files.copy(resourceStream, localPath.resolve(prefix + libraryName + suffix),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Console.writeStackTrace(JavaLibrary.class.getName(), ex);
            return false;
        }

        // library paths check
        if (isLibraryPath(localPath)) {
            return loadLibrary(libraryName, systemLoad);
        }
        // add PATHS
        PATHS.add(localPath);
        // set java.library.path
        return setLibraryPath(libraryName, systemLoad);
    }

    /**
     *
     * @param path
     * @param libraryName
     * @param systemLoad
     * @return
     */
    public static boolean addLibraryPath(Path path, String libraryName, boolean systemLoad) {
        // library paths check
        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                path = path.getParent();
            }
            if (isLibraryPath(path)) {
                return loadLibrary(libraryName, systemLoad);
            }
            // add PATHS
            PATHS.add(path);
            // set java.library.path
            return setLibraryPath(libraryName, systemLoad);
        }
        return false;
    }

    /**
     *
     * @param libraryName
     * @param systemLoad
     * @return
     */
    public static boolean setLibraryPath(String libraryName, boolean systemLoad) {
        if (!PATHS.isEmpty()) {
            refreshLibraryPath();
            return loadLibrary(libraryName, systemLoad);
        }
        return false;
    }

    /**
     *
     * @param libraryName
     * @return
     */
    public static boolean isLibrary(String libraryName) {
        return LIBS.stream().anyMatch((lib) -> (lib.equals(libraryName)));
    }

    /**
     *
     * @param libraryName
     * @return
     */
    public static boolean loadLibrary(String libraryName) {
        return loadLibrary(libraryName, true);
    }

    /**
     *
     * @param libraryName
     * @param systemLoad
     * @return
     */
    public static boolean loadLibrary(String libraryName, boolean systemLoad) {
        if ((libraryName == null) || libraryName.isEmpty()) {
            return false;
        }
        if (isLibrary(libraryName)) {
            return false;
        }
        try {
            if (systemLoad) {
                Path libraryFile = findLibraryFile(libraryName);
                if (libraryFile != null) {
                    // System.load() takes an absolute path, so it does not depend on the
                    // library search path cached inside the class loader
                    System.load(libraryFile.toAbsolutePath().toString());
                } else {
                    System.loadLibrary(libraryName);
                }
            }
            LIBS.add(libraryName);
            return true;
        } catch (UnsatisfiedLinkError ex) {
        } catch (SecurityException | NullPointerException ex) {
            Console.writeStackTrace(JavaLibrary.class.getName(), ex);
        }
        return false;
    }

    /**
     * search the collected library paths for a file matching the library name
     *
     * @param libraryName
     * @return null when no library file was found
     */
    private static Path findLibraryFile(String libraryName) {
        List<String> fileNames = new ArrayList<>();

        if (isWindows()) {
            fileNames.add(libraryName + ".dll");
        } else if (isLinux()) {
            fileNames.add("lib" + libraryName + ".so");
            fileNames.add(libraryName + ".so");
        } else if (isMac()) {
            fileNames.add("lib" + libraryName + ".dylib");
            fileNames.add(libraryName + ".dylib");
        }
        fileNames.add(libraryName);

        for (Path path : PATHS) {
            for (String fileName : fileNames) {
                Path file = path.resolve(fileName);
                if (Files.isRegularFile(file)) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    /**
     *
     * @return
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");
    }

    /**
     * rebuild java.library.path from the collected library directories
     *
     * @return
     */
    public static String refreshLibraryPath() {
        StringBuilder pathString = new StringBuilder(DEFAULT_LIBRARY_PATH);
        PATHS.stream().forEach((p) -> {
            try {
                pathString.append(File.pathSeparator);
                pathString.append(p.toRealPath().toString());
            } catch (IOException ex) {
                Console.writeStackTrace(JavaLibrary.class.getName(), ex);
            }
        });

        // Since JDK 12 the cached ClassLoader.sys_paths field does not exist any more
        // (JDK-8190173): the search path is resolved from java.library.path on every
        // lookup, so updating the system property is enough for System.loadLibrary and
        // for jnr-ffi, which reads the property again when it builds its search list.
        System.setProperty("java.library.path", pathString.toString());
        return pathString.toString();
    }

    /**
     *
     * @return
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("linux");
    }

    /**
     *
     * @return
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac");
    }

    /**
     *
     * @return
     */
    public static boolean is32Bit() {
        String arch;

        arch = System.getProperty("sun.arch.data.model");
        if (arch != null) {
            arch = arch.trim();
            if (!arch.isEmpty()) {
                return arch.equals("32");
            }
        }

        arch = System.getProperty("os.arch");
        if (arch != null) {
            arch = arch.trim();
            if (!arch.isEmpty()) {
                return arch.endsWith("86");
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public static boolean is64Bit() {
        String arch;

        arch = System.getProperty("sun.arch.data.model");
        if (arch != null) {
            arch = arch.trim();
            if (!arch.isEmpty()) {
                return arch.equals("64");
            }
        }

        arch = System.getProperty("os.arch");
        if (arch != null) {
            arch = arch.trim();
            if (!arch.isEmpty()) {
                return arch.endsWith("64");
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public static String getUserName() {
        return System.getProperty("user.name");
    }

    /**
     *
     * @param file
     * @return
     */
    public static String removeFileExtension(Path file) {
        return removeFileExtension(file.getFileName().toString());
    }

    /**
     *
     * @param name
     * @return
     */
    public static String removeFileExtension(String name) {
        int lastDotPos = name.lastIndexOf('.');

        if (lastDotPos < 1) {
            return name;
        } else {
            return name.substring(0, lastDotPos);
        }
    }
}
