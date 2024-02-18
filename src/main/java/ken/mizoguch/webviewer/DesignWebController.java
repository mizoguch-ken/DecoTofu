/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.webviewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import ken.mizoguch.console.Console;
import ken.mizoguch.decotofu.JavaLibrary;
import ken.mizoguch.decotofu.ShutdownHook;
import ken.mizoguch.ladders.DesignLaddersController;
import ken.mizoguch.ladders.Ladders;
import ken.mizoguch.ladders.LaddersPlugin;
import ken.mizoguch.soem.Soem;
import ken.mizoguch.soem.SoemPlugin;

/**
 * FXML Controller class
 *
 * @author mizoguch-ken
 */
public class DesignWebController implements Initializable {

    @FXML
    private WebView webView;

    private StageSettingsPlugin stageSettingsPlugin_;
    private Soem soem_;
    private SoemPlugin soemPlugin_;
    private Ladders ladders_;
    private LaddersPlugin laddersPlugin_;

    private Stage stage_;
    private StageSettings stageSettings_;
    private List<Image> icons_;
    private Path currentPath_, webPath_, logPath_;
    private WebViewer webViewer_;
    private final String template_ = "<!DOCTYPE html>" + "\n"
            + "<html>" + "\n"
            + "<head>" + "\n"
            + "\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">" + "\n"
            + "\t<meta charset=\"utf-8\">" + "\n"
            + "\t<title>DecoTofu</title>" + "\n"
            + "\t<meta name=\"description\" content=\"\">" + "\n"
            + "\t<meta name=\"author\" content=\"\">" + "\n"
            + "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" + "\n"
            + "\t<link rel=\"stylesheet\" href=\"\">" + "\n"
            + "\t<!--[if lt IE 9]>" + "\n"
            + "\t\t<script src=\"//cdn.jsdelivr.net/html5shiv/3.7.2/html5shiv.min.js\"></script>" + "\n"
            + "\t\t<script src=\"//cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js\"></script>" + "\n"
            + "\t<![endif]-->" + "\n"
            + "\t<link rel=\"shortcut icon\" href=\"\">" + "\n"
            + "</head>" + "\n"
            + "<body>" + "\n"
            + "</body>" + "\n"
            + "</html>";

    // stage settings
    public class StageSettings {

        private boolean bExit;
        private boolean stageMaximized;
        private double stageWidth, stageHeight;

        public boolean isExit() {
            return bExit;
        }

        private void setExit(boolean bln) {
            bExit = bln;
        }

        public boolean isStageMaximized() {
            return stageMaximized;
        }

        public void setStageMaximized(boolean bln) {
            setStageMaximized(bln, true);
        }

        public void setStageMaximized(boolean bln, boolean update) {
            stageMaximized = bln;
            if (update) {
                if (Platform.isFxApplicationThread()) {
                    stage_.setMaximized(stageMaximized);
                } else {
                    Platform.runLater(() -> {
                        stage_.setMaximized(stageMaximized);
                    });
                }
            }
        }

        public double getStageWidth() {
            return stageWidth;
        }

        public void setStageWidth(double value) {
            setStageWidth(value, true);
        }

        public void setStageWidth(double value, boolean update) {
            stageWidth = value;
            if (update) {
                if (Platform.isFxApplicationThread()) {
                    if (!stageMaximized) {
                        stage_.setWidth(stageWidth);
                    }
                } else {
                    Platform.runLater(() -> {
                        if (!stageMaximized) {
                            stage_.setWidth(stageWidth);
                        }
                    });
                }
            }
        }

        public double getStageHeight() {
            return stageHeight;
        }

        public void setStageHeight(double value) {
            setStageHeight(value, true);
        }

        public void setStageHeight(double value, boolean update) {
            stageHeight = value;
            if (update) {
                if (Platform.isFxApplicationThread()) {
                    if (!stageMaximized) {
                        stage_.setHeight(stageHeight);
                    }
                } else {
                    Platform.runLater(() -> {
                        if (!stageMaximized) {
                            stage_.setHeight(stageHeight);
                        }
                    });
                }
            }
        }
    }

