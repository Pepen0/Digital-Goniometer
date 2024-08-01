package com.example.goniometer;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class LeftElbow extends AppCompatActivity {
    protected Button StartButtonElbow;
    protected Button SaveButtonElbow;
    protected TextView LeftMax;
    protected TextView RightMax;
    protected TextView LiveDataWrist;
    private BLEManager bleManager;
    private float maxLeftElbow = 0;
    private float maxRightElbow = 0;
    private boolean isMeasuring = false;

    private DatabaseHelper dbHelper;
    private long patientId; // This should be passed from the previous activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_left_elbow);
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
            Toast.makeText(this, "Passing as a Guest", Toast.LENGTH_SHORT).show();
        }
        setupUI();
    }
    private void setupUI() {

        StartButtonElbow = findViewById(R.id.StartButtonElbow);
        SaveButtonElbow = findViewById(R.id.SaveButtonElbow);
        LeftMax = findViewById(R.id.LeftMax);
        RightMax = findViewById(R.id.RightMax);
        LiveDataWrist = findViewById(R.id.LiveDataWrist);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback((Yaw, Pitch, Roll, Debug) -> runOnUiThread(() -> {
          if (Pitch < 0 && (Pitch + maxRightElbow < 0) && isMeasuring) {
              maxRightElbow = -Pitch;
          }
          if (Pitch > 0 && (Pitch-maxLeftElbow > 0) && isMeasuring) {
              maxLeftElbow = Pitch;
          }
          if (Debug.equals("Reset")){
              maxLeftElbow = 0;
              maxRightElbow = 0;
          }
          LeftMax.setText("Left Rotation: " + maxLeftElbow);
          RightMax.setText("Right Rotation: " + maxRightElbow);
          LiveDataWrist.setText("Pitch: " + Pitch);
        }));


        StartButtonElbow.setOnClickListener(v -> {
            if (!isMeasuring) {
                askForConfirmation_p();

            } else {
                isMeasuring = false;
                StartButtonElbow.setText("START");
                StartButtonElbow.setBackgroundResource(R.drawable.circular_button_start);
                SaveButtonElbow.setBackgroundResource(R.drawable.custom_button2);
                SaveButtonElbow.setText("Save Measurement");
            }
        });

        SaveButtonElbow.setOnClickListener(v -> {
            if(!isMeasuring) {
                saveMeasurement();
            }
        });
    }

    private void askForConfirmation_p() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Measuring Confirmation");
        builder.setMessage("Are you sure you want to start a new measurement?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            isMeasuring = true;
            bleManager.sendDataToArduino("Reset data");
            Log.d("Reset command sent", "Reset data");
            StartButtonElbow.setText("STOP");
            StartButtonElbow.setBackgroundResource(R.drawable.circular_button_stop);
            SaveButtonElbow.setBackgroundColor(Color.GRAY);
            SaveButtonElbow.setVisibility(View.VISIBLE);
            SaveButtonElbow.setText("Stop Measuring To Save");
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void saveMeasurement() {
        // Dummy data for testing
        double leftAngle = maxLeftElbow;
        double rightAngle = maxRightElbow;
        String measurementType = "Left Elbow";

        // Format the current timestamp to include date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd @ HH:mm", Locale.getDefault()); // ISO 8601 format
        String timestamp = sdf.format(new Date());

        // Add measurement to the database
        long id = dbHelper.addMeasurement(patientId, measurementType, leftAngle, rightAngle, timestamp);

        // Check if the insertion was successful
        if (id != -1) {
            Toast.makeText(this, "Measurement saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save measurement", Toast.LENGTH_SHORT).show();
        }
        SaveButtonElbow.setVisibility(View.GONE);
    }
}