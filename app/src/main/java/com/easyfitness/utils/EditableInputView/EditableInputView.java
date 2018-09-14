package com.easyfitness.utils.EditableInputView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;

import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditableInputView extends RelativeLayout implements DatePickerDialog.OnDateSetListener {
    View rootView;
    TextView valueTextView;
    View editButton;

    String mAttrsText;

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        valueTextView.setText(DateConverter.dateToLocalDateStr(year, month, dayOfMonth, getContext()));
        if (mConfirmClickListener != null)
            mConfirmClickListener.onTextChanged(EditableInputView.this);
    }

    private EditableInputView.OnTextChangedListener mConfirmClickListener;


    public interface OnTextChangedListener {
        void onTextChanged(EditableInputView view);
    }

    public EditableInputView(Context context) {
        super(context);
        init(context, null);
    }

    public EditableInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditableInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //do setup work here

        rootView = inflate(context, R.layout.editableinput_view, this);
        valueTextView = (TextView) rootView.findViewById(R.id.valueTextView);

        editButton = rootView.findViewById(R.id.editButton);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.editableinput_view,
                    0, 0);
            try {
                valueTextView.setText(a.getString(R.styleable.editableinput_view_android_text));
                valueTextView.setGravity(a.getInt(R.styleable.editableinput_view_android_gravity, 0));
                valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimension(R.styleable.editableinput_view_android_textSize, 0));
                valueTextView.setMaxLines(a.getInt(R.styleable.editableinput_view_android_maxLines, 1));
                valueTextView.setLines(a.getInt(R.styleable.editableinput_view_android_lines, 1));
                valueTextView.setInputType(a.getInt(R.styleable.editableinput_view_android_inputType, 0));
            } finally {
                a.recycle();
            }
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialog(v.getContext()); //we'll define this method later
            }
        });
    }


    private void editDialog(Context context) {
        final EditText editText = new EditText(context);
        editText.setText(getText());

        LinearLayout linearLayout = new LinearLayout(context.getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText);

        if ((valueTextView.getInputType() & InputType.TYPE_DATETIME_VARIATION_DATE) > 0) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(DateConverter.localDateStrToDate(editText.getText().toString(), getContext()));
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(), this, year, month, day);
            datePickerDialog.show();
        } else {
            final SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getContext().getString(R.string.edit_value))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                            setText(editText.getText().toString());
                            if (mConfirmClickListener != null)
                                mConfirmClickListener.onTextChanged(EditableInputView.this);
                            sDialog.dismissWithAnimation();
                        }
                    });
            dialog.setCustomView(linearLayout);
            dialog.show();
        }
    }

    public String getText() {
        return valueTextView.getText().toString();
    }

    public void setText(String newValue) {
        valueTextView.setText(newValue);
    }

    public void setHint(String newValue) {
        valueTextView.setHint(newValue);
    }

    public TextView getTextView() {
        return valueTextView;
    }

    public void setOnTextChangeListener(EditableInputView.OnTextChangedListener listener) {
        mConfirmClickListener = listener;
    }

}