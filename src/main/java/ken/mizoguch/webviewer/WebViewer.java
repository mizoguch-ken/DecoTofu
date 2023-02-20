/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.webviewer;

import ken.mizoguch.webviewer.plugin.WebViewerPlugin;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import ken.mizoguch.console.Console;
import ken.mizoguch.ladders.LaddersPlugin;
import ken.mizoguch.soem.SoemPlugin;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author mizoguch-ken
 */
public class WebViewer implements WebViewerPlugin {

    private Stage stage_;
    private List<Image> icons_;
    private final WebEngine webEngine_;
    private Worker.State workerState_;
    private Path webPath_;

    private StageSettingsPlugin stageSettingsPlugin_;
    private SoemPlugin soemPlugin_;
    private LaddersPlugin laddersPlugin_;
    private final List<WebViewerPlugin> plugins_;

    private final String undefined_ = "undefined";

    /**
     *
     * @return
     */
    public WebEngine getWebEngine() {
        return webEngine_;
    }

    /**
     *
     * @return
     */
    public Worker.State getWorkerState() {
        return workerState_;
    }

    /**
     *
     * @param stageSettingsPlugin
     */
    public void setStageSettings(StageSettingsPlugin stageSettingsPlugin) {
        stageSettingsPlugin_ = stageSettingsPlugin;
    }

    /**
     *
     * @param soemPlugin
     */
    public void setSoem(SoemPlugin soemPlugin) {
        soemPlugin_ = soemPlugin;
    }

    /**
     *
     * @param laddersPlugin
     */
    public void setLadders(LaddersPlugin laddersPlugin) {
        laddersPlugin_ = laddersPlugin;
    }

    /**
     *
     * @param index
     */
    public void load(String index) {
        webEngine_.load(index);
    }

