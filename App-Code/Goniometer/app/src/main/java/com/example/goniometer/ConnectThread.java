package com.example.goniometer;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final BluetoothSocket cSocket;
    private static final String TAG = "logs";
    public static Handler handler;
    private final static int ERROR_Read = 0;

    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler){
        BluetoothSocket tmp= null;
        this.handler = handler;
        try{
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        }catch (IOException e){
            Log.e(TAG, "Socket's create() method failed", e);
        }
        cSocket = tmp;
    }
    public void run(){
        try{
            cSocket.connect();
        }catch (IOException connectException){
            handler.obtainMessage(ERROR_Read, "Unable to connect to the BT device").sendToTarget();
            Log.e(TAG, "connectException: "+ connectException);
            try {
                cSocket.close();
            }catch (IOException closeException){
        Log.e(TAG, "could not close the client socket", closeException);
            }
            return;
    }
        public void cancel(){
            try{
                cSocket.close();
            }catch (IOException e){
                Log.e(TAG,"Could not close the client socket", e);
            }
        }
public BluetoothSocket getCSocket(){
            return cSocket; }
        }



}
