package com.easyfitness.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easyfitness.DAO.record.Record;
import com.easyfitness.R;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.UnitConverter;

import java.text.DecimalFormat;
import java.text.ParseException;

public class FoodWorkoutValuesInputView extends LinearLayout {

    private View rootView;
    // Selection part

    private SingleValueInputView quantityInputView;
    private SingleValueInputView caloriesInputView;
    private SingleValueInputView carbsInputView;
    private SingleValueInputView proteinInputView;
    private SingleValueInputView fatsInputView;

    public FoodWorkoutValuesInputView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public FoodWorkoutValuesInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FoodWorkoutValuesInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        rootView = inflate(context, R.layout.foodvaluesinput_view, this);

        caloriesInputView = rootView.findViewById(R.id.CaloriesInputView);
        carbsInputView = rootView.findViewById(R.id.CarbsInputView);
        proteinInputView = rootView.findViewById(R.id.ProteinInputView);
        fatsInputView = rootView.findViewById(R.id.FatInputView);
        quantityInputView = rootView.findViewById(R.id.QuantityInputView);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WorkoutValuesInputView,
                0, 0);

        try {
            mShowExerciseTypeSelector = a.getBoolean(R.styleable.WorkoutValuesInputView_showTypeSelector, false);
            setShowExerciseTypeSelector(mShowExerciseTypeSelector);
            mSelectedType = ExerciseType.fromInteger(a.getInteger(R.styleable.WorkoutValuesInputView_selectedType, 0));
            setSelectedType(mSelectedType);
            mShowRestTime = a.getBoolean(R.styleable.WorkoutValuesInputView_showRestTime, false);
            setShowRestTime(mShowRestTime);
        } finally {
            a.recycle();
        }

        // Events
        //strenghSelector.setOnClickListener(clickExerciseTypeSelector);
        //cardioSelector.setOnClickListener(clickExerciseTypeSelector);
        //isometricSelector.setOnClickListener(clickExerciseTypeSelector);
    }

    public int getSets() {
        return Integer.parseInt(setsInputView.getValue());
    }

    public void setSets(int sets) {
        setsInputView.setValue(String.valueOf(sets));
        invalidate();
        requestLayout();
    }

    public int getReps() {
        try {
            return Integer.parseInt(repsInputView.getValue());
        } catch (Exception e) {
            return 0;
        }
    }

    public void setReps(int reps) {
        repsInputView.setValue(String.valueOf(reps));
        invalidate();
        requestLayout();
    }

    public int getSeconds() {
        try {
            return Integer.parseInt(secondsInputView.getValue());
        } catch (Exception e) {
            return 0;
        }
    }

    public void setSeconds(int seconds) {
        secondsInputView.setValue(String.valueOf(seconds));
        invalidate();
        requestLayout();
    }

    public float getWeightValue() {
        try {
            return Float.parseFloat(weightInputView.getValue().replaceAll(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    public WeightUnit getWeightUnit() {
        return WeightUnit.fromString(weightInputView.getSelectedUnit());
    }

    public void setWeightUnit(WeightUnit unit) {
        weightInputView.setSelectedUnit(unit.toString());
    }


    public void setWeight(float weight, WeightUnit unit) {
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        weightInputView.setValue(numberFormat.format(weight));
        weightInputView.setSelectedUnit(unit.toString());
        invalidate();
        requestLayout();
    }

    public boolean isFilled() {
        return !(quantityInputView.isEmpty() ||
                caloriesInputView.isEmpty() ||
                carbsInputView.isEmpty() ||
                fatsInputView.isEmpty() ||
                proteinInputView.isEmpty());
    }

    public void setDurationUnit(DistanceUnit unit) {
        distanceInputView.setSelectedUnit(unit.toString());
    }

    public void setShowRestTime(boolean isShown) {
        mShowRestTime = isShown;
        if (isShown) restTimeCardView.setVisibility(VISIBLE);
        else restTimeCardView.setVisibility(GONE);
    }

    public void setRecord(Record record) {
        // TODO: Implement this
    }
}
