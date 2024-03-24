package com.example.quickshare.Bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.quickshare.CONSTANTS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ConnectedThread extends Thread {
    private static final int ACKNOWLEDGMENT_TIMEOUT = 3000;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handler;
    private byte[] mmBuffer; // mmBuffer store for the stream

    // Define a constant for acknowledgment message
    private static final byte ACKNOWLEDGMENT = 1;

    /**
     * @param socket  an already connected BluetoothSocket on which the connection was made.
     */
    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        if(socket == null||!socket.isConnected()) {
            cancel();
        }
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

    private int expectedBytes = -1; // Track expected bytes
    private int receivedBytes = 0; // Track received bytes]
    public void run() {
        mmBuffer = new byte[1024]; // Adjust buffer size as needed
        int numBytes; // bytes returned from read()

        ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream(); // Buffer to collect the entire message

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);

                // Write received bytes to the message buffer
                messageBuffer.write(mmBuffer, 0, numBytes);

                // Check for end of transmission
                if (isEndOfTransmission(mmBuffer)) {
                    // Send the obtained bytes to the UI activity.
                    byte[] messageBytes = messageBuffer.toByteArray(); // Get the collected message
                    String fileType = new String(messageBytes, 0, 4); // Extract file type

                    // Send the obtained bytes to the UI activity using handler
                    Message readMsg = handler.obtainMessage(
                            CONSTANTS.MessageConstants.MESSAGE_READ_FILE_TYPE, fileType);
                    readMsg.sendToTarget();

                    // Remove file type bytes from the message
                    byte[] dataBytes = Arrays.copyOfRange(messageBytes, 4, messageBytes.length);

                    // Send the actual data bytes to the UI activity using handler
                    readMsg = handler.obtainMessage(
                            CONSTANTS.MessageConstants.MESSAGE_READ, dataBytes.length, -1,
                            dataBytes);
                    readMsg.sendToTarget();

                    // Reset message buffer for the next message
                    messageBuffer.reset();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    // Method to check for end of transmission
    private boolean isEndOfTransmission(byte[] buffer) {
        // Define the end of transmission sequence
        byte[] endOfTransmissionSequence = END_SEQUENCE; // Example sequence: \r\n\r\n

        // Check if the buffer contains the end of transmission sequence
        int sequenceIndex = 0; // Index to track the position within the end of transmission sequence
        for (byte b : buffer) {
            if (b == endOfTransmissionSequence[sequenceIndex]) {
                sequenceIndex++; // Move to the next byte in the sequence
                if (sequenceIndex == endOfTransmissionSequence.length) {
                    return true; // End of transmission sequence found
                }
            } else {
                sequenceIndex = 0; // Reset the index if the byte doesn't match the sequence
            }
        }
        return false; // End of transmission sequence not found
    }



    private static final byte[] START_LENGTH_SEQUENCE = {0x01, 0x02, 0x03, 0x04}; // Example start sequence for length
    private static final byte[] START_FILE_TYPE_SEQUENCE = {0x05, 0x06, 0x07, 0x08}; // Example start sequence for file type
    private static final byte[] START_DATA_SEQUENCE = {0x09, 0x0A, 0x0B, 0x0C}; // Example start sequence for data
    private static final byte[] END_SEQUENCE = {0x0D, 0x0E, 0x0F, 0x10}; // Example end sequence

    // Modify your ConnectedThread class to handle chunking and writing from URI
// Modify your ConnectedThread class to handle chunking and writing from URI
    public void write(Uri fileUri, Context context) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);

            // Create a buffer for reading from the input stream
            byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer, you can adjust this size as needed
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                mmOutStream.write(START_DATA_SEQUENCE);
                mmOutStream.write(buffer, 0, bytesRead);
                // No need to write END_SEQUENCE here
            }

            // After all chunks have been sent, write the END_SEQUENCE
            mmOutStream.write(END_SEQUENCE);

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle IO exception
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
