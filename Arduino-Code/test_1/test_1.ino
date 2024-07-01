// ------------------------------- Useful Links -------------------------------------
// General resources on Arduino and libraries
// Arduino: https://www.arduino.cc

// Sensor Datasheets and Library Information:
// LSM9DS1 IMU Datasheet: https://html.alldatasheet.com/html-pdf/1242998/STMICROELECTRONICS/LSM9DS1/6815/3/LSM9DS1.html
// LSM9DS1 Library for Arduino: https://github.com/arduino-libraries/Arduino_LSM9DS1

// Learning Resources - Tutorials and guides:
// Inertial Measurement Units (IMUs) Introduction: https://learn.sparkfun.com/tutorials/all-about-imus

// Additional Resources:
// Kalman Filter Explanation and Tutorial: https://www.kalmanfilter.net/default.aspx
// Sensor Fusion Guide: https://www.thepoorengineer.com/en/sensor-fusion/

// ------------------------------- Feedback -------------------------------------
// Feedback on current sensor performance issues
// angle x: accurate measurement, no drift, no bias , stable
// angle y: accurate measurement, no drift, no bias , stable
// angle z: no accuracy, no drift, no bias , stable

// light: not used by this algorythm
// Not Sensitive to inaccurate value when lighting is poor
// A little slower at measuring the andgle , but way more stable


// Header
#include <Arduino_LSM9DS1.h> 

// Global variable
float RateRoll, RatePitch, RateYaw;
float RateCalibrationRoll, RateCalibrationPitch, RateCalibrationYaw;
int RateCalibrationNumber; 
float AccX, AccY, AccZ;
float MagX, MagY, MagZ;
float AngleRoll, AnglePitch, AngleYaw;
uint32_t LoopTimer;

// Kalman filter variables
float KalmanAngleRoll = 0, KalmanUncertaintyAngleRoll = 4;
float KalmanAnglePitch = 0, KalmanUncertaintyAnglePitch = 4;
float KalmanAngleYaw = 0, KalmanUncertaintyAngleYaw = 4;
float Kalman1DOutput[2]; 

// Kalman filter function
void kalman_1d(float &KalmanState, float &KalmanUncertainty, float KalmanInput, float KalmanMeasurement) {
  KalmanState += 0.004 * KalmanInput; // Predict next state
  KalmanUncertainty += 0.004 * 0.004 * 16; // Increase uncertainty
  float KalmanGain = KalmanUncertainty / (KalmanUncertainty + 9); // Calculate Kalman gain
  KalmanState += KalmanGain * (KalmanMeasurement - KalmanState); // Update estimate with measurement
  KalmanUncertainty = (1 - KalmanGain) * KalmanUncertainty; // Update uncertainty
  Kalman1DOutput[0] = KalmanState; // Store updated state
  Kalman1DOutput[1] = KalmanUncertainty; // Store updated uncertainty
}

// Function to read all sensors and store data in global variables
void readSensors() {
  if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable() && IMU.magneticFieldAvailable()) {
    float xAcc, yAcc, zAcc; 
    float xGyro, yGyro, zGyro;
    float xMag, yMag, zMag;

    IMU.readAcceleration(xAcc, yAcc, zAcc); 
    IMU.readGyroscope(xGyro, yGyro, zGyro); 
    IMU.readMagneticField(xMag, yMag, zMag); 

    RateRoll = xGyro;
    RatePitch = yGyro;
    RateYaw = zGyro;

    AccX = xAcc;
    AccY = yAcc;
    AccZ = zAcc;
    
    MagX = xMag;
    MagY = yMag;
    MagZ = zMag;

    // Calculate angles based on sensor data
    AngleRoll = atan2(yAcc, sqrt(xAcc * xAcc + zAcc * zAcc)) * (180.0 / PI);
    AnglePitch = -atan2(xAcc, sqrt(yAcc * yAcc + zAcc * zAcc)) * (180.0 / PI);
    AngleYaw = atan2(-yMag, xMag) * (180.0 / PI);
  }
}

// Setup function to initialize hardware and calibrate sensors
void setup() {
  Serial.begin(115200);
  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1); 
  }

  // Calibrate gyroscopes by averaging multiple readings
  for (RateCalibrationNumber = 0; RateCalibrationNumber < 2000; RateCalibrationNumber++) {
    readSensors();
    RateCalibrationRoll += RateRoll;
    RateCalibrationPitch += RatePitch;
    RateCalibrationYaw += RateYaw;
  }
  RateCalibrationRoll /= 2000;
  RateCalibrationPitch /= 2000;
  RateCalibrationYaw /= 2000;
  LoopTimer = micros(); 
}

// Loop function 
void loop() {

  // Read data and adjust for drifting
  readSensors(); 
  RateRoll -= RateCalibrationRoll; 
  RatePitch -= RateCalibrationPitch;
  RateYaw -= RateCalibrationYaw;

  // Apply Kalman filter for each angle
  kalman_1d(KalmanAngleRoll, KalmanUncertaintyAngleRoll, RateRoll, AngleRoll);
  KalmanAngleRoll = Kalman1DOutput[0];
  KalmanUncertaintyAngleRoll = Kalman1DOutput[1];
  kalman_1d(KalmanAnglePitch, KalmanUncertaintyAnglePitch, RatePitch, AnglePitch);
  KalmanAnglePitch = Kalman1DOutput[0];
  KalmanUncertaintyAnglePitch = Kalman1DOutput[1];
  kalman_1d(KalmanAngleYaw, KalmanUncertaintyAngleYaw, RateYaw, AngleYaw);
  KalmanAngleYaw = Kalman1DOutput[0];
  KalmanUncertaintyAngleYaw = Kalman1DOutput[1];

  // Output the filtered angles to the serial monitor
  Serial.print("Roll Angle [°] ");
  Serial.print(KalmanAngleRoll);
  Serial.print(" Pitch Angle [°] ");
  Serial.print(KalmanAnglePitch);
  Serial.print(" Yaw Angle [°] ");
  Serial.println(KalmanAngleYaw);

  while (micros() - LoopTimer < 4000);
  LoopTimer = micros();
}
