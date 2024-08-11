import numpy as np
import matplotlib.pyplot as plt

# Constants for gyro sensitivity
gyroSensitivity = 0.0175


# Simulation of reading sensor data from files
def read_sensor_data(file_name):
    try:
        # Reading the files assuming that they contain headers or unique formatting shown in the screenshots
        if "gyroscope" in file_name or "acceleration" in file_name:
            data = np.loadtxt(
                file_name, delimiter=" ", usecols=(1, 3, 5)
            )  # Adjust column indices based on your file format
        elif "dt" in file_name:
            data = np.loadtxt(
                file_name, delimiter=":", usecols=[1]
            )  # Adjust column index for dt values
        return data
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
def process_imu_data(acc_data, gyro_data, dt_data):
    # Calibrate gyros
    gx_bias, gy_bias, gz_bias = calibrate_gyros(gyro_data)
    rolls, pitches, yaws = [], [], []
    roll, pitch, yaw = 0, 0, 0

    # Ensure dt_data is in seconds if it's not already
    dt_data = dt_data / 1000  # Uncomment this line if dt values are in milliseconds

    # Process each set of data
    for i, ((acc, gyro, dt)) in enumerate(zip(acc_data, gyro_data, dt_data)):
        # Update angles based on gyro data, corrected for bias
        gx_dps = (gyro[0] * gyroSensitivity - gx_bias) * dt
        gy_dps = (gyro[1] * gyroSensitivity - gy_bias) * dt
        gz_dps = (gyro[2] * gyroSensitivity - gz_bias) * dt

        roll += gx_dps
        pitch += gy_dps
        yaw += gz_dps

        # Store each angle for plotting
        rolls.append(roll)
        pitches.append(pitch)
        yaws.append(yaw)

    return rolls, pitches, yaws


def main():
    # TODO: Adjust file paths as needed
    acc_data = read_sensor_data(
        "Arduino-Code/Computer_simulation/Data_container/pitch/acceleration.txt"
    )
    gyro_data = read_sensor_data(
        "Arduino-Code/Computer_simulation/Data_container/pitch/gyroscope.txt"
    )
    dt_data = read_sensor_data(
        "Arduino-Code/Computer_simulation/Data_container/pitch/dt.txt"
    )

    if acc_data is not None and gyro_data is not None and dt_data is not None:
        rolls, pitches, yaws = process_imu_data(acc_data, gyro_data, dt_data)

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
