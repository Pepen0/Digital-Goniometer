// ------------------------------- Useful Links -------------------------------------
// General resources on Arduino and libraries
// Arduino: https://www.arduino.cc

// Wire Library - Detailed documentation and examples for I2C communication:
// Official documentation: https://www.arduino.cc/reference/en/language/functions/communication/wire/
// ESP8266 Wire library repository: https://github.com/esp8266/Arduino/tree/master/libraries/Wire

// Sensor Datasheets - Detailed technical information about the sensors:
// LSM9DS1 IMU Datasheet: https://html.alldatasheet.com/html-pdf/1242998/STMICROELECTRONICS/LSM9DS1/6815/3/LSM9DS1.html
// APDS-9960 Gesture/Proximity/Ambient Light Sensor Datasheet:
// https://www.alldatasheet.com/datasheet-pdf/pdf/918047/AVAGO/APDS-9960.html

// Additional Sensor Libraries - Libraries to interface with the hardware:
// LSM9DS1 Library for Arduino: https://github.com/arduino-libraries/Arduino_LSM9DS1
// APDS-9960 Library for Arduino: https://github.com/arduino-libraries/Arduino_APDS9960

// Learning Resources - Tutorials and guides:
// Inertial Measurement Units (IMUs) Introduction: https://learn.sparkfun.com/tutorials/all-about-imus
// Guide to using APDS-9960 with Arduino: https://learn.adafruit.com/adafruit-apds9960-breakout

// Community Forums - Get help and share knowledge:
// Arduino Forum: https://forum.arduino.cc
// ESP8266 Community Forum: https://www.esp8266.com

// Example Projects - See what others have built:
// IMU-based projects on Instructables: https://www.instructables.com/howto/LSM9DS1/
// APDS-9960 based gesture control projects: https://create.arduino.cc/projecthub/search?q=APDS-9960


// ------------------------------- Feedback -------------------------------------
// Feedback on current sensor performance issues
// angle x: no accuracy, drift every 3 seconds, not stable
// angle y: no accuracy, drift every 3 seconds, not stable
// angle z: no accuracy, drift too much , not stable
// light: accurate measurement from sensor

// is sensitive to inacurate value when the lighting is bad


// Header
#include <Arduino_LSM9DS1.h> 
#include <Arduino_APDS9960.h>  
#include <Math.h>  

// Setup function 
void setup() {
  Serial.begin(9600);  
  while (!Serial); 

  // Initialize IMU (Inertial Measurement Unit)
  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1); 
  }

  // Initialize APDS9960 sensor
  if (!APDS.begin()) {
    Serial.println("Failed to initialize APDS9960!");
    while (1);
  }

  Serial.println("IMU and APDS9960 initialized successfully!");
}

// Loop function
void loop() {
  float x, y, z; 
  int lightLevel = readLightIntensity(); 

  
  bool accAvailable = readAcceleration(x, y, z);
  float ax = x, ay = y, az = z;  

  bool gyroAvailable = readGyroscope(x, y, z);
  float gx = x, gy = y, gz = z;  

  bool magAvailable = readMagnetometer(x, y, z);
  float mx = x, my = y, mz = z; 

  float roll, pitch, yaw; 
  readOrientation(ax, ay, az, mx, my, mz, roll, pitch, yaw); 

  
  static int lastLightLevel = lightLevel; 
  bool displacementDetected = detectDisplacement(ax, ay, az, lightLevel, lastLightLevel);

 
  printAllSensors(ax, ay, az, gx, gy, gz, mx, my, mz, lightLevel, roll, pitch, yaw, displacementDetected);

  delay(10); 
}

// Print paragraph
void printAllSensors(float ax, float ay, float az, float gx, float gy, float gz, float mx, float my, float mz, int lightLevel, float roll, float pitch, float yaw, bool displacementDetected) {
    String message = "Sensor Readings:\n";
    message += "Acceleration - X: " + String(ax) + ", Y: " + String(ay) + ", Z: " + String(az) + "\n";
    message += "Gyroscope - X: " + String(gx) + ", Y: " + String(gy) + ", Z: " + String(gz) + "\n";
    message += "Magnetometer - X: " + String(mx) + ", Y: " + String(my) + ", Z: " + String(mz) + "\n";
    message += "Ambient Light Level: " + String(lightLevel) + "\n";
    message += "Roll: " + String(roll) + ", Pitch: " + String(pitch) + ", Yaw: " + String(yaw) + "\n";
    message += "Displacement Detected: " + String(displacementDetected ? "Yes" : "No");

    Serial.println(message); 
}

// Reading functions for each sensor, checking availability and reading data
bool readAcceleration(float &x, float &y, float &z) {
  if (IMU.accelerationAvailable()) {
    IMU.readAcceleration(x, y, z);
    return true;
  }
  return false;
}
bool readGyroscope(float &x, float &y, float &z) {
  if (IMU.gyroscopeAvailable()) {
    IMU.readGyroscope(x, y, z);
    return true;
  }
  return false;
}
bool readMagnetometer(float &x, float &y, float &z) {
  if (IMU.magneticFieldAvailable()) {
    IMU.readMagneticField(x, y, z);
    return true;
  }
  return false;
}
int readLightIntensity() {
  static int lastValidReading = 0;
  int r, g, b, c;
  if (APDS.colorAvailable()) {
    if (APDS.readColor(r, g, b, c)) { 
      lastValidReading = c;
    }
  }
  return lastValidReading;
}


// Orientation calculation based on sensor data
void readOrientation(float ax, float ay, float az, float mx, float my, float mz, float &roll, float &pitch, float &yaw) {
    // Calculate roll and pitch based on accelerometer data
    roll = atan2(ay, az) * 57.2958;
    pitch = atan2(-ax, sqrt(ay * ay + az * az)) * 57.2958;

    // Calculate yaw (heading) based on magnetometer data
    float mag_x = mx * cos(pitch) + mz * sin(pitch);
    float mag_y = mx * sin(roll) * sin(pitch) + my * cos(roll) - mz * sin(roll) * cos(pitch);
    yaw = atan2(-mag_y, mag_x) * 57.2958;
}

// Check if light intensity is stable, indicating no significant environmental change
bool isLightStable(int currentLightLevel, int &lastLightLevel) {
    const int lightThreshold = 2; 
    if (abs(currentLightLevel - lastLightLevel) > lightThreshold) {
        lastLightLevel = currentLightLevel;
        return false;
    }
    return true;
}

// Detect displacement based on accelerometer data and light stability
bool detectDisplacement(float &ax, float &ay, float &az, int currentLightLevel, int &lastLightLevel) {
    static float lastAx = 0, lastAy = 0, lastAz = 0;  // Hold last acceleration readings
    const float accelerationThreshold = 0.5;  // Threshold for detecting significant acceleration

    // Reset last acceleration values if light is not stable
    if (!isLightStable(currentLightLevel, lastLightLevel)) {
        
        lastAx = ax;
        lastAy = ay;
        lastAz = az;
        return false;
    }

    // Check for significant acceleration changes
    if (abs(ax - lastAx) > accelerationThreshold || abs(ay - lastAy) > accelerationThreshold || abs(az - lastAz) > accelerationThreshold) {
        lastAx = ax;
        lastAy = ay;
        lastAz = az;
        return true;
    }

    return false;  
}

