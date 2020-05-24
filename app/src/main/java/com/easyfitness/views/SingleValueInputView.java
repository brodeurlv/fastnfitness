package com.easyfitness.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyfitness.R;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;

public class SingleValueInputView extends LinearLayout {
    private View rootView;
    private TextView titleTextView;
    private AppCompatEditText valueEditText;
    private AppCompatSpinner unitSpinner;
    private LinearLayout commentLayout;
    private TextView commentTextView;
    private CharSequence[] mUnits;

    private boolean mShowUnit;
    private String mTitle;
    private boolean mShowComment;
    private String mComment;
    private String mValue;
    private int mType;
    private boolean mIsTimePickerShown = false;

    public SingleValueInputView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public SingleValueInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SingleValueInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        rootView = inflate(context, R.layout.singlevalueinput_view, this);
        titleTextView = rootView.findViewById(R.id.singlevalueinput_title);
        valueEditText = rootView.findViewById(R.id.singlevalueinput_value);
        unitSpinner = rootView.findViewById(R.id.singlevalueinput_unitSpinner);
        commentTextView = rootView.findViewById(R.id.singlevalueinput_comment);
        commentLayout = rootView.findViewById(R.id.singlevalueinput_commentLayout);

        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.SingleValueInputView,
            0, 0);

        try {
            mShowUnit = a.getBoolean(R.styleable.SingleValueInputView_showUnit, false);
            setShowUnit(mShowUnit);
            mShowComment = a.getBoolean(R.styleable.SingleValueInputView_showComment, false);
            setShowComment(mShowComment);
            mTitle = a.getString(R.styleable.SingleValueInputView_title);
            setTitle(mTitle);
            mComment = a.getString(R.styleable.SingleValueInputView_comment);
            setComment(mComment);
            mValue = a.getString(R.styleable.SingleValueInputView_value);
            setValue(mValue);
            mType = a.getInteger(R.styleable.SingleValueInputView_type, 0);
            setType(mType);
            CharSequence[] entries = a.getTextArray(R.styleable.SingleValueInputView_units);
            if (entries != null)
            {
                setUnits(entries);
            }
        } finally {
            a.recycle();
        }
    }

    public boolean isShowUnit() {
        return mShowUnit;
    }

    public void setShowUnit(boolean showUnit) {
        mShowUnit = showUnit;
        if (!mShowUnit)
            unitSpinner.setVisibility(View.GONE);
        else
            unitSpinner.setVisibility(View.VISIBLE);
        invalidate();
        requestLayout();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        titleTextView.setText(mTitle);
        invalidate();
        requestLayout();
    }

    public boolean isShowComment() {
        return mShowComment;
    }

    public void setShowComment(boolean showComment) {
        mShowComment = showComment;
        if (!mShowComment)
            commentLayout.setVisibility(View.GONE);
        else
            commentLayout.setVisibility(View.VISIBLE);
        invalidate();
        requestLayout();
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
        commentTextView.setText(mComment);
        if (comment==null || comment.isEmpty()) {
            commentLayout.setVisibility(View.GONE);
        } else {
            commentLayout.setVisibility(View.VISIBLE);
        }
        invalidate();
        requestLayout();
    }

    public String getValue() {
        return valueEditText.getText().toString();
    }

    public void setValue(String value) {
        mValue = value;
        valueEditText.setText(mValue);
        invalidate();
        requestLayout();
    }

    public CharSequence[] getUnits() {
        return mUnits;
    }

    public String getSelectedUnit() {
        return unitSpinner.getSelectedItem().toString();
    }

    public void setSelectedUnit(String selectedUnit) {
        ArrayAdapter arrayAdapter = (ArrayAdapter)unitSpinner.getAdapter();
        if (arrayAdapter!=null)
            unitSpinner.setSelection(arrayAdapter.getPosition(selectedUnit));
    }

    public void setSelectedUnit(int selectedUnit) {
        unitSpinner.setSelection(selectedUnit);
    }

    public void setUnits(CharSequence[] value) {
        mUnits = value;
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(unitSpinner.getContext(), android.R.layout.simple_spinner_item, mUnits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);
        invalidate();
        requestLayout();
    }

    public boolean isEmpty() {
        return valueEditText.getText().toString().isEmpty();
    }

    public int getType() {
        return mType;
    }

    public void setType(int value) {
        mType = value;

        if (value == 3) { // time
            valueEditText.setFocusable(false);
            valueEditText.setOnClickListener(v -> {
                if (mIsTimePickerShown) return;
                String tx = valueEditText.getText().toString();
                int hour;
                try {
                    hour = Integer.parseInt(tx.substring(0, 2));
                } catch (Exception e) {
                    hour = 0;
                }
                int minute;
                try {
                    minute = Integer.parseInt(tx.substring(3, 5));
                } catch (Exception e) {
                    minute = 0;
                }
                int seconds;
                try {
                    seconds = Integer.parseInt(tx.substring(6));
                } catch (Exception e) {
                    seconds = 0;
                }

                MyTimePickerDialog mTimePicker;
                mTimePicker = new MyTimePickerDialog(this.getContext(), (timePicker, selectedHour, selectedMinute, selectedSeconds) -> {
                    String strMinute = "00";
                    String strHour = "00";
                    String strSecond = "00";

                    if (selectedHour < 10) strHour = "0" + selectedHour;
                    else strHour = Integer.toString(selectedHour);
                    if (selectedMinute < 10) strMinute = "0" + selectedMinute;
                    else strMinute = Integer.toString(selectedMinute);
                    if (selectedSeconds < 10) strSecond = "0" + selectedSeconds;
                    else strSecond = Integer.toString(selectedSeconds);

                    valueEditText.setText(strHour + ":" + strMinute + ":" + strSecond);
                }, hour, minute, seconds, true);//Yes 24 hour time


                mTimePicker.setOnDismissListener(dialog -> mIsTimePickerShown = false);
                mTimePicker.setTitle("Select Time");
                mIsTimePickerShown = true;
                mTimePicker.show();
            });
        } else {
            valueEditText.setFocusable(true);
            valueEditText.setOnClickListener(null);
        }
    }
}
