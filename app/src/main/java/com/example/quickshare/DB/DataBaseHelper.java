package com.example.quickshare.DB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.quickshare.sharedFiles.SharedFile;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "shared_files.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "shared_files_table";

    // Remove COL_ID constant

    public static final String COL_FILE_PATH = "FILE_PATH";
    public static final String COL_FILE_TYPE = "FILE_TYPE";
    public static final String COL_DATE = "DATE";
    public static final String COL_FILE_SIZE = "FILE_SIZE";

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_FILE_PATH + " TEXT PRIMARY KEY, " + COL_FILE_TYPE + " TEXT, " + COL_DATE + " TEXT, " + COL_FILE_SIZE + " TEXT);");
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
        long result = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public Cursor getAllSharedFiles() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
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
                SharedFile sharedFile = new SharedFile(filePath, fileType, date, fileSize);
                sharedFilesList.add(sharedFile);
            }
        } finally {
            cursor.close();
        }
        return sharedFilesList;
    }

    @SuppressLint("Range")
    public SharedFile getSharedFileByPath(String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_FILE_PATH + " = ?", new String[]{filePath});
        SharedFile sharedFile = null;
        if (cursor.moveToFirst()) {
            String fileType = cursor.getString(cursor.getColumnIndex(COL_FILE_TYPE));
            String date = cursor.getString(cursor.getColumnIndex(COL_DATE));
            String fileSize = cursor.getString(cursor.getColumnIndex(COL_FILE_SIZE));
            sharedFile = new SharedFile(filePath, fileType, date, fileSize);
        }
        cursor.close();
        return sharedFile;
    }

    public boolean deleteSharedFile(String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_FILE_PATH + " = ?", new String[]{filePath}) > 0;
    }
}
