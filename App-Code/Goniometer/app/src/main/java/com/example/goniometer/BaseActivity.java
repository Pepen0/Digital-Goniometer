//package com.example.goniometer;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.ImageButton;
//import android.widget.Toolbar;
//import android.bluetooth.BluetoothManager;
//
//
//
//
package com.example.goniometer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected ImageView bluetoothStatus;
    protected BLEManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize BLEManager
        bleManager = BLEManager.getInstance();
        if (bleManager == null) {
            bleManager = new BLEManager(this);
        }

        // Set up connection callback
        bleManager.setConnectionCallback(new BLEManager.ConnectionCallback() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> bluetoothStatus.setImageResource(R.drawable.baseline_bluetooth_connected_24));
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> bluetoothStatus.setImageResource(R.drawable.baseline_bluetooth_disabled_24));
            }
        });
    }

    protected void setupToolbar() {
        bluetoothStatus = findViewById(R.id.bluetooth_status);
        ImageButton backButton = findViewById(R.id.Back_Button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bleManager.disconnect();
    }
}