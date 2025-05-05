module com.welltech {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires twilio;
    requires mysql.connector.j;
    
    opens com.welltech to javafx.fxml;
    opens com.welltech.controller to javafx.fxml;
    opens com.welltech.model to javafx.base;
    
    exports com.welltech;
    exports com.welltech.controller;
    exports com.welltech.model;
    exports com.welltech.dao;
    exports com.welltech.db;
    exports com.welltech.service;
    exports com.welltech.util;
} 