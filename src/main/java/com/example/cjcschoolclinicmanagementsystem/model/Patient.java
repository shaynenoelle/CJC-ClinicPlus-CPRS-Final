package com.example.cjcschoolclinicmanagementsystem.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Patient {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty age;
    private final StringProperty course;
    private final StringProperty year;
    private final StringProperty gender;
    private final StringProperty college;
    private final StringProperty contact;
    private final StringProperty email;
    private final StringProperty birthdate;
    private final StringProperty type;
    private final StringProperty emergencyName;
    private final StringProperty relationship;
    private final StringProperty emergencyContact;
    private final StringProperty allergies;
    private final StringProperty conditions;
    private final StringProperty bloodType;
    private final StringProperty height;
    private final StringProperty weight;
    private final StringProperty medicalRemarks;

    public Patient(String id, String name, String course, String year) {
        this(id, name, "", course, year, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }

    public Patient(String id, String name, String age, String course, String year, String gender, String college, String contact, String email, String birthdate, String type,
                   String emergencyName, String relationship, String emergencyContact, String allergies, String conditions, String bloodType, String height, String weight, String medicalRemarks) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.age = new SimpleStringProperty(age);
        this.course = new SimpleStringProperty(course);
        this.year = new SimpleStringProperty(year);
        this.gender = new SimpleStringProperty(gender);
        this.college = new SimpleStringProperty(college);
        this.contact = new SimpleStringProperty(contact);
        this.email = new SimpleStringProperty(email);
        this.birthdate = new SimpleStringProperty(birthdate);
        this.type = new SimpleStringProperty(type);
        this.emergencyName = new SimpleStringProperty(emergencyName);
        this.relationship = new SimpleStringProperty(relationship);
        this.emergencyContact = new SimpleStringProperty(emergencyContact);
        this.allergies = new SimpleStringProperty(allergies);
        this.conditions = new SimpleStringProperty(conditions);
        this.bloodType = new SimpleStringProperty(bloodType);
        this.height = new SimpleStringProperty(height);
        this.weight = new SimpleStringProperty(weight);
        this.medicalRemarks = new SimpleStringProperty(medicalRemarks);
    }

    // Properties for TableView
    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty ageProperty() { return age; }
    public StringProperty courseProperty() { return course; }
    public StringProperty yearProperty() { return year; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty collegeProperty() { return college; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty emailProperty() { return email; }
    public StringProperty birthdateProperty() { return birthdate; }
    public StringProperty typeProperty() { return type; }
    public StringProperty emergencyNameProperty() { return emergencyName; }
    public StringProperty relationshipProperty() { return relationship; }
    public StringProperty emergencyContactProperty() { return emergencyContact; }
    public StringProperty allergiesProperty() { return allergies; }
    public StringProperty conditionsProperty() { return conditions; }
    public StringProperty bloodTypeProperty() { return bloodType; }
    public StringProperty heightProperty() { return height; }
    public StringProperty weightProperty() { return weight; }
    public StringProperty medicalRemarksProperty() { return medicalRemarks; }

    // Simple getters
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getAge() { return age.get(); }
    public String getCourse() { return course.get(); }
    public String getYear() { return year.get(); }
    public String getGender() { return gender.get(); }
    public String getCollege() { return college.get(); }
    public String getContact() { return contact.get(); }
    public String getEmail() { return email.get(); }
    public String getBirthdate() { return birthdate.get(); }
    public String getType() { return type.get(); }
    public String getEmergencyName() { return emergencyName.get(); }
    public String getRelationship() { return relationship.get(); }
    public String getEmergencyContact() { return emergencyContact.get(); }
    public String getAllergies() { return allergies.get(); }
    public String getConditions() { return conditions.get(); }
    public String getBloodType() { return bloodType.get(); }
    public String getHeight() { return height.get(); }
    public String getWeight() { return weight.get(); }
    public String getMedicalRemarks() { return medicalRemarks.get(); }
    // Alias getters for compatibility
    public String getPatientId() { return id.get(); }
    public String getFullName() { return name.get(); }
}