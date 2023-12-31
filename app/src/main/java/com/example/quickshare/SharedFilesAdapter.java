package com.example.quickshare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SharedFilesAdapter extends RecyclerView.Adapter<SharedFilesAdapter.SharedFileViewHolder> {

    private List<SharedFileHistory> sharedFiles;

    public SharedFilesAdapter(List<SharedFileHistory> sharedFiles) {
        this.sharedFiles = sharedFiles;
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
        SharedFileHistory sharedFile = sharedFiles.get(position);
        holder.textFileName.setText(sharedFile.getFileName());
        holder.textFileType.setText(sharedFile.getFileType());
        holder.textRecipient.setText(sharedFile.getRecipient());
        holder.textDate.setText(sharedFile.getDate());

        // Implement click listeners for buttons
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement the logic for deleting the shared file
            }
        });

        holder.shareAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement the logic for sharing the file again
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

