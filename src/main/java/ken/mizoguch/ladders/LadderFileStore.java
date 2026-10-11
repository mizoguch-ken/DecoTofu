/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.stage.FileChooser;
import ken.mizoguch.console.Console;
import ken.mizoguch.decotofu.JavaLibrary;

/**
 * the file of the ladders : the path of the ladder which the editor shows, the json representation
 * read from and written to a file, and the chooser of the file the user selects.
 *
 * the json skema is holded by LadderJson, so only this class knows how a ladder is written to a
 * file and how a file is read
 *
 * @author mizoguch-ken
 */
class LadderFileStore {

    private final Gson gson_ = new GsonBuilder().setPrettyPrinting().create();
    private final String newLineCharacter_ = "\r\n";

    private Path filePath_;

    /**
     *
     * @return the file of the ladder which the editor shows, null when the ladder is not saved
     */
    Path getFilePath() {
        return filePath_;
    }

    /**
     *
     * @param filePath the file of the ladder which the editor shows
     */
    void setFilePath(Path filePath) {
        filePath_ = filePath;
    }

    /**
     *
     * @return the name of the file of the ladder, null when the ladder is not saved
     */
    String getFileName() {
        if (filePath_ == null) {
            return null;
        }
        if (Files.isDirectory(filePath_)) {
            return null;
        }
        return filePath_.getFileName().toString();
    }

    /**
     *
     * @return the directory of the ladder, an empty string when no directory is given
     */
    String getWorkFilePath() {
        if (filePath_ == null) {
            return "";
        } else {
            return filePath_.toString();
        }
    }

    /**
     * the directory of the ladder : a file gives the directory which holds it
     *
     * @param workFilePath a directory or a file, nothing when the parameter is not given
     */
    void setWorkFilePath(String workFilePath) {
        if (workFilePath != null) {
            if (workFilePath.isEmpty()) {
                filePath_ = null;
            } else {
                filePath_ = Paths.get(workFilePath);
                if (!Files.isDirectory(filePath_)) {
                    filePath_ = filePath_.getParent();
                }
            }
        }
    }

    /**
     * the name of the file of the ladder without its extension : a chooser of a file shows it as
     * the name of the file to create
     *
     * @return the name without the extension, null when no file of the ladder is a file
     */
    String getFileBaseName() {
        if ((filePath_ == null) || Files.isDirectory(filePath_)) {
            return null;
        }
        return JavaLibrary.removeFileExtension(filePath_.getFileName());
    }

    /**
     * the json representation of a ladder file : the caller keeps the current ladder when the
     * answer is null
     *
     * @param file
     * @return the ladder of the file, null when the file is not a readable ladder json
     */
    LadderJson readJson(Path file) {
        if (file != null) {
            if (Files.isRegularFile(file) && Files.isReadable(file)) {
                try {
                    return gson_.fromJson(Files.readString(file, StandardCharsets.UTF_8), LadderJson.class);
                } catch (JsonParseException ex) {
                    // the file is not a valid ladder json : the caller keeps the current ladder
                    writeLog("json parse error [" + file.getFileName() + "]", true);
                    Console.writeStackTrace(Ladders.class.getName(), ex);
                } catch (IOException ex) {
                    Console.writeStackTrace(Ladders.class.getName(), ex);
                }
            }
        }
        return null;
    }

    /**
     * write the json representation of the ladders of the editor to a file : the lines of the
     * answer of the json library are written with the new line character of the application
     *
     * @param file
     * @param ladderJson
     * @return true when the file is written
     */
    boolean writeJson(Path file, LadderJson ladderJson) {
        if ((file != null) && (ladderJson != null)) {
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(file), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(new StringReader(gson_.toJson(ladderJson)))) {
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    outputStreamWriter.write(line);
                    if (bufferedReader.ready()) {
                        outputStreamWriter.write(newLineCharacter_);
                    }
                }
                return true;
            } catch (IOException ex) {
                Console.writeStackTrace(Ladders.class.getName(), ex);
            }
        }
        return false;
    }

    /**
     * a chooser of a json or a pdf file, opened in the directory of the ladder of the editor
     *
     * @param description description of the filter of the extension
     * @param extension extension of the files shown by the chooser, with its star
     * @param fileName name shown for the file to create, null when the chooser only opens a file
     * @return the chooser of the file
     */
    FileChooser getFileChooser(String description, String extension, String fileName) {
        FileChooser fileChooser = new FileChooser();

        if (filePath_ != null) {
            if (filePath_.getParent() != null) {
                if (Files.exists(filePath_.getParent())) {
                    fileChooser.setInitialDirectory(filePath_.getParent().toFile());
                }
            }
        }
        if (fileName != null) {
            fileChooser.setInitialFileName(fileName);
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        return fileChooser;
    }

    /**
     * number of the blocks which hold a script in a ladder json
     *
     * @param ladderJson
     * @return
     */
    static int countScripts(LadderJson ladderJson) {
        List<LadderJsonLadder> ladders;
        List<LadderJsonBlock> blocks;
        int count = 0;

        if (ladderJson == null) {
            return 0;
        }
        ladders = ladderJson.getLadders();
        if (ladders == null) {
            return 0;
        }
        for (LadderJsonLadder ladder : ladders) {
            blocks = ladder.getBlocks();
            if (blocks == null) {
                continue;
            }
            for (LadderJsonBlock block : blocks) {
                if (block.hasScript()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * remove the scripts of a ladder json, so that a ladder loaded from a file is used
     * without the javascript of its script blocks
     *
     * @param ladderJson
     * @return the number of the removed scripts
     */
    static int removeScripts(LadderJson ladderJson) {
        List<LadderJsonLadder> ladders;
        List<LadderJsonBlock> blocks;
        int count = 0;

        if (ladderJson == null) {
            return 0;
        }
        ladders = ladderJson.getLadders();
        if (ladders == null) {
            return 0;
        }
        for (LadderJsonLadder ladder : ladders) {
            blocks = ladder.getBlocks();
            if (blocks == null) {
                continue;
            }
            for (LadderJsonBlock block : blocks) {
                if (block.hasScript()) {
                    block.removeScript();
                    count++;
                }
            }
        }
        return count;
    }

    private void writeLog(final String msg, final boolean err) {
        Console.write(Ladders.class.getName(), msg, err);
    }
}