    private void addEventDesign() {
        // root
        stage_.setOnCloseRequest((WindowEvent event) -> {
            if (!cleanUp()) {
                event.consume();
            }
        });
        stage_.maximizedProperty()
                .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (newValue != null) {
                        stageSettings_.setStageMaximized(newValue, false);
                        if (!newValue) {
                            stageSettings_.setStageWidth(stageSettings_.getStageWidth(), false);
                            stageSettings_.setStageHeight(stageSettings_.getStageHeight(), false);
                        }
                    }
                });
        stage_.widthProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!stageSettings_.isStageMaximized()) {
                        stageSettings_.setStageWidth(stage_.getWidth(), false);
                    }
                });
        stage_.heightProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!stageSettings_.isStageMaximized()) {
                        stageSettings_.setStageHeight(stage_.getHeight(), false);
                    }
                });
    }

    private void initProperties() {
        // stage
        stageSettings_.setStageMaximized(false, false);
        stageSettings_.setStageWidth(640.0, false);
        stageSettings_.setStageHeight(480.0, false);
    }

    private boolean loadProperties(Path propertyFile) {
        if (Files.exists(propertyFile) && Files.isRegularFile(propertyFile) && Files.isReadable(propertyFile)) {
            Properties properties = new Properties();
            try {
                properties.loadFromXML(Files.newInputStream(propertyFile));

                // stage
                stageSettings_.setStageMaximized(
                        (Boolean.parseBoolean(properties.getProperty("STAGE_MAXIMIZED", "false"))), true);
                stageSettings_.setStageWidth(Double.parseDouble(properties.getProperty("STAGE_WIDTH", "640.0")), true);
                stageSettings_.setStageHeight(Double.parseDouble(properties.getProperty("STAGE_HEIGHT", "480.0")),
                        true);

                return true;
            } catch (FileNotFoundException ex) {
                Console.writeStackTrace(DesignWebController.class.getName(), ex);
            } catch (IOException ex) {
                Console.writeStackTrace(DesignWebController.class.getName(), ex);
            }
        } else {
            // stage
            stageSettings_.setStageMaximized(stageSettings_.isStageMaximized(), true);
            stageSettings_.setStageWidth(stageSettings_.getStageWidth(), true);
            stageSettings_.setStageHeight(stageSettings_.getStageHeight(), true);
        }
        return false;
    }

    private boolean saveProperties(Path propertyFile) {
        try {
            if (!Files.exists(propertyFile)) {
                Files.createFile(propertyFile);
            }
            if (Files.isWritable(propertyFile)) {
                Properties properties = new Properties();

                // stage
                properties.setProperty("STAGE_MAXIMIZED", Boolean.toString(stageSettings_.isStageMaximized()));
                properties.setProperty("STAGE_WIDTH", Double.toString(stageSettings_.getStageWidth()));
                properties.setProperty("STAGE_HEIGHT", Double.toString(stageSettings_.getStageHeight()));

                properties.storeToXML(Files.newOutputStream(propertyFile), JavaLibrary.getClassName());
                return true;
            }
        } catch (IOException ex) {
            Console.writeStackTrace(DesignWebController.class.getName(), ex);
        }
        return false;
    }

    /**
     *
     * @param stage
     * @param icons
     */
    public void startUp(Stage stage, List<Image> icons) {
        Path indexPath;

        stage_ = stage;
        icons_ = icons;

        // current path
        currentPath_ = Paths.get("properties.xml");
        if (Files.exists(currentPath_)) {
            currentPath_ = currentPath_.toAbsolutePath().getParent();
        } else {
            currentPath_ = Paths.get(System.getProperty("java.class.path").split(";")[0]).toAbsolutePath().getParent();
        }

        // web path
        webPath_ = currentPath_.resolve("web");
        if (!Files.exists(webPath_)) {
            try {
                Files.createDirectories(webPath_);
            } catch (IOException ex) {
                Console.writeStackTrace(DesignWebController.class.getName(), ex);
            }
        }

        // index path
        indexPath = Paths.get(webPath_.toUri().resolve("index.html"));
        if (!Files.exists(indexPath)) {
            try (OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(indexPath), "UTF-8")) {
                osw.write(template_);
            } catch (IOException ex) {
                Console.writeStackTrace(DesignWebController.class.getName(), ex);
            }
        }

        // log path
        logPath_ = currentPath_.resolve("log");
        if (logPath_ != null) {
            // If the log path exists, the log file will be output
            if (Files.exists(logPath_)) {
                SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMddHHmmss");
                Console.setPrintStream(logPath_.resolve("Console_" + sdfFile.format(new Date()) + ".txt"));
            }
        }

        // load properties
        initProperties();
        loadProperties(currentPath_.resolve("properties.xml"));

        // add Event
        addEventDesign();

        // stage settings
        stageSettingsPlugin_ = new StageSettingsPlugin();

        // soem
        soem_ = new Soem();
        soemPlugin_ = new SoemPlugin(soem_);

        // ladder
        try {
            Stage stageLadder = new Stage(StageStyle.DECORATED);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("/fxml/DesignLadders.fxml"));
            Parent root = (Parent) loader.load();
            DesignLaddersController laddersController = (DesignLaddersController) loader.getController();
            stageLadder.setScene(new Scene(root));
            if (!icons_.isEmpty()) {
                stageLadder.getIcons().addAll(icons_);
            }
            laddersController.startUp(stageLadder, icons_, currentPath_);
            ladders_ = laddersController.getLadders();
            laddersPlugin_ = new LaddersPlugin(soem_, ladders_);
        } catch (IOException ex) {
            Console.writeStackTrace(DesignWebController.class.getName(), ex);
        }

        // web viewer
        webViewer_.setStageSettings(stageSettingsPlugin_);
        webViewer_.setSoem(soemPlugin_);
        webViewer_.setLadders(laddersPlugin_);
        webViewer_.startUp(stage_, icons_, webPath_);
        webViewer_.load(indexPath.toUri().toString());
    }

    /**
     *
     * @return
     */
    public boolean cleanUp() {
        if (!stageSettings_.isExit()) {
            // ladder
            if (!ladders_.getDesignController().cleanUp()) {
                return false;
            }
            if (ladders_.getDesignController().getStage().isShowing()) {
                ladders_.getDesignController().getStage().close();
            }

            webViewer_.cleanUp();
            webViewer_ = null;

            // save properties
            saveProperties(currentPath_.resolve("properties.xml"));

            // console
            Console.close();

            // exit
            stageSettings_.setExit(true);
        }
        return true;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        stageSettings_ = new StageSettings();
        webViewer_ = new WebViewer(webView);

        Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));

        stageSettings_.setExit(false);
    }
}
