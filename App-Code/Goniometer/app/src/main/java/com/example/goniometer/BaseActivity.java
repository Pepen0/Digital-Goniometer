package com.example.goniometer;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BaseActivity extends AppCompatActivity {


protected Toolbar toolbar;
private BluetoothAdapter bluetoothAdapter;
protected ImageButton bluetoothbutton;
private final BroadcastReceiver bluetoothReceiver= new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action=intent.getAction();
        if (action!= null);{
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        bluetoothbutton.setImageResource(R.drawable.ic_bluetooth_disabled);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        ;
                        bluetoothbutton.setImageResource(R.drawable.ic_bluetooth_connected);
                        break;
                }
            }



                }
            }


};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

    }
protected void setuptoobar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothbutton = findViewById(R.id.Bluetoothbutton);
        updatebluetoothbutton();

        bluetoothbutton.setOnClickListener(view->){
            if (bluetoothAdapter.isEnabled()) {
                bluetoothbutton.disable();
            }else{
                bluetoothAdapter.enable();
            }
            updatebluetoothbutton();
    }

    }
    private void updateBluetoothButton() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothButton.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            bluetoothButton.setImageResource(R.drawable.ic_bluetooth_disabled);
        }
    }



}
