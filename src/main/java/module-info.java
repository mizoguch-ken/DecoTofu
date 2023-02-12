module DecoTofu {
    requires java.logging;
    requires jdk.jsobject;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    
    requires diffutils;
    requires com.google.gson;
    requires org.jnrproject.ffi;
    requires org.jnrproject.jffi;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.tree.analysis;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
    requires jnr.a64asm;
    requires jnr.x86asm;
    requires pdfbox;
    requires fontbox;
    requires commons.logging;
    
    exports ken.mizoguch.webviewer.plugin;
}
