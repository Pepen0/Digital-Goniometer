package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DeviceAddress.SetDeviceAddress {

    protected Button buttonPatientButton;
    protected Button BluetoothButton;
    protected BLEManager bleManager;
    private static final int REQUEST_PERMISSIONS = 1001;
    protected ImageButton SettingsButton;
    public String deviceAddress = "F7:93:D0:8D:99:4A";
    public String InputAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check and request permissions
        if (hasPermissions()) {
            setupUI();
        } else {
            requestPermissions();
        }
    }

    private void setupUI() {
        BluetoothButton = findViewById(R.id.bluetoothButton);
        buttonPatientButton = findViewById(R.id.buttonPatientButton);
        bleManager = new BLEManager(this);
        bleManager.setConnectionCallback(new BLEManager.ConnectionCallback() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Connected to device", Toast.LENGTH_SHORT).show();
                    BluetoothButton.setVisibility(View.GONE);
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Disconnected from device", Toast.LENGTH_SHORT).show();
                    BluetoothButton.setVisibility(View.VISIBLE);
                });
            }
        });

        // Setting up Buttons
        BluetoothButton.setOnClickListener(v -> {
            if(InputAddress == null){
                DialogDeviceAddress();
            } else if (InputAddress.equals(deviceAddress)) {
                bleManager.connectToDevice(InputAddress);
            }else{
                Toast.makeText(this,"Invalid input Try Again", Toast.LENGTH_SHORT).show();
                DialogDeviceAddress();
            }

        });

        buttonPatientButton.setOnClickListener(v -> goToPatientPage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleManager != null) {
            bleManager.disconnect();
        }
    }

    private void goToPatientPage() {
        Intent intent = new Intent(this, PatientListActivity.class);
        startActivity(intent);
    }

    private boolean hasPermissions() {
        // Checks if the user have provided the needed Permissions
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        // Will Request the user to Provide the permissions in case they are not
        // provided
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
        }, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Check if the user successfully provided the Permissions
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean PermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    PermissionsGranted = false;
                    break;
                }
            }
            if (PermissionsGranted) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                setupUI();
            } else {
                // Permissions not granted
                // Send user to application settings so the user provide the permissions
                GoToSettingsDialog();

            }
        }
    }

    private void GoToSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions Confirmation");
        builder.setMessage(
                "Location and Bluetooth Permissions are required to make this application function well please provide both nearby devices and location permissions");
        // implementation of Yes, No choices and what action it does
        builder.setPositiveButton("GoToSettings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            System.exit(1);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            GoToSettingsDialog();
            Toast.makeText(MainActivity.this, "Please Provide the permission or the application wont work properly",
                    Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
    private void DialogDeviceAddress(){
        DeviceAddress deviceAddressDialog = new DeviceAddress();
        deviceAddressDialog.SetDeviceAddress(this);
        deviceAddressDialog.show(getSupportFragmentManager(), "DeviceAddressDialog");
    }

    @Override
    public void OnAddressChange(String PhysicalAddress) {
        InputAddress =PhysicalAddress;
        Toast.makeText(this, "Device Address Changed" + PhysicalAddress, Toast.LENGTH_SHORT).show();
    }
}