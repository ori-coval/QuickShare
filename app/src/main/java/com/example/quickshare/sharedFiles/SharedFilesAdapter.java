package com.example.quickshare.sharedFiles;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickshare.CONSTANTS;
import com.example.quickshare.DB.DataBaseHelper;
import com.example.quickshare.MainActivity;
import com.example.quickshare.R;
import com.example.quickshare.shareReceiveFile.SendReceiveFileActivity;

import java.util.List;

public class SharedFilesAdapter extends RecyclerView.Adapter<SharedFilesAdapter.SharedFileViewHolder> {

    private final List<SharedFile> sharedFiles;
    private DataBaseHelper dataBaseHelper;
    Context context;

    public SharedFilesAdapter(List<SharedFile> sharedFiles, Context context) {
        this.context = context;
        this.sharedFiles = sharedFiles;
        dataBaseHelper = new DataBaseHelper(context);
    }

    @NonNull
    @Override
    public SharedFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shared_file, parent, false);
        return new SharedFileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedFileViewHolder holder, int position) {
        SharedFile sharedFile = sharedFiles.get(position);
        holder.textFileName.setText(sharedFile.getFilePath());
        holder.textFileType.setText(sharedFile.getFileType());
        holder.textDate.setText(sharedFile.getDate());

        // Implement click listeners for buttons
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    dataBaseHelper.deleteSharedFile(sharedFiles.get(adapterPosition).getFilePath());
                    sharedFiles.remove(adapterPosition);
                    notifyDataSetChanged(); // Refresh the RecyclerView
                }
            }
        });

        holder.shareAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the File Sharing Activity
                Intent sendFileIntent = new Intent(context, SendReceiveFileActivity.class);
                sendFileIntent.putExtra("default_fragment", CONSTANTS.SEND_FILE_POSE);
                sendFileIntent.putExtra("file_path", sharedFile.getFilePath());
                sendFileIntent.putExtra("file_type", sharedFile.getFileType());
                sendFileIntent.putExtra("file_size", sharedFile.getFileSize());
                startActivity(context, sendFileIntent, null); // Pass the context and sendFileIntent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return sharedFiles.size();
    }

    public class SharedFileViewHolder extends RecyclerView.ViewHolder {
        public TextView textFileName, textFileType, textRecipient, textDate;
        public Button deleteButton, shareAgainButton;

        public SharedFileViewHolder(View itemView) {
            super(itemView);
            textFileName = itemView.findViewById(R.id.textFileName);
            textFileType = itemView.findViewById(R.id.textFileType);
            textRecipient = itemView.findViewById(R.id.textRecipient);
            textDate = itemView.findViewById(R.id.textDate);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            shareAgainButton = itemView.findViewById(R.id.shareAgainButton);
        }
    }
}

