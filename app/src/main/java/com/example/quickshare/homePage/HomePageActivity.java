package com.example.quickshare.homePage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickshare.DB.DataBaseHelper;
import com.example.quickshare.R;
import com.example.quickshare.Utils.CONSTANTS;
import com.example.quickshare.shareReceiveFile.SendReceiveFileActivity;
import com.example.quickshare.sharedFiles.SharedFile;
import com.example.quickshare.sharedFiles.SharedFilesAdapter;

import java.util.List;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Set click listeners for the buttons
        findViewById(R.id.send_file_button).setOnClickListener(this);
        findViewById(R.id.receive_file_button).setOnClickListener(this);

        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.shared_files_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<SharedFile> sharedFiles = null;


        if (!dataBaseHelper.checkIfDataBaseEmpty()) {
            sharedFiles = dataBaseHelper.getAllSharedFilesList();
            SharedFilesAdapter adapter = new SharedFilesAdapter(sharedFiles, this);
            recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_file_button) {// Start the File Sharing Activity for sending files
            Intent sendFileIntent = new Intent(HomePageActivity.this, SendReceiveFileActivity.class);
            sendFileIntent.putExtra("default_fragment", CONSTANTS.misc.SEND_FILE_POSE);
            startActivity(sendFileIntent);
        } else if (v.getId() == R.id.receive_file_button) {// Start the File Sharing Activity for receiving files
            Intent receiveFileIntent = new Intent(HomePageActivity.this, SendReceiveFileActivity.class);
            receiveFileIntent.putExtra("default_fragment", CONSTANTS.misc.RECEIVE_FILE_POSE);
            startActivity(receiveFileIntent);
        }
    }
}
