package com.example.goniometer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class LeftElbow extends AppCompatActivity {
    protected Button StartButtonWrist;
    protected Button SaveButtonWrist;
    protected TextView LeftMax;
    protected TextView RightMax;
    protected TextView LiveDataWrist;
    private BLEManager bleManager;
    private boolean userConfirmation = false;
    private float maxLeftWrist = 0;
    private float maxRightWrist = 0;
    private boolean ismeasuring = false;
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
        setupUI();
    }
    private void setupUI() {

        StartButtonWrist = findViewById(R.id.StartButtonWrist);
        SaveButtonWrist = findViewById(R.id.SaveButtonWrist);
        LeftMax = findViewById(R.id.LeftMax);
        RightMax = findViewById(R.id.RightMax);
        LiveDataWrist = findViewById(R.id.LiveDataWrist);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback(new BLEManager.DataCallback() {
            @Override
            public void onDataReceived(int Yaw, int Pitch, int Roll, String Debug) {
                  runOnUiThread(() -> {
                if (Pitch < 0 && (Pitch + maxRightWrist < 0) && ismeasuring) {
                    maxRightWrist = -Pitch;
                }
                if (Pitch > 0 && (Pitch-maxLeftWrist > 0) && ismeasuring) {
                    maxLeftWrist = Pitch;
                }
                LeftMax.setText("Left Rotation: " + maxLeftWrist);
                RightMax.setText("Right Rotation: " + maxRightWrist);
                LiveDataWrist.setText("Pitch: " + Pitch);
                    });
            }
        });


//        //This will hide the save button for guests
//        Intent intent = getIntent();
//        boolean isGuest = intent.getBooleanExtra("isGuest", false);
//
//        // Hide the button if the user is a guest
//        if (isGuest) {
//            SaveButtonWrist.setVisibility(View.GONE);
//        }

        StartButtonWrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ismeasuring) {
                    askForConfirmation_p();
                    if (userConfirmation) {
                        resetValues();
                        bleManager.startMeasuring();
                    }

                } else {
                    ismeasuring = false;
                    StartButtonWrist.setText("Start Measuring");
                }
            }
        });

        SaveButtonWrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
               // resetValues();
                userConfirmation = true;
                ismeasuring = true;
                bleManager.sendDataToArduino("Reset data");
                Log.d("Reset command sent", "Reset data");
                StartButtonWrist.setText("Stop Measuring");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userConfirmation = false;
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void resetValues(){
        maxLeftWrist = 0;
        maxRightWrist=0;
        LeftMax.setText("Left Rotation: " + maxLeftWrist);
        RightMax.setText("Right Rotation: " + maxRightWrist);
        LiveDataWrist.setText("Pitch: 0");
        bleManager.setReset(true);
        Log.d("Reset", "UI and values reset to 0");
    }

}