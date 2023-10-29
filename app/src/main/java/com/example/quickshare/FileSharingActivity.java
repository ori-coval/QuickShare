package com.example.quickshare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class FileSharingActivity extends Activity {

    private Button selectFileButton;
    private Button viaBluetoothButton;
    private Button viaNfcButton;
    private Button sendFileButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        context = getApplicationContext();

        // Initialize UI elements
        selectFileButton = findViewById(R.id.select_file_button);
        viaBluetoothButton = findViewById(R.id.via_bluetooth_button);
        viaNfcButton = findViewById(R.id.via_nfc_button);
        sendFileButton = findViewById(R.id.send_file_button);
        cancelButton = findViewById(R.id.cancel_button);
        progressBar = findViewById(R.id.progress_bar);

        // Set initial button states
        sendFileButton.setEnabled(false);

        // Set click listeners for buttons
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
                // Implement file selection logic
            }
        });

        viaBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement Bluetooth recipient selection
            }
        });

        viaNfcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement NFC recipient selection
            }
        });

        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement file sharing logic
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Close the File Sharing Activity
            }
        });
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_share_screen){
            finish();
        }

        if( id == R.id.action_home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
