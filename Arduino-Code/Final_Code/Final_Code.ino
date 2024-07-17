
// Header
#include <ArduinoBLE.h>
#include <Arduino_LSM9DS1.h>

// Global variable
// Constants for gyro sensitivity based on expected range settings (not used by this algorithm)
const float GyroSensitivity = 0.16375;

float RateRoll, RatePitch, RateYaw;
float AccX, AccY, AccZ;
float MagX, MagY, MagZ;
float AngleRoll, AnglePitch;
float yaw = 0;
uint32_t LoopTimer;
float gz_bias = 0;
float KalmanAngleRoll = 0, KalmanUncertaintyAngleRoll = 4;
float KalmanAnglePitch = 0, KalmanUncertaintyAnglePitch = 4;
float Kalman1DOutput[2];

BLEService imuService("19B10000-E8F2-537E-4F6C-D104768A1214");
BLEFloatCharacteristic rollCharacteristic("19B10001-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEFloatCharacteristic pitchCharacteristic("19B10002-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEFloatCharacteristic yawCharacteristic("19B10003-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEFloatCharacteristic AccXCharacteristic("19B10004-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEFloatCharacteristic AccYCharacteristic("19B10005-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEFloatCharacteristic AccZCharacteristic("19B10006-E8F2-537E-4F6C-D104768A1214", BLENotify);

// Kalman filter function
void kalman_1d(float &KalmanState, float &KalmanUncertainty, float KalmanInput, float KalmanMeasurement)
{
  KalmanState += 0.004 * KalmanInput;                             // Predict next state with input
  KalmanUncertainty += 0.004 * 0.004 * 16;                        // Increase uncertainty
  float KalmanGain = KalmanUncertainty / (KalmanUncertainty + 9); // Compute Kalman gain
  KalmanState += KalmanGain * (KalmanMeasurement - KalmanState);  // Update estimate with measurement
  KalmanUncertainty = (1 - KalmanGain) * KalmanUncertainty;       // Update uncertainty
  Kalman1DOutput[0] = KalmanState;                                // Store updated state
  Kalman1DOutput[1] = KalmanUncertainty;                          // Store updated uncertainty
}

// Function to read all sensors and store data in global variables
bool readSensors()
{
  if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable() && IMU.magneticFieldAvailable())
  {
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
    float gz_dps = RateYaw - gz_bias;              // Calculate degrees per second
    float dt = (micros() - LoopTimer) / 1000000.0; // Calculate elapsed time in seconds
    yaw += gz_dps * dt;

    return true;
  }
  return false;
}

// Setup function to initialize hardware and calibrate sensors
void setup()
{
  Serial.begin(115200);
  if (!IMU.begin())
  {
    Serial.println("Failed to initialize IMU!");
    while (1)
      ;
  }

  if (!BLE.begin())
  {
    Serial.println("starting BLE failed!");
    while (1)
      ;
  }

  // Calculate gyro Z-axis bias for accurate yaw calculation
  gz_bias = calculate_gz_bias();
  LoopTimer = micros();

  // Setup ble
  BLE.setLocalName("IMU Sensor");
  BLE.setAdvertisedService(imuService);

  imuService.addCharacteristic(rollCharacteristic);
  imuService.addCharacteristic(pitchCharacteristic);
  imuService.addCharacteristic(yawCharacteristic);
  imuService.addCharacteristic(AccXCharacteristic);
  imuService.addCharacteristic(AccYCharacteristic);
  imuService.addCharacteristic(AccZCharacteristic);
  BLE.addService(imuService);

  rollCharacteristic.writeValue(0.0);  // Initial dummy value
  pitchCharacteristic.writeValue(0.0); // Initial dummy value
  yawCharacteristic.writeValue(0.0);   // Initial dummy value
  AccXCharacteristic.writeValue(0.0);  // Initial dummy value
  AccYCharacteristic.writeValue(0.0);  // Initial dummy value
  AccZCharacteristic.writeValue(0.0);  // Initial dummy value
  BLE.advertise();

  Serial.println("Bluetooth device active, waiting for connections...");
}

// loop function
void loop()
{

  BLEDevice central = BLE.central();
  if (central)
  {

    while (central.connected())
    {
      Serial.print("Connected to central: ");
      Serial.print(central.address());
      Serial.print(" ");

      readSensors();

      // Kalman filter calculations for Roll and Pitch only
      kalman_1d(KalmanAngleRoll, KalmanUncertaintyAngleRoll, RateRoll, AngleRoll);
      KalmanAngleRoll = Kalman1DOutput[0];
      KalmanUncertaintyAngleRoll = Kalman1DOutput[1];
      kalman_1d(KalmanAnglePitch, KalmanUncertaintyAnglePitch, RatePitch, AnglePitch);
      KalmanAnglePitch = Kalman1DOutput[0];
      KalmanUncertaintyAnglePitch = Kalman1DOutput[1];

      // update ble
      rollCharacteristic.writeValue(KalmanAngleRoll);
      pitchCharacteristic.writeValue(KalmanAnglePitch);
      yawCharacteristic.writeValue(yaw);
      AccXCharacteristic.writeValue(AccX);
      AccYCharacteristic.writeValue(AccY);
      AccZCharacteristic.writeValue(AccZ);

      // Print the measured and calculated angles to the serial monitor
      Serial.print("Roll Angle [°]: ");
      Serial.print(KalmanAngleRoll);
      Serial.print(" | Pitch Angle [°]: ");
      Serial.print(KalmanAnglePitch);
      Serial.print(" | Yaw Angle [°]: ");
      Serial.print(yaw);
      Serial.print(" | X Acc: ");
      Serial.print(AccX);
      Serial.print(" | Y Acc: ");
      Serial.print(AccY);
      Serial.print(" | Z Acc: ");
      Serial.println(AccZ);

      // Maintain a loop timing of 40 milliseconds
      while (micros() - LoopTimer < 4000);
      LoopTimer = micros(); // Reset loop timer
    }
    Serial.println("Connection Lost");
  }
}

// Function to calculate and return gyroscope Z-axis bias
float calculate_gz_bias()
{
  int numSamples = 2000;
  float sum = 0;
  for (int i = 0; i < numSamples; i++)
  {
    float xGyro, yGyro, zGyro;
    IMU.readGyroscope(xGyro, yGyro, zGyro);
    sum += zGyro / GyroSensitivity;
    delay(2);
  }
  return sum / numSamples;
}
