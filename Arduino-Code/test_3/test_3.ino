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

// Function to calculate and return gyroscope biases
void calculate_gyro_bias()
{
    int numSamples = 5000;
    float sumX = 0, sumY = 0, sumZ = 0;
    for (int i = 0; i < numSamples; i++)
    {
        float xGyro, yGyro, zGyro;
        IMU.readGyroscope(xGyro, yGyro, zGyro);
        sumX += xGyro * GyroSensitivity;
        sumY += yGyro * GyroSensitivity;
        sumZ += zGyro * GyroSensitivity;
        delay(2);
    }
    gx_bias = sumX / numSamples;
    gy_bias = sumY / numSamples;
    gz_bias = sumZ / numSamples;
}

void setup()
{
    // Initialize serial communication
    Serial.begin(115200);
    while (!Serial)
        ;

    // Initialize the IMU
    if (!IMU.begin())
    {
        Serial.println("Failed to initialize IMU!");
        while (1)
            ;
    }

    Serial.println("IMU initialized successfully.");

    // Calculate gyro biases for accurate angle calculations
    calculate_gyro_bias();
    LoopTimer = micros();
}

void loop()
{
    // Read accelerometer and gyroscope data
    if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable())
    {
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

        // Print the measured and calculated angles to the Serial Monitor
        Serial.print("Roll Angle [°]: ");
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

        // Reset loop timer
        LoopTimer = micros();
    }

    // Maintain a loop timing of 40 milliseconds
    while (micros() - LoopTimer < 40000)
        ;
    LoopTimer = micros(); // Reset loop timer
}
