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


public class LeftElbow extends BaseActivity {
    protected Button StartButtonElbow;
    protected Button SaveButtonElbow;
    protected TextView LeftMax;
    protected TextView RightMax;
    protected TextView LiveDataElbow;
    private BLEManager bleManager;
    private int maxLeftElbow = 0;  //Maximum left elbow rotation angle
    private int maxRightElbow = 0; //Maximum right elbow rotation angle
    private boolean isMeasuring = false; //indicate whether measurement is ongoing

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
        // Remove action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setupUI();
        setupToolbar(); //Setup the toolbar
    }


    //Setup the user interface including buttons and TextViews functionality
    private void setupUI() {

        StartButtonElbow = findViewById(R.id.StartButton);
        SaveButtonElbow = findViewById(R.id.SaveButtonElbow);
        LeftMax = findViewById(R.id.LeftMax);
        RightMax = findViewById(R.id.RightMax);
        LiveDataElbow = findViewById(R.id.LiveDataElbow);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback((Yaw, Pitch, Roll, Debug) -> runOnUiThread(() -> {

            //Filter the received data according to the Angle's signal (-/+) and update maximum rotation value based on received Pitch value
          if (Pitch < 0 && (Pitch + maxRightElbow < 0) && isMeasuring) {
              maxRightElbow = -Pitch;
          }
          if (Pitch > 0 && (Pitch-maxLeftElbow > 0) && isMeasuring) {
              maxLeftElbow = Pitch;
          }

            //Check if Arduino sent a "Reset" command (debug)
            //To indicate the values were reset and set the textViews to 0
          if (Debug.equals("Reset")){
              maxLeftElbow = 0;
              maxRightElbow = 0;
          }

            //Update TextViews with the current max values
            LeftMax.setText("Pronation: " + maxLeftElbow);
            RightMax.setText("Supination: " + maxRightElbow);
          LiveDataElbow.setText("Pitch: " + Pitch);
        }));


        StartButtonElbow.setOnClickListener(v -> {
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
                            StartButtonElbow.setText("STOP");
                            StartButtonElbow.setBackgroundResource(R.drawable.circular_button_stop);
                            SaveButtonElbow.setBackgroundColor(Color.GRAY);
                            SaveButtonElbow.setVisibility(View.VISIBLE);
                            SaveButtonElbow.setText("Stop Measuring To Save");
                        }
                );

            } else {

                //Updates button state and visibility
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