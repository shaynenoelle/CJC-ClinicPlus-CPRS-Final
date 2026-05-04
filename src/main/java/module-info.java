module com.example.cjcschoolclinicmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    // ── Apache POI (Excel import) ─────────────────────────────────
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.compress;
    requires java.prefs;   // POI-OOXML depends on this

    opens com.example.cjcschoolclinicmanagementsystem to javafx.fxml;
    opens com.example.cjcschoolclinicmanagementsystem.controller to javafx.fxml;
    opens com.example.cjcschoolclinicmanagementsystem.model to javafx.base;

    exports com.example.cjcschoolclinicmanagementsystem;
    exports com.example.cjcschoolclinicmanagementsystem.controller;
    exports com.example.cjcschoolclinicmanagementsystem.model;
    exports com.example.cjcschoolclinicmanagementsystem.util;
}