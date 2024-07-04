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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.util.Log;
public class MainActivity extends AppCompatActivity {

    protected Button buttonGuestButton;
    protected Button buttonPatientButton;
    protected Button BluetoothButton;
    protected Button ConnectionButton;

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

    setupUI();

    }

    private void setupUI() {
        buttonGuestButton = findViewById(R.id.buttonGuestButton);
        BluetoothButton = findViewById(R.id.bluetoothButton);
        ConnectionButton = findViewById(R.id.ConnectionButton);
        buttonPatientButton = findViewById(R.id.buttonPatientButton);

//Setting up Buttons
        BluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
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

    private void goToPatientPage() {
        Intent intent = new Intent(this, PatientActivity.class);
        startActivity(intent);
    }


    private void goToAssessmentActivity() {
        Intent intent = new Intent(this, AssessmentActivity.class);
        startActivity(intent);
    }
}