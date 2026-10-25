module DecoTofu {
    requires transitive java.desktop;
    requires java.logging;
    requires jdk.jsobject;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.web;
    requires javafx.swing;

    requires io.github.javadiffutils;
    requires com.google.gson;
    requires transitive org.jnrproject.ffi;
    requires org.jnrproject.jffi;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.tree.analysis;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
    // jnr-a64asm and jnr-x86asm are used only by the jnr-ffi provider : they are filename based
    // automatic modules, so they are kept in the shaded jar without being required by the module
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
