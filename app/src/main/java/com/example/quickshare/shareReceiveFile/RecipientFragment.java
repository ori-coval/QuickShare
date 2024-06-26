package com.example.quickshare.shareReceiveFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.quickshare.Bluetooth.ConnectedThread;
import com.example.quickshare.Utils.CustomToast;
import com.example.quickshare.homePage.HomePageActivity;
import com.example.quickshare.R;
import com.example.quickshare.Utils.CONSTANTS;

import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecipientFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private AcceptThread acceptThread;
    private TextView fileSizeTextView;
    private Handler handler;
    private int fileSize;
    private ProgressBar progressBar;
    private AlertDialog.Builder builder;
    byte[] data;
    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipient, container, false);

        builder = new AlertDialog.Builder(requireActivity());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button cancelButton = view.findViewById(R.id.cancel_button);
        fileSizeTextView = view.findViewById(R.id.textFileSize);
        progressBar = view.findViewById(R.id.progressBar);

        handler = new Handler(requireActivity().getMainLooper()) {
            public void handleMessage(@NonNull Message msg) {
                FragmentActivity activity = getActivity();
                AlertDialog dialog;
                switch (msg.what) {
                    case CONSTANTS.MessageConstants.FINISHED:
                        byte[] readBuf = (byte[]) msg.obj;
                        handleReceivedFileData(readBuf);
                        break;

                    case CONSTANTS.MessageConstants.MESSAGE_READ_FILE_SIZE:
                        fileSize = (int)msg.obj;
                        progressBar.setVisibility(View.VISIBLE);
                        fileSizeTextView.setVisibility(View.VISIBLE);
                        fileSizeTextView.setText("File Size: " + fileSize + " MB");
                        break;

                    case CONSTANTS.MessageConstants.MESSAGE_PROGRESS:
                        int progress = 0;
                        try {
                            progress = ((Integer) msg.obj * 100) / fileSize ;
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        progressBar.setProgress(progress);
                        break;

                    case CONSTANTS.MessageConstants.MESSAGE_CONNECTED:
                        builder.setMessage("Connection Established")
                                .setTitle("");
                        dialog = builder.create();
                        dialog.show();
                        break;
                }
            }
        };



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
//            handleReceivedFileData(data);
                Intent intent = new Intent(requireActivity(), HomePageActivity.class);
                startActivity(intent);
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
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(CONSTANTS.misc.NAME, CONSTANTS.misc.MY_UUID);
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
            builder.setMessage("File saved successfully at " + receivedFile.toPath()).create();
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle file saving failure
        }
    }

    public static String detectFileType(byte[] fileBytes) {
        try (InputStream stream = new ByteArrayInputStream(fileBytes)) {
            Detector detector = new AutoDetectParser().getDetector();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, "filename");

            MediaType mediaType = detector.detect(TikaInputStream.get(stream), metadata);
            return mediaType.getSubtype(); // Get only the subtype
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
