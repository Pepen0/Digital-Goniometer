package com.example.goniometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

//This class will Take care of communication between arduino and our application
public class BLEManager {

    private static final String TAG = "IMU Sensor"; //Tag for logging purposes

    //UUIDs for the bluetooth service and characteristics
    private static final UUID SERVICE_UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("19B10005-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Context context;
    private BluetoothGattCharacteristic characteristic;
    private DataCallback dataCallback;
    private ConnectionCallback connectionCallback;
    private boolean isConnected = false; // Track connection state

    public interface DataCallback {

        //Call back for receiving data
        void onDataReceived(int Yaw, int Pitch, int Roll, String Debug);
    }

    public interface ConnectionCallback {

        //call back for connection state changes
        void onConnected();

        void onDisconnected();
    }

    private static BLEManager instance;

    public static BLEManager getInstance() {
        //Returns the instance of BLEManager
        if (instance == null) {
            throw new IllegalStateException("BLEManager not initialized");
        }
        return instance;
    }

    public BLEManager(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        instance = this;
    }

    public void setDataCallback(DataCallback callback) {
        this.dataCallback = callback;
    }

    public void setConnectionCallback(ConnectionCallback callback) {
        this.connectionCallback = callback;
    }

    public void connectToDevice(String deviceAddress) {
        //initiate a connection to the BLE using the bluetooth physical address entered by the user
        // to provide a secure connection
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {


        // Handle the bluetooth changes and display it for the user
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                bluetoothGatt.discoverServices();
                isConnected = true; // Update connection state
                if (connectionCallback != null) {
                    connectionCallback.onConnected();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                isConnected = false; // Update connection state
                if (connectionCallback != null) {
                    connectionCallback.onDisconnected();
                }
            }
        }
        //Handle service discovery and retrieve the data/notifications sent from arduino
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                } else {
                    Log.w(TAG, "Service not found: " + SERVICE_UUID.toString());
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        //Handle characteristic value updates
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                ByteBuffer buffer = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN);
                String data = StandardCharsets.UTF_8.decode(buffer).toString();

                try {
                    if (dataCallback != null) {
                        String[] Variables = data.split(",");

                        //This will Parse the data and split the retrieved string and removing "," from the string
                        for (int i = 0; i < Variables.length; i++) {
                            Variables[i] = Variables[i].replaceAll(",", "");
                        }
                        if (Variables.length == 4 || Variables.length == 3) {
                            int LiveYaw = 0;
                            int LivePitch = 0;
                            int LiveRoll = 0;
                            String DebugMessage = "";

                            try {
                                LiveYaw = Integer.parseInt(Variables[0]);
                            } catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Yaw Value: " + Variables[0]);
                            }
                            try {
                                LivePitch = Integer.parseInt(Variables[1]);
                            } catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Pitch Value: " + Variables[1]);
                            }
                            try {
                                LiveRoll = Integer.parseInt(Variables[2]);
                            } catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Roll Value: " + Variables[2]);
                            }
                            try {
                                if (Variables.length == 4) {
                                    DebugMessage = Variables[3].trim();
                                    Log.d(TAG, "Debug message: " + DebugMessage);
                                }
                            } catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Debug Message Value: " + Variables[3].trim());
                            }
                            dataCallback.onDataReceived(LiveYaw, LivePitch, LiveRoll, DebugMessage);
                        }
                    } else {
                        Log.d("The String does not have 3 values", data);
                    }
                } catch (NumberFormatException e) {
                    Log.d("Error parsing integers: ", e.getMessage());
                }
            }
        }
// Handle the result of a characteristic write operation
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Arduino resetting data");
            } else {
                Log.e(TAG, "Failed to send data to Arduino. Status: " + 0);
            }
        }
    };
    //Disconnect from Gatt server and close the connection
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            isConnected = false; // Update connection state
        }
    }

    // Send a command to the Arduino to reset data
    //This will be called and provided with String "Reset data" whenever we start a new measurement
    public void sendDataToArduino(String data) {
        if (characteristic != null) {
            characteristic.setValue(data);
            boolean success = bluetoothGatt.writeCharacteristic(characteristic);
            if (success) {
                Log.d(TAG, "Data sent successfully: " + data);
            } else {
                Log.e(TAG, "Failed to write characteristic");
            }
        } else {
            Log.e(TAG, "BluetoothGatt or dataCharacteristic is null. Have you connected to the device?");
        }

    }

}
