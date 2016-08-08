package com.easyfitness;

import java.util.Calendar;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;

public class TimePickerDialogFragment extends DialogFragment {
    private Fragment mFragment;

    public TimePickerDialogFragment(Fragment callback) {
        mFragment = callback;
    }

     public Dialog onCreateDialog(Bundle savedInstanceState) {
    	// Use the current time as the default values for the picker
         final Calendar c = Calendar.getInstance();
         int hour = c.get(Calendar.HOUR_OF_DAY);
         int minute = c.get(Calendar.MINUTE);

         // Create a new instance of TimePickerDialog and return it
         return new TimePickerDialog(getActivity(), (OnTimeSetListener) mFragment, hour, minute,
                 DateFormat.is24HourFormat(getActivity()));
     }
}