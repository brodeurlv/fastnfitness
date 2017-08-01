package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import gr.antoniom.chronometer.Chronometer;
import gr.antoniom.chronometer.Chronometer.OnChronometerTickListener;

public class CountdownDialogbox extends Dialog implements
        View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button exit;
    public Chronometer chrono;
    public ProgressBar progressBar;

    int iRestTime = 60;
    private OnChronometerTickListener onChronometerTick = new OnChronometerTickListener() {

        @Override
        public void onChronometerTick(Chronometer chronometer) {
            // Update progressbar
            //progressBar = (ProgressBar) findViewById(R.id.progressBarCountdown);
            int secElapsed = (int) (chrono.getTimeElapsed() / 1000);
            progressBar.setProgress(iRestTime + secElapsed);
            if (iRestTime + secElapsed >= iRestTime) {
                chrono.stop();
                dismiss();
            }
        }
    };

    public CountdownDialogbox(Activity a, int pRestTime) {
        super(a);
        this.c = a;
        iRestTime = pRestTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(c.getResources().getString(R.string.ChronometerLabel)); //ChronometerLabel
        setContentView(R.layout.dialog_rest);
        this.setCanceledOnTouchOutside(true); // make it not modal

        exit = (Button) findViewById(R.id.btn_exit);
        progressBar = (ProgressBar) findViewById(R.id.progressBarCountdown);
        chrono = (Chronometer) findViewById(R.id.chronoValue);

        exit.setOnClickListener(this);

        // If setting prefShowRestTime is ON
        /*SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(c.getBaseContext());
        iRestTime = Integer.valueOf(SP.getString("prefRestTimeValue", "60"));*/
        progressBar.setMax(iRestTime);

        chrono.setOnChronometerTickListener(onChronometerTick);
        chrono.setBase(SystemClock.elapsedRealtime() + iRestTime * 1000);
        chrono.setPrecision(false);
        chrono.start(); // Start automatically
    }
  
  /*
  public OnDismissListener onDismissChrono = new OnDismissListener()
  {
	  @Override
	  public void onDismiss(DialogInterface dialog) {
		  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	  }
  };*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit:
                chrono.stop();
                chrono.setText("00:00:0");
                dismiss();
                break;
            default:
                break;
        }
    }


}