package com.easyfitness.views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;

import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditableInputViewWithDate extends EditableInputView implements DatePickerDialog.OnDateSetListener {
    private Date date;
    private TextView dateEditView = null;

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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = DateConverter.dateToDate(year, month, dayOfMonth);
        if (dateEditView != null)
            dateEditView.setText(DateConverter.dateToLocalDateStr(date, getContext()));
    }

    private EditableInputViewWithDate getEditableInputViewWithDate() {
        return this;
    }

    @Override
    protected void editDialog(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        final TextView editDate = new TextView(getContext());
        date = DateConverter.getNewDate();
        editDate.setLayoutParams(params);
        editDate.setText(DateConverter.dateToLocalDateStr(date, getContext()));
        editDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editDate.setGravity(Gravity.CENTER);
        editDate.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(DateConverter.getNewDate());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), getEditableInputViewWithDate(), year, month, day);
            dateEditView = editDate;
            datePickerDialog.show();
        });

        final EditText editText = new EditText(context);
        if (getText().contentEquals("-")) {
            editText.setText("");
            editText.setHint("Enter value here");
        } else {
            editText.setText(getText());
        }
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(params);
        editText.requestFocus();
        editText.selectAll();

        LinearLayout linearLayout = new LinearLayout(getContext().getApplicationContext());

        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editDate);
        linearLayout.addView(editText);

        final SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(mTitle)
                .showCancelButton(true)
                .setCancelClickListener(sDialog -> {
                    editText.clearFocus();
                    Keyboard.hide(context, editText);
                    sDialog.dismissWithAnimation();
                })
                .setCancelText(getContext().getString(android.R.string.cancel))
                .setConfirmText(getContext().getString(R.string.AddLabel))
                .setConfirmClickListener(sDialog -> {
                    Keyboard.hide(sDialog.getContext(), editText);
                    setText(editText.getText().toString());
                    if (mConfirmClickListener != null)
                        mConfirmClickListener.onTextChanged(EditableInputViewWithDate.this);
                    sDialog.dismissWithAnimation();
                });
        dialog.setCustomView(linearLayout);
        dialog.setOnShowListener(sDialog -> Keyboard.show(getContext(), editText));
        dialog.show();
    }

    public Date getDate() {
        return date;
    }
}
