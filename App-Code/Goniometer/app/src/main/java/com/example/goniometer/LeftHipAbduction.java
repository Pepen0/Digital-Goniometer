package com.example.goniometer;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LeftHipAbduction extends AppCompatActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView AbductionM;
    protected TextView LiveRoll;
    private BLEManager bleManager;
    private int AbductionMax = 0;
    private boolean isMeasuring = false;
    private DatabaseHelper dbHelper;
    private long patientId; // This should be passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_left_arm_abduction);
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
        StartButton = findViewById(R.id.StartButton);
        SaveButton = findViewById(R.id.SaveButton);
        AbductionM = findViewById(R.id.AbductionM);
        LiveRoll = findViewById(R.id.Roll);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback((Yaw, Pitch, Roll, Debug) -> runOnUiThread(() -> {
            if (Roll > 0 && (Roll - AbductionMax > 0) && isMeasuring) {
                AbductionMax = Roll;
            }
            if (Debug.equals("Reset")){
                AbductionMax = 0;
            }
            AbductionM.setText("Left Abduction: " + AbductionMax);
            LiveRoll.setText("Roll: " + Roll);
        }));
        StartButton.setOnClickListener(v -> {
            if (!isMeasuring) {
                FunctionsController.askForConfirmation(this,
                        "Start Measuring Confirmation",
                        "Are you sure you want to start a new measurement?",
                        "Yes",()-> {
                            isMeasuring = true;
                            bleManager.sendDataToArduino("Reset data");
                            Log.d("Reset command sent", "Reset data");
                            StartButton.setText("STOP");
                            StartButton.setBackgroundResource(R.drawable.circular_button_stop);
                            SaveButton.setBackgroundColor(Color.GRAY);
                            SaveButton.setVisibility(View.VISIBLE);
                            SaveButton.setText("Stop Measuring To Save");
                        }
                );

            } else {
                isMeasuring = false;
                StartButton.setText("START");
                StartButton.setBackgroundResource(R.drawable.circular_button_start);
                SaveButton.setBackgroundResource(R.drawable.custom_button2);
                SaveButton.setText("Save Measurement");
            }
        });

        SaveButton.setOnClickListener(v -> {
            if(!isMeasuring) {
                saveMeasurement();
            }
        });
    }

    private void saveMeasurement() {
        double AbductionAngle = AbductionMax;
        double NotUsed = 0;
        String measurementType = "Left Abduction";

        // Format the current timestamp to include date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd @ HH:mm", Locale.getDefault()); // ISO 8601 format
        String timestamp = sdf.format(new Date());

        // Add measurement to the database
        long id = dbHelper.addMeasurement(patientId, measurementType, AbductionAngle,NotUsed, timestamp);

        // Check if the insertion was successful
        if (id != -1) {
            Toast.makeText(this, "Measurement saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save measurement", Toast.LENGTH_SHORT).show();
        }
        SaveButton.setVisibility(View.GONE);
    }

}
