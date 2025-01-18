package com.easyfitness.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easyfitness.DAO.macros.FoodRecord;
import com.easyfitness.R;
import com.easyfitness.enums.FoodQuantityUnit;
import java.util.Locale;

public class FoodValuesInputView extends LinearLayout {

    private View rootView;

    // Selection part
    private SingleValueInputView quantityInputView;
    private SingleValueInputView caloriesInputView;
    private SingleValueInputView carbsInputView;
    private SingleValueInputView proteinInputView;
    private SingleValueInputView fatsInputView;

    private float lockedQuantity;
    private float lockedCalories;
    private float lockedCarbs;
    private float lockedProtein;
    private float lockedFats;

    private boolean lockRatio = false;

    public FoodValuesInputView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public FoodValuesInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FoodValuesInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        lockRatio = false;
        lockedQuantity = 0.0f;
        lockedCalories = 0.0f;
        lockedCarbs = 0.0f;
        lockedProtein = 0.0f;
        lockedFats = 0.0f;

        // Events

        // If the ratio lock is enabled, then whenever the user changes the quantity, change the other values.
        quantityInputView.setOnKeyListener((v, keyCode, event) -> {
            if (!lockRatio) {
                return false;
            }
            float newQuantity = getQuantity();

            float ratio = newQuantity /  lockedQuantity;
            setFat(lockedFats * ratio);
            setCarbs(lockedCarbs * ratio);
            setProtein(lockedProtein * ratio);
            setCalories(lockedCalories * ratio);

            // Return false to signal that the event was
            // not consumed, this allows the edit text to update it's value still
            return false;
        });

        // If the ratio lock is on, but the user begins editing values other than quantity, then cancel the ratio lock
        for (SingleValueInputView v : new SingleValueInputView[]{carbsInputView, caloriesInputView, fatsInputView, proteinInputView}) {
            v.setOnKeyListener((v1, keyCode, event) -> {
                if (lockRatio) {
                    lockRatio = false;
                }
                return false;
            });
        }

        // If the ratio lock is on, but the user changes the units of the quantity, then cancel the lock
        quantityInputView.setOnUnitSelectedListener(new UnitListener());

    }

    public boolean isRatioLocked() {
        return lockRatio;
    }

    public void setRatioLock(boolean locked) {
        if (!lockRatio && locked) {
            lockedQuantity = getQuantity();
            lockedFats = getFat();
            lockedCalories = getCalories();
            lockedCarbs = getCarbs();
            lockedProtein = getProtein();
        }
        lockRatio = locked;
    }

    public void reset() {
        setCalories(0);
        setFat(0);
        setCarbs(0);
        setProtein(0);
        lockRatio = false;
        setQuantity(0, FoodQuantityUnit.SERVINGS);
    }

    private float getFloatFromInputView(SingleValueInputView view) {
        String s = view.getValue().replaceAll(",", ".");
        if (s.isEmpty()) {
            return 0.0f;
        }
        return Float.parseFloat(s);
    }

    private void setInputViewToFloat(SingleValueInputView view, float value) {
        view.setValue(String.format(Locale.getDefault(), "%.1f", value));
    }

    public void setQuantity(float quantity, FoodQuantityUnit unit) {
        setInputViewToFloat(quantityInputView, quantity);
        quantityInputView.setSelectedUnit(unit.toString());
    }

    public float getQuantity() {
        return getFloatFromInputView(quantityInputView);
    }

    public void setQuantityUnit(FoodQuantityUnit unit) {
        quantityInputView.setSelectedUnit(unit.toString());
    }

    public FoodQuantityUnit getQuantityUnit() {
        return FoodQuantityUnit.fromString(quantityInputView.getSelectedUnit());
    }

    public void setCalories(float calories) {
        setInputViewToFloat(caloriesInputView, calories);
    }

    public float getCalories() {
        return getFloatFromInputView(caloriesInputView);
    }

    public void setCarbs(float carbs) {
        setInputViewToFloat(carbsInputView, carbs);
    }

    public float getCarbs() {
        return getFloatFromInputView(carbsInputView);
    }
    public void setProtein(float protein) {
        setInputViewToFloat(proteinInputView, protein);
    }

    public float getProtein() {
        return getFloatFromInputView(proteinInputView);
    }
    public void setFat(float fat) {
        setInputViewToFloat(fatsInputView, fat);
    }

    public float getFat() {
        return getFloatFromInputView(fatsInputView);
    }
    public boolean isFilled() {
        return !(quantityInputView.isEmpty() ||
                caloriesInputView.isEmpty() ||
                carbsInputView.isEmpty() ||
                fatsInputView.isEmpty() ||
                proteinInputView.isEmpty());
    }

    public void setRecord(FoodRecord record) {
        setCalories(record.getCalories());
        setFat(record.getFats());
        setCarbs(record.getCarbs());
        setProtein(record.getProtein());
        setQuantity(record.getQuantity(), record.getQuantityUnit());
    }

    private class UnitListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
        int pos, long id) {
            if (lockRatio) {
                lockRatio = false;
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    }
}
