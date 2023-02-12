/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.decotofu;

import ken.mizoguch.webviewer.DesignWebController;
import java.util.ArrayList;
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
        JavaLibrary.setClassName(getClass().getSimpleName());

        List<Image> icons = new ArrayList<>();
        icons.add(new Image(getClass().getClassLoader().getResourceAsStream("icons/icon_016.png")));
        icons.add(new Image(getClass().getClassLoader().getResourceAsStream("icons/icon_032.png")));
        icons.add(new Image(getClass().getClassLoader().getResourceAsStream("icons/icon_048.png")));
        icons.add(new Image(getClass().getClassLoader().getResourceAsStream("icons/icon_128.png")));
        icons.add(new Image(getClass().getClassLoader().getResourceAsStream("icons/icon_256.png")));
        icons.add(new Image(getClass().getClassLoader().getResourceAsStream("icons/icon_512.png")));

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/DesignWeb.fxml"));
        Parent root = (Parent) loader.load();
        DesignWebController controller = (DesignWebController) loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        if (!icons.isEmpty()) {
            stage.getIcons().addAll(icons);
        }
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
