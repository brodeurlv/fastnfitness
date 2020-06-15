package com.easyfitness.DAO;

import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.UnitConverter;

import java.text.DecimalFormat;

public class Weight {
    private float pWeight;
    private WeightUnit pUnit;

    public Weight(float weight, WeightUnit unit) {
        pWeight = weight;
        pUnit = unit;
    }

    public float getStoredWeight() {
        return pWeight;
    }

    public float getWeight(WeightUnit unit) {
        float weight = pWeight;
        if (unit == WeightUnit.LBS) {
            weight = UnitConverter.KgtoLbs(pWeight);
        }
        return weight;
    }

    public WeightUnit getStoredUnit() {
        return pUnit;
    }

    public String toString() {
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        return numberFormat.format(pWeight);
    }

    public String getWeightStr(WeightUnit unit) {
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        return numberFormat.format(getWeight(unit));
    }
}
