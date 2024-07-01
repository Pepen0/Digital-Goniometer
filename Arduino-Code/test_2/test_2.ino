// ------------------------------- Useful Links -------------------------------------
// General resources on Arduino and libraries
// Arduino: https://www.arduino.cc


// Sensor Datasheets and Library Information:
// LSM9DS1 IMU Datasheet: https://html.alldatasheet.com/html-pdf/1242998/STMICROELECTRONICS/LSM9DS1/6815/3/LSM9DS1.html
// LSM9DS1 Library for Arduino: https://github.com/arduino-libraries/Arduino_LSM9DS1


// Learning Resources - Tutorials and guides:
// Inertial Measurement Units (IMUs) Introduction: https://learn.sparkfun.com/tutorials/all-about-imus
// Kalman Filter Explanation and Tutorial: https://www.kalmanfilter.net/default.aspx
// Sensor Fusion Guide: https://www.thepoorengineer.com/en/sensor-fusion/


// ------------------------------- Feedback -------------------------------------
// Feedback on current sensor performance issues
// angle x: accurate measurement, no drift, no bias , stable
// angle y: accurate measurement, no drift, no bias , stable
// angle z: accurate measurement, no drift, negligeable bias , stable
// light: not used by this algorithm
// Not Sensitive to inaccurate value when lighting is poor
// A little slower at measuring the angle x and y 
// fast at measuring z angle
// measures the pitch 

// Header
#include <Arduino_LSM9DS1.h>


// Global variable
// Constants for gyro sensitivity based on expected range settings (not used by this algorithm)
const float GyroSensitivity = 0.081875;

float RateRoll, RatePitch, RateYaw;
float RateCalibrationRoll, RateCalibrationPitch, RateCalibrationYaw;
int RateCalibrationNumber;
float AccX, AccY, AccZ;
float MagX, MagY, MagZ;
float AngleRoll, AnglePitch;
float yaw = 0; 
uint32_t LoopTimer;
float gz_bias = 0;
float KalmanAngleRoll = 0, KalmanUncertaintyAngleRoll = 4;
float KalmanAnglePitch = 0, KalmanUncertaintyAnglePitch = 4;
float Kalman1DOutput[2];

// Kalman filter function
void kalman_1d(float &KalmanState, float &KalmanUncertainty, float KalmanInput, float KalmanMeasurement) {
    KalmanState += 0.004 * KalmanInput; // Predict next state with input
    KalmanUncertainty += 0.004 * 0.004 * 16; // Increase uncertainty
    float KalmanGain = KalmanUncertainty / (KalmanUncertainty + 9); // Compute Kalman gain
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
        RateYaw = zGyro / GyroSensitivity;

        AccX = xAcc;
        AccY = yAcc;
        AccZ = zAcc;
        MagX = xMag;
        MagY = yMag;
        MagZ = zMag;

        // Calculate Roll and Pitch based on sensor data
        AngleRoll = atan2(yAcc, sqrt(xAcc * xAcc + zAcc * zAcc)) * (180.0 / PI);
        AnglePitch = -atan2(xAcc, sqrt(yAcc * yAcc + zAcc * zAcc)) * (180.0 / PI);

        // Continuous yaw update based on gyroscope data, corrected for bias
        float gz_dps = RateYaw - gz_bias; // Calculate degrees per second
        float dt = (micros() - LoopTimer) / 1000000.0; // Calculate elapsed time in seconds
        yaw += gz_dps * dt;
    }
}


// Setup function to initialize hardware and calibrate sensors
void setup() {
    Serial.begin(115200); 
    if (!IMU.begin()) {
        Serial.println("Failed to initialize IMU!");
        while (1); 
    }

    // Calculate gyro Z-axis bias for accurate yaw calculation
    gz_bias = calculate_gz_bias();
    LoopTimer = micros();
}

// loop function
void loop() {
    readSensors(); 

    // Kalman filter calculations for Roll and Pitch only
    kalman_1d(KalmanAngleRoll, KalmanUncertaintyAngleRoll, RateRoll, AngleRoll);
    KalmanAngleRoll = Kalman1DOutput[0];
    KalmanUncertaintyAngleRoll = Kalman1DOutput[1];
    kalman_1d(KalmanAnglePitch, KalmanUncertaintyAnglePitch, RatePitch, AnglePitch);
    KalmanAnglePitch = Kalman1DOutput[0];
    KalmanUncertaintyAnglePitch = Kalman1DOutput[1];

    // Print the calculated angles to the serial monitor
    Serial.print("Roll Angle [°]: ");
    Serial.print(KalmanAngleRoll);
    Serial.print(" | Pitch Angle [°]: ");
    Serial.print(KalmanAnglePitch);
    Serial.print(" | Yaw Angle [°]: ");
    Serial.println(yaw); // Directly print the continuously updated yaw angle

    while (micros() - LoopTimer < 4000); // Maintain a loop timing of 4 milliseconds
    LoopTimer = micros(); // Reset loop timer
}


// Function to calculate and return gyroscope Z-axis bias
float calculate_gz_bias() {
    int numSamples = 2000;
    float sum = 0;
    for (int i = 0; i < numSamples; i++) {
        float xGyro, yGyro, zGyro;
        IMU.readGyroscope(xGyro, yGyro, zGyro);
        sum += zGyro / GyroSensitivity; 
        delay(2); 
    }
    return sum / numSamples;
}
