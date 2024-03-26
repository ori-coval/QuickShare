package com.example.quickshare;

import java.util.UUID;

public class CONSTANTS {

    public  static final int SEND_FILE_POSE = 0;
    public  static final int RECEIVE_FILE_POSE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int REQUEST_CONNECT_DEVICE = 3;
    public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String NAME = "Quick share";
    public static final int BUFFER_SIZE = 100000000;

    public interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
        int MESSAGE_PROGRESS = 564564;
        int MESSAGE_READ_FILE = 4589;
        int MESSAGE_FILE_RECEIVED = 897456;
        int MESSAGE_READ_FILE_COMPLETE = 39999;
        int MESSAGE_READ_FILE_TYPE = 8888;
        int MESSAGE_READ_FILE_SIZE = 89315;
        int FINISHED = 4891854;
        int STARTED = 88824564;
    }
}
