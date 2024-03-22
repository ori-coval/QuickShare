package com.example.quickshare.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.quickshare.CONSTANTS;
import com.example.quickshare.shareReceiveFile.FileSharingFragment;

import java.io.IOException;

public class ConnectThread extends Thread {
    private BluetoothSocket mmSocket;
    private BluetoothAdapter bluetoothAdapter;

    public ConnectThread(BluetoothDevice device) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(CONSTANTS.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
    }

    public void run() {
        bluetoothAdapter.cancelDiscovery();
        try {
            mmSocket.connect();
            FileSharingFragment.socket = mmSocket;
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}