module com.welltech { // Or tn.esprit if that's your actual base package

    // === JavaFX Modules ===
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;       // Only if you use WebView
    requires javafx.media;     // Only if you use Media
    requires javafx.swing;     // For Swing interop or some chart fallbacks

    // === Standard Java Modules ===
    requires java.sql;
    requires java.desktop;     // For AWT/Swing classes (java.awt.Desktop, etc.)
    requires java.net.http;    // For HttpClient if used

    // === Third-party Libraries (Attempting to require by common automatic module names) ===
    // It's often better if these are not explicitly required if they cause "module not found"
    // and instead rely on them being on the classpath.
    // If compilation fails due to "module not found" for these, comment them out.

    // requires com.twilio.sdk.twilio; // Assuming this is the correct auto name from MANIFEST
    // If still errors, remove this and rely on classpath
    // requires mysql.connector.j;  // Correct automatic name for MySQL Connector/J
    // If still errors, remove this

    requires com.fasterxml.jackson.databind; // Jackson

    requires com.google.zxing;              // ZXing core
    requires com.google.zxing.javase;       // ZXing JavaSE

    requires org.apache.poi.poi;            // Apache POI main
    requires org.apache.poi.ooxml;          // Apache POI OOXML
    requires org.apache.commons.collections4; // Commons Collections 4
    requires org.apache.commons.compress;   // Commons Compress (for POI)
    // requires org.apache.poi.ooxml.schemas; // Often part of poi-ooxml, may not need separate
    // requires com.github.virtuald.curvesapi; // Curves API (for POI)

    requires org.glassfish.jaxb.runtime;    // JAXB Runtime

    requires flexmark.all;                  // Flexmark (if it provides this module name)

    requires cloudinary.core;               // Cloudinary
    requires cloudinary.http5;              // Cloudinary HTTP (check if you use http5 or http4)

    requires stripe.java;                   // Stripe

    requires org.apache.pdfbox;
    requires twilio;             // Apache PDFBox

    // === Opening Packages ===
    // Your base package where WellTechApplication might be
    opens com.welltech to javafx.graphics, javafx.fxml;

    // Package containing your FXML controllers
    opens com.welltech.controller to javafx.fxml;

    // Package containing your model classes (User, Reclamation, etc.)
    // Open to javafx.base for PropertyValueFactory in TableView, ComboBox, etc.
    // Open to other libraries if they need reflection (e.g., Jackson for JSON)
    opens com.welltech.model to javafx.base, com.fasterxml.jackson.databind;

    // === Exporting Packages (Mainly if other modules were to use this one) ===
    exports com.welltech;
    exports com.welltech.controller;
    exports com.welltech.model;
    exports com.welltech.dao;
    exports com.welltech.db;
    exports com.welltech.util;
    exports com.welltech.service;
}