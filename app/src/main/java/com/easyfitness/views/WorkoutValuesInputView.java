package com.easyfitness.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.DistanceUnit;
import com.easyfitness.utils.ExerciseType;
import com.easyfitness.utils.WeightUnit;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

public class WorkoutValuesInputView extends LinearLayout {

    private boolean mShowExerciseTypeSelector;
    private ExerciseType mSelectedType;

    private View rootView;
    // Selection part
    private LinearLayoutCompat exerciseTypeSelectorLayout = null;
    private TextView strenghSelector = null;
    private TextView cardioSelector = null;
    private TextView isometricSelector = null;

    private SingleValueInputView setsInputView;
    private SingleValueInputView repsInputView;
    private SingleValueInputView weightInputView;
    private SingleValueInputView secondsInputView;
    private SingleValueInputView distanceInputView;
    private SingleValueInputView durationInputView;

    public WorkoutValuesInputView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public WorkoutValuesInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WorkoutValuesInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        rootView = inflate(context, R.layout.workoutvaluesinput_view, this);

        strenghSelector = rootView.findViewById(R.id.StrenghSelector);
        cardioSelector = rootView.findViewById(R.id.CardioSelector);
        isometricSelector = rootView.findViewById(R.id.IsometricSelector);
        exerciseTypeSelectorLayout = rootView.findViewById(R.id.ExerciseTypeSelectorLayout);

