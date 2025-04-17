module com.welltech {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.welltech to javafx.fxml;
    opens com.welltech.controller to javafx.fxml;
    
    exports com.welltech;
    exports com.welltech.controller;
    exports com.welltech.model;
    exports com.welltech.dao;
    exports com.welltech.db;
} 