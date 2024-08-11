import numpy as np
import matplotlib.pyplot as plt

# Constants for gyro sensitivity
gyroSensitivity = 0.0175


# Simulated reading function
def read_sensor_data(file_name):
    try:
        data = np.loadtxt(file_name, delimiter=",")
        return data
    except Exception as e:
        print(f"Failed to read {file_name}: {e}")
        return None


# Kalman filter implementation
def kalman_1d(state, uncertainty, input, measurement):
    dt = 0.004  # time step
    process_noise = dt * dt * 16  # Process noise variance
    measurement_noise = 9  # Measurement noise variance

    # Prediction update
    state += dt * input
    uncertainty += process_noise

    # Measurement update
    gain = uncertainty / (uncertainty + measurement_noise)
    state += gain * (measurement - state)
    uncertainty *= 1 - gain

    return state, uncertainty


# Main function to process and plot data
def main():
    # TODO Add the proper data container path before running
    accelerometer_file = "accelerometer_data.txt"
    gyroscope_file = "gyroscope_data.txt"
    magnetometer_file = "magnetometer_data.txt"

    acc_data = read_sensor_data(accelerometer_file)
    gyro_data = read_sensor_data(gyroscope_file)
    mag_data = read_sensor_data(magnetometer_file)

    # Initial conditions for Kalman filter
    kalman_roll = kalman_pitch = 0
    uncertainty_roll = uncertainty_pitch = 4
    yaw = 0
    gz_bias = calculate_gz_bias(gyro_data)

    # Lists to hold data for plotting
    rolls, pitches, yaws = [], [], []
    last_time = 0

    if acc_data is not None and gyro_data is not None and mag_data is not None:
        for (
            (xAcc, yAcc, zAcc),
            (xGyro, yGyro, zGyro),
            (xMag, yMag, zMag),
            timestamp,
        ) in zip(acc_data, gyro_data, mag_data, np.arange(len(acc_data))):
            # Calculate roll and pitch based on sensor data
            angle_roll = np.arctan2(yAcc, np.sqrt(xAcc**2 + zAcc**2)) * (180 / np.pi)
            angle_pitch = -np.arctan2(xAcc, np.sqrt(yAcc**2 + zAcc**2)) * (180 / np.pi)

            # Continuous yaw update based on gyroscopic data
            dt = (timestamp - last_time) / 1000.0
            gz_dps = (zGyro / gyroSensitivity) - gz_bias
            yaw += gz_dps * dt
            last_time = timestamp

            # Kalman filter for roll and pitch
            kalman_roll, uncertainty_roll = kalman_1d(
                kalman_roll, uncertainty_roll, xGyro, angle_roll
            )
            kalman_pitch, uncertainty_pitch = kalman_1d(
                kalman_pitch, uncertainty_pitch, yGyro, angle_pitch
            )

            # Store data for plotting
            rolls.append(kalman_roll)
            pitches.append(kalman_pitch)
            yaws.append(yaw)

    # Plotting the filtered orientation data
    plt.figure(figsize=(10, 7))
    plt.plot(rolls, label="Roll")
    plt.plot(pitches, label="Pitch")
    plt.plot(yaws, label="Yaw")
    plt.title("Orientation Data Over Time")
    plt.xlabel("Time (index)")
    plt.ylabel("Degrees")
    plt.legend()
    plt.show()


def calculate_gz_bias(gyro_data):
    return np.mean(gyro_data[:, 2] / gyroSensitivity)


if __name__ == "__main__":
    main()
