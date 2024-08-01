package com.example.goniometer;

public class Measurement {
    private final long id;
    private final long patientId;
    private final String measurementType;
    private final int leftAngle;
    private final int rightAngle;
    private final String timestamp;

    // Constructor
    public Measurement(long id, long patientId, String measurementType, int leftAngle, int rightAngle, String timestamp) {
        this.id = id;
        this.patientId = patientId;
        this.measurementType = measurementType;
        this.leftAngle = leftAngle;
        this.rightAngle = rightAngle;
        this.timestamp = timestamp;
    }

    // Getters
    public long getId() {
        return id;
    }

    public long getPatientId() {
        return patientId;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public int getLeftAngle() {
        return leftAngle;
    }

    public int getRightAngle() {
        return rightAngle;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
