module com.example.cjcschoolclinicmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // Permission for SQLite JDBC

    // Opens packages so JavaFX can access your classes at runtime
    opens com.example.cjcschoolclinicmanagementsystem.controller to javafx.fxml;
    opens com.example.cjcschoolclinicmanagementsystem to javafx.fxml;

    exports com.example.cjcschoolclinicmanagementsystem;
}