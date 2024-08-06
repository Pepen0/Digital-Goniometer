#include <Arduino_LSM9DS1.h>

// Setup status LED
#define LED_RED 22
#define LED_GREEN 23
#define LED_BLUE 24

// Constants for gyro sensitivity based on expected range settings
float gyroSensitivity = 0.0175; // Adjusted to match the sensitivity for LSM9DS1 in degrees per second per LSB

// Global variables for accelerometer and gyroscope data
float AccX, AccY, AccZ;
float xGyro, yGyro, zGyro;
uint32_t LoopTimer = 0;
float dt = 0;

void setup()
{
  // Initialize LED pins
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

  // Turn off all LED colors initially
  digitalWrite(LED_RED, HIGH);
  digitalWrite(LED_GREEN, HIGH);
  digitalWrite(LED_BLUE, HIGH);

  // Initialize serial communication
  Serial.begin(9600);
  while (!Serial)
    ; // Wait for serial port to connect (for native USB)

  // Initialize the IMU
  if (!IMU.begin())
  {
    Serial.println("Failed to initialize IMU!");
    digitalWrite(LED_RED, LOW); // Red LED for failure
    while (1)
      ; // Stop here
  }
  Serial.println("IMU initialized successfully.");

  // Indicate setup is complete
  digitalWrite(LED_GREEN, LOW); // Green LED for success

  // Initialize loop timer
  LoopTimer = micros();
}

void loop()
{
  // Check if new accelerometer and gyroscope data is available
  if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable())
  {
    // Calculate elapsed time (dt) in seconds
    uint32_t currentTime = micros();
    dt = (currentTime - LoopTimer) / 1000000.0;
    LoopTimer = currentTime;

    IMU.readAcceleration(AccX, AccY, AccZ);
    IMU.readGyroscope(xGyro, yGyro, zGyro);

    // Print the acceleration, gyroscope, and dt data to the Serial Monitor
    Serial.print("AccX: ");
    Serial.print(AccX);
    Serial.print(" AccY: ");
    Serial.print(AccY);
    Serial.print(" AccZ: ");
    Serial.print(AccZ);
    Serial.print(" | GyroX: ");
    Serial.print(xGyro);
    Serial.print(" GyroY: ");
    Serial.print(yGyro);
    Serial.print(" GyroZ: ");
    Serial.print(zGyro);
    Serial.print(" | dt: ");
    Serial.println(dt, 6); // Print dt with 6 decimal places
  }
}
