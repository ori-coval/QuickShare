package com.example.quickshare.sharedFiles;

import androidx.annotation.NonNull;

public class SharedFile {
    private final String filePath;
    private final String fileType;
    private final String date;
    private final String fileSize;
    private final byte[] fileData;


    public SharedFile(@NonNull String filePath, @NonNull String fileType, @NonNull String date,@NonNull int fileSize, @NonNull byte[] fileData) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.date = date;
        this.fileSize = String.valueOf(fileSize);
        this.fileData = fileData;
    }

    public SharedFile(@NonNull String filePath, @NonNull String fileType, @NonNull String date, @NonNull String fileSize, @NonNull byte[] fileData) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.date = date;
        this.fileSize = fileSize;
        this.fileData = fileData;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public String getDate() {
        return date;
    }

    public String getFileSize() {
        return fileSize;
    }

    public byte[] getFileData() {
        return fileData;
    }
}