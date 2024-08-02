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

public class RightHipAbduction extends AppCompatActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView rightAbductionM ;
    private int AbductionMax = 0;
    private boolean isMeasuring = false;
    private BLEManager bleManager;
    protected TextView LiveRoll;
    private DatabaseHelper dbHelper;
    private long patientId; // This should be passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_right_hip_abduction);
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
        rightAbductionM = findViewById(R.id.AbductionM);
        LiveRoll = findViewById(R.id.rightRotation);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback((Yaw, Pitch, Roll, Debug) -> runOnUiThread(() -> {
            if (Roll > 0 && (Roll - AbductionMax > 0) && isMeasuring) {
                AbductionMax = Roll;
            }
            if (Debug.equals("Reset")){
                AbductionMax = 0;
            }
            rightAbductionM.setText("Right Abduction: " + AbductionMax);
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
                FunctionsController.saveMeasurement(this, dbHelper, patientId,  "Right Hip Abduction", AbductionMax, 0, SaveButton);
            }
        });
    }

}
