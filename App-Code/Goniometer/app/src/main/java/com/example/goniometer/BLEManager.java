package com.example.goniometer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.le.ScanResult;
import android.content.Context;

import java.util.UUID;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class BLEManager {
    private static final String Device_Name = "LED";
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothLeScanner bluetoothLeScanner;
    private Context context;
    private Activity activity;

    public BLEManager(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "This Device Does Not Support BLE", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothManager == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Enable bluetooth to proceed", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void startScanning() {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                if (Device_Name.equals(device.getName())) {
                    bluetoothLeScanner.stopScan(scanCallback);
                    connectDevice(device);
                }
            }
        }

        private void connectDevice(BluetoothDevice device) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                bluetoothGatt = device.connectGatt(context, false, gattCallback);
            }
        }

        private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        gatt.discoverServices();
                    } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                        returnToast("Disconnected from device");
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(CHARACTERISTIC_UUID);
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                        if (characteristic != null) {
                            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                                gatt.readCharacteristic(characteristic);
                            }
                        }
                    }

                } else {
                    returnToast("Failed to discover services");
                }
            }

            @Override
            public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    String measurements = new String(characteristic.getValue());
                    returnToast("Measurements received" + measurements);
                } else {
                    returnToast("Failed to read characteristic");
                }
            }
        };

        private void returnToast(String message) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }

        @SuppressLint("MissingPermission")
        public void stopConnection() {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
            }
        }
    };
}

