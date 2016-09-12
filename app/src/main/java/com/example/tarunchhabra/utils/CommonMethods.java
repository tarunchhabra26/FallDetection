package com.example.tarunchhabra.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.example.tarunchhabra.exampleapplication.MainActivity;

/**
 * Created by tarunchhabra on 9/11/16.
 */
public class CommonMethods {
    /**
     * Public method to show a toast notification
     */
    public void createToast(Context context, CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
