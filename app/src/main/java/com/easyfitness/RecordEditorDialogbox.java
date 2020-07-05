package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.views.WorkoutValuesInputView;

public class RecordEditorDialogbox extends Dialog implements View.OnClickListener {

    private final boolean mShowRestTime;
    private Activity mActivity;
    public Dialog d;
    private Button cancelButton;
    private Button updateButton;
    private WorkoutValuesInputView mWorkoutValuesInput;
    private Record mRecord;

    public RecordEditorDialogbox(Activity a, Record record) {
        super(a);
        this.mActivity = a;
        mRecord=record;
        mShowRestTime=false;
    }

    public RecordEditorDialogbox(Activity a, Record record, boolean showRestTime) {
        super(a);
        this.mActivity = a;
        mRecord=record;
        mShowRestTime = showRestTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record_editor);
        this.setCanceledOnTouchOutside(false);

        updateButton = findViewById(R.id.btn_update);
        cancelButton = findViewById(R.id.btn_cancel);
        mWorkoutValuesInput = findViewById(R.id.EditorWorkoutValuesInput);

        mWorkoutValuesInput.setRecord(mRecord);

        mWorkoutValuesInput.setShowRestTime(mShowRestTime);

        updateButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            cancel();
        } else if (v.getId() == R.id.btn_update) {
            // update record
            DAORecord daoRecord = new DAORecord(mActivity.getBaseContext());
            switch (mRecord.getExerciseType()) {
                case CARDIO:
                    float distance = mWorkoutValuesInput.getDistanceValue();
                    if (mWorkoutValuesInput.getDistanceUnit()== DistanceUnit.MILES) {
                        distance = UnitConverter.MilesToKm(distance); // Always convert to KG
                    }
                    mRecord.setDuration(mWorkoutValuesInput.getDurationValue());
                    mRecord.setDistance(distance);
                    mRecord.setDistanceUnit(mWorkoutValuesInput.getDistanceUnit());
                    break;
                case ISOMETRIC:
                    float tmpPoids = mWorkoutValuesInput.getWeightValue();
                    if (mWorkoutValuesInput.getWeightUnit()== WeightUnit.LBS) {
                        tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
                    }
                    mRecord.setSets(mWorkoutValuesInput.getSets());
                    mRecord.setSeconds(mWorkoutValuesInput.getSeconds());
                    mRecord.setWeight(tmpPoids);
                    mRecord.setWeightUnit(mWorkoutValuesInput.getWeightUnit());
                    break;
                case STRENGTH:
                    float tmpWeight = mWorkoutValuesInput.getWeightValue();
                    if (mWorkoutValuesInput.getWeightUnit()== WeightUnit.LBS) {
                        tmpWeight = UnitConverter.LbstoKg(tmpWeight); // Always convert to KG
                    }
                    mRecord.setSets(mWorkoutValuesInput.getSets());
                    mRecord.setReps(mWorkoutValuesInput.getReps());
                    mRecord.setWeight(tmpWeight);
                    mRecord.setWeightUnit(mWorkoutValuesInput.getWeightUnit());
                    break;
            }
            if (mShowRestTime) {
                if (mWorkoutValuesInput.isRestTimeActivated()) {
                    mRecord.setRestTime(mWorkoutValuesInput.getRestTime());
                } else {
                    mRecord.setRestTime(0);
                }
            }
            daoRecord.updateRecord(mRecord);
            dismiss();
        }
    }

}
