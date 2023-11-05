package com.example.quickshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SharedFilesHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView empty;
    private SharedFilesAdapter adapter;
    private List<SharedFileHistory> sharedFiles;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_files_history);

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.shared_files_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        empty = findViewById(R.id.empty_state_message);
        cancelButton = findViewById(R.id.cancel_button);

        // Initialize the adapter and set it to the RecyclerView
        sharedFiles = getSharedFilesData(); // Implement this method to load shared files data
        adapter = new SharedFilesAdapter(sharedFiles);
        recyclerView.setAdapter(adapter);

        if(sharedFiles.isEmpty()){
            empty.setVisibility(View.VISIBLE);
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Close the File Sharing Activity
            }
        });
    }

    // Implement a method to load shared files data
    private List<SharedFileHistory> getSharedFilesData() {
        // Implement this method to retrieve and return a list of shared files
        // Replace this with your actual data retrieval logic.

        ArrayList<SharedFileHistory> test = new ArrayList<>();
        test.add(new SharedFileHistory("Test","Test","Test","Test","Test"));
        test.add(new SharedFileHistory("Test","Test","Test","Test","Test"));
        return test;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem shareFileAction = menu.findItem(R.id.action_history);
        shareFileAction.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_home){
            Intent intent = new Intent(SharedFilesHistoryActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.action_share_screen){
            Intent intent = new Intent(SharedFilesHistoryActivity.this, FileSharingActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.action_recipient){
            Intent intent = new Intent(SharedFilesHistoryActivity.this, RecipientActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
