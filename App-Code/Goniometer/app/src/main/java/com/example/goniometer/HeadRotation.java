package com.example.goniometer;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

public class HeadRotation extends BaseActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView LeftM;
    protected TextView RightM;
    protected TextView Livedata;
    private BLEManager bleManager;

    private int maxLeft = 0; //Maximum left rotation angle
    private int maxRight = 0; //Maximum right rotation angle
    private boolean isMeasuring = false; //indicate whether measurement is ongoing

    private DatabaseHelper dbHelper;
    private long patientId; // This should be passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_head_rotation);
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
        StartButton = findViewById(R.id.StartButton);
        SaveButton = findViewById(R.id.SaveButton);
        LeftM = findViewById(R.id.leftRotation);
        RightM = findViewById(R.id.rightRotation);
        Livedata = findViewById(R.id.Livedata);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback((Yaw, Pitch, Roll, Debug) -> runOnUiThread(() -> {

            //Filter the received data according to the Angle's signal (-/+) and update maximum rotation value based on received Yaw value
            if (Yaw < 0 && (Yaw + maxRight < 0) && isMeasuring) {
                maxRight = -Yaw;
            }
            if (Yaw > 0 && (Yaw-maxLeft > 0) && isMeasuring) {
                maxLeft = Yaw;
            }

            //Check if Arduino sent a "Reset" command (debug)
            //To indicate the values were reset and set the textViews to 0
            if (Debug.equals("Reset")) {
                maxLeft = 0;
                maxRight = 0;
            }

            //Update TextViews with the current max values
            LeftM.setText("Left Rotation: " + maxLeft); //set the TextView with the maxLeft value
            RightM.setText("Right Rotation: " + maxRight);
            Livedata.setText("Yaw: "+ Yaw);
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
        SaveButton.setOnClickListener(v ->{
            if(!isMeasuring) {
                FunctionsController.saveMeasurement(this, dbHelper, patientId,  "Head Rotation", maxLeft, maxRight, SaveButton);
            }
        });
    }
}
