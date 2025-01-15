package com.easyfitness.views;

import android.content.Context;
import android.util.AttributeSet;
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

        // Events
        //strenghSelector.setOnClickListener(clickExerciseTypeSelector);
        //cardioSelector.setOnClickListener(clickExerciseTypeSelector);
        //isometricSelector.setOnClickListener(clickExerciseTypeSelector);

        //caloriesInputView.setOnKeyListener();
    }

    private float getFloatFromInputView(SingleValueInputView view) {
        return Float.parseFloat(view.getValue().replaceAll(",", "."));
    }

    private void setInputViewToFloat(SingleValueInputView view, float value) {
        view.setValue(String.format(Locale.getDefault(), "%f", value));
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
        // TODO: Implement this
    }
}
