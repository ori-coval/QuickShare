package com.example.quickshare;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class CustomToast {

    public static void showWithDuration(Context context, String message, double durationInSeconds) {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);

        // Show the toast
        toast.show();

        // Schedule a handler to dismiss the toast after the specified duration
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, (long) durationInSeconds * 1000); // Convert seconds to milliseconds
    }
}