    /**
     *
     * @param stage
     * @param icons
     * @param webPath
     */
    public void startUp(Stage stage, List<Image> icons, Path webPath) {
        stage_ = stage;
        icons_ = icons;
        webPath_ = webPath;

        try {
            // add stage settings plugin
            if (stageSettingsPlugin_ != null) {
                stageSettingsPlugin_.initialize(this);
                plugins_.add(stageSettingsPlugin_);
            }
            // add soem plugin
            if (soemPlugin_ != null) {
                soemPlugin_.initialize(this);
                plugins_.add(soemPlugin_);
            }

            // add ladders plugin
            if (laddersPlugin_ != null) {
                laddersPlugin_.initialize(this);
                plugins_.add(laddersPlugin_);
            }

            // web plugins
            Path pluginPath = webPath_.resolve("plugins");
            if (Files.exists(pluginPath)) {
                Files.list(pluginPath).forEach((plugin) -> {
                    if (Files.isRegularFile(plugin)) {
                        try {
                            JarFile jarFile = new JarFile(plugin.toFile());
                            Manifest manifest = jarFile.getManifest();
                            if (manifest != null) {
                                String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                                if (mainClass != null) {
                                    URLClassLoader loader = URLClassLoader.newInstance(new URL[]{plugin.toUri().toURL()}, getClass().getClassLoader());
                                    Class<WebViewerPlugin> clazz = (Class<WebViewerPlugin>) loader.loadClass(mainClass);
                                    WebViewerPlugin webViewerPlugin = clazz.getDeclaredConstructor().newInstance();
                                    webViewerPlugin.initialize(this);
                                    plugins_.add(webViewerPlugin);
                                }
                            }
                        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                            writeStackTrace(WebViewer.class.getName(), ex);
                        } catch (IOException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                            writeStackTrace(WebViewer.class.getName(), ex);
                        }
                    }
                });
            }
        } catch (IOException ex) {
            writeStackTrace(WebViewer.class.getName(), ex);
        }

        webEngine_.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            workerState_ = newValue;

            for (int index = 0, size = plugins_.size(); index < size; index++) {
                plugins_.get(index).state(workerState_);
            }
            if (workerState_ == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine_.executeScript("window");
                    stage_.setTitle((String) webEngine_.executeScript("document.title"));
                    for (int index = 0, size = plugins_.size(); index < size; index++) {
                        if (plugins_.get(index).functionName() != null) {
                            if (undefined_.equals(window.getMember(plugins_.get(index).functionName()))) {
                                window.setMember(plugins_.get(index).functionName(), plugins_.get(index));
                            }
                        }
                    }
                    if (!undefined_.equals(window.getMember("setup"))) {
                        webEngine_.executeScript("setup();");
                    }
                } catch (JSException | ClassCastException | SecurityException ex) {
                    writeStackTrace(WebViewer.class.getName(), ex);
                }
            } else if ((workerState_ == Worker.State.FAILED) && (webEngine_.getLoadWorker().getException() != null)) {
                write(WebViewer.class.getName(), webEngine_.getLoadWorker().getException().toString(), true);
            }
        });
    }

    /**
     *
     */
    public void cleanUp() {
        for (int index = 0, size = plugins_.size(); index < size; index++) {
            plugins_.get(index).close();
        }
        plugins_.clear();
    }

    /**
     *
     * @param webView
     */
    public WebViewer(WebView webView) {
        webEngine_ = webView.getEngine();
        webEngine_.setJavaScriptEnabled(true);

        webEngine_.setCreatePopupHandler((PopupFeatures param) -> {
            Stage stage = new Stage(StageStyle.DECORATED);
            WebView view = new WebView();
            WebViewer viewer = new WebViewer(view);

            stage.setScene(new Scene(view));
            if (!icons_.isEmpty()) {
                stage.getIcons().addAll(icons_);
            }
            stage.setResizable(param.isResizable());
            stage.setOnCloseRequest((WindowEvent event) -> {
                viewer.cleanUp();
            });
            viewer.setStageSettings(stageSettingsPlugin_);
            viewer.setLadders(laddersPlugin_);
            viewer.setSoem(soemPlugin_);
            viewer.startUp(stage, icons_, webPath_);

            stage.show();
            return view.getEngine();
        });

        webEngine_.setOnAlert((WebEvent<String> event) -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            if (!icons_.isEmpty()) {
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons_);
            }
            alert.setResizable(true);
            alert.setTitle("Alert");
            alert.getDialogPane().setHeaderText(null);
            alert.getDialogPane().setContentText(event.getData());
            alert.showAndWait();
        });

        webEngine_.setConfirmHandler((String param) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            if (!icons_.isEmpty()) {
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons_);
            }
            alert.setResizable(true);
            alert.setTitle("Confirm");
            alert.getDialogPane().setHeaderText(null);
            alert.getDialogPane().setContentText(param);
            Optional<ButtonType> result = alert.showAndWait();
            return (result.get() == ButtonType.OK);
        });

        webEngine_.setPromptHandler((PromptData param) -> {
            TextInputDialog alert = new TextInputDialog(param.getDefaultValue());
            if (!icons_.isEmpty()) {
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons_);
            }
            alert.setResizable(true);
            alert.setTitle("Prompt");
            alert.getDialogPane().setHeaderText(null);
            alert.getDialogPane().setContentText(param.getMessage());
            Optional<String> result = alert.showAndWait();
            if (result.isPresent()) {
                return result.get();
            } else {
                return null;
            }
        });

        webEngine_.setOnError((WebErrorEvent event) -> {
            write(WebViewer.class.getName(), event.getMessage(), true);
        });

        workerState_ = Worker.State.READY;
        plugins_ = new ArrayList<>();
    }

    @Override
    public void initialize(WebViewerPlugin webViewer) {
    }

    @Override
    public String functionName() {
        return null;
    }

    @Override
    public void state(Worker.State state) {
    }

    @Override
    public void close() {
    }

    @Override
    public Stage stage() {
        return stage_;
    }

    @Override
    public List<Image> icons() {
        return icons_;
    }

    @Override
    public WebEngine webEngine() {
        return webEngine_;
    }

    @Override
    public Path webPath() {
        return webPath_;
    }

    @Override
    public void writeStackTrace(String name, Throwable throwable) {
        Console.writeStackTrace(name, throwable);
    }

    @Override
    public void write(String name, String msg, boolean err) {
        Console.write(name, msg, err);
    }
}
