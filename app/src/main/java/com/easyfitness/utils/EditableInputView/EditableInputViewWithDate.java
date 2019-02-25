package com.easyfitness.utils.EditableInputView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;

import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditableInputViewWithDate extends EditableInputView implements DatePickerDialog.OnDateSetListener {
    private Date date;
    private EditText dateEditView=null;

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = DateConverter.dateToDate(year, month, dayOfMonth);
        if (dateEditView!=null) dateEditView.setText(DateConverter.dateToLocalDateStr(date, getContext()));
        if (mConfirmClickListener != null)
            mConfirmClickListener.onTextChanged(EditableInputViewWithDate.this);
    }

    public EditableInputViewWithDate(Context context) {
        super(context);
        init(context, null);
    }

    public EditableInputViewWithDate(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditableInputViewWithDate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private EditableInputViewWithDate getEditableInputViewWithDate() {
        return this;
    }

    @Override
    protected void editDialog(Context context) {
        final EditText editDate = new EditText(getContext());
        date=DateConverter.getNewDate();
        editDate.setText(DateConverter.dateToLocalDateStr(date, getContext()));
        editDate.setGravity(Gravity.CENTER);
        editDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Calendar calendar = Calendar.getInstance();

                    calendar.setTime(DateConverter.getNewDate());
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    int year = calendar.get(Calendar.YEAR);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), getEditableInputViewWithDate(), year, month, day);
                    dateEditView = editDate;
                    datePickerDialog.show();
                }
            }
        });

        final EditText editText = new EditText(getContext());
        if (getText().contentEquals("-")) {
            editText.setText("");
            editText.setHint("Enter value here");
        } else {
            editText.setText(getText());
        }
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setGravity(Gravity.CENTER);
        editText.requestFocus();
        editText.selectAll();

        LinearLayout linearLayout = new LinearLayout(getContext().getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editDate);
        linearLayout.addView(editText);

        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getContext().getString(R.string.edit_value))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                        setText(editText.getText().toString());
                        if (mConfirmClickListener != null)
                            mConfirmClickListener.onTextChanged(EditableInputViewWithDate.this);
                        sDialog.dismissWithAnimation();
                    }
                });
        dialog.setCustomView(linearLayout);
        dialog.show();
    }

    public Date getDate() {
        return date;
    }
}