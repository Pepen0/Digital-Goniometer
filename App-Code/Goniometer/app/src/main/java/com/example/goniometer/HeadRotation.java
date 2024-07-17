package com.example.goniometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HeadRotation extends AppCompatActivity {

    protected Button StartButton;
    protected Button SaveButton;
    protected TextView LeftM;
    protected TextView RightM;
    protected TextView Livedata;
    private BLEManager bleManager;

    private float maxLeft = 0;
    private float maxRight = 0;
    private boolean ismeasuring = false;

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
            public void onDataReceived(float yaw) {
                runOnUiThread(() -> {
                    Livedata.setText(String.format("Yaw: %.2f", yaw));

                    if (yaw < 0 && (yaw+maxRight < 0) && ismeasuring) {
                        maxRight = -yaw;
                    }
                    if (yaw > 0 && (yaw-maxLeft > 0) && ismeasuring) {
                        maxLeft = yaw;
                    }
                    LeftM.setText("Left Rotation: " + maxLeft);
                    RightM.setText("Right Rotation: " + maxRight);
                });
            }
        });

        //This will hide the save button for guests
        Intent intent = getIntent();
        boolean isGuest = intent.getBooleanExtra("isGuest", false);

        // Hide the button if the user is a guest
        if (isGuest) {
            SaveButton.setVisibility(View.GONE);
        }


        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleManager.startMeasuring();
                if (ismeasuring) {
                    ismeasuring = false;
                    StartButton.setText("Start Measuring");
                    bleManager.askforConfirmation();
                }else {
                    ismeasuring = true;
                    StartButton.setText("Stop Measuring");
                }
            }
        });

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}