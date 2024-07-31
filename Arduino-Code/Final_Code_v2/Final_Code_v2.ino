// Header
// crossing the casme book
// hard to isolate a mouvement {need extra reference}
#include <ArduinoBLE.h>
#include <Arduino_LSM9DS1.h>

// Constants for gyro sensitivity based on expected range settings
float gyroSensitivity = 0.0175; // Adjusted to match the sensitivity for LSM9DS1 in degrees per second per LSB

// Global variables for accelerometer, gyroscope data, and calculated angles
float RateYaw, RateRoll, RatePitch;
float AccX, AccY, AccZ;
float Roll = 0, Pitch = 0, Yaw = 0;
float AngleRoll = 0, AnglePitch = 0, AngleYaw = 0;
uint32_t LoopTimer;
float gz_bias = 0, gx_bias = 0, gy_bias = 0;

// Calibration variable
int sampleCounter = 0;
int numSamples = 100;
float sumX = 0, sumY = 0, sumZ = 0;

// IMU state manager variable
bool isMeasuring = false;
bool isCalibrating = false;
bool isConnected = false;
bool isReseting = false;

// Ble transfer variable
BLEService imuService("19B10000-E8F2-537E-4F6C-D104768A1214");
BLEStringCharacteristic dataCharacteristic("19B10005-E8F2-537E-4F6C-D104768A1214", BLENotify | BLEWrite, 512);

String bleAddress;
String bleCommand = "None";
String bleDebugMessage = "None";
String bleStatus = "";

void checkBleInput(BLEDevice &central)
{
    // Check the address
    bleAddress = String(central.address());

    // Check the commands receive
    // bleCommand = "None";
    if (dataCharacteristic.written())
    {
        bleCommand = dataCharacteristic.value();
    }
}

void sendBleOutput()
{
    int YawInt = (int)AngleYaw;
    int PitchInt = (int)AnglePitch;
    int RollInt = (int)AngleRoll;

    String data = String(YawInt) + "," + String(PitchInt) + "," + String(RollInt)+ "," + bleStatus;
    // update ble
    dataCharacteristic.writeValue(data);
}

void startCalibrateImu()
{
    sampleCounter++;
    isCalibrating = true;
    bleDebugMessage += " Started callibration -";
    bleStatus = " Started callibration ";
}

void continueCalibrateImu()
{
    // Update acceleration
    float xGyro, yGyro, zGyro;
    IMU.readGyroscope(xGyro, yGyro, zGyro);
    sumX += xGyro * gyroSensitivity;
    sumY += yGyro * gyroSensitivity;
    sumZ += zGyro * gyroSensitivity;
    sampleCounter++;
    delay(2);

    // Update bias
    gx_bias = sumX / sampleCounter;
    gy_bias = sumY / sampleCounter;
    gz_bias = sumZ / sampleCounter;
}

void finishCalibrateImu()
{
    // initiate and handle the callibration unprevented calibration termination
    gx_bias = sumX / sampleCounter;
    gy_bias = sumY / sampleCounter;
    gz_bias = sumZ / sampleCounter;
    sampleCounter = 0;
    sumX = 0, sumY = 0, sumZ = 0;
    isCalibrating = false;
    Roll = 0, Pitch = 0, Yaw = 0;
    bleDebugMessage += " Finished callibration -";
    bleStatus = " Finished callibration ";

    if (!isMeasuring)
    {
        isMeasuring = true;
        bleDebugMessage += " Started measuring -";
        bleStatus = " Started measuring ";
    }
}

void resetMeasuring()
{
    // Set values to zero
    Roll = 0, Pitch = 0, Yaw = 0;
    bleDebugMessage += " Resetted Angles -";
    bleStatus = " Resetted Angles ";
}

void readIMU()
{
    float xGyro, yGyro, zGyro;

    IMU.readAcceleration(AccX, AccY, AccZ);
    IMU.readGyroscope(xGyro, yGyro, zGyro);

    RateYaw = zGyro * gyroSensitivity;
    RateRoll = xGyro * gyroSensitivity;
    RatePitch = yGyro * gyroSensitivity;

    // Continuous roll, pitch, and yaw update based on gyroscope data, corrected for bias
    float gx_dps = RateRoll - gx_bias;             // Calculate degrees per second
    float gy_dps = RatePitch - gy_bias;            // Calculate degrees per second
    float gz_dps = RateYaw - gz_bias;              // Calculate degrees per second
    float dt = (micros() - LoopTimer) / 1000000.0; // Calculate elapsed time in seconds

    Roll += gx_dps * dt;
    Pitch += gy_dps * dt;
    Yaw += gz_dps * dt;

    AngleRoll = 900 * Roll;
    AnglePitch = 900 * Pitch;
    AngleYaw = 900 * Yaw;
}

void printImuMeasurement()
{
    // Prepare data to print
    String bleAddressPrinter = "Connected to central: " + String(bleAddress);
    String AngleRollPrinter = " | Roll Angle [°]: " + String(AngleRoll);
    String AnglePitchPrinter = " | Pitch Angle [°]: " + String(AnglePitch);
    String AngleYawPrinter = " | Yaw Angle [°]: " + String(AngleYaw);
    String bleCommandPrinter = " | Ble Command: " + String(bleCommand);
    String bleDebugMessagePrinter = " |Ble Debug: " + String(bleDebugMessage);

    // Concatonate the message
    String imuDataPrinter = bleAddressPrinter + AngleRollPrinter + AnglePitchPrinter + AngleYawPrinter + bleCommandPrinter + bleDebugMessagePrinter;

    // Printint the entire data
    Serial.println(imuDataPrinter);
}

void setup()
{
    // Initialize serial communication
    Serial.begin(9600);
    while (!Serial)
        ;

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
    imuService.addCharacteristic(dataCharacteristic);
    BLE.addService(imuService);
    dataCharacteristic.writeValue("0,0,0");

    BLE.advertise();
    Serial.println("IMU initialized successfully.");
    Serial.println("Bluetooth device active, waiting for connections...");
}

void loop()
{
    // Handle the connection status
    BLEDevice central = BLE.central();
    if (central.connected())
    {
        if (!isConnected)
        {
            isConnected = true;
            Serial.println("Connected to device");
        }

        // Check if new accelerometer and gyroscope data is available
        if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable())
        {
            LoopTimer = micros(); // Update LoopTimer at the start of data handling

            if (!isMeasuring)
            {
                // Start calibration if not measuring
                if (sampleCounter == 0)
                {
                    startCalibrateImu();
                }
                else if ((sampleCounter <= numSamples) && isCalibrating)
                {
                    continueCalibrateImu();
                }
                else if ((sampleCounter >= numSamples) && isCalibrating)
                {
                    finishCalibrateImu();
                }
            }
            else
            {
                checkBleInput(central);

                // Execute BLE command
                if (bleCommand == "Reset data")
                {
                    bleDebugMessage += " Command received [" + String(bleCommand) + "] -";
                    bleStatus = " Command received [" + String(bleCommand) + "] ";
                    resetMeasuring();
                    bleDebugMessage += "Reset data done -";
                    bleStatus = "Reset data done";
                    bleCommand = "None";
                }

                readIMU();
                sendBleOutput();
                printImuMeasurement();
            }
        }
    }
    else if (isConnected)
    {
      isConnected = false;
      bleDebugMessage += "Disconnected from device -";
      bleStatus = "Disconnected from device -";
      Serial.println("Disconnected from device");
    }

    // Maintain a loop timing of 40 milliseconds
    while (micros() - LoopTimer < 40000)
        ;
}