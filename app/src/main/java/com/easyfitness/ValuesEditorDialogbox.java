package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.easyfitness.enums.Unit;
import com.easyfitness.utils.Value;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.views.SingleValueInputView;

import java.util.Date;

public class ValuesEditorDialogbox extends Dialog implements View.OnClickListener {

    private final Date mDate;
    private final String mTime;
    private final Value[] mValues;
    public Dialog d;
    private SingleValueInputView dateEdit;
    private SingleValueInputView timeEdit;
    private SingleValueInputView[] valueEdits;
    private Button updateButton;
    private Button cancelButton;
    private boolean mCancelled = false;
    private TextView titleTextView;
    private String mTitle = "";
    private String mUpdateText = "";

    public ValuesEditorDialogbox(Activity a, Date date, String time, Value[] values) {
        super(a);
        mDate = date;
        mTime = time;
        mValues = values;
        valueEdits = new SingleValueInputView[values.length];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_value_editor);
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(true);

        dateEdit = findViewById(R.id.EditorValueDateInput);
        timeEdit = findViewById(R.id.EditorValueTimeInput);
        final ViewGroup valuesContainer = findViewById(R.id.EditorValuesContainer);
        titleTextView = findViewById(R.id.EditorValueTitle);
        if (!mTitle.isEmpty()) titleTextView.setText(mTitle);

        for (int i = 0; i < this.mValues.length; i++){
            final Value value = this.mValues[i];
            final Unit unit = value.getUnit();
            final String label = this.getContext().getResources().getString(value.getLabel() != ResourcesCompat.ID_NULL ? value.getLabel() : R.string.edit_value);
            SingleValueInputView valueEdit = new SingleValueInputView(this.getContext());

            switch (unit.getUnitType()) {
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
                    break;
                case WEIGHT_OR_PERCENTAGE:
                    valueEdit.setUnits(new CharSequence[]{Unit.PERCENTAGE.toString(), Unit.KG.toString(), Unit.LBS.toString(), Unit.STONES.toString()});
                    break;
                case NONE:
                default:
                    valueEdit.setUnits(new CharSequence[]{Unit.UNITLESS.toString()});
                    valueEdit.showUnit(false);
            }
            valueEdit.setTitle(label);
            valueEdit.showUnit(true);
            valueEdit.setValue(String.format("%.1f", value.getValue()));
            valueEdit.setSelectedUnit(unit.toString());

            valueEdits[i] = valueEdit;
            valuesContainer.addView(valueEdit);
        }

        dateEdit.setValue(DateConverter.dateToLocalDateStr(mDate, getContext()));
        timeEdit.setValue(mTime);


        updateButton = findViewById(R.id.btn_update);
        if (!mUpdateText.isEmpty()) updateButton.setText(mUpdateText);
        cancelButton = findViewById(R.id.btn_cancel);

        updateButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        for(SingleValueInputView valueEdit: this.valueEdits){
            Keyboard.hide(getContext(), valueEdit);
        }
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

    /**
     * @return All of the currently set values. Empty values will be omitted, so this array could have less values than the input array
     */
    public Value[] getValues() {
        final Value[] returnValues = new Value[this.valueEdits.length];
        for(int i = 0; i < this.mValues.length; i++){
            final Value prevValue = this.mValues[i];
            final SingleValueInputView valueEdit = this.valueEdits[i];
            Float value = null;
            try {
                value = Float.parseFloat(valueEdit.getValue().replaceAll(",", "."));
            } catch(Exception ignored) {}
            returnValues[i] = new Value(
                    value,
                    Unit.fromString(valueEdit.getSelectedUnit()),
                    prevValue.getId(),
                    prevValue.getLabel());
        }
        return returnValues;
    }


    @Override
    public void setTitle(int title) {
        mTitle = getContext().getString(title);
    }

    public void setPositiveButton(int text) {
        mUpdateText = getContext().getString(text);
    }
}
