/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.webviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ken.mizoguch.decotofu.DecoTofu;
import ken.mizoguch.decotofu.I18n;
import ken.mizoguch.webviewer.plugin.WebViewerPlugin;

/**
 *
 * @author mizoguch-ken
 */
public class StageSettingsPlugin implements WebViewerPlugin {

    private WebViewerPlugin webViewer_;
    private static final String FUNCTION_NAME = "stage";

    private javafx.stage.Stage stage_;

    /**
     *
     */
    public StageSettingsPlugin() {
        stage_ = null;
    }

    /**
     *
     */
    public void showLicenses() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        TextArea textArea = new TextArea();
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream licenseStream = this.getClass().getClassLoader().getResourceAsStream("LICENSES")) {
            if (licenseStream != null) {
                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(licenseStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }
                }
            } else {
                write(StageSettingsPlugin.class.getName(), I18n.get("log.stage.licensesNotFound"), true);
            }
        } catch (IOException ex) {
            writeStackTrace(StageSettingsPlugin.class.getName(), ex);
        }
        textArea.setText(stringBuilder.toString());
        List<Image> icons = icons();
        if ((icons != null) && !icons.isEmpty()) {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons);
        }
        alert.setResizable(true);
        alert.setTitle(I18n.get("stage.licenses.title"));
        alert.getDialogPane().setHeaderText(null);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        alert.getDialogPane().setContent(textArea);
        alert.show();
    }

    /**
     *
     */
    public void showVersion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        String version = I18n.get("stage.version.content", getVersion());

        List<Image> icons = icons();
        if ((icons != null) && !icons.isEmpty()) {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(icons);
        }
        alert.setResizable(true);
        alert.setTitle(I18n.get("stage.version.title"));
        alert.setHeaderText("DecoTofu");
        alert.setContentText(version);
        alert.show();
    }

    public String getVersion() {
        String implVersion = DecoTofu.class.getPackage().getImplementationVersion();
        return (implVersion != null) ? implVersion : "unknown";
    }

    public String getLanguage() {
        return I18n.getLanguageSetting();
    }

    public String getResolvedLanguage() {
        return I18n.getResolvedLanguage();
    }

    public boolean setLanguage(String language) {
        return I18n.setLanguage(language);
    }

    public Boolean isFullScreen() {
        return (stage_ != null) ? stage_.isFullScreen() : null;
    }

    public void setFullScreen(boolean state) {
        if (stage_ != null) {
            stage_.setFullScreen(state);
        }
    }

    public Boolean isIconified() {
        return (stage_ != null) ? stage_.isIconified() : null;
    }

    public void setIconified(boolean state) {
        if (stage_ != null) {
            stage_.setIconified(state);
        }
    }

    /**
     *
     * @return
     */
    public Boolean isShowing() {
        return (stage_ != null) ? stage_.isShowing() : null;
    }

    /**
     *
     */
    public void show() {
        if (stage_ != null) {
            stage_.show();
        }
    }

    /**
     *
     */
    public void hide() {
        if (stage_ != null) {
            stage_.hide();
        }
    }

    /**
     *
     * @return
     */
    public Boolean isFocused() {
        return (stage_ != null) ? stage_.isFocused() : null;
    }

    /**
     *
     */
    public void requestFocus() {
        if (stage_ != null) {
            stage_.requestFocus();
        }
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return (stage_ != null) ? stage_.getTitle() : null;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        if (stage_ != null) {
            stage_.setTitle(title);
        }
    }

    /**
     *
     * @return
     */
    public Double getWidth() {
        return (stage_ != null) ? stage_.getWidth() : null;
    }

    /**
     *
     * @param value
     */
    public void setWidth(double value) {
        if (stage_ != null) {
            stage_.setWidth(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMinWidth() {
        return (stage_ != null) ? stage_.getMinWidth() : null;
    }

    /**
     *
     * @param value
     */
    public void setMinWidth(double value) {
        if (stage_ != null) {
            stage_.setMinWidth(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMaxWidth() {
        return (stage_ != null) ? stage_.getMaxWidth() : null;
    }

    /**
     *
     * @param value
     */
    public void setMaxWidth(double value) {
        if (stage_ != null) {
            stage_.setMaxWidth(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getHeight() {
        return (stage_ != null) ? stage_.getHeight() : null;
    }

    /**
     *
     * @param value
     */
    public void setHeight(double value) {
        if (stage_ != null) {
            stage_.setHeight(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMinHeight() {
        return (stage_ != null) ? stage_.getMinHeight() : null;
    }

    /**
     *
     * @param value
     */
    public void setMinHeight(double value) {
        if (stage_ != null) {
            stage_.setMinHeight(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getMaxHeight() {
        return (stage_ != null) ? stage_.getMaxHeight() : null;
    }

    /**
     *
     * @param value
     */
    public void setMaxHeight(double value) {
        if (stage_ != null) {
            stage_.setMaxHeight(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getOpacity() {
        return (stage_ != null) ? stage_.getOpacity() : null;
    }

    /**
     *
     * @param value
     */
    public void setOpacity(double value) {
        if (stage_ != null) {
            stage_.setOpacity(value);
        }
    }

    /**
     *
     * @return
     */
    public Boolean isAlwaysOnTop() {
        return (stage_ != null) ? stage_.isAlwaysOnTop() : null;
    }

    /**
     *
     * @param state
     */
    public void setAlwaysOnTop(boolean state) {
        if (stage_ != null) {
            stage_.setAlwaysOnTop(state);
        }
    }

    /**
     *
     * @return
     */
    public Boolean isMaximized() {
        return (stage_ != null) ? stage_.isMaximized() : null;
    }

    /**
     *
     * @param state
     */
    public void setMaximized(boolean state) {
        if (stage_ != null) {
            stage_.setMaximized(state);
        }
    }

    /**
     *
     * @return
     */
    public Double getX() {
        return (stage_ != null) ? stage_.getX() : null;
    }

    /**
     *
     * @param value
     */
    public void setX(double value) {
        if (stage_ != null) {
            stage_.setX(value);
        }
    }

    /**
     *
     * @return
     */
    public Double getY() {
        return (stage_ != null) ? stage_.getY() : null;
    }

    /**
     *
     * @param value
     */
    public void setY(double value) {
        if (stage_ != null) {
            stage_.setY(value);
        }
    }

    /**
     *
     */
    public void toBack() {
        if (stage_ != null) {
            stage_.toBack();
        }
    }

    /**
     *
     */
    public void toFront() {
        if (stage_ != null) {
            stage_.toFront();
        }
    }

    @Override
    public void initialize(WebViewerPlugin webViewer) {
        webViewer_ = webViewer;
        stage_ = (webViewer != null) ? webViewer.stage() : null;
    }

    @Override
    public String functionName() {
        return FUNCTION_NAME;
    }

    @Override
    public void state(Worker.State state) {
    }

    @Override
    public void close() {
        // The window is closed by its own close request handler. Closing the stage from
        // here would re-enter the close request of the window that is already closing.
    }

    @Override
    public javafx.stage.Stage stage() {
        return (webViewer_ != null) ? webViewer_.stage() : null;
    }

    @Override
    public List<Image> icons() {
        return (webViewer_ != null) ? webViewer_.icons() : null;
    }

    @Override
    public WebEngine webEngine() {
        return (webViewer_ != null) ? webViewer_.webEngine() : null;
    }

    @Override
    public Path webPath() {
        return (webViewer_ != null) ? webViewer_.webPath() : null;
    }

    @Override
    public void writeStackTrace(String name, Throwable throwable) {
        if (webViewer_ != null) {
            webViewer_.writeStackTrace(name, throwable);
        } else {
            ken.mizoguch.console.Console.writeStackTrace(name, throwable);
        }
    }

    @Override
    public void write(String name, String msg, boolean err) {
        if (webViewer_ != null) {
            webViewer_.write(name, msg, err);
        } else {
            ken.mizoguch.console.Console.write(name, msg, err);
        }
    }
}
