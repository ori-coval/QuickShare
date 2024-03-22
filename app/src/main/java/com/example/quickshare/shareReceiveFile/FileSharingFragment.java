package com.example.quickshare.shareReceiveFile;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.quickshare.Bluetooth.ConnectThread;
import com.example.quickshare.Bluetooth.ConnectedThread;
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
    private static BluetoothSocket socket;
    private boolean hasFile = false;
    private String fileType;
    private String fileSize;
    private String filePath;
    private SharedFile sharedFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_sharing, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        connectDevice(data);
                        if (connectThread != null && connectThread.isAlive()) {
//                            connectedThread = new ConnectedThread(socket);
                            if (hasFile) {
                                sendFileButton.setEnabled(true);
                            }
                        }
                        else {
                            sendFileButton.setEnabled(false);
                        }
                    }
                });

        viaBluetoothButton.setOnClickListener(view12 -> {
            Intent serverIntent = new Intent(requireActivity(), DeviceListActivity.class);
            startActivityForResult(serverIntent, 2);
            bluetoothConnectLauncher.launch(serverIntent);
        });

        viaNfcButton.setOnClickListener(view13 -> {
            //TODO: Implement NFC recipient selection
        });

        sendFileButton.setOnClickListener(view14 -> {
            if (socket != null && hasFile) {
                sharedFile = new SharedFile(filePath, fileType, LocalDate.now().toString(), fileSize);
                ConnectedThread connectedThread = new ConnectedThread(socket);
                File file = new File(filePath);
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(sharedFile.getFilePath());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                byte[] bytes = new byte[(int) file.length()];
                try {
                    inputStream.read(bytes);
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                connectedThread.write( bytes);
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

        filePath = returnCursor.getString(nameIndex);
        fileSize = String.format("%.3f", returnCursor.getDouble(sizeIndex) / 1000000) + " MB";
        fileType = getActivity().getContentResolver().getType(fileUri);
        int index = 0;
        if (fileType != null) {
            index = fileType.indexOf('/');
        }
        fileType = fileType.substring(0, index);

        // Construct the file info text
        String fileInfoText = "Selected File: " + filePath + " (Size: " + fileSize + ", Type: " + fileType + ")";
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
}
