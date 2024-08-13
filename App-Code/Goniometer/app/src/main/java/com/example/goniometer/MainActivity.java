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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DeviceAddress.SetDeviceAddress {

    protected Button buttonPatientButton;
    public static Button BluetoothButton;
    protected BLEManager bleManager;
    private static final int REQUEST_PERMISSIONS = 1001;
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
    //Setup the user interface including buttons and TextViews functionality
    private void setupUI() {
        BluetoothButton = findViewById(R.id.bluetoothButton);
        buttonPatientButton = findViewById(R.id.buttonPatientButton);
        bleManager = new BLEManager(this);

        //setup the connection callback for the ble manager
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

            //If address does not matches the pattern ask for correct entry
            //This was our Arduino Bluetooth physical address "F7:93:D0:8D:99:4A". Therefore,
            // it will check for the same pattern
            if(!DeviceAddress.validInput(InputAddress)){
                DialogDeviceAddress();

            }else{

                //connect to device that holds a specific physical address

                bleManager.connectToDevice(InputAddress);
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

//Navigate to Patient Page
    private void goToPatientPage() {
        Intent intent = new Intent(this, PatientListActivity.class);
        startActivity(intent);
    }

    // Checks if the user have provided the necessary Permissions
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    // Request the user to Provide the necessary permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
        }, REQUEST_PERMISSIONS);
    }

//Check the result of request permission If granted the setup UI will run
// if not a dialog alert will force the user to go to settings and provide permissions
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

//show an alert dialog to inform the user about required permissions and
// hold the package name to open the proper app settings
    private void GoToSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions Confirmation");
        builder.setMessage(
                "Location and Bluetooth Permissions are required to make this application function well please provide both nearby devices and location permissions");
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
        AlertDialog dialog = builder.create();
        dialog.show();

        //Change the style of the buttons
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if(positiveButton != null){
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.cyan));
        }
        if(negativeButton != null){
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.cyan));
        }
    }

    //show a dialog to enter the device's physical address
    private void DialogDeviceAddress(){
        DeviceAddress deviceAddressDialog = new DeviceAddress();
        deviceAddressDialog.SetDeviceAddress(this);
        deviceAddressDialog.show(getSupportFragmentManager(), "DeviceAddressDialog");
    }

//save the new address to InputAddress and attempt to establish a connection
    @Override
    public void OnAddressChange(String PhysicalAddress) {
        InputAddress =PhysicalAddress;
        Toast.makeText(this, "Device Address Changed to: " + PhysicalAddress, Toast.LENGTH_SHORT).show();
    }

}