# Digital Goniometer Project

## Overview

The Digital Goniometer, developed by Team 8, is an advanced mobile application and IoT device designed to measure and record Range of Motion (ROM) with high precision. This digital tool targets healthcare professionals, providing them with a modern, efficient alternative to traditional goniometers for physical assessments. This project involved agile development and multiple sprints, ensuring functionality aligns with end-user needs, such as physiotherapists and orthopedic specialists.

For a full breakdown of the project, watch our [project overview on YouTube](https://youtu.be/OdDAHtXPX4Y).

## Project Description

The device integrates an Inertial Measurement Unit (IMU) sensor with Bluetooth Low Energy (BLE) to relay movement data to an Android application. The application’s robust features include patient data storage, real-time measurement display, and export options, enhancing user experience and patient progress tracking.

## Key Features

- **High-Precision ROM Measurement**: Measures joint angles for arm abduction, elbow rotation, head rotation, and hip abduction with a +/-5° accuracy.
- **User-Friendly Android Application**: Enables Bluetooth pairing, patient data management, and the display of real-time measurements.
- **Data Export**: Exports patient data in CSV format for reporting or integration into other systems.
- **Compact, Portable Hardware**: A 3D-printed enclosure encases the Arduino-based device for ease of use, durability, and portability.

## Development Process

Using agile Scrum methodologies, the team completed three sprints with the following goals:

- **Sprint 1**: Established core application features, including Bluetooth pairing, a simple user interface for ROM measurement, and initial patient file creation in SQLite.
- **Sprint 2**: Enhanced with additional ROM types (e.g., head rotation, arm abduction), improved patient record management, and user feedback on measurement accuracy and BLE connectivity.
- **Sprint 3**: Refined the UI design, added more ROM measurement types (head, arm, and elbow), and introduced comprehensive patient records.

The development process included rigorous testing for each feature through black-box testing, ensuring system functionality aligned with user requirements and feedback.

## Product Components

1. **3D-Printed Enclosure**: A custom, shock-resistant case housing the Arduino, battery, and IMU sensor.
2. **Arduino with IMU Sensor**: Captures precise angle measurements and transfers data to the Android application via BLE.
3. **Android Application**: Provides measurement and data management capabilities, supporting multiple patients and various measurement types.

## Getting Started

### Installation

1. **Download the App**: Locate and install the Digital Goniometer Android app from the `build/app` directory.
2. **Setup Device**: Power the device and connect via Bluetooth from within the app, entering the device’s hardware ID.
3. **Initialize Measurements**: Set up patient profiles, select desired ROM measurements, and begin tracking.

### Measurement Types Supported

The Digital Goniometer app supports the following measurements:
- **Head Rotation**: Measures left and right rotation angles.
- **Arm Abduction**: Captures the angle of arm lift away from the body.
- **Elbow Rotation**: Measures forearm rotation angles.
- **Hip Abduction**: Monitors the angle of hip movement.

For detailed usage instructions, refer to the [User Manual](./user-Manual.pdf).

## Testing

The team implemented a black-box testing strategy to validate functionality against user stories. Key test cases covered:
- **Measurement Accuracy**: Ensuring ROM is accurately recorded and displayed.
- **Bluetooth Connectivity**: Testing for stable and consistent device pairing.
- **Data Management**: Validating data recording, retrieval, and export in the app.

## Troubleshooting

1. **Bluetooth Connection Issues**: Ensure Bluetooth is enabled and device is powered on with the correct hardware ID entered.
2. **Inaccurate Measurements**: Check the sensor’s positioning and recalibrate if needed.

## Contributing

This project was developed collaboratively, and we welcome suggestions for improvements. Contact Team 8 for inquiries or to contribute.
