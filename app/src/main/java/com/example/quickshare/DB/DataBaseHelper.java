package com.example.quickshare.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.quickshare.sharedFiles.SharedFile;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "shared_files.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "shared_files_table";

    public static final String COL_FILE_PATH = "FILE_PATH";
    public static final String COL_FILE_TYPE = "FILE_TYPE";
    public static final String COL_DATE = "DATE";
    public static final String COL_FILE_SIZE = "FILE_SIZE";
    public static final String COL_FILE_URI = "FILE_URI";
    public static final String COL_FILE_DATA = "FILE_DATA";

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_FILE_PATH + " TEXT PRIMARY KEY, " +
                COL_FILE_TYPE + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_FILE_SIZE + " TEXT, " +
                COL_FILE_URI + " TEXT, " +
                COL_FILE_DATA + " BLOB);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public boolean insertSharedFile(SharedFile sharedFile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_FILE_PATH, sharedFile.getFilePath());
        contentValues.put(COL_FILE_TYPE, sharedFile.getFileType());
        contentValues.put(COL_DATE, sharedFile.getDate());
        contentValues.put(COL_FILE_SIZE, sharedFile.getFileSize());
        contentValues.put(COL_FILE_URI, sharedFile.getFileUri().toString());
        contentValues.put(COL_FILE_DATA, sharedFile.getFileData());
        long result = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public ArrayList<SharedFile> getAllSharedFilesList() {
        ArrayList<SharedFile> sharedFilesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        try {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_PATH));
                String fileType = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String fileSize = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_SIZE));
                Uri fileUri = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_URI)));
                byte[] fileData = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_FILE_DATA));
                SharedFile sharedFile = new SharedFile(filePath, fileType, date, fileSize, fileUri, fileData);
                sharedFilesList.add(sharedFile);
            }
        } finally {
            cursor.close();
        }
        return sharedFilesList;
    }

    public boolean deleteSharedFile(String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_FILE_PATH + " = ?", new String[]{filePath}) > 0;
    }

    public boolean checkIfDataBaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        try {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            return count == 0;
        } finally {
            cursor.close();
        }
    }

}
