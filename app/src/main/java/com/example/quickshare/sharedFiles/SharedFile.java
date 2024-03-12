package com.example.quickshare.sharedFiles;

public class SharedFile {
    private final String fileName;
    private final String fileType;
    private final String recipient;
    private final String date;
    private final String filePath; // Add the file path property

    public SharedFile(String fileName, String fileType, String recipient, String date, String filePath) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.recipient = recipient;
        this.date = date;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getDate() {
        return date;
    }

    public String getFilePath() {
        return filePath;
    }
}

