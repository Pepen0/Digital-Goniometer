package com.example.goniometer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.DialogInterface;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HeadRotation extends AppCompatActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView LeftM;
    protected TextView RightM;
    BluetoothAdapter adapter;
    protected TextView Livedata;
    private BLEManager bleManager;

    private float maxLeft = 0;
    private float maxRight = 0;
    private boolean ismeasuring = false;
    private boolean userConfirmation = false;

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
        setupUI();
    }

    private void setupUI() {
        StartButton = findViewById(R.id.StartButton);
        SaveButton = findViewById(R.id.SaveButton);
        LeftM = findViewById(R.id.LeftM);
        RightM = findViewById(R.id.RightM);
        Livedata = findViewById(R.id.Livedata);
        bleManager = BLEManager.getInstance();

        bleManager.setDataCallback(new BLEManager.DataCallback() {
            @Override
            public void onDataReceived(int Yaw, int Pitch, int Roll, String Debug) {
                runOnUiThread(() -> {
                    Livedata.setText("Yaw: "+ Yaw);
                    if (Yaw < 0 && (Yaw + maxRight < 0) && ismeasuring) {
                        maxRight = -Yaw;
                    }
                    if (Yaw > 0 && (Yaw-maxLeft > 0) && ismeasuring) {
                        maxLeft = Yaw;
                    }
                    LeftM.setText("Left Rotation: " + maxLeft);
                    RightM.setText("Right Rotation: " + maxRight);
               });
            }
        });

        // This will hide the save button for guests
//        Intent intent = getIntent();
//        boolean isGuest = intent.getBooleanExtra("isGuest", false);

        // Hide the button if the user is a guest
//        if (isGuest) {
//            SaveButton.setVisibility(View.GONE);
//        }

        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ismeasuring) {
                    askforConfirmation();
                    Log.d("userConfirmation", String.valueOf(userConfirmation));
                } else {
                    ismeasuring = false;
                    StartButton.setText("Start Measuring");
                }
            }

        });
        SaveButton.setOnClickListener(v -> saveMeasurement());
    }

    private void saveMeasurement() {
        // Dummy data for testing
        double leftAngle = 10;
        double rightAngle = 10;
        String measurementType = "HeadRotation";

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
    }

    private void askforConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Measuring Confirmation");
        builder.setMessage("Are you sure you want to start a new measurement?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetValues();
                userConfirmation = true;
                ismeasuring = true;
                bleManager.startMeasuring();
                bleManager.sendDataToArduino("Reset data");
                Log.d("Reset command sent", "Reset data");
                StartButton.setText("Stop Measuring");
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

    private void resetValues() {
        maxLeft = 0;
        maxRight = 0;
        LeftM.setText("Left Rotation: " + maxLeft);
        RightM.setText("Right Rotation: " + maxRight);
        bleManager.setReset(true);
    }
}
