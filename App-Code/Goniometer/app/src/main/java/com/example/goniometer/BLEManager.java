package com.example.goniometer;
import android.app.Activity;
import android.app.AlertDialog;
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
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("19B10005-E8F2-537E-4F6C-D104768A1214");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    private boolean ResetStatus = false;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Context context;
    private BluetoothGattCharacteristic characteristic;
    private DataCallback dataCallback;
    private ConnectionCallback connectionCallback;
    private AlertDialog startMeasuring;

    public interface DataCallback {
        void onDataReceived(int Yaw, int Pitch, int Roll, String Debug);
    }

    public interface ConnectionCallback {
        boolean onConnected();

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

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                ByteBuffer buffer = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN);
                String data = StandardCharsets.UTF_8.decode(buffer).toString();
                if ("Reseted ".equalsIgnoreCase(data)) {
                    ResetStatus = true;
                    Log.d(TAG, "Reset message received from Arduino");
                    return;
                }
                try {
                    if (dataCallback != null) {
                        String[] Variables = data.split(",");
                        for (int i = 0; i < Variables.length; i++) {
                            Variables[i] = Variables[i].replaceAll(",", "");
                        }
                        if (Variables.length == 4 || Variables.length == 3) {
                            //separating the variables from the string into 3 integers
                            int LiveYaw = 0;
                            int LivePitch = 0;
                            int LiveRoll = 0;
                            String DebugMessage = "";

                            try {
                                LiveYaw = Integer.parseInt(Variables[0]);
                            }catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Yaw Value: " + Variables[0]);
                            }
                            try {
                                LivePitch = Integer.parseInt(Variables[1]);
                            }catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Pitch Value: " + Variables[1]);
                            }
                            try {
                                 LiveRoll = Integer.parseInt(Variables[2]);
                            }catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Roll Value: " + Variables[2]);
                            }
                            try {
                                if (Variables.length == 4) {
                                    DebugMessage = Variables[3].trim();
                                }
                            }catch (NumberFormatException e) {
                                Log.d(TAG, "Invalid Debug Message Value: " + Variables[3].trim());
                            }



                            if(ResetStatus){
                                dataCallback.onDataReceived(0, 0, 0, "");
                                ResetStatus = false;
                            }else {
                                dataCallback.onDataReceived(LiveYaw, LivePitch, LiveRoll, DebugMessage);
                            }
                            Log.d("data values:", String.valueOf(LivePitch) + String.valueOf(LiveRoll) + String.valueOf(LiveYaw));
                        }
                    } else {
                        // Handle the case where the string doesn't contain three parts
                        Log.d("The String does not have 3 values", data);
                    }
                } catch (NumberFormatException e) {
                    // Handle any potential parsing errors
                    Log.d("Error parsing integers: ", e.getMessage());
                }

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Arduino resetting data");
            } else {
                Log.e(TAG, "Failed to send data to Arduino. Status: " + 0);
            }
        };
    };

    public void startMeasuring() {
        // Logic to start measurements and reset values to 0 if any existed
        if (characteristic != null) {
            bluetoothGatt.readCharacteristic(characteristic);
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
    public void sendDataToArduino(String data) {
        if (characteristic != null) {
            characteristic.setValue(data);
            boolean success = bluetoothGatt.writeCharacteristic(characteristic);
            if(success){
                Log.d(TAG, "Data sent successfully: " + data);
            }else{
                Log.e(TAG, "Failed to write characteristic");
            }
        } else {
            Log.e(TAG, "BluetoothGatt or dataCharacteristic is null. Have you connected to the device?");
        }
    }
public void setReset(boolean Reset){
        this.ResetStatus = Reset;
    }
}
