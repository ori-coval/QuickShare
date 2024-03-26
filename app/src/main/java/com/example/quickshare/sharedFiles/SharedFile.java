package com.example.quickshare.sharedFiles;

import android.net.Uri;

public class SharedFile {
    private final String filePath;
    private final String fileType;
    private final String date;
    private final String fileSize;
    private final Uri fileUri;;
    private final byte[] fileData;

    public SharedFile(String filePath, String fileType, String date, int fileSize, Uri fileUri, byte[] fileData) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.date = date;
        this.fileSize = String.valueOf(fileSize);
        this.fileUri = fileUri;
        this.fileData = fileData;
    }
    public SharedFile(String filePath, String fileType, String date, String fileSize, Uri fileUri, byte[] fileData) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.date = date;
        this.fileSize = fileSize;
        this.fileUri = fileUri;
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

    public Uri getFileUri() {
        return fileUri;
    }
    public byte[] getFileData() {
        return fileData;
    }
}