package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import gr.antoniom.chronometer.Chronometer;

public class ChronoDialogbox extends Dialog implements
    android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button startstop, exit, reset;
    public Chronometer chrono;
    String strCurrentTime = "";
    long startTime = 0;
    long stopTime = 0;
    private boolean chronoStarted = false;
    private boolean chronoResetted = false;

    public ChronoDialogbox(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(c.getResources().getString(R.string.ChronometerLabel)); //ChronometerLabel
        setContentView(R.layout.dialog_chrono);
        this.setCanceledOnTouchOutside(false); // make it modal

        startstop = findViewById(R.id.btn_startstop);
        exit = findViewById(R.id.btn_exit);
        reset = findViewById(R.id.btn_reset);
        chrono = findViewById(R.id.chronoValue);

        startstop.setOnClickListener(this);
        exit.setOnClickListener(this);
        reset.setOnClickListener(this);
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
        startTime = SystemClock.elapsedRealtime();
        chronoStarted = true;

        startstop.setText("Stop");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_startstop:
                if (chronoStarted) {
                    chrono.stop();
                    stopTime = SystemClock.elapsedRealtime();
                    chronoStarted = false;
                    startstop.setText("Start");
                } else {
                    if (chronoResetted) {
                        startTime = SystemClock.elapsedRealtime();
                        chrono.setBase(startTime);
                    } else {
                        startTime = SystemClock.elapsedRealtime() - (stopTime - startTime);
                        chrono.setBase(startTime);
                    }
                    chrono.start();
                    chronoStarted = true;
                    startstop.setText("Stop");
                }
                chronoResetted = false;
                break;
            case R.id.btn_reset:
                startTime = SystemClock.elapsedRealtime();
                chrono.setBase(startTime);
                chrono.setText("00:00:0");
                chronoResetted = true;
                break;
            case R.id.btn_exit:
                chrono.stop();
                chronoStarted = false;
                chrono.setText("00:00:0");
                startstop.setText("Start");
                dismiss();
                break;
            default:
                break;
        }
    }
}
