package com.example.quickshare.Utils;

import java.util.UUID;

public class CONSTANTS {

    public interface DBConstants {
        String DB_NAME = "shared_files.db";
        int DB_VERSION = 3;
        String TABLE_NAME = "shared_files_table";

        String COL_FILE_PATH = "FILE_PATH";
        String COL_FILE_TYPE = "FILE_TYPE";
        String COL_DATE = "DATE";
        String COL_FILE_SIZE = "FILE_SIZE";
        String COL_FILE_DATA = "FILE_DATA";
    }


    public interface misc {
        int SEND_FILE_POSE = 0;
        int RECEIVE_FILE_POSE = 1;

        //a UUID that was hashed from "totally and absolutely random UUID for my bluetooth file sharing app"
        UUID MY_UUID = UUID.fromString("4120b109-97c9-13c7-8d36-27d1c3a7a732");
        String NAME = "Quick share";
    }

    public interface MessageConstants {
        String MESSAGE_TOAST = "TOAST";
        int MESSAGE_PROGRESS = 564564;
        int MESSAGE_READ_FILE_SIZE = 89315;
        int FINISHED = 4891854;
        int STARTED = 88824564;
    }
}
