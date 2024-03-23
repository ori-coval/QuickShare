package com.example.quickshare.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.quickshare.CONSTANTS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private static final int ACKNOWLEDGMENT_TIMEOUT = 3000;
    private static final int BUFFER_SIZE = 1024;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handler;
    private byte[] mmBuffer; // mmBuffer store for the stream

    // Define a constant for acknowledgment message
    private static final byte ACKNOWLEDGMENT = 1;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.handler = handler; // Initialize handler

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    // Method to send acknowledgment message
    private void sendAcknowledgment() {
        try {
            mmOutStream.write(ACKNOWLEDGMENT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        // Allocate a buffer to read data
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        // Keep reading from the InputStream until an exception occurs
        while (true) {
            try {
                // Read data into the buffer
                bytesRead = mmInStream.read(buffer);

                // Check if bytesRead is -1, indicating the end of the stream
                if (bytesRead == -1) {
                    // End of stream reached, break the loop
                    break;
                }

                // Send the obtained bytes to the UI activity or fragment
                Message readMsg = handler.obtainMessage(
                        CONSTANTS.MessageConstants.MESSAGE_READ_FILE, bytesRead, -1,
                        buffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle read failure
                break;
            }
        }

        // Notify the recipient fragment that the file has been received
        Message fileReceivedMsg = handler.obtainMessage(CONSTANTS.MessageConstants.MESSAGE_FILE_RECEIVED);
        handler.sendMessage(fileReceivedMsg);
    }



    public void write(final byte[] bytes) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int totalBytes = bytes.length;
                    int bytesWritten = 0;

                    while (bytesWritten < totalBytes) {
                        int bytesToWrite = Math.min(totalBytes - bytesWritten, 1024); // Adjust buffer size as needed
                        mmOutStream.write(bytes, bytesWritten, bytesToWrite);
                        bytesWritten += bytesToWrite;

                        // Calculate progress
                        int progress = (int) ((bytesWritten / (float) totalBytes) * 100);

                        // Send progress to UI using handler
                        Message progressMsg = handler.obtainMessage(CONSTANTS.MessageConstants.MESSAGE_PROGRESS);
                        Bundle bundle = new Bundle();
                        bundle.putInt("progress", progress);
                        progressMsg.setData(bundle);
                        handler.sendMessage(progressMsg);
                    }

                    // After writing, wait for acknowledgment with delay
                    waitForAcknowledgmentWithDelay();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle write failure
                }
            }
        });
    }

    // Method to wait for acknowledgment with a delay
    private void waitForAcknowledgmentWithDelay() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                waitForAcknowledgment();
            }
        }, ACKNOWLEDGMENT_TIMEOUT);
    }

    // Method to wait for acknowledgment
    private void waitForAcknowledgment() {
        try {
            int acknowledgment = mmInStream.read();
            if (acknowledgment != ACKNOWLEDGMENT) {
                // Handle acknowledgment failure
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle acknowledgment failure
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
