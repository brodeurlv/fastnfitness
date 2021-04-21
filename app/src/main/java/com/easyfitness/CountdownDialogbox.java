package com.easyfitness;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyfitness.DAO.Machine;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.AlarmReceiver;
import com.easyfitness.utils.UnitConverter;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.text.DecimalFormat;

import gr.antoniom.chronometer.Chronometer;
import gr.antoniom.chronometer.Chronometer.OnChronometerTickListener;

public class CountdownDialogbox extends Dialog implements
        View.OnClickListener {

    private final ExerciseType mExerciseType;
    private final Machine mExercise;
    public Activity activity;
    public Dialog d;
    public Button exit;
    public Chronometer chrono;
    public OnDismissListener onDismissChrono = dialog -> unregisterAlarm(getContext(), 100101);
    //public ProgressBar progressBar;
    private DonutProgress progressCircle;
    private int lNbSerie = 0;
    private float lTotalSession = 0;
    private float lTotalMachine = 0;
    private int iRestTime = 60;
    private final OnChronometerTickListener onChronometerTick = new OnChronometerTickListener() {

        final boolean bFirst = true;

        @Override
        public void onChronometerTick(Chronometer chronometer) {
            int secElapsed = (int) (chrono.getTimeElapsed() / 1000); //secElapsed is a negative value
            progressCircle.setProgress(iRestTime + secElapsed);

            if (secElapsed >= 0) {
                chrono.stop();
                dismiss();
            }
        }
    };

    public CountdownDialogbox(Activity a, int pRestTime, Machine exercise) {
        super(a);
        this.activity = a;
        iRestTime = pRestTime;

        mExercise = exercise;
        if (mExercise != null)
            mExerciseType = exercise.getType();
        else
            mExerciseType = ExerciseType.CARDIO; // The simplest by default
    }

    public static void registerAlarm(Context context, int uniqueId, long triggerAlarmAt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean playSound = prefs.getBoolean("prefPlaySoundAfterRestTimer", true);
        boolean playVibration = prefs.getBoolean("prefPlayVibrationAfterRestTimer", true);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("playSoundAfterRestTimer", playSound);
        intent.putExtra("playVibrationAfterRestTimer", playVibration);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAlarmAt, pendingIntent);
        }
    }

    public static void unregisterAlarm(Context context, int uniqueId) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rest);
        this.setCanceledOnTouchOutside(true); // make it not modal

        exit = findViewById(R.id.btn_exit);
        //progressBar = (ProgressBar) findViewById(R.id.progressBarCountdown);
        chrono = findViewById(R.id.chronoValue);
        TextView nbSeries = findViewById(R.id.idNbSeries);
        TextView totalSession = findViewById(R.id.idTotalSession);
        TextView totalMachine = findViewById(R.id.idTotalWeightMachine);
        TextView totalOnExercise = findViewById(R.id.totalOnExerciseTitle);
        LinearLayout totalExerciseLayout = findViewById(R.id.totalExerciseLayout);
        LinearLayout totalWorkoutLayout = findViewById(R.id.totalWorkoutLayout);

        progressCircle = findViewById(R.id.donut_progress);
        progressCircle.setMax(iRestTime);

        exit.setOnClickListener(this);

        if (mExerciseType != ExerciseType.CARDIO) {
            WeightUnit defaultUnit = SettingsFragment.getDefaultWeightUnit(activity);
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            totalMachine.setText(numberFormat.format(UnitConverter.weightConverter(lTotalMachine, Unit.KG, defaultUnit.toUnit())) + " " + defaultUnit.toString());
            totalSession.setText(numberFormat.format(UnitConverter.weightConverter(lTotalSession, Unit.KG, defaultUnit.toUnit())) + " " + defaultUnit.toString());
            nbSeries.setText(Integer.toString(lNbSerie));
            String totalOnExerciseTitle = getContext().getString(R.string.total_on) + " " + mExercise.getName();
            totalOnExercise.setText(totalOnExerciseTitle);
        } else {
            totalExerciseLayout.setVisibility(View.GONE);
            totalWorkoutLayout.setVisibility(View.GONE);
        }

        chrono.setOnChronometerTickListener(onChronometerTick);
        chrono.setBase(SystemClock.elapsedRealtime() + (iRestTime + 1) * 1000);
        chrono.setPrecision(false);
        chrono.start(); // Start automatically

        setOnDismissListener(onDismissChrono);

        registerAlarm(getContext(), 100101, SystemClock.elapsedRealtime() + (iRestTime - 3) * 1000);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_exit) {
            chrono.stop();
            chrono.setText("00:00");
            dismiss();
        }
    }

    public void setTotalWeightSession(float pTotalWeight) {
        lTotalSession = pTotalWeight;
    }

    public void setTotalWeightMachine(float pTotalWeight) {
        lTotalMachine = pTotalWeight;
    }

    public void setNbSeries(int pNbSeries) {
        lNbSerie = pNbSeries;
    }

}
