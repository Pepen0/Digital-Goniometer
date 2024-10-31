# Quick Start Guide for the Digital Goniometer App

This guide will help you set up the Digital Goniometer hardware and run the Android app, connecting it via Bluetooth Low Energy (BLE).

---

## 1. Prerequisites

- **Android Device** with Bluetooth enabled.
- **Digital Goniometer Hardware** (Arduino Nano with IMU sensor and BLE module).
- **Batteries** installed in the Digital Goniometer.
- **App APK** from the `build/app` directory (or directly from the Play Store, if available).

---

## 2. Hardware Setup

1. **Power On the Device**: Insert batteries and switch on the Digital Goniometer. A LED indicator should light up to show it’s powered.
   
2. **Check LED Status**: 
   - **Green**: Device is ready and waiting for a connection.
   - **Red**: Indicates a connection or setup error. Double-check batteries and power switch.

---

## 3. Installing the App

1. **Install APK**: 
   - Download the APK file from the `build/app` directory.
   - Open the APK on your Android device and install it (ensure “Install from Unknown Sources” is enabled in your settings if necessary).

2. **Grant Permissions**: 
   - When launching the app for the first time, allow Bluetooth and location permissions as prompted.

---

## 4. Connecting to the Device via BLE

1. **Open the App**: Launch the Digital Goniometer app on your Android device.

2. **Enter Device ID**:
   - On the main screen, press the **Connect** button.
   - Enter the Bluetooth hardware ID (visible on the goniometer) to establish a secure connection.
   
3. **Confirm Connection**:
   - A green Bluetooth icon will appear if the connection is successful.
   - If unsuccessful, check the LED on the device and ensure the hardware ID is correct.

---

## 5. Taking a Measurement

1. **Create or Select Patient**:
   - Add a new patient by selecting **Add Patient** or choose an existing patient from the list.

2. **Select Measurement Type**:
   - Choose the desired ROM measurement (e.g., Head Rotation, Arm Abduction) from the **Assessment** page.

3. **Start Measurement**:
   - Press the **Start** button to begin recording. Move the limb as required.
   - Press **Stop** to finalize the measurement, and **Save** to store it in the patient’s record.

---

## 6. Exporting Data

1. **Go to Patient Records**:
   - Select a patient and open their record.

2. **Export to CSV**:
   - Tap the **Export** button to save the patient’s data in a CSV file on your Android device.

---

## Troubleshooting

- **Connection Issues**:
  - Ensure Bluetooth is enabled on the Android device.
  - Verify the device is powered on and in range (less than 3 meters).
  
- **Measurement Errors**:
  - Restart the device and reset the app. Reconnect if the BLE signal drops.

---

For further assistance, consult the [User Manual](./user-Manual.pdf).
