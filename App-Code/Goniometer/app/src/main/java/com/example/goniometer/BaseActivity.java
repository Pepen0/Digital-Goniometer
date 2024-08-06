package com.example.goniometer;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
        if (bleManager == null) {
            Log.e("BaseActivity", "BLEManager not initialized");
            return;
        }

        // Setup toolbar
        setupToolbar();

        // Set up connection callback
        bleManager.setConnectionCallback(new BLEManager.ConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d("BaseActivity", "Connected to BLE device");
                runOnUiThread(() -> updateUI(true));
            }

            @Override
            public void onDisconnected() {
                Log.d("BaseActivity", "Disconnected from BLE device");
                runOnUiThread(() -> updateUI(false));
            }
        });

        // Initialize UI based on current connection status
        updateUI(false);
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
        if (bluetoothStatus != null) {
            int color = isConnected ? R.color.bluetooth_connected : R.color.bluetooth_disconnected;
            Log.d("BaseActivity", "Updating Bluetooth status color to: " + (isConnected ? "Green" : "Red"));
            bluetoothStatus.setColorFilter(ContextCompat.getColor(this, color));
        } else {
            Log.e("BaseActivity", "Bluetooth status ImageView is null");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
