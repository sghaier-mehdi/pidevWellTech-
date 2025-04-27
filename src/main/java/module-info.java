module com.welltech {
    // === JavaFX Modules (Standard) ===
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Often needed for UI rendering
    requires javafx.base;     // Often needed for properties and bindings
    requires java.net.http;
    requires com.fasterxml.jackson.databind;


    // === Standard JDK Modules ===
    requires java.sql;      // For JDBC database operations
    requires java.desktop;  // For AWT (BufferedImage) and ImageIO (used by ZXing for image conversion)

    // === ZXing Modules (Automatic Module Names) ===
    requires com.google.zxing;
    requires com.google.zxing.javase; // Provides Java SE-specific client code

    // === Apache POI Modules and Dependencies (Automatic Module Names) ===
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.collections4; // From commons-collections4 JAR
    requires org.apache.commons.compress; // From commons-compress JAR

    // === Explicitly require other POI transitive dependencies if needed at runtime ===
    // Check these names against MANIFEST.MF or Maven Central if module not found
    // These are common automatic module names for POI transitives:
    requires org.apache.poi.ooxml.schemas; // From poi-ooxml-schemas JAR
    requires com.github.virtuald.curvesapi; // From curvesapi JAR
    requires org.glassfish.jaxb.runtime;
    // Often needed transitively by POI-OOXML for JAXB (check if you have a JAXB dependency)


    // === OPEN PACKAGES FOR REFLECTIVE ACCESS ===
    // JavaFX FXML Loader needs to access your controller constructors and @FXML fields/methods reflectively.
    // JavaFX UI components (like TableView, ComboBox) need to access model properties reflectively (PropertyValueFactory).
    // This is the most common cause of runtime errors after compilation.

    // Open controller package to javafx.fxml so FXML can create controllers and inject @FXML fields
    opens com.welltech.controller to javafx.fxml;
    // Open model package to javafx.base so JavaFX UI components can access model properties
    opens com.welltech.model to javafx.base;
    // Open your base package if it contains FXML or classes accessed reflectively
    opens com.welltech to javafx.fxml;
    // Open other packages if needed (less common unless they have FXML or reflected access needs)
    // opens com.welltech.dao to ...;
    // opens com.welltech.db to ...;
    // opens com.welltech.util to ...;


    // === EXPORT PACKAGES ===
    // Export packages that should be visible to other modules that might depend on com.welltech
    exports com.welltech;
    exports com.welltech.controller;
    exports com.welltech.model;
    exports com.welltech.dao;
    exports com.welltech.db;
    exports com.welltech.util; // Export utility package (e.g., for ExcelExporter)
}