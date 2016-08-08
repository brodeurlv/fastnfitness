package com.easyfitness;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;

public class DatePickerDialogFragment extends DialogFragment {
    private Fragment mFragment;

    public DatePickerDialogFragment(Fragment callback) {
        mFragment = callback;
    }

     public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
         return new DatePickerDialog(getActivity(), (OnDateSetListener) mFragment, year, month, day);
     }
}