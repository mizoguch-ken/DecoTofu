/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.decotofu;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import ken.mizoguch.webviewer.DesignWebController;
import javafx.application.Platform;

/**
 *
 * @author mizoguch-ken
 */
public class ShutdownHook extends Thread {

    private static final long CLEANUP_WAIT_SECONDS = 5;

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
        if (controller_ == null) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            controller_.cleanUp();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.runLater(() -> {
                    try {
                        controller_.cleanUp();
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await(CLEANUP_WAIT_SECONDS, TimeUnit.SECONDS);
            } catch (IllegalStateException ex) {
                // FX toolkit has already stopped
                controller_.cleanUp();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
