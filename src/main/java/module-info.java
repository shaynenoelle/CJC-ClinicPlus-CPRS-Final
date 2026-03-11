module com.example.cjcschoolclinicmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // Permission for SQLite JDBC
    requires java.desktop;

    // Opens packages so JavaFX can access your classes at runtime
    opens com.example.cjcschoolclinicmanagementsystem.controller to javafx.fxml, javafx.graphics;
    opens com.example.cjcschoolclinicmanagementsystem to javafx.fxml, javafx.graphics;
    opens com.example.cjcschoolclinicmanagementsystem.model to javafx.fxml, javafx.base;

    exports com.example.cjcschoolclinicmanagementsystem;
    exports com.example.cjcschoolclinicmanagementsystem.controller;
}