package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.easyfitness.utils.UnitConverter;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.text.DecimalFormat;

import gr.antoniom.chronometer.Chronometer;
import gr.antoniom.chronometer.Chronometer.OnChronometerTickListener;

public class CountdownDialogbox extends Dialog implements
        View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button exit;
    public Chronometer chrono;
    //public ProgressBar progressBar;
    public DonutProgress progressCircle;
    public TextView nbSeries;
    public TextView totalSession;
    public TextView totalMachine;

    int lNbSerie=0;
    float lTotalSession=0;
    float lTotalMachine=0;


    int iRestTime = 60;
    private OnChronometerTickListener onChronometerTick = new OnChronometerTickListener() {

        boolean bFirst=true;

        @Override
        public void onChronometerTick(Chronometer chronometer) {
            // Update progressbar
            //progressBar = (ProgressBar) findViewById(R.id.progressBarCountdown);

            int secElapsed = (int) (chrono.getTimeElapsed() / 1000); //secElapsed is a negative value
            //progressBar.setProgress(iRestTime + secElapsed);
            progressCircle.setProgress(iRestTime + secElapsed);
            if (secElapsed >= -2) { // Vibrate
                if (bFirst==false) {
                    Vibrator v = (Vibrator) c.getApplicationContext().getSystemService(c.getApplicationContext().VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(500);
                    }
                } else {
                    bFirst=false;
                }
            }
            if (secElapsed >= 0) {
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
        //progressBar = (ProgressBar) findViewById(R.id.progressBarCountdown);
        chrono = (Chronometer) findViewById(R.id.chronoValue);
        nbSeries = (TextView) findViewById(R.id.idNbSeries);
        totalSession = (TextView) findViewById(R.id.idTotalSession);
        totalMachine = (TextView) findViewById(R.id.idTotalWeightMachine);

        progressCircle = (DonutProgress) findViewById(R.id.donut_progress);

        progressCircle.setMax(iRestTime);

        exit.setOnClickListener(this);

        // If setting prefShowRestTime is ON
        /*SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(c.getBaseContext());
        iRestTime = Integer.valueOf(SP.getString("prefRestTimeValue", "60"));*/
        //progressBar.setMax(iRestTime);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
        int defaultUnit= Integer.valueOf(SP.getString("defaultUnit", "0"));

        DecimalFormat numberFormat = new DecimalFormat("#.##");

        if (defaultUnit == UnitConverter.UNIT_KG) {
            totalMachine.setText(numberFormat.format(lTotalMachine) + " " + this.getContext().getResources().getText(R.string.KgUnitLabel));
            totalSession.setText(numberFormat.format(lTotalSession) + " " + this.getContext().getResources().getText(R.string.KgUnitLabel));
        }
        else if (defaultUnit == UnitConverter.UNIT_LBS)
        {
            totalMachine.setText(numberFormat.format(UnitConverter.KgtoLbs(lTotalMachine)) + " " + this.getContext().getResources().getText(R.string.LbsUnitLabel));
            totalSession.setText(numberFormat.format(UnitConverter.KgtoLbs(lTotalSession)) + " " + this.getContext().getResources().getText(R.string.LbsUnitLabel));
        }

        nbSeries.setText(Integer.toString(lNbSerie));

        chrono.setOnChronometerTickListener(onChronometerTick);
        chrono.setBase(SystemClock.elapsedRealtime() + (iRestTime+1) * 1000);
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
                chrono.setText("00:00");
                dismiss();
                break;
            default:
                break;
        }
    }

    public void setTotalWeightSession(float pTotalWeight) {
        lTotalSession=pTotalWeight;
    }

    public void setTotalWeightMachine(float pTotalWeight) {
        lTotalMachine=pTotalWeight;
    }

    public void setNbSeries(int pNbSeries) {
        lNbSerie=pNbSeries;
    }

}