package com.easyfitness;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class TimePickerDialogFragment extends DialogFragment {

    private TimePickerDialog.OnTimeSetListener onTimeSetListener;

    static public TimePickerDialogFragment newInstance(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        TimePickerDialogFragment pickerFragment = new TimePickerDialogFragment();
        pickerFragment.setOnTimeSetListener(onTimeSetListener);

        //Pass the date in a bundle.
        Bundle bundle = new Bundle();
        pickerFragment.setArguments(bundle);
        return pickerFragment;
    }

     public Dialog onCreateDialog(Bundle savedInstanceState) {
    	// Use the current time as the default values for the picker
         final Calendar c = Calendar.getInstance();
         int hour = c.get(Calendar.HOUR_OF_DAY);
         int minute = c.get(Calendar.MINUTE);

         // Create a new instance of TimePickerDialog and return it
         return new TimePickerDialog(getActivity(), onTimeSetListener, hour, minute,
                 DateFormat.is24HourFormat(getActivity()));
     }

    private void setOnTimeSetListener(TimePickerDialog.OnTimeSetListener listener) {
        this.onTimeSetListener = listener;
    }
}