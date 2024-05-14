package com.example.quickshare.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.example.quickshare.Utils.CONSTANTS;
import com.example.quickshare.Utils.CustomToast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ConnectedThread extends Thread {
    private static final int ACKNOWLEDGMENT_TIMEOUT = 3000;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handler;
    private byte[] mmBuffer; // mmBuffer store for the stream

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

    private int expectedBytes = -1; // Track expected bytes
    private int receivedBytes = 0; // Track received bytes]
    public void run() {
        mmBuffer = new byte[1024*1024];
        int numBytes; // bytes returned from read()

        ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream(); // Buffer to collect the entire message

        Message connectedMsg = handler.obtainMessage(
                CONSTANTS.MessageConstants.MESSAGE_CONNECTED, expectedBytes);
        connectedMsg.sendToTarget();


        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                receivedBytes += numBytes;

                // Check if expectedBytes is not set yet
                // Check if start sequence for length is received
                if (startsWith(mmBuffer, START_LENGTH_SEQUENCE)) {
                    // Extract length information
                    ByteBuffer buffer = ByteBuffer.wrap(mmBuffer, START_LENGTH_SEQUENCE.length, 4);
                    expectedBytes = buffer.getInt();
                    receivedBytes = 0; // Reset received bytes count
                    Message fileSizeMsg = handler.obtainMessage(
                            CONSTANTS.MessageConstants.MESSAGE_READ_FILE_SIZE);
                    fileSizeMsg.obj = expectedBytes;
                    fileSizeMsg.sendToTarget();

                    // Adjust mmBuffer size to read actual data
                    mmBuffer = new byte[1024*1024];

                }
                else {
                    // Write received bytes to the message buffer
                    messageBuffer.write(mmBuffer, 0, numBytes);
                    Message receivedBytesMsg = handler.obtainMessage(
                            CONSTANTS.MessageConstants.MESSAGE_PROGRESS, receivedBytes);
                    receivedBytesMsg.sendToTarget();
                }

                // Check for end of transmission
                if (isEndOfTransmission(mmBuffer)) {
                    // Send the obtained bytes to the UI activity.
                    byte[] messageBytes = messageBuffer.toByteArray(); // Get the collected message

                    // Send the actual data bytes to the UI activity using handler
                    Message readMsg = handler.obtainMessage(
                            CONSTANTS.MessageConstants.FINISHED, messageBytes.length, -1,
                            messageBytes);
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


    // Method to check if a byte array starts with a specific sequence
    private boolean startsWith(byte[] array, byte[] sequence) {
        if (array.length < sequence.length) {
            return false;
        }

        for (int i = 0; i < sequence.length; i++) {
            if (array[i] != sequence[i]) {
                return false;
            }
        }

        return true;
    }

    private static final byte[] START_LENGTH_SEQUENCE = {0x01, 0x02, 0x03, 0x04}; // Example start sequence for length
    private static final byte[] START_FILE_TYPE_SEQUENCE = {0x05, 0x06, 0x07, 0x08}; // Example start sequence for file type
    private static final byte[] START_DATA_SEQUENCE = {0x09, 0x0A, 0x0B, 0x0C}; // Example start sequence for data
    private static final byte[] END_SEQUENCE = {0x0D, 0x0E, 0x0F, 0x10}; // Example end sequence

    // Method to write data to the output stream
    public void write(byte[] bytes, String fileType, int fileSize, Activity activity) {
        try {



            // Create a byte array that contains START_LENGTH_SEQUENCE and bytes.length
            byte[] combined = new byte[START_LENGTH_SEQUENCE.length + 4];
            // Copy START_LENGTH_SEQUENCE to combined array
            System.arraycopy(START_LENGTH_SEQUENCE, 0, combined, 0, START_LENGTH_SEQUENCE.length);
            // Convert length of bytes to byte array
            byte[] lengthBytes = new byte[] {
                    (byte) ((bytes.length >> 24) & 0xFF),
                    (byte) ((bytes.length >> 16) & 0xFF),
                    (byte) ((bytes.length >> 8) & 0xFF),
                    (byte) (bytes.length & 0xFF)
            };
            // Copy lengthBytes to combined array
            System.arraycopy(lengthBytes, 0, combined, START_LENGTH_SEQUENCE.length, 4);

            mmOutStream.write(combined);

            Message startedMsg = handler.obtainMessage(
                    CONSTANTS.MessageConstants.STARTED);
            startedMsg.sendToTarget();

            Thread.sleep(100);
            // Write the actual data
            mmOutStream.write(bytes);

            // Write the finish sequence for data
            mmOutStream.write(END_SEQUENCE);

            Message finishedMsg = handler.obtainMessage(
                    CONSTANTS.MessageConstants.FINISHED);
            finishedMsg.sendToTarget();

        } catch (IOException e) {
            e.printStackTrace();
            CustomToast.showWithDuration(activity, "Error: blueTooth not connected" + e.getMessage(), 0.5);
            // Handle write failure
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
