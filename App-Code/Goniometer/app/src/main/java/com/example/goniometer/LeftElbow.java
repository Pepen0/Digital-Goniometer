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


public class LeftElbow extends AppCompatActivity {
    protected Button StartButtonElbow;
    protected Button SaveButtonElbow;
    protected TextView LeftMax;
    protected TextView RightMax;
    protected TextView LiveDataElbow;
    private BLEManager bleManager;
    private int maxLeftElbow = 0;
    private int maxRightElbow = 0;
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

        StartButtonElbow = findViewById(R.id.StartButton);
        SaveButtonElbow = findViewById(R.id.SaveButtonElbow);
        LeftMax = findViewById(R.id.LeftMax);
        RightMax = findViewById(R.id.RightMax);
        LiveDataElbow = findViewById(R.id.LiveDataElbow);
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
          LiveDataElbow.setText("Pitch: " + Pitch);
        }));


        StartButtonElbow.setOnClickListener(v -> {
            if (!isMeasuring) {
                FunctionsController.askForConfirmation(this,
                        "Start Measuring Confirmation",
                        "Are you sure you want to start a new measurement?",
                        "Yes",()-> {
                            isMeasuring = true;
                            bleManager.sendDataToArduino("Reset data");
                            Log.d("Reset command sent", "Reset data");
                            StartButtonElbow.setText("STOP");
                            StartButtonElbow.setBackgroundResource(R.drawable.circular_button_stop);
                            SaveButtonElbow.setBackgroundColor(Color.GRAY);
                            SaveButtonElbow.setVisibility(View.VISIBLE);
                            SaveButtonElbow.setText("Stop Measuring To Save");
                        }
                );

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
                FunctionsController.saveMeasurement(this, dbHelper, patientId,  "Left Elbow Rotation", maxLeftElbow, maxRightElbow, SaveButtonElbow);
            }
        });
    }
}