// Header
#include <ArduinoBLE.h>
#include <Arduino_LSM9DS1.h>

// Constants for gyro sensitivity based on expected range settings
const float GyroSensitivity = 0.0175; // Adjusted to match the sensitivity for LSM9DS1 in degrees per second per LSB

// Global variables for accelerometer, gyroscope data, and calculated angles
float RateYaw, RateRoll, RatePitch;
float AccX, AccY, AccZ;
float Roll = 0, Pitch = 0, Yaw = 0;
float AngleRoll =0, AnglePitch = 0, AngleYaw = 0;
uint32_t LoopTimer;
float gz_bias = 0, gx_bias = 0, gy_bias = 0;

// Calibration variable
int sampleCounter = 0;
int numSamples = 2000;
float sumX = 0, sumY = 0, sumZ = 0;
unsigned long lastCalibrationTime = 0;
// unsigned long calibrationInterval = 60000; //60 sec

// IMU state manager variable
bool isMeasuring = false;
bool isCalibrating = false;
bool isConnected = false;

// Ble transfer variable
BLEService imuService("19B10000-E8F2-537E-4F6C-D104768A1214");
BLEFloatCharacteristic rollCharacteristic("19B10001-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEFloatCharacteristic pitchCharacteristic("19B10002-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEFloatCharacteristic yawCharacteristic("19B10003-E8F2-537E-4F6C-D104768A1214", BLENotify);
BLEStringCharacteristic debugMessageCharacteristic("19B10004-E8F2-537E-4F6C-D104768A1214", BLEWrite | BLENotify, 512); // 512 is the maximum size of the debug message.

String address;

void checkBleInput(BLEDevice &central){
  address = String(central.address());
}

void sendBleOutput(){
  // update ble
  rollCharacteristic.writeValue(AngleRoll);
  pitchCharacteristic.writeValue(AnglePitch);
  yawCharacteristic.writeValue(AngleYaw);
}

void DebugMessage(const String &message) {
  Serial.println(message);
  if (BLE.connected()) {
      debugMessageCharacteristic.writeValue(message);
  }
}


void startCalibrateImu(){
  sampleCounter++;
  isCalibrating = true;
  DebugMessage("Started callibration");
}

void continueCalibrateImu(){
  // Update acceleration
  float xGyro, yGyro, zGyro;
  IMU.readGyroscope(xGyro, yGyro, zGyro);
  sumX += xGyro * GyroSensitivity;
  sumY += yGyro * GyroSensitivity;
  sumZ += zGyro * GyroSensitivity;
  sampleCounter++;
  delay(2);

  // Update bias
  gx_bias = sumX / sampleCounter;
  gy_bias = sumY / sampleCounter;
  gz_bias = sumZ / sampleCounter;
}

void finishCalibrateImu(){
  //initiate and handle the callibration unprevented calibration termination
  gx_bias = sumX / sampleCounter;
  gy_bias = sumY / sampleCounter;
  gz_bias = sumZ / sampleCounter;
  sampleCounter = 0;
  sumX = 0, sumY = 0, sumZ = 0;
  isCalibrating = false;
  Roll = 0, Pitch = 0, Yaw = 0;
  lastCalibrationTime = millis();
  DebugMessage("Finished callibration");
  if(!isMeasuring){
    isMeasuring = true;
    DebugMessage("Started measuring");
  }
}

void readIMU(){
  float xGyro, yGyro, zGyro;

  IMU.readAcceleration(AccX, AccY, AccZ);
  IMU.readGyroscope(xGyro, yGyro, zGyro);

  RateYaw = zGyro * GyroSensitivity;
  RateRoll = xGyro * GyroSensitivity;
  RatePitch = yGyro * GyroSensitivity;

  // Continuous roll, pitch, and yaw update based on gyroscope data, corrected for bias
  float gx_dps = RateRoll - gx_bias;            // Calculate degrees per second
  float gy_dps = RatePitch - gy_bias;           // Calculate degrees per second
  float gz_dps = RateYaw - gz_bias;             // Calculate degrees per second
  float dt = (micros() - LoopTimer) / 1000000.0; // Calculate elapsed time in seconds

  Roll += gx_dps * dt;
  Pitch += gy_dps * dt;
  Yaw += gz_dps * dt;

  AngleRoll = 900 * Roll;
  AnglePitch = 900 * Pitch;
  AngleYaw = 900 * Yaw;
}

void printImuMeasurement(){
  // Print the measured and calculated angles to the Serial Monitor
  Serial.print("Connected to central: ");
  Serial.print(address);
  Serial.print(" | Roll Angle [°]: ");
  Serial.print(AngleRoll);
  Serial.print(" | Pitch Angle [°]: ");
  Serial.print(AnglePitch);
  Serial.print(" | Yaw Angle [°]: ");
  Serial.print(AngleYaw);
  Serial.print(" | AccX: ");
  Serial.print(AccX);
  Serial.print(" | AccY: ");
  Serial.print(AccY);
  Serial.print(" | AccZ: ");
  Serial.println(AccZ);
}

void setup()
{
  // Initialize serial communication
  Serial.begin(115200);
  while (!Serial);

  // Initialize the IMU
  if (!IMU.begin())
  {
      Serial.println("Failed to initialize IMU!");
      while (1)
          ;
  }

  // Initialize the BLE
  if (!BLE.begin())
  {
    Serial.println("Failed to starting BLE!");
    while (1)
      ;
  }

  // Setup ble
  BLE.setLocalName("IMU Sensor");
  BLE.setAdvertisedService(imuService);

  imuService.addCharacteristic(rollCharacteristic);
  imuService.addCharacteristic(pitchCharacteristic);
  imuService.addCharacteristic(yawCharacteristic);
  imuService.addCharacteristic(debugMessageCharacteristic);
  
  BLE.addService(imuService);

  rollCharacteristic.writeValue(0.0);  
  pitchCharacteristic.writeValue(0.0);
  yawCharacteristic.writeValue(0.0);

  BLE.advertise();

  Serial.println("IMU initialized successfully.");
  Serial.println("Bluetooth device active, waiting for connections...");
}

void loop()
{
  // Handle the connection status
  BLEDevice central = BLE.central();
  if(!isConnected){
    BLEDevice central = BLE.central();
    if(central.connected()){
      isConnected = true;
      Serial.println("Connected to device");
    }
  } else {
    if(!central.connected()){
      isConnected = false;
      Serial.println("Disconnected to device");
    }
  }

  if(isConnected){
    // Read accelerometer and gyroscope data
    if(!isMeasuring)
    {
      //initiate and handle the callibration state
      if(sampleCounter==0){
        startCalibrateImu();
      }
      else if((sampleCounter <= numSamples) && isCalibrating)
      {    
        continueCalibrateImu();      

      }else if((sampleCounter >= numSamples) && isCalibrating){
        finishCalibrateImu();
      }
      
    }else if (isCalibrating && isMeasuring){
      finishCalibrateImu();
    }else if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable()){
      LoopTimer = micros();

      checkBleInput(central);
      readIMU();
      sendBleOutput();
      printImuMeasurement();

      // Maintain a loop timing of 40 milliseconds
      while (micros() - LoopTimer < 40000);
      LoopTimer = micros(); // Reset loop timer

      // if(millis() - lastCalibrationTime >= calibrationInterval){
      //   isMeasuring = false;
      // }
    }
  } else if (isCalibrating){
    finishCalibrateImu();
    isMeasuring = false;
  } else if (isMeasuring){
    Serial.println("Measurement stopped ");
    isMeasuring = false;
  }
}
