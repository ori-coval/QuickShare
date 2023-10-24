package com.example.quickshare;

public class SharedFileHistory {
    private String fileName;
    private String fileType;
    private String recipient;
    private String date;
    private String filePath; // Add the file path property

    public SharedFileHistory(String fileName, String fileType, String recipient, String date, String filePath) {
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

