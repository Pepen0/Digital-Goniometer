package com.example.goniometer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Color;
import android.os.Bundle;
<<<<<<< HEAD
import android.util.Log;
=======
>>>>>>> 2cc1387 (added new measurement)
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

public class RightLegRotation extends AppCompatActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView LeftM;
    protected TextView RightM;
    protected TextView Livedata;
    private BLEManager bleManager;

    private float maxLeft = 0;
    private float maxRight = 0;
    private boolean isMeasuring = false;

    private DatabaseHelper dbHelper;
    private long patientId; // This should be passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_right_leg_rotation);
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
<<<<<<< HEAD
        LeftM = findViewById(R.id.AbductionM);
        RightM = findViewById(R.id.Roll);
        Livedata = findViewById(R.id.Livedata);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback((Yaw, Pitch, Roll, Debug) -> runOnUiThread(() -> {
            if (Yaw < 0 && (Yaw + maxRight < 0) && isMeasuring) {
                maxRight = -Yaw;
            }
            if (Yaw > 0 && (Yaw-maxLeft > 0) && isMeasuring) {
                maxLeft = Yaw;
            }
            if (Debug.equals("Reset")) {
                maxLeft = 0;
                maxRight = 0;
            }
            LeftM.setText("Left Rotation: " + maxLeft);
            RightM.setText("Right Rotation: " + maxRight);
            Livedata.setText("Yaw: "+ Yaw);
        }));

=======
        LeftM = findViewById(R.id.RightAbductionM);
        RightM = findViewById(R.id.RRoll);
>>>>>>> 2cc1387 (added new measurement)

        StartButton.setOnClickListener(v -> {
            if (!isMeasuring) {
                askForConfirmation();
            } else {
                isMeasuring = false;
                StartButton.setText("START");
                StartButton.setBackgroundResource(R.drawable.circular_button_start);
                SaveButton.setBackgroundResource(R.drawable.custom_button2);
                SaveButton.setText("Save Measurement");
            }
        });
        SaveButton.setOnClickListener(v ->{
            if(!isMeasuring) {
                saveMeasurement();
            }
        });
    }

    private void saveMeasurement() {
        // Dummy data for testing
        double leftAngle = maxLeft;
        double rightAngle = maxRight;
        String measurementType = "Right Leg";

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
        SaveButton.setVisibility(View.GONE);
    }

    private void askForConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Measuring Confirmation");
        builder.setMessage("Are you sure you want to start a new measurement?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            isMeasuring = true;
            bleManager.sendDataToArduino("Reset data");
            Log.d("Reset command sent", "Reset data");
            StartButton.setText("STOP");
            StartButton.setBackgroundResource(R.drawable.circular_button_stop);
            SaveButton.setBackgroundColor(Color.GRAY);
            SaveButton.setVisibility(View.VISIBLE);
            SaveButton.setText("Stop Measuring To Save");
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
