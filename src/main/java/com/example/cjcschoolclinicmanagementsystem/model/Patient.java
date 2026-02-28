package com.example.cjcschoolclinicmanagementsystem.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Patient {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty course;
    private final StringProperty year;

    public Patient(String id, String name, String course, String year) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.course = new SimpleStringProperty(course);
        this.year = new SimpleStringProperty(year);
    }

    // Getters for TableView
    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty courseProperty() { return course; }
    public StringProperty yearProperty() { return year; }
}