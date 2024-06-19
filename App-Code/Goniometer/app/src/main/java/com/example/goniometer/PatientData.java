package com.example.goniometer;

public class PatientData {
    long id;
    String patientName;

    public PatientData(long id, String patientName) {
        this.id = id;
        this.patientName = patientName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
