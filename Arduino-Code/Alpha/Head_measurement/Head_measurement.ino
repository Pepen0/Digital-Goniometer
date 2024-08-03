// ------------------------------- Usefull Link -------------------------------------
// Arduino: https://www.arduino.cc
// Wire lib: https://www.arduino.cc/reference/en/language/functions/communication/wire/
//           https://github.com/esp8266/Arduino/tree/master/libraries/Wire
// LSM9DS1 Datasheet: https://html.alldatasheet.com/html-pdf/1242998/STMICROELECTRONICS/LSM9DS1/6815/3/LSM9DS1.html
// APDS-9960 Datasheet: https://www.alldatasheet.com/datasheet-pdf/pdf/918047/AVAGO/APDS-9960.html

// ------------------------------- feedback -------------------------------------

// angle x: low accuracy, drif every 3 seconds
// angle y: low accuracy, drif every 3 seconds
// angle z: no accuracy, drif every 3 seconds
// light: No measurement from sensor

// Header 
#include <Arduino.h>
#include <Arduino_LSM9DS1.h>
#include <Wire.h>


// Setup Global variables 
#define APDS9960_I2C_ADDRESS   0x39
#define APDS9960_ENABLE        0x80
#define APDS9960_ATIME         0x81
#define APDS9960_CONTROL       0x8F
#define APDS9960_CDATAL        0x94
#define APDS9960_CDATAH        0x95

struct Gyroscope {
    float x = 0.0, y = 0.0, z = 0.0; 
    float storedX = 0.0, storedY = 0.0, storedZ = 0.0; 
    float offsetX = 0.0, offsetY = 0.0, offsetZ = 0.0; 
    float angle = 0.0; 
    const float alpha = 0.98; 
} gyro;

unsigned long lastTime = 0, lastLightChangeTime = 0;
float lastLightLevel;


// Function prototypes
void setupAPDS9960();
void writeRegister(uint8_t addr, uint8_t reg, uint8_t value);
uint16_t readClearChannel();
float readLightIntensity();
void calibrateGyroscope();
void performBiasCorrection();


// Arduino setup function
void setup() {
    Serial.begin(9600);
    while (!Serial);
    setupAPDS9960();
    if (!IMU.begin()) {
        Serial.println("Failed to initialize IMU!");
        while (1);
    }
    lastLightLevel = readLightIntensity();
    lastLightChangeTime = millis();
    calibrateGyroscope();
    Serial.println("Sensors initialized and calibrated.");
}


// Arduino loop function
void loop() {
    float currentLightLevel = readLightIntensity();
    unsigned long currentTime = millis();
    float dt = (currentTime - lastTime) / 1000.0;

    // Handle lighting change and bias correction when the lighting changes
    if (fabs(currentLightLevel - lastLightLevel) > 5) { 
        lastLightChangeTime = currentTime;
        lastLightLevel = currentLightLevel;
    }
    if (currentTime - lastLightChangeTime >= 300) {
        performBiasCorrection();
    }

    // Read and process sensor data
    float ax, ay, az, gx, gy, gz;
    IMU.readAcceleration(ax, ay, az);
    IMU.readGyroscope(gx, gy, gz);

    gx -= gyro.offsetX;
    gy -= gyro.offsetY;
    gz -= gyro.offsetZ;

    gyro.x += gx * dt;
    gyro.y += gy * dt;
    gyro.z += gz * dt;

    float angleAccel = atan2(ay, sqrt(ax * ax + az * az)) * 180 / PI;
    gyro.angle = gyro.alpha * (gyro.angle + gy * dt) + (1 - gyro.alpha) * angleAccel;

    // print result
    Serial.print("Gyro Angle X: "); Serial.print(gyro.x);
    Serial.print(" Y: "); Serial.print(gyro.y);
    Serial.print(" Z: "); Serial.print(gyro.z);
    Serial.print(" Current Light: "); Serial.println(currentLightLevel);

    lastTime = currentTime;
}

// configure the APDS9960 sensor.
void setupAPDS9960() {
    Wire.begin();
    writeRegister(APDS9960_I2C_ADDRESS, APDS9960_ATIME, 0xD6); 
    writeRegister(APDS9960_I2C_ADDRESS, APDS9960_CONTROL, 0x02); 
    writeRegister(APDS9960_I2C_ADDRESS, APDS9960_ENABLE, 0x03); 
}
// ccontrol the APDS9960 sensor.
void writeRegister(uint8_t addr, uint8_t reg, uint8_t value) {
    Wire.beginTransmission(addr);
    Wire.write(reg);
    Wire.write(value);
    Wire.endTransmission();
}


// manage reading data from the light sensor.
uint16_t readClearChannel() {
    Wire.beginTransmission(APDS9960_I2C_ADDRESS);
    Wire.write(APDS9960_CDATAL);
    Wire.endTransmission(false);
    Wire.requestFrom(APDS9960_I2C_ADDRESS, 2);
    if (Wire.available() == 2) {
        uint8_t lsb = Wire.read();
        uint8_t msb = Wire.read();
        return ((uint16_t)msb << 8) | lsb;
    }
    return 0;
}
float readLightIntensity() {
    uint16_t clearLight = readClearChannel();
    return static_cast<float>(clearLight);
}


// Adjusting for any sensor drift.
void calibrateGyroscope() {
    float gx, gy, gz;
    for (int i = 0; i < 100; i++) {
        if (IMU.gyroscopeAvailable()) {
            IMU.readGyroscope(gx, gy, gz);
            gyro.offsetX += gx;
            gyro.offsetY += gy;
            gyro.offsetZ += gz;
            delay(10);
        }
    }

    gyro.offsetX /= 100;
    gyro.offsetY /= 100;
    gyro.offsetZ /= 100;
}


// Adjusting for any sensor bias
void performBiasCorrection() {
    float gx, gy, gz;
    float tempOffsetX = 0.0, tempOffsetY = 0.0, tempOffsetZ = 0.0;
    for (int i = 0; i < 10; i++) {
        if (IMU.gyroscopeAvailable()) {
            IMU.readGyroscope(gx, gy, gz);
            tempOffsetX += gx;
            tempOffsetY += gy;
            tempOffsetZ += gz;
            delay(10);
        }
    }

    tempOffsetX /= 10;
    tempOffsetY /= 10;
    tempOffsetZ /= 10;

    gyro.offsetX = (gyro.offsetX + tempOffsetX) / 2;
    gyro.offsetY = (gyro.offsetY + tempOffsetY) / 2;
    gyro.offsetZ = (gyro.offsetZ + tempOffsetZ) / 2;
}
