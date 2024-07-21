package com.example.goniometer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WristRotation extends AppCompatActivity {

    protected Button StartButtonWrist;
    protected Button SaveButtonWrist;
    protected TextView LeftMaxWrist;
    protected TextView RightMaxWrist;
    protected TextView LiveDataWrist;
    //protected Switch switchRightLeft;
    private BLEManager bleManager_p;
    private boolean userConfirmation = false;
    private float maxLeftWrist = 0;
    private float maxRightWrist = 0;

    private boolean ismeasuring = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wrist_rotation);
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
        LeftMaxWrist = findViewById(R.id.LeftMaxWrist);
        RightMaxWrist = findViewById(R.id.RightMaxWrist);
        LiveDataWrist = findViewById(R.id.LiveDataWrist);
        bleManager_p = BLEManager.getInstance();

        bleManager_p.setPitchCallback(new BLEManager.PitchCallback() {
            @Override
            public void onPitchReceived(float pitch) {
                runOnUiThread(() -> {
                    LiveDataWrist.setText(String.format("pitch: %.2f", pitch));

                    if (pitch < 0 && (pitch + maxRightWrist < 0) && ismeasuring) {
                        maxRightWrist = -pitch;
                    }
                    if (pitch > 0 && (pitch - maxLeftWrist > 0) && ismeasuring) {
                        maxLeftWrist = pitch;
                    }
                    LeftMaxWrist.setText("Left Rotation: " + maxLeftWrist);
                    RightMaxWrist.setText("Right Rotation: " + maxRightWrist);
                });
            }
        });

        //This will hide the save button for guests
        Intent intent = getIntent();
        boolean isGuest = intent.getBooleanExtra("isGuest", false);

        // Hide the button if the user is a guest
        if (isGuest) {
            SaveButtonWrist.setVisibility(View.GONE);
        }

        StartButtonWrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ismeasuring) {
                    askForConfirmation_p();
                    if (userConfirmation) {
                        bleManager_p.startMeasuring();
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
                resetValues();
                userConfirmation = true;
                ismeasuring = true;
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
        LeftMaxWrist.setText("Left Rotation: " + maxLeftWrist);
        RightMaxWrist.setText("Right Rotation: " + maxRightWrist);
    }

}