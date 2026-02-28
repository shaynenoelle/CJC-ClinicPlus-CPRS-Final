package com.example.cjcschoolclinicmanagementsystem.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Patient {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty course;
    private final StringProperty year;
    private final StringProperty gender;
    private final StringProperty contact;
    private final StringProperty birthdate;
    private final StringProperty medicalHistory;

    public Patient(String id, String name, String course, String year) {
        this(id, name, course, year, "", "", "", "");
    }

    public Patient(String id, String name, String course, String year, String gender, String contact, String birthdate, String medicalHistory) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.course = new SimpleStringProperty(course);
        this.year = new SimpleStringProperty(year);
        this.gender = new SimpleStringProperty(gender);
        this.contact = new SimpleStringProperty(contact);
        this.birthdate = new SimpleStringProperty(birthdate);
        this.medicalHistory = new SimpleStringProperty(medicalHistory);
    }

    // Getters for TableView
    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty courseProperty() { return course; }
    public StringProperty yearProperty() { return year; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty birthdateProperty() { return birthdate; }
    public StringProperty medicalHistoryProperty() { return medicalHistory; }

    // Simple getters
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getCourse() { return course.get(); }
    public String getYear() { return year.get(); }
    public String getGender() { return gender.get(); }
    public String getContact() { return contact.get(); }
    public String getBirthdate() { return birthdate.get(); }
    public String getMedicalHistory() { return medicalHistory.get(); }
}