package com.example.goniometer;

import java.io.Serializable;
public class Patient implements Serializable {
    private final long id;
    private final String firstName;
    private final String lastName;

    // Constructor
    public Patient(long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }



}