        setsInputView  = rootView.findViewById(R.id.SetsInputView);
        repsInputView = rootView.findViewById(R.id.RepsInputView);
        weightInputView = rootView.findViewById(R.id.WeightInputView);
        secondsInputView = rootView.findViewById(R.id.SecondsInputView);
        distanceInputView = rootView.findViewById(R.id.DistanceInputView);
        durationInputView = rootView.findViewById(R.id.DurationInputView);

        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.WorkoutValuesInputView,
            0, 0);

        try {
            mShowExerciseTypeSelector = a.getBoolean(R.styleable.WorkoutValuesInputView_showTypeSelector, false);
            setShowExerciseTypeSelector(mShowExerciseTypeSelector);
            mSelectedType = ExerciseType.fromInteger(a.getInteger(R.styleable.WorkoutValuesInputView_selectedType, 0));
            setSelectedType(mSelectedType);
        } finally {
            a.recycle();
        }

        // Events
        strenghSelector.setOnClickListener(clickExerciseTypeSelector);
        cardioSelector.setOnClickListener(clickExerciseTypeSelector);
        isometricSelector.setOnClickListener(clickExerciseTypeSelector);
    }

    public boolean isShowExerciseTypeSelector() {
        return mShowExerciseTypeSelector;
    }

    public void setShowExerciseTypeSelector(boolean showTypeSelector) {
        mShowExerciseTypeSelector = showTypeSelector;
        if (!mShowExerciseTypeSelector)
            exerciseTypeSelectorLayout.setVisibility(View.GONE);
        else
            exerciseTypeSelectorLayout.setVisibility(View.VISIBLE);
        invalidate();
        requestLayout();
    }

    public ExerciseType getSelectedType() {
        return mSelectedType;
    }

    public void setSelectedType(ExerciseType selectedType) {
        mSelectedType = selectedType;

        switch (mSelectedType) {
            case CARDIO:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
                strenghSelector.setBackgroundColor(getResources().getColor(R.color.background));
                isometricSelector.setBackgroundColor(getResources().getColor(R.color.background));
                setsInputView.setVisibility(View.GONE);
                repsInputView.setVisibility(View.GONE);
                weightInputView.setVisibility(View.GONE);
                secondsInputView.setVisibility(View.GONE);
                distanceInputView.setVisibility(View.VISIBLE);
                durationInputView.setVisibility(View.VISIBLE);
                break;
            case STRENGTH:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.background));
                strenghSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
                isometricSelector.setBackgroundColor(getResources().getColor(R.color.background));
                setsInputView.setVisibility(View.VISIBLE);
                repsInputView.setVisibility(View.VISIBLE);
                weightInputView.setVisibility(View.VISIBLE);
                secondsInputView.setVisibility(View.GONE);
                distanceInputView.setVisibility(View.GONE);
                durationInputView.setVisibility(View.GONE);
                break;
            case ISOMETRIC:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.background));
                strenghSelector.setBackgroundColor(getResources().getColor(R.color.background));
                isometricSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
                setsInputView.setVisibility(View.VISIBLE);
                repsInputView.setVisibility(View.GONE);
                weightInputView.setVisibility(View.VISIBLE);
                secondsInputView.setVisibility(View.VISIBLE);
                distanceInputView.setVisibility(View.GONE);
                durationInputView.setVisibility(View.GONE);
                break;
        }

        invalidate();
        requestLayout();
    }

    public int getSets() {
        return Integer.parseInt(setsInputView.getValue());
    }

    public int getReps() {
        return Integer.parseInt(repsInputView.getValue());
    }

    public int getSeconds() {
        return Integer.parseInt(repsInputView.getValue());
    }

    public float getWeightValue() {
        return Float.parseFloat(weightInputView.getValue().replaceAll(",", "."));
    }

    public WeightUnit getWeightUnit() {
        return WeightUnit.fromString(weightInputView.getSelectedUnit());
    }

    public long getDurationValue() {
        long duration;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date tmpDate = dateFormat.parse(durationInputView.getValue());
            duration = tmpDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            duration = 0;
        }
        return duration;
    }

    public float getDistanceValue() {
        return Float.parseFloat(distanceInputView.getValue().toString().replaceAll(",", "."));
    }

    public DistanceUnit getDistanceUnit() {
        return DistanceUnit.fromString(distanceInputView.getSelectedUnit());
    }

    public void setSets(int sets) {
        setsInputView.setValue(String.valueOf(sets));
        invalidate();
        requestLayout();
    }

    public void setReps(int reps) {
        repsInputView.setValue(String.valueOf(reps));
        invalidate();
        requestLayout();
    }

    public void setSeconds(int seconds) {
        secondsInputView.setValue(String.valueOf(seconds));
        invalidate();
        requestLayout();
    }

    public void setWeight(float weight, WeightUnit unit) {
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        weightInputView.setValue(numberFormat.format(weight));
        weightInputView.setSelectedUnit(unit.toString());
        invalidate();
        requestLayout();
    }

    public void setDistance(float distance, DistanceUnit unit) {
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        distanceInputView.setValue(numberFormat.format(distance));
        distanceInputView.setSelectedUnit(unit.toString());
        invalidate();
        requestLayout();
    }

    public void setDuration(long duration) {
        durationInputView.setValue(DateConverter.durationToHoursMinutesSecondsStr(duration));
        invalidate();
        requestLayout();
    }

    private OnClickListener clickExerciseTypeSelector = v -> {
        switch (v.getId()) {
            case R.id.IsometricSelector:
                setSelectedType(ExerciseType.ISOMETRIC);
                break;
            case R.id.CardioSelector:
                setSelectedType(ExerciseType.CARDIO);
                break;
            case R.id.StrenghSelector:
            default:
                setSelectedType(ExerciseType.STRENGTH);
                break;
        }
    };

    public boolean isFilled() {
        switch (mSelectedType) {
            case CARDIO:
                return !durationInputView.isEmpty() && !distanceInputView.isEmpty();
            case STRENGTH:
                return !setsInputView.isEmpty() && !repsInputView.isEmpty() && !weightInputView.isEmpty();
            case ISOMETRIC:
                return !setsInputView.isEmpty() && !secondsInputView.isEmpty() && !weightInputView.isEmpty();
            default:
                return false;
        }
    }

    public void setWeightComment(String s) {
        weightInputView.setComment(s);
    }

    public void setWeightUnit(WeightUnit unit) {
        weightInputView.setSelectedUnit(unit.toString());
    }

    public void setDurationUnit(DistanceUnit unit) {
        distanceInputView.setSelectedUnit(unit.toString());
    }
}
