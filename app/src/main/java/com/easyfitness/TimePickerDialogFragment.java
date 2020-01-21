package com.easyfitness;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

//@SuppressLint("ValidFragment")
public class TimePickerDialogFragment extends DialogFragment {

    private MyTimePickerDialog.OnTimeSetListener onTimeSetListener;
    private int Hours=0;
    private int Minutes=0;
    private int Seconds=0;

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
