package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import gr.antoniom.chronometer.Chronometer;
import gr.antoniom.chronometer.Chronometer.OnChronometerTickListener;
//import android.widget.Chronometer;
//import android.widget.Chronometer.OnChronometerTickListener;

public class ChronoDialogbox extends Dialog implements
    android.view.View.OnClickListener {

  public Activity c;
  public Dialog d;
  public Button startstop, exit, reset;
  public Chronometer chrono;
  private boolean chronoStarted=false;
  private boolean chronoResetted=false;
  String strCurrentTime="";
  long startTime=0;
  long stopTime=0;

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
    
    startstop = (Button) findViewById(R.id.btn_startstop);
    exit = (Button) findViewById(R.id.btn_exit);
    reset = (Button) findViewById(R.id.btn_reset);
    chrono= (Chronometer) findViewById(R.id.chronoValue);
    
    startstop.setOnClickListener(this);
    exit.setOnClickListener(this);
    reset.setOnClickListener(this);
    chrono.setOnChronometerTickListener(onChronometerTick);
    chrono.setBase(SystemClock.elapsedRealtime());
    chrono.start();
    startTime=SystemClock.elapsedRealtime();
    chronoStarted=true;
    
    startstop.setText("Stop");
    
    //setOnDismissListener(onDismissChrono) ;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.btn_startstop:
    	if (chronoStarted) {
    		chrono.stop();    		
    		stopTime = SystemClock.elapsedRealtime();
    		chronoStarted=false;
    		startstop.setText("Start");
    	} else {
    		if (chronoResetted) {
    			startTime=SystemClock.elapsedRealtime();
    			chrono.setBase(startTime);    			
    		} else {
    			startTime=SystemClock.elapsedRealtime()-(stopTime-startTime);
    			chrono.setBase(startTime);
    		}
    		chrono.start();    		
    	    chronoStarted=true;
    	    startstop.setText("Stop");
    	}
    	chronoResetted=false;
      break;
    case R.id.btn_reset:
    	startTime=SystemClock.elapsedRealtime();
    	chrono.setBase(startTime);		
		chrono.setText("00:00:0");
    	chronoResetted = true;
    	break;
    case R.id.btn_exit:
		chrono.stop();
		chronoStarted=false;
		chrono.setText("00:00:0");
		startstop.setText("Start");
      dismiss();
      break;
    default:
      break;
    }
  }
  
  /*
  public OnDismissListener onDismissChrono = new OnDismissListener()
  {
	  @Override
	  public void onDismiss(DialogInterface dialog) {
		  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	  }
  };*/
  
  private OnChronometerTickListener onChronometerTick = new OnChronometerTickListener(){

		@Override
		public void onChronometerTick(Chronometer chronometer) {
			/*long minutes=((SystemClock.elapsedRealtime()-chrono.getBase())/1000)/60;
			long seconds=((SystemClock.elapsedRealtime()-chrono.getBase())/1000)%60;
			long milliseconds=((SystemClock.elapsedRealtime()-chrono.getBase()))%100;
			if (minutes<10) {
				strCurrentTime="0"+String.valueOf(minutes)+":";
			} else {
				strCurrentTime=String.valueOf(minutes)+":";
			}
			if (seconds<10) {
				strCurrentTime=strCurrentTime+"0"+String.valueOf(seconds)+":";
			} else {
				strCurrentTime=strCurrentTime+String.valueOf(seconds)+":";
			}
			if (milliseconds<10) {
				strCurrentTime=strCurrentTime+"0"+String.valueOf(milliseconds);
			} else {
				strCurrentTime=strCurrentTime+String.valueOf(milliseconds);
			}
			chrono.setText(strCurrentTime);*/
		}	
	};
	
	
	
}