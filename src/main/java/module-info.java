module com.welltech {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Standard
    requires java.net.http;
    requires java.sql;

    // Jackson
    requires com.fasterxml.jackson.databind;

    // ZXing
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // Apache POI
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.poi.ooxml.schemas;
    requires com.github.virtuald.curvesapi;
    requires org.glassfish.jaxb.runtime;

    // === FLEXMARK (Markdown Parser) ===
    requires flexmark;
    requires flexmark.util;
    requires javafx.media;
    requires twilio;
    requires javafx.swing;
    requires org.apache.pdfbox;
    requires stripe.java;
    requires cloudinary.core;

    // Open packages
    opens com.welltech.controller to javafx.fxml;
    opens com.welltech.model to javafx.base;
    opens com.welltech to javafx.fxml;

    // Exports
    exports com.welltech;
    exports com.welltech.controller;
    exports com.welltech.model;
    exports com.welltech.dao;
    exports com.welltech.db;
    exports com.welltech.util;
}
