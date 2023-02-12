/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.decotofu;

import ken.mizoguch.webviewer.DesignWebController;
import javafx.application.Platform;

/**
 *
 * @author mizoguch-ken
 */
public class ShutdownHook extends Thread {

    final DesignWebController controller_;

    /**
     *
     * @param controller
     */
    public ShutdownHook(DesignWebController controller) {
        controller_ = controller;
    }

    @Override
    public void run() {
        if (Platform.isFxApplicationThread()) {
            if (controller_ != null) {
                controller_.cleanUp();
            }
        } else {
            Platform.runLater(() -> {
                if (controller_ != null) {
                    controller_.cleanUp();
                }
            });
        }
    }
}
