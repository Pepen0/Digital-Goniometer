import serial
import time

# Configure the serial connection
ser = serial.Serial("/dev/cu.usbmodem141201", 9600)
time.sleep(2)

# Open files for writing
accel_file = open(
    "Arduino-Code/Computer_simulation/Data_container/roll/acceleration.txt", "w"
)
gyro_file = open(
    "Arduino-Code/Computer_simulation/Data_container/roll/gyroscope.txt", "w"
)
dt_file = open("Arduino-Code/Computer_simulation/Data_container/roll/dt.txt", "w")

try:
    while True:
        line = ser.readline().decode("utf-8").strip()  # Read a line of serial data
        print(line)  # Print to console (optional)

        # Parse the line for accelerometer, gyroscope, and dt data
        if "AccX" in line and "GyroX" in line and "dt" in line:
            parts = line.split("|")
            accel_data = parts[0].strip()
            gyro_data = parts[1].strip()
            dt_data = parts[2].strip()

            accel_file.write(accel_data + "\n")
            gyro_file.write(gyro_data + "\n")
            dt_file.write(dt_data + "\n")

except KeyboardInterrupt:
    print("Exiting...")

finally:
    ser.close()
    accel_file.close()
    gyro_file.close()
    dt_file.close()
