package com.example.goniometer;

import android.content.DialogInterface;
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

public class RightElbow extends AppCompatActivity {

    protected Button StartButtonWrist;
    protected Button SaveButtonWrist;
    protected TextView LeftMaxWrist;
    protected TextView RightMaxWrist;
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
        setContentView(R.layout.activity_right_elbow);
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

        StartButtonWrist = findViewById(R.id.StartButtonElbow);
        SaveButtonWrist = findViewById(R.id.SaveButtonElbow);
        LeftMaxWrist = findViewById(R.id.LeftMax);
        RightMaxWrist = findViewById(R.id.RightMax);
        LiveDataWrist = findViewById(R.id.LiveDataWrist);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback(new BLEManager.DataCallback() {
            @Override
            public void onDataReceived(int Yaw, int Pitch, int Roll, String Debug) {
                runOnUiThread(() -> {
                if (Pitch < 0 && (Pitch + maxRightElbow < 0) && isMeasuring) {
                    maxRightElbow = -Pitch;
                }
                if (Pitch > 0 && (Pitch-maxLeftElbow > 0) && isMeasuring) {
                    maxLeftElbow = Pitch;
                }
                    if (Debug.equals("Reset")) {
                        maxLeftElbow = 0;
                        maxRightElbow = 0;
                    }
                LeftMaxWrist.setText("Left Rotation: " + maxLeftElbow);
                RightMaxWrist.setText("Right Rotation: " + maxRightElbow);
                LiveDataWrist.setText("Pitch: " + Pitch);
               });
            }
        });

        StartButtonWrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMeasuring) {
                    askForConfirmation_p();
                } else {
                    isMeasuring = false;
                    StartButtonWrist.setText("Start Measuring");
                    SaveButtonWrist.setBackgroundResource(R.drawable.custom_button2);
                    SaveButtonWrist.setText("Save Measurement");
                }
            }
        });

        SaveButtonWrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMeasuring) {
                    saveMeasurement();
                }
            }
        });
    }

    private void askForConfirmation_p() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Measuring Confirmation");
        builder.setMessage("Are you sure you want to start a new measurement?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isMeasuring = true;
                bleManager.sendDataToArduino("Reset data");
                Log.d("Reset command sent", "Reset data");
                StartButtonWrist.setText("Stop Measuring");
                SaveButtonWrist.setBackgroundColor(Color.GRAY);
                SaveButtonWrist.setVisibility(View.VISIBLE);
                SaveButtonWrist.setText("Stop Measuring To Save");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void saveMeasurement() {
        double leftAngle = maxLeftElbow;
        double rightAngle = maxRightElbow;
        String measurementType = "Right Elbow";

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
        SaveButtonWrist.setVisibility(View.GONE);
    }
}

