package com.example.quickshare.shareReceiveFile;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.quickshare.Bluetooth.ConnectedThread;
import com.example.quickshare.CONSTANTS;
import com.example.quickshare.MainActivity;
import com.example.quickshare.R;
import com.example.quickshare.sharedFiles.SharedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class FileSharingFragment extends Fragment {
    private TextView selectedFileInfo;
    private Uri selectedFileUri;
    private ActivityResultLauncher<String> fileSelectionLauncher;
    private ActivityResultLauncher<Intent> bluetoothConnectLauncher;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private static BluetoothSocket socket;
    private boolean hasFile = false;
    private String fileType;
    private int fileSize;
    private String filePath;
    private SharedFile sharedFile;
    private Handler handler;
    private String fileInfoText;

    public FileSharingFragment() {

    }

    public FileSharingFragment(String filePath, String fileType, int fileSize) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;

        sharedFile = new SharedFile(filePath, fileType, LocalDate.now().toString(), fileSize);
        fileInfoText = "Selected File: " + filePath + " (Size: " + fileSize + ", Type: " + fileType + ")";
        hasFile = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_sharing, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(requireActivity().getMainLooper());

        // Initialize UI elements
        Button selectFileButton = view.findViewById(R.id.select_file_button);
        Button viaBluetoothButton = view.findViewById(R.id.via_bluetooth_button);
        Button viaNfcButton = view.findViewById(R.id.via_nfc_button);
        Button sendFileButton = view.findViewById(R.id.send_file_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        selectedFileInfo = view.findViewById(R.id.TV_file_info);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);

        // Set initial file info
        if(hasFile) {
            selectedFileInfo.setText(fileInfoText);
        }

        Handler handler = new Handler(requireActivity().getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CONSTANTS.MessageConstants.MESSAGE_PROGRESS) {
                    int progress = msg.getData().getInt("progress");
                    progressBar.setProgress(progress);
                }
            }
        };

        // Set initial button states
        sendFileButton.setEnabled(false);

        // Set click listeners for buttons
        selectFileButton.setOnClickListener(view1 -> {
            chooseFile();
            //TODO: Implement file selection logic
        });


        // Initialize Activity Result Launcher
        bluetoothConnectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Toast.makeText(getContext(), "Connecting", Toast.LENGTH_SHORT).show();
                        for(int i = 0; i < 5; i++) {
                            connectDevice(data);
                            if(socket!=null && socket.isConnected())
                                break;
                            if(i == 4) {
                                Toast.makeText(getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                                Intent serverIntent = new Intent(requireActivity(), DeviceListActivity.class);
                                bluetoothConnectLauncher.launch(serverIntent);
                            }
                        }

                        if(socket!=null && socket.isConnected()){
                            if (hasFile) {
                                sendFileButton.setEnabled(true);
                            }
                            else {
                                sendFileButton.setEnabled(false);
                            }
                        }
                    }
                });

        viaBluetoothButton.setOnClickListener(view12 -> {
            Intent serverIntent = new Intent(requireActivity(), DeviceListActivity.class);
            bluetoothConnectLauncher.launch(serverIntent);
        });

        viaNfcButton.setOnClickListener(view13 -> {
            //TODO: Implement NFC recipient selection
        });


// Modify your sendFileButton click listener to pass the file URI directly
        sendFileButton.setOnClickListener(view14 -> {
            if (socket != null && hasFile) {
                connectedThread = new ConnectedThread(socket, handler);
                progressBar.setVisibility(View.VISIBLE);
                sharedFile = new SharedFile(filePath, fileType, LocalDate.now().toString(), fileSize);
                connectedThread.write(selectedFileUri, getContext());
            }
        });


        cancelButton.setOnClickListener(view15 -> {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);
        });

        // Initialize the file selection Activity Result Launcher
        fileSelectionLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        // Handle the selected file URI here
                        selectedFileUri = result;
                        handleSelectedFile(selectedFileUri);
                    }
                    if (connectThread != null && connectThread.isAlive() && hasFile) {
                        sharedFile = new SharedFile(filePath, fileType, LocalDate.now().toString(), fileSize);
                        sendFileButton.setEnabled(true);
                    }
                });

        return view;
    }

    private void chooseFile() {
        fileSelectionLauncher.launch("*/*");
    }

    private void handleSelectedFile(Uri fileUri) {
        // Handle the selected file URI here
        Cursor returnCursor = getActivity().getContentResolver().query(fileUri, null, null, null, null);
        int nameIndex = 0;
        if (returnCursor != null) {
            nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        }
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        filePath = fileUri.getPath();
        fileSize = returnCursor.getInt(sizeIndex);
        fileType = getActivity().getContentResolver().getType(fileUri);
        int index = 0;
        if (fileType != null) {
            index = fileType.indexOf('/');
        }
        fileType = fileType.substring(index + 1);

        // Construct the file info text
        fileInfoText = "Selected File: " + filePath + " (Size: " + String.format("%.3f", returnCursor.getDouble(sizeIndex) / 1000000) + " MB" + ", Type: " + fileType + ")";
        hasFile = true;

        selectedFileInfo.setText(fileInfoText);
        returnCursor.close();
    }

    private void connectDevice(Intent data) {
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connect(device);
    }

    public synchronized void connect(BluetoothDevice device) {
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private class ConnectThread extends Thread {
        public BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
                tmp = device.createRfcommSocketToServiceRecord(CONSTANTS.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 33);
            }
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
}
