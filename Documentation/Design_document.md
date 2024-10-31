# Digital Goniometer Design Document

## 1. Introduction

The **Digital Goniometer** project aims to provide a modern, accurate tool for measuring Range of Motion (ROM) by integrating an IoT device with an Android application. The device is designed to cater to healthcare professionals and individuals managing rehabilitation, offering a streamlined alternative to traditional physical assessment tools. This document outlines the project’s hardware, software architecture, and system components.

## 2. System Overview

### 2.1 Purpose
The digital goniometer combines an Inertial Measurement Unit (IMU) sensor with a Bluetooth Low Energy (BLE) module to measure joint angles and ROM. The Android app provides a user interface for healthcare professionals to record, display, and export ROM data for individual patients, improving tracking and therapeutic decision-making.

### 2.2 Key Features
- **Real-time ROM Measurement**: Accurately measures joint angles with a +/- 5° margin.
- **Data Management**: Stores patient records, including measurement history and demographics.
- **Data Export**: Enables patient data export in CSV format.
- **User-Friendly Interface**: The Android application offers a straightforward interface for easy patient record management and device connectivity.

## 3. Hardware Architecture

The goniometer’s hardware consists of the following components:
- **Arduino Nano with IMU Sensor**: Collects motion data and transmits it to the Android app via BLE.
- **3D Printed Enclosure**: Protects the Arduino and battery, with access points for USB, battery switch, and LED indicators.
- **Battery Pack**: Powers the device, providing portability.

The IMU sensor measures angles and transmits data through the BLE module to the Android device, where it is displayed and recorded.

### 3.1 3D Enclosure Design
The enclosure is custom-designed to ensure secure handling and usability. It includes:
- **LED and Button Access**: Openings for the reset button and indicator LEDs.
- **Battery Switch**: External access to the power switch for easy device operation.

## 4. Software Architecture

### 4.1 Android Application
The Android app has been developed with modular activities and follows a simplified, user-centric design. Key modules include:
- **MainActivity**: Connects to the goniometer via BLE and provides access to the PatientListActivity.
- **PatientListActivity**: Manages patient records, including the ability to add, delete, and access patient measurement records.
- **Measurement Activities**: Specialized screens for each measurement type (e.g., Head Rotation, Arm Abduction), enabling live data display and data storage.

### 4.2 BLE Communication
The BLE connection facilitates seamless data transfer between the Arduino and the Android app. It includes:
- **Device Pairing**: Manual input of device ID for initial connection.
- **Connection Status**: Displays connection status throughout the app, using green (connected) and red (error) indicators.

### 4.3 Database (SQLite)
An embedded SQLite database manages patient records and ROM measurements. The database stores:
- **Patient Details**: Patient ID, name, and measurement history.
- **Measurement Data**: Includes timestamps, measurement types, and angle data.

## 5. Use Cases and Sequence Diagrams

### 5.1 Connecting to Bluetooth
The following sequence occurs when a user attempts to connect the device via Bluetooth:
1. User initiates the connection in MainActivity.
2. BLE manager verifies the connection status.
3. Toast messages confirm connection success or failure.

### 5.2 Measurement Process
1. User selects a measurement type (e.g., Arm Abduction).
2. The measurement activity initializes and displays real-time data.
3. The data is saved to the database upon completion.

### 5.3 Data Management
- **Add Patient**: Allows the user to input and save a new patient profile.
- **Access Patient Records**: Enables users to view historical measurements for specific patients.
- **Export Data**: The CSV export function provides accessible records for analysis or sharing.

## 6. Testing Strategy

The project was tested primarily through black-box testing, validating each function against the project’s user stories:
- **ROM Measurement Accuracy**: Ensured each measurement type meets the accuracy margin (+/- 5°).
- **Bluetooth Connectivity**: Confirmed stable and reliable BLE connections across typical usage distances.
- **Database Integrity**: Verified that patient data is stored, retrieved, and exported accurately.

## 7. Future Enhancements

- **AI Integration**: Implementing AI for user assistance, e.g., answering app-related questions in real-time.
- **Data Sharing**: Adding secure, compliant data sharing for collaborative healthcare teams.
- **Dynamic UI Animations**: Real-time animations to provide a visual guide for each ROM measurement.

## 8. References

1. **Android Studio Documentation**: [https://developer.android.com/studio](https://developer.android.com/studio)
2. **Bluetooth Low Energy Overview**: [https://developer.android.com/ble](https://developer.android.com/ble)
3. **Arduino Libraries and Resources**: [https://www.arduino.cc/reference/en/libraries](https://www.arduino.cc/reference/en/libraries)
