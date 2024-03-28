package com.example.quickshare.shareReceiveFile;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.quickshare.Bluetooth.ConnectedThread;
import com.example.quickshare.Bluetooth.DeviceListActivity;
import com.example.quickshare.DB.DataBaseHelper;
import com.example.quickshare.homePage.HomePageActivity;
import com.example.quickshare.R;
import com.example.quickshare.Utils.CONSTANTS;
import com.example.quickshare.Utils.CustomToast;
import com.example.quickshare.sharedFiles.SharedFile;

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
    private ConnectedThread connectedThread;
    private static BluetoothSocket socket;
    private boolean hasFile = false;
    private String fileType;
    private int fileSize;
    private String filePath;
    private SharedFile sharedFile;
    private String fileInfoText;
    private DataBaseHelper dataBaseHelper;
    private boolean isSendingFile = false;
    private byte[] fileData;

    public FileSharingFragment() {

    }

    public FileSharingFragment(String filePath, String fileType, int fileSize, byte[] fileData) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileData = fileData;

        fileInfoText = "Selected File: " + filePath + " (Size: " + fileSize + ", Type: " + fileType + ")";
        hasFile = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_sharing, container, false);

        dataBaseHelper = new DataBaseHelper(requireActivity());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize UI elements
        Button selectFileButton = view.findViewById(R.id.select_file_button);
        Button viaBluetoothButton = view.findViewById(R.id.via_bluetooth_button);
        Button sendFileButton = view.findViewById(R.id.send_file_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        selectedFileInfo = view.findViewById(R.id.TV_file_info);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);

        // Set initial file info
        if(hasFile) {
            selectedFileInfo.setText(fileInfoText);
        }

        if(fileData != null) {
            isSendingFile = true;
        }

        Handler handler = new Handler(requireActivity().getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CONSTANTS.MessageConstants.FINISHED) {
                    progressBar.setVisibility(View.GONE);
                }

                if (msg.what == CONSTANTS.MessageConstants.STARTED) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        };

        // Set initial button states
        sendFileButton.setEnabled(false);

        // Set click listeners for buttons
        selectFileButton.setOnClickListener(view1 -> {
            chooseFile();
        });


        // Initialize Activity Result Launcher
        bluetoothConnectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        CustomToast.showWithDuration(getContext(), "Connecting", 0.5);
                        for(int i = 0; i <= 5; i++) {
                            connectDevice(data);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            if(socket!=null && socket.isConnected())
                                break;
                            if(i == 5) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                Intent serverIntent = new Intent(requireActivity(), DeviceListActivity.class);
                                serverIntent.putExtra(CONSTANTS.MessageConstants.MESSAGE_TOAST, "Connection Failed try again");
                                bluetoothConnectLauncher.launch(serverIntent);
                            }
                        }

                        if(socket!=null && socket.isConnected()){
                            sendFileButton.setEnabled(hasFile);
                        }
                    }
                });

        viaBluetoothButton.setOnClickListener(view12 -> {
            Intent serverIntent = new Intent(requireActivity(), DeviceListActivity.class);
            bluetoothConnectLauncher.launch(serverIntent);
        });

        sendFileButton.setOnClickListener(view14 -> {
            if (socket != null && socket.isConnected()) {
                if(hasFile){
                    connectedThread=new ConnectedThread(socket,handler);
                    progressBar.setVisibility(View.VISIBLE);
                    byte[] bytes;

                    if(isSendingFile){
                        bytes = this.fileData;
                    }
                    else {
                        InputStream inputStream;
                        try {
                            inputStream = requireContext().getContentResolver().openInputStream(selectedFileUri);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        bytes = new byte[fileSize];
                        try {
                            inputStream.read(bytes);
                            inputStream.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    sharedFile = new SharedFile(filePath, fileType, LocalDate.now().toString(), fileSize, bytes);
                    connectedThread.write(bytes,fileType, fileSize, requireActivity());
                    dataBaseHelper.insertSharedFile(sharedFile);

                }
            }
            else{
                CustomToast.showWithDuration(getContext(), "bluetooth not connected", 0.5);

                Intent serverIntent = new Intent(requireActivity(), DeviceListActivity.class);
                bluetoothConnectLauncher.launch(serverIntent);
            }
        });


        cancelButton.setOnClickListener(view15 -> {
            Intent intent = new Intent(requireActivity(), HomePageActivity.class);
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
                    if ( socket != null && socket.isConnected() && hasFile) {
                        sendFileButton.setEnabled(true);
                    }
                });

        return view;
    }

    private void chooseFile() {
        fileSelectionLauncher.launch("*/*");
    }

    @SuppressLint("DefaultLocale")
    private void handleSelectedFile(Uri fileUri) {
        // Handle the selected file URI here
        Cursor returnCursor = requireActivity().getContentResolver().query(fileUri, null, null, null, null);

        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        filePath = fileUri.getPath();
        fileSize = returnCursor.getInt(sizeIndex);
        fileType = requireActivity().getContentResolver().getType(fileUri);
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
        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private class ConnectThread extends Thread {
        public BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
                tmp = device.createRfcommSocketToServiceRecord(CONSTANTS.misc.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 33);
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
    }
}
