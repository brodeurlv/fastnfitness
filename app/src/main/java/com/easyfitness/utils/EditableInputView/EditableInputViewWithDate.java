package com.easyfitness.utils.EditableInputView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
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
        if (mConfirmClickListener != null)
            mConfirmClickListener.onTextChanged(EditableInputViewWithDate.this);
    }

    private EditableInputViewWithDate getEditableInputViewWithDate() {
        return this;
    }

    @Override
    protected void editDialog(Context context) {
        final TextView editDate = new TextView(getContext());
        date = DateConverter.getNewDate();
        editDate.setText(DateConverter.dateToLocalDateStr(date, getContext()));
        editDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editDate.setGravity(Gravity.CENTER);
        editDate.setOnClickListener((view) -> {
                Calendar calendar = Calendar.getInstance();

                calendar.setTime(DateConverter.getNewDate());
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), getEditableInputViewWithDate(), year, month, day);
                dateEditView = editDate;
                datePickerDialog.show();
        });

        final EditText editText = new EditText(getContext());
        if (getText().contentEquals("-")) {
            editText.setText("");
            editText.setHint("Enter date and value here");
        } else {
            editText.setText(getText());
        }
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setGravity(Gravity.CENTER);
        editText.requestFocus();
        editText.selectAll();

        LinearLayout linearLayout = new LinearLayout(getContext().getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editDate);
        linearLayout.addView(editText);

        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(getContext().getString(R.string.edit_value))
            .showCancelButton(true)
            .setCancelText(getContext().getString(R.string.global_cancel))
            .setConfirmText(getContext().getString(R.string.AddLabel))
            .setConfirmClickListener(sDialog -> {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                setText(editText.getText().toString());
                if (mConfirmClickListener != null)
                    mConfirmClickListener.onTextChanged(EditableInputViewWithDate.this);
                sDialog.dismissWithAnimation();
            });
        dialog.setCustomView(linearLayout);
        dialog.show();
    }

    public Date getDate() {
        return date;
    }
}
