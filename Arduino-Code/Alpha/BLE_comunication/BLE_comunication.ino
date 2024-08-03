#include <Arduino_LSM9DS1.h>
#include <ArduinoBLE.h>

// Define the BLE characteristic UUID
const char *bleServiceUuid = "19B10000-E8F2-537E-4F6C-D104768A1214";
const char *bleCharUuid = "19B10001-E8F2-537E-4F6C-D104768A1214";

// BLE Service
BLEService gyroService(bleServiceUuid);

// BLE Characteristic
BLECharacteristic gyroCharacteristic(bleCharUuid, BLERead | BLENotify, 12);

void setup() {
  Serial.begin(9600);
  while (!Serial);

  // Initialize the LSM9DS1 (Gyroscope)
  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }
  Serial.println("IMU initialized!");

  // Initialize BLE
  if (!BLE.begin()) {
    Serial.println("Failed to initialize BLE!");
    while (1);
  }
  Serial.println("BLE initialized!");

  // Set the device name and advertised service
  BLE.setLocalName("LED");
  BLE.setAdvertisedService(gyroService);

  // Add the characteristic to the service
  gyroService.addCharacteristic(gyroCharacteristic);

  // Add the service
  BLE.addService(gyroService);

  // Start advertising
  BLE.advertise();
  Serial.println("BLE advertising...");
}

void loop() {
  // Wait for a BLE central device to connect
  BLEDevice central = BLE.central();

  // If a central device is connected
  if (central) {
    Serial.print("Connected to central: ");
    Serial.println(central.address());

    while (central.connected()) {
      // Read gyroscope data
      float x, y, z;
      if (IMU.gyroscopeAvailable()) {
        IMU.readGyroscope(x, y, z);

        // Print gyroscope data to Serial
        Serial.print("X: ");
        Serial.print(x);
        Serial.print("Y: ");
        Serial.print(y);
        Serial.print("Z: ");
        Serial.println(z);

        // Convert the float values to a byte array
        byte gyroData[12];
        memcpy(gyroData, &x, 4);
        memcpy(gyroData + 4, &y, 4);
        memcpy(gyroData + 8, &z, 4);

        // Update the characteristic value
        gyroCharacteristic.writeValue(gyroData, 12);
        delay(100); // Delay to limit the data rate
      }
    }

    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
}