package com.easyfitness.utils;

import android.util.Log;

public class Debug {
    public static final String TAG = "com.easyfitness.liveDebug";
    public static void i(String message) {
        Log.i(TAG, message);
    }
}
