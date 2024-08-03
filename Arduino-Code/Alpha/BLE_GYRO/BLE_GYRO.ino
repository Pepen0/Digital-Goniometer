#include <Arduino_LSM9DS1.h>
#include <ArduinoBLE.h>

// Define the BLE characteristic UUIDs
const char *bleServiceUuid = "19B10000-E8F2-537E-4F6C-D104768A1214";
const char *bleCharUuidRoll = "19B10001-E8F2-537E-4F6C-D104768A1214";
const char *bleCharUuidPitch = "19B10002-E8F2-537E-4F6C-D104768A1214";
const char *bleCharUuidYaw = "19B10003-E8F2-537E-4F6C-D104768A1214";

// BLE Service and Characteristics
BLEService imuService(bleServiceUuid);
BLEFloatCharacteristic rollCharacteristic(bleCharUuidRoll, BLERead | BLENotify);
BLEFloatCharacteristic pitchCharacteristic(bleCharUuidPitch, BLERead | BLENotify);
BLEFloatCharacteristic yawCharacteristic(bleCharUuidYaw, BLERead | BLENotify);


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

void setup() {
  Serial.begin(115200);
  while (!Serial);

  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }

  if (!BLE.begin()) {
    Serial.println("Failed to initialize BLE!");
    while (1);
  }

  BLE.setLocalName("LED");
  BLE.setAdvertisedService(imuService);

  imuService.addCharacteristic(rollCharacteristic);
  imuService.addCharacteristic(pitchCharacteristic);
  imuService.addCharacteristic(yawCharacteristic);

  BLE.addService(imuService);
  BLE.advertise();

  Serial.println("BLE ready. Advertise IMU data.");
}

void loop() {
  BLEDevice central = BLE.central();
  if (central) {
    Serial.print("Connected to central: ");
    Serial.println(central.address());
    while (central.connected()) {
      readSensors();
      kalman_1d(KalmanAngleRoll, KalmanUncertaintyAngleRoll, RateRoll, AngleRoll);
      KalmanAngleRoll = Kalman1DOutput[0];
      kalman_1d(KalmanAnglePitch, KalmanUncertaintyAnglePitch, RatePitch, AnglePitch);
      KalmanAnglePitch = Kalman1DOutput[0];
      // Send the angles via BLE
      rollCharacteristic.writeValue(KalmanAngleRoll);
      pitchCharacteristic.writeValue(KalmanAnglePitch);
      yawCharacteristic.writeValue(yaw);  // Direct yaw angle
      delay(100); // Adjust as needed to manage BLE data rate

      Serial.print("Gyroscope data - X: ");
      Serial.print(KalmanAngleRoll);
      Serial.print(" Y: ");
      Serial.print(KalmanAnglePitch);
      Serial.print(" Z: ");
      Serial.println(yaw);
    }
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
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
