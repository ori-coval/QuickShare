package com.example.quickshare.shareReceiveFile;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.quickshare.Bluetooth.ConnectedThread;
import com.example.quickshare.CONSTANTS;
import com.example.quickshare.MainActivity;
import com.example.quickshare.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;

public class RecipientFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private AcceptThread acceptThread;
    private TextView textView;
    private Handler handler;

    byte[] data;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipient, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case CONSTANTS.MessageConstants.MESSAGE_FILE_RECEIVED:
                        data = (byte[]) msg.obj;
                        return true;
                    case CONSTANTS.MessageConstants.MESSAGE_READ_FILE:
                        // Handle the read file message
                        textView.setText(msg.obj.toString());
                        return true;
                }
                return false;
            }
        });

        Button cancelButton = view.findViewById(R.id.cancel_button);
        textView = view.findViewById(R.id.textView1);

        acceptThread = new AcceptThread((RecipientFragment) getParentFragment());
        acceptThread.start();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 33);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_MEDIA_VIDEO}, 33);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_MEDIA_AUDIO}, 33);
        }

        cancelButton.setOnClickListener(view1 -> {
            handleReceivedFileData(data);
//                Intent intent = new Intent(requireActivity(), MainActivity.class);
//                startActivity(intent);
        });

        return view;
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public BluetoothSocket socket;

        public AcceptThread(RecipientFragment recipientFragment) {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(CONSTANTS.NAME, CONSTANTS.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }


        public void run() {
            socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    SendReceiveFileActivity.socket = socket;
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    //manageMyConnectedSocket(socket);
                    try {
                        ConnectedThread connectedThread = new ConnectedThread(socket, handler);
                        connectedThread.start();
                        mmServerSocket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final int REQUEST_WRITE_STORAGE = 112;

    private void handleReceivedFileData(byte[] data) {
        // Check if write permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        // Generate filename with date, time, and file type
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentTimeStamp = dateFormat.format(new Date());
        String filename = "received_file_" + currentTimeStamp + "." + detectFileType(data);

        // Define the directory path
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "QuickShare");
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        // Save the received file to the device's storage within the QuickShare directory
        File receivedFile = new File(directory, filename);
        try (OutputStream outputStream = new FileOutputStream(receivedFile)) {
            outputStream.write(data);
            // File saved successfully, show a message or perform any necessary action
            Toast.makeText(requireContext(), "File saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle file saving failure
        }
    }

    public static String detectFileType(byte[] fileBytes) {
        try (InputStream stream = new ByteArrayInputStream(fileBytes)) {
            Detector detector = new AutoDetectParser().getDetector();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, "filename"); // You can set the filename here if available

            MediaType mediaType = detector.detect(TikaInputStream.get(stream), metadata);
            return mediaType.getSubtype(); // Get only the subtype
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
