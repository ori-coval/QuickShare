package com.example.quickshare.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quickshare.Utils.CONSTANTS;
import com.example.quickshare.sharedFiles.SharedFile;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {



    public DataBaseHelper(Context context) {
        super(context, CONSTANTS.DBConstants.DB_NAME, null, CONSTANTS.DBConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CONSTANTS.DBConstants.TABLE_NAME + " (" +
                CONSTANTS.DBConstants.COL_FILE_PATH + " TEXT PRIMARY KEY, " +
                CONSTANTS.DBConstants.COL_FILE_TYPE + " TEXT, " +
                CONSTANTS.DBConstants.COL_DATE + " TEXT, " +
                CONSTANTS.DBConstants.COL_FILE_SIZE + " TEXT, " +
                CONSTANTS.DBConstants.COL_FILE_DATA + " BLOB);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONSTANTS.DBConstants.TABLE_NAME);
        onCreate(db);
    }


    public void insertSharedFile(SharedFile sharedFile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONSTANTS.DBConstants.COL_FILE_PATH, sharedFile.getFilePath());
        contentValues.put(CONSTANTS.DBConstants.COL_FILE_TYPE, sharedFile.getFileType());
        contentValues.put(CONSTANTS.DBConstants.COL_DATE, sharedFile.getDate());
        contentValues.put(CONSTANTS.DBConstants.COL_FILE_SIZE, sharedFile.getFileSize());
        contentValues.put(CONSTANTS.DBConstants.COL_FILE_DATA, sharedFile.getFileData());
        db.insertWithOnConflict(CONSTANTS.DBConstants.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ArrayList<SharedFile> getAllSharedFilesList() {
        ArrayList<SharedFile> sharedFilesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + CONSTANTS.DBConstants.TABLE_NAME, null)) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(CONSTANTS.DBConstants.COL_FILE_PATH));
                String fileType = cursor.getString(cursor.getColumnIndexOrThrow(CONSTANTS.DBConstants.COL_FILE_TYPE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(CONSTANTS.DBConstants.COL_DATE));
                String fileSize = cursor.getString(cursor.getColumnIndexOrThrow(CONSTANTS.DBConstants.COL_FILE_SIZE));
                byte[] fileData = cursor.getBlob(cursor.getColumnIndexOrThrow(CONSTANTS.DBConstants.COL_FILE_DATA));
                SharedFile sharedFile = new SharedFile(filePath, fileType, date, fileSize, fileData);
                sharedFilesList.add(sharedFile);
            }
        }
        return sharedFilesList;
    }

    public void deleteSharedFile(String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CONSTANTS.DBConstants.TABLE_NAME, CONSTANTS.DBConstants.COL_FILE_PATH + " = ?", new String[]{filePath});
    }

    public boolean checkIfDataBaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + CONSTANTS.DBConstants.TABLE_NAME, null)) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            return count == 0;
        }
    }

}
