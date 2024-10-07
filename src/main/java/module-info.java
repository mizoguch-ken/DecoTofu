module DecoTofu {
    requires transitive java.desktop;
    requires java.logging;
    requires jdk.jsobject;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.web;
    requires javafx.swing;

    requires diffutils;
    requires com.google.gson;
    requires transitive org.jnrproject.ffi;
    requires org.jnrproject.jffi;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.tree.analysis;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
    requires jnr.a64asm;
    requires jnr.x86asm;
    requires org.apache.pdfbox;
    requires org.apache.fontbox;

    exports ken.mizoguch.decotofu to javafx.graphics;
    exports ken.mizoguch.ladders;
    exports ken.mizoguch.soem;
    exports ken.mizoguch.webviewer;
    exports ken.mizoguch.webviewer.plugin;

    opens ken.mizoguch.ladders to javafx.fxml;
    opens ken.mizoguch.webviewer to javafx.fxml;
}
