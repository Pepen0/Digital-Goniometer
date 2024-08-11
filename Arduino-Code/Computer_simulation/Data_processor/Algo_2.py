import numpy as np
import matplotlib.pyplot as plt


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
    dt = 0.004
    process_noise = 0.004 * 0.004 * 16
    measurement_noise = 9

    # Prediction update
    state += dt * input
    uncertainty += dt * dt * process_noise

    # Measurement update
    gain = uncertainty / (uncertainty + measurement_noise)
    state += gain * (measurement - state)
    uncertainty *= 1 - gain

    return state, uncertainty


# Main function to process data
def main():
    # TODO Add the proper data container path before running
    accelerometer_file = "accelerometer_data.txt"
    gyroscope_file = "gyroscope_data.txt"
    magnetometer_file = "magnetometer_data.txt"

    acc_data = read_sensor_data(accelerometer_file)
    gyro_data = read_sensor_data(gyroscope_file)
    mag_data = read_sensor_data(magnetometer_file)

    # Kalman filter initial states
    kalman_roll = kalman_pitch = kalman_yaw = 0
    uncertainty_roll = uncertainty_pitch = uncertainty_yaw = 4

    # Store filtered results for plotting
    filtered_rolls, filtered_pitchs, filtered_yaws = [], [], []

    if acc_data is not None and gyro_data is not None and mag_data is not None:
        for (xAcc, yAcc, zAcc), (xGyro, yGyro, zGyro), (xMag, yMag, zMag) in zip(
            acc_data, gyro_data, mag_data
        ):
            # Calculate angles based on sensor data
            angle_roll = np.arctan2(yAcc, np.sqrt(xAcc**2 + zAcc**2)) * (180 / np.pi)
            angle_pitch = -np.arctan2(xAcc, np.sqrt(yAcc**2 + zAcc**2)) * (180 / np.pi)
            angle_yaw = np.arctan2(-yMag, xMag) * (180 / np.pi)

            # Apply Kalman filter for each angle
            kalman_roll, uncertainty_roll = kalman_1d(
                kalman_roll, uncertainty_roll, xGyro, angle_roll
            )
            kalman_pitch, uncertainty_pitch = kalman_1d(
                kalman_pitch, uncertainty_pitch, yGyro, angle_pitch
            )
            kalman_yaw, uncertainty_yaw = kalman_1d(
                kalman_yaw, uncertainty_yaw, zGyro, angle_yaw
            )

            # Store results for plotting
            filtered_rolls.append(kalman_roll)
            filtered_pitchs.append(kalman_pitch)
            filtered_yaws.append(kalman_yaw)

    # Plotting the filtered orientation data
    plt.figure(figsize=(10, 7))
    plt.plot(filtered_rolls, label="Roll")
    plt.plot(filtered_pitchs, label="Pitch")
    plt.plot(filtered_yaws, label="Yaw")
    plt.title("Orientation Data Over Time")
    plt.xlabel("Time (index)")
    plt.ylabel("Degrees")
    plt.legend()
    plt.show()


if __name__ == "__main__":
    main()
