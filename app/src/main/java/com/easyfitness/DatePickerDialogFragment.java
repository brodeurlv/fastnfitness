package com.easyfitness;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class DatePickerDialogFragment extends DialogFragment implements OnDateSetListener {

    /*public DatePickerDialogFragment(Fragment callback) {
        mFragment = callback;
    }*/

     public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
         return new DatePickerDialog(getActivity(), this, year, month, day);
     }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
    }

}