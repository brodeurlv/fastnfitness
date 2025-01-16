package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.easyfitness.DAO.macros.DAOFoodRecord;
import com.easyfitness.DAO.macros.FoodRecord;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.views.FoodValuesInputView;
import com.easyfitness.views.WorkoutValuesInputView;

public class FoodRecordEditorDialogbox extends Dialog implements View.OnClickListener {

    private final Activity mActivity;
    private final FoodRecord mRecord;
    public Dialog d;
    private FoodValuesInputView mFoodValuesInput;
    private boolean mCancelled = false;

    public FoodRecordEditorDialogbox(Activity a, FoodRecord record) {
        super(a);
        this.mActivity = a;
        mRecord = record;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_food_record_editor);
        this.setCanceledOnTouchOutside(false);

        Button updateButton = findViewById(R.id.btn_update);
        Button cancelButton = findViewById(R.id.btn_cancel);
        mFoodValuesInput = findViewById(R.id.EditorFoodValuesInput);

        mFoodValuesInput.setRecord(mRecord);

        updateButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            mCancelled = true;
            cancel();
        } else if (v.getId() == R.id.btn_update) {
            // update record
            mCancelled = false;
            mRecord.setCalories(mFoodValuesInput.getCalories());
            mRecord.setCarbs(mFoodValuesInput.getCarbs());
            mRecord.setFats(mFoodValuesInput.getFat());
            mRecord.setProtein(mFoodValuesInput.getProtein());
            mRecord.setQuantity(mFoodValuesInput.getQuantity());
            mRecord.setQuantityUnit(mFoodValuesInput.getQuantityUnit());
            DAOFoodRecord daoFoodRecord = new DAOFoodRecord(getContext());
            daoFoodRecord.updateRecord(mRecord);
            dismiss();
        }
    }

    public boolean isCancelled() {
        return mCancelled;
    }

}
