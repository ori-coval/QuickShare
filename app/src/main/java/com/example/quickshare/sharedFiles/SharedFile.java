package com.example.quickshare.sharedFiles;

public class SharedFile {
    private final String filePath;
    private final String fileType;
    private final String date;
    private final String fileSize; // Add the file path property

    public SharedFile(String filePath, String fileType, String date, String fileSize) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.date = date;
        this.fileSize = fileSize;
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
}

