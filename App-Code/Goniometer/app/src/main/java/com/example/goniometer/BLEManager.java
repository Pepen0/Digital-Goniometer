//This Class will Manage the Bluetooth Low Energy (BLE) Communication and Data stream handling
package com.example.goniometer;
import android.app.Activity;
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
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;


public class BLEManager {
    private static final String TAG = "IMU Sensor";
    private static final UUID SERVICE_UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_roll = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_pitch = UUID.fromString("19B10002-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_yaw = UUID.fromString("19B10003-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_debug = UUID.fromString("19B10004-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final Context context;
    private BluetoothGattCharacteristic yawCharacteristic, rollCharacteristic, pitchCharacteristic, debugCharacteristic;
    private DataCallback dataCallback;
    private ConnectionCallback connectionCallback;

    public interface DataCallback {
        //interface for receiving data updates
        void onDataReceived(float yaw);
    }

    public interface ConnectionCallback {
        //interface for connection status
        void onConnected();

        void onDisconnected();
    }

    private static BLEManager instance;

    public static BLEManager getInstance() {
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
        //Initiate connection to a BLE device
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                bluetoothGatt.discoverServices();
                if (connectionCallback != null) {
                    connectionCallback.onConnected();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                if (connectionCallback != null) {
                    connectionCallback.onDisconnected();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_yaw);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                    // Pitch Characteristic
                    BluetoothGattCharacteristic characteristic_P = service.getCharacteristic(CHARACTERISTIC_pitch);
                    if (characteristic_P != null) {
                        gatt.setCharacteristicNotification(characteristic_P, true);
                        BluetoothGattDescriptor descriptor_P = characteristic_P.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                        if (descriptor_P != null) {
                            descriptor_P.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor_P);
                        }
                        // Roll Characteristic
                        BluetoothGattCharacteristic characteristic_R = service.getCharacteristic(CHARACTERISTIC_roll);
                        if (characteristic_R != null) {
                            gatt.setCharacteristicNotification(characteristic_R, true);
                            BluetoothGattDescriptor descriptor_R = characteristic_R.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                            if (descriptor_R != null) {
                                descriptor_R.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor_R);
                            }

                            // Debug Characteristic
                            BluetoothGattCharacteristic characteristic_D = service.getCharacteristic(CHARACTERISTIC_pitch);
                            if (characteristic_D != null) {
                                gatt.setCharacteristicNotification(characteristic_D, true);
                                BluetoothGattDescriptor descriptor_D = characteristic_D.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                                if (descriptor_D != null) {
                                    descriptor_D.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    gatt.writeDescriptor(descriptor_D);
                                }
                            } else {
                                Log.w(TAG, "Service not found: " + SERVICE_UUID.toString());
                            }
                        } else {
                            Log.w(TAG, "onServicesDiscovered received: " + status);
                        }
                    }
                }
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // This will handle all the data streamed from arduino for each characteristic
            if (CHARACTERISTIC_yaw.equals(characteristic.getUuid())) {
                ByteBuffer buffer = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN);
                float yaw = buffer.getFloat();
                if (dataCallback != null) {
                    dataCallback.onDataReceived(yaw);
                }
            } else if (CHARACTERISTIC_pitch.equals(characteristic.getUuid())) {
                ByteBuffer buffer = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN);
                float pitch = buffer.getFloat();
                if (dataCallback != null) {
                    dataCallback.onDataReceived(pitch);
                }
            } else if (CHARACTERISTIC_roll.equals(characteristic.getUuid())) {
                ByteBuffer buffer = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN);
                float roll = buffer.getFloat();
                if (dataCallback != null) {
                    dataCallback.onDataReceived(roll);
                }
            } else if (CHARACTERISTIC_debug.equals(characteristic.getUuid())) {
                ByteBuffer buffer = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN);
                float debug = buffer.getFloat();
                if (dataCallback != null) {
                    dataCallback.onDataReceived(debug);
                }
            }
        }
    };

    public void startMeasuring() {
        // Logic to start measurements and reset values to 0 if any existed
        if (yawCharacteristic !=null && rollCharacteristic != null &&pitchCharacteristic !=null &&debugCharacteristic !=null) {
            bluetoothGatt.readCharacteristic(yawCharacteristic);
            bluetoothGatt.readCharacteristic(pitchCharacteristic);
            bluetoothGatt.readCharacteristic(rollCharacteristic);
            bluetoothGatt.readCharacteristic(debugCharacteristic);
        }
    }

//    public void stopMeasuring() {
//        //Logic to stop measuring and hold the measurements until Start button is pressed again
//        if (dataCallback != null) {
//            dataCallback.onDataReceived(lastYawValue);
//        }
//    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private void returnToast(String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
        }
    }
}

