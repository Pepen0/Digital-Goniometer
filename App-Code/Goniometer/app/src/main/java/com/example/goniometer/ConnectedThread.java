package com.example.goniometer;

import android.bluetooth.BluetoothSocket;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread {
    private static final String TAG = "Logs";
    private final BluetoothSocket cSocket;
    private final InputStream cInStream;
    private final OutputStream cOutStream;
    private String valueRead;

    public ConnectedThread(BluetoothSocket socket){
        cSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        //Get Input/Output streams
        try{
            tmpInt = socket.getInputStream();
        }catch (IOException e){
            Log.e(TAG, "Error occurred when creating input stream", e)  ;
        }try {
            tmpOut = socket.getOutputStream();
        }catch (IOException e){
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        cInStream =tmpIn;
        cOutStream = tmpOut;
    }
    public String getValueRead(){
        return valueRead;
    }
    public void run(){
        byte[] buffer = new byte[1024];
        int bytes = 0;
        int numberOfReadings = 0;
        while(numberOfReadings <1){
            //keep listening to the InputStream Until an Exception occurs
            try {
                buffer[bytes] = (byte) cInStream.read();
                String readMessage;
if (buffer[bytes] == '/n'){
    readMessage = new String(buffer, 0, bytes);
    log.e(TAG, readMessage);
    valueRead = readMessage;
    bytes = 0;
    numberOfReadings++;
}else{
    bytes++;
}
            }catch (IOException e){
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }
    //this method will shutdown the connection
    public void cancel(){
        try{
            cSocket.close();
        }catch (IOException e){
            Log.e(TAG, "could not close the connect socket",e);
        }
    }
}
