package com.example.quickshare.shareReceiveFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.quickshare.CONSTANTS;
import com.example.quickshare.DeviceListActivity;
import com.example.quickshare.MainActivity;
import com.example.quickshare.R;

import java.io.IOException;

public class FileSharingFragment extends Fragment {
    private TextView selectedFileInfo;
    private Uri selectedFileUri;
    private Context context;

    private ActivityResultLauncher<String> fileSelectionLauncher;
    private ActivityResultLauncher<Intent> bluetoothConnectLauncher;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private static BluetoothSocket socket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_sharing, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        askPermission();
        enableBluetooth();
        ensureDiscoverable();


        context = requireContext();

        // Initialize UI elements
        Button selectFileButton = view.findViewById(R.id.select_file_button);
        Button viaBluetoothButton = view.findViewById(R.id.via_bluetooth_button);
        Button viaNfcButton = view.findViewById(R.id.via_nfc_button);
        Button sendFileButton = view.findViewById(R.id.send_file_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        selectedFileInfo = view.findViewById(R.id.TV_file_info);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);

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
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                connectDevice(data);
                            }
                        }
                    }
                });

        viaBluetoothButton.setOnClickListener(view12 -> {
            Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
            bluetoothConnectLauncher.launch(serverIntent);
        });

        viaNfcButton.setOnClickListener(view13 -> {
            //TODO: Implement NFC recipient selection
        });

        sendFileButton.setOnClickListener(view14 -> {
            //TODO: Implement file sharing logic
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

        String filePath = returnCursor.getString(nameIndex);
        String fileSize = String.format("%.3f",returnCursor.getDouble(sizeIndex)/1000000) + " MB";
        String fileType = getActivity().getContentResolver().getType(fileUri);
        int index = 0;
        if (fileType != null) {
            index = fileType.indexOf('/');
        }
        fileType = fileType.substring(0, index);

        // Construct the file info text
        String fileInfoText = "Selected File: " + filePath + " (Size: " + fileSize + ", Type: " + fileType + ")";

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

    private void ensureDiscoverable() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            // Initialize Activity Result Launcher for enabling Bluetooth
            ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK) {
                            Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            // Create intent to enable Bluetooth
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBluetoothIntent);
        }
    }


    public void askPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 1);
            try {
                wait(750);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO: Add additional methods and logic as needed for recipient handling and file sharing.
}
