package com.example.quickshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private Button sharedFilesButton;
    private Button sendFileButton;
    private Button receiveFileButton;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT)== PackageManager.PERMISSION_DENIED){
            if(Build.VERSION.SDK_INT>=31){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            }
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT)== PackageManager.PERMISSION_DENIED){

        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        sharedFilesButton = findViewById(R.id.shared_files_button);
        sendFileButton = findViewById(R.id.send_file_button);
        receiveFileButton = findViewById(R.id.receive_file_button);

        // Set a click listener for the "Send File" button
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the File Sharing Activity
                Intent intent = new Intent(MainActivity.this, FileSharingActivity.class);
                startActivity(intent);
            }
        });

        // Set a click listener for the "receive File" button
        receiveFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the File Sharing Activity
                Intent intent = new Intent(MainActivity.this, RecipientActivity.class);
                startActivity(intent);
            }
        });

        // Set a click listener for the "Shared Files" button
        sharedFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the Shared Files Activity
                Intent intent = new Intent(MainActivity.this, SharedFilesHistoryActivity.class);
                startActivity(intent);
            }
        });


    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder mb = (MenuBuilder) menu;
            mb.setOptionalIconsVisible(true);
        }
        MenuItem homeAction = menu.findItem(R.id.action_home);
        homeAction.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_share_screen){
            Intent intent = new Intent(MainActivity.this, FileSharingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}