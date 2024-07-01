package com.example.goniometer;

import androidx.annotation.NonNull;

public class Profile {
    private String firstName;
    private String lastName;
    private int ID;



    public Profile(int ID, String firstName, String lastName) {
        this.ID = ID;
        this.firstName = firstName;
        this.lastName = lastName;

    }
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
