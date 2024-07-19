package com.example.goniometer;

public class Measurement {
    private final long id;
    private final long patientId;
    private final String measurementType;
    private final double leftAngle;
    private final double rightAngle;
    private final String timestamp;

    // Constructor
    public Measurement(long id, long patientId, String measurementType, double leftAngle, double rightAngle, String timestamp) {
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

    public double getLeftAngle() {
        return leftAngle;
    }

    public double getRightAngle() {
        return rightAngle;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
