package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.easyfitness.enums.Unit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.views.SingleValueInputView;

import java.util.Date;

public class ValueEditorDialogbox extends Dialog implements View.OnClickListener {

    private final Date mDate;
    private final String mTime;
    private final double mValue;
    private final Unit mUnit;
    public Dialog d;
    private SingleValueInputView dateEdit;
    private SingleValueInputView timeEdit;
    private SingleValueInputView valueEdit;
    private Button updateButton;
    private Button cancelButton;
    private boolean mCancelled = false;
    private TextView titleTextView;
    private String mTitle = "";
    private String mUpdateText = "";

    public ValueEditorDialogbox(Activity a, Date date, String time, double value) {
        super(a);
        mDate = date;
        mTime = time;
        mValue = value;

        mUnit = Unit.UNITLESS;
    }

    public ValueEditorDialogbox(Activity a, Date date, String time, double value, Unit units) {
        super(a);
        mDate = date;
        mTime = time;
        mValue = value;

        mUnit = units;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_value_editor);
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(true);

        dateEdit = findViewById(R.id.EditorValueDateInput);
        timeEdit = findViewById(R.id.EditorValueTimeInput);
        valueEdit = findViewById(R.id.EditorValueInput);
        titleTextView = findViewById(R.id.EditorValueTitle);
        if (!mTitle.isEmpty()) titleTextView.setText(mTitle);

        switch (mUnit.getUnitType()) {
            case SIZE:
                valueEdit.setUnits(new CharSequence[]{Unit.CM.toString(), Unit.INCH.toString()});
                break;
            case WEIGHT:
                valueEdit.setUnits(new CharSequence[]{Unit.KG.toString(), Unit.LBS.toString(), Unit.STONES.toString()});
                break;
            case DISTANCE:
                valueEdit.setUnits(new CharSequence[]{Unit.KM.toString(), Unit.MILES.toString()});
                break;
            case PERCENTAGE:
                valueEdit.setUnits(new CharSequence[]{Unit.PERCENTAGE.toString()});
                valueEdit.showUnit(true);
                break;
            case NONE:
            default:
                valueEdit.setUnits(new CharSequence[]{Unit.UNITLESS.toString()});
                valueEdit.showUnit(false);
        }

        dateEdit.setValue(DateConverter.dateToLocalDateStr(mDate, getContext()));
        timeEdit.setValue(mTime);
        valueEdit.setValue(String.format("%.1f", mValue));
        valueEdit.setSelectedUnit(mUnit.toString());

        updateButton = findViewById(R.id.btn_update);
        if (!mUpdateText.isEmpty()) updateButton.setText(mUpdateText);
        cancelButton = findViewById(R.id.btn_cancel);

        updateButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Keyboard.hide(getContext(), valueEdit);
        Keyboard.hide(getContext(), timeEdit);

        if (v.getId() == R.id.btn_cancel) {
            mCancelled = true;
            cancel();
        } else if (v.getId() == R.id.btn_update) {
            mCancelled = false;
            dismiss();
        }
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public String getDate() {
        return dateEdit.getValue();
    }

    public String getTime() {
        return timeEdit.getValue();
    }

    public String getValue() {
        return valueEdit.getValue();
    }

    public String getUnit() {
        return valueEdit.getSelectedUnit();
    }

    @Override
    public void setTitle(int title) {
        mTitle = getContext().getString(title);
    }

    public void setPositiveButton(int text) {
        mUpdateText = getContext().getString(text);
    }
}
