package com.example.quickshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FileSharingFragment extends Fragment {

    private Button selectFileButton;
    private Button viaBluetoothButton;
    private Button viaNfcButton;
    private Button sendFileButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_sharing, container, false);

        context = requireContext();

        // Initialize UI elements
        selectFileButton = view.findViewById(R.id.select_file_button);
        viaBluetoothButton = view.findViewById(R.id.via_bluetooth_button);
        viaNfcButton = view.findViewById(R.id.via_nfc_button);
        sendFileButton = view.findViewById(R.id.send_file_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set initial button states
        sendFileButton.setEnabled(false);

        // Set click listeners for buttons
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
                //TODO: Implement file selection logic
            }
        });

        viaBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Implement Bluetooth recipient selection
            }
        });

        viaNfcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Implement NFC recipient selection
            }
        });

        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Implement file sharing logic
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    // Rest of the code remains the same

    // Add additional methods and logic as needed for file selection, recipient handling, and file sharing.

    int requestCode = 1;

    public void  onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == requestCode && resultCode == Activity.RESULT_OK){
            if(data == null)
                return;
        }
        Uri uri = data.getData();

        Toast.makeText(context, uri.getPath(), Toast.LENGTH_SHORT).show();
    }

    public  void chooseFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }
}
