package com.easyfitness.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyfitness.R;

import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SweetAlertDialogWithDate extends SweetAlertDialog implements DatePickerDialog.OnDateSetListener {
    private Date date;
    private TextView dateEditView = null;
    private EditText editText = null;
    private LinearLayout linearLayout = null;

    private View view = null;
    private ViewGroup viewGroup = null;

    public SweetAlertDialogWithDate(Context context) {
        super(context);
    }

    public SweetAlertDialogWithDate(Context context, int alertType) {
        super(context, alertType);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        super.setCustomView(linearLayout);
    }

    private void init() {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );

        dateEditView = new TextView(getContext().getApplicationContext());

        date = DateConverter.getNewDate();
        dateEditView.setText(DateConverter.dateToLocalDateStr(date, getContext()));
        dateEditView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        dateEditView.setGravity(Gravity.CENTER);
        dateEditView.setLayoutParams(params);
        dateEditView.setOnClickListener((view) -> {
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(DateConverter.getNewDate());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this, year, month, day);
            datePickerDialog.show();
        });

        //editText = view.findViewById(R.id.valueEditText);
        editText = new EditText(getContext().getApplicationContext());
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        //editText.setTextColor(getContext().getColor(R.color.text_color));
        editText.setText("");
        editText.setHint("Enter value here");
        editText.setLayoutParams(params);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setGravity(Gravity.CENTER);

        editText.requestFocus();
        editText.selectAll();

        linearLayout = new LinearLayout(getContext().getApplicationContext());
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(dateEditView);
        linearLayout.addView(editText);
    }

    public Date GetDate() {
        return date;
    }

    public String GetDateString() {
        return dateEditView.getText().toString();
    }

    public String GetText() {
        return editText.getText().toString();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = DateConverter.dateToDate(year, month, dayOfMonth);
        if (dateEditView != null)
            dateEditView.setText(DateConverter.dateToLocalDateStr(date, getContext().getApplicationContext()));
    }
}
