package com.easyfitness.utils;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.easyfitness.enums.Unit;

public class Value {
    @Nullable private Float mValue;
    private Unit mUnit;
    /** Identifier for this value so we can later differentiate between values */
    private final String mId;
    private final int mLabel;

    /**
     * Create a value
     * @param value Actual value
     * @param unit Unit of the value
     * @param id Custom identifier to differentiate between values
     * @param label String resource to label this value is described by
     */
    public Value(@Nullable Float value, Unit unit, @Nullable String id, int label){
        this.mValue = value;
        this.mUnit = unit;
        this.mId = id;
        this.mLabel = label;
    }
    /**
     * Create a value
     * @param value Actual value
     * @param unit Unit of the value
     * @param id Custom identifier to differentiate between values
     */
    public Value(@Nullable Float value, Unit unit, @Nullable String id){
        this(value, unit, id, ResourcesCompat.ID_NULL);
    }
    /**
     * Create a value
     * @param value Actual value
     * @param unit Unit of the value
     * @param label String resource to label this value is described by
     */
    public Value(@Nullable Float value, Unit unit, int label){
        this(value, unit, null, label);
    }
    /**
     * Create a value
     * @param value Actual value
     * @param unit Unit of the value
     */
    public Value(@Nullable Float value, Unit unit){
        this(value, unit, null, ResourcesCompat.ID_NULL);
    }

    public @Nullable Float getValue() {
        return this.mValue;
    }
    public void setValue(float value) {
        this.mValue = value;
    }
    public Unit getUnit(){
        return this.mUnit;
    }
    public void setUnit(Unit unit){
        this.mUnit = unit;
    }
    @Nullable public String getId(){
        return this.mId;
    }
    public int getLabel(){
        return this.mLabel;
    }
}
