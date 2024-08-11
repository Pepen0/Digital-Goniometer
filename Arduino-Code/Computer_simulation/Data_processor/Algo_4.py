import numpy as np
import matplotlib.pyplot as plt

# Constants for gyro sensitivity
gyroSensitivity = 0.0175


# Simulation of reading sensor data from files
def read_sensor_data(file_name):
    try:
        return np.loadtxt(file_name, delimiter=",")
    except Exception as e:
        print(f"Error reading {file_name}: {e}")
        return None


# Function to simulate gyro calibration
def calibrate_gyros(gyro_data, num_samples=100):
    if len(gyro_data) < num_samples:
        print("Not enough data for calibration")
        return None, None, None
    # Calculate mean bias for each gyro axis
    gx_bias = np.mean(gyro_data[:num_samples, 0]) * gyroSensitivity
    gy_bias = np.mean(gyro_data[:num_samples, 1]) * gyroSensitivity
    gz_bias = np.mean(gyro_data[:num_samples, 2]) * gyroSensitivity
    return gx_bias, gy_bias, gz_bias


# Main processing function
def process_imu_data(acc_data, gyro_data):
    # Calibrate gyros
    gx_bias, gy_bias, gz_bias = calibrate_gyros(gyro_data)
    rolls, pitches, yaws = [], [], []
    roll, pitch, yaw = 0, 0, 0

    # Simulate continuous reading and updating of angles
    for i, (acc, gyro) in enumerate(zip(acc_data, gyro_data)):
        dt = 0.04  # assuming a fixed time step of 40 milliseconds
        if i == 0:
            continue

        # Update angles based on gyro data, corrected for bias
        gx_dps = gyro[0] * gyroSensitivity - gx_bias
        gy_dps = gyro[1] * gyroSensitivity - gy_bias
        gz_dps = gyro[2] * gyroSensitivity - gz_bias

        roll += gx_dps * dt
        pitch += gy_dps * dt
        yaw += gz_dps * dt

        # Store each angle for plotting
        rolls.append(roll)
        pitches.append(pitch)
        yaws.append(yaw)

    return rolls, pitches, yaws


def main():
    # TODO Add the proper data container path before running
    acc_data = read_sensor_data("accelerometer_data.txt")
    gyro_data = read_sensor_data("gyroscope_data.txt")

    if acc_data is not None and gyro_data is not None:
        rolls, pitches, yaws = process_imu_data(acc_data, gyro_data)

        # Plotting the data
        plt.figure(figsize=(10, 7))
        plt.plot(rolls, label="Roll")
        plt.plot(pitches, label="Pitch")
        plt.plot(yaws, label="Yaw")
        plt.title("Orientation Data Over Time")
        plt.xlabel("Time (index)")
        plt.ylabel("Degrees")
        plt.legend()
        plt.show()


if __name__ == "__main__":
    main()
