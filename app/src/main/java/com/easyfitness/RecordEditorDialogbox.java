package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.views.WorkoutValuesInputView;

public class RecordEditorDialogbox extends Dialog implements View.OnClickListener {

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(getContext().getResources().getString(R.string.ChronometerLabel)); //ChronometerLabel
        setContentView(R.layout.dialog_record_editor);
        this.setCanceledOnTouchOutside(false);

        updateButton = findViewById(R.id.btn_update);
        cancelButton = findViewById(R.id.btn_cancel);
        mWorkoutValuesInput = findViewById(R.id.EditorWorkoutValuesInput);

        mWorkoutValuesInput.setRecord(mRecord);

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
                    mRecord.setDuration(mWorkoutValuesInput.getDurationValue());
                    mRecord.setDistance(mWorkoutValuesInput.getDistanceValue());
                    mRecord.setDistanceUnit(mWorkoutValuesInput.getDistanceUnit());
                    break;
                case ISOMETRIC:
                    mRecord.setSets(mWorkoutValuesInput.getSets());
                    mRecord.setSeconds(mWorkoutValuesInput.getSeconds());
                    mRecord.setWeight(mWorkoutValuesInput.getWeightValue());
                    mRecord.setWeightUnit(mWorkoutValuesInput.getWeightUnit());
                    break;
                case STRENGTH:
                    mRecord.setSets(mWorkoutValuesInput.getSets());
                    mRecord.setReps(mWorkoutValuesInput.getReps());
                    mRecord.setWeight(mWorkoutValuesInput.getWeightValue());
                    mRecord.setWeightUnit(mWorkoutValuesInput.getWeightUnit());
                    break;
            }
            daoRecord.updateRecord(mRecord);
            dismiss();
        }
    }

}
