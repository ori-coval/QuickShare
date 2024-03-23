package com.example.quickshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.TextView;

import com.example.quickshare.DB.DataBaseHelper;
import com.example.quickshare.shareReceiveFile.SendReceiveFileActivity;
import com.example.quickshare.sharedFiles.SharedFile;
import com.example.quickshare.sharedFiles.SharedFilesAdapter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button sendFileButton;
    private Button receiveFileButton;
    private BluetoothAdapter bluetoothAdapter;

    private RecyclerView recyclerView;
    private TextView empty;
    private SharedFilesAdapter adapter;
    private List<SharedFile> sharedFiles;
    private DataBaseHelper dataBaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataBaseHelper = new DataBaseHelper(this);

        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT)== PackageManager.PERMISSION_DENIED){
            if(Build.VERSION.SDK_INT>=31){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            }
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT)== PackageManager.PERMISSION_DENIED){

        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        sendFileButton = findViewById(R.id.send_file_button);
        receiveFileButton = findViewById(R.id.receive_file_button);

        // Set a click listener for the "Send File" button
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the File Sharing Activity
                Intent sendFileIntent = new Intent(MainActivity.this, SendReceiveFileActivity.class);
                sendFileIntent.putExtra("default_fragment", CONSTANTS.SEND_FILE_POSE);
                startActivity(sendFileIntent);
            }
        });


        // Set a click listener for the "Send File" button
        receiveFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the File Sharing Activity
                Intent receiveFileIntent = new Intent(MainActivity.this, SendReceiveFileActivity.class);
                receiveFileIntent.putExtra("default_fragment", CONSTANTS.RECEIVE_FILE_POSE);
                startActivity(receiveFileIntent);
            }
        });

        dataBaseHelper.insertSharedFile(new SharedFile("Test","Test", LocalDate.now().toString(),"Test"));//TODO: Remove this
        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.shared_files_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        empty = findViewById(R.id.empty_state_message);

        // Initialize the adapter and set it to the RecyclerView
        sharedFiles = dataBaseHelper.getAllSharedFilesList(); // Implement this method to load shared files data
        adapter = new SharedFilesAdapter(sharedFiles, this);
        recyclerView.setAdapter(adapter);

        if(sharedFiles.isEmpty()){
            empty.setVisibility(View.VISIBLE);
        }
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
            Intent intent = new Intent(MainActivity.this, SendReceiveFileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // Implement a method to load shared files data
    private List<SharedFile> getSharedFilesData() {
        // Implement this method to retrieve and return a list of shared files
        // Replace this with your actual data retrieval logic.

        ArrayList<SharedFile> test = new ArrayList<>();
        test.add(new SharedFile("Test","Test", LocalDate.now().toString(),"Test"));
        return test;
    }
}