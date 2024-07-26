package com.example.goniometer;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RightArmRotation extends AppCompatActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView LeftM;
    protected TextView RightM;
    BluetoothAdapter adapter;
    private DatabaseHelper dbHelper;
    private long patientId; // This should be passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_right_arm_rotation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);

        // Retrieve patientId from intent
        patientId = getIntent().getLongExtra("PATIENT_ID", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Error: No patient ID provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no patient ID is passed
            return;
        }

        setupUI();
    }

    private void setupUI() {
        StartButton = findViewById(R.id.StartButton);
        SaveButton = findViewById(R.id.SaveButton);
        LeftM = findViewById(R.id.LeftM);
        RightM = findViewById(R.id.RightM);

        StartButton.setOnClickListener(v -> {
            // Your logic to start measurement
        });

        SaveButton.setOnClickListener(v -> saveMeasurement());
    }

    private void saveMeasurement() {
        // Dummy data for testing
        double leftAngle = 10;
        double rightAngle = 10;
        String measurementType = "Right Arm";
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Add measurement to the database
        long id = dbHelper.addMeasurement(patientId, measurementType, leftAngle, rightAngle, timestamp);

        // Check if the insertion was successful
        if (id != -1) {
            Toast.makeText(this, "Measurement saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save measurement", Toast.LENGTH_SHORT).show();
        }
    }
}
