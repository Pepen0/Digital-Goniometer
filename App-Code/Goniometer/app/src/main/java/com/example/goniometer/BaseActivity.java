package com.example.goniometer;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected ImageView bluetoothStatus;
    protected BLEManager bleManager;
    protected ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Ensure the correct layout is set

        // Initialize BLEManager
        bleManager = BLEManager.getInstance();

        // Setup toolbar
        setupToolbar();

        // Set up connection callback
        bleManager.setConnectionCallback(new BLEManager.ConnectionCallback() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> updateUI(true));
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> updateUI(false));
            }
        });
    }

    protected void setupToolbar() {
        bluetoothStatus = findViewById(R.id.bluetooth_status);
        backButton = findViewById(R.id.Back_Button);

        if (bluetoothStatus == null) {
            Log.e("BaseActivity", "Bluetooth status ImageView not found");
        }
        if (backButton == null) {
            Log.e("BaseActivity", "Back button ImageButton not found");
        } else {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }


    protected void updateUI(boolean isConnected) {
        if (isConnected) {
            bluetoothStatus.setImageResource(R.drawable.baseline_bluetooth_connected_24);
        } else {
            bluetoothStatus.setImageResource(R.drawable.baseline_bluetooth_disabled_24);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //bleManager.disconnect();
    }

    public abstract void onPatientDeleted(int position);
}
