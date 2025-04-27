module com.welltech {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires stripe.java;
    requires org.apache.pdfbox; // Ensure the correct module name matches the library
    
    opens com.welltech to javafx.fxml;
    opens com.welltech.controller to javafx.fxml;
    
    exports com.welltech;
    exports com.welltech.controller;
    exports com.welltech.model;
    exports com.welltech.dao;
    exports com.welltech.db;
}