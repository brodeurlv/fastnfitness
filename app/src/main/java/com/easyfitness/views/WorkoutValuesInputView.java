package com.easyfitness.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.easyfitness.DAO.record.Record;
import com.easyfitness.R;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WorkoutValuesInputView extends LinearLayout {

    private boolean mShowExerciseTypeSelector;
    private boolean mShowRestTime;
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
    private final OnClickListener clickExerciseTypeSelector = v -> {
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
    private CardView restTimeCardView = null;
    private AppCompatEditText restTimeEditText;
    private AppCompatCheckBox restTimeCheckBox;

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

        setsInputView = rootView.findViewById(R.id.SetsInputView);
        repsInputView = rootView.findViewById(R.id.RepsInputView);
        weightInputView = rootView.findViewById(R.id.WeightInputView);
        secondsInputView = rootView.findViewById(R.id.SecondsInputView);
        distanceInputView = rootView.findViewById(R.id.DistanceInputView);
        durationInputView = rootView.findViewById(R.id.DurationInputView);

        restTimeCardView = rootView.findViewById(R.id.restTimeCardView);
        restTimeEditText = rootView.findViewById(R.id.restTimeEditText);
        restTimeCheckBox = rootView.findViewById(R.id.restTimeCheckBox);

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
        return Float.parseFloat(distanceInputView.getValue().replaceAll(",", "."));
    }

    public DistanceUnit getDistanceUnit() {
        return DistanceUnit.fromString(distanceInputView.getSelectedUnit());
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

    public void setDurationUnit(DistanceUnit unit) {
        distanceInputView.setSelectedUnit(unit.toString());
    }

    public void setShowRestTime(boolean isShown) {
        mShowRestTime = isShown;
        if (isShown) restTimeCardView.setVisibility(VISIBLE);
        else restTimeCardView.setVisibility(GONE);
    }

    public boolean isRestTimeActivated() {
        return restTimeCheckBox.isChecked();
    }

    public void activatedRestTime(boolean activated) {
        restTimeCheckBox.setChecked(activated);
    }

    public int getRestTime() {
        if (isRestTimeActivated())
            return Integer.parseInt(restTimeEditText.getText().toString());
        else
            return 0;
    }

    public void setRestTime(int unit) {
        restTimeEditText.setText(String.valueOf(unit));
    }

    public void setRecord(Record record) {
        setSelectedType(record.getExerciseType());
        activatedRestTime(record.getRestTime() != 0);
        setRestTime(record.getRestTime());
        switch (record.getExerciseType()) {
            case STRENGTH:
                setSets(record.getSets());
                setReps(record.getReps());
                setWeight(UnitConverter.weightConverter(record.getWeight(), WeightUnit.KG, record.getWeightUnit()), record.getWeightUnit());
                break;
            case ISOMETRIC:
                setSets(record.getSets());
                setSeconds(record.getSeconds());
                setWeight(UnitConverter.weightConverter(record.getWeight(), WeightUnit.KG, record.getWeightUnit()), record.getWeightUnit());
            case CARDIO:
                setDuration(record.getDuration());
                if (record.getDistanceUnit() == DistanceUnit.MILES)
                    setDistance(UnitConverter.KmToMiles(record.getDistance()), DistanceUnit.MILES);
                else
                    setDistance(record.getDistance(), DistanceUnit.KM);
        }
    }
}
