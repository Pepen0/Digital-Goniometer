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
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class BLEManager {
    private static final String TAG = "IMU Sensor";
    private static final UUID SERVICE_UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_Roll = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_Pitch = UUID.fromString("19B10002-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_Yaw = UUID.fromString("19B10003-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CHARACTERISTIC_Debug = UUID.fromString("19B10004-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final Context context;
    private BluetoothGattCharacteristic yawCharacteristic, rollCharacteristic, pitchCharacteristic, debugCharacteristic;
    private YawCallback yawCallback;
    private PitchCallback pitchCallback;
    private RollCallback rollCallback;
    private DebugCallback debugCallback;
    private ConnectionCallback connectionCallback;

    public interface YawCallback {
        //interface for receiving Yaw updates
        void onYawReceived(float yaw);
    }
    public interface PitchCallback {
        //interface for receiving Pitch updates
        void onPitchReceived(float pitch);
    }
    public interface RollCallback {
        //interface for receiving Roll updates
        void onRollReceived(float roll);
    }
    public interface DebugCallback {
        //interface for receiving Roll updates
        void onDebugReceived(String Debug);
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

    public void setYawCallback(YawCallback yawCallback) {this.yawCallback = yawCallback;}

    public void setPitchCallback(PitchCallback pitchCallback) {this.pitchCallback = pitchCallback;}

    public void setRollCallback(RollCallback rollCallback) {this.rollCallback = rollCallback;}

    public void setDebugCallback(DebugCallback debugCallback) {this.debugCallback = debugCallback;}
    public void setConnectionCallback(ConnectionCallback callback) {this.connectionCallback = callback;}

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
                    characteristicManager(service, CHARACTERISTIC_Yaw, true);
                    characteristicManager(service, CHARACTERISTIC_Pitch, false);
                    characteristicManager(service, CHARACTERISTIC_Roll, false);
                    characteristicManager(service, CHARACTERISTIC_Debug, false);
                } else {
                    Log.w(TAG, "Service not found" + SERVICE_UUID.toString());
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        private void characteristicManager(BluetoothGattService service, UUID uuid, boolean isYaw){
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
            if(characteristic !=null){
                bluetoothGatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                if (descriptor !=null){
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                }
                if(isYaw) {
                    yawCharacteristic = characteristic;
                } else if(uuid.equals(CHARACTERISTIC_Pitch)){
                    pitchCharacteristic = characteristic;
                } else if (uuid.equals(CHARACTERISTIC_Roll)){
                    rollCharacteristic = characteristic;
                } else if(uuid.equals(CHARACTERISTIC_Debug)){
                    debugCharacteristic = characteristic;
                }
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // This will handle all the data streamed from arduino for each characteristic
            if (CHARACTERISTIC_Yaw.equals(characteristic.getUuid())) {
                float yaw = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
              //  float yaw = buffer.getFloat();
                if (yawCallback != null) {
                    yawCallback.onYawReceived(yaw);
                }
            } else if (CHARACTERISTIC_Pitch.equals(characteristic.getUuid())) {
                float pitch = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                //float pitch = buffer_p.getFloat();
                if (pitchCallback != null) {
                    pitchCallback.onPitchReceived(pitch);
                }
            } else if (CHARACTERISTIC_Roll.equals(characteristic.getUuid())) {
                float roll = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                //float roll = buffer_r.getFloat();
                if (rollCallback != null) {
                    rollCallback.onRollReceived(roll);
                }
            }// else if (CHARACTERISTIC_Debug.equals (characteristic.getUuid())) {
               // String debug = new String (ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN);
                //String debug = new String(characteristic.getValue(), StandardCharsets.UTF_8);
                //if (debugCallback != null) {
                  //  debugCallback.onDebugReceived(debug);
               // }
            }
       // }
    };

    public void startMeasuring() {
        // Logic to start measurements and reset values to 0 if any existed
        if (yawCharacteristic != null) {
            bluetoothGatt.readCharacteristic(yawCharacteristic);
        }
        if (pitchCharacteristic != null) {
            bluetoothGatt.readCharacteristic(pitchCharacteristic);
        }
        if (rollCharacteristic != null) {
            bluetoothGatt.readCharacteristic(rollCharacteristic);
        }
        if (debugCharacteristic != null) {
            bluetoothGatt.readCharacteristic(debugCharacteristic);
        }
    }


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

