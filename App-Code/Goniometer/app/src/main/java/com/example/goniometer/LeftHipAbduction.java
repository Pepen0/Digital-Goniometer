package com.example.goniometer;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LeftHipAbduction extends BaseActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView AbductionM;
    protected TextView LiveRoll;
    private BLEManager bleManager;
    private int AbductionMax = 0;   //Maximum abduction angle
    private boolean isMeasuring = false; //indicate whether measurement is ongoing
    private DatabaseHelper dbHelper;
    private long patientId; // This should be passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_left_hip_abduction);
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
        // Remove action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        // Remove action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setupUI();
        setupToolbar(); //Setup the toolbar
    }


    //Setup the user interface including buttons and TextViews functionality
    private void setupUI() {
        StartButton = findViewById(R.id.StartButton);
        SaveButton = findViewById(R.id.SaveButton);
        AbductionM = findViewById(R.id.leftRotation);
        LiveRoll = findViewById(R.id.Livedata);
        bleManager = BLEManager.getInstance();

        //Filter the received data according to the negative Angle's and update maximum abduction value based on received Roll value
        bleManager.setDataCallback((Yaw, Pitch, Roll, Debug) -> runOnUiThread(() -> {
            if (Roll < 0 && (Roll + AbductionMax < 0) && isMeasuring) {
                AbductionMax = -Roll;
            }

            //Check if Arduino sent a "Reset" command (debug)
            //To indicate the values were reset and set the textViews to 0
            if (Debug.equals("Reset")){
                AbductionMax = 0;
            }

            //Update TextViews with the current max values
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

                            //Send command to arduino to reset values to 0
                            bleManager.sendDataToArduino("Reset data");
                            Log.d("Reset command sent", "Reset data");

                            //Updates button state and visibility
                            StartButton.setText("STOP");
                            StartButton.setBackgroundResource(R.drawable.circular_button_stop);
                            SaveButton.setBackgroundColor(Color.GRAY);
                            SaveButton.setVisibility(View.VISIBLE);
                            SaveButton.setText("Stop Measuring To Save");
                        }
                );

            } else {

                //Updates button state and visibility
                isMeasuring = false;
                StartButton.setText("START");
                StartButton.setBackgroundResource(R.drawable.circular_button_start);
                SaveButton.setBackgroundResource(R.drawable.custom_button2);
                SaveButton.setText("Save Measurement");
            }
        });

        SaveButton.setOnClickListener(v -> {
            if(!isMeasuring) {
                FunctionsController.saveMeasurement(this, dbHelper, patientId,  "Left Hip Abduction",AbductionMax, 0 , SaveButton);
            }
        });
    }

}
