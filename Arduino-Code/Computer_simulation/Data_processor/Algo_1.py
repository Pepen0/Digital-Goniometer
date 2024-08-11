import numpy as np
import matplotlib.pyplot as plt


# Simulated reading functions
def read_sensor_data(file_name):
    try:
        data = np.loadtxt(file_name, delimiter=",")
        return data
    except Exception as e:
        print(f"Failed to read {file_name}: {e}")
        return None


# Calculate orientation from accelerometer and magnetometer data
def calculate_orientation(ax, ay, az, mx, my, mz):
    roll = np.arctan2(ay, az) * (180 / np.pi)
    pitch = np.arctan2(-ax, np.sqrt(ay**2 + az**2)) * (180 / np.pi)

    # Adjust magnetometer readings based on roll and pitch
    adjusted_mx = mx * np.cos(pitch) + mz * np.sin(pitch)
    adjusted_my = (
        mx * np.sin(roll) * np.sin(pitch)
        + my * np.cos(roll)
        - mz * np.sin(roll) * np.cos(pitch)
    )
    yaw = np.arctan2(-adjusted_my, adjusted_mx) * (180 / np.pi)
    return roll, pitch, yaw


# Main function to process and plot data
def main():
    # TODO Add the proper data container path before running
    accelerometer_file = "accelerometer_data.txt"
    gyroscope_file = "gyroscope_data.txt"
    magnetometer_file = "magnetometer_data.txt"
    light_file = "light_data.txt"

    acc_data = read_sensor_data(accelerometer_file)
    gyro_data = read_sensor_data(gyroscope_file)
    mag_data = read_sensor_data(magnetometer_file)
    light_levels = read_sensor_data(light_file)

    # Lists to store orientation data for plotting
    rolls, pitches, yaws = [], [], []

    if (
        acc_data is not None
        and gyro_data is not None
        and mag_data is not None
        and light_levels is not None
    ):
        for acc, gyro, mag, light in zip(acc_data, gyro_data, mag_data, light_levels):
            ax, ay, az = acc
            mx, my, mz = mag

            roll, pitch, yaw = calculate_orientation(ax, ay, az, mx, my, mz)
            rolls.append(roll)
            pitches.append(pitch)
            yaws.append(yaw)

    # Plotting the orientation data
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
