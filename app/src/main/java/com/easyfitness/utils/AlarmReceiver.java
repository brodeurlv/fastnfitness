package com.easyfitness.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent arg1) {
        long[] pattern = {
                0,  // Start immediately
                500, 500, 500, 500, 500};
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneGen.startTone(ToneGenerator.TONE_SUP_BUSY, 3000);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, -1), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
        } else {
            //deprecated in API 26
            if (v != null) {
                v.vibrate(pattern, -1);
            }
        }
    }
}
