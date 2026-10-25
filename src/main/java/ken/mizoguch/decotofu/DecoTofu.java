/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.decotofu;

import ken.mizoguch.webviewer.DesignWebController;

import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author mizoguch-ken
 */
public class DecoTofu extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        JavaLibrary.setClassName(this.getClass().getSimpleName());

        List<Image> icons = List.of(
                new Image(this.getClass().getResourceAsStream("/icons/icon_032.png")),
                new Image(this.getClass().getResourceAsStream("/icons/icon_048.png")),
                new Image(this.getClass().getResourceAsStream("/icons/icon_128.png")),
                new Image(this.getClass().getResourceAsStream("/icons/icon_256.png")),
                new Image(this.getClass().getResourceAsStream("/icons/icon_512.png")));

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/DesignWeb.fxml"));
        Parent root = loader.load();
        DesignWebController controller = loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().addAll(icons);
        controller.startUp(stage, icons);
        stage.show();
    }

    @Override
    public void stop() {
        Platform.exit();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
