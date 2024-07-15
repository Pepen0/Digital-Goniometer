package com.example.goniometer;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
public class MainActivity extends AppCompatActivity {

    protected Button buttonGuestButton;
    protected Button buttonPatientButton;
    protected Button BluetoothButton;
    protected BLEManager bleManager;
    private static final int REQUEST_PERMISSIONS = 1001;

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
        setContentView(R.layout.activity_main);

        // Check and request permissions
        if (!hasPermissions()) {
            requestPermissions();
        }
    setupUI();

    }

    private void setupUI() {
        buttonGuestButton = findViewById(R.id.buttonGuestButton);
        BluetoothButton = findViewById(R.id.bluetoothButton);
        buttonPatientButton = findViewById(R.id.buttonPatientButton);
        bleManager = new BLEManager(this);
        bleManager.setConnectionCallback(new BLEManager.ConnectionCallback() {
            @Override
            public void onConnected() {
                runOnUiThread(()->{
                    Toast.makeText(MainActivity.this, "Connected to device", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(MainActivity.this, HeadRotation.class);
//                    startActivity(intent);
                });
            }
            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Disconnected from device", Toast.LENGTH_SHORT).show();
                });
            }
        });

//Setting up Buttons
        BluetoothButton.setOnClickListener(v-> {
                String deviceAddress = "73:B3:B7:66:20:70";
                bleManager.connectToDevice(deviceAddress);
        });
        buttonGuestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAssessmentActivity();

            }
        });

        buttonPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPatientPage();
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleManager != null) {
            bleManager.disconnect();
        }
    }
    private void goToPatientPage() {
        Intent intent = new Intent(this, PatientActivity.class);
        startActivity(intent);
    }


    private void goToAssessmentActivity() {
        Intent intent = new Intent(this, AssessmentActivity.class);
        startActivity(intent);
    }
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
        }, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (hasPermissions()) {
                // Permissions granted
            } else {
                // Permissions not granted
            }
        }
    }

}