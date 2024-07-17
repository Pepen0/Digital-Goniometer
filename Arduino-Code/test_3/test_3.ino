#include <Arduino_LSM9DS1.h>

// Constants for gyro sensitivity based on expected range settings
const float GyroSensitivity = 0.87333333333;

// Global variables for accelerometer, gyroscope data, and calculated angles
float RateYaw;
float AccX, AccY, AccZ;
float AngleRoll, AnglePitch, yaw = 0;
uint32_t LoopTimer;
float gz_bias = 0;

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

void setup() {
  // Initialize serial communication
  Serial.begin(115200);
  while (!Serial);

  // Initialize the IMU
  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }

  Serial.println("IMU initialized successfully.");

  // Calculate gyro Z-axis bias for accurate yaw calculation
  gz_bias = calculate_gz_bias();
  LoopTimer = micros();
}

void loop() {
  // Read accelerometer and gyroscope data
  if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable()) {
    float xGyro, yGyro, zGyro;

    IMU.readAcceleration(AccX, AccY, AccZ);
    IMU.readGyroscope(xGyro, yGyro, zGyro);

    RateYaw = zGyro / GyroSensitivity;

    // Calculate Roll and Pitch based on accelerometer data
    AngleRoll = atan2(AccY, sqrt(AccX * AccX + AccZ * AccZ)) * (180.0 / PI);
    AnglePitch = atan2(-AccX, AccZ) * (180.0 / PI);

    // Continuous yaw update based on gyroscope data, corrected for bias
    float gz_dps = RateYaw - gz_bias;              // Calculate degrees per second
    float dt = (micros() - LoopTimer) / 1000000.0; // Calculate elapsed time in seconds
    yaw += gz_dps * dt;
    LoopTimer = micros(); // Reset loop timer

    // Print the measured and calculated angles to the Serial Monitor
    Serial.print("Roll Angle [°]: ");
    Serial.print(AngleRoll);
    Serial.print(" | Pitch Angle [°]: ");
    Serial.print(AnglePitch);
    Serial.print(" | Yaw Angle [°]: ");
    Serial.println(yaw);
  }

  // Add a short delay to avoid flooding the serial monitor
  delay(100);
}
