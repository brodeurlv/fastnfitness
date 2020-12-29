package com.easyfitness;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

//@SuppressLint("ValidFragment")
public class TimePickerDialogFragment extends DialogFragment {

    private final int Hours = 0;
    private final int Minutes = 0;
    private final int Seconds = 0;
    private MyTimePickerDialog.OnTimeSetListener onTimeSetListener;

    static public TimePickerDialogFragment newInstance(MyTimePickerDialog.OnTimeSetListener onTimeSetListener, int hour, int min, int sec) {
        TimePickerDialogFragment pickerFragment = new TimePickerDialogFragment();
        pickerFragment.setOnTimeSetListener(onTimeSetListener);

        //Pass the date in a bundle.
        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", hour);
        bundle.putInt("MINUTE", min);
        bundle.putInt("SECOND", sec);
        pickerFragment.setArguments(bundle);
        return pickerFragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int hour = bundle.getInt("HOUR");
        int min = bundle.getInt("MINUTE");
        int sec = bundle.getInt("SECOND");

        // Create a new instance of TimePickerDialog and return it
        return new MyTimePickerDialog(getActivity(), onTimeSetListener, hour, min, sec, true);
    }

    private void setOnTimeSetListener(MyTimePickerDialog.OnTimeSetListener listener) {
        this.onTimeSetListener = listener;
    }
}
